## Content assistance

Content assistance is a functionality provided by Eclipse platform extended to
understand Robot data model.  
By default it is invoked by pressing CTRL+SPACE key short-cut, but it can be
changed in binding keys preferences.  
It can be also invoked by typing one of auto activation characters defined in
preferences.  

### Content assistance mode of working

Assist window has multiple modes which are cycled by CTRL+SPACE. Next mode
type is displayed at the bottom of the window.  
  
![](content_assist/content-assist-modes.gif)  
  
  
All proposal containing given input are displayed. Proposals starting with
given input are displayed first.  
  
![](content_assist/content-assist-search.png)  
  
  
When given input is camel case string (for example: REA, CrBiFi, WaUCr)
keywords proposals that match that string are displayed before other matches.  
  
![](content_assist/content-assist-camel-case.png)  
  
  

### Content assistance preferences

Behavior of content assist can be changed at _[Window->Preferences->Robot
Framework->Editor->Content
Assist](javascript:executeCommand\('org.eclipse.ui.window.preferences\(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.editor.assist\)'\))_.  
  
**Auto activation**  
When auto activation is enabled, triggers and delay can be specified to
automatically show assist window when one of defined characters is typed.  
  
**Keywords**  
It can also be configured if library/resource prefix should be always used
when accepting content proposal.  
Moreover, keyword propositions from libraries available in red.xml, but not
imported in robot file, can be enabled. Library import will be added
automatically when such proposal is accepted.  
Another option is Tab behavior for automatically added arguments in source
view. Cycle between arguments or exit on last argument can be chosen.  
  
![](content_assist/content_pref.png)  
  

[Return to Help index](http://nokia.github.io/RED/help/)
