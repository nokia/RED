/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.collect.Range;

class CommentedVariablesFilter {

    boolean isInCommentedPart(final RobotFileOutput rfo, final RobotToken token) {
        final int offset = token.getStartOffset();
        final Optional<RobotLine> robotLine = rfo.getFileModel().getRobotLineBy(offset);

        if (robotLine.isPresent()) {
            final RobotLine line = robotLine.get();

            for (final IRobotLineElement lineElem : line.getLineElements()) {
                final List<IRobotTokenType> types = lineElem.getTypes();
                if (types.contains(RobotTokenType.START_HASH_COMMENT)
                        || types.contains(RobotTokenType.COMMENT_CONTINUE)) {

                    return Range.closed(lineElem.getStartOffset(),
                            line.getEndOfLine().getStartOffset()).contains(offset);

                }
            }
        }
        return false;
    }

    static class FilteredVariables {

        private final List<VariableDeclaration> commented = new ArrayList<>();

        private final List<VariableDeclaration> used = new ArrayList<>();

        public List<VariableDeclaration> getCommented() {
            return commented;
        }

        public List<VariableDeclaration> getUsed() {
            return used;
        }
    }

    FilteredVariables filter(final RobotFileOutput rfo, final List<VariableDeclaration> vars) {
        final FilteredVariables result = new FilteredVariables();

        for (final VariableDeclaration var : vars) {
            if (isInCommentedPart(rfo, var.asToken())) {
                result.commented.add(var);
            } else {
                result.used.add(var);
            }
        }

        return result;
    }
}
