package com.birds.bird_app.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bird_species")
public class BirdSpecies {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String commonName;

    @Column(nullable = false)
    private String scientificName;

    @Column(length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "bird_species_characteristics", joinColumns = @JoinColumn(name = "species_id"))
    @Column(name = "characteristic")
    private Set<String> characteristics = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "bird_species_aliases", joinColumns = @JoinColumn(name = "species_id"))
    @Column(name = "alias")
    private Set<String> aliases = new HashSet<>();

    @Column
    private String habitat;

    @Column
    private String region;

    @Column
    private String size;

    @Column
    private String color;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getCharacteristics() {
        return characteristics;
    }

    public void setCharacteristics(Set<String> characteristics) {
        this.characteristics = characteristics;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public void setAliases(Set<String> aliases) {
        this.aliases = aliases;
    }

    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
} 