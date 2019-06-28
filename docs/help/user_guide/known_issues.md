<html>
<head>
<link href="PLUGINS_ROOT/org.robotframework.ide.eclipse.main.plugin.doc.user/help/style.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<a href="../../../help/index.html">RED - Robot Editor User Guide</a> &gt; <a href="../../../help/user_guide/user_guide.html">User guide</a> &gt; 
<h2>Known issues and problems</h2>
<p>For all currently opened issues please visit our repository at 
<a class="external" href="https://github.com/nokia/RED/" target="_blank">GitHub</a>
</p>
<ul>
<li>
<h3>Memory consumption</h3>
<p>Memory allocation is controlled in eclipse.ini/RED.ini file in eclipse/RED folder 
		with <i>Java Virtual Machine</i> <code>Xms</code> and 
		<code>Xmx</code> parameters. By default Eclipse is configured to run on 
		system with 2GB of memory, in case of stronger systems it is advised to increase those
		parameters to respectful levels. Change or add those parameters in <i>.ini</i> file after
		<code>-vmargs</code> parameter:
		</p>
<div class="code">
		-vmargs<br/>
		-Xms2048m<br/>
		-Xmx4096m<br/>
		...
		</div>
</li>
<li>
<h3>Unicode characters in test material</h3>
<p>Eclipse by default is not configured to interpret and display language specific
		characters in text editors. This leads to displaying such characters as non readable, 
		also markers for errors and warnings will be shifted thus an offset of error and underline
		will be observed.</p>
<p><img src="images/unicode.png"/></p>
<p>This can be fixed by selecting proper file encoding in 
		Preferences (at <code><a class="command" href="javascript:executeCommand('org.eclipse.ui.window.preferences(preferencePageId=org.eclipse.ui.preferencePages.Workspace)')">
		Window -&gt; Preferences -&gt; General -&gt; Workspace</a></code> change <b>Text file encoding</b> to UTF-8).</p>
</li>
<li>
<h3>Unicode characters in paths with Python 2 on Windows platform</h3>
<p>Python 2 does not always support unicode characters in paths on Windows. 
		Using such characters may cause problems with starting RED XML-RPC server or running Robot tests. 
		When unicode characters are used in paths, Python 3 is recommended.
		</p>
<p>Workaround for XML-RPC server starting problems is changing TEMP folder path to path without unicode characters.</p>
</li>
<li>
<h3>GTK3 issues under Linux</h3>
<p>It was observed that using GTK3 library with Eclipse (older then version 2019-03 - in this version GTK3 is required) causes unexpected graphical glitches.
		Those glitches are system independent although occurs more often under Debian and Ubuntu than RedHat.
		Most annoying one is screen flickering when editing large file with multiple folding sections in source editor.
		Also some of the right click actions in red.xml are not activated.
		</p>
<p>It is recommended to switch to GTK2 when working under Linux for Eclipse older then 2019-03 and RED version 0.8.13.</p>
<p>RED Product for Linux has configuration to use GTK2, Eclipse users should check and change - if necessary - to GTK2.
		</p>
<h4>Verifying current GTK version used</h4>
<p>GTK version can be checked in Eclipse under <b><code>Help -&gt; Installation Details -&gt; Configuration</code></b>, 
		search for <code>org.eclipse.swt.internal.gtk.version</code> entry.
		</p>
<h4>Setting GTK2 system wide</h4>
<p>In order to run eclipse/RED with GTK2 execute following:</p>
<div class="code">
		export SWT_GTK3=0
		</div>
<p>then start eclipse:</p>
<div class="code">
		./eclipse
		</div>
<p>or RED:</p>
<div class="code">
		./RED
		</div>
<h4>Setting GTK2 in eclipse.ini/RED.ini</h4>
<p>GTK2 usage can be forced for eclipse/RED instance. In order to do it simply add following 2 lines to 
		<b>eclipse.ini</b>/<b>RED.ini</b> file before <code>-vmargs</code> section:
		</p>
<div class="code">
		--launcher.GTK_version 2<br/>
		...<br/>
		-vmargs<br/>
</div>
</li>
</ul>
</body>
</html>