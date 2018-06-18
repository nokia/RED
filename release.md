# RED - Robot Editor v0.8.6
## Introduction
RED Robot Editor is Eclipse based editor for [RobotFramework](https://github.com/robotframework/robotframework) test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.8+  https://www.java.com/
*  RED feature only: Eclipse Oxygen (v 4.7), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
[Installation instructions](https://github.com/nokia/RED/blob/master/installation.md)
Due to internal changes to support Eclipse Oxygen API, RED Product 0.8.2 **cannot** be upgraded from 0.8.0 and earlier versions. 
For RED feature installed on Eclipse Neon, please upgrade your Eclipse platform: [https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F](https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F)

## Notable updates
- Adding relative paths for RobotExec and ReferencedLibs in red.xml 
- Handle environment variables in red.xml for classpath and pythonpaths

## GitHub issues updates
    #73 - red.xml - relative paths for RobotExec and ReferencedLibs
    #204 - Handle environment variables in red.xml for classpath and pythonpaths
    #203 - reload libraries with RobotCommandRpcExecutor
    #96 - Positional argument cannot be used after named arguments for usual KW (examples from RED_781)
    #210 - Autodiscovery don't work for RemoteSwingLibrary in RED 0.8.5
    #220 - Editor throws exception when opening specific .robot file
    #225 - Problems with unicode keywords using embedded variables
    #179 - A wrong error message when using embedding arguments keyword name

full changes in [changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)

# RED - Robot Editor v0.8.5
## Introduction
RED Robot Editor is Eclipse based editor for [RobotFramework](https://github.com/robotframework/robotframework) test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.8+  https://www.java.com/
*  RED feature only: Eclipse Oxygen (v 4.7), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
[Installation instructions](https://github.com/nokia/RED/blob/master/installation.md)
Due to internal changes to support Eclipse Oxygen API, RED Product 0.8.2 **cannot** be upgraded from 0.8.0 and earlier versions. 
For RED feature installed on Eclipse Neon, please upgrade your Eclipse platform: [https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F](https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F)

## Notable updates
- RED now uses html format for documentation
- many fixes in coloring in text and table editors
- Rflint now has field for command line parameters - check Preferences
- RED can use Eclipse parameters in fields for launching (rflint,run configurations) #184 - more info in help: RED - Robot Editor User Guide > User guide > Launching Tests >String substitution variables
- Graceful Robot/process killing to allow Robot to generate reports #170
- Autodiscovery and quick fix improvements

## GitHub issues updates
    #125 - try to look for python2/python3 filenames when looking for python installations
    #170 - interrupt tests processes instead of killing them
    #185 - Libdoc view from PE - does not show docs when doc contains &lt; and &gt; strings
    #180 - Add command line filed to Rflint
    #184 - Resolve Eclipse parameters in fields for launching stuff (rflint,run configs)
    #187 - Keyword with parameterized name coloring
    #194 - Coloring variables using square brackets

full changes in [changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)

# RED - Robot Editor v0.8.4
## Introduction
RED Robot Editor is Eclipse based editor for [RobotFramework](https://github.com/robotframework/robotframework) test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.8+  https://www.java.com/
*  RED feature only: Eclipse Oxygen (v 4.7), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
[Installation instructions](https://github.com/nokia/RED/blob/master/installation.md)
Due to internal changes to support Eclipse Oxygen API, RED Product 0.8.2 **cannot** be upgraded from 0.8.0 and earlier versions. 
For RED feature installed on Eclipse Neon, please upgrade your Eclipse platform: [https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F](https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F)

## Notable updates
- table contents are colored in the same manner as in source
- adding mandatory arguments proposals for Keywords in Table editors
- remote libraries are taken into Autodiscovery process and added to red.xml
- #177 TODO/Tasks implemented - check http://nokia.github.io/RED/help/user_guide/validation/tasks.md
- Library autodiscovery fixes #188 #189 #191

## GitHub issues updates
 - #177 - TODO/Tasks support - by default TODO and FIXME are handled - http://nokia.github.io/RED/help/user_guide/validation/tasks.md
 - #188 - python library module and class name different, cannot be added to red.xml
 - #189 - _init__.py in lib folder breaks QuickFix/autodicovery
 - #191 - calling robot logger with also_console=True in init breaks lib refreash

full changes in [changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)

# RED - Robot Editor v0.8.3
## Introduction
RED Robot Editor is Eclipse based editor for [RobotFramework](https://github.com/robotframework/robotframework) test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.8+  https://www.java.com/
*  RED feature only: Eclipse Oxygen (v 4.7), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
[Installation instructions](https://github.com/nokia/RED/blob/master/installation.md)
Due to internal changes to support Eclipse Oxygen API, RED Product 0.8.2 **cannot** be upgraded from 0.8.0 and earlier versions. 
For RED feature installed on Eclipse Neon, please upgrade your Eclipse platform: [https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F](https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F)

## Notable updates
- support for UTF charset mentioned in #169 and RED-914
- improved performance of autodiscovery - from now on robot dry run will be run only when quick fix will not return any libs. This also fixes SshLibrary import via __init__.py
- changed Include/Exclude - from now on, Excluded folders will not be taken into autodiscovery and keyword assistance.
- Errors/Validation can be switched off globally (under Preferences->RobotFramework->Errors/Warnings
- fixed inserting/deleting cell in Table Editor in Settings tab

## GitHub issues updates
#155 - resolve value of ${EXECDIR} in VM when used in mappings to another variable
#166 - exception thrown by debugger
#167 - allow to change text size in Source by [ctrl] + [=] and [ctrl] + [-]
#162 - incorrect "Invalid Variable syntax" error with list and dictionary variables
#169 - rflint/session server does not work with non-asci chars in filename
#160 - console output encoding problems
#174 - venv hidden folders should be handled correctly

full changes in [changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)

# RED - Robot Editor v0.8.2
## Introduction
RED Robot Editor is Eclipse based editor for [RobotFramework](https://github.com/robotframework/robotframework) test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.8+  https://www.java.com/
*  RED feature only: Eclipse Oxygen (v 4.7), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
[Installation instructions](https://github.com/nokia/RED/blob/master/installation.md)
Due to internal changes to support Eclipse Oxygen API, RED Product 0.8.2 **cannot** be upgraded from 0.8.0 and earlier versions. 
For RED feature installed on Eclipse Neon,please upgrade your Eclipse platform: [https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F](https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F)

## Notable updates
- From now RED feature will install only on Eclipse Oxygen, for earlier Eclipse version follow: [https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F](https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F)
- GitHub #158 - fixed validation and autocomplate remote libraries 
- Dark code color profile is now switcched when Dark profile is selected
- Improved look of scroll bars in Table Editors when in Dark theme
- Project Explorer tree items will not collapse when list of imported libraries changed 

## GitHub issues updates
- #158 - fixed validation and autocomplate for remote libraries 

full changes in [changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)

# RED - Robot Editor v0.8.1
## Introduction
RED Robot Editor is Eclipse based editor for [RobotFramework](https://github.com/robotframework/robotframework) test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.8+  https://www.java.com/
*  RED feature only: Eclipse Oxygen (v 4.7), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
[Installation instructions](https://github.com/nokia/RED/blob/master/installation.md)
Due to internal changes to support Eclipse Oxygen API, RED Product 0.8.1 **cannot** be upgraded from earlier versions. 
For RED feature installed on Eclipse Neon,please upgrade your Eclipse platform: [https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F](https://wiki.eclipse.org/FAQ_How_do_I_upgrade_Eclipse_IDE%3F)

## Notable updates
- Support for dark profile by adding profiles of syntax color, fixed RED GUI elements when dark. Together with [Darkest Dark Theme](https://marketplace.eclipse.org/content/darkest-dark-theme), RED looks nice. Details can be found in [docs](https://github.com/nokia/RED/blob/master/docs/help/user_guide/working_with_RED/dark_theme.md "RED with Darkest Dark Theme")
![](https://raw.githubusercontent.com/nokia/RED/master/misc/img/release_notes/0.8.1/darkest-dark.png)
- Integrated static code analysis reports from [rflint](https://github.com/boakley/robotframework-lint) into RED - details in [docs](https://github.com/nokia/RED/blob/master/docs/help/user_guide/tools_integration/rflint.md)
![](https://raw.githubusercontent.com/nokia/RED/master/misc/img/release_notes/0.8.1/rflint-1.png "rflint reports")
- Made Robot links from console a clicable links even when destination files are not reachable
- It is possible to disconnect from Robot run session leaving Robot test execution running independent from RED state
- Pause&Resume buttons added to Console View
- Fixed UTF-8 names in red.xml
- Improved memory footprint from Message Log data part due to #149
- Fixed library autodiscovery freeze when activating action on multiple inhertied folders
- Variable with no name ${} gets error marker


## GitHub issues updates
- #112 - Fixed issue when file rename triggers Referenced libraries change in red.xml
- #132 - RED correctly works with python 2.6
- #136 - Insert cell/delete cell actions in table editors
- #141 - Added Pause and Resume buttons to Console View to easy access
- #148 - Source focuses on currently edited cell
- #149 - Limited Message Log launching history from 100 to 1
- #149 - Added button+action in Message Log to clear message log view/data container
- #149 - Java memory leak during use of the message log - limit lines in Message Log

full changes in [changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)

# RED - Robot Editor v0.8.0
## Introduction
RED Robot Editor is Eclipse based editor for [RobotFramework](https://github.com/robotframework/robotframework) test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.8+  https://www.java.com/
*  RED feature only: Eclipse Neon (v 4.6),Eclipse Mars (v 4.5), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
[Installation instructions](https://github.com/nokia/RED/blob/master/installation.md)

## Notable updates
- content assist can search for patterns inside proposals (eg. searching for "og" will return "log" keyword)
- content assist can return Keywords when searching for capital letters (eg. activating content assist and writing LM will return Log Many)
- content assist now provides keywords from libs which are not included in TestSuite but available in project (entry in Referenced libraries in red.xml)
- debug is more aware which file it showing, this relates to cases when suite names are not unique within project. Debug also can check if debug run in robot is consistent to suite file being displayed.Covered in #111
- table editors should preserve empty lines and show duplicated sections. Still there is some work to do though.
- variable mappings can use already mapped variables to resolve values.Item covered in #120 
- more detailed progress bar for Automatically discover libraries from Robot Framework right-click menu
- top level entries (libraries) in library discovery summary report is now copy-able.This allows to copy libs names and install missing libraries to Robot
- filenames in tabs above editor can be prepanded with parent folder.Useful if filenames are not uniques in Project. Enabled by preference
- when setting Robot nature to Project, red.xml is not overwritten by default - proper user action is proposed


## GitHub issues updates
- #84 - references menu in source view - added view to source editor
- #95 - empty lines get removed when test case is edited from TestCases tab - empty lines should not be removed when changing from table to source editor
- #111 - In Debug Variable Panel difference between Robot Dict and Python Dict not visible - implemented
- #114 - add help content for Content Assist -> Auto activation triggers - added to Help
- #118 - use preference for content assistant trigger - now Content Assist trigger key (CTRL+space) is no longer hardcoded,it can be defined in preferences
- #120 - variable mapping could use already mapped vars in consecutive entries - implemented
- #122 - red.xml charsets issues - encoding of red.xml is no in UTF-8
- #128 - better handle projects without red.xml - for now,projects without red.xml can use Robot and system variables. Handling built-in libs will be done later. 
- #138 - do not override existing red.xml file
    
full changes in [changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)

# RED - Robot Editor v0.7.9
## Introduction
RED Robot Editor is Eclipse based editor for [RobotFramework](https://github.com/robotframework/robotframework) test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.8+  https://www.java.com/
*  RED feature only: Eclipse Neon (v 4.6),Eclipse Mars (v 4.5), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
[Installation instructions](https://github.com/nokia/RED/blob/master/installation.md)

## Updates

- #112 - revert/remove Project Rename from src for release 0.7.9 - this feature will be reimplemented
- RED can open .py files in editor instead of interpreter for the first time
- Autodiscovery takes Variable Mapping into account for discovering libs, now parametrized paths to python libs should be discovered correctly 
- Autodiscovery now discovers libs  inside resources
- Autodiscovery now uses Remote Session Server if defined

    
full changes in [changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)



# RED - Robot Editor v0.7.8
## Introduction
RED Robot Editor is Eclipse based editor for [RobotFramework](https://github.com/robotframework/robotframework) test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.8+  https://www.java.com/
*  RED feature only: Eclipse Neon (v 4.6),Eclipse Mars (v 4.5), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
[Installation instructions](https://github.com/nokia/RED/blob/master/installation.md)

## Updates

- #98 It should be possible to launch tests event without interpreter defined
- #99 Fixed font size in TestCases and Keywords tab
- #107 Settings KWs are not case sensitive
- #108 Update docs with red.xml and how to use Remote Server for libs
- Fixes in table view
- Resources are visible in project outside of workspace
- Resolved paths problems
	- Quick Fix on lib from PythonPath creates correct entry in red.xml
- Improved dealing with variable files
	- Display python/robot internal error with Error marker on Variable files when something goes wrong
	- Variables taken from external python file are reloaded after edition
	- Variable file extension should be checked
- Messege Log view could wrap its content and save it
- Changing red.xml file after renaming of project

    
full changes in [changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)
    
    
--------------------------------------------------------------------------------------------------------------------------   
# RED - Robot Editor v0.7.7-fix
## Introduction
RED Robot Editor is Eclipse based editor for RobotFramework test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.7+, preferably 1.8+  https://www.java.com/
*  RED feature only: Eclipse Neon (v 4.6),Eclipse Mars (v 4.5), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
RED 0.7.7-fix is available only in GitHub binaries,Update Site has not been updated with this version. If you whish to update RED to 0.7.7-fix, either download RED Product zip or RED feature and perform Install New Software action from Help menu with selected RED feature zip file.

## Updates
#105  - fix for running tests with Jython with classpath variable defined in OS     

--------------------------------------------------------------------------------------------------------------------------   
# RED - Robot Editor v0.7.7
## Introduction
RED Robot Editor is Eclipse based editor for [RobotFramework](https://github.com/robotframework/robotframework) test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.8+  https://www.java.com/
*  RED feature only: Eclipse Neon (v 4.6),Eclipse Mars (v 4.5), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## Installation
[Installation instructions](https://github.com/nokia/RED/blob/master/installation.md)

## Updates

- Handling old (pre-RED 0.7.6) Run/Debug configurations gracefully
- #101 Fixed error while debugging with multiple __init__ files
- #102 Added enum to handle all errors from Robot
- #78 fixed setting breakpoints in Debug perspective
- added marker for variable files when importing went wrong
- provide resource/lib content assist for Suite Setup/Teardown in Settings
- add preference how User Script arguments should be passed (wrapped by "" or space separated)
- RED help moved to GitHub pages: (http://nokia.github.io/RED/)[http://nokia.github.io/RED/]
- changed some help images to animations

    
full changes in [changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)

        
--------------------------------------------------------------------------------------------------------------------------   
# RED - Robot Editor v0.7.6
## Introduction
RED Robot Editor is Eclipse based editor for [RobotFramework](https://github.com/robotframework/robotframework) test cases. 
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
- #86 - wrap content of cells in table editors. Check : **Preferences->Robot Framework->Editor->Folding**
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
