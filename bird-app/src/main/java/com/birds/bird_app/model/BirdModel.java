package com.birds.bird_app.model;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class BirdModel {
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @NotBlank(message = "Kind is required")
    @Size(min = 2, max = 50, message = "Kind must be between 2 and 50 characters")
    private String kind;
    
    @NotBlank(message = "Color is required")
    @Size(min = 2, max = 50, message = "Color must be between 2 and 50 characters")
    private String color;
    
    @NotNull(message = "Age is required")
    @Positive(message = "Age must be a positive number")
    private Integer age;
    
    @Size(max = 500, message = "Fun fact must be less than 500 characters")
    private String funFact;
    
    @URL(message = "Image URL must be a valid URL")
    private String imageUrl;

    // No-args constructor for JSON serialization
    public BirdModel() {
    }

    public BirdModel(String name, String kind, String color, Integer age, String funFact, String imageUrl) {  
        this.name = name;
        this.kind = kind;
        this.color = color;
        this.age = age;
        this.funFact = funFact;
        this.imageUrl = imageUrl;
    }

    // getters and setters
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
}