/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.Container;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.Container.ContainerType;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.ContainerElementType;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.IContainerElement;


public class DeclarationMapper {

    private final Map<ContainerElementType, IElementMapper> mappers = new HashMap<>();
    private String fileMapped;


    public DeclarationMapper() {
        this.fileMapped = "<NOT_SET>";
        mappers.put(ContainerElementType.TEXT, new TextDeclarationMapper());
    }


    public MappingResult map(final FilePosition fp, final Container container,
            final String filename) {
        MappingResult mappingResult = new MappingResult(fp, filename);
        FilePosition currentPosition = fp;

        if (container.getContainerType() == ContainerType.MIX) {
            if (container.getParent() != null) {
                throw new IllegalStateException(
                        "Mix container is only supported on the top level extraction.");
            }
        }

        ContainerMappingHelper mappingHelper = ContainerMappingHelper
                .createDeclaration(container, currentPosition, mappingResult);
        IElementDeclaration topContainer = mappingHelper
                .getContainerDeclarationHolder();
        if (topContainer != null) {
            mappingResult.addMappedElement(topContainer);
        }
        List<IContainerElement> elements = container.getElements();
        for (int index = mappingHelper.getContentStart(); index < mappingHelper
                .getContentEnd(); index++) {
            IContainerElement containerElement = elements.get(index);
            if (containerElement.isComplex()) {
                MappingResult subResult = map(currentPosition,
                        (Container) containerElement, filename);
                mappingResult.addBuildMessages(subResult.getMessages());
                if (topContainer != null) {
                    List<IElementDeclaration> mappedElements = subResult
                            .getMappedElements();
                    for (IElementDeclaration dec : mappedElements) {
                        topContainer.addElementDeclarationInside(dec);
                        dec.setLevelUpElement(topContainer);
                    }
                } else {
                    mappingResult.addMappedElements(subResult
                            .getMappedElements());
                }
                currentPosition = subResult.getLastFilePosition();
            } else {
                ContainerElementType type = containerElement.getType();
                IElementMapper mapper = mappers.get(type);
                if (mapper == null) {
                    throw new UnsupportedOperationException(
                            "ContainerElementType \'" + type
                                    + "\' is not supported yet!");
                }

                MappingResult subResult = mapper.map(mappingResult,
                        containerElement, currentPosition, filename);

                if (topContainer != null) {
                    List<IElementDeclaration> mappedElements = subResult
                            .getMappedElements();
                    for (IElementDeclaration sub : mappedElements) {
                        topContainer.addElementDeclarationInside(sub);
                        sub.setLevelUpElement(topContainer);
                    }
                } else {
                    mappingResult.addMappedElements(subResult
                            .getMappedElements());
                }

                currentPosition = subResult.getLastFilePosition();
            }
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

    private interface IElementMapper {

        MappingResult map(final MappingResult currentResult,
                final IContainerElement containerElement,
                final FilePosition fp, final String filename);
    }

    private class TextDeclarationMapper implements IElementMapper {

        @Override
        public MappingResult map(MappingResult currentResult,
                IContainerElement containerElement, FilePosition fp,
                String filename) {
            MappingResult mr = new MappingResult(fp, filename);

            return mr;
        }
    }
}
