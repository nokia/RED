# RED - Robot Editor v0.6
## Introduction
RED Robot Editor is Eclipse based editor for RobotFramework test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
Oracle Java 1.7+
Eclipse Mars (v 4.5) or Luna (v 4.4)
Python & RobotFramework installed

## How to install
Download RED eclipse feature zip: <link>
Please refer to First Steps doc under https://github.com/nokia/RED/blob/master/First_steps.md

### Install on clean Eclipse 
In short:
- GUI: Help -> Install New software -> Add -> Archive and continue with prompts
- CLI: by issuing command: 
`eclipse -application org.eclipse.equinox.p2.director -nosplash -consoleLog --launcher.suppressErrors -repository jar:file:<PATH_TO_ZIP>\!/,http://download.vogella.com/luna/e4tools,http://download.eclipse.org/releases/luna/   -installIU org.robotframework.ide.eclipse.main.feature.feature.group 

### Update existing RED installation
We recommend to not to do direct update of newer version, instead perform unistall old RED and install new RED after Eclipse restart.
Uninstall:
Open Help -> Installation Details, select old RED feature and perform unistall

## Updates
- Validation scope - include/exclude folders for validation (under right click menu in Project Explorer - see help for more info)
- Fixed NullPointerExceptions for numerous cases in tsv format
- Fixed NullPointerException when RobotFramework is not installed
- Check **changelog.txt** for details

## What's in the package
- Robot Perspective & Robot nature (Project,files and other file artefacts)
- Text Source editor with syntax colouring
- Table editors (non persistent for trial usage)
- Debug Perspective for test case debugging with breakpoints, stepping, variable watch
- Remote debug
- Support for dictionaries & lists
- Code assistance 
- Eclipse help
- File support: .robot, .txt, .tsv

## Known issues
- Eclipse does not have UTF-8 support in editors by default. Check Preferences and RED help to get how to change it.
- Usage of keywords with dots is not discovered properly. Now unknown keyword error marker is shown in such cases. 
- Table editors edits are not persistent, changes done in Table editors will not be visible in source nor saved to file. Sync is done from Source -> Table only.
- Library auto-discovery is not yet supported thus user needs to add any external library to red.xml by themselves, if not then validation will show unknown library error. Either you add it in red.xml or invoke option Quick Fix on item in Source editor or in Problems view.  
- Due to difficult to comprehend and countless exceptions in RF syntax, validation can show false positive errors. In such case please provide us sample test case for us to have a look at.


## Short term plans
- Table editor persistence

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
We would appreciate any feedback about RED - Robot Editor, especially in every day functions like editors usability, false positive validation errors, problem with source parsing& colouring, performance etc. We are open for improvements and new functionalities. Fell free to create issue in GitHub issue tracker.


