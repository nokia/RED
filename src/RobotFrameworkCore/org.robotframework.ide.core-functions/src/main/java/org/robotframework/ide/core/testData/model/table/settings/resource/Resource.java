package org.robotframework.ide.core.testData.model.table.settings.resource;

import org.robotframework.ide.core.testData.model.table.settings.AExternalImported;
import org.robotframework.ide.core.testData.model.table.settings.ImportElementLocation;


public class Resource extends AExternalImported {

    private final ImportedResource resourceWord;


    public Resource(ImportedResource resourceWord,
            ImportElementLocation location) {
        super(ImportTypes.RESOURCE, location);
        this.resourceWord = resourceWord;
    }

}
