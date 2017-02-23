/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.model;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.execution.server.AgentClient;
import org.rf.ide.core.execution.server.response.ChangeVariable;
import org.rf.ide.core.execution.server.response.InterruptExecution;
import org.rf.ide.core.execution.server.response.ResumeExecution;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.RobotDebugVariablesManager;

class UserController {

    private final AgentClient client;

    private final RobotDebugVariablesManager variablesManager;

    UserController(final RobotDebugVariablesManager variablesManager, final AgentClient client) {
        this.variablesManager = variablesManager;
        this.client = client;
    }

    void stop() {
    }

    void resume() {
        try {
            client.send(new ResumeExecution());
        } catch (ResponseException | IOException e) {
            e.printStackTrace();
        }
    }

    void interrupt() {
        try {
            client.send(new InterruptExecution());
        } catch (ResponseException | IOException e) {
            e.printStackTrace();
        }
    }

    void changeVariable(final String expression, final String variableName, final RobotDebugVariable variable) {
        try {
            if (variable != null) {
                final List<String> childNameList = new ArrayList<>();
                final String root = variablesManager.extractVariableRootAndChilds(variable, childNameList,
                        variableName);

                final List<String> arguments = newArrayList();
                arguments.addAll(childNameList);
                arguments.add(expression);

                client.send(new ChangeVariable(root, arguments));
            } else {
                client.send(new ChangeVariable(variableName, newArrayList(expression)));
            }
        } catch (ResponseException | IOException e) {
            e.printStackTrace();
        }
    }
}
