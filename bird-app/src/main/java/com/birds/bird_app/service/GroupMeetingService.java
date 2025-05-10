package com.birds.bird_app.service;

import com.birds.bird_app.model.GroupEntity;
import com.birds.bird_app.model.GroupSettings;
import com.birds.bird_app.model.BirdSubmission;
import com.birds.bird_app.repository.BirdSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupMeetingService {
    
    @Autowired
    private BirdSubmissionRepository birdSubmissionRepository;

    /**
     * Reset meetings based on their frequency
     * This method is scheduled to run daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void resetMeetings() {
        LocalDateTime now = LocalDateTime.now();
        
        // Get all active submissions
        List<BirdSubmission> activeSubmissions = birdSubmissionRepository.findByStatus("ACTIVE");
        
        for (BirdSubmission submission : activeSubmissions) {
            GroupEntity group = submission.getGroup();
            GroupSettings settings = group.getSettings();
            
            if (shouldResetMeeting(submission, settings, now)) {
                // Archive the current meeting
                submission.setStatus("ARCHIVED");
                birdSubmissionRepository.save(submission);
                
                // Reset the current meeting start time
                settings.setCurrentMeetingStartedAt(now);
                group.setSettings(settings);
            }
        }
    }

    /**
     * Check if a meeting should be reset based on its frequency
     */
    private boolean shouldResetMeeting(BirdSubmission submission, GroupSettings settings, LocalDateTime now) {
        if (settings.getCurrentMeetingStartedAt() == null) {
            return true;
        }

        LocalDateTime meetingStart = settings.getCurrentMeetingStartedAt();
        
        switch (settings.getMeetingFrequency()) {
            case WEEKLY:
                // Reset if it's been 7 days since the meeting started
                return now.isAfter(meetingStart.plusDays(7));
                
            case MONTHLY:
                // Reset if it's been 30 days since the meeting started
                return now.isAfter(meetingStart.plusDays(30));
                
            case CUSTOM:
                // For custom frequency, you might want to add additional logic
                // For now, we'll use a default of 14 days
                return now.isAfter(meetingStart.plusDays(14));
                
            default:
                return false;
        }
    }

    /**
     * Manually reset a meeting for a specific group
     */
    @Transactional
    public void resetGroupMeeting(GroupEntity group) {
        // Archive all active submissions for this group
        List<BirdSubmission> activeSubmissions = birdSubmissionRepository
            .findByGroupAndStatus(group, "ACTIVE");
            
        for (BirdSubmission submission : activeSubmissions) {
            submission.setStatus("ARCHIVED");
            birdSubmissionRepository.save(submission);
        }
        
        // Reset the current meeting start time
        GroupSettings settings = group.getSettings();
        settings.setCurrentMeetingStartedAt(LocalDateTime.now());
        group.setSettings(settings);
    }
} 