## PyDev and PyLint

[PyDev](https://marketplace.eclipse.org/content/pydev-python-ide-eclipse) and
[PyLint](https://www.pylint.org/) can be used together with RED. Those
components needs to be installed and properly configured first.  
Remember that PyDev has its own perspective. To open python files in RED with
PyDev editor, use right-click menu **Open With Python Editor**.

### PyDev installation

We recommend to install PyDev from Eclipse Marketplace:

  * Open Eclipse Marketplace: **Help -&gt; Eclipse Marketplace**,
  * in Find field enter PyDev and proceed with installation instructions,
  * when prompted, restart Eclipse/RED Product,
  * check PyDev preferences for python interpreters under **Windows -&gt; Preferences -&gt; PyDev**.

### Debugging tests with RED &amp; PyDev

It is possible to debug the tests together with python code using RED and
PyDev. For detailed description please visit [Robot &amp; Python
debugging](../launching/robot_python_debug.html) topic.

### PyLint installation

We recommend to install PyLint using `pip`:

  * Install PyLint with `pip` from console/command line, 

_pip install pylint_

  * restart Eclipse/RED Product,
  * enable PyLint,
  * enable redirecting PyLint output to console,
  * provide proper PyLint executable paths in PyDev preferences: **Windows -&gt; Preferences -&gt; PyDev -&gt; PyLint**, 
  * sometimes it is necessary to set folder with sources for PyDev in Project properties: right click on **Project -&gt; Properties -&gt; PyDev - PYTHONPATH -&gt; Add source folder**, 
  * restart Eclipse/RED Product.

