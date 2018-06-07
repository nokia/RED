/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.exec.descs.TextPosition;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.Container;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.Container.ContainerType;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.ContainerElementType;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.IContainerElement;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.SimpleElementsMapper.IElementMapper;

@SuppressWarnings("PMD.GodClass")
public class DeclarationMapper {

    private String fileMapped;

    private final SimpleElementsMapper mapperFactory;

    public DeclarationMapper() {
        this.mapperFactory = new SimpleElementsMapper();
        this.fileMapped = "<NOT_SET>";
    }

    public MappingResult map(final FilePosition fp, final Container container, final String filename) {
        return map(new MappingResult(fp, filename), fp, container, filename);
    }

    private MappingResult map(final MappingResult topLevel, final FilePosition fp, final Container container,
            final String filename) {
        final MappingResult mappingResult = new MappingResult(fp, filename);
        FilePosition currentPosition = fp;

        if (container.getContainerType() == ContainerType.MIX) {
            if (container.getParent() != null) {
                throw new IllegalStateException("Mix container is only supported on the top level extraction.");
            }
        }

        final ContainerMappingHelper mappingHelper = ContainerMappingHelper.createDeclaration(container,
                currentPosition, mappingResult);
        final IElementDeclaration topContainer = mappingHelper.getContainerDeclarationHolder();
        if (topContainer != null) {
            mappingResult.addMappedElement(topContainer);
        }
        final List<IContainerElement> elements = container.getElements();
        final int contentEnd = mappingHelper.getContentEnd();
        for (int index = mappingHelper.getContentStart(); index < contentEnd; index++) {
            final IContainerElement containerElement = elements.get(index);
            if (containerElement.isComplex()) {
                final Container subContainer = (Container) containerElement;
                final FilePosition previousForContainer = currentPosition;
                final MappingResult subResult = map(mappingResult, currentPosition, subContainer, filename);
                mappingResult.addCorrectVariables(subResult.getCorrectVariables());
                mappingResult.addBuildMessages(subResult.getMessages());
                if (topContainer != null) {
                    final List<IElementDeclaration> mappedElements = subResult.getMappedElements();
                    for (final IElementDeclaration dec : mappedElements) {
                        topContainer.addElementDeclarationInside(dec);
                        dec.setLevelUpElement(topContainer);
                    }
                } else {
                    mappingResult.addMappedElements(subResult.getMappedElements());
                }

                List<IElementDeclaration> mappedElements;
                if (topContainer != null) {
                    mappedElements = topContainer.getElementsDeclarationInside();
                } else {
                    mappedElements = mappingResult.getMappedElements();
                }

                final IElementDeclaration lastComplex = mappedElements.get(mappedElements.size() - 1);
                final IElementDeclaration variableIdentifier = getPossibleVariableIdentifier(mappedElements);
                if (lastComplex instanceof VariableDeclaration) {
                    final VariableDeclaration variableDec = (VariableDeclaration) lastComplex;
                    final List<IElementDeclaration> escape = getEscape(mappedElements);
                    if (!escape.isEmpty()) {
                        if (topContainer != null) {
                            for (final IElementDeclaration d : escape) {
                                topContainer.removeExactlyTheSameInstance(d);
                            }
                        } else {
                            if (variableIdentifier != null) {
                                for (final IElementDeclaration d : escape) {
                                    mappingResult.removeExactlyTheSameInstance(d);
                                }
                            }
                        }

                        variableDec.setEscape(escape.get(0).getStart());
                    }

                    if (variableIdentifier != null) {
                        if (topContainer != null) {
                            topContainer.removeExactlyTheSameInstance(variableIdentifier);
                        } else {
                            mappingResult.removeExactlyTheSameInstance(variableIdentifier);
                        }
                        variableDec.setTypeIdentificator(new TextPosition(
                                variableIdentifier.getStart().getFullText(), variableIdentifier.getStart().getStart(),
                                variableIdentifier.getEnd().getEnd()));
                    }

                    if (seemsToBeCorrectRobotVariable(previousForContainer, mappingResult, variableDec)) {
                        mappingResult.addCorrectVariable(variableDec);
                    } else {
                        convertIncorrectVariableBackToText(mappingResult, topContainer, variableDec);
                    }
                } else {
                    final IndexDeclaration indexDec = (IndexDeclaration) lastComplex;
                    if (!seemsToBeCorrectRobotVariableIndex(mappedElements, indexDec,
                            !subContainer.isOpenForModification())) {
                        convertIncorrectIndexElementBackToText(mappingResult, topContainer, indexDec,
                                !subContainer.isOpenForModification());
                    }
                }
            } else {
                final ContainerElementType type = containerElement.getType();
                final IElementMapper mapper = mapperFactory.getMapperFor(type);
                if (mapper == null) {
                    throw new UnsupportedOperationException(
                            "ContainerElementType \'" + type + "\' is not supported yet!");
                }

                final MappingResult subResult = mapper.map(mappingResult, containerElement, currentPosition, filename);
                if (topContainer != null) {
                    final List<IElementDeclaration> mappedElements = subResult.getMappedElements();
                    for (final IElementDeclaration dec : mappedElements) {
                        topContainer.addElementDeclarationInside(dec);
                        dec.setLevelUpElement(topContainer);
                    }
                } else {
                    mappingResult.addMappedElements(subResult.getMappedElements());
                }
                currentPosition = subResult.getLastFilePosition();
            }
        }
        mappingResult.setLastFilePosition(currentPosition);

        return mappingResult;
    }

