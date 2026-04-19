package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceUser {
    private static final String TABLE_NAME = "fitopia_users";
    private final Connection cnx;

    public ServiceUser() {
        cnx = MyDataBase.getInstance().getCnx();
        if (cnx != null) {
            initializeTable();
        }
    }

    public boolean isAvailable() {
        return cnx != null;
    }

    public List<FitopiaUser> getAll() {
        ensureConnection();
        List<FitopiaUser> users = new ArrayList<>();
        String query = "SELECT * FROM `" + TABLE_NAME + "` ORDER BY id DESC";

        try (Statement statement = cnx.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement des utilisateurs : " + e.getMessage(), e);
        }

        return users;
    }

    public Optional<FitopiaUser> authenticate(String identifier, String password) {
        ensureConnection();
        String query = "SELECT * FROM `" + TABLE_NAME + "` WHERE (LOWER(email)=LOWER(?) OR LOWER(username)=LOWER(?)) AND password=? LIMIT 1";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, identifier);
            pstm.setString(2, identifier);
            pstm.setString(3, password);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la connexion utilisateur : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<FitopiaUser> findByIdentifier(String identifier) {
        ensureConnection();
        String query = "SELECT * FROM `" + TABLE_NAME + "` WHERE LOWER(email)=LOWER(?) OR LOWER(username)=LOWER(?) LIMIT 1";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, identifier);
            pstm.setString(2, identifier);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche utilisateur : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public Optional<FitopiaUser> authenticateWithFaceId(String identifier) {
        ensureConnection();
        String query = "SELECT * FROM `" + TABLE_NAME + "` WHERE (LOWER(email)=LOWER(?) OR LOWER(username)=LOWER(?)) "
                + "AND face_id_enabled=1 AND face_image_path IS NOT NULL AND face_image_path <> '' LIMIT 1";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, identifier);
            pstm.setString(2, identifier);
            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'authentification Face ID : " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void enableFaceId(int userId, String faceImagePath) {
        ensureConnection();
        String query = "UPDATE `" + TABLE_NAME + "` SET face_id_enabled=?, face_image_path=? WHERE id=?";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setBoolean(1, true);
            pstm.setString(2, faceImagePath);
            pstm.setInt(3, userId);
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'activation Face ID : " + e.getMessage(), e);
        }
    }

    public boolean emailExists(String email) {
        return existsByColumn("email", email);
    }

    public boolean usernameExists(String username) {
        return existsByColumn("username", username);
    }

    public boolean emailExistsForOtherUser(String email, int userId) {
        return existsByColumnForOtherUser("email", email, userId);
    }

    public boolean usernameExistsForOtherUser(String username, int userId) {
        return existsByColumnForOtherUser("username", username, userId);
    }

    public void add(FitopiaUser user) {
        ensureConnection();
        String query = "INSERT INTO `" + TABLE_NAME + "` (first_name,last_name,username,email,password,phone,birth_date,gender,role,avatar_path,"
                + "professional_title,specialization,qualification,years_experience,bio,license_number,height,weight,target_weight,"
                + "fitness_level,health_conditions,dietary_preferences,fitness_goals,face_id_enabled,face_image_path) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            fillStatement(pstm, user, false);
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage(), e);
        }
    }

    private boolean existsByColumn(String column, String value) {
        ensureConnection();
        String query = "SELECT 1 FROM `" + TABLE_NAME + "` WHERE LOWER(" + column + ")=LOWER(?) LIMIT 1";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, value);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la verification " + column + " : " + e.getMessage(), e);
        }
    }

    private boolean existsByColumnForOtherUser(String column, String value, int userId) {
        ensureConnection();
        String query = "SELECT 1 FROM `" + TABLE_NAME + "` WHERE LOWER(" + column + ")=LOWER(?) AND id<>? LIMIT 1";
        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setString(1, value);
            pstm.setInt(2, userId);
            try (ResultSet rs = pstm.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la verification " + column + " : " + e.getMessage(), e);
        }
    }

    public void update(FitopiaUser user) {
        ensureConnection();
        String query = "UPDATE `" + TABLE_NAME + "` SET first_name=?,last_name=?,username=?,email=?,password=?,phone=?,birth_date=?,gender=?,role=?,avatar_path=?,"
                + "professional_title=?,specialization=?,qualification=?,years_experience=?,bio=?,license_number=?,height=?,weight=?,target_weight=?,"
                + "fitness_level=?,health_conditions=?,dietary_preferences=?,fitness_goals=?,face_id_enabled=?,face_image_path=? WHERE id=?";

        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            fillStatement(pstm, user, true);
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de l'utilisateur : " + e.getMessage(), e);
        }
    }

    public void delete(int id) {
        ensureConnection();
        String query = "DELETE FROM `" + TABLE_NAME + "` WHERE id = ?";

        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            pstm.setInt(1, id);
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur : " + e.getMessage(), e);
        }
    }

    private void initializeTable() {
        String query = "CREATE TABLE IF NOT EXISTS `" + TABLE_NAME + "` ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "first_name VARCHAR(100) NOT NULL,"
                + "last_name VARCHAR(100) NOT NULL,"
                + "username VARCHAR(100) NOT NULL UNIQUE,"
                + "email VARCHAR(150) NOT NULL UNIQUE,"
                + "password VARCHAR(150) NOT NULL,"
                + "phone VARCHAR(50),"
                + "birth_date VARCHAR(50),"
                + "gender VARCHAR(20),"
                + "role VARCHAR(50) NOT NULL,"
                + "avatar_path VARCHAR(255),"
                + "professional_title VARCHAR(150),"
                + "specialization VARCHAR(255),"
                + "qualification VARCHAR(255),"
                + "years_experience VARCHAR(50),"
                + "bio TEXT,"
                + "license_number VARCHAR(120),"
                + "height VARCHAR(50),"
                + "weight VARCHAR(50),"
                + "target_weight VARCHAR(50),"
                + "fitness_level VARCHAR(50),"
                + "health_conditions TEXT,"
                + "dietary_preferences TEXT,"
                + "fitness_goals TEXT,"
                + "face_id_enabled BOOLEAN NOT NULL DEFAULT FALSE,"
                + "face_image_path VARCHAR(255)"
                + ")";

        try (Statement statement = cnx.createStatement()) {
            statement.executeUpdate(query);
            ensureColumn("face_id_enabled", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN face_id_enabled BOOLEAN NOT NULL DEFAULT FALSE");
            ensureColumn("face_image_path", "ALTER TABLE `" + TABLE_NAME + "` ADD COLUMN face_image_path VARCHAR(255)");
        } catch (SQLException e) {
            throw new RuntimeException("Impossible de preparer la table utilisateur : " + e.getMessage(), e);
        }
    }

    private void ensureConnection() {
        if (cnx == null) {
            throw new IllegalStateException("Connexion MySQL indisponible. Verifiez la base fitopiabd.");
        }
    }

    private void fillStatement(PreparedStatement pstm, FitopiaUser user, boolean includeId) throws SQLException {
        pstm.setString(1, user.getFirstName());
        pstm.setString(2, user.getLastName());
        pstm.setString(3, user.getUsername());
        pstm.setString(4, user.getEmail());
        pstm.setString(5, user.getPassword());
        pstm.setString(6, user.getPhone());
        pstm.setString(7, user.getBirthDate());
        pstm.setString(8, user.getGender());
        pstm.setString(9, user.getRole());
        pstm.setString(10, user.getAvatarPath());
        pstm.setString(11, user.getProfessionalTitle());
        pstm.setString(12, user.getSpecialization());
        pstm.setString(13, user.getQualification());
        pstm.setString(14, user.getYearsExperience());
        pstm.setString(15, user.getBio());
        pstm.setString(16, user.getLicenseNumber());
        pstm.setString(17, user.getHeight());
        pstm.setString(18, user.getWeight());
        pstm.setString(19, user.getTargetWeight());
        pstm.setString(20, user.getFitnessLevel());
        pstm.setString(21, user.getHealthConditions());
        pstm.setString(22, user.getDietaryPreferences());
        pstm.setString(23, user.getFitnessGoals());
        pstm.setBoolean(24, user.isFaceIdEnabled());
        pstm.setString(25, user.getFaceImagePath());
        if (includeId) {
            pstm.setInt(26, user.getId());
        }
    }

    private FitopiaUser mapUser(ResultSet rs) throws SQLException {
        FitopiaUser user = new FitopiaUser();
        user.setId(rs.getInt("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setBirthDate(rs.getString("birth_date"));
        user.setGender(rs.getString("gender"));
        user.setRole(rs.getString("role"));
        user.setAvatarPath(rs.getString("avatar_path"));
        user.setProfessionalTitle(rs.getString("professional_title"));
        user.setSpecialization(rs.getString("specialization"));
        user.setQualification(rs.getString("qualification"));
        user.setYearsExperience(rs.getString("years_experience"));
        user.setBio(rs.getString("bio"));
        user.setLicenseNumber(rs.getString("license_number"));
        user.setHeight(rs.getString("height"));
        user.setWeight(rs.getString("weight"));
        user.setTargetWeight(rs.getString("target_weight"));
        user.setFitnessLevel(rs.getString("fitness_level"));
        user.setHealthConditions(rs.getString("health_conditions"));
        user.setDietaryPreferences(rs.getString("dietary_preferences"));
        user.setFitnessGoals(rs.getString("fitness_goals"));
        user.setFaceIdEnabled(rs.getBoolean("face_id_enabled"));
        user.setFaceImagePath(rs.getString("face_image_path"));
        return user;
    }

    private void ensureColumn(String columnName, String alterQuery) throws SQLException {
        DatabaseMetaData metaData = cnx.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, TABLE_NAME, columnName)) {
            if (!rs.next()) {
                try (Statement statement = cnx.createStatement()) {
                    statement.executeUpdate(alterQuery);
                }
            }
        }
    }
}
