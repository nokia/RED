## PyDev and PyLint with RED - Robot Editor

PyDev and PyLint can be used together with RED - Robot Editor. Those
components needs to be installed and properly configured first.  
Remember that PyDev has its own perspective. To open python files in RED with
PyDev editor, use right-click menu _Open With Python Editor._

### PyDev installation

We recommend to install PyDev from Eclipse Marketplace:  

  * Open Eclipse Marketplace: _Help -&gt; Eclipse Marketplace _
  * In Find field enter PyDev and proceed with installation instructions
  * When prompted, restart Eclipse/RED Product
  * Check PyDev preferences for python interpreters under _Windows -&gt; Preferences -&gt; PyDev_

### PyLint installation

We recommend to install PyLint with pip:  

  * Install PyLint with pip from console/command line: _pip install pylint_
  * Restart Eclipse/RED Product
  * Enable PyLint, enable redirect PyLint output to console, provide proper PyLint executable paths in PyDev preferences: _Windows -&gt; Preferences -&gt; PyDev -&gt; PyLint_
  * Sometimes it is necessary to set folder with sources for PyDev in Project properties: right click on _Project -&gt; Properites -&gt; PyDev - PYTHONPATH -&gt; Add source folder_
  * Restart Eclipse/RED Product

