package com.birds.bird_app.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.birds.bird_app.data.TestimonialDataService;
import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.model.TestimonialModel;
import com.birds.bird_app.model.BirdModel;
import com.birds.bird_app.service.BirdService;

@Controller 
public class HomeController {
    @Autowired
    private final TestimonialDataService tds;

    @Autowired
    private final BirdService birdService;

    // Single constructor for dependency injection
    public HomeController(TestimonialDataService testimonialDataService, BirdService birdService) {
        this.tds = testimonialDataService;
        this.birdService = birdService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<TestimonialModel> allTestimonials = tds.getAllTestimonials();
        List<TestimonialModel> randomTestimonials = new ArrayList<>(allTestimonials);
        Collections.shuffle(randomTestimonials);
        
        // Get only 6 random testimonials
        List<TestimonialModel> testimonials = randomTestimonials.size() > 6 
            ? randomTestimonials.subList(0, 6) 
            : randomTestimonials;

        List<BirdEntity> birds = birdService.getAllBirds();

        List<String> colors = new ArrayList<>();
        List<String> types = new ArrayList<>();
        for (BirdEntity bird : birds) {
            colors.add(bird.getColor());
            types.add(bird.getKind());
        }

        model.addAttribute("testimonials", testimonials);
        model.addAttribute("birds", birds);
        model.addAttribute("colors", colors);
        model.addAttribute("types", types);

        return "home"; 
    }

    @PostMapping("/testimonial")
    @ResponseBody
    public ResponseEntity<TestimonialModel> createTestimonial(
        @RequestParam(required = true) String name,
        @RequestParam(required = true) String location,
        @RequestParam(required = true) String content
    ) {
        try {
            TestimonialModel testimonial = new TestimonialModel(name, location, content);
            
            if(!tds.createTestimonial(testimonial)) {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(testimonial);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
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
}