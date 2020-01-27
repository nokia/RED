/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs;

import org.rf.ide.core.testdata.model.FileRegion;

/**
 * Interface allowing to visit both variable usages and other textual parts of given token
 * 
 * @author anglart
 */
public abstract class ExpressionVisitor implements VariablesVisitor {

    @Override
    public boolean visit(final VariableUse usage) {
        return true;
    }

    /**
     * Visits given textual part of token (a part outside of any variable)
     * 
     * @param text
     *            Textual part of token
     * @param region
     *            Region in file of visted part
     * @return True if should go to next usage or false otherwise
     */
    public boolean visit(final String text, final FileRegion region) {
        return true;
    }
}