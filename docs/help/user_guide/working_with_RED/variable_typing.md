## Variable typing in editors

RED supports several variable typing enhancements. When this feature is
enabled, variable brackets {} are automatically inserted after typing one of
Robot variable identifiers ($, @, &, %).  
Corresponding bracket is automatically deleted when brackets are empty and
first bracket is deleted by typing Delete or Backspace. When selected text
matches given pattern and one of variable identifiers is typed, selection is
wrapped with variable brackets.  
  
![](variable_typing/variable_typing.gif)  
  

### Variable typing preferences

All variable typing related preferences (bracket insertion, text wrapping,
text wrapping pattern) can be configured at `[ Window -> Preferences -> Robot
Framework ->
Editor](javascript:executeCommand\('org.eclipse.ui.window.preferences\(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.editor\)'\))`
in **Variables** section.  
  
![](variable_typing/variable_preferneces.png)  
  

[Return to Help index](http://nokia.github.io/RED/help/)
