/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import java.util.List;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.FileRegion;
import org.robotframework.ide.core.testData.model.RobotFileOutput.BuildMessage;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.TextPosition;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.Container;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.Container.ContainerType;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.ContainerElement;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.ContainerElementType;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.IContainerElement;


public class ContainerMappingHelper {

    private final int contentStart;
    private final int contentEnd;
    private final IElementDeclaration dec;
    private final MappingResult mappingResult;


    private ContainerMappingHelper(final int contentStart,
            final int contentEnd, final IElementDeclaration dec,
            final MappingResult mappingResult) {
        this.contentStart = contentStart;
        this.contentEnd = contentEnd;
        this.dec = dec;
        this.mappingResult = mappingResult;
    }


    public int getContentStart() {
        return contentStart;
    }


    public int getContentEnd() {
        return contentEnd;
    }


    public IElementDeclaration getContainerDeclarationHolder() {
        return dec;
    }


    public MappingResult getMappingResult() {
        return mappingResult;
    }


    public static ContainerMappingHelper createDeclaration(
            final Container container, final FilePosition currentPosition,
            final MappingResult mappingResult) {
        ContainerMappingHelper mappingHelper = null;

        ContainerType containerType = container.getContainerType();
        List<IContainerElement> elements = container.getElements();

        int contentStart = 1;
        int contentEnd;
        IElementDeclaration dec = null;
        ContainerElement startElement = (ContainerElement) elements.get(0);
        TextPosition startPos = startElement.getPosition();
        int textLength = (startPos.getEnd() - startPos.getStart());
        FilePosition newPosition = currentPosition;
        TextPosition endPos = null;
        if (containerType == ContainerType.MIX) {
            contentStart = 0;
            contentEnd = elements.size();
        } else {
            newPosition = new FilePosition(currentPosition.getLine(),
                    currentPosition.getColumn() + textLength,
                    currentPosition.getOffset() + textLength);

            if (container.isOpenForModification()) {
                if (container.getParent() != null) {
                    BuildMessage warn = BuildMessage.createWarnMessage(
                            createWarningAboutMissingClose(containerType,
                                    startElement), mappingResult.getFilename());
                    warn.setFileRegion(new FileRegion(currentPosition,
                            newPosition));
                    mappingResult.addBuildMessage(warn);
                }
                contentEnd = elements.size();
            } else {
                endPos = ((ContainerElement) elements.get(elements.size() - 1))
                        .getPosition();
                contentEnd = elements.size() - 1;
            }

            if (containerType == ContainerType.INDEX) {
                dec = new IndexDeclaration(startPos, endPos);
            } else if (containerType == ContainerType.VARIABLE) {
                dec = new VariableDeclaration(startPos, null, endPos);
            } else {
                throw new UnsupportedOperationException("Type " + containerType
                        + " is not supported yet!");
            }
        }

        mappingResult.setLastFilePosition(newPosition);
        mappingHelper = new ContainerMappingHelper(contentStart, contentEnd,
                dec, mappingResult);
        return mappingHelper;
    }


    private static String createWarningAboutMissingClose(
            final ContainerType containerType,
            final ContainerElement startElement) {

        return String.format(
                "Missing closing \'%s\' for type %s in %s.",
                ""
                        + ContainerElementType.getCloseContainerType(
                                containerType.getOpenType())
                                .getRepresentation(), containerType,
                startElement);
    }


    @Override
    public String toString() {
        return String
                .format("ContainerMappingHelper [contentStart=%s, contentEnd=%s, dec=%s, mappingResult=%s]",
                        contentStart, contentEnd, dec, mappingResult);
    }
}