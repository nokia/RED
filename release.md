# RED - Robot Editor v0.5
## Introduction
RED Robot Editor is Eclipse based editor for RobotFramework test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
Oracle Java 1.7+
Eclipse Luna (v 4.4) or Mars (v 4.5)
Python & RobotFramework 

## How to install
Download RED eclipse feature zip: https://github.com/nokia/RED/archive/0.5.zip
Please refer to First Steps doc under https://github.com/nokia/RED/blob/master/First_steps.md

In short:
- GUI: Help -> Install New software -> Add -> Archive and continue with prompts
- CLI: by issuing command: 
`eclipse -application org.eclipse.equinox.p2.director -nosplash -consoleLog --launcher.suppressErrors -repository jar:file:<PATH_TO_ZIP>\!/,http://download.vogella.com/luna/e4tools,http://download.eclipse.org/releases/luna/   -installIU org.robotframework.ide.eclipse.main.feature.feature.group 

## Updates
- issue #3 - support for txt files and resources apart from .robot  
- ***TSV file support*** added for test files and resource,Quick Fix to handle improper separators in TSV
- ***Data driven validation & colouring***
- ***Gherkin validation & colouring***
- ***Remote Debug*** - debug testcase on remote testline using RED locally. Refer to RED help for more info. 
- Variable mapping in red.xml - way to handle parametrised paths to resources and libs. 
- Library and resources aliases (library.keyword , resource.keyword) validation and colouring
- Duplicated keywords discovery
- TAB key handling customisation - what to do when TAB is pressed, check Preferences 
- Loops validation ,colouring and debug
- Keywords with embedded arguments validation and colouring
- Embedded variables validation & colouring
- Custom code colouring preference
- Commented keywords and test cases are hidden in Outline
- Performance and memory management improvements especially visible when working with big (+500mb) projects
- Automatic indent when editing test case and keyword
- Content assistance modes - insert or replace 
- Content assistance library prefix - preference to automatically add library or resource prefix for proposals 
- Extended help

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
- Table editors edits are not persistent, changes done in Table editors will not be visible in source nor saved to file. Sync is done from Source -> Table only.
- Discovery and validation of local library without extension in the same directory as testcase is problematic. For now workaround is to add library extension to library setup in test case or add library to Python path. 
- Colouring of cases such as ${string , &{string and @{string is improper. Colour indicates variable (green by default) but it should be text (black as default)
- Library auto-discovery is not yet supported thus user needs to add any external library to red.xml by themselves, if not then validation will show unknown library error. Either you add it in red.xml or invoke option Quick Fix on item in Source editor or in Problems view.  
- Due to difficult to comprehend and countless exceptions in RF syntax, validation can show false positive errors. In such case please provide us sample test case for us to have a look at.
- If RPC XML error dialogue appears, it indicates that there is an issue with importing one of python files associated with Robot test case. Please report that.   

## Short term plans
- Table editor persistence
- Check Style-like module 

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
