## Working with Remote Library

### Introduction

Following [RobotFramework
RemoteInterface](https://github.com/robotframework/RemoteInterface)

The remote library interface allows Robot Framework test libraries to be run
as external processes. An important use case for this support is running test
libraries on different machines than where Robot Framework itself is
executed.Another big use case is implementing test libraries using other
languages that Robot Framework supports natively. In practice test libraries
can be implemented using any language that supports the XML-RPC protocol that
the remote interface uses for communication.

The remote interface consists of the Remote library, remote servers and actual
test libraries running behind these servers.

The Remote library is one of Robot Framework's standard libraries and thus
automatically installed with the framework. It does not have any keywords of
its own, but instead works as a proxy between Robot Framework and remote
servers.  
Remote servers expose the keywords provided by the actual test libraries to
the Remote library. The Remote library and remote servers communicate using a
simple remote protocol on top of an XML-RPC channel.

### Using Remote libraries in RED

#### Settings in Referenced Libraries in red.xml

In order to use Keywords from Remote Library together with validation and code
assistance, information about Remote Library address needs to be included in
red.xml under Referenced Libraries tab.

  
  
![](remote_library/remote_library_settings.png)  
  

Note that IP, Port and name library served by remote library server may
differ. Please follow usage details of your Remote Library.

#### Settings in TestSuite

As with standard libraries, Remote library needs to be imported in _Settings_
part.

Standard syntax is as follows: _Library Remote http://${ADDRESS}:${PORT}_

  
  
![](remote_library/remote_library_testcase.png)  
  

For more examples please check RobotFramewoke RemoteInterface webpage
<https://github.com/robotframework/RemoteInterface#available-remote-servers>

[Return to Help index](http://nokia.github.io/RED/help/)
