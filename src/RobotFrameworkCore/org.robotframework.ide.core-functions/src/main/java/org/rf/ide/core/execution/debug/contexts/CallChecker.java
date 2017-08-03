/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug.contexts;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

class CallChecker {

    static boolean isCallOf(final String callUnderConsideration, final RunningKeyword keyword) {
        final String libraryName = keyword.getSourceName();
        final String keywordName = keyword.getName();
        return QualifiedKeywordName.fromOccurrence(callUnderConsideration)
                .matchesIgnoringCase(QualifiedKeywordName.create(keywordName, libraryName))
                || (EmbeddedKeywordNamesSupport.hasEmbeddedArguments(callUnderConsideration)
                        && EmbeddedKeywordNamesSupport.matchesIgnoreCase(callUnderConsideration, keywordName));
    }

    static boolean isSameForLoop(final ForLoopDeclarationRowDescriptor<?> descriptor,
            final RunningKeyword keyword) {
        return keyword.getName().equals(createName(descriptor));
    }

    static String createName(final ForLoopDeclarationRowDescriptor<?> descriptor) {
        final List<String> variables = descriptor.getCreatedVariables()
                .stream()
                .map(VariableDeclaration::asToken)
                .map(RobotToken::getText)
                .collect(toList());

        final String in = descriptor.getInAction().getToken().getText().toUpperCase();
        final List<RobotToken> args = descriptor.getRow().getArguments();
        final String source = args.stream()
                .skip(variables.size() + 1)
                .map(RobotToken::getText)
                .collect(joining(" | ", "[ ", " ]"));
        return variables.stream().collect(joining(" | ")) + " " + in + " " + source;
    }
}
