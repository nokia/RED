/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ListVariable extends AVariable {

    private final List<RobotToken> items = new ArrayList<>();

    public ListVariable(final String name, final RobotToken declaration, final VariableScope scope) {
        super(VariableType.LIST, name, declaration, scope);
    }

    public void addItem(final RobotToken item) {
        items.add(item);
    }

    public void addItem(final RobotToken item, final int position) {
        items.set(position, item);
    }

    public void removeItem(final RobotToken item) {
        items.remove(item);
    }

    public boolean moveLeftItem(final RobotToken item) {
        return getMoveHelper().moveLeft(items, item);
    }

    public boolean moveRightItem(final RobotToken item) {
        return getMoveHelper().moveRight(items, item);
    }

    public List<RobotToken> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public boolean isPresent() {
        return (getDeclaration() != null);
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            tokens.addAll(getItems());
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
