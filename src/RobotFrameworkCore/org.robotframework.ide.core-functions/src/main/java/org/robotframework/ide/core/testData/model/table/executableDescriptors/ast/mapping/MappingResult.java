/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput.BuildMessage;


public class MappingResult {

    private final List<BuildMessage> messages = new LinkedList<>();
    private final List<IElementDeclaration> mappedElements = new LinkedList<>();
    private final List<VariableDeclaration> correctVariables = new LinkedList<>();
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
        for (IElementDeclaration elemDec : mapped) {
            addMappedElement(elemDec);
        }
    }


    public void updateMappedElements(final List<IElementDeclaration> mapped) {
        mappedElements.clear();
        addMappedElements(mappedElements);
    }


    public List<IElementDeclaration> getMappedElements() {
        return Collections.unmodifiableList(mappedElements);
    }


    public List<VariableDeclaration> getCorrectVariables() {
        return Collections.unmodifiableList(correctVariables);
    }


    public void addCorrectVariable(final VariableDeclaration variable) {
        correctVariables.add(variable);
    }


    public void addCorrectVariables(final List<VariableDeclaration> variables) {
        for (VariableDeclaration variable : variables) {
            addCorrectVariable(variable);
        }
    }


    public void updateCorrectVariables(final List<VariableDeclaration> variables) {
        correctVariables.clear();
        addCorrectVariables(correctVariables);
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
            IElementDeclaration d = mappedElements.get(i);
            if (d == elem) {
                mappedElements.remove(i);
                i--;
            }
        }
    }
}