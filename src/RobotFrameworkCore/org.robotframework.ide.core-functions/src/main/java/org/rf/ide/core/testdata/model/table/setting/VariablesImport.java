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

public class VariablesImport extends AImported {

    private static final long serialVersionUID = 1L;

    private final List<RobotToken> arguments = new ArrayList<>();

    public VariablesImport(final RobotToken variablesDeclaration) {
        super(Type.VARIABLES, variablesDeclaration);
    }

    @Override
    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public void setArguments(final int index, final String argument) {
        updateOrCreateTokenInside(arguments, index, argument, RobotTokenType.SETTING_VARIABLES_ARGUMENT);
    }

    public void setArguments(final int index, final RobotToken argument) {
        updateOrCreateTokenInside(arguments, index, argument, RobotTokenType.SETTING_VARIABLES_ARGUMENT);
    }

    public void addArgument(final String argument) {
        final RobotToken rt = new RobotToken();
        rt.setText(argument);

        addArgument(rt);
    }

    public void addArgument(final RobotToken argument) {
        this.arguments.add(argument);
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
            tokens.addAll(getArguments());
            tokens.addAll(getComment());
        }

        return tokens;
    }

    @Override
    public boolean removeElementToken(final int index) {
        return super.removeElementFromList(arguments, index);
    }

    @Override
    public void insertValueAt(String value, int position) {
        final RobotToken tokenToInsert = new RobotToken();
        tokenToInsert.setText(value);
        if (position - 2 <= arguments.size()) { // new argument
            fixForTheType(tokenToInsert, RobotTokenType.SETTING_VARIABLES_ARGUMENT, true);
            arguments.add(position - 2, tokenToInsert);
        } else if (position - 2 - arguments.size() <= getComment().size()) { // new comment part
            addCommentPartAt(position - 2 - arguments.size(), tokenToInsert);
        }
    }
}
