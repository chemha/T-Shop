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
  `name` VARCHAR(50) NULL,
  `surname` VARCHAR(50) NULL,
  `birthdate` DATE NULL,
  `email` VARCHAR(254) NOT NULL,
  `password` VARCHAR(128) NOT NULL,
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
  INDEX `fk_Client_adress_Client_idx` (`client_id` ASC) VISIBLE,
  CONSTRAINT `fk_Client_adress_Client`
    FOREIGN KEY (`client_id`)
    REFERENCES `t-shop`.`Client` (`id`)
    ON DELETE NO ACTION
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
  INDEX `fk_Order_Client1_idx` (`client_id` ASC) VISIBLE,
  INDEX `fk_Order_Client_adress1_idx` (`address_id` ASC) VISIBLE,
  CONSTRAINT `fk_Order_Client1`
    FOREIGN KEY (`client_id`)
    REFERENCES `t-shop`.`Client` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Order_Client_adress1`
    FOREIGN KEY (`address_id`)
    REFERENCES `t-shop`.`ClientAddress` (`id`)
    ON DELETE NO ACTION
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
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `t-shop`.`Good`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `t-shop`.`Good` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id` INT UNSIGNED NOT NULL,
  `product_id` INT UNSIGNED NOT NULL,
  `quantity` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_Good_Order1_idx` (`order_id` ASC) VISIBLE,
  INDEX `fk_Good_Product1_idx` (`product_id` ASC) VISIBLE,
  CONSTRAINT `fk_Good_Order1`
    FOREIGN KEY (`order_id`)
    REFERENCES `t-shop`.`Order` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Good_Product1`
    FOREIGN KEY (`product_id`)
    REFERENCES `t-shop`.`Product` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

-- -----------------------------------------------------
-- Sample data
-- -----------------------------------------------------

INSERT INTO `t-shop`.`Client`
(`id`,
`name`,
`surname`,
`birthdate`,
`email`,
`password`)
VALUES
(1,
'test',
'test',
'2000-01-01',
'test@test.test',
'test');

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