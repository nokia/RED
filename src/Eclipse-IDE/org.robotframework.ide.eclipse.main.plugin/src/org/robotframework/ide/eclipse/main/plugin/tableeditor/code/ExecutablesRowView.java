/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRowView;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

public class ExecutablesRowView {

    public static List<String> rowData(final RobotKeywordCall element) {
        return rowTokens(element, RobotToken::getText);
    }

    public static List<RobotToken> rowTokens(final RobotKeywordCall element) {
        return rowTokens(element, t -> t);
    }

    public static <R> List<R> rowTokens(final RobotKeywordCall element, final Function<RobotToken, R> fun) {
        if (element.isExecutable()) {
            final AModelElement<?> modelElement = element.getLinkedElement();
            @SuppressWarnings("unchecked")
            final RobotExecutableRowView view = RobotExecutableRowView
                    .buildView((RobotExecutableRow<? extends IExecutableStepsHolder<?>>) modelElement);

            return modelElement.getElementTokens()
                    .stream()
                    .map(RobotKeywordCall.tokenViaExecutableViewUpdateToken(view))
                    .map(fun)
                    .collect(toList());

        } else {
            final List<RobotToken> tokens = new ArrayList<>();
            tokens.addAll(element.getLinkedElement().getElementTokens());
            if (isArtificialActionBeforeComment(tokens)) {
                tokens.remove(0);
            }
            return tokens.stream().map(fun).collect(toList());
        }
    }

    private static boolean isArtificialActionBeforeComment(final List<RobotToken> tokens) {
        if (tokens.size() >= 2) {
            final RobotToken actionToken = tokens.get(0);
            if (actionToken.getFilePosition().isNotSet() && actionToken.getText().isEmpty() && !actionToken.isDirty()) {
                final List<IRobotTokenType> types = tokens.get(1).getTypes();
                return types.contains(RobotTokenType.START_HASH_COMMENT)
                        || types.contains(RobotTokenType.COMMENT_CONTINUE);
            }
        }
        return false;
    }
}
