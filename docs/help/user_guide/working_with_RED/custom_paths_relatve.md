<html>
<head>
<link href="PLUGINS_ROOT/org.robotframework.ide.eclipse.main.plugin.doc.user/help/style.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<a href="RED/../../../../help/index.html">RED - Robot Editor User Guide</a> &gt; <a href="RED/../../../../help/user_guide/user_guide.html">User guide</a> &gt; <a href="RED/../../../../help/user_guide/working_with_RED.html">Working with RED</a> &gt; 
<h2>Custom python/class paths and path relativeness</h2>
Settings available in <b><code>red.xml -> Libraries</code></b>
<h3>Custom python/class paths</h3>
Certain RED usage scenarios such as library development, requires custom path for python/jython interpreter to fulfill dependencies. <br/>
Either this can be done on OS level by updating environment variables or can be included in red.xml.<br/>
Path entries can be absolute or relative, they will be taken into use anytime when interpreter executes (testcase and debug run or libdoc generation). <br/>
<br/><img src="images/custom_path.png"/>
<h3>Path relativeness</h3>
By default, relative paths are resolved around Project scope although in some scenarios like storing Robot testsuites in one project and libraries in other, there is a need to change relativeness regards to Workspace.<br/>
This can be changed in <b><code>red.xml -> Libraries -> Paths</code></b> part.
<br/><b>Note that relativeness setting is used together with class/python path and libraries only.</b>
</body>
</html>