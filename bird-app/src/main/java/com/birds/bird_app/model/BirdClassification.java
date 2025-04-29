package com.birds.bird_app.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BirdClassification {
    private boolean isBird;
    private Set<String> categories;
    private List<GroupEntity> suggestedGroups;

    public BirdClassification(boolean isBird, Set<String> categories) {
        this.isBird = isBird;
        this.categories = categories != null ? categories : new HashSet<>();
        this.suggestedGroups = new ArrayList<>();
    }

    public boolean isBird() {
        return isBird;
    }

    public void setBird(boolean bird) {
        isBird = bird;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories != null ? categories : new HashSet<>();
    }

    public List<GroupEntity> getSuggestedGroups() {
        return suggestedGroups;
    }

    public void setSuggestedGroups(List<GroupEntity> suggestedGroups) {
        this.suggestedGroups = suggestedGroups != null ? suggestedGroups : new ArrayList<>();
    }
} 