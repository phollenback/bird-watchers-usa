package com.birds.bird_app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.birds.bird_app.model.BirdEntity;
import com.birds.bird_app.model.UserEntity;
import com.birds.bird_app.service.BirdService;
import jakarta.servlet.http.HttpSession;

@Controller
public class BirdSearchController {

    @Autowired
    private BirdService birdService;

    @GetMapping("/birds/search")
    public String searchBirds(@RequestParam(required = false) String query, 
                            Model model,
                            @AuthenticationPrincipal UserEntity currentUser,
                            HttpSession session) {
        if (currentUser != null) {
            session.setAttribute("user", currentUser);
        }
        List<BirdEntity> birds = birdService.searchBirds(query);
        model.addAttribute("birds", birds);
        model.addAttribute("query", query);
        return "birds/search";
    }
}