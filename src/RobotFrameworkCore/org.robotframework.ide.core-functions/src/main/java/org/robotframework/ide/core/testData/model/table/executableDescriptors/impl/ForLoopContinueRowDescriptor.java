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


    @Override
    public RobotAction getAction() {
        return continueAction;
    }


    public RobotAction getKeywordAction() {
        return keywordAction;
    }


    @Override
    public List<VariableDeclaration> getUsedVariables() {
        return Collections.unmodifiableList(usedVariables);
    }


    @Override
    public List<IElementDeclaration> getTextParameters() {
        return Collections.unmodifiableList(textParameters);
    }


    @Override
    public List<BuildMessage> getMessages() {
        return Collections.unmodifiableList(messages);
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
