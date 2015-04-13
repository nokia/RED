package org.robotframework.ide.core.testData.model.table;

/**
 * Table of tests included in test case file and test suites
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class TestCaseTable implements IRobotSectionTable {

    public static final String TABLE_NAME = "Test Case";
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
        return TABLE_NAME;
    }
}
