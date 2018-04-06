## Launching preferences

### General launching preferences

General preferences used when launching Robot tests can be found at [
Window->Preferences->Robot Framework->Launching
](javascript:executeCommand\('org.eclipse.ui.window.preferences\(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.launch\)'\))
preference page. Following settings can be changed:

  * **Pass Robot arguments using arguments file** \- when enabled RED will put all the Robot arguments inside arguments file (temporary file) and call Robot using `--argumentfile` argument; when disabled all the Robot arguments are passed directly in command line call. 
  * **Run single suite using suite path** \- when enabled RED will use path to suite during single suite test execution (Robot `__init__` files will not be used then); when disabled path to project is used as data source, even for single suite run. 
  * **Use single argument to pass robot execution command line** \- when tests are [launched using script](local_launch_scripting.md) the actual command line call is either passed as a single argument (when preference is enabled) or simply as a sequence of arguments (when disabled). 
  * **Limit Message Log output** \- when enabled RED will apply provided characters limit in Message Log view. 

### Default launch configurations

All arguments which are set at [ Window->Preferences->Robot
Framework->Launching->Default Launch Configurations
](javascript:executeCommand\('org.eclipse.ui.window.preferences\(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.launch.default\)'\))
preference page will be used by RED when new launch configuration is
automatically created. This may happen for example when launching suites from
**Project Explorer** and there is no launch configuration matching selected
suites (read [launching](ui_elements.html#launching) paragraph to see when RED
automatically creates launch configurations).

Note

    Additional arguments and executable file path preferences accept Eclipse [string variables](string_substitution.md).

### Default behavior of Run/Debug actions

Run and Debug actions, depending on preferences, can use previously used or
created launch configuration or create a new one. Standard behavior can be
changed at
[Window->Preferences->Run/Debug>Launching](javascript:executeCommand\("org.eclipse.ui.window.preferences\(preferencePageId=org.eclipse.debug.ui.LaunchingPreferencePage\)"\))
under Launch Operation.

[Return to Help index](http://nokia.github.io/RED/help/)
