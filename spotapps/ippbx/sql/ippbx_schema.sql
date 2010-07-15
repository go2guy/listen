create database ip_pbx;
use ip_pbx;

-- phpMyAdmin SQL Dump
-- version 3.3.2deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jul 15, 2010 at 12:13 PM
-- Server version: 5.1.41
-- PHP Version: 5.3.2-1ubuntu4.2

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `ip_pbx`
--

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Table structure for table `active_clients`
--

CREATE TABLE IF NOT EXISTS `active_clients` (
  `connection_id` varchar(255) NOT NULL,
  `client` varchar(50) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `active_clients`
--

-- --------------------------------------------------------

--
-- Table structure for table `active_prompts`
--

CREATE TABLE IF NOT EXISTS `active_prompts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `start` int(11) NOT NULL,
  `end` int(11) NOT NULL,
  `available_prompts_id` int(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `start` (`start`),
  UNIQUE KEY `end` (`end`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=12 ;

--
-- Dumping data for table `active_prompts`
--


-- --------------------------------------------------------

--
-- Table structure for table `available_prompts`
--

CREATE TABLE IF NOT EXISTS `available_prompts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=12 ;

--
-- Dumping data for table `available_prompts`
--

INSERT INTO `available_prompts` (`id`, `name`, `description`) VALUES
(1, '1401', 'Thanks for calling Interact Incorporated, we are currently closed to welcome in the New Year'),
(2, '1405', 'Thanks for calling Interact Incorporated, we are currently closed for the Memorial Day Holiday'),
(3, '1407', 'Thanks for calling Interact Incorporated, we are currently closed in observance of the Independence Day Holiday'),
(4, '1409', 'Thanks for calling Interact Incorporated, we are currently closed for the Labor Day Holiday'),
(5, '1411', 'Thanks for calling Interact Incorporated, we are currently closed for the Thanksgiving holiday'),
(6, '1412', 'Thanks for calling Interact Incorporated, we are currently closed for our year end meeting and Holiday party'),
(7, '1413', 'Thanks for calling Interact Incorporated, we are currently closed for the Christmas holiday'),
(8, '1414', 'Thank you for calling Interact Inc. We are currently closed for the Holidays'),
(9, '1415', 'Thank you for calling Interact Inc. We are currently closed for the Holiday'),
(10, '1416', 'Thanks for calling Interact Incorporated, we are currently closed for our year end meeting'),
(11, '1417', 'Thanks for calling Interact Incorporated, we are currently due to the inclement weather');

