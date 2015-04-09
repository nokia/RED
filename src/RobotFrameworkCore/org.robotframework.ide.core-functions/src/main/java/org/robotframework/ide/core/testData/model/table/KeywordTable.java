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

    @Override
    public boolean isPresent() {
        return false;
    }


    @Override
    public String getTableName() {
        return "Keywords";
    }
}
