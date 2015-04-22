package org.robotframework.ide.core.testData.parser.result;

import org.robotframework.ide.core.testData.parser.IDataLocator;
import org.robotframework.ide.core.testData.parser.IParsePositionMarkable;


/**
 * Holder of incorrect data plus their location in file
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class TrashData<InputFormatType extends IParsePositionMarkable> {

    private final InputFormatType trash;
    private final IDataLocator<InputFormatType> locationInFile;


    public TrashData(InputFormatType trash,
            IDataLocator<InputFormatType> location) {
        this.trash = trash;
        this.locationInFile = location;
    }


    public InputFormatType getTrash() {
        return trash;
    }


    public IDataLocator<InputFormatType> getLocation() {
        return locationInFile;
    }
}
