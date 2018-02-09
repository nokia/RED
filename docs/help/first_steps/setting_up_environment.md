## Establishing environment

As mentioned [earlier](download_install.md) RED requires Python interpreter
with Robot Framework installed in your system. Without it numerous RED
features will not work: you will not be able to run and debug tests, the
project will not be validated or content assistant will not give any proposals
for keywords coming from libraries.

If there is no environment established RED will signal this: error will be
reported by workspace validation for each red.xml file in your projects or the
dialog will be presented when opening editor:

![](images/error_missing_env.png)

![](images/error_missing_env2.png)

There are two ways in which Python & RF environment can be established:
automatically or manually via Preferences

### Automatic environment detection

In most cases there is actually nothing required from user and RED will
automatically detect Python installation. This only require that the path to
one of those executables (Unix-like / Windows):

  * python / python.exe
  * jython / jython.exe
  * ipy / ipy.exe
  * ipy64 / ipy64.exe
  * pypy / pypy.exe

is appended to [ PATH](https://en.wikipedia.org/wiki/PATH_\(variable\))
environment variable in your system.

Warning

    On Unix-like systems (Linux / macOS) executables are often named python2 or python3 for respective versions of python. As of now RED does not recognize such executables even if they are appended to PATH variable. Probably the best solution for this problem is to create a [symlink](https://en.wikipedia.org/wiki/Symbolic_link) named python targeted to either python2 or python3. 

### Manual environment configuration

It is possible to add environments manually in _[Window->Preferences->Robot
Framework->Installed
frameworks](javascript:executeCommand\('org.eclipse.ui.window.preferences\(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.installed\)'\))_.
Simply use  Add... button to open dialog in which the directory can be chosen.
It should be the directory which contains one of the executable files listed
above.

If proper directory is chosen you will see it listed in the table together
with the information about version of Robot Framework installed in it.
Otherwise the added entry will be marked with orange/red color and tooltip
would show information about the problem (either it was not recognized as
Python directory or there is no RF installed).

Multiple environments can be defined here, but only one should be selected and
it will be used by all project unless the project overrides this setting
locally in its red.xml configuration file.

Additionally the automatic recognition process can be started from preference
page through Discover button. It will look through directories in your PATH
variable to see if any of listed executables are there.

  

[Return to Help index](http://nokia.github.io/RED/help/)
