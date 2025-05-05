package com.birds.bird_app.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.birds.bird_app.model.TestimonialModel;
import com.birds.bird_app.repository.TestimonialRepository;

@Service
public class TestimonialDataService {
    private final TestimonialRepository testimonialRepository;

    @Autowired
    public TestimonialDataService(TestimonialRepository testimonialRepository) {
        this.testimonialRepository = testimonialRepository;
    }

    public List<TestimonialModel> getAllTestimonials() {
        return testimonialRepository.findAll();
    }

    public boolean createTestimonial(TestimonialModel model) {
        try {
            testimonialRepository.save(model);
            return true;
        } catch (Exception e) {
            System.out.println("Error creating testimonial: " + e.getMessage());
            return false;
        }
    }
}