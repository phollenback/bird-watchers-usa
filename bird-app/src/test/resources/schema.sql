-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    profile_picture_url VARCHAR(255)
);

-- User Settings table
CREATE TABLE IF NOT EXISTS user_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    notification_preferences VARCHAR(50),
    theme VARCHAR(50),
    language VARCHAR(10),
    privacy_level VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Testimonials table
CREATE TABLE IF NOT EXISTS testimonials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Groups table
CREATE TABLE IF NOT EXISTS groups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT NOT NULL,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Group Settings table
CREATE TABLE IF NOT EXISTS group_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    visibility_type VARCHAR(50),
    guest_viewers_allowed BOOLEAN DEFAULT FALSE,
    photo_sharing_enabled BOOLEAN DEFAULT TRUE,
    verification_required BOOLEAN DEFAULT FALSE,
    auto_approve_membership BOOLEAN DEFAULT FALSE,
    meeting_frequency VARCHAR(50),
    seasonal_activity VARCHAR(50),
    region VARCHAR(255),
    theme VARCHAR(50),
    group_image_url VARCHAR(255),
    FOREIGN KEY (group_id) REFERENCES groups(id)
);

-- Group Members table
CREATE TABLE IF NOT EXISTS group_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Bird Species table
CREATE TABLE IF NOT EXISTS bird_species (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    common_name VARCHAR(255) NOT NULL,
    scientific_name VARCHAR(255),
    description TEXT,
    habitat VARCHAR(255),
    region VARCHAR(255),
    size VARCHAR(50),
    color VARCHAR(255)
);

-- Bird Species Characteristics table
CREATE TABLE IF NOT EXISTS bird_species_characteristics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    species_id BIGINT NOT NULL,
    characteristic VARCHAR(255) NOT NULL,
    FOREIGN KEY (species_id) REFERENCES bird_species(id)
);

-- Bird Species Aliases table
CREATE TABLE IF NOT EXISTS bird_species_aliases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    species_id BIGINT NOT NULL,
    alias VARCHAR(255) NOT NULL,
    FOREIGN KEY (species_id) REFERENCES bird_species(id)
); 