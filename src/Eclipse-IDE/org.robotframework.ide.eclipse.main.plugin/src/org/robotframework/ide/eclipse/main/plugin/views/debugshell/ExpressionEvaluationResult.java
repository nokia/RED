/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.ui.services.IDisposable;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;


class ExpressionEvaluationResult implements IDisposable {

    private final Map<Integer, ExpressionEvaluationResultListener> listeners = new HashMap<>();

    private ExpressionEvaluationResultListener listener;

    void addListener(final int id, final ExpressionEvaluationResultListener listener) {
        listeners.put(id, listener);
    }

    void evaluationEnded(final int id, final ExpressionType expressionType, final Optional<String> result,
            final Optional<String> error) {

        // the listener has to be informed that the evaluator finished when it paused again or
        // terminated
        listener = listeners.remove(id);
        if (listener != null) {
            listener.handleResult(id, expressionType, result, error);
        }
    }

    void paused() {
        if (listener != null) {
            listener.evaluatorFinished();
        }
    }

    void terminated() {
        if (listener != null) {
            listener.evaluatorFinished();
        }
        listeners.forEach((id, listener) -> listener.evaluatorFinished());
        listeners.clear();
    }

    @Override
    public void dispose() {
        terminated();
    }

    static interface ExpressionEvaluationResultListener {

        void handleResult(int id, ExpressionType expressionType, Optional<String> result, Optional<String> error);

        void evaluatorFinished();
    }
}
