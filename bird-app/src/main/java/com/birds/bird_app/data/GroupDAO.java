package com.birds.bird_app.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
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

import com.birds.bird_app.model.GroupEntity;


@Repository
public class GroupDAO implements JpaRepository<GroupEntity, Long> {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupDAO.class);

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Query("SELECT g FROM GroupEntity g WHERE g.name LIKE %:query% OR g.description LIKE %:query%")
    public List<GroupEntity> search(@Param("query") String query) {
        List<GroupEntity> groups = new ArrayList<>();
        String sql = "SELECT * FROM bird_groups WHERE name LIKE ? OR description LIKE ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + query + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                groups.add(mapResultSetToGroup(rs));
            }
        } catch (SQLException e) {
            logger.error("Error searching groups with query: {}", query, e);
        }
        return groups;
    }

    @Override
    public List<GroupEntity> findAll() {
        List<GroupEntity> groups = new ArrayList<>();
        String sql = "SELECT * FROM bird_groups";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                groups.add(mapResultSetToGroup(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all groups", e);
        }
        return groups;
    }

    @Override
    public Optional<GroupEntity> findById(Long id) {
        String sql = "SELECT * FROM bird_groups WHERE id = ?";
    
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                return Optional.of(mapResultSetToGroup(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching group with id {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public GroupEntity save(GroupEntity group) {
        String sql = "INSERT INTO bird_groups (name, description, founder_id, bird_keeper_id, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        logger.info("Attempting to save group: {}", group);

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, group.getName());
            stmt.setString(2, group.getDescription());
            stmt.setLong(3, group.getFounder().getId());
            stmt.setLong(4, group.getBirdKeeper().getId());
            stmt.setObject(5, group.getCreatedAt());
            stmt.setObject(6, group.getUpdatedAt());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    group.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving group: {}", group.getName(), e);
        }
        return group;
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM bird_groups WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting group with id {}", id, e);
        }
    }

    private GroupEntity mapResultSetToGroup(ResultSet rs) throws SQLException {
        GroupEntity group = new GroupEntity();
        group.setId(rs.getLong("id"));
        group.setName(rs.getString("name"));
        group.setDescription(rs.getString("description"));
        group.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        group.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        
        // Note: In a real implementation, you would need to fetch the related entities
        // (founder, birdKeeper, settings) using their respective DAOs
        return group;
    }

    // Required JpaRepository methods
    @Override
    public <S extends GroupEntity> List<S> findAll(Example<S> example) {
        List<S> results = new ArrayList<>();
        List<GroupEntity> allGroups = findAll();
        for (GroupEntity group : allGroups) {
            if (example.getProbe().equals(group)) {
                results.add((S) group);
            }
        }
        return results;
    }

    @Override
    public <S extends GroupEntity> List<S> findAll(Example<S> example, Sort sort) {
        List<S> results = findAll(example);
        results.sort((a, b) -> {
            for (Sort.Order order : sort) {
                int comparison = 0;
                switch (order.getProperty()) {
                    case "name":
                        comparison = a.getName().compareTo(b.getName());
                        break;
                    case "createdAt":
                        comparison = a.getCreatedAt().compareTo(b.getCreatedAt());
                        break;
                    default:
                        comparison = 0;
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
    public <S extends GroupEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        List<S> results = findAll(example);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        return new PageImpl<>(results.subList(start, end), pageable, results.size());
    }

    @Override
    public List<GroupEntity> findAll(Sort sort) {
        List<GroupEntity> results = findAll();
        results.sort((a, b) -> {
            for (Sort.Order order : sort) {
                int comparison = 0;
                switch (order.getProperty()) {
                    case "name":
                        comparison = a.getName().compareTo(b.getName());
                        break;
                    case "createdAt":
                        comparison = a.getCreatedAt().compareTo(b.getCreatedAt());
                        break;
                    default:
                        comparison = 0;
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
    public Page<GroupEntity> findAll(Pageable pageable) {
        List<GroupEntity> results = findAll();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), results.size());
        return new PageImpl<>(results.subList(start, end), pageable, results.size());
    }

    @Override
    public <S extends GroupEntity> long count(Example<S> example) {
        return findAll(example).size();
    }

    @Override
    public <S extends GroupEntity> Optional<S> findOne(Example<S> example) {
        List<S> results = findAll(example);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public <S extends GroupEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        List<S> savedEntities = new ArrayList<>();
        for (S entity : entities) {
            savedEntities.add(saveAndFlush(entity));
        }
        return savedEntities;
    }

    @Override
    public GroupEntity getReferenceById(Long id) {
        return getById(id);
    }

    @Override
    public <S extends GroupEntity> S saveAndFlush(S entity) {
        S saved = (S) save(entity);
        return saved;
    }

    @Override
    public void flush() {
        // No-op for JDBC implementation
    }

    @Override
    public <S extends GroupEntity, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("findBy with Example and Function is not supported in this implementation");
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        String sql = "DELETE FROM bird_groups WHERE id IN (?)";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Long id : ids) {
                stmt.setLong(1, id);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            logger.error("Error deleting groups in batch", e);
        }
    }

    @Override
    public void deleteAllInBatch(Iterable<GroupEntity> entities) {
        List<Long> ids = new ArrayList<>();
        for (GroupEntity entity : entities) {
            if (entity.getId() != null) {
                ids.add(entity.getId());
            }
        }
        deleteAllByIdInBatch(ids);
    }

    @Override
    public void deleteAllInBatch() {
        deleteAll();
    }

    @Override
    public GroupEntity getById(Long id) {
        return findById(id).orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + id));
    }

    @Override
    public <S extends GroupEntity> List<S> saveAll(Iterable<S> entities) {
        List<S> savedEntities = new ArrayList<>();
        for (S entity : entities) {
            savedEntities.add((S) save(entity));
        }
        return savedEntities;
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    @Override
    public List<GroupEntity> findAllById(Iterable<Long> ids) {
        List<GroupEntity> groups = new ArrayList<>();
        for (Long id : ids) {
            findById(id).ifPresent(groups::add);
        }
        return groups;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM bird_groups";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error counting groups", e);
        }
        return 0;
    }

    @Override
    public void delete(GroupEntity entity) {
        if (entity.getId() != null) {
            deleteById(entity.getId());
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends GroupEntity> entities) {
        for (GroupEntity entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM bird_groups";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting all groups", e);
        }
    }

    @Override
    public <S extends GroupEntity> boolean exists(Example<S> example) {
        return !findAll(example).isEmpty();
    }

    @Override
    public GroupEntity getOne(Long id) {
        return getById(id);
    }
} 