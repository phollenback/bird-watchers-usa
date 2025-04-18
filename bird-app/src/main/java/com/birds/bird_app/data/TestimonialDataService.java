package com.birds.bird_app.data;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.birds.bird_app.model.TestimonialModel;

@Service
public class TestimonialDataService {
    private List<TestimonialModel> testimonials;

    public TestimonialDataService() {
        testimonials = new ArrayList<>();
        // Add initial testimonials
        testimonials.add(new TestimonialModel("Karen", "New York", "How do they do it? How do they display all these pictures of birds? love it!"));
        testimonials.add(new TestimonialModel("Peter", "New Jersey", "Boy does this app work. These birds make me so happy each morning!"));
        testimonials.add(new TestimonialModel("Sally", "California", "I love the birds in my backyard. This app helps me identify them."));
        testimonials.add(new TestimonialModel("John", "Florida", "I love the birds in my backyard. This app helps me identify them."));
    }

    public List<TestimonialModel> getAllTestimonials() {
        return testimonials;
    }

    public boolean createTestimonial(TestimonialModel model) {
        try {
            System.out.println("==== Creating New Testimonial ====");
            System.out.println("Name: " + model.getName());
            System.out.println("Location: " + model.getLocation());
            System.out.println("Content: " + model.getContent());
            
            testimonials.add(model);
            
            System.out.println("Testimonial added successfully");
            System.out.println("Total testimonials: " + testimonials.size());
            return true;
        } catch (Exception e) {
            System.out.println("Error creating testimonial: " + e.getMessage());
            return false;
        }
    }
}