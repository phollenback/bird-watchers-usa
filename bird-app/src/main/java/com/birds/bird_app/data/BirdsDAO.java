package com.birds.bird_app.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.repository.query.Param;

import com.birds.bird_app.model.BirdEntity;

@Repository
public class BirdsDAO implements JpaRepository<BirdEntity, Long> {
    
    private static final Logger logger = LoggerFactory.getLogger(BirdsDAO.class);

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Query("SELECT b FROM BirdEntity b WHERE b.name LIKE %:query% OR b.kind LIKE %:query% OR b.color LIKE %:query%")
    public List<BirdEntity> search(@Param("query") String query) {
        List<BirdEntity> birds = new ArrayList<>();
        String sql = "SELECT * FROM birds WHERE name LIKE ? OR kind LIKE ? OR color LIKE ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + query + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                birds.add(mapResultSetToBird(rs));
            }
        } catch (SQLException e) {
            logger.error("Error searching birds with query: {}", query, e);
        }
        return birds;
    }

    @Override
    public List<BirdEntity> findAll() {
        List<BirdEntity> birds = new ArrayList<>();
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

    @Override
    public Optional<BirdEntity> findById(Long id) {
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

    @Override
    public BirdEntity save(BirdEntity bird) {
        String sql = "INSERT INTO birds (name, kind, color, age, fun_fact, image_url) VALUES (?, ?, ?, ?, ?, ?)";
        logger.info("Attempting to save bird: {}", bird);

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, bird.getName());
            stmt.setString(2, bird.getKind());
            stmt.setString(3, bird.getColor());
            stmt.setInt(4, bird.getAge());
            stmt.setString(5, bird.getFunFact() != null ? bird.getFunFact() : "");
            stmt.setString(6, bird.getImageUrl() != null ? bird.getImageUrl() : "");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    bird.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving bird: {}", bird.getName(), e);
        }
        return bird;
    }

    public boolean update(BirdEntity bird) {
        String sql = "UPDATE birds SET name = ?, kind = ?, color = ?, age = ?, fun_fact = ?, image_url = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bird.getName());
            stmt.setString(2, bird.getKind());
            stmt.setString(3, bird.getColor());
            stmt.setInt(4, bird.getAge());
            stmt.setString(5, bird.getFunFact() != null ? bird.getFunFact() : "");
            stmt.setString(6, bird.getImageUrl() != null ? bird.getImageUrl() : "");
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

    private BirdEntity mapResultSetToBird(ResultSet rs) throws SQLException {
        BirdEntity bird = new BirdEntity();
        bird.setId(rs.getLong("id"));
        bird.setName(rs.getString("name"));
        bird.setKind(rs.getString("kind"));
        bird.setColor(rs.getString("color"));
        bird.setAge(rs.getInt("age"));
        bird.setFunFact(rs.getString("fun_fact"));
        bird.setImageUrl(rs.getString("image_url"));
        return bird;
    }

    // Required JpaRepository methods
    @Override
    public <S extends BirdEntity> List<S> findAll(Example<S> example) {
        List<S> results = new ArrayList<>();
        List<BirdEntity> allBirds = findAll();
        for (BirdEntity bird : allBirds) {
            if (example.getProbe().equals(bird)) {
                results.add((S) bird);
            }
        }
        return results;
    }

    @Override
    public <S extends BirdEntity> List<S> findAll(Example<S> example, Sort sort) {
        List<S> results = findAll(example);
        results.sort((a, b) -> {
            for (Sort.Order order : sort) {
                int comparison = 0;
                switch (order.getProperty()) {
                    case "name":
                        comparison = a.getName().compareTo(b.getName());
                        break;
                    case "kind":
                        comparison = a.getKind().compareTo(b.getKind());
                        break;
                    case "color":
                        comparison = a.getColor().compareTo(b.getColor());
                        break;
                    case "age":
                        comparison = Integer.compare(a.getAge(), b.getAge());
                        break;
                }
                if (comparison != 0) {
                    return order.isAscending() ? comparison : -comparison;
                }
            }
            return 0;
        });
        return results;
    }

    @Override
    public <S extends BirdEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        List<S> results = findAll(example);
        return new PageImpl<>(results, pageable, results.size());
    }

    @Override
    public List<BirdEntity> findAll(Sort sort) {
        List<BirdEntity> birds = findAll();
        birds.sort((a, b) -> {
            for (Sort.Order order : sort) {
                int comparison = 0;
                switch (order.getProperty()) {
                    case "name":
                        comparison = a.getName().compareTo(b.getName());
                        break;
                    case "kind":
                        comparison = a.getKind().compareTo(b.getKind());
                        break;
                    case "color":
                        comparison = a.getColor().compareTo(b.getColor());
                        break;
                    case "age":
                        comparison = Integer.compare(a.getAge(), b.getAge());
                        break;
                }
                if (comparison != 0) {
                    return order.isAscending() ? comparison : -comparison;
                }
            }
            return 0;
        });
        return birds;
    }

    @Override
    public Page<BirdEntity> findAll(Pageable pageable) {
        List<BirdEntity> birds = findAll();
        return new PageImpl<>(birds, pageable, birds.size());
    }

    @Override
    public <S extends BirdEntity> long count(Example<S> example) {
        return findAll(example).size();
    }

    @Override
    public <S extends BirdEntity> Optional<S> findOne(Example<S> example) {
        List<S> results = findAll(example);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public <S extends BirdEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        List<S> saved = new ArrayList<>();
        for (S entity : entities) {
            saved.add((S) save(entity));
        }
        return saved;
    }

    @Override
    public BirdEntity getReferenceById(Long id) {
        return findById(id).orElseThrow();
    }

    @Override
    public <S extends BirdEntity> S saveAndFlush(S entity) {
        return (S) save(entity);
    }

    @Override
    public void flush() {
        // No-op for JDBC implementation
    }

    @Override
    public <S extends BirdEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        for (Long id : ids) {
            delete(id);
        }
    }

    @Override
    public void deleteAllInBatch(Iterable<BirdEntity> entities) {
        for (BirdEntity entity : entities) {
            delete(entity.getId());
        }
    }

    @Override
    public void deleteAllInBatch() {
        deleteAll();
    }

    @Override
    public BirdEntity getById(Long id) {
        return findById(id).orElseThrow();
    }

    @Override
    public <S extends BirdEntity> List<S> saveAll(Iterable<S> entities) {
        List<S> saved = new ArrayList<>();
        for (S entity : entities) {
            saved.add((S) save(entity));
        }
        return saved;
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    @Override
    public List<BirdEntity> findAllById(Iterable<Long> ids) {
        List<BirdEntity> birds = new ArrayList<>();
        for (Long id : ids) {
            findById(id).ifPresent(birds::add);
        }
        return birds;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM birds";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting birds", e);
        }
        return 0;
    }

    @Override
    public void delete(BirdEntity entity) {
        delete(entity.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            delete(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends BirdEntity> entities) {
        for (BirdEntity entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM birds";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting all birds", e);
        }
    }

    @Override
    public <S extends BirdEntity> boolean exists(Example<S> example) {
        return !findAll(example).isEmpty();
    }

    @Override
    public BirdEntity getOne(Long id) {
        return getById(id);
    }

    @Override
    public void deleteById(Long id) {
        delete(id);
    }
}
