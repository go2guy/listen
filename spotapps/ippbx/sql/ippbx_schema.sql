create database ip_pbx;
use ip_pbx;

-- phpMyAdmin SQL Dump
-- version 2.11.10
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Aug 04, 2010 at 02:15 PM
-- Server version: 5.0.22
-- PHP Version: 5.1.6

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `ip_pbx`
--

-- --------------------------------------------------------

--
-- Table structure for table `active_clients`
--

CREATE TABLE IF NOT EXISTS `active_clients` (
  id bigint(20) not null primary key auto_increment,
  `connection_id` varchar(255) NOT NULL,
  `client` varchar(50) NOT NULL,
  `session_id` varchar(255) default NULL,
  `time_stamp` int(11) NOT NULL,
  index (time_stamp)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `active_clients`
--

-- --------------------------------------------------------

--
-- Table structure for table `active_prompts`
--

CREATE TABLE IF NOT EXISTS `active_prompts` (
  `id` int(11) NOT NULL auto_increment,
  `start` int(11) NOT NULL,
  `end` int(11) NOT NULL,
  `available_prompts_id` int(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `start` (`start`),
  UNIQUE KEY `end` (`end`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=12 ;

--
-- Dumping data for table `active_prompts`
--


-- --------------------------------------------------------

--
-- Table structure for table `available_prompts`
--

CREATE TABLE IF NOT EXISTS `available_prompts` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=14 ;

--
-- Dumping data for table `available_prompts`
--

INSERT INTO `available_prompts` (`id`, `name`, `description`) VALUES
(1, '1401', 'Thank you for calling Interact Incorporated. We are currently closed to welcome in the New Year'),
(2, '1405', 'Thank you for calling Interact Incorporated. We are currently closed for the Memorial Day Holiday'),
(3, '1407', 'Thank you for calling Interact Incorporated. We are currently closed in observance of the Independence Day Holiday'),
(4, '1409', 'Thank you for calling Interact Incorporated. We are currently closed for the Labor Day Holiday'),
(5, '1411', 'Thank you for calling Interact Incorporated. We are currently closed for the Thanksgiving holiday'),
(8, '1414', 'Thank you for calling Interact Incorporated. We are currently closed for the Holidays'),
(9, '1415', 'Thank you for calling Interact Incorporated. We are currently closed for the Holiday'),
(11, '1417', 'Thank you for calling Interact Incorporated. We are currently closed due to the inclement weather'),
(12, '1402', 'Thank you for calling Interact Incorporated. We are currently closed for a company event'),
(13, '1403', 'Thank you for calling Interact Incorporated. Our office is currently closed');

-- --------------------------------------------------------

--
-- Table structure for table `forwarded_extensions`
--

CREATE TABLE IF NOT EXISTS `forwarded_extensions` (
  `origination` int(11) NOT NULL,
  `destination` varchar(25) NOT NULL,
  `time_stamp` int(11) NOT NULL,
  PRIMARY KEY  (`origination`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `forwarded_extensions`
--
