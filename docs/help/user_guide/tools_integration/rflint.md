[RED - Robot Editor User Guide](http://nokia.github.io/RED/help/index.md) >
[User guide](http://nokia.github.io/RED/help/user_guide/user_guide.md) >
[Integration with other
tools](http://nokia.github.io/RED/help/user_guide/tools_integration.md) >

## Robot Framework Lint analysis

Starting with RED 0.8.1 it is possible to run [Robot Framework
Lint](http://github.com/boakley/robotframework-lint/) analysis tool. Of course
one have to have it installed in the python installation used by the project.

### Running RfLint

RfLint can be run on selected file or folder (or whole project) in **Project
Explorer** view. In order to start analysis open context menu for selected
resource and choose **`Robot Framework -> Run RfLint analysis`**.

![](images/rflint_run.png)

The analysis should start and its progress is visible in **Progress** view. At
any time you can abort running validation:

![](images/rflint_progress.png)

Analysis performed from RED is reporting all the findings as **Problem
markers** of a separate type called **RfLint Problem**. This is a different
type than those reported by standard RED [ validation](../validation.md)
mechanism (they have **Robot Problem** type). Overall this means that the
findings are visible in **Problems** view and are also visible in editors.

![](images/rflint_problems.png)

In order to remove problems simply choose **`Robot Framework -> Clean RfLint
problems`** from context menu of selected resource.

Note

    Robot Framework Lint analysis is not run on excluded project parts (see more under topic [Limiting validation scope](../validation/scope.md). 

### Configuration

It is possible to configure RfLint behavior in Preferences ( `[ Window ->
Preferences -> Robot Framework -> Errors/Warnings -> RfLint
validation](javascript:executeCommand\('org.eclipse.ui.window.preferences\(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.rflint\)'\))`).

![](images/rflint_preferences.png)

The table shows all available rules grouped by the source files where they are
defined. It is possible to:

  * add **additional rules files** \- those files will be attached with all the rules when running RfLint analysis, 

  * **ignore** rules - by checking/unchecking them. By default all the rules are not ignored. It is also possible to check/uncheck file which will apply to all the rules from this file, 

  * define rule **severity** \- each rule can have severity specified: **Error** and **Warning**. Similarly one can change severity of a file which would change severities of all its rules, 

  * rule **configuration** \- as described in [RfLint Wiki](http://github.com/boakley/robotframework-lint/wiki/How-to-write-custom-rules) some rules can be parameterized: simply write arguments for given rule and remember that multiple arguments should be separated with colon (:) character. 

The preference page displays the rule documentation in order to have a quick
rule overview. Custom command line arguments for RfLint can be passed using
**Additional arguments for RfLint** field - refer toe RfLint user guide for
more details on possible arguments.

  

### Displaying rules documentation

It is possible to see the rule documentation straight from **Problems** view
by using context menu action on given RfLint problem. The documentation will
be displayed in **Documentation** view.

![](images/rflint_showdoc.png)  
  

