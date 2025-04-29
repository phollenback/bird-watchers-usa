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
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "birds")
public class BirdEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Kind is required")
    @Size(min = 2, max = 100, message = "Kind must be between 2 and 100 characters")
    @Column(nullable = false)
    private String kind;

    @NotBlank(message = "Color is required")
    @Size(min = 2, max = 50, message = "Color must be between 2 and 50 characters")
    @Column(nullable = false)
    private String color;

    @Min(value = 0, message = "Age must be a positive number")
    @Column(nullable = false)
    private Integer age;

    @Size(max = 500, message = "Fun fact must be less than 500 characters")
    @Column(columnDefinition = "TEXT")
    private String funFact;

    @URL(message = "Image URL must be a valid URL")
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    // Default constructor
    public BirdEntity() {
    }

    // Constructor with fields
    public BirdEntity(String name, String kind, String color, Integer age, String funFact, String imageUrl) {
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
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