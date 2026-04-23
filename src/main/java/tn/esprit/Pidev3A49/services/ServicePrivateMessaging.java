package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.PrivateConversation;
import tn.esprit.Pidev3A49.Models.PrivateMessage;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServicePrivateMessaging {

    private static final String CONVERSATION_TABLE = "private_conversation";
    private static final String MESSAGE_TABLE = "private_message";

    private final Connection cnx;

    public ServicePrivateMessaging() {
        cnx = MyDataBase.getInstance().getCnx();
        ensureSchema();
    }

    public PrivateConversation getOrCreateConversation(int userA, int userB) {
        int low = Math.min(userA, userB);
        int high = Math.max(userA, userB);

        String select = "SELECT id, user_a_id, user_b_id, updated_at FROM " + CONVERSATION_TABLE + " WHERE user_a_id = ? AND user_b_id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(select)) {
            statement.setInt(1, low);
            statement.setInt(2, high);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapConversation(rs);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer la conversation privee.", exception);
        }

        String insert = "INSERT INTO " + CONVERSATION_TABLE + " (user_a_id, user_b_id, updated_at) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement statement = cnx.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, low);
            statement.setInt(2, high);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    PrivateConversation conversation = new PrivateConversation();
                    conversation.setId(keys.getInt(1));
                    conversation.setUserAId(low);
                    conversation.setUserBId(high);
                    conversation.setLastMessageAt(LocalDateTime.now());
                    return conversation;
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de creer la conversation privee.", exception);
        }
        throw new IllegalStateException("Creation de conversation impossible.");
    }

    public void sendMessage(int senderId, int receiverId, String content) {
        if (content == null || content.trim().isBlank()) {
            throw new IllegalArgumentException("Le message ne peut pas etre vide.");
        }
        PrivateConversation conversation = getOrCreateConversation(senderId, receiverId);
        String insert = "INSERT INTO " + MESSAGE_TABLE + " (conversation_id, sender_id, receiver_id, content, is_read, created_at) VALUES (?, ?, ?, ?, 0, CURRENT_TIMESTAMP)";
        try (PreparedStatement statement = cnx.prepareStatement(insert)) {
            statement.setInt(1, conversation.getId());
            statement.setInt(2, senderId);
            statement.setInt(3, receiverId);
            statement.setString(4, content.trim());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'envoyer le message.", exception);
        }
        touchConversation(conversation.getId());
    }

    public List<PrivateMessage> getMessages(int conversationId) {
        List<PrivateMessage> messages = new ArrayList<>();
        String query = "SELECT id, conversation_id, sender_id, receiver_id, content, is_read, created_at FROM " + MESSAGE_TABLE
                + " WHERE conversation_id = ? ORDER BY created_at ASC, id ASC";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, conversationId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapMessage(rs));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les messages.", exception);
        }
        return messages;
    }

    public List<PrivateConversation> getUserConversations(int userId) {
        List<PrivateConversation> conversations = new ArrayList<>();
        String query = """
                SELECT c.id,
                       c.user_a_id,
                       c.user_b_id,
                       (
                           SELECT pm.content
                           FROM %s pm
                           WHERE pm.conversation_id = c.id
                           ORDER BY pm.created_at DESC, pm.id DESC
                           LIMIT 1
                       ) AS last_message_preview,
                       (
                           SELECT pm.created_at
                           FROM %s pm
                           WHERE pm.conversation_id = c.id
                           ORDER BY pm.created_at DESC, pm.id DESC
                           LIMIT 1
                       ) AS last_message_at,
                       (
                           SELECT COUNT(*)
                           FROM %s pm
                           WHERE pm.conversation_id = c.id
                             AND pm.receiver_id = ?
                             AND pm.is_read = 0
                       ) AS unread_count
                FROM %s c
                WHERE c.user_a_id = ? OR c.user_b_id = ?
                ORDER BY COALESCE(
                    (
                        SELECT pm.created_at
                        FROM %s pm
                        WHERE pm.conversation_id = c.id
                        ORDER BY pm.created_at DESC, pm.id DESC
                        LIMIT 1
                    ),
                    c.updated_at
                ) DESC, c.id DESC
                """.formatted(MESSAGE_TABLE, MESSAGE_TABLE, MESSAGE_TABLE, CONVERSATION_TABLE, MESSAGE_TABLE);

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            statement.setInt(3, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    PrivateConversation conversation = mapConversation(rs);
                    conversation.setLastMessagePreview(rs.getString("last_message_preview"));
                    Timestamp lastTimestamp = rs.getTimestamp("last_message_at");
                    if (lastTimestamp != null) {
                        conversation.setLastMessageAt(lastTimestamp.toLocalDateTime());
                    }
                    conversation.setUnreadCount(rs.getInt("unread_count"));
                    conversations.add(conversation);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de recuperer les conversations utilisateur.", exception);
        }
        return conversations;
    }

    public void markMessagesAsRead(int conversationId, int currentUserId) {
        String query = "UPDATE " + MESSAGE_TABLE + " SET is_read = 1 WHERE conversation_id = ? AND receiver_id = ? AND is_read = 0";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, conversationId);
            statement.setInt(2, currentUserId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de marquer les messages comme lus.", exception);
        }
    }

    private void touchConversation(int conversationId) {
        String update = "UPDATE " + CONVERSATION_TABLE + " SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(update)) {
            statement.setInt(1, conversationId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'actualiser la conversation.", exception);
        }
    }

    private PrivateConversation mapConversation(ResultSet rs) throws SQLException {
        PrivateConversation conversation = new PrivateConversation();
        conversation.setId(rs.getInt("id"));
        conversation.setUserAId(rs.getInt("user_a_id"));
        conversation.setUserBId(rs.getInt("user_b_id"));
        Timestamp updatedAt = hasColumn(rs, "updated_at") ? rs.getTimestamp("updated_at") : null;
        if (updatedAt != null) {
            conversation.setLastMessageAt(updatedAt.toLocalDateTime());
        }
        return conversation;
    }

    private PrivateMessage mapMessage(ResultSet rs) throws SQLException {
        PrivateMessage message = new PrivateMessage();
        message.setId(rs.getInt("id"));
        message.setConversationId(rs.getInt("conversation_id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setReceiverId(rs.getInt("receiver_id"));
        message.setContent(rs.getString("content"));
        message.setRead(rs.getBoolean("is_read"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            message.setCreatedAt(createdAt.toLocalDateTime());
        }
        return message;
    }

    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException exception) {
            return false;
        }
    }

    private void ensureSchema() {
        String createConversationTable = """
                CREATE TABLE IF NOT EXISTS %s (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_a_id INT NOT NULL,
                    user_b_id INT NOT NULL,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_private_conversation_pair (user_a_id, user_b_id)
                )
                """.formatted(CONVERSATION_TABLE);

        String createMessageTable = """
                CREATE TABLE IF NOT EXISTS %s (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    conversation_id INT NOT NULL,
                    sender_id INT NOT NULL,
                    receiver_id INT NOT NULL,
                    content TEXT NOT NULL,
                    is_read TINYINT(1) NOT NULL DEFAULT 0,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    KEY idx_private_message_conversation (conversation_id),
                    KEY idx_private_message_receiver_read (receiver_id, is_read)
                )
                """.formatted(MESSAGE_TABLE);

        try (Statement statement = cnx.createStatement()) {
            statement.executeUpdate(createConversationTable);
            statement.executeUpdate(createMessageTable);
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'initialiser le schema de messagerie privee.", exception);
        }
    }
}
