package com.birds.bird_app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.birds.bird_app.data.TestimonialDataService;
import com.birds.bird_app.model.TestimonialModel;

@Controller 
public class HomeController {
    @Autowired
    private final TestimonialDataService tds;

    // Single constructor for dependency injection
    public HomeController(TestimonialDataService testimonialDataService) {
        this.tds = testimonialDataService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<TestimonialModel> testimonials = tds.getAllTestimonials();
        model.addAttribute("testimonials", testimonials);

        return "home"; 
    }


    @PostMapping("/signup")
    public String signUp(
        @RequestParam String email,
        @RequestParam String name, 
        RedirectAttributes redirectAttributes
    ) {
        System.out.println("=== Signing up ===");
        System.out.println("Email: " + email);
        System.out.println("Name: " + name);

        redirectAttributes.addFlashAttribute("message", "Thank you for signing up with: " + email);
        return "redirect:/";
    }

    @PostMapping("/testimonial")
    public String createTestimonial(
        @RequestParam(required = true) String name,
        @RequestParam(required = true) String location,
        @RequestParam(required = true) String content,
        RedirectAttributes redirectAttributes
    ) {
        System.out.println("=== Creating Testimonial ===");
        System.out.println("Name: " + name);
        System.out.println("Location: " + location);
        System.out.println("Content: " + content);

        try {
            TestimonialModel testimonial = new TestimonialModel(name, location, content);
            
            if(!tds.createTestimonial(testimonial)) {
                System.out.println("Failed to save testimonial");
                redirectAttributes.addFlashAttribute("error", "Failed to create testimonial.");
                return "redirect:/";
            }

            System.out.println("Testimonial created successfully");
            redirectAttributes.addFlashAttribute("message", 
                "Thank you " + name + " for your testimonial. Sending love to " + location);
            
            return "redirect:/";
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "An error occurred while creating your testimonial.");
            return "redirect:/";
        }
    }
    
}