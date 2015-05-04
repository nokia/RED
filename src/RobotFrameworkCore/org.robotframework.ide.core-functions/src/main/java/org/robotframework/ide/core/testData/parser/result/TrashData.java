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


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((locationInFile == null) ? 0 : locationInFile.hashCode());
        result = prime * result + ((trash == null) ? 0 : trash.hashCode());
        return result;
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TrashData<InputFormatType> other;
        try {
            other = ((TrashData<InputFormatType>) obj);
        } catch (ClassCastException cce) {
            return false;
        }
        if (locationInFile == null) {
            if (other.locationInFile != null)
                return false;
        } else if (!locationInFile.equals(other.locationInFile))
            return false;
        if (trash == null) {
            if (other.trash != null)
                return false;
        } else if (!trash.equals(other.trash))
            return false;
        return true;
    }
}
