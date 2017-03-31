## Debugging Robot &amp; Python with RED &amp; PyDev

From RED 0.7.6 it is possible to debug Robot test cases and Python libraries
using RED together with PyDev in one RED/Eclipse instance.

### Prerequisites:

  * RED 0.7.6 and newer 
  * PyDev installed to RED or Eclipse with RED and PyDev 
  * Download `runPyDevDebug.py` script from GitHub: <https://github.com/nokia/RED/tree/master/src/RobotUserScripts>

### Environment Setup:

  * install PyDev to RED or Eclipse instance where RED is installed if not already installed. 
  * set Python nature to Robot project to be debugged - right click on Project,from PyDev menu select set Python nature 
  * set port for remote PyDev debugger: _Preferences -> PyDev -> Debug -> Port for remote debugger_
  * for convenience Remote debugger server activation can be changed from _Manual_ to _Keep always on_: _Preferences -> PyDev -> Debug -> Remote debugger server activation_. 
  * to use `runPyDevDebug.py` script logic, RED needs to pass parameters separated by space to external script, check [Launching preferences](launch_prefs.html)

### Script runPyDevDebug.py update:

  * **PyDev debugger**, `pydevd.py`file path needs to be specified, this can be done in two ways. You can install **pydevd** by **pip** ( `pip install pydevd `) therefore script will use it straight away, alternatively update **_pathToPyDevD_** variable, **pydevd** is usually located inside PyDev RED/Eclipse under `eclipse/plugins/org.python.pydev/pysrc/pydevd.py`
  * if **PyDev port for remote debugger** was changed, variable **_pydevdPort_** needs to be updated 

### Debug configuration setup:

  * open [Debug Configuration](javascript:executeCommand\('org.eclipse.debug.ui.commands.OpenDebugConfigurations'\)),create new Robot Debug configuration
  * in Robot tab, populate Project name,test suites to be debugged with all other relevant information
  * in Executor tab, in Executable file provide path to Python interpreter ( for instance `c:\Python27\python.exe` on Windows or `/usr/bin/python` on Linux)
  * in Additional executable file arguments field provide path to `runPyDevDebug.py`

Note

    Default values for both executable and arguments fields can be defined in [preferences](launch_prefs.html), so every time when RED is creating new launch configuration it will use those values. It may be useful if you want to always use some script without manually changing launch configurations before launching. 

### Debug:

  * remember to place at least one breakpoint on executable line either in Robot test or Python library 
  * before running custom Debug Configuration, PyDev Debug server needs to be started if the startup was not changed to Automatic in Environment Setup part. Switch to Debug perspective, from PyDev menu,select Start Debug Server 
  * run created Debug Configuration, RED should start the `runPyDevDebug.py` script which will take command line from RED and start Robot Framework debug run simultaneously being connected with Pydev Debugger. 
  * if breakpoint is reached, appropriated Debugger is used depending on the source from where the breakpoint was reached (either Robot testcase or Python library) 

