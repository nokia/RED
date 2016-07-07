## Headless - running validation in command line

RED can be run in headless mode to provide validation info of included
projects.  
During validation, settings are read from red.xml of each of the projects thus
preserving links to referenced libraries, variable mappings or include/exclude
folders. Only opened projects are validated  
Output of headless run is report of validated projects and xml report in
checkstyle format.  
  
Syntax:  
_eclipse -application org.robotframework.ide.eclipse.main.plugin.validation
-data $WORKSPACE_PATH -nosplash -consoleLog --launcher.suppressErrors -import
$PROJECT_PATHS -projects $PROJECT_NAMES -report $RAPORT_FILE _  
  
$PROJECT_PATHS – list of paths to projects to be imported into workspace,
separated by space  
$PROJECT_NAMES – list of projects to be validated from workspace, separated by
space  

