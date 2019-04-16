# RED - Robot Editor
## Building from sources

### Prerequisites
- Eclipse for RCP and RAP Developers
- JDK 1.8+
- Python 2.7+ or Python 3.4+
- Following modules installed with pip
	- robotframework
	- unittest-xml-reporting
 
### Eclipse settings
- Set JDK 1.8+ as default (```Windows → Preferences → Java → Installed JREs```)
- Check network connection, if needed define proxy servers in
	- Eclipse preferences (```Windows → Preferences → General → Network Connections```)
	- Maven settings.xml file (```Windows → Preferences → Maven → User Settings```)

### Project setup
- Clone RED repository
- Import to workspace all Eclipse project from RED/src folder (```File →  Import →  General →  Existing Projects into Workspace```)
- Setup Maven plugin connectors
	- Use Quick Fix (Ctrl+1) on marker with message starting from "Plugin execution not covered by lifecycle configuration"
	- Use option "Discover new m2e connectors"
	- Use option "Select All" to select all pom.xml files
	- Follow instructions to install connectors
	- After Eclipse restart Maven updates all projects
	- Revert all changes in RED repository made by Maven update
- Set target platform, default one can be found in org.robotframework.ide.eclipse.target-platform/pom.xml (```Windows → Preferences → Plug-in Development → Target Platform```)

### Product building
- Build following projects in given order using Maven goals "clean install" (default eclipse.version parameter can be replaced with supported ones from org.robotframework.ide.eclipse.target-platform/pom.xml) on:
	- org.robotframework.ide.eclipse.parent
	- org.robotframework.ide.eclipse.product.feature.buildParent
	- org.robotframework.ide.eclipse.product.feature.build
	- org.robotframework.ide.eclipse.main.feature.buildParent
	- org.robotframework.ide.eclipse.main.feature.build
	- org.robotframework.ide.eclipse.product.product
	- org.robotframework.ide.eclipse.main.feature.update-site
- Clean all projects (```Project → Clean → Clean all projects```) and use Refresh (F5) action on them 
- Run org.robotframework.ide.eclipse.product.plugin as Eclipse Application selecting org.robotframework.red as product to run
- Binaries can be found under
	- src/Eclipse-IDE/org.robotframework.ide.eclipse.product.product/target/products/
	- src/Eclipse-IDE/org.robotframework.ide.eclipse.main.feature.build/target/

### Remarks
- org.robotframework.ide.core-functions-0.0.1-SNAPSHOT.jar can be copied from src/RobotFrameworkCore/org.robotframework.ide.core-functions/target to src/Eclipse-IDE/org.robotframework.ide.eclipse.main.plugin/lib to avoid rebuilding all after changes in core
- Problems like "Artifact has not been packaged yet. When used on reactor artifact, copy should be executed after packaging: see MDEP-187." can be solved like in following thread https://stackoverflow.com/questions/30642630/artifact-has-not-been-packaged-yet#answer-50745567
by using mapping file lifecycle-mapping-metadata.xml in (```Windows → Preferences → Maven → Lifecycle Mappings```)
- Static analysis can be run with "site" Maven goal, addition rule configuration files specified in pom.xml reporting section have to be provided
- For release purpose RED version, description and update sites should be updated in configuration files (category.xml, feature.xml, pom.xml, feature.properties, RED.product, plugin.xml, about.ini)
- For platforms newer than photon org.eclipse.equinox.ds dependency from RED.product has to be replaced with org.apache.felix.scr
