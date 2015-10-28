/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import java.util.List;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.Container;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.IContainerElement;


public class DeclarationMapper {

    private String fileMapped;


    public DeclarationMapper() {
        this.fileMapped = "<NOT_SET>";
    }


    public MappingResult map(final FilePosition fp, final Container container,
            final String filename) {
        MappingResult mappingResult = new MappingResult(fp, filename);
        FilePosition currentPosition = fp;

        List<IContainerElement> elements = container.getElements();
        if (!elements.isEmpty()) {

        } else {

        }

        mappingResult.setLastFilePosition(currentPosition);

        return mappingResult;
    }


    public String getFileMapped() {
        return fileMapped;
    }


    public void setFileMapped(final String fileMapped) {
        this.fileMapped = fileMapped;
    }
}
