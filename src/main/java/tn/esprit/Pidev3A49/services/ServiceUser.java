package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.FitopiaUser;
import tn.esprit.Pidev3A49.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

    public void add(FitopiaUser user) {
        ensureConnection();
        String query = "INSERT INTO `" + TABLE_NAME + "` (first_name,last_name,username,email,password,phone,birth_date,gender,role,avatar_path,"
                + "professional_title,specialization,qualification,years_experience,bio,license_number,height,weight,target_weight,"
                + "fitness_level,health_conditions,dietary_preferences,fitness_goals) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement pstm = cnx.prepareStatement(query)) {
            fillStatement(pstm, user, false);
            pstm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'utilisateur : " + e.getMessage(), e);
        }
    }

    public void update(FitopiaUser user) {
        ensureConnection();
        String query = "UPDATE `" + TABLE_NAME + "` SET first_name=?,last_name=?,username=?,email=?,password=?,phone=?,birth_date=?,gender=?,role=?,avatar_path=?,"
                + "professional_title=?,specialization=?,qualification=?,years_experience=?,bio=?,license_number=?,height=?,weight=?,target_weight=?,"
                + "fitness_level=?,health_conditions=?,dietary_preferences=?,fitness_goals=? WHERE id=?";

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
                + "fitness_goals TEXT"
                + ")";

        try (Statement statement = cnx.createStatement()) {
            statement.executeUpdate(query);
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
        if (includeId) {
            pstm.setInt(24, user.getId());
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
        return user;
    }
}
