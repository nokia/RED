/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration.IVariableType;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;

public class MappingResult {

    private final List<BuildMessage> messages = new ArrayList<>();

    private final List<IElementDeclaration> mappedElements = new ArrayList<>();

    private final List<VariableDeclaration> correctVariables = new ArrayList<>();

    private final String fileName;

    private FilePosition fp;

    public MappingResult(final FilePosition fp, final String fileName) {
        this.fp = fp;
        this.fileName = fileName;
    }

    public FilePosition getLastFilePosition() {
        return fp;
    }

    public void setLastFilePosition(final FilePosition fp) {
        this.fp = fp;
    }

    public void addMappedElement(final IElementDeclaration mapped) {
        mappedElements.add(mapped);
    }

    public void removeMappedElement(final int indexToRemove) {
        mappedElements.remove(indexToRemove);
    }

    public void addMappedElements(final List<IElementDeclaration> mapped) {
        for (final IElementDeclaration elemDec : mapped) {
            addMappedElement(elemDec);
        }
    }

    public List<IElementDeclaration> getTextElements() {
        final List<IElementDeclaration> texts = new ArrayList<>();
        for (final IElementDeclaration e : mappedElements) {
            if (!e.isComplex()) {
                texts.add(e);
            }
        }

        return texts;
    }

    public List<IElementDeclaration> getMappedElements() {
        return Collections.unmodifiableList(mappedElements);
    }

    public boolean isOnlyPossibleCollectionVariable() {
        boolean result = false;
        if (mappedElements.size() == 1) {
            IElementDeclaration elemDec = mappedElements.get(0);
            if (elemDec instanceof VariableDeclaration) {
                VariableDeclaration varDec = (VariableDeclaration) elemDec;
                VariableType robotType = varDec.getRobotType();
                if (robotType == VariableType.DICTIONARY || robotType == VariableType.LIST
                        || robotType == VariableType.SCALAR || robotType == VariableType.SCALAR_AS_LIST) {
                    IVariableType extractorVariableType = varDec.getVariableType();
                    result = !isCollectionVariableElementGet()
                            && (extractorVariableType == VariableDeclaration.GeneralVariableType.NORMAL_TEXT
                                    || extractorVariableType == VariableDeclaration.GeneralVariableType.DYNAMIC);
                }
            }
        }

        return result;
    }

    public boolean isCollectionVariableElementGet() {
        boolean result = false;
        if (mappedElements.size() == 2) {
            final List<IElementDeclaration> elems = new ArrayList<>();
            elems.addAll(mappedElements.get(0).getElementsDeclarationInside());
            elems.addAll(mappedElements.get(1).getElementsDeclarationInside());

            if (mappedElements.get(0) instanceof VariableDeclaration
                    && mappedElements.get(1) instanceof IndexDeclaration) {
                VariableDeclaration varDec = (VariableDeclaration) mappedElements.get(0);
                if (varDec.getRobotType() != VariableType.ENVIRONMENT) {
                    int indexElementsNumber = 0;
                    for (final IElementDeclaration dec : elems) {
                        if (dec instanceof IndexDeclaration) {
                            indexElementsNumber = 1;
                            break;
                        }
                    }

                    if (indexElementsNumber == 0) {
                        result = true;
                    }
                }
            }
        } else if (mappedElements.size() == 1) {
            if (mappedElements.get(0) instanceof VariableDeclaration) {
                VariableDeclaration varDec = (VariableDeclaration) mappedElements.get(0);
                if (varDec.getRobotType() != VariableType.ENVIRONMENT) {
                    int indexElementsNumber = 0;
                    for (final IElementDeclaration dec : mappedElements.get(0).getElementsDeclarationInside()) {
                        if (dec instanceof IndexDeclaration) {
                            indexElementsNumber++;
                        }
                    }

                    if (indexElementsNumber == 1) {
                        result = true;
                    }
                }
            }
        }

        return result;
    }

    public List<VariableDeclaration> getCorrectVariables() {
        return Collections.unmodifiableList(correctVariables);
    }

    public void addCorrectVariable(final VariableDeclaration variable) {
        correctVariables.add(variable);
    }

    public void addCorrectVariables(final List<VariableDeclaration> variables) {
        for (final VariableDeclaration variable : variables) {
            addCorrectVariable(variable);
        }
    }

    public void addBuildMessage(final BuildMessage msg) {
        messages.add(msg);
    }

    public void addBuildMessages(final List<BuildMessage> msgs) {
        messages.addAll(msgs);
    }

    public List<BuildMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public String getFilename() {
        return fileName;
    }

    public void removeExactlyTheSameInstance(final IElementDeclaration elem) {
        for (int i = 0; i < mappedElements.size(); i++) {
            final IElementDeclaration d = mappedElements.get(i);
            if (d == elem) {
                mappedElements.remove(i);
                i--;
            }
        }
    }
}
