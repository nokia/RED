# RED - Robot Editor v0.7.6
## Introduction
RED Robot Editor is Eclipse based editor for RobotFramework test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.8+  https://www.java.com/
*  RED feature only: Eclipse Neon (v 4.6),Eclipse Mars (v 4.5), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Important notice
*  **Due to overhaul of Run/Debug configurations, old run and debug configurations will not work. Please remove all old entries from Run/Debug Configuration pane.**

## Installation
[Installation instructions](https://github.com/nokia/RED/blob/master/installation.md)

## Updates

- #70 - RED can execute tests using custom user scripts (*.bat,*.exe,*.sh,*.py). 
**More info in help under User guide -> Launching Test -> Local launches scripting.** 
- RED user scripts provides possibility to debug robot&python libraries in one RED instance together with PyDev. **More info in help under: User guide -> Launching Tests -> Debugging Robot&Python with RED&PyDev**
- changes in Run/Debug configuration - removed and merged configurations types,rearranged configuration fields
- changes in how Run/Debug actions are executed - now it is contextual like in other Eclipse editors. It matters where is the focus/cursor when using general Run/Debug buttons. 
**Check User guide -> Launching Tests -> Launching preferences.**
- now RED uses argument file as Robot arguments to shorten command line.
- changes in coloring in various places
- RED will use Source editor when Debug is executed when switched to Debug perspective
- wizard will not allow to create files when there is not project 
- moved to Java 8, from now older Java versions are not supported
- #81 changes for Auto-discovery fix in Selenium2Library merged with master branch
- #91 fixed lib decoration when using jython
- added jump to Keyword definitions in cell with multiple keywords
- content assist can show resource names while typing in Settings section
- RED asks when running testsuites with Fatal errors which will fail due to Robot exception
    
full changes in [changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)

        
--------------------------------------------------------------------------------------------------------------------------    
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
    
    
    
--------------------------------------------------------------------------------------------------------------------------
# RED - Robot Editor v0.7.5
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
- #54 - RED goes to line in python file where keyword is defined
- #66 - run/debug actions on test cases in Outline
- #55 and #67 - new preference in Preferences to allow passing default additional interpreter and Robot arguments. Check Preferences -> Robot Framework-> Default Launch Configurations
- #71 - support for IronPython 64 bits - from now on IronPython 32bits adn 64 bits are separate entry in Installed Frameworks in Preferences and red.xml
- Debug breakpoints fix to include breakpoints in Suite Setup/Teardown/__init__ 
- Better reporting of FOR loop errors
- WITH NAME code coloring and code assistance
- Autodiscover fix to work when red.xml is opened and dirty,also refactoring around when to trigger autodiscovery on save
- Removed hardcoded dependency with E4tools package to get rid of errors when different version is installed already in Eclipse (Eclipse for Eclipse Committers for instance). Installation requires Internet connection to get dependencies.
- #72 - Renamed long filename used in jUnits so filepath is not longer than ~220 chars, checkout under Windows/NTFS should work if done near the root of disk.
- Check **[changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)** for details

## What's in the package
- Robot Perspective & Robot nature (Project,files and other file artifacts)
- Text Source editor with syntax coloring, code assistance and real-time validation
- Table editors synced with Source
- Debug Perspective for test case debugging with breakpoints, stepping, variable watch
- Remote debug
- Support for dictionaries & lists
- Eclipse help
- File support: .robot, .txt, .tsv

## Known issues
- GTK3 seems to mess up with Eclipse under Linux. Force usage of GTK2 by executing command in terminal: export SWT_GTK3=0
- Under Ubuntu/Xubuntu it was discovered that NullPointerException is shown when opening file from Project Explorer and clicking on file structure instead of file name. As a workaround open file by clicking on filename. Use GTK2 if possible. 
- Cannot delete library by right click of mouse in red.xml under Ubuntu/Mint. Use Delete key as workaround Use GTK2 if possible.



--------------------------------------------------------------------------------------------------------------------------
# RED - Robot Editor v0.7.4
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
- Improved performance for Content Assist when there is a large list of user defined keywords
- RED Product is now able to update itself
- Added preference to check for new version on startup
- Quick Fix can create missing resource files within Workspace
- Possibility to include/exclude files for validation in the same style as folders
- Validate file on file focus
- Error window for errors in python libraries are now resizable,copy also works
- fixes for #57,#63
- Check **[changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)** for details

## What's in the package
- Robot Perspective & Robot nature (Project,files and other file artifacts)
- Text Source editor with syntax coloring, code assistance and real-time validation
- Table editors synced with Source
- Debug Perspective for test case debugging with breakpoints, stepping, variable watch
- Remote debug
- Support for dictionaries & lists
- Eclipse help
- File support: .robot, .txt, .tsv

## Known issues
- GTK3 seems to mess up with Eclipse under Linux. Force usage of GTK2 by executing command in terminal: export SWT_GTK3=0
- Under Ubuntu/Xubuntu it was discovered that NullPointerException is shown when opening file from Project Explorer and clicking on file structure instead of file name. As a workaround open file by clicking on filename. Use GTK2 if possible. 
- Cannot delete library by right click of mouse in red.xml under Ubuntu/Mint. Use Delete key as workaround Use GTK2 if possible.



--------------------------------------------------------------------------------------------------------------------------
# RED - Robot Editor v0.7.3
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
- [table editors] display comments-only line from left side with formatting - this allows to create comment lines above executable row which describes arguments of executable row formatted row by row
- #50 - configurable list of problems/warnings - from now on, levels of errors/warnings can be changed, check Preferences/RobotFramework and help for info
- user-friendly way of adding external Resources (libraries etc) in Settings section - content assist provides list of resources and discovered libs
- Re-validate file/folder/project without running Project/Clean - new action in right-click menu under Robot Framework,check RED key shortcuts for list hints
- Automatically add indentation after :FOR loop and [Documentation] line continuation 
- Resolve resources from PythonPath
- fixes for #45,#50,#56,#58,#59,#60,#61,#62,#63
- Check **[changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)** for details

## What's in the package
- Robot Perspective & Robot nature (Project,files and other file artifacts)
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