/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs;

import java.util.function.Consumer;

/**
 * Interface allowing to visit python expressions usages (of form ${{expr}, @{{expr}} etc. -
 * introduced in RF 3.2)
 * 
 * @author anglart
 */
@FunctionalInterface
public interface PythonExpressionVisitor {

    /**
     * Creates a {@link PythonExpressionVisitor} which will visit all possible python expressions
     * usages and will call given consumer for each of them.
     * 
     * @param pythonExprConsumer
     *            Consumer to be guided through python expressions usages
     * @return A visitor guided through all python expressions usages
     */
    public static PythonExpressionVisitor pythonExpressionVisitor(
            final Consumer<PythonExpression> pythonExprConsumer) {
        return expr -> {
            pythonExprConsumer.accept(expr);
            return true;
        };
    }

    /**
     * Visits given python expression. This method should return true if the visitor wishes to
     * continue to next expression or false otherwise.
     * 
     * @param expression
     *            Visited python expression
     * @return True if should go to next expression or false otherwise
     */
    public boolean visit(final PythonExpression expression);
}
