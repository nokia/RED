## Custom python/class paths and path relativeness

Settings available in **red.xml -> Referenced Libraries**

### Custom python/class paths

Certain RED usage scenarios such as library development, requires custom path
for python/jython interpreter to fulfill dependencies.  
Either this can be done on OS level by updating environment variables or can
be included in red.xml.  
Path entries can be absolute or relative, they will be taken into use anytime
when interpreter executes (testcase and debug run or libdoc generation).  
  
  
![](custom_paths_relatve/custom_path.png)  
  

### Path relativeness

By default, relative paths are resolved around Project scope although in some
scenarios like storing Robot testsuites in one project and libraries in other,
there is a need to change relativeness regards to Workspace.  
This can be changed in red.xml under Referenced Libraries -> Path part.  
**Note that relativeness setting is used together with class/python path and
libraries only.**

[Return to Help index](http://nokia.github.io/RED/help/)
