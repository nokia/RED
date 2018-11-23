## Diagnostics

### Session server

RED is using small python script `robot_session_server.py` as a bridge between
the IDE itself and your **Robot Framework** installation. This is needed for
various reasons: checking RF version so that files are validated accordingly,
generating library documentations, running **RfLint** , providing variables
from python files and so on.

Sometimes when diagnosing problems with RED it may be desirable to take a look
at operations performed by `robot_session_server.py` process. By default this
server runs in background invisibly for users. There are however two
possibilities to have a deeper look.

#### 1\. Displaying server console

  1. Exit RED 
  2. edit `RED.ini` file (or `eclipse.ini` if you're using eclipse with RED installed as a feature) 
  3. add `-Dred.showSessionConsole=true` line somewhere after `-vmargs` line 
  4. start RED/eclipse again 

After starting again there should be server output visible in **Console**
view. Server is logging every call coming from RED together with arguments as
well as results/exceptions of that call.

#### 2\. Running server manually and connecting to it

Firstly server script files has to be obtained. They can be downloaded from [
GitHub
](https://github.com/nokia/RED/tree/master/src/RobotFrameworkCore/org.robotframework.ide
.core-functions/src/main/python/scripts) (all files except
`TestRunnerAgent.py` are needed). Alternatively those files can be copied from
temporary directory of your system when RED is running: go to temporary
directory and look for a directory with name starting from `RobotTempDir`
containing same python files as on GitHub.

  1. Start server by issuing: 

python robot_session_server.py 12345

in command line

  2. this will start server on port `12345` at `localhost`
  3. exit RED 
  4. edit `RED.ini` file (or `eclipse.ini` if you're using eclipse with RED installed as a feature) 
  5. add `-Dred.connectToServerAt=127.0.0.1:12345` line somewhere after `-vmargs` line 
  6. start RED/eclipse again 

After starting again RED should connect to manually started session server.

[Return to Help index](http://nokia.github.io/RED/help/)
