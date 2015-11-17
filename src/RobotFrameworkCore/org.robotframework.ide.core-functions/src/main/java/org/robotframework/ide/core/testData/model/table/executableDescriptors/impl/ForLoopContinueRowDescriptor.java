/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.executableDescriptors.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFileOutput.BuildMessage;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.IExecutableRowDescriptor;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.RobotAction;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.IElementDeclaration;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ast.mapping.VariableDeclaration;


public class ForLoopContinueRowDescriptor<T> implements
        IExecutableRowDescriptor<T> {

    private final List<VariableDeclaration> createdVariables = new LinkedList<>();
    private RobotAction continueAction;
    private RobotAction keywordAction;
    private final List<VariableDeclaration> usedVariables = new LinkedList<>();
    private final List<IElementDeclaration> textParameters = new LinkedList<>();
    private int forLoopStartRowIndex = -1;
    private IRowType type = ERowType.FOR_CONTINUE;
    private final List<BuildMessage> messages = new LinkedList<>();
    private final RobotExecutableRow<T> row;


    public ForLoopContinueRowDescriptor(final RobotExecutableRow<T> row) {
        this.row = row;
    }


    @Override
    public List<VariableDeclaration> getCreatedVariables() {
        return Collections.unmodifiableList(createdVariables);
    }


    public void addCreatedVariables(final List<VariableDeclaration> variables) {
        for (VariableDeclaration var : variables) {
            addCreatedVariable(var);
        }
    }


    public void addCreatedVariable(final VariableDeclaration variable) {
        this.createdVariables.add(variable);
    }


    @Override
    public RobotAction getAction() {
        return continueAction;
    }


    public int getForLoopStartRowIndex() {
        return forLoopStartRowIndex;
    }


    public void setForLoopStartRowIndex(final int forLoopStartRowIndex) {
        this.forLoopStartRowIndex = forLoopStartRowIndex;
    }


    public void setAction(final RobotAction continueAction) {
        this.continueAction = continueAction;
    }


    public RobotAction getKeywordAction() {
        return keywordAction;
    }


    public void setKeywordAction(final RobotAction keywordAction) {
        this.keywordAction = keywordAction;
    }


    @Override
    public List<VariableDeclaration> getUsedVariables() {
        return Collections.unmodifiableList(usedVariables);
    }


    public void addUsedVariables(final List<VariableDeclaration> variables) {
        for (VariableDeclaration var : variables) {
            addUsedVariable(var);
        }
    }


    public void addUsedVariable(final VariableDeclaration variable) {
        this.usedVariables.add(variable);
    }


    @Override
    public List<IElementDeclaration> getTextParameters() {
        return Collections.unmodifiableList(textParameters);
    }


    public void addTextParameters(final List<IElementDeclaration> textParams) {
        for (IElementDeclaration tP : textParams) {
            addTextParameter(tP);
        }
    }


    public void addTextParameter(final IElementDeclaration textParam) {
        this.textParameters.add(textParam);
    }


    @Override
    public List<BuildMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }


    public void addMessages(final List<BuildMessage> msgs) {
        for (BuildMessage bm : msgs) {
            addMessage(bm);
        }
    }


    public void addMessage(final BuildMessage bm) {
        messages.add(bm);
    }


    @Override
    public IRowType getRowType() {
        return type;
    }


    @Override
    public RobotExecutableRow<T> getRow() {
        return row;
    }
}
