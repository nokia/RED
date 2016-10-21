# RED - Robot Editor v0.7.2
## Introduction
RED Robot Editor is Eclipse based editor for RobotFramework test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.7+, preferably 1.8+  https://www.java.com/
*  RED feature only: Eclipse Neon (v 4.6),Eclipse Mars (v 4.5) or Luna (v 4.4), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
[Installation instructions](https://github.com/nokia/RED/blob/master/installation.md)

## Updates
- #48 - fix for Debugger not working if :FOR in main suite __init__ file
- #49 - fix for Running sinlge test not possible if eclipse project named different than main test directory.
- extend_pythonpath.py script doesn't support pth files directories to be modules
- fix for PyDev & RED nature conflicts - unable to set RED nature when project is PyDev type
- possibility to go to Declaration from Table editors
- fix for Documentation view - no libdoc from TC and KW when Settings has Documentation entry
- Change coloring of Given/When/And/Then
- Libs Autodiscovering should not add all modules in project recursively when virtualenv is used
- Check **[changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)** for details

## What's in the package
- Robot Perspective & Robot nature (Project,files and other file artefacts)
- Text Source editor with syntax coloring, code assistance and real-time validation
- Table editors synced with Source
- Debug Perspective for test case debugging with breakpoints, stepping, variable watch
- Remote debug
- Support for dictionaries & lists
- Eclipse help
- File support: .robot, .txt, .tsv

## Known issues
- Under Ubuntu/Xubuntu it was discovered that NullPointerException is shown when opening file from Project Explorer and clicking on file structure instead of file name. As a workaround open file by clicking on filename. 
- Cannot delete library by right click of mouse in red.xml under Ubuntu/Mint. Use Delete key as workaround
- GTK3 seems to mess up with Eclipse under Linux. Force usage of GTK2 by executing command in terminal: export SWT_GTK3=0


--------------------------------------------------------------------------------------------------------------------------
# RED - Robot Editor v0.7.1
## Introduction
RED Robot Editor is Eclipse based editor for RobotFramework test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.7+, preferably 1.8+  https://www.java.com/
*  RED feature only: Eclipse Neon (v 4.6),Eclipse Mars (v 4.5) or Luna (v 4.4), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
[Installation instructions](https://github.com/nokia/RED/blob/master/installation.md)

## Updates
- KW/TC sections folding in source - it is possible to fold sections(especially multiline Documentation of KW/TC) in Source editor. Expand All/Collapse All action can be found when right click on annotation ruller (left side of source editor where line numbers are shown)
- Proper content assist for BDD 
- Report when KW/TC with empty name
- Library auto-discovery for libs with aliases
- CTRL+Z in Tables
- Defaults in RED Product - now with UTF-8,line numbers,memory by default
- Added to help: integration with maven, libdoc reload after lib change
- validation for duplicated sections in KW/TC (RF 2.9 - 3.0 compatibility)
- Check **[changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)** for details

## What's in the package
- Robot Perspective & Robot nature (Project,files and other file artefacts)
- Text Source editor with syntax coloring, code assistance and real-time validation
- Table editors synced with Source
- Debug Perspective for test case debugging with breakpoints, stepping, variable watch
- Remote debug
- Support for dictionaries & lists
- Eclipse help
- File support: .robot, .txt, .tsv
## Known issues
- Under Ubuntu/Xubuntu it was discovered that NullPointerException is shown when opening file from Project Explorer and clicking on file structure instead of file name. As a workaround open file by clicking on filename. 
- Cannot delete library by right click of mouse in red.xml under Ubuntu/Mint. Use Delete key as workaround
- GTK3 seems to mess up with Eclipse under Linux. Force usage of GTK2 by executing command in terminal: export SWT_GTK3=0
- RED feature - Eclipse does not have UTF-8 support in editors by default. Check Preferences and RED help to get how to change it. 

## Short term plans
- Usability improvements & comments formatting in table editors


## Notes
By default, Eclipse is shipped with limited heap size which impacts performance while working with bigger projects. It is recommended to increase Xmx and Xmx parameters to higher values. Memory allocation can be done via startup arguments or by editing eclipse.ini in Eclipse folder.
We recommend to set following lines to eclipse.ini :
```
-vmargs
-Xms512m
-Xmx1024m
```
For details visit Eclipse Wiki: https://wiki.eclipse.org/FAQ_How_do_I_increase_the_heap_size_available_to_Eclipse%3F

## Feedback
We would appreciate any feedback about RED - Robot Editor, especially in every day functions like editors usability, false positive validation errors, problem with source parsing& coloring, performance etc. We are open for improvements and new functionalities. Fell free to create issue in GitHub issue tracker.


--------------------------------------------------------------------------------------------------------------------------
# RED - Robot Editor v0.7.0
## Introduction
RED Robot Editor is Eclipse based editor for RobotFramework test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.7+, preferably 1.8+  https://www.java.com/
*  RED feature only: Eclipse Neon (v 4.6),Eclipse Mars (v 4.5) or Luna (v 4.4), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
[Instructions](https://github.com/nokia/RED/blob/master/installation.md)

## Updates
- Testcases and Keywords table editors fully funcitonal - now you can edit test suites in RIDE style as well.
- Preferences - what to do after pressing Enter in cell during edit - check section Robot Framework\Editor
- Documentation View - new view to show current Keyword documentation or libdoc of selected keyword. View can be opened from Window -> Show View -> Other -> Robot -> Documentation View
- Fixes for #30 #31 #32 #33 #34 #38
- Fixes for processing variable files with Python3
- Added to help how to debug with PyDev 
- Many more small but important changes
- Check **[changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)** for details

## What's in the package
- Robot Perspective & Robot nature (Project,files and other file artefacts)
- Text Source editor with syntax coloring, code assistance and real-time validation
- Table editors synced with Source
- Debug Perspective for test case debugging with breakpoints, stepping, variable watch
- Remote debug
- Support for dictionaries & lists
- Eclipse help
- File support: .robot, .txt, .tsv

## Known issues
- Under Ubuntu/Xubuntu it was discovered that NullPointerException is shown when opening file from Project Explorer and clicking on file structure instead of file name. As a workaround open file by clicking on filename. 
- Cannot delete library by right click of mouse in red.xml under Ubuntu/Mint. Use Delete key as workaround
- GTK3 seems to mess up with Eclipse under Linux. Force usage of GTK2 by executing command in terminal: export SWT_GTK3=0
- Eclipse does not have UTF-8 support in editors by default. Check Preferences and RED help to get how to change it. 

## Short term plans
- Usability improvments