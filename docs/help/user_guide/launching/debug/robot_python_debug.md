## Debugging Robot & Python with RED & PyDev

From RED 0.7.6 it is possible to debug Robot test cases and Python libraries
using RED together with PyDev in one RED/Eclipse instance.

### Prerequisites

  * RED 0.7.6 and newer 
  * PyDev installed to RED or Eclipse with RED and PyDev 
  * Download `runPyDevDebug.py` script from GitHub: [ https://github.com/nokia/RED/tree/master/src/RobotUserScripts](https://github.com/nokia/RED/tree/master/src/RobotUserScripts)

### Environment Setup

  * Install PyDev to RED or Eclipse with RED instance (if not already installed), 
  * set Python nature to Robot project to be debugged - right click on Project, from PyDev menu select `Set as PyDev project`, 
  * set port for remote PyDev debugger: `Preferences -> PyDev -> Debug -> Port for remote debugger`, 
  * for convenience Remote debugger server activation can be changed from `Manual` to `Keep always on` (from `Preferences -> PyDev -> Debug -> Remote debugger server activation`), 
  * to use `runPyDevDebug.py` script logic RED needs to pass parameters separated by space to external script, check [Launching preferences](../launch_prefs.md). 

### Script runPyDevDebug.py update

  * **PyDev debugger** (`pydevd.py` file path) needs to be specified and it can be done in two ways. You can install **pydevd** by **pip** (`pip install pydevd`) therefore script will use it straight away. Alternatively update `pathToPyDevD` variable, **pydevd** is usually located inside PyDev RED/Eclipse under `eclipse/plugins/org.python.pydev/pysrc/pydevd.py`
  * if **PyDev port for remote debugger** was changed, variable `pydevdPort`code> needs to be updated 

### Debug configuration setup

  * Open [ Debug Configuration](javascript:executeCommand\('org.eclipse.debug.ui.commands.OpenDebugConfigurations'\)), 
  * create new Robot Debug configuration, 
  * in Robot tab populate **Project name** and **test suites** to be debugged with all other relevant information, 
  * in Executor tab in **Executable file** provide path to Python interpreter (for instance `c:\Python27\python.exe` on Windows or `/usr/bin/python` on Linux), 
  * in **Additional executable file arguments** field provide path to `runPyDevDebug.py`

Note

    Default values for both executable and arguments fields can be defined in [preferences](../launch_prefs.md), so every time when RED is creating new launch configuration it will use those values. It may be useful if you want to always use some script without manually changing launch configurations before launching. 

### Debug

  * Remember to place at least one breakpoint on executable line either in Robot test or Python library. 
  * Before running custom Debug Configuration, PyDev Debug server needs to be started if the startup was not changed to Automatic in Environment Setup part. Switch to Debug perspective and from PyDev menu select `Start Debug Server`. 
  * Run created Debug Configuration, RED should start the `runPyDevDebug.py` script which will take command line from RED and start Robot Framework debug run simultaneously being connected with Pydev Debugger. 
  * If breakpoint is reached, appropriated Debugger is used depending on the source from where the breakpoint was reached (either Robot testcase or Python library). 

[Return to Help index](http://nokia.github.io/RED/help/)
