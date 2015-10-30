# RED - Robot Editor v0.4
## Introduction
RED - Robot Editor, first public release.
RED Robot Editor is Eclipse based editor for RobotFramework test cases. 
Release contains Eclipse feature of RED Robot Editor to be installed into Eclipse. 

## Requirements 
Oracle Java 1.7+
Eclipse Luna (v 4.4) or Mars (v 4.5)
Python&RobotFramework 

## How to install
Please refer to First Steps doc under https://github.com/nokia/RED/blob/master/First_steps.md

In short:
- GUI: Help -> Install New software -> Add -> Archive and continue with prompts
- CLI: by issuing command: 
`eclipse -application org.eclipse.equinox.p2.director -nosplash -consoleLog --launcher.suppressErrors -repository jar:file:<PATH_TO_ZIP>\!/,http://download.vogella.com/luna/e4tools,http://download.eclipse.org/releases/luna/   -installIU org.robotframework.ide.eclipse.main.feature.feature.group -vmargs -Dosgi.requiredJavaVersion=1.7`


## What's in the package
- Robot Perspective & Robot nature (Project,files and other file artefacts)
- Text Source editor with syntax coloring
- Table editors (non persistent for trial usage)
- Debug Perspective for test case debuging with breakpoints, stepping, variable watch
- Support for dictionaries & lists
- Code assistance 
- Eclipse help
- File support: .robot


## Known issues
- Table editors edits are not persistent, changes done in Table editors will not be visible in source nor saved to file. Sync is done from Source -> Table only.
- Editor actions (validation,lookup etc.) for data driven,embedded keywords and Gherkin (given-when-than) are not yet supported thus validation will show errors
- Library auto-discovery is not yet supported thus user needs to add any external library to red.xml by themselves, if not then validation will show unknown library error. Either you add it in red.xml or invoke option Quick Fix on item in Source editor or in Problems view.  
- Due to difficult to comprehend and countless exceptions in RF syntax, validation can show false positive errors. In such case please provide us sample test case for us to have a look at.
- Handling of FOR block is disabled thus validation does not work ,also Outline will treat FOR block as keyword for every line of FOR block. 
- Resource handling (validation and code assistant) is limited to single level. Any multilevel inheritances are not yet handled,false positive validation marker will be shown in such cases.
- Library aliasing is not yet supported ,library keyword call by by library prefix name is not supported yet  (validation)

## Feedback
We would appreciate any feedback about RED - Robot Editor, especially in every day functions like editors usability, false positive validation errors, problem with source parsing& coloring, performance etc. We are open for imprisonments and new functionalities. Fell free to create issue in GitHub issue tracker.

## What's in the pipeline
- internal refactoring and code publish on GitHub
- Table editors with 2 way synchronisation
- For loop validation and proper handling in Outline
- Multilevel Resource inheritance handling
- Libraries aliases and prefixed handling
- Debug goodies (Variable breakpoints)