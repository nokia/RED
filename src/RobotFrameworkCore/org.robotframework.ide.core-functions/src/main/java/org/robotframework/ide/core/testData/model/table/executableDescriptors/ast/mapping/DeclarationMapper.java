/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.Container;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.Container.ContainerType;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.ContainerElementType;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.IContainerElement;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.SimpleElementsMapper.IElementMapper;


public class DeclarationMapper {

    private String fileMapped;
    private final SimpleElementsMapper mapperFactory;


    public DeclarationMapper() {
        this.mapperFactory = new SimpleElementsMapper();
        this.fileMapped = "<NOT_SET>";

    }


    public MappingResult map(final FilePosition fp, final Container container,
            final String filename) {
        return map(new MappingResult(fp, filename), fp, container, filename);
    }


    private MappingResult map(final MappingResult topLevel,
            final FilePosition fp, final Container container,
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
                MappingResult subResult = map(mappingResult, currentPosition,
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

                List<IElementDeclaration> mappedElements = new LinkedList<>();
                if (topContainer != null) {
                    mappedElements = topContainer
                            .getElementsDeclarationInside();
                } else {
                    mappedElements = mappingResult.getMappedElements();
                }

                final IElementDeclaration variableIdentificator = getPossibleVariableIdentificator(mappedElements);
                if (variableIdentificator != null) {
                    System.out.println("==== VAR ====\n\n start-of-id\n"
                            + variableIdentificator + "\n end-of-id\n\n");
                    List<IElementDeclaration> escape = getEscape(mappedElements);
                    System.out.println("=== END ===");
                }
            } else {
                ContainerElementType type = containerElement.getType();
                IElementMapper mapper = mapperFactory.getMapperFor(type);
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
                    for (IElementDeclaration dec : mappedElements) {
                        topContainer.addElementDeclarationInside(dec);
                        dec.setLevelUpElement(topContainer);
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


    private List<IElementDeclaration> getEscape(
            final List<IElementDeclaration> mappedElements) {
        List<IElementDeclaration> varElements = new LinkedList<>();
        if (mappedElements != null) {
            for (IElementDeclaration dec : mappedElements) {
                System.out.println(dec);
            }
        }
        return varElements;
    }


    private IElementDeclaration getPossibleVariableIdentificator(
            final List<IElementDeclaration> mappedElements) {
        IElementDeclaration elem = null;
        if (mappedElements != null) {
            int numberOfMapped = mappedElements.size();
            if (numberOfMapped >= 2) {
                IElementDeclaration lastSubContainer = mappedElements
                        .get(numberOfMapped - 1);
                if (lastSubContainer instanceof VariableDeclaration) {
                    elem = mappedElements.get(numberOfMapped - 2);
                }
            }
        }

        return elem;
    }


    public String getFileMapped() {
        return fileMapped;
    }


    public void setFileMapped(final String fileMapped) {
        this.fileMapped = fileMapped;
    }

}
