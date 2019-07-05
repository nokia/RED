<html>
<head>
<link href="PLUGINS_ROOT/org.robotframework.ide.eclipse.main.plugin.doc.user/help/style.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<a href="../../../help/index.html">RED - Robot Editor User Guide</a> &gt; <a href="../../../help/user_guide/user_guide.html">User guide</a> &gt; 
<h2>Validation</h2>
<h3>General information</h3>
<p>Validation is a mechanism to read and parse Robot test cases in order to provide feedback about Robot defined
spell check. Validation mechanism walk down the project tree and read all test files format supported by RED 
(.robot, .txt and .tsv), mark all unknown/undefined keywords, variable misuse, missing resources etc. <br/>
</p>
<h3>Validation execution</h3>
<p>Validation mechanism is executed any time when edited file is changed with slight delay while <b>Build Automatic </b>
option is selected from Project. Whole project validation can be manually started using option <b><code>Project -> Clean</code></b>.<br>
Resources can be also validated by selecting <b><code>Robot Framework -> Revalidate</code></b> option in Project explorer.<br/>
</br></p>
<p>The file which currently edited within Suite Editor is constantly validated in background giving quick feedback
about potential problems.
</p>
<dl class="note">
<dt>Note</dt>
<dd>Although not recommended, validation can be turned off in Preferences (<code><a class="command" href="javascript:executeCommand('org.eclipse.ui.window.preferences(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.validation)')">
   Window -> Preferences -> Robot Framework -> Errors/Warnings</a></code>).
   </dd>
</dl>
<h3>Validation preferences</h3>
<p>Validation problems severity level settings are covered under topic <a href="validation/validation_preferences.html">Configuring problems severity</a></p>
<p>Validation scope is covered under topic <a href="validation/scope.html">Limiting validation scope</a></p>
<p>Validation as command line tool (without GUI, headless) is covered under topic <a href="validation/headless.html">Running validation in command line</a></p>
<h3>Contents</h3>
<ul>
<li><a href="../../../help/user_guide/validation/scope.html">Limiting validation scope</a>
</li>
<li><a href="../../../help/user_guide/validation/validation_preferences.html">Configuring problems severity</a>
</li>
<li><a href="../../../help/user_guide/validation/headless.html">Running validation in command line</a>
</li>
<li><a href="../../../help/user_guide/validation/tasks.html">Tasks/TODO</a>
</li>
</ul>
</body>
</html>