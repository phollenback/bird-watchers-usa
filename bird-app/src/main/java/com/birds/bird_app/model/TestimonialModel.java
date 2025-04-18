package com.birds.bird_app.model;

public class TestimonialModel {
    private String name;
    private String location;
    private String content;

    public TestimonialModel() {
    }
    public TestimonialModel(String name, String location, String content) {
        this.name = name;
        this.location = location;
        this.content = content;
    }

    //getters and setters for all fields
    public String getName() {
        return name;
    }
    public String getLocation() {
        return location;
    }
    public String getContent() {
        return content;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setContent(String content) {
        this.content = content;
    }   
}
