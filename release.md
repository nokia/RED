# RED - Robot Editor v0.7.5-fix
## Introduction
RED Robot Editor is Eclipse based editor for RobotFramework test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.7+, preferably 1.8+  https://www.java.com/
*  RED feature only: Eclipse Neon (v 4.6),Eclipse Mars (v 4.5) or Luna (v 4.4), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
RED 0.7.5-fix is available only in GitHub binaries,Update Site has not been updated with this version. If you whish to update RED to 0.7.5-fix, either download RED Product zip or RED feature and perform Install New Software action from Help menu with selected RED feature zip file.

## Updates
#81 - fix for autodiscovery of Selenium2Library, from now Selenium2Lib will be discovered like in pre 0.7.5 but no jump to line in python sources will be available. This is due to specific Selenium2Lib keywords definition type. 