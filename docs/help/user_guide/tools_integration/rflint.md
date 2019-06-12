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

#### General tab

On **General** tab it is possible to define:

  * **additional rules files** \- those files will be attached with all the rules when running RfLint analysis. Those rules can be further configured at **Rules** tab, 

  * **additional arguments** \- custom RfLint arguments that will be used for analysis. See RfLint user guide for more details on possible arguments. 

Note

    Additional arguments field accepts Eclipse [string variables](../launching/string_substitution.md).
![](images/rflint_prefs1.png)

#### Rules tab

On **Rules** tab it is possible to configure rule severity as well as
additional rule parameters.

  * rule **severity** \- rule of a given name can have severity specified: **Error** and **Warning** changes severity to one of those levels, while **Ignore** will silence the rule (Ignore can be set also by check-box), 

  * rule **configuration** \- as described in [RfLint Wiki](http://github.com/boakley/robotframework-lint/wiki/How-to-write-custom-rules) some rules can be parameterized: simply write arguments for given rule and remember that multiple arguments should be separated with colon (:) character. 

Note

    RED will only store changes to default rule configurations. The changed rule is marked with bold font. It is possible that RED is storing configuration of a rule which is currently not recognised by your RfLint installation and such rule is marked red.
![](images/rflint_prefs2.png)  
  

