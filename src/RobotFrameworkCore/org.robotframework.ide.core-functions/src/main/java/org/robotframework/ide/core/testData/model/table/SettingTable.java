package org.robotframework.ide.core.testData.model.table;



/**
 * Mapping for Setting table used to import test libraries, resource files and
 * variable files and to define metadata for test suites and test cases. It can
 * included in test case files and resource files. Note that in resource file, a
 * Setting table can only include settings for importing libraries, resources,
 * and variables.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class SettingTable implements IRobotSectionTable {

    private boolean declarationOfTableAppears = false;


    public void unsetPresent() {
        this.declarationOfTableAppears = false;
    }


    public void setPresent() {
        this.declarationOfTableAppears = true;
    }


    @Override
    public boolean isPresent() {
        return this.declarationOfTableAppears;
    }


    @Override
    public String getTableName() {
        return "Settings";
    }
}
