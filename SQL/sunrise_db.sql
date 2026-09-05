CREATE DATABASE IF NOT EXISTS `sunrise_db`;
USE `sunrise_db`;

CREATE TABLE `patients` (
  `patient_id` int NOT NULL AUTO_INCREMENT,
  `patient_name` varchar(100) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `contact_no` varchar(15) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`patient_id`);

INSERT INTO `patients` VALUES 
(1,'Mr. Kamal Perera','58/18/A, Swarna road, Colombo','071846987','2026-08-19 15:20:48'),
(2,'Mr. Sunil Jayamaha Updated','58/15, Galle road,Colombo','07752321545','2026-08-20 04:10:53'),
(3,'Mr. Shehan Abeysinghe','89, Helan Lane, Colombo','0755889640','2026-08-20 04:11:35'),
(4,'Mr. Anil Jayasinghe','58/15, Sarana Road, Malabe','0755889645','2026-08-20 04:14:00'),

-- 2. Dentists Table
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
  UNIQUE KEY `username` (`username`);

INSERT INTO `dentists` VALUES 
(1,'Dr. Shehan Jayasinghe','dr.shehan','shehan123','General Dentistry','0771234567',7580.00,'2026-08-19 15:13:47'),
(2,'Dr. Shevon De Silva','dr.shevon','shevon123','Orthodontics','0777654321',8500.00,'2026-08-19 15:13:47');

-- 3. Staff Table
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `staff` VALUES 
(1,'Admin User','admin','admin123','0112345678','ADMIN','2026-08-19 15:13:47'),
(2,'Admin 1','admin1','jagath123','0775815493','ADMIN','2026-08-19 17:09:49'),
(3,'Shehara Hansani','shehara01','shehara123','0779696856','RECEPTIONIST','2026-09-01 17:32:00');

-- 4. Treatment Types Table
DROP TABLE IF EXISTS `treatment_types`;
CREATE TABLE `treatment_types` (
  `treatment_id` int NOT NULL AUTO_INCREMENT,
  `treatment_name` varchar(100) NOT NULL,
  `treatment_cost` decimal(10,2) NOT NULL,
  PRIMARY KEY (`treatment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `treatment_types` VALUES 
(1,'Tooth Filling',8000.00),
(2,'Tooth Extraction',9500.00),
(3,'Root Canal Treatment',15000.00),
(4,'Teeth Cleaning (Scaling)',5500.00),
(5,'Braces Fitting',45000.00),
(6,'Inlays and onlays',12500.00);

-- 5. Appointments Table
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
  CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patients` (`patient_id`) ON DELETE CASCADE,
  CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`dentist_id`) REFERENCES `dentists` (`dentist_id`) ON DELETE CASCADE,
  CONSTRAINT `appointments_ibfk_3` FOREIGN KEY (`treatment_id`) REFERENCES `treatment_types` (`treatment_id`) ON DELETE CASCADE,
  CONSTRAINT `appointments_ibfk_4` FOREIGN KEY (`booked_by_username`) REFERENCES `staff` (`username`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `appointments` VALUES 
('APT0001',1,2,5,'admin','2026-08-22','10:25:00','COMPLETED','2026-08-19 15:20:48'),
('APT0002',2,2,6,'admin','2026-08-20','16:30:00','COMPLETED','2026-08-20 04:10:53'),
('APT0003',3,2,5,'admin','2026-08-20','17:00:00','COMPLETED','2026-08-20 04:11:35'),
('APT0004',4,2,4,'admin','2026-08-22','10:02:00','CANCELLED','2026-08-20 04:14:00'),


-- 6. Bills Table
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
  CONSTRAINT `bills_ibfk_1` FOREIGN KEY (`appointment_no`) REFERENCES `appointments` (`appointment_no`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `bills` VALUES 
(1,'APT0001',45000.00,2000.00,47000.00,'2026-08-19 15:53:12'),
(2,'APT0002',4500.00,2000.00,6500.00,'2026-08-20 04:21:56'),
(3,'APT0005',45000.00,8700.00,53700.00,'2026-09-02 10:00:21');

-- 7. Notices Table
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
  CONSTRAINT `notices_ibfk_2` FOREIGN KEY (`sent_by`) REFERENCES `staff` (`username`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `notices` VALUES 
(1,2,'hello','admin',1,'2026-08-20 05:38:25'),
(2,2,'hi','admin',1,'2026-08-20 05:38:45'),
(3,2,'hi','admin',1,'2026-08-20 05:40:48'),
(4,2,'Hello Doc!!!','admin',1,'2026-09-01 08:24:00');

SET FOREIGN_KEY_CHECKS = 1;