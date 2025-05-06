-- MySQL dump 10.13  Distrib 8.0.42, for macos14.7 (arm64)
--
-- Host: localhost    Database: bird_app
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `bird_groups`
--

USE bird_app;

DROP TABLE IF EXISTS `bird_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bird_groups` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `description` text,
  `image_url` varchar(255) DEFAULT NULL,
  `keeper_id` bigint DEFAULT NULL,
  `member_count` int DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `bird_keeper_id` bigint NOT NULL,
  `founder_id` bigint NOT NULL,
  `settings_id` bigint DEFAULT NULL,
  `common_names` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_h6edwrq4608b0posxuk1b8r13` (`settings_id`),
  KEY `FKq2lu6ajubelald9b5is3d3018` (`bird_keeper_id`),
  KEY `FKceyqhhy8o78bp364wve0c3lty` (`founder_id`),
  CONSTRAINT `FKceyqhhy8o78bp364wve0c3lty` FOREIGN KEY (`founder_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKd2vv1alrlwwfholxigcqcfuuc` FOREIGN KEY (`settings_id`) REFERENCES `group_settings` (`id`),
  CONSTRAINT `FKq2lu6ajubelald9b5is3d3018` FOREIGN KEY (`bird_keeper_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bird_species`
--

DROP TABLE IF EXISTS `bird_species`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bird_species` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `color` varchar(255) DEFAULT NULL,
  `common_name` varchar(255) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `habitat` varchar(255) DEFAULT NULL,
  `region` varchar(255) DEFAULT NULL,
  `scientific_name` varchar(255) NOT NULL,
  `size` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6ycx7expfqtmyi322opm77823` (`common_name`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bird_species_aliases`
--

DROP TABLE IF EXISTS `bird_species_aliases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bird_species_aliases` (
  `species_id` bigint NOT NULL,
  `alias` varchar(255) DEFAULT NULL,
  KEY `FKokn9hfm0kwj5px0rhufmtnn6a` (`species_id`),
  CONSTRAINT `FKokn9hfm0kwj5px0rhufmtnn6a` FOREIGN KEY (`species_id`) REFERENCES `bird_species` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bird_species_characteristics`
--

DROP TABLE IF EXISTS `bird_species_characteristics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bird_species_characteristics` (
  `species_id` bigint NOT NULL,
  `characteristic` varchar(255) DEFAULT NULL,
  KEY `FKid8hsfado3yrxeluj1lwqe8u0` (`species_id`),
  CONSTRAINT `FKid8hsfado3yrxeluj1lwqe8u0` FOREIGN KEY (`species_id`) REFERENCES `bird_species` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bird_submissions`
--

DROP TABLE IF EXISTS `bird_submissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bird_submissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `image_url` text NOT NULL,
  `status` varchar(255) NOT NULL,
  `submitted_at` datetime(6) NOT NULL,
  `votes` int DEFAULT NULL,
  `group_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `bird_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9e7b17yw5canci5sfxcp9xjh1` (`group_id`),
  KEY `FKst30b86jkhy1sbg3eya9vpjox` (`user_id`),
  CONSTRAINT `FK9e7b17yw5canci5sfxcp9xjh1` FOREIGN KEY (`group_id`) REFERENCES `bird_groups` (`id`),
  CONSTRAINT `FKst30b86jkhy1sbg3eya9vpjox` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `birds`
--

DROP TABLE IF EXISTS `birds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `birds` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `age` int NOT NULL,
  `color` varchar(255) NOT NULL,
  `fun_fact` text,
  `image_url` text,
  `kind` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  `uploaded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FKskqv6df1er11rej3ydalem0c0` (`user_id`),
  CONSTRAINT `FKskqv6df1er11rej3ydalem0c0` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `birds_chk_1` CHECK ((`age` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group_members`
--

DROP TABLE IF EXISTS `group_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_members` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `is_verified` bit(1) DEFAULT NULL,
  `joined_at` datetime(6) NOT NULL,
  `last_active` datetime(6) DEFAULT NULL,
  `notification_preferences` json DEFAULT NULL,
  `role` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `verification_date` datetime(6) DEFAULT NULL,
  `group_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKp940p7g0r9yihubnf6rtaheog` (`user_id`,`group_id`),
  KEY `FK9o8vmfect633qikunfys4b1fr` (`group_id`),
  CONSTRAINT `FK9o8vmfect633qikunfys4b1fr` FOREIGN KEY (`group_id`) REFERENCES `bird_groups` (`id`),
  CONSTRAINT `FKnr9qg33qt2ovmv29g4vc3gtdx` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group_settings`
--

DROP TABLE IF EXISTS `group_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `auto_approve_membership` bit(1) DEFAULT NULL,
  `guest_viewers_allowed` bit(1) DEFAULT NULL,
  `meeting_frequency` enum('WEEKLY','MONTHLY','CUSTOM') DEFAULT NULL,
  `photo_sharing_enabled` bit(1) DEFAULT NULL,
  `region` varchar(255) DEFAULT NULL,
  `seasonal_activity` enum('SPRING','SUMMER','FALL','WINTER') DEFAULT NULL,
  `verification_required` bit(1) DEFAULT NULL,
  `visibility_type` enum('PUBLIC','PRIVATE','INVITE_ONLY') DEFAULT NULL,
  `theme` varchar(50) DEFAULT 'default',
  `group_image_url` varchar(1000) DEFAULT NULL,
  `current_meeting_started_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `submission_votes`
--

DROP TABLE IF EXISTS `submission_votes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `submission_votes` (
  `submission_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`submission_id`,`user_id`),
  KEY `FKafsenm9drm2hsa67lje3o19r7` (`user_id`),
  CONSTRAINT `FKafsenm9drm2hsa67lje3o19r7` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKex7x334brovl6jprmd0crvn4u` FOREIGN KEY (`submission_id`) REFERENCES `bird_submissions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `testimonials`
--

DROP TABLE IF EXISTS `testimonials`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `testimonials` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `location` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_activities`
--

DROP TABLE IF EXISTS `user_activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_activities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_url` text,
  `created_at` datetime(6) NOT NULL,
  `description` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  `bird_id` bigint DEFAULT NULL,
  `group_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbe7yq8t74yxeoarmxlxevoped` (`user_id`),
  KEY `FK8w3n7v9mxge2w8n792yq4ahfw` (`bird_id`),
  KEY `FKr9rryw7ycg56tcg89kucc17il` (`group_id`),
  CONSTRAINT `FK8w3n7v9mxge2w8n792yq4ahfw` FOREIGN KEY (`bird_id`) REFERENCES `birds` (`id`),
  CONSTRAINT `FKbe7yq8t74yxeoarmxlxevoped` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKr9rryw7ycg56tcg89kucc17il` FOREIGN KEY (`group_id`) REFERENCES `bird_groups` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_settings`
--

DROP TABLE IF EXISTS `user_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `theme` varchar(50) DEFAULT 'light',
  `email_notifications` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_public` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `user_settings_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `name` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  `profile_picture_url` text,
  `email_notifications` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-06 12:15:31
