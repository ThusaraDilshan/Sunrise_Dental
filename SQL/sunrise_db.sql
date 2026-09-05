CREATE DATABASE IF NOT EXISTS `sunrise_db`;
USE `sunrise_db`;

--
-- Table structure for table `patients`
--

DROP TABLE IF EXISTS `patients`;
CREATE TABLE `patients` (
  `patient_id` int NOT NULL AUTO_INCREMENT,
  `patient_name` varchar(100) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `contact_no` varchar(15) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`patient_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `patients`
--

LOCK TABLES `patients` WRITE;
INSERT INTO `patients` VALUES 
(1,'Kamal','58/18/A,Swarna road,Colombo','071846987','2026-08-19 20:50:48'),
(2,'Jayamaha','58/15,galle road,Colombo','07752321545','2026-08-20 09:40:53'),
(3,'Abeysinghe','89,Helan Lane,colombo','0755889640','2026-08-20 09:41:35'),
(4,'Anil Jayasinghe','58/15, Sarana Road, Malabe','0755889645','2026-08-20 09:44:00');
UNLOCK TABLES;

--
-- Table structure for table `staff`
--

DROP TABLE IF EXISTS `staff`;
CREATE TABLE `staff` (
  `staff_id` int NOT NULL AUTO_INCREMENT,
  `staff_name` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `contact_no` varchar(15) DEFAULT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'RECEPTIONIST',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`staff_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `staff`
--

LOCK TABLES `staff` WRITE;
INSERT INTO `staff` VALUES 
(2,'Super Admin','admin','admin123','0775801800','ADMIN','2026-08-19 20:43:47'),
(4,'Admin 1','admin1','jagath123','0775815493','ADMIN','2026-08-19 22:39:49'),
(5,'Thehara Saduni','saduni1','saduni123','0775815487','COORDINATOR','2026-08-19 22:42:33'),
(6,'shehara hansani','hansani1','hansani123','0775956332','RECEPTIONIST','2026-08-19 22:43:16'),
(7,'Nehan Vidulak','nehan1','nehan123','0779696851','TECHNOLOGIST','2026-08-19 22:43:48');
UNLOCK TABLES;

--
-- Table structure for table `dentists`
--

DROP TABLE IF EXISTS `dentists`;
CREATE TABLE `dentists` (
  `dentist_id` int NOT NULL AUTO_INCREMENT,
  `dentist_name` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `specialization` varchar(100) DEFAULT NULL,
  `contact_no` varchar(15) DEFAULT NULL,
  `consultation_fee` decimal(10,2) NOT NULL DEFAULT '1500.00',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`dentist_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `dentists`
--

LOCK TABLES `dentists` WRITE;
INSERT INTO `dentists` VALUES 
(1,'Dr. Nimal Perera','dr.nimal','dent123','General Dentistry','0771234567',1500.00,'2026-08-19 20:43:47'),
(2,'Dr. Kamala Silva','dr.kamala','dent456','Orthodontics','0777654321',2000.00,'2026-08-19 20:43:47');
UNLOCK TABLES;

--
-- Table structure for table `treatment_types`
--

DROP TABLE IF EXISTS `treatment_types`;
CREATE TABLE `treatment_types` (
  `treatment_id` int NOT NULL AUTO_INCREMENT,
  `treatment_name` varchar(100) NOT NULL,
  `treatment_cost` decimal(10,2) NOT NULL,
  PRIMARY KEY (`treatment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `treatment_types`
--

LOCK TABLES `treatment_types` WRITE;
INSERT INTO `treatment_types` VALUES 
(1,'Tooth Filling',3000.00),
(2,'Tooth Extraction',4000.00),
(3,'Root Canal Treatment',15000.00),
(4,'Teeth Cleaning (Scaling)',2500.00),
(5,'Braces Fitting',45000.00),
(7,'Inlays and onlays',4500.00);
UNLOCK TABLES;

--
-- Table structure for table `appointments`
--

DROP TABLE IF EXISTS `appointments`;
CREATE TABLE `appointments` (
  `appointment_no` varchar(20) NOT NULL,
  `patient_id` int NOT NULL,
  `dentist_id` int NOT NULL,
  `treatment_id` int NOT NULL,
  `booked_by_username` varchar(50) DEFAULT NULL,
  `appointment_date` date NOT NULL,
  `appointment_time` time NOT NULL,
  `status` varchar(20) DEFAULT 'PENDING',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`appointment_no`),
  KEY `patient_id` (`patient_id`),
  KEY `dentist_id` (`dentist_id`),
  KEY `treatment_id` (`treatment_id`),
  KEY `booked_by_username` (`booked_by_username`),
  CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`),
  CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`dentist_id`) REFERENCES `dentists` (`dentist_id`),
  CONSTRAINT `appointments_ibfk_3` FOREIGN KEY (`treatment_id`) REFERENCES `treatment_types` (`treatment_id`),
  CONSTRAINT `appointments_ibfk_4` FOREIGN KEY (`booked_by_username`) REFERENCES `staff` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `appointments`
--

LOCK TABLES `appointments` WRITE;
INSERT INTO `appointments` VALUES 
('APT0001',1,2,5,'admin','2026-08-22','10:25:00','COMPLETED','2026-08-19 20:50:48'),
('APT0002',2,2,7,'admin','2026-08-20','04:30:00','COMPLETED','2026-08-20 09:40:53'),
('APT0003',3,2,5,'admin','2026-08-20','05:20:00','COMPLETED','2026-08-20 09:41:35'),
('APT0004',4,2,4,'admin','2026-08-22','10:02:00','CANCELLED','2026-08-20 09:44:00');
UNLOCK TABLES;

--
-- Table structure for table `bills`
--

DROP TABLE IF EXISTS `bills`;
CREATE TABLE `bills` (
  `bill_id` int NOT NULL AUTO_INCREMENT,
  `appointment_no` varchar(20) NOT NULL,
  `treatment_cost` decimal(10,2) NOT NULL,
  `consultation_fee` decimal(10,2) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `bill_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`bill_id`),
  KEY `appointment_no` (`appointment_no`),
  CONSTRAINT `bills_ibfk_1` FOREIGN KEY (`appointment_no`) REFERENCES `appointments` (`appointment_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `bills`
--

LOCK TABLES `bills` WRITE;
INSERT INTO `bills` VALUES 
(1,'APT0001',45000.00,2000.00,47000.00,'2026-08-19 21:23:12'),
(2,'APT0002',4500.00,2000.00,6500.00,'2026-08-20 09:51:56');
UNLOCK TABLES;

--
-- Table structure for table `notices`
--

DROP TABLE IF EXISTS `notices`;
CREATE TABLE `notices` (
  `notice_id` int NOT NULL AUTO_INCREMENT,
  `dentist_id` int NOT NULL,
  `description` text NOT NULL,
  `sent_by` varchar(50) DEFAULT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notice_id`),
  KEY `dentist_id` (`dentist_id`),
  KEY `sent_by` (`sent_by`),
  CONSTRAINT `notices_ibfk_1` FOREIGN KEY (`dentist_id`) REFERENCES `dentists` (`dentist_id`) ON DELETE CASCADE,
  CONSTRAINT `notices_ibfk_2` FOREIGN KEY (`sent_by`) REFERENCES `staff` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `notices`
--

LOCK TABLES `notices` WRITE;
INSERT INTO `notices` VALUES 
(1,2,'hello','admin',1,'2026-08-20 11:08:25'),
(2,2,'hi','admin',1,'2026-08-20 11:08:45'),
(3,2,'hi','admin',1,'2026-08-20 11:10:48');
UNLOCK TABLES;
