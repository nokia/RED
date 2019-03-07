[RED - Robot Editor User Guide](http://nokia.github.io/RED/help/index.md) >
[User guide](http://nokia.github.io/RED/help/user_guide/user_guide.md) >
[Working with
RED](http://nokia.github.io/RED/help/user_guide/working_with_RED.md) >

## Variable mapping - dealing with parameterized paths to libraries and
resources

Whenever parameterized file path is used in resources or libraries paths
(paths are resolved during Robot runtime when parameter in path is known), RED
will not be able to evaluate parameter value by itself.
![](images/variable_mapping_5.png)

Variables mappings can be used to statically assign value to parameters in
paths to resolve such paths to resources and libraries.

Note

    Variables provided in red.xml are used only by RED for validation purpose and are not added to Robot run command line. During test execution variables have to be provided by Robot.

Open **`red.xml -> Variables`** and assign static value for path parameter in
Variable mappings section: ![](images/variable_mapping_6.gif) When successful,
path will be recognized and validation will take place.
![](images/variable_mapping_7.png)

