/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Optional;
import com.google.common.collect.Range;

public class CommentedVariablesFilter {

    public static class FilteredVariables {

        private final List<VariableDeclaration> commented = new ArrayList<>();

        private final List<VariableDeclaration> used = new ArrayList<>();

        public List<VariableDeclaration> getCommented() {
            return commented;
        }

        public List<VariableDeclaration> getUsed() {
            return used;
        }
    }

    public FilteredVariables filter(final RobotFileOutput rfo, final List<VariableDeclaration> vars) {
        FilteredVariables result = new FilteredVariables();

        for (final VariableDeclaration var : vars) {
            if (isInCommentedPart(rfo, var.asToken())) {
                result.commented.add(var);
            } else {
                result.used.add(var);
            }
        }

        return result;
    }

    public boolean isInCommentedPart(final RobotFileOutput rfo, final RobotToken token) {
        boolean result = false;

        Optional<Integer> startCommentRange = Optional.absent();
        RobotFile fileModel = rfo.getFileModel();
        int tokenOffset = token.getStartOffset();
        Optional<Integer> robotLineIndex = fileModel.getRobotLineIndexBy(tokenOffset);
        if (robotLineIndex.isPresent()) {
            RobotLine robotLine = fileModel.getFileContent().get(robotLineIndex.get());
            List<IRobotLineElement> lineElements = robotLine.getLineElements();
            for (IRobotLineElement lineElem : lineElements) {
                List<IRobotTokenType> types = lineElem.getTypes();
                if (types.contains(RobotTokenType.START_HASH_COMMENT)
                        || types.contains(RobotTokenType.COMMENT_CONTINUE)) {
                    startCommentRange = Optional.of(lineElem.getStartOffset());
                    break;
                }
            }

            if (startCommentRange.isPresent()) {
                Range<Integer> range = Range.closed(startCommentRange.get(), robotLine.getEndOfLine().getStartOffset());
                result = range.contains(tokenOffset);
            }
        }

        return result;
    }
}
