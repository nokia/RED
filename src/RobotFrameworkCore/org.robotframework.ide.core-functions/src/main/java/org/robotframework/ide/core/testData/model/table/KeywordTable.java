package org.robotframework.ide.core.testData.model.table;

/**
 * Represents high-level user keyword defined in test data file - test case or
 * better in resource files
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class KeywordTable implements IRobotSectionTable {

    private boolean declarationOfTableAppears = false;


    @Override
    public boolean isPresent() {
        return declarationOfTableAppears;
    }


    @Override
    public void setPresent() {
        declarationOfTableAppears = true;
    }


    @Override
    public void unsetPresent() {
        declarationOfTableAppears = false;
    }


    @Override
    public String getName() {
        return "Keywords";
    }
}
