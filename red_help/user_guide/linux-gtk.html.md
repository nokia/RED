## GTK3 issues under Linux

It was observed that using GTK3 library with Eclipse causes unexpected
graphical glitches.  
Those glitches are system independent although occurs more often under Debian
and Ubuntu than RedHat  
Most annoying one is screen flickering when editing large file with multiple
folding sections in source editor.  
Also some of the right click actions in red.xml are not activated.  
  
It is recommended to switch to GTK2 when working under Linux.  
  
RED Product for Linux has configuration to use GTK2,Eclipse users should check
and change,if necessary, to GTK2.  

### Verifying current GTK version used

GTK version can be checked in Eclipse under _Help-&amp;gtInstallation;
Details-&amp;gtConfiguration;_,search for
_org.eclipse.swt.internal.gtk.version_ entry.

### Setting GTK2 system wide

In console execute following:  
_export SWT_GTK3=0_  
than start eclipse:  
_./eclipse_  

### Setting GTK2 in eclipse.ini

GTK2 can be forced for eclipse instance,edit eclipse.ini to include following
2 lines above -vmargs section:  
_\--launcher.GTK_version 2_

