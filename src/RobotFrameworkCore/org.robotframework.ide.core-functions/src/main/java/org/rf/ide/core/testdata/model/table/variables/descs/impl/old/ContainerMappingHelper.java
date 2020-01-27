/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.variables.descs.impl.old.Container.ContainerType;

class ContainerMappingHelper {

    private final int contentStart;

    private final int contentEnd;

    private final IElementDeclaration dec;

    private final MappingResult mappingResult;

    private ContainerMappingHelper(final int contentStart, final int contentEnd, final IElementDeclaration dec,
            final MappingResult mappingResult) {
        this.contentStart = contentStart;
        this.contentEnd = contentEnd;
        this.dec = dec;
        this.mappingResult = mappingResult;
    }

    int getContentStart() {
        return contentStart;
    }

    int getContentEnd() {
        return contentEnd;
    }

    IElementDeclaration getContainerDeclarationHolder() {
        return dec;
    }

    MappingResult getMappingResult() {
        return mappingResult;
    }

    static ContainerMappingHelper createDeclaration(final Container container,
            final FilePosition currentPosition, final MappingResult mappingResult) {
        ContainerMappingHelper mappingHelper = null;

        final ContainerType containerType = container.getContainerType();
        final List<IContainerElement> elements = container.getElements();

        FilePosition newPosition = currentPosition;
        IElementDeclaration dec = null;
        int contentStart = -1;
        int contentEnd = -1;
        if (!elements.isEmpty()) {
            contentStart = 1;
            final IContainerElement startElement = elements.get(0);
            final TextPosition startPos = startElement.getPosition();
            final int textLength = (startPos.getEnd() - startPos.getStart());
            TextPosition endPos = null;
            if (containerType == ContainerType.MIX) {
                contentStart = 0;
                contentEnd = elements.size();
            } else {
                newPosition = new FilePosition(currentPosition.getLine(), currentPosition.getColumn() + textLength,
                        currentPosition.getOffset() + textLength);

                if (container.isOpenForModification()) {
                    if (container.getParent() != null) {
                        final BuildMessage warn = BuildMessage.createWarnMessage(
                                createWarningAboutMissingClose(containerType),
                                new FileRegion(currentPosition, newPosition));
                        mappingResult.addBuildMessage(warn);
                    }
                    contentEnd = elements.size();
                } else {
                    endPos = ((ContainerElement) elements.get(elements.size() - 1)).getPosition();
                    contentEnd = elements.size() - 1;
                }

                if (containerType == ContainerType.INDEX) {
                    dec = new IndexDeclaration(startPos, endPos);
                } else if (containerType == ContainerType.VARIABLE) {
                    dec = new VariableDeclaration(startPos, endPos);
                } else {
                    throw new UnsupportedOperationException("Type " + containerType + " is not supported yet!");
                }
            }
        }

        mappingResult.setLastFilePosition(newPosition);
        mappingHelper = new ContainerMappingHelper(contentStart, contentEnd, dec, mappingResult);
        return mappingHelper;
    }

    private static String createWarningAboutMissingClose(final ContainerType containerType) {
        return String.format("Missing closing bracket \'%s\' for type %s.",
                ContainerElementType.getCloseContainerType(containerType.getOpenType()).getRepresentation().get(0),
                containerType);
    }

    @Override
    public String toString() {
        return String.format("ContainerMappingHelper [contentStart=%s, contentEnd=%s, dec=%s, mappingResult=%s]",
                contentStart, contentEnd, dec, mappingResult);
    }
}
