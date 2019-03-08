[RED - Robot Editor User Guide](http://nokia.github.io/RED/help/index.md) >
[User guide](http://nokia.github.io/RED/help/user_guide/user_guide.md) >

## Launching Tests

With RED it is possible to launch Robot Framework tests in 2 modes ( **run**
or **debug** ) in conjunction with 2 types of configurations: ( **local** or
**remote** ). Furthermore local configuration can be launched directly using
python interpreter or with script (e.g batch or bash) provided by user.

The debug mode is different from run mode in only aspect: it allows to stop
test execution on defined breakpoints and perform step-by-step execution.

[Remote configuration](launching/remote_launch.md) is rather simple: it
starts a server in RED which is listening for a connection from running tests
and outputs messages coming to server into **Log Message** view as well as
updates tests suites tree inside **Execution** view. When launched in debug
the debugging capabilities are enabled. In **Console** view there are only
server status messages printed, as output of remotely running tests are not
send to RED. User has to start the tests manually with [RED
agent](launching/red_agent.md) attached.

[Local configuration](launching/local_launch.md) is a bit more complicated:
apart from starting above-mentioned server it automatically starts Robot test
execution. Moreover it can be either started using some Robot environment
(python interpreter + Robot Framework) or using some script. In latter case it
depends upon script if particular RED feature will work (Console will have
tests output only if script is redirecting it; other features work if script
is executing script with [RED agent](launching/red_agent.md) or not).

Table below summarizes which RED features are working when running different
configuration in different modes

Configuration & mode | Console view | Message log view | Execution view |
Debugging  
---|---|---|---|---  
Local run |  |  |  |  
Local debug |  |  |  |  
Local run through script | Output of script | Only if script run tests with
agent | Only if script run tests with agent |  
Local debug through script | Output of script | Only if script run tests with
agent | Only if script run tests with agent | Only if script run tests with
agent  
Remote run | Only server messages | Only if user run tests with agent | Only
if user run tests with agent |  
Remote debug | Only server messages | Only if user run tests with agent | Only
if user run tests with agent | Only if user run tests with agent  
  
### Contents

  * [User interface](http://nokia.github.io/RED/help/user_guide/launching/ui_elements.md)
  * [Local launches](http://nokia.github.io/RED/help/user_guide/launching/local_launch.md)
  * [Local launches scripting](http://nokia.github.io/RED/help/user_guide/launching/local_launch_scripting.md)
  * [Remote launches](http://nokia.github.io/RED/help/user_guide/launching/remote_launch.md)
  * [Parameterizing launches](http://nokia.github.io/RED/help/user_guide/launching/string_substitution.md)
  * [Controlling execution](http://nokia.github.io/RED/help/user_guide/launching/exec_control.md)
  * [Debugging Robot](http://nokia.github.io/RED/help/user_guide/launching/debug.md)
    * [User interface](http://nokia.github.io/RED/help/user_guide/launching/debug/ui_elements.md)
    * [Breakpoints](http://nokia.github.io/RED/help/user_guide/launching/debug/breakpoints.md)
    * [Hitting a breakpoint](http://nokia.github.io/RED/help/user_guide/launching/debug/hitting_a_breakpoint.md)
    * [Debugger preferences](http://nokia.github.io/RED/help/user_guide/launching/debug/preferences.md)
    * [Debugging Robot & Python with RED & PyDev](http://nokia.github.io/RED/help/user_guide/launching/debug/robot_python_debug.md)
  * [Launching preferences](http://nokia.github.io/RED/help/user_guide/launching/launch_prefs.md)
  * [RED Tests Runner Agent](http://nokia.github.io/RED/help/user_guide/launching/red_agent.md)

