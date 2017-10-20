## PyDev and PyLint

[PyDev](https://marketplace.eclipse.org/content/pydev-python-ide-eclipse) and
[PyLint](https://www.pylint.org/) can be used together with RED. Those
components needs to be installed and properly configured first.  
Remember that PyDev has its own perspective. To open python files in RED with
PyDev editor, use right-click menu **Open With Python Editor**.

### PyDev installation

We recommend to install PyDev from Eclipse Marketplace:

  * Open Eclipse Marketplace: **Help - > Eclipse Marketplace**,
  * in Find field enter PyDev and proceed with installation instructions,
  * when prompted, restart Eclipse/RED Product,
  * check PyDev preferences for python interpreters under **Windows - > Preferences -> PyDev**.

### Debugging tests with RED & PyDev

It is possible to debug the tests together with python code using RED and
PyDev. For detailed description please visit [Robot & Python
debugging](../launching/debug/robot_python_debug.md) topic.

### PyLint installation

We recommend to install PyLint using `pip`:

  * Install PyLint with `pip` from console/command line, 

_pip install pylint_

  * restart Eclipse/RED Product,
  * enable PyLint,
  * enable redirecting PyLint output to console,
  * provide proper PyLint executable paths in PyDev preferences: **Windows - > Preferences -> PyDev -> PyLint**, 
  * sometimes it is necessary to set folder with sources for PyDev in Project properties: right click on **Project -> PyDev -> Set as Source Folder (add to PYTHONPATH)** , 
  * restart Eclipse/RED Product.

[Return to Help index](http://nokia.github.io/RED/help/)
