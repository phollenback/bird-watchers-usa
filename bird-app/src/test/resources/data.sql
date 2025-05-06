-- Users
INSERT INTO users (email, name, password, role, enabled, profile_picture_url) VALUES
('admin@birdwatchers.com', 'Admin User', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'ADMIN', true, 'https://example.com/admin.jpg'),
('john.doe@example.com', 'John Doe', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'USER', true, 'https://example.com/john.jpg'),
('jane.smith@example.com', 'Jane Smith', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'USER', true, 'https://example.com/jane.jpg'),
('mike.wilson@example.com', 'Mike Wilson', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'USER', true, 'https://example.com/mike.jpg'),
('sarah.brown@example.com', 'Sarah Brown', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'USER', true, 'https://example.com/sarah.jpg');

-- User Settings
INSERT INTO user_settings (user_id, notification_preferences, theme, language, privacy_level) VALUES
(1, 'EMAIL', 'DARK', 'EN', 'PUBLIC'),
(2, 'EMAIL', 'LIGHT', 'EN', 'PUBLIC'),
(3, 'PUSH', 'DARK', 'EN', 'PRIVATE'),
(4, 'NONE', 'LIGHT', 'EN', 'PUBLIC'),
(5, 'EMAIL', 'DARK', 'EN', 'PRIVATE');

-- Testimonials
INSERT INTO testimonials (name, location, content) VALUES
('Robert Johnson', 'California, USA', 'This platform has revolutionized how I track and share my bird watching experiences. The community is incredibly supportive and knowledgeable!'),
('Maria Garcia', 'Texas, USA', 'I''ve discovered so many rare birds in my area thanks to the detailed maps and community reports. Absolutely love this platform!'),
('David Chen', 'Washington, USA', 'The bird identification features are incredibly accurate. It''s helped me learn so much about local species.'),
('Emma Thompson', 'Oregon, USA', 'The photo sharing and community feedback have helped me improve my bird photography skills tremendously.'),
('James Wilson', 'Florida, USA', 'Great platform for both beginners and experienced bird watchers. The seasonal bird migration tracking is particularly useful.'); 