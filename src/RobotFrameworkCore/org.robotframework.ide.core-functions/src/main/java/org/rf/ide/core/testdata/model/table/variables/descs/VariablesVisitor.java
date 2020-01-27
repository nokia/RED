/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs;

import java.util.function.Consumer;

/**
 * Interface allowing to visit variable usages
 * 
 * @author anglart
 */
@FunctionalInterface
public interface VariablesVisitor {

    /**
     * Creates a {@link VariablesVisitor} which will visit all possible variable usages and will
     * call given consumer for each of them.
     * 
     * @param varUsageConsumer
     *            Consumer to be guided through variable usages
     * @return A visitor guided through all variable usages
     */
    public static VariablesVisitor variableUsagesVisitor(final Consumer<VariableUse> varUsageConsumer) {
        return usage -> {
            varUsageConsumer.accept(usage);
            return true;
        };
    }

    /**
     * Visits given variable usage. This method should return true if the visitor wishes to continue
     * to next usage or false otherwise.
     * 
     * @param usage
     *            Variable usage to visit
     * @return True if should go to next usage or false otherwise
     */
    public boolean visit(final VariableUse usage);
}
