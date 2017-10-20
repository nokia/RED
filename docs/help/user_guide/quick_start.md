## Quick start with RED - Robot Editor

### Switch to Robot perspective

In order to work with RED, Robot perspective needs to be activated. This can
be done in several ways, the easiest is to use top menu:  
[Window -> Perspective -> Open perspective ->
Other](javascript:executeCommand\('org.eclipse.ui.perspectives.showPerspective\(\)'\))
and select
[Robot](javascript:executeCommand\('org.eclipse.ui.perspectives.showPerspective\(org.eclipse.ui.perspectives.showPerspective.perspectiveId=org.eclipse.ui.perspectives.RobotPerspective\)'\))

### Create Robot Project

All projects with Robot Framework files should be kept in project-type (
_nature_ ) Robot, as this allows RED to activate RED specific features on
Robot files. Select from top menu bar:  
[File -> New -> Other -> Robot Framework -> Robot
Project](javascript:executeCommand\('org.eclipse.ui.newWizard\(newWizardId=org.robotframework.ide.eclipse.wizards.newRobotProject\)'\))

### Import Robot files into project

Import Robot files by drag&drop to created project, alternatively you can
import folders by right click on project and selecting  
[Import -> General -> File
System](javascript:executeCommand\('org.eclipse.ui.file.import\(importWizardId=org.eclipse.ui.wizards.import.FileSystem\)'\))

### Validate project and imported files

When Robot files has been imported, it is time to validate all the files to
get indication about possible issues - either errors in test cases or missing
libraries links in red.xml. Use [Project ->
Clean](javascript:executeCommand\('org.eclipse.ui.project.buildAll\(\)'\))
from top menu.

[Return to Help index](http://nokia.github.io/RED/help/)
