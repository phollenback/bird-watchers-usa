package com.birds.bird_app.model;

import org.hibernate.validator.constraints.URL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "birds")
public class BirdEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Kind is required")
    @Column(nullable = false)
    private String kind;

    @NotBlank(message = "Color is required")
    @Column(nullable = false)
    private String color;

    @Min(value = 0, message = "Age must be at least 0")
    @Column(nullable = false)
    private int age;

    @Column(columnDefinition = "TEXT")
    private String funFact;

    @URL(message = "Image URL must be a valid URL")
    @Column(name = "image_url")
    private String imageUrl;

    // Default constructor
    public BirdEntity() {
    }

    // Constructor with fields
    public BirdEntity(String name, String kind, String color, int age, String funFact, String imageUrl) {
        this.name = name;
        this.kind = kind;
        this.color = color;
        this.age = age;
        this.funFact = funFact;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getFunFact() {
        return funFact;
    }

    public void setFunFact(String funFact) {
        this.funFact = funFact;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Convert to BirdModel
    public BirdModel toBirdModel() {
        BirdModel model = new BirdModel(name, kind, color, age, funFact, imageUrl);
        model.setId(this.id);
        return model;
    }

    // Create from BirdModel
    public static BirdEntity fromBirdModel(BirdModel model) {
        BirdEntity entity = new BirdEntity(
            model.getName(),
            model.getKind(),
            model.getColor(),
            model.getAge(),
            model.getFunFact(),
            model.getImageUrl()
        );
        if (model.getId() != null) {
            entity.setId(model.getId());
        }
        return entity;
    }
}