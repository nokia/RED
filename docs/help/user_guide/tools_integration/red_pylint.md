<html>
<head>
<link href="PLUGINS_ROOT/org.robotframework.ide.eclipse.main.plugin.doc.user/help/style.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<a href="RED/../../../../help/index.html">RED - Robot Editor User Guide</a> &gt; <a href="RED/../../../../help/user_guide/user_guide.html">User guide</a> &gt; <a href="RED/../../../../help/user_guide/tools_integration.html">Integration with other tools</a> &gt; 
	<h2>PyDev and PyLint</h2>
<p>
<a class="external" href="https://marketplace.eclipse.org/content/pydev-python-ide-eclipse" target="_blank">PyDev</a> and 
		<a class="external" href="https://www.pylint.org/" target="_blank">PyLint</a> can be used together with RED. Those
		components needs to be installed and properly configured first.<br/>
		Remember that PyDev has its own perspective. To open python files in
		RED with PyDev editor, use right-click menu <b>Open With Python Editor</b>.
	</p>
<h3>PyDev installation</h3>
<p>We recommend to install PyDev from Eclipse Marketplace:</p>
<ul>
<li>Open Eclipse Marketplace: <b><code>Help -> Eclipse
				Marketplace</code></b>,</li>
<li>in Find field enter PyDev and proceed with installation
			instructions,</li>
<li>when prompted, restart Eclipse/RED Product,</li>
<li>check PyDev preferences for python interpreters under <b><code>Windows
				-> Preferences -> PyDev</code></b>.</li>
</ul>
<h3>Debugging tests with RED &amp; PyDev</h3>
<p>It is possible to debug the tests together with python code using RED and PyDev. For detailed
	description please visit <a href="../launching/debug/robot_python_debug.html">Robot &amp; Python debugging</a> topic.
	</p>
<h3>PyLint installation</h3>
<p>
		We recommend to install PyLint using <code>pip</code>:
	</p>
<ul>
<li>Install PyLint with <code>pip</code> from
			console/command line,
			<div class="code">
<i>pip install pylint</i>
</div>
</li>
<li>restart Eclipse/RED Product,</li>
<li>enable PyLint,</li>
<li>enable redirecting PyLint output to console,</li>
<li>
			provide proper PyLint executable paths in PyDev preferences: <b><code>Window -> Preferences -> PyDev -> PyLint</code></b>,
		</li>
<li>sometimes it is necessary to set folder with sources for
			PyDev in Project properties: right click on <b><code>Project -> PyDev -> Set as Source Folder (add to PYTHONPATH)</code></b>,
		</li>
<li>restart Eclipse/RED Product.</li>
</ul>
</body>
</html>