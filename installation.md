## How to install

### Binaries
All RED binaries are stored under Release section:

https://github.com/nokia/RED/releases

### Product
Product zip is ready to use RED installation bundled with latest Eclipse. 
There is no need to install RED feature into it. 
Choose proper OS version from **Downloads** section in Release section in RED GitHub page.

### Feature
Please refer to First Steps doc under https://github.com/nokia/RED/blob/master/First_steps.md

#### Install form Marketplace/Update Site
Click Help -> Eclipse Marketplace -> and type into Find field "RED robot"

Click Help -> Install New Software -> Add and set address in Location to:
http://master.dl.sourceforge.net/project/red-robot-editor/repository



#### Install on clean Eclipse 
Download RED eclipse feature zip
In short:
- GUI: Help -> Install New software -> Add -> Archive and continue with prompts (unselect "Contact all update sites) 
- CLI: by issuing command: 

```eclipse -application org.eclipse.equinox.p2.director -nosplash -consoleLog --launcher.suppressErrors -repository jar:file:<PATH_TO_ZIP>\!/,http://download.eclipse.org/releases/mars/   -installIU org.robotframework.ide.eclipse.main.feature.feature.group ```

#### Update existing RED feature
Update can be done in the same way as installation on clean Eclipse.
If RED update site is present in Eclipse Check For Updates from Help menu will provide information about new version avaliable.


