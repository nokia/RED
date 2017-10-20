## RED Agent

RED Tests Runner Agent is something of which you should be aware if you're
planning to [launch tests using remote configuration](remote_launch.md) or
if you're [writing own script](local_launch_scripting.md) which will run the
tests in local configuration. The agent is a python script which should be
attached to test execution as a listener (see [ Listener interface
](http://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html
#listener-interface) topic in RF User Guide).

Agent is responsible for listening to execution events happening in running
tests, so without agent both **Message Log** and **Execution** views will not
work. The script is also responsible for stopping/resuming tests on
breakpoints, so RED debugging capabilities also only work when agent was
injected into tests execution.

### Obtaining agent script

You can always obtain agent script from RED within Preferences dialog (at
_[Window->Preferences->Robot Framework->Default Launch Configurations
](javascript:executeCommand\('org.eclipse.ui.window.preferences\(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.launch.default\)'\))_
, use **Export Client Script** button). Additionally the file may be saved
straight from launch configuration dialog.

Warning

    Agent script is a subject of changes, so it may happen that script exported from RED version `x` is not able to work properly with RED version `y` and vice versa. From version `0.7.6` onwards both script and RED are checking and handling this when establishing connection, so you will be notified. **Important** : script taken from RED older than `0.7.6` will not work with newer RED and vice-versa without notifying you (possibly even hanging infinitely). 

### Taking agent into Robot Tests execution

Agent script have to be injected into Robot tests using [ listeners
mechanism](http://robotframework.org/robotframework/latest/RobotFrameworkUserGuide.html
#listener-interface). When starting Robot execution pass the listener as
follows:

` _robot_ --listener /path/to/script/TestRunnerAgent.py:ARG1:ARG2:ARG3
_other_arguments_ `

Agent script can take up to 3 arguments which are added to script path after
colon ( **:** ) separator, the arguments themselves are also separated with a
colon ( **:** ). The arguments are:

  * **HOST** \- IP number of a server to which agent should connect and send execution data, 
  * **PORT** \- port number of a server, 
  * **TIMEOUT** \- when connecting to server agent will try to connect as long as the timeout is reached (in seconds) 

  * When script is used with only one parameter it has to be the **PORT** number: 

` _robot_ --listener /path/to/script/TestRunnerAgent.py:12345
_other_arguments_ `

in this case `HOST=localhost`, `PORT=12345` and `TIMEOUT=30`

  * When script is used with two parameters it has to be the **HOST** and **PORT** number (in this order): 

` _robot_ --listener /path/to/script/TestRunnerAgent.py:192.168.0.5:54321
_other_arguments_ `

in this case `HOST=192.168.0.5`, `PORT=54321` and `TIMEOUT=30`

  * When script is used with three parameters it all of them has to be defined (in **HOST** , **PORT** , **TIMEOUT** order): 

` _robot_ --listener /path/to/script/TestRunnerAgent.py:192.168.0.7:12321:60
_other_arguments_ `

in this case `HOST=192.168.0.7`, `PORT=12321` and `TIMEOUT=60`

  
  

[Return to Help index](http://nokia.github.io/RED/help/)
