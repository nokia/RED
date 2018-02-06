## Locally launched tests using own scripts

The **Robot** launch configurations may be called through user-defined
executable/script. This is useful when integrating tests launches from RED
with other tools like PyDev for Robot  & Python debugging, Maven, Gradle, etc.

The general idea is that the command line call which RED executes for
launching is wrapped with a call to user defined executable. For example RED
would normally use following command line call:

` python.exe -m robot.run --suite mySuite c:\\location\\to\\project `

but when script `my_script.bat` with arguments `arg1`, `arg2` is used the
command line call becomes:

` my_script.bat arg1 arg2 python.exe -m robot.run --suite mySuite
c:\\location\\to\\project `

The script is now free to process the arguments which were passed - it may use
them or not, or select those which are interesting for the script but
eventually it should start robot tests execution.

Note

    By default, RED passes Robot executable command line to user script as is thus each space separated entry is own parameter. From above example, following Robot command line passed to script by RED: 

` python.exe -m robot.run --suite mySuite c:\\location\\to\\project `

is passed to a user script as 6 arguments. This can be changed in
[preferences](launch_prefs.md), so whole Robot executable command line is
wrapped with quotation marks. This affects how script handles input
parameters.

### Defining script call in launch configuration

Script/executable to be used when launching is defined in launch configuration
dialog at **Executor** tab:

![](images/local_config_exec_script.png)

In first field an executable has to be chosen from local system. Under Windows
this may be `.exe`, `.bat` or `.com` file. Under Linux this may be any binary
executable, but also any text script file which contains
[shebang](https://en.wikipedia.org/wiki/Shebang_\(Unix\)) line - just remember
that this file need to have `x` permission granted, so that the system will
allow to execute it.

In second line any arguments which are required by the script can be passed.

Note

    Default values for both executable and arguments fields can be defined in [preferences](launch_prefs.md), so every time when RED is creating new launch configuration it will use those values. It may be useful if you want to always use some script without manually changing launch configurations before launching. 

### Simple example

Note

    User scipts examples can be found at <https://github.com/nokia/RED/tree/master/src/RobotUserScripts>. 

We'll use simple python script which will be used to launch tests:

` import sys  
from io import StringIO  
from subprocess import Popen, PIPE  
  
print('##########')  
print('Running Robot tests via script!')  
print('##########')  
sys.stdout.flush()  
  
execution = Popen(sys.argv[1:])  
execution.communicate()  
`

Save code from above into `my_script.py` file, then at **Executor** tab of
desired launch configuration browse your computer for **python.exe** and set
it in **executable file** field and pass the location to `my_script.py` inside
**arguments field**.

When configuration defined as described will be launched you should be able to
see the message from script as well as the output from tests.

  
  

[Return to Help index](http://nokia.github.io/RED/help/)
