<html>
<head>
<link href="PLUGINS_ROOT/org.robotframework.ide.eclipse.main.plugin.doc.user/help/style.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<a href="RED/../../../../help/index.html">RED - Robot Editor User Guide</a> &gt; <a href="RED/../../../../help/user_guide/user_guide.html">User guide</a> &gt; <a href="RED/../../../../help/user_guide/project_config.html">Project configuration</a> &gt; 

<h2>Custom PYTHONPATH/CLASSPATH and path relativeness</h2>
<p>Settings available in <code>red.xml -> Libraries</code>.

<h3>Custom python/class paths</h3>
<p>Certain RED usage scenarios such as library development requires custom path for python/jython interpreter to 
fulfill dependencies. Either this can be done on OS level by updating environment variables or can be included in 
red.xml. Path entries can be absolute or relative - they will be taken into use anytime when interpreter executes 
(test case and debug run or libdoc generation).
</p>
<img src="images/custom_path.png"/>
<h3>Path relativeness</h3>
<p>By default, relative paths are resolved around Project scope although in some scenarios like storing Robot 
test suites in one project and libraries in other, there is a need to change relativeness regards to Workspace.
This can be changed in <code>red.xml -> Libraries -> Paths</code> part. <b>Note that relativeness setting is 
used together with class/python path and libraries only.</b>
</p>
</p></body>
</html>