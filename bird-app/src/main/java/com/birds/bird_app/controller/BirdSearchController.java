package com.birds.bird_app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.birds.bird_app.model.BirdModel;
import com.birds.bird_app.service.BirdService;

@Controller
public class BirdSearchController {

    @Autowired
    private BirdService birdService;

    @GetMapping("/birds/search")
    public String searchBirds(@RequestParam(required = false) String query, Model model) {
        List<BirdModel> birds = birdService.searchBirds(query);
        model.addAttribute("birds", birds);
            
        model.addAttribute("query", query);
        return "birds/search";
    }
}