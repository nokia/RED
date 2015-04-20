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
        TestCaseTable other = (TestCaseTable) obj;
        if (declarationOfTableAppears != other.declarationOfTableAppears)
            return false;
        return true;
    }
}