    private void convertIncorrectIndexElementBackToText(final MappingResult mappingResult,
            final IElementDeclaration topContainer, final IndexDeclaration indexDec, final boolean isClosed) {
        final TextDeclaration textDec = new TextDeclaration(indexDec.getStart(),
                ContainerElementType.SQUARE_BRACKET_OPEN);
        final JoinedTextDeclarations joinedStart = new JoinedTextDeclarations();
        joinedStart.addElementDeclarationInside(textDec);

        if (topContainer != null) {
            topContainer.removeExactlyTheSameInstance(indexDec);
            topContainer.addElementDeclarationInside(joinedStart);
        } else {
            mappingResult.removeExactlyTheSameInstance(indexDec);
            mappingResult.addMappedElement(joinedStart);
        }
        joinedStart.setLevelUpElement(topContainer);

        final List<IElementDeclaration> elementsDeclarationInside = indexDec.getElementsDeclarationInside();
        for (final IElementDeclaration dec : elementsDeclarationInside) {
            if (dec.isComplex()) {
                if (dec.getEnd() != null) {
                    if (topContainer != null) {
                        topContainer.addElementDeclarationInside(dec);
                    } else {
                        mappingResult.addMappedElement(dec);
                    }
                    dec.setLevelUpElement(topContainer);
                }
            } else {
                if (topContainer != null) {
                    topContainer.addElementDeclarationInside(dec);
                } else {
                    mappingResult.addMappedElement(dec);
                }

                dec.setLevelUpElement(topContainer);
            }
        }
        if (isClosed) {
            final TextDeclaration textDecClose = new TextDeclaration(indexDec.getEnd(),
                    ContainerElementType.SQUARE_BRACKET_CLOSE);
            final JoinedTextDeclarations joinedEnd = new JoinedTextDeclarations();
            joinedEnd.addElementDeclarationInside(textDecClose);

            if (topContainer != null) {
                topContainer.addElementDeclarationInside(joinedEnd);
            } else {
                mappingResult.addMappedElement(joinedEnd);
            }
            joinedEnd.setLevelUpElement(topContainer);
        }
    }

    private void convertIncorrectVariableBackToText(final MappingResult mappingResult,
            final IElementDeclaration topContainer, final VariableDeclaration variableDec) {
        final JoinedTextDeclarations joinedStart = new JoinedTextDeclarations();
        if (variableDec.isEscaped()) {
            final TextDeclaration escapeDec = new TextDeclaration(variableDec.getEscape(), ContainerElementType.ESCAPE);
            joinedStart.addElementDeclarationInside(escapeDec);
        }
        if (variableDec.getTypeIdentificator() != null) {
            final TextDeclaration typeId = new TextDeclaration(variableDec.getTypeIdentificator(),
                    ContainerElementType.VARIABLE_TYPE_ID);
            joinedStart.addElementDeclarationInside(typeId);
        }
        final TextDeclaration variableCurlyBracket = new TextDeclaration(variableDec.getStart(),
                ContainerElementType.CURLY_BRACKET_OPEN);
        joinedStart.addElementDeclarationInside(variableCurlyBracket);

        if (topContainer != null) {
            topContainer.removeExactlyTheSameInstance(variableDec);
            topContainer.addElementDeclarationInside(joinedStart);
        } else {
            mappingResult.removeExactlyTheSameInstance(variableDec);
            mappingResult.addMappedElement(joinedStart);
        }
        joinedStart.setLevelUpElement(topContainer);

        final List<IElementDeclaration> elementsDeclarationInside = variableDec.getElementsDeclarationInside();
        for (final IElementDeclaration dec : elementsDeclarationInside) {
            if (dec.isComplex()) {
                if (dec.getEnd() != null) {
                    if (topContainer != null) {
                        topContainer.addElementDeclarationInside(dec);
                    } else {
                        mappingResult.addMappedElement(dec);
                    }

                    dec.setLevelUpElement(topContainer);
                }
            } else {
                if (topContainer != null) {
                    topContainer.addElementDeclarationInside(dec);
                } else {
                    mappingResult.addMappedElement(dec);
                }

                dec.setLevelUpElement(topContainer);
            }
        }

        final TextPosition end = variableDec.getEnd();
        if (end != null) {
            final JoinedTextDeclarations joinedEnd = new JoinedTextDeclarations();
            joinedEnd.addElementDeclarationInside(new TextDeclaration(end, ContainerElementType.CURLY_BRACKET_CLOSE));
            if (topContainer != null) {
                topContainer.addElementDeclarationInside(joinedEnd);
            } else {
                mappingResult.addMappedElement(joinedEnd);
            }

            joinedEnd.setLevelUpElement(topContainer);
        }
    }

