/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

class MappingResult {

    private final List<BuildMessage> messages = new ArrayList<>();

    private final List<IElementDeclaration> mappedElements = new ArrayList<>();

    private final List<VariableDeclaration> correctVariables = new ArrayList<>();

    private FilePosition fp;

    MappingResult(final FilePosition fp) {
        this.fp = fp;
    }

    FilePosition getLastFilePosition() {
        return fp;
    }

    void setLastFilePosition(final FilePosition fp) {
        this.fp = fp;
    }

    void addMappedElement(final IElementDeclaration mapped) {
        mappedElements.add(mapped);
    }

    void addMappedElements(final List<IElementDeclaration> mapped) {
        mappedElements.addAll(mapped);
    }

    List<IElementDeclaration> getMappedElements() {
        return mappedElements;
    }

    List<VariableDeclaration> getCorrectVariables() {
        return correctVariables;
    }

    Stream<RobotToken> getVariables() {
        return correctVariables.stream().map(VariableDeclaration::asToken);
    }

    void addCorrectVariable(final VariableDeclaration variable) {
        correctVariables.add(variable);
    }

    void addCorrectVariables(final List<VariableDeclaration> variables) {
        correctVariables.addAll(variables);
    }

    void addBuildMessage(final BuildMessage msg) {
        messages.add(msg);
    }

    void addBuildMessages(final List<BuildMessage> msgs) {
        messages.addAll(msgs);
    }

    List<BuildMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    void removeExactlyTheSameInstance(final IElementDeclaration elem) {
        int i = 0;
        while (i < mappedElements.size()) {
            if (mappedElements.get(i) == elem) {
                mappedElements.remove(i);
            } else {
                i++;
            }
        }
    }

    boolean isCollectionVariableElementGet() {
        if (mappedElements.size() == 2 && mappedElements.get(0) instanceof VariableDeclaration
                && mappedElements.get(1) instanceof IndexDeclaration) {
            final VariableDeclaration varDec = (VariableDeclaration) mappedElements.get(0);
            if (varDec.getType() != VariableType.ENVIRONMENT) {
                return mappedElements.stream()
                        .map(IElementDeclaration::getElementsDeclarationInside)
                        .flatMap(List<IElementDeclaration>::stream)
                        .noneMatch(declaration -> declaration instanceof IndexDeclaration);
            }
        } else if (mappedElements.size() == 1 && mappedElements.get(0) instanceof VariableDeclaration) {
            final VariableDeclaration varDec = (VariableDeclaration) mappedElements.get(0);
            if (varDec.getType() != VariableType.ENVIRONMENT) {
                final long indexDeclarationsCount = varDec.getElementsDeclarationInside()
                        .stream()
                        .filter(declaration -> declaration instanceof IndexDeclaration)
                        .count();
                return indexDeclarationsCount == 1;
            }
        }
        return false;
    }

    boolean isPlainVariable() {
        return correctVariables.size() == 1 && mappedElements.size() == 1;
    }

    boolean isPlainVariableAssign() {
        // assign is a token consist of only a variable or a variable followed by = character
        // no nested variables allowed
        if (isPlainVariable()) {
            return true;
        } else if (correctVariables.size() == 1 && mappedElements.size() == 2) {
            final IElementDeclaration lastElement = mappedElements.get(1);
            return lastElement instanceof JoinedTextDeclarations
                    && "=".equals(((JoinedTextDeclarations) lastElement).getText().trim());
        }
        return false;
    }
}
