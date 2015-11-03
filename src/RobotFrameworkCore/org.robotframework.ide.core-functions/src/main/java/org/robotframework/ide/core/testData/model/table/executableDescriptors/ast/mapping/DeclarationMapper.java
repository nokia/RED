/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.TextPosition;
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
        int contentEnd = mappingHelper.getContentEnd();
        for (int index = mappingHelper.getContentStart(); index < contentEnd; index++) {
            IContainerElement containerElement = elements.get(index);
            if (containerElement.isComplex()) {
                Container subContainer = (Container) containerElement;
                MappingResult subResult = map(mappingResult, currentPosition,
                        subContainer, filename);
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

                List<IElementDeclaration> mappedElements;
                if (topContainer != null) {
                    mappedElements = topContainer
                            .getElementsDeclarationInside();
                } else {
                    mappedElements = mappingResult.getMappedElements();
                }

                IElementDeclaration lastComplex = mappedElements
                        .get(mappedElements.size() - 1);
                final IElementDeclaration variableIdentificator = getPossibleVariableIdentificator(mappedElements);
                if (lastComplex instanceof VariableDeclaration) {
                    VariableDeclaration variableDec = (VariableDeclaration) lastComplex;
                    List<IElementDeclaration> escape = getEscape(mappedElements);
                    if (!escape.isEmpty()) {
                        if (topContainer != null) {
                            for (IElementDeclaration d : escape) {
                                topContainer.removeExactlyTheSameInstance(d);
                            }
                        } else {
                            for (IElementDeclaration d : escape) {
                                mappingResult.removeExactlyTheSameInstance(d);
                            }
                        }

                        variableDec.setEscape(escape.get(0).getStart());
                    }

                    if (variableIdentificator != null) {
                        if (topContainer != null) {
                            topContainer
                                    .removeExactlyTheSameInstance(variableIdentificator);
                        } else {
                            mappingResult
                                    .removeExactlyTheSameInstance(variableIdentificator);
                        }
                        variableDec.setTypeIdentificator(new TextPosition(
                                variableIdentificator.getStart().getFullText(),
                                variableIdentificator.getStart().getStart(),
                                variableIdentificator.getEnd().getEnd()));
                    }

                    if (subContainer.isOpenForModification()) {
                        variableDec.getEscape();
                    } else {
                        mappingResult.addCorrectVariable(variableDec);
                    }
                } else {
                    IndexDeclaration indexDec = (IndexDeclaration) lastComplex;
                    if (subContainer.isOpenForModification()) {
                        TextDeclaration textDec = new TextDeclaration(
                                indexDec.getStart(),
                                ContainerElementType.SQUARE_BRACKET_OPEN);
                        JoinedTextDeclarations joinedStart = new JoinedTextDeclarations();
                        joinedStart.addElementDeclarationInside(textDec);

                        if (topContainer != null) {
                            topContainer.removeExactlyTheSameInstance(indexDec);
                            topContainer
                                    .addElementDeclarationInside(joinedStart);
                        } else {
                            mappingResult
                                    .removeExactlyTheSameInstance(indexDec);
                            mappingResult.addMappedElement(joinedStart);
                        }
                        joinedStart.setLevelUpElement(topContainer);

                        List<IElementDeclaration> elementsDeclarationInside = indexDec
                                .getElementsDeclarationInside();
                        for (IElementDeclaration dec : elementsDeclarationInside) {
                            if (dec.isComplex()) {
                                if (dec.getEnd() != null) {
                                    if (topContainer != null) {
                                        topContainer
                                                .addElementDeclarationInside(dec);
                                    } else {
                                        mappingResult.addMappedElement(dec);
                                    }

                                    dec.setLevelUpElement(topContainer);
                                }
                            } else {
                                if (topContainer != null) {
                                    topContainer
                                            .addElementDeclarationInside(dec);
                                } else {
                                    mappingResult.addMappedElement(dec);
                                }

                                dec.setLevelUpElement(topContainer);
                            }
                        }
                    }
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
        final List<IElementDeclaration> varElements = new LinkedList<>();

        if (mappedElements != null) {
            int nrOfMapped = mappedElements.size();
            if (nrOfMapped >= 3) {
                final IElementDeclaration possibleEscape = mappedElements
                        .get(nrOfMapped - 3);
                if (possibleEscape instanceof JoinedTextDeclarations) {
                    final JoinedTextDeclarations joined = (JoinedTextDeclarations) possibleEscape;
                    if (mapperFactory.containsOnly(joined,
                            Arrays.asList(ContainerElementType.ESCAPE))) {
                        List<IElementDeclaration> elementsInside = joined
                                .getElementsDeclarationInside();
                        if (elementsInside.size() == 1) {
                            TextDeclaration dec = (TextDeclaration) elementsInside
                                    .get(0);
                            if (dec.getLength() == 1) {
                                varElements.add(possibleEscape);
                            }
                        }
                    }
                }
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
                    IElementDeclaration previous = mappedElements
                            .get(numberOfMapped - 2);
                    JoinedTextDeclarations text = new JoinedTextDeclarations();
                    text.addElementDeclarationInside(previous);
                    String idText = text.getText();
                    if (idText != null) {
                        String trimmed = idText.trim();
                        if (trimmed.length() == 1) {
                            if (ContainerElementType.VARIABLE_TYPE_ID
                                    .getRepresentation().contains(
                                            trimmed.charAt(0))) {
                                elem = previous;
                            }
                        }
                    }
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
