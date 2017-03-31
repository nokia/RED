## Debugging Robot

Debug functionality is unique way of checking what is happening during
TestCase and TestSuite execution. It works similar as debug functionality in
most of programming languages - it allows to track execution of program for
checking unwanted behavior either on TestCase side or in tested software.

It is important to note that Debug functionality works only with text editor
(debug actions can be set only there) therefore it is crucial to active it
before switching to Debug perspective.

Try to not make changes in script in Debug mode as Eclipse have a problem with
following changes respectful to Breakpoints.

Note

    Step-into works only for Robot written Keywords. If you wish to step-into Python Keywords, check [Debug Robot and Python scripts](robot_python_debug.html) help content. 

### Basics

In order to work with Debug, save the file beforehand. If you made any changes
in Table editors, close and reopen file so changes will be visible in Source
tab (this will be non issue in close future). Activate Source page and change
to Debug perspective:

![](images/debug_1.png)

Debug perspective looks as follows:

![](images/debug_2.png)

First thing with working with Debug is to place at least one breakpoint. This
allows RED to pause Debug execution and activate Stepping options. To place
breakpoint in Source view click on line in script and under right click menu
choose Add breakpoint. You can also add breakpoint directly by clicking on
line number in Source editor view. When successful, blue ball icon will appear
next to it, new breakpoint entry will be visible in Breakpoint lists.

#### Start Debug

To start debugging you need to [launch](ui_elements.html) the configuration in
debug mode. For example by clicking on "green bug" icon at the top toolbar:

![](images/debug_3.png)

#### Limiting TestCases to be debugged

Limit works just like TestCase limitation in Robot perspective. In order to
limit which TC will be executed in Debug, open [ Run -> Debug
Configurations...](javascript:executeCommand\('org.eclipse.debug.ui.commands.OpenDebugConfigurations'\))
dialog and choose which cases should be executed:

![](images/debug_4.png)

### Working with Debug

#### Breakpoints and execution stepping

Breakpoints are essential items, each pause execution and allows to initiate
Debug actions. When breakpoint line is activated during execution, following
icons are active:

![](images/debug_5.png)

moving from left to right:

  * **Skip All Breakpoints** \- allow to continue execution onwards without stopping on defined breakpoints
  * **Resume** \- resumes test execution
  * **Pause**
  * **Stop**
  * **Disconnect**
  * **Step Into** \- F5 \- each F5 key press will execute active line and move to next one. If active line consists Keyword or embedded TestCase, test executor will jump into item and execute it line by line. To exit from executing inherited items use Step Return (F7)
  * **Step Over** \- F6 \- each F6 key press will execute active line and move to next one. If keyword exists in current line, keyword result will be returned without going into Keyword content
  * **Step Return** \- F7 \- allows to return to main TestCase execution from embedded TestCase or Keyword if Step Into was used before

List of breakpoints can be seen in upper right side of the Debug perspective
in **Breakpoints** view:

![](images/debug_breakpoints.png)

  * each breakpoint can be enabled/disabled
  * each breakpoint can be removed
  * for each breakpoint, action condition can be set: 
    * Hit count: breakpoint pause execution only after selected number of previous hits. Useful to activate breakpoint inside a loop statement on certain iteration
    * Conditional: expression when pass will activate breakpoint. Expression should be in RobotFramework syntax, for instance: `Should be Equal ${variable} 10`

#### Variables watch

Variables can be watched and changed during TestCase Debug. All actions
related to variables are accessible in **Variables** view which is shared with
Breakpoints list. Variables are only shown during TestCase execution.

![](images/debug_variables.png)

At the top of the list, TestCase variables are displayed followed by RF
environmental variables. Each variable can be changed during test run during
breakpoint event. When variable is changed, it is indicated by distinct colour
in the list.

