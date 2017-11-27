# RED - Robot Editor
## Building from sources

### Prerequisites
- maven installed
- network connection to allow maven to fetch updartes from Eclipse's update sites
- python to execute RedReleaseVersionUpdater.py which updates version number of packages for proper dependency management
- Xvfb for some unit tests, this can be bypassed by commenting/removing sections ```<reporting>``` and ```<module>../org.robotframework.ide.eclipse.main.plugin.tests</module> ``` in src/Eclipse-IDE/org.robotframework.ide.eclipse.main.feature.buildParent/pom.xml 

### Setup
- source should be under src folder (just like in git repo)
- copy PMD files (misc/scripts/building/PMD) to src/PMD folder
- copy findbugs file (misc/scripts/building/findbugs) to src/findbugs folder
- copy RedReleaseVersionUpdater.py (misc/scripts/building/RedReleaseVersionUpdater.py) to parent of src 

Folder structure should look as follows:
```
RedReleaseVersionUpdater.py
src/Eclipse-IDE
src/findbugs
src/PMD
src/RobotFrameworkCore
```
If you need to change it, check folder dependencies in maven poms and RedReleaseVersionUpdater.py

### Building
Execut commands in partent to src,assign proper values to variables (*eclipseVersion* can hold string Oxygen number *version* should be in notation x.x.x)  

```
eclipseVersion=mars
version=0.6.5

python "RedReleaseVersionUpdater.py" --start-dir "dir=src/Eclipse-IDE" newVersion=$version
mvn clean install site -f src/Eclipse-IDE/org.robotframework.ide.eclipse.parent/pom.xml -Declipse.version=$eclipseVersion
mvn clean install site -f src/Eclipse-IDE/org.robotframework.ide.eclipse.product.feature.buildParent/pom.xml -Dcore.functions.dir=../../../src/RobotFrameworkCore -Declipse.version=$eclipseVersion
mvn clean install site -f src/Eclipse-IDE/org.robotframework.ide.eclipse.product.feature.build/pom.xml -Declipse.version=$eclipseVersion
mvn clean install site -f src/Eclipse-IDE/org.robotframework.ide.eclipse.main.feature.buildParent/pom.xml -Dcore.functions.dir=../../../src/RobotFrameworkCore -Declipse.version=$eclipseVersion
mvn clean install site -f src/Eclipse-IDE/org.robotframework.ide.eclipse.main.feature.build/pom.xml -Declipse.version=$eclipseVersion
mvn clean install site -f src/Eclipse-IDE/org.robotframework.ide.eclipse.product.product/pom.xml -Declipse.version=$eclipseVersion
mvn clean install site -f src/Eclipse-IDE/org.robotframework.ide.eclipse.main.feature.update-site/pom.xml -Declipse.version=$eclipseVersion
```


### Binaries
Binaries can be found under
-  src/Eclipse-IDE/org.robotframework.ide.eclipse.product.product/target/products/
-  src/Eclipse-IDE/org.robotframework.ide.eclipse.main.feature.build/target/

