# RED - Robot Editor v0.6.4
## Introduction
RED Robot Editor is Eclipse based editor for RobotFramework test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
*  Oracle Java 1.7+, preferably 1.8+  https://www.java.com/
*  RED feature only: Eclipse Mars (v 4.5) or Luna (v 4.4), preferably IDE for Java Developers  https://www.eclipse.org/downloads/
*  Python/Jython & RobotFramework installed

## How to install
### Product
Product zip is ready to use RED installation bundled with Eclipse Mars. There is no need to install RED feature into it. Choose proper OS version from **Downloads** section.

### Feature
Download RED eclipse feature zip: https://github.com/nokia/RED/releases/download/0.6.4/RED_0.6.4.201605051229.zip
Please refer to First Steps doc under https://github.com/nokia/RED/blob/master/First_steps.md

#### Install form Marketplace/Update Site
Click Help -> Eclipse Marketplace -> and type into Find field "RED"

Click Help -> Install New Software -> Add and set address in Location to:
http://master.dl.sourceforge.net/project/red-robot-editor/repository



#### Install on clean Eclipse 
In short:
- GUI: Help -> Install New software -> Add -> Archive and continue with prompts (unselect "Contact all update sites) 
- CLI: by issuing command: 

```eclipse -application org.eclipse.equinox.p2.director -nosplash -consoleLog --launcher.suppressErrors -repository jar:file:<PATH_TO_ZIP>\!/,http://download.eclipse.org/releases/mars/   -installIU org.robotframework.ide.eclipse.main.feature.feature.group ```

#### Update existing RED installation
We recommend to not to do direct update of newer version, instead perform uninstall old RED and install new RED after Eclipse restart.
Uninstall:
Open Help -> Installation Details, select old RED feature and perform uninstall

## Updates
- Corrected bug for Debug when Suite Setup was inside __init__ file for RobotFramework version greater than 3.0
- Updated RED to support :For in Suite Setup in Debug for RobotFramework 2.9 
- Check **[changelog.txt](https://github.com/nokia/RED/blob/master/changelog.txt)** for details

## What's in the package
- Robot Perspective & Robot nature (Project,files and other file artefacts)
- Text Source editor with syntax coloring, code assistance and real-time validation
- Table editors (non persistent for trial usage)
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
- Table editors edits are not persistent, changes done in Table editors will not be visible in source nor saved to file. Sync is done from Source -> Table only.
- Due to difficult to comprehend and countless exceptions in RF syntax, validation can show false positive errors. In such case please provide us sample test case for us to have a look at.

## Short term plans
- Table Editor persistency



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
