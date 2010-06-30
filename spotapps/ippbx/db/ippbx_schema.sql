-- phpMyAdmin SQL Dump
-- version 2.11.10
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jun 30, 2010 at 07:39 AM
-- Server version: 5.0.22
-- PHP Version: 5.1.6

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `ip_pbx`
--

-- --------------------------------------------------------

--
-- Table structure for table `auto_attendant_prompts`
--

CREATE TABLE IF NOT EXISTS `auto_attendant_prompts` (
  `start` datetime NOT NULL,
  `end` datetime NOT NULL,
  `prompt` varchar(255) NOT NULL,
  UNIQUE KEY `start` (`start`),
  UNIQUE KEY `end` (`end`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `auto_attendant_prompts`
--


-- --------------------------------------------------------

--
-- Table structure for table `extension_map`
--

CREATE TABLE IF NOT EXISTS `extension_map` (
  `ext` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `contact_phone` int(11) default NULL,
  `email` varchar(255) default NULL,
  `location` varchar(25) NOT NULL default 'lincoln',
  `in_use` varchar(1) NOT NULL default 'n',
  PRIMARY KEY  (`ext`,`name`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `extension_map`
--

INSERT INTO `extension_map` (`ext`, `name`, `contact_phone`, `email`, `location`, `in_use`) VALUES
(373, 'AARON BONEBRIGHT', 2147483647, 'bonebrighta', 'lincoln', 'n'),
(364, 'ADRIAN OLIVERA', 2147483647, 'oliveraa', 'lincoln', 'n'),
(281, 'ANTHONY FOSTER', 2147483647, 'fostera', 'lincoln', 'n'),
(282, 'AUSTIN SHOEMAKER', 2147483647, 'shoemakera', 'lincoln', 'n'),
(309, 'BRAD MARTIN', 2147483647, 'martinb', 'lincoln', 'n'),
(356, 'BRIAN JOHNSTON', 2147483647, 'brian', 'lincoln', 'n'),
(283, 'CALEB CASSEL', 2147483647, 'casselc', 'lincoln', 'n'),
(348, 'CARRIE POWERS', 2147483647, 'powersc', 'lincoln', 'n'),
(393, 'CHAD SCRIBNER', 2147483647, 'scribnerc', 'lincoln', 'n'),
(117, 'CHRIS DAVIS', 0, 'davisc', 'austin', 'n'),
(400, 'CHUCK KENNEDY', 2147483647, 'chuck', 'pheonix', 'n'),
(288, 'DARYN WARRINER', 2147483647, 'daryn', 'lincoln', 'n'),
(322, 'DAVID PLOTT', 2147483647, 'plottd', 'lincoln', 'n'),
(383, 'DOUGLAS PAGE', 2147483647, 'paged', 'lincoln', 'n'),
(111, 'GREG GISSLER', 2147483647, 'greg', 'lincoln', 'n'),
(379, 'GREG ZOUBEK', 2147483647, 'zoubekg', 'lincoln', 'n'),
(358, 'HERB SCRUGGS', 2147483647, 'herb', 'lincoln', 'n'),
(357, 'JOE EBMEIER', 2147483647, 'ebmeierj', 'lincoln', 'n'),
(354, 'JOE REED', 2147483647, 'joe', 'lincoln', 'n'),
(391, 'JOE SMITH', 2147483647, 'smithj', 'lincoln', 'n'),
(121, 'JOHN SEACREST', 0, 'johns', 'lincoln', 'n'),
(101, 'JULIE SHESTAK', 2147483647, 'shestakj', 'lincoln', 'n'),
(382, 'KATHLEEN SHERIDAN', 2147483647, 'sheridank', 'lincoln', 'n'),
(369, 'KATIE SHERIDAN', 2147483647, 'kasheridan', 'lincoln', 'n'),
(355, 'KARINE YAPP', 0, 'karine', 'lincoln', 'n'),
(347, 'LADI AKINYEMI', 2147483647, 'ladi', 'lincoln', 'n'),
(330, 'LAUREEN EHMEN', 2147483647, 'laureen', 'lincoln', 'n'),
(156, 'LOUIS STRICKLAND', 2147483647, 'stricklandl', 'austin', 'n'),
(141, 'LYNN MCKEE', 2147483647, 'lynn', 'lincoln', 'n'),
(342, 'MARK WEMHOFF', 2147483647, 'wemhoffm', 'lincoln', 'n'),
(115, 'MICHAEL GLOVER', 2147483647, 'gloverm', 'austin', 'n'),
(312, 'MICHAEL SCHULTZ', 2147483647, 'schultzm', 'lincoln', 'n'),
(181, 'MICHELLE MICHENER', 2147483647, 'michenerm', 'lincoln', 'n'),
(365, 'NICK WALTER', 2147483647, 'waltern', 'lincoln', 'n'),
(280, 'PHIL VAN BEVEREN', 2147483647, 'phil', 'lincoln', 'n'),
(362, 'PHILLIP RAPP', 2147483647, 'rappp', 'lincoln', 'n'),
(388, 'RANDY WHITE', 2147483647, 'whiter', 'lincoln', 'n'),
(112, 'RICARDO MASTROLEO', 2147483647, 'mastrolr', 'austin', 'n'),
(290, 'ROB HRUSKA', 2147483647, 'hruskar', 'lincoln', 'n'),
(371, 'SCOTT FARWELL', 2147483647, 'scott', 'lincoln', 'n'),
(115, 'SHARON ZELENY', 2147483647, 'sharon', 'lincoln', 'n'),
(363, 'SHERRI WAHL', 2147483647, 'wahls', 'lincoln', 'n'),
(359, 'TODD BRITSON', 2147483647, 'todd', 'lincoln', 'n'),
(378, 'TONY WILBRAND', 2147483647, 'wilbrandt', 'lincoln', 'n'),
(313, 'VALERIE STEHLIK', 2147483647, 'stehlikv', 'lincoln', 'n'),
(367, 'VARIYA RUENPROM', 0, 'ruenpromv', 'lincoln', 'n'),
(116, 'WAYNE WILSON', 2147483647, 'wilsonw', 'austin', 'n');

-- --------------------------------------------------------

--
-- Table structure for table `operator`
--

CREATE TABLE IF NOT EXISTS `operator` (
  `location` varchar(25) NOT NULL default 'lincoln',
  `ext` int(11) NOT NULL default '101'
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `operator`
--

INSERT INTO `operator` (`location`, `ext`) VALUES
('austin', 116),
('lincoln', 101);

-- --------------------------------------------------------

--
-- Table structure for table `prompt_numbers`
--

CREATE TABLE IF NOT EXISTS `prompt_numbers` (
  `number` int(4) NOT NULL,
  `description` varchar(255) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Dumping data for table `prompt_numbers`
--

INSERT INTO `prompt_numbers` (`number`, `description`) VALUES
(1401, '"Thanks for calling Interact Incorporated, we are currently closed to welcome in the New Year"'),
(1405, '"Thanks for calling Interact Incorporated, we are currently closed for the Memorial Day Holiday"'),
(1407, '"Thanks for calling Interact Incorporated, we are currently closed in observance of the Independence Day Holiday"'),
(1409, '"Thanks for calling Interact Incorporated, we are currently closed for the Labor Day Holiday"'),
(1411, '"Thanks for calling Interact Incorporated, we are currently closed for the Thanksgiving holiday"'),
(1412, '"Thanks for calling Interact Incorporated, we are currently closed for our year end meeting and Holiday party"'),
(1413, '"Thanks for calling Interact Incorporated, we are currently closed for the Christmas holiday"'),
(1414, '"Thank you for calling Interact Inc. We are currently closed for the Holidays"'),
(1415, '"Thank you for calling Interact Inc. We are currently closed for the Holiday"'),
(1416, '"Thanks for calling Interact Incorporated, we are currently closed for our year end meeting"'),
(1417, '"Thanks for calling Interact Incorporated, we are currently due to the inclement weather"');
