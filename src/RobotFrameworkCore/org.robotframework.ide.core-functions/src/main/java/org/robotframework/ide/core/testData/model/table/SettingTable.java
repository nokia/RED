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

    public static final String TABLE_NAME = "Settings";
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
    public String getName() {
        return TABLE_NAME;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (declarationOfTableAppears ? 1231 : 1237);
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SettingTable other = (SettingTable) obj;
        if (declarationOfTableAppears != other.declarationOfTableAppears)
            return false;
        return true;
    }
}
