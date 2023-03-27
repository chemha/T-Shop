-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema t-shop
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema t-shop
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `t-shop` DEFAULT CHARACTER SET utf8 ;
USE `t-shop` ;

-- -----------------------------------------------------
-- Table `t-shop`.`Client`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `t-shop`.`Client` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `surname` VARCHAR(50) NOT NULL,
  `birthdate` DATE NOT NULL,
  `email` VARCHAR(254) NOT NULL,
  `password` VARCHAR(128) NOT NULL,
  `admin` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `t-shop`.`ClientAddress`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `t-shop`.`ClientAddress` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `client_id` INT UNSIGNED NOT NULL,
  `country` VARCHAR(45) NOT NULL,
  `postal_code` VARCHAR(10) NOT NULL,
  `street` VARCHAR(100) NOT NULL,
  `home` VARCHAR(50) NULL,
  `apartment` VARCHAR(50) NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_ClientAddress_Client1_idx` (`client_id` ASC) VISIBLE,
  CONSTRAINT `fk_ClientAddress_Client1`
    FOREIGN KEY (`client_id`)
    REFERENCES `t-shop`.`Client` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `t-shop`.`Order`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `t-shop`.`Order` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `client_id` INT UNSIGNED NOT NULL,
  `address_id` INT UNSIGNED NOT NULL,
  `payment_method` VARCHAR(10) NOT NULL,
  `delivery_method` VARCHAR(10) NOT NULL,
  `payment_status` VARCHAR(10) NOT NULL,
  `order_status` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_Order_Client_idx` (`client_id` ASC) VISIBLE,
  INDEX `fk_Order_ClientAddress1_idx` (`address_id` ASC) VISIBLE,
  CONSTRAINT `fk_Order_Client`
    FOREIGN KEY (`client_id`)
    REFERENCES `t-shop`.`Client` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Order_ClientAddress1`
    FOREIGN KEY (`address_id`)
    REFERENCES `t-shop`.`ClientAddress` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `t-shop`.`Category`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `t-shop`.`Category` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `title_UNIQUE` (`title` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `t-shop`.`Product`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `t-shop`.`Product` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `category_id` INT UNSIGNED NOT NULL,
  `title` VARCHAR(50) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `brand` VARCHAR(30) NULL,
  `color` VARCHAR(30) NULL,
  `weight` DECIMAL(10,2) NULL,
  `volume` DECIMAL(10,2) NULL,
  `quantity` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_Product_Category1_idx` (`category_id` ASC) VISIBLE,
  CONSTRAINT `fk_Product_Category1`
    FOREIGN KEY (`category_id`)
    REFERENCES `t-shop`.`Category` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `t-shop`.`Good`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `t-shop`.`Good` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `order_id` INT UNSIGNED NOT NULL,
  `product_id` INT UNSIGNED NOT NULL,
  `quantity` INT UNSIGNED NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_Good_Product1_idx` (`product_id` ASC) VISIBLE,
  INDEX `fk_Good_Order1_idx` (`order_id` ASC) VISIBLE,
  CONSTRAINT `fk_Good_Product1`
    FOREIGN KEY (`product_id`)
    REFERENCES `t-shop`.`Product` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Good_Order1`
    FOREIGN KEY (`order_id`)
    REFERENCES `t-shop`.`Order` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;


-- -----------------------------------------------------
-- Test data
-- -----------------------------------------------------

INSERT INTO `t-shop`.`Client`
(`id`,
`name`,
`surname`,
`birthdate`,
`email`,
`password`,
`admin`)
VALUES
(1,
'admin',
'admin',
'2000-01-01',
'admin@admin.admin',
-- Password is admin
'$2a$10$WmmU6.wZnniP524nKOCRy.CsHe6.k109..ezFI9OiidmsRYIR/3aW',
true);

INSERT INTO `t-shop`.`Client`
(`id`,
`name`,
`surname`,
`birthdate`,
`email`,
`password`,
`admin`)
VALUES
(2,
'test',
'test',
'2000-01-01',
'test@test.test',
-- Password is test
'$2a$10$2.3RP8B0uhCx8.vRPo7oQuyB8mNBK7L1I7zmiZ7scH4Tq37fSfSsu',
false);

INSERT INTO `t-shop`.`ClientAddress`
(`id`,
`client_id`,
`country`,
`postal_code`,
`street`,
`home`,
`apartment`)
VALUES
(1,
1,
'test',
1234,
'test',
'test',
'test');

INSERT INTO `t-shop`.`Category`
(`id`,
`title`)
VALUES
(1,
'test');

INSERT INTO `t-shop`.`Product`
(`id`,
`category_id`,
`title`,
`price`,
`brand`,
`color`,
`weight`,
`volume`,
`quantity`)
VALUES
(1,
1,
'test',
1.23,
'test',
'test',
1.23,
1.23,
1);

INSERT INTO `t-shop`.`Order`
(`id`,
`client_id`,
`address_id`,
`payment_method`,
`delivery_method`,
`payment_status`,
`order_status`)
VALUES
(1,
1,
1,
'test',
'test',
'test',
'test');

