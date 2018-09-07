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
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration.VariableDeclarationType;
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
        mappedElements.addAll(mapped);
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
        if (mappedElements.size() == 1 && mappedElements.get(0) instanceof VariableDeclaration) {
            final VariableDeclaration varDec = (VariableDeclaration) mappedElements.get(0);
            final VariableType robotType = varDec.getRobotType();
            if (robotType == VariableType.DICTIONARY || robotType == VariableType.LIST
                    || robotType == VariableType.SCALAR || robotType == VariableType.SCALAR_AS_LIST) {
                final VariableDeclarationType extractorVariableType = varDec.getVariableType();
                return !isCollectionVariableElementGet()
                        && (extractorVariableType == VariableDeclarationType.NORMAL_TEXT
                                || extractorVariableType == VariableDeclarationType.DYNAMIC);
            }
        }

        return false;
    }

    public boolean isCollectionVariableElementGet() {
        if (mappedElements.size() == 2 && mappedElements.get(0) instanceof VariableDeclaration
                && mappedElements.get(1) instanceof IndexDeclaration) {
            final VariableDeclaration varDec = (VariableDeclaration) mappedElements.get(0);
            if (varDec.getRobotType() != VariableType.ENVIRONMENT) {
                return mappedElements.stream()
                        .map(IElementDeclaration::getElementsDeclarationInside)
                        .flatMap(List<IElementDeclaration>::stream)
                        .noneMatch(declaration -> declaration instanceof IndexDeclaration);
            }
        } else if (mappedElements.size() == 1 && mappedElements.get(0) instanceof VariableDeclaration) {
            final VariableDeclaration varDec = (VariableDeclaration) mappedElements.get(0);
            if (varDec.getRobotType() != VariableType.ENVIRONMENT) {
                final long indexDeclarationsCount = varDec.getElementsDeclarationInside()
                        .stream()
                        .filter(declaration -> declaration instanceof IndexDeclaration)
                        .count();
                return indexDeclarationsCount == 1;
            }
        }

        return false;
    }

    public List<VariableDeclaration> getCorrectVariables() {
        return Collections.unmodifiableList(correctVariables);
    }

    public void addCorrectVariable(final VariableDeclaration variable) {
        correctVariables.add(variable);
    }

    public void addCorrectVariables(final List<VariableDeclaration> variables) {
        correctVariables.addAll(variables);
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
