/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class RobotExecutableRow<T> extends AModelElement<T> {

    private RobotToken action;
    private final List<RobotToken> arguments = new LinkedList<>();


    public RobotExecutableRow() {
        this.action = new RobotToken();
    }


    public RobotToken getAction() {
        return action;
    }


    public void setAction(RobotToken action) {
        this.action = action;
    }


    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }


    public void addArgument(final RobotToken argument) {
        arguments.add(argument);
    }


    @Override
    public boolean isPresent() {
        return true;
    }


    @Override
    public ModelType getModelType() {
        ModelType type = ModelType.UNKNOWN;

        List<IRobotTokenType> types = getAction().getTypes();
        if (types.contains(RobotTokenType.TEST_CASE_ACTION_NAME)) {
            type = ModelType.TEST_CASE_EXECUTABLE_ROW;
        } else if (types.contains(RobotTokenType.KEYWORD_ACTION_NAME)) {
            type = ModelType.USER_KEYWORD_EXECUTABLE_ROW;
        }

        return type;
    }


    @Override
    public FilePosition getBeginPosition() {
        return getAction().getFilePosition();
    }


    @Override
    public List<RobotToken> getElementTokens() {
        List<RobotToken> tokens = new LinkedList<>();
        tokens.add(getAction());
        tokens.addAll(getArguments());

        return tokens;
    }


    public boolean isExecutable() {
        RobotToken action = getAction();
        return (action != null && !action.getTypes().contains(
                RobotTokenType.START_HASH_COMMENT))
                && isNotEmptyForContinoue();
    }


    private boolean isNotEmptyForContinoue() {
        return getElementTokens().size() > 1
                || !"\\".equals(action.getRaw().toString().trim());
    }


    public ExecutionLineDescriptor buildLineDescription() {
        ExecutionLineDescriptor execLine = new ExecutionLineDescriptor();
        if (isExecutable()) {
            boolean isAfterTheFirstAction = false;

            List<RobotToken> elementTokens = getElementTokens();
            for (RobotToken token : elementTokens) {
                if (isAfterTheFirstAction) {
                    execLine.addRestParameter(token);
                } else {
                    if (token.isVariableDeclaration()) {
                        execLine.addAssignment(token);
                    } else {
                        execLine.setTheFirstAction(token);
                        isAfterTheFirstAction = true;
                    }
                }
            }
        }

        return execLine;
    }

    public static class ExecutionLineDescriptor {

        private List<RobotToken> assignments = new LinkedList<>();
        private RobotToken theFirstAction = new RobotToken();
        private List<RobotToken> restParameters = new LinkedList<>();


        private void addAssignment(final RobotToken token) {
            this.assignments.add(token);
        }


        public List<RobotToken> getAssignments() {
            return assignments;
        }


        private void setTheFirstAction(final RobotToken token) {
            this.theFirstAction = token;
        }


        public RobotToken getFirstAction() {
            return theFirstAction;
        }


        private void addRestParameter(final RobotToken token) {
            this.restParameters.add(token);
        }


        public List<RobotToken> getParameters() {
            return restParameters;
        }


        @Override
        public String toString() {
            return String
                    .format("ExecutionLineDescriptor [assignments=%s, theFirstAction=%s, restParameters=%s]",
                            assignments, theFirstAction, restParameters);
        }
    }
}