    private boolean seemsToBeCorrectRobotVariable(final FilePosition currentPosition, final MappingResult mappingResult,
            final VariableDeclaration variableDec) {
        boolean result = false;
        if (!variableDec.isEscaped()) {
            final TextPosition typeId = variableDec.getTypeIdentificator();
            if (typeId != null) {
                final String idText = typeId.getText();
                if (idText != null && !idText.isEmpty()) {
                    if (idText.length() == 1) {
                        if (variableDec.getEnd() != null) {
                            result = true;
                        }
                    } else {
                        final BuildMessage warnMessage = BuildMessage.createWarnMessage(
                                "Incorrect variable id with space between " + idText.charAt(0) + " and '{'.",
                                getFileMapped());
                        warnMessage.setFileRegion(new FileRegion(
                                new FilePosition(currentPosition.getLine(), currentPosition.getColumn(),
                                        currentPosition.getOffset()),
                                new FilePosition(currentPosition.getLine(),
                                        currentPosition.getColumn() + variableDec.getEnd().getEnd(),
                                        currentPosition.getOffset() + variableDec.getEnd().getEnd())));
                        mappingResult.addBuildMessage(warnMessage);
                    }
                }
            }
        }

        return result;
    }

    private boolean seemsToBeCorrectRobotVariableIndex(List<IElementDeclaration> mappedElements,
            IndexDeclaration indexDec, final boolean isClosed) {
        if (!isClosed) {
            return false;
        }
        final int possibleIndexId = mappedElements.indexOf(indexDec);
        return (possibleIndexId - 1 >= 0 && mappedElements.get(possibleIndexId - 1) instanceof VariableDeclaration)
                || mappedElements.get(possibleIndexId).getLevelUpElement() instanceof VariableDeclaration;
    }

    private List<IElementDeclaration> getEscape(final List<IElementDeclaration> mappedElements) {
        final List<IElementDeclaration> varElements = new ArrayList<>();

        if (mappedElements != null) {
            final int nrOfMapped = mappedElements.size();
            if (nrOfMapped >= 3) {
                final IElementDeclaration possibleEscape = mappedElements.get(nrOfMapped - 3);
                if (possibleEscape instanceof JoinedTextDeclarations) {
                    final JoinedTextDeclarations joined = (JoinedTextDeclarations) possibleEscape;
                    if (mapperFactory.containsOnly(joined, Arrays.asList(ContainerElementType.ESCAPE))) {
                        final List<IElementDeclaration> elementsInside = joined.getElementsDeclarationInside();
                        if (elementsInside.size() == 1) {
                            final TextDeclaration dec = (TextDeclaration) elementsInside.get(0);
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

    private IElementDeclaration getPossibleVariableIdentifier(final List<IElementDeclaration> mappedElements) {
        IElementDeclaration elem = null;
        if (mappedElements != null) {
            final int numberOfMapped = mappedElements.size();
            if (numberOfMapped >= 2) {
                final IElementDeclaration lastSubContainer = mappedElements.get(numberOfMapped - 1);
                if (lastSubContainer instanceof VariableDeclaration) {
                    final IElementDeclaration previous = mappedElements.get(numberOfMapped - 2);
                    final JoinedTextDeclarations text = new JoinedTextDeclarations();
                    text.addElementDeclarationInside(previous);
                    final String idText = text.getText();
                    if (idText != null) {
                        final String trimmed = idText.trim();
                        if (trimmed.length() >= 1) {
                            if (ContainerElementType.VARIABLE_TYPE_ID.getRepresentation().contains(trimmed.charAt(0))) {
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
