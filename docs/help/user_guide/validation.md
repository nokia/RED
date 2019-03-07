[RED - Robot Editor User Guide](http://nokia.github.io/RED/help/index.md) >
[User guide](http://nokia.github.io/RED/help/user_guide/user_guide.md) >

## Validation

### General information

Validation is a mechanism to read and parse Robot test cases in order to
provide feedback about Robot defined spell check. Validation mechanism walk
down the project tree and read all test files format supported by RED (.robot,
.txt and .tsv), mark all unknown/undefined keywords, variable misuse, missing
resources etc.  

### Validation execution

Validation mechanism is executed any time when edited file is changed with
slight delay while **Build Automatic** option is selected from Project. Whole
project validation can be manually started using option **`Project ->
Clean`**.  
Resources can be also validated by selecting **`Robot Framework ->
Revalidate`** option in Project explorer.  

The file which currently edited within Suite Editor is constantly validated in
background giving quick feedback about potential problems.

Note

    Although not recommended, validation can be turned off in Preferences (`[ Window -> Preferences -> Robot Framework -> Errors/Warnings](javascript:executeCommand\('org.eclipse.ui.window.preferences\(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.validation\)'\))`). 

### Validation preferences

Validation problems severity level settings are covered under topic
[Configuring problems severity](validation/validation_preferences.md)

Validation scope is covered under topic [Limiting validation
scope](validation/scope.md)

Validation as command line tool (without GUI, headless) is covered under topic
[Running validation in command line](validation/headless.md)

### Contents

  * [Limiting validation scope](http://nokia.github.io/RED/help/user_guide/validation/scope.md)
  * [Configuring problems severity](http://nokia.github.io/RED/help/user_guide/validation/validation_preferences.md)
  * [Running validation in command line](http://nokia.github.io/RED/help/user_guide/validation/headless.md)
  * [Tasks/TODO](http://nokia.github.io/RED/help/user_guide/validation/tasks.md)

