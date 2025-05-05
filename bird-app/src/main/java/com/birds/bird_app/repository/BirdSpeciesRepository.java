package com.birds.bird_app.repository;

import com.birds.bird_app.model.BirdSpecies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BirdSpeciesRepository extends JpaRepository<BirdSpecies, Long> {
    
    @Query("SELECT bs FROM BirdSpecies bs WHERE " +
           "LOWER(bs.commonName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(bs.scientificName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "EXISTS (SELECT a FROM bs.aliases a WHERE LOWER(a) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<BirdSpecies> findBySearchTerm(@Param("term") String term);

    @Query("SELECT bs FROM BirdSpecies bs WHERE " +
           "LOWER(bs.color) LIKE LOWER(CONCAT('%', :color, '%'))")
    List<BirdSpecies> findByColor(@Param("color") String color);
} 