package org.robotframework.ide.core.testData.model.table;

/**
 * User defined scalars, lists included in test data
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class VariablesTable implements IRobotSectionTable {

    private final boolean declarationOfTableAppears = false;


    @Override
    public boolean isPresent() {
        return declarationOfTableAppears;
    }


    @Override
    public String getTableName() {
        return "Variables";
    }
}
