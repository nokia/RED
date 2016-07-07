# RED - Robot Editor
## General information 

RED is modern editor based on Java IDEs (Eclipse, IntelliJ in future) to allow quick and comfortable work with Robot testware.

**Latest release - RED 0.6.5 (2016-07-07): https://github.com/nokia/RED/releases/latest**

## What RED provides:
* text editor with validation and code colouring
* table editors like in Ride (currently read-only)
* debug&remote debug with:
	* breakpoints
	* testcase stepping (step into, step over)
	* runtime variable lookup & modification
* code assistance & completion for variables, keywords, testcases, resources and libraries
* real time testcase validation
* execution view
* support for plugins via Eclipse mechanisms

## Binaries distribution:
RED is distributed as independent binary (Eclipse product) and Eclipse feature to be installed on existing Eclipse binary. 

###Install form Marketplace/Update Site

Marketpalce: Click Help -> Eclipse Marketplace -> and type into Find field "RED"

Update Site: Click Help -> Install New Software -> Add and set address in Location to:
http://master.dl.sourceforge.net/project/red-robot-editor/repository

### Update existing RED installation
We recommend to not to do direct update of newer version, instead perform unistall old RED and install new RED after Eclipse restart.
Open Help -> Installation Details, select old RED feature and perform unistall, restart Eclipse

## RED Help
![RED Robot Editor's application help](https://github.com/nokia/RED/tree/master/red_help)
## Look & feel
![](https://github.com/nokia/RED/blob/master/doc/img/red_overview_source_1.png "Robot perspective with text editor")

![](https://github.com/nokia/RED/blob/master/doc/img/red_testcases_table.png "Table editor")

![](https://github.com/nokia/RED/blob/master/doc/img/red_overview_debug.png "Debug perspective")





