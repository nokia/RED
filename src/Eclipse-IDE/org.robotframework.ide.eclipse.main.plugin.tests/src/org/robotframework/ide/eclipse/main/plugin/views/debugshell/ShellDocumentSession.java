/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import java.util.Optional;

import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;

class ShellDocumentSession {

    private final ShellDocument doc;

    public ShellDocumentSession() {
        this(null);
    }

    public ShellDocumentSession(final String delimiter) {
        this.doc = new ShellDocument(delimiter);
    }

    public ShellDocument get() {
        return doc;
    }

    public ShellDocumentSession type(final String stringToType) {
        doc.append(stringToType);
        return this;
    }

    public ShellDocumentSession changeMode(final ExpressionType mode) {
        doc.switchToMode(mode);
        return this;
    }

    public ShellDocumentSession continueExpr() {
        doc.continueExpressionInNewLine();
        return this;
    }

    public ShellDocumentSession execute(final String result) {
        final ExpressionType type = doc.getMode();
        doc.executeExpression(s -> 1);
        doc.putEvaluationResult(1, type, Optional.of(result), Optional.empty());
        return this;
    }
}