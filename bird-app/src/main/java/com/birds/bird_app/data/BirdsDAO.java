package com.birds.bird_app.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.birds.bird_app.model.BirdModel;

@Repository
public class BirdsDAO {
    private static final Logger logger = LoggerFactory.getLogger(BirdsDAO.class);

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    public List<BirdModel> findAll() {
        List<BirdModel> birds = new ArrayList<>();
        String sql = "SELECT * FROM birds";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                birds.add(mapResultSetToBird(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all birds", e);
        }
        return birds;
    }

    public Optional<BirdModel> findById(Long id) {
        String sql = "SELECT * FROM birds WHERE id = ?";
    
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                return Optional.of(mapResultSetToBird(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching bird with id {}", id, e);
        }
        return Optional.empty();
    }

    public BirdModel save(BirdModel bird) {
        String sql = "INSERT INTO birds (name, kind, color, age, fun_fact, image_url) VALUES (?, ?, ?, ?, ?, ?)";
        logger.info("Attempting to save bird: {}", bird);
        logger.info("Database URL: {}", url);
        logger.info("Database username: {}", username);
        logger.info("Database password: {}", password != null ? "****" : "null");

        try {
            // Test database connection first
            try (Connection testConn = DriverManager.getConnection(url, username, password)) {
                logger.info("Successfully connected to database: {}", url);
                
                // Verify table exists and create if not
                try (Statement stmt = testConn.createStatement()) {
                    stmt.executeQuery("SELECT 1 FROM birds LIMIT 1");
                    logger.info("Birds table exists and is accessible");
                } catch (SQLException e) {
                    logger.info("Creating birds table...");
                    try (Statement createStmt = testConn.createStatement()) {
                        String createTableSql = "CREATE TABLE IF NOT EXISTS birds (" +
                            "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                            "name VARCHAR(255) NOT NULL, " +
                            "kind VARCHAR(255) NOT NULL, " +
                            "color VARCHAR(255) NOT NULL, " +
                            "age INT NOT NULL, " +
                            "fun_fact TEXT, " +
                            "image_url VARCHAR(255))";
                        logger.info("Executing create table SQL: {}", createTableSql);
                        createStmt.executeUpdate(createTableSql);
                        logger.info("Birds table created successfully");
                    } catch (SQLException createError) {
                        logger.error("Failed to create birds table", createError);
                        logger.error("SQL State: {}", createError.getSQLState());
                        logger.error("Error Code: {}", createError.getErrorCode());
                        logger.error("Error Message: {}", createError.getMessage());
                        return bird;
                    }
                }
            } catch (SQLException e) {
                logger.error("Failed to connect to database: {}", url);
                logger.error("SQL State: {}", e.getSQLState());
                logger.error("Error Code: {}", e.getErrorCode());
                logger.error("Error Message: {}", e.getMessage());
                return bird;
            }

            try (Connection conn = DriverManager.getConnection(url, username, password);
                 PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                logger.info("Setting parameters for bird: name={}, kind={}, color={}, age={}, fun_fact={}, image_url={}",
                    bird.getName(), bird.getKind(), bird.getColor(), bird.getAge(), bird.getFunFact(), bird.getImageUrl());

                // Check for null values
                if (bird.getName() == null || bird.getKind() == null || bird.getColor() == null) {
                    logger.error("Required fields are null: name={}, kind={}, color={}", 
                        bird.getName(), bird.getKind(), bird.getColor());
                    return bird;
                }

                // Set parameters with proper null handling
                stmt.setString(1, bird.getName());
                stmt.setString(2, bird.getKind());
                stmt.setString(3, bird.getColor());
                stmt.setInt(4, bird.getAge() != null ? bird.getAge() : 0);
                stmt.setString(5, bird.getFunFact() != null ? bird.getFunFact() : "");
                stmt.setString(6, bird.getImageUrl() != null ? bird.getImageUrl() : "");

                logger.info("Executing SQL: {}", sql);
                int affectedRows = stmt.executeUpdate();
                logger.info("SQL execution completed. Affected rows: {}", affectedRows);

                if (affectedRows > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        long newId = generatedKeys.getLong(1);
                        bird.setId(newId);
                        logger.info("Successfully saved bird with ID: {}", newId);
                    } else {
                        logger.error("No generated keys returned after insert");
                    }
                } else {
                    logger.error("No rows were affected by the insert operation");
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving bird: {}", bird.getName(), e);
            logger.error("SQL State: {}", e.getSQLState());
            logger.error("Error Code: {}", e.getErrorCode());
            logger.error("Error Message: {}", e.getMessage());
            logger.error("SQL: {}", sql);
        }
        return bird;
    }

    public boolean update(BirdModel bird) {
        String sql = "UPDATE birds SET name = ?, kind = ?, color = ?, age = ?, fun_fact = ?, image_url = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bird.getName());
            stmt.setString(2, bird.getKind());
            stmt.setString(3, bird.getColor());
            stmt.setInt(4, bird.getAge());
            stmt.setString(5, bird.getFunFact());
            stmt.setString(6, bird.getImageUrl());
            stmt.setLong(7, bird.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating bird with id {}", bird.getId(), e);
        }
        return false;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM birds WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting bird with id {}", id, e);
        }
        return false;
    }

    // Secure version using PreparedStatement
    public List<BirdModel> search(String query) {
        List<BirdModel> birds = new ArrayList<>();
        String sql = "SELECT * FROM birds WHERE name LIKE ?";
        logger.info("Starting search with query: {}", query);
        logger.info("SQL query: {}", sql);

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) { 
            
            stmt.setString(1, "%" + query + "%");
            logger.info("Executing query with parameter: %{}%", query);
            ResultSet rs = stmt.executeQuery(); 
            
            int count = 0;
            while (rs.next()) {
                BirdModel bird = mapResultSetToBird(rs);
                birds.add(bird);
                count++;
                logger.info("Found bird: ID={}, Name={}, Kind={}", bird.getId(), bird.getName(), bird.getKind());
            }
            logger.info("Total birds found: {}", count);
            
            // Log all birds in database for debugging
            if (count == 0) {
                logger.info("No birds found. Listing all birds in database:");
                String allBirdsSql = "SELECT * FROM birds";
                try (Statement allStmt = conn.createStatement();
                     ResultSet allRs = allStmt.executeQuery(allBirdsSql)) {
                    while (allRs.next()) {
                        logger.info("Available bird: ID={}, Name={}, Kind={}", 
                            allRs.getLong("id"), 
                            allRs.getString("name"), 
                            allRs.getString("kind"));
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching birds with query: {}", query, e);
            logger.error("SQL State: {}", e.getSQLState());
            logger.error("Error Code: {}", e.getErrorCode());
            logger.error("Error Message: {}", e.getMessage());
        }
        return birds;
    }


    // Vulnerable version 
    // public List<BirdModel> search(String query) {
    //     List<BirdModel> birds = new ArrayList<>();
    //     String sql = "SELECT * FROM birds WHERE name = '" + query + "'";
    //     logger.info("Starting search with query: {}", query);
    //     logger.info("SQL query being executed: {}", sql);

    //     try (Connection conn = DriverManager.getConnection(url, username, password);
    //          Statement stmt = conn.createStatement();
    //          ResultSet rs = stmt.executeQuery(sql)) {
            
    //         logger.info("Executing SQL query: {}", sql);
    //         int count = 0;
    //         while (rs.next()) {
    //             BirdModel bird = mapResultSetToBird(rs);
    //             birds.add(bird);
    //             count++;
    //             logger.info("Found bird: ID={}, Name={}, Kind={}", bird.getId(), bird.getName(), bird.getKind());
    //         }
    //         logger.info("Total birds found: {}", count);
            
    //         // Log all birds in database for debugging
    //         if (count == 0) {
    //             logger.info("No birds found. Listing all birds in database:");
    //             String allBirdsSql = "SELECT * FROM birds";
    //             try (Statement allStmt = conn.createStatement();
    //                  ResultSet allRs = allStmt.executeQuery(allBirdsSql)) {
    //                 while (allRs.next()) {
    //                     logger.info("Available bird: ID={}, Name={}, Kind={}", 
    //                         allRs.getLong("id"), 
    //                         allRs.getString("name"), 
    //                         allRs.getString("kind"));
    //                 }
    //             }
    //         }
    //     } catch (SQLException e) {
    //         logger.error("Error searching birds with query: {}", query, e);
    //         logger.error("SQL State: {}", e.getSQLState());
    //         logger.error("Error Code: {}", e.getErrorCode());
    //         logger.error("Error Message: {}", e.getMessage());
    //     }
    //     return birds;
    // }

    private BirdModel mapResultSetToBird(ResultSet rs) throws SQLException {
        BirdModel bird = new BirdModel();
        bird.setId(rs.getLong("id"));
        bird.setName(rs.getString("name"));
        bird.setKind(rs.getString("kind"));
        bird.setColor(rs.getString("color"));
        bird.setAge(rs.getInt("age"));
        bird.setFunFact(rs.getString("fun_fact"));
        bird.setImageUrl(rs.getString("image_url"));
        return bird;
    }
}
