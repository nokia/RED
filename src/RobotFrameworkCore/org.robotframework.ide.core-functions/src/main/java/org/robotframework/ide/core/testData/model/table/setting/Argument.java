package org.robotframework.ide.core.testData.model.table.setting;

/**
 * Represents {@code argument_value} or {@code argument_name=argument_value} <br/>
 * Example: {@code Library   OperatingSystem2    d_arg_value}
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 * @see LibraryReference
 * @see VariablesFileReference
 * 
 */
public class Argument {

    private String name;
    private String value;


    /**
     * @return optional argument name
     */
    public String getName() {
        return name;
    }


    /**
     * @param name
     *            optional argument name
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * @return mandatory argument value
     */
    public String getValue() {
        return value;
    }


    /**
     * @param value
     *            mandatory argument value
     */
    public void setValue(String value) {
        this.value = value;
    }
}
