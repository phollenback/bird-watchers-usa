package com.birds.bird_app.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.HtmlUtils;

import com.birds.bird_app.model.UserEntity;

@Repository
public class UsersDAO {
    private static final Logger logger = LoggerFactory.getLogger(UsersDAO.class);
    private final String username = "root";
    private final String password = "new_password";
    private final String url = "jdbc:mysql://localhost:3306/bird_app";

    public List<UserEntity> findAll() {
        List<UserEntity> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all users", e);
        }
        return users;
    }

    public UserEntity findById(Long id) {
        UserEntity user = null;
        String sql = "SELECT * FROM users WHERE id = ?";
    
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by id: " + id, e);
        }
        return user;
    }

    public UserEntity findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        UserEntity user = null;
        String sql = "SELECT * FROM users WHERE email = ?";
    
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by email: " + email, e);
        }
        return user;
    }

    private UserEntity mapResultSetToUser(ResultSet rs) throws SQLException {
        UserEntity user = new UserEntity();
        user.setId(rs.getLong("id"));
        user.setEmail(HtmlUtils.htmlEscape(rs.getString("email")));
        user.setName(HtmlUtils.htmlEscape(rs.getString("name")));
        user.setRole(HtmlUtils.htmlEscape(rs.getString("role")));
        user.setPassword(rs.getString("password")); // Don't escape password hash
        user.setEnabled(rs.getBoolean("enabled"));
        return user;
    }

    public void save(UserEntity user) {
        String sql = "INSERT INTO users (email, name, role, password, enabled) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getPassword());
            stmt.setBoolean(5, user.isEnabled());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error saving user: " + user.getEmail(), e);
        }
    }

    public void update(UserEntity user) {
        String sql = "UPDATE users SET email = ?, name = ?, role = ?, password = ?, enabled = ? WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getPassword());
            stmt.setBoolean(5, user.isEnabled());
            stmt.setLong(6, user.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating user: " + user.getId(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting user: " + id, e);
        }
    }
}
