package com.birds.bird_app.config;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.birds.bird_app.service.ImageVerificationService;

@Service
@Primary
public class TestImageVerificationService extends ImageVerificationService {
    @Override
    public boolean isBirdImage(MultipartFile image) {
        return true;
    }
} 