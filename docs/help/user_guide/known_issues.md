## Known issues and problems

For all currently opened issues please visit our repository at
[GitHub](https://github.com/nokia/RED/)

  * ### Memory consumption

Memory allocation is controlled in eclipse.ini/RED.ini file in eclipse/RED
folder with _Java Virtual Machine_ `Xms` and `Xmx` parameters. By default
Eclipse is configured to run on system with 2GB of memory, in case of stronger
systems it is advised to increase those parameters to respectful levels.
Change or add those parameters in _.ini_ file after `-vmargs` parameter:

-vmargs  
-Xms2048m  
-Xmx4096m  
...

  * ### Unicode characters in test material

Eclipse by default is not configured to interpret and display language
specific characters in text editors. This leads to displaying such characters
as non readable, also markers for errors and warnings will be shifted thus an
offset of error and underline will be observed.

![](images/unicode.png)

This can be fixed by selecting proper file encoding in Preferences (at
_[Window->Preferences->General->Workspace](javascript:executeCommand\('org.eclipse.ui.window.preferences\(preferencePageId=org.eclipse.ui.preferencePages.Workspace\)'\))_
change **Text file encoding** to UTF-8).

  * ### GTK3 issues under Linux

It was observed that using GTK3 library with Eclipse causes unexpected
graphical glitches. Those glitches are system independent although occurs more
often under Debian and Ubuntu than RedHat Most annoying one is screen
flickering when editing large file with multiple folding sections in source
editor. Also some of the right click actions in red.xml are not activated.

It is recommended to switch to GTK2 when working under Linux.

RED Product for Linux has configuration to use GTK2, Eclipse users should
check and change - if necessary - to GTK2.

#### Verifying current GTK version used

GTK version can be checked in Eclipse under **Help- >Installation
Details->Configuration**, search for `org.eclipse.swt.internal.gtk.version`
entry.

#### Setting GTK2 system wide

In order to run eclipse/RED with GTK2 execute following:

export SWT_GTK3=0

then start eclipse:

./eclipse

or RED:

./RED

#### Setting GTK2 in eclipse.ini/RED.ini

GTK2 usage can be forced for eclipse/RED instance. In order to do it simply
add following 2 lines to **eclipse.ini** / **RED.ini** file before `-vmargs`
section:

\--launcher.GTK_version 2  
...  
-vmargs  

[Return to Help index](http://nokia.github.io/RED/help/)
