package org.robotframework.ide.core.testData.model.table.settings;

import org.robotframework.ide.core.testData.model.common.Comment;


public abstract class AExternalImported {

    private final ImportTypes type;
    private ImportElementLocation location;
    private Comment comment;


    public AExternalImported(final ImportTypes type,
            final ImportElementLocation location) {
        this.type = type;
        this.location = location;
    }


    public ImportTypes getType() {
        return type;
    }

    public enum ImportTypes {
        LIBRARY, RESOURCE, VARIABLES
    }
}