INSERT INTO `t-shop`.`Good`
(`id`,
`order_id`,
`product_id`,
`quantity`)
VALUES
(1,
1,
1,
1);

-- -----------------------------------------------------
-- Sample data
-- -----------------------------------------------------

INSERT INTO `t-shop`.`Client` (`name`, `surname`, `birthdate`, `email`, `password`, `admin`)
VALUES
('John', 'Doe', '1990-01-01', 'john.doe@example.com', 'password', 0),
('Jane', 'Doe', '1992-05-10', 'jane.doe@example.com', 'password', 0),
('Mike', 'Smith', '1985-12-25', 'mike.smith@example.com', 'password', 0),
('Samantha', 'Johnson', '1998-07-20', 'samantha.johnson@example.com', 'password', 0),
('David', 'Brown', '1995-09-15', 'david.brown@example.com', 'password', 0),
('Sarah', 'Jones', '1994-03-12', 'sarah.jones@example.com', 'password', 0),
('Robert', 'Lee', '1988-11-07', 'robert.lee@example.com', 'password', 0),
('Emily', 'Wilson', '2000-02-18', 'emily.wilson@example.com', 'password', 0),
('Benjamin', 'Taylor', '1997-06-30', 'benjamin.taylor@example.com', 'password', 0),
('Oliver', 'Clark', '1991-09-23', 'oliver.clark@example.com', 'password', 0),
('Lucy', 'Hall', '1996-12-04', 'lucy.hall@example.com', 'password', 0),
('Max', 'Harris', '1993-08-08', 'max.harris@example.com', 'password', 0),
('Lily', 'Young', '1999-04-29', 'lily.young@example.com', 'password', 0),
('Ethan', 'Wright', '1990-07-16', 'ethan.wright@example.com', 'password', 0),
('Ava', 'King', '1998-05-02', 'ava.king@example.com', 'password', 0);

INSERT INTO `t-shop`.`ClientAddress` (`client_id`, `country`, `postal_code`, `street`, `home`, `apartment`)
VALUES
(1, 'USA', '10001', '5th Ave', '20', '10A'),
(2, 'USA', '10002', 'Broadway', '25', '8B'),
(3, 'Canada', 'M5J 2T3', 'Queens Quay W', '15', NULL),
(4, 'UK', 'EC1A 1BB', 'Cheapside', '30', '5C'),
(5, 'Australia', '2000', 'George St', '40', '12A'),
(6, 'USA', '10003', 'Union Square E', '35', '9D'),
(7, 'UK', 'SW1A 1AA', 'Whitehall', '10', '3B'),
(8, 'Canada', 'V6C 3G1', 'Hastings St W', '5', '6A'),
(9, 'USA', '10004', 'Broad St', '45', '7C'),
(10, 'Australia', '3000', 'Swanston St', '50', NULL);

INSERT INTO `t-shop`.`Category` (`title`)
VALUES
('Laptops'),
('Desktops'),
('Tablets'),
('Printers'),
('Monitors'),
('Accessories'),
('Gaming PCs'),
('Workstations'),
('2-in-1 Laptops'),
('Inkjet Printers'),
('Laser Printers'),
('Gaming Monitors'),
('Office Monitors'),
('Keyboards'),
('Mice'),
('Webcams');

INSERT INTO `t-shop`.`Product` (`category_id`, `title`, `price`, `brand`, `color`, `weight`, `volume`, `quantity`)
VALUES
(1, 'Dell XPS 13', 1099.99, 'Dell', 'Silver', 1.27, 11.64, 10),
(1, 'MacBook Pro', 1799.99, 'Apple', 'Space Gray', 1.4, 14.9, 5),
(2, 'HP Pavilion Desktop', 599.99, 'HP', 'Black', 5.15, 13.28, 7),
(2, 'Alienware Aurora R12', 1999.99, 'Alienware', 'Lunar Light', 14.94, 28.2, 3),
(3, 'Microsoft Surface Pro 7', 899.99, 'Microsoft', 'Platinum', 0.77, 5.3, 8),
(3, 'Samsung Galaxy Tab S7+', 849.99, 'Samsung', 'Mystic Black', 0.91, 6.44, 6),
(4, 'HP OfficeJet Pro 9025e', 349.99, 'HP', 'White', 8.2, 14.45, 4),
(4, 'Canon PIXMA TR4520', 99.99, 'Canon', 'Black', 5.4, 11.7, 9),
(5, 'Dell UltraSharp U2720Q', 719.99, 'Dell', 'Black', 13.6, 15.61, 2),
(5, 'ASUS TUF Gaming VG27AQ', 499.99, 'ASUS', 'Black', 8.8, 16.92, 6),
(6, 'Logitech MX Master 3', 99.99, 'Logitech', 'Midnight Teal', 0.14, 0.07, 12),
(6, 'HyperX Cloud II', 99.99, 'HyperX', 'Red', 0.32, 0.14, 8),
(6, 'Logitech C920s Pro HD Webcam', 79.99, 'Logitech', 'Black', 0.16, 0.04, 5);
