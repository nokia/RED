package org.robotframework.ide.core.testData.model.table;

import org.robotframework.ide.core.testData.parser.IDataLocator;
import org.robotframework.ide.core.testData.parser.IParsePositionMarkable;


/**
 * User defined scalars, lists included in test data
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class VariablesTable implements IRobotSectionTable {

    public static final String TABLE_NAME = "Variables";
    private boolean declarationOfTableAppears = false;
    private IDataLocator<? extends IParsePositionMarkable> positionInFile;


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


    public void setTableSectionPosition(
            IDataLocator<? extends IParsePositionMarkable> positionInFile) {
        this.positionInFile = positionInFile;
    }


    @Override
    public IDataLocator<? extends IParsePositionMarkable> getTableSectionPosition() {
        return this.positionInFile;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (declarationOfTableAppears ? 1231 : 1237);
        result = prime * result
                + ((positionInFile == null) ? 0 : positionInFile.hashCode());
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
        VariablesTable other = (VariablesTable) obj;
        if (declarationOfTableAppears != other.declarationOfTableAppears)
            return false;
        if (positionInFile == null) {
            if (other.positionInFile != null)
                return false;
        } else if (!positionInFile.equals(other.positionInFile))
            return false;
        return true;
    }
}
