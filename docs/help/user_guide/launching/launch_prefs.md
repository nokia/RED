## Launching preferences

### General launching preferences

General preferences used when launching Robot tests can be found at [
Window->Preferences->Robot Framework->Launching
](javascript:executeCommand\('org.eclipse.ui.window.preferences\(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.launch\)'\))
preference page. Following settings can be changed:

  * **Pass Robot arguments using arguments file** \- when enabled RED will put all the Robot arguments inside arguments file (temporary file) and call Robot using `--argumentfile` argument; when disabled all the Robot arguments are passed directly in command line call, 
  * **Use single argument to pass robot execution command line** \- when tests are [launched using script](local_launch_scripting.md) the actual command line call is either passed as a single argument (when preference is enabled) or simply as a sequence of arguments (when disabled). 

### Default launch configurations

All arguments which are set at [ Window->Preferences->Robot
Framework->Launching->Default Launch Configurations
](javascript:executeCommand\('org.eclipse.ui.window.preferences\(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.launch\)'\))
preference page will be used by RED when new launch configuration is
automatically created. This may happen for example when launching suites from
**Project Explorer** and there is no launch configuration matching selected
suites (read [launching](ui_elements.html#launching) paragraph to see when RED
is automatically creating launch configurations).

### Default behavior of Run/Debug actions

Run and Debug actions, depending on preferences, can use previously used or
created launch configuration or create a new one. Standard behaviour can be
change in
[Window->Preferences->Run/Debug>Launching](javascript:executeCommand\("org.eclipse.ui.window.preferences\(preferencePageId=org.eclipse.debug.ui.LaunchingPreferencePage\)"\))
under Launch Operation.

[Return to Help index](http://nokia.github.io/RED/help/)
