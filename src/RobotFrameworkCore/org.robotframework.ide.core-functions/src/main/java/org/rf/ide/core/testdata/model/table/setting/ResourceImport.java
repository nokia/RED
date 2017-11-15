/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class ResourceImport extends AImported {

    private static final long serialVersionUID = 1L;

    private final List<RobotToken> unexpectedTrashArguments = new ArrayList<>();

    public ResourceImport(final RobotToken resourceDeclaration) {
        super(Type.RESOURCE, resourceDeclaration);
    }

    @Override
    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(new ArrayList<RobotToken>());
    }

    public List<RobotToken> getUnexpectedTrashArguments() {
        return Collections.unmodifiableList(unexpectedTrashArguments);
    }

    public void setUnexpectedTrashArguments(final int index, final String trashArgument) {
        updateOrCreateTokenInside(unexpectedTrashArguments, index, trashArgument,
                RobotTokenType.SETTING_RESOURCE_UNWANTED_ARGUMENT);
    }

    public void setUnexpectedTrashArguments(final int index, final RobotToken trashArgument) {
        updateOrCreateTokenInside(unexpectedTrashArguments, index, trashArgument,
                RobotTokenType.SETTING_RESOURCE_UNWANTED_ARGUMENT);
    }

    public void addUnexpectedTrashArgument(final String trashArgument) {
        final RobotToken rt = new RobotToken();
        rt.setText(trashArgument);

        addUnexpectedTrashArgument(rt);
    }

    public void addUnexpectedTrashArgument(final RobotToken trashArgument) {
        this.unexpectedTrashArguments.add(trashArgument);
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
            final RobotToken pathOrName = getPathOrName();
            if (pathOrName != null) {
                tokens.add(pathOrName);
            }
            tokens.addAll(getUnexpectedTrashArguments());
            tokens.addAll(getComment());
        }

        return tokens;
    }

    @Override
    public boolean removeElementToken(final int index) {
        return super.removeElementFromList(unexpectedTrashArguments, index);
    }

    @Override
    public void insertValueAt(String value, int position) {
        final RobotToken tokenToInsert = new RobotToken();
        tokenToInsert.setText(value);
        if (position - 2 <= unexpectedTrashArguments.size()) { // new argument
            fixForTheType(tokenToInsert, RobotTokenType.SETTING_RESOURCE_UNWANTED_ARGUMENT, true);
            unexpectedTrashArguments.add(position - 2, tokenToInsert);
        } else if (position - 2 - unexpectedTrashArguments.size() <= getComment().size()) { // new comment part
            addCommentPartAt(position - 2 - unexpectedTrashArguments.size(), tokenToInsert);
        }
    }
}
