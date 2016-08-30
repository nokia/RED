/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author wypych
 */
public class RobotExecutableRowView {

    private final ListMultimap<RobotToken, RobotToken> specialTokens;

    public RobotExecutableRowView(final ListMultimap<RobotToken, RobotToken> specialTokens) {
        this.specialTokens = specialTokens;
    }

    public static <T extends IExecutableStepsHolder<?>> RobotExecutableRowView buildView(
            final RobotExecutableRow<T> execRowOne) {
        final ListMultimap<RobotToken, RobotToken> specialTokens = ArrayListMultimap.create();

        final T parent = execRowOne.getParent();
        if (parent != null) {
            final AModelElement<? extends ARobotSectionTable> holder = parent.getHolder();
            if (holder != null) {
                final ARobotSectionTable table = holder.getParent();
                if (table != null) {
                    final RobotFile modelFile = table.getParent();
                    final List<RobotLine> fileContent = modelFile.getFileContent();

                    final List<RobotToken> rowElements = execRowOne.getElementTokens();
                    for (final RobotToken token : rowElements) {
                        final List<IRobotTokenType> types = token.getTypes();
                        if (types.contains(RobotTokenType.START_HASH_COMMENT)
                                || types.contains(RobotTokenType.COMMENT_CONTINUE)) {
                            break;
                        } else if (types.contains(RobotTokenType.VARIABLE_USAGE)) {
                            extractAdditionalAssignmentAndPrettyAlignForCurrentToken(specialTokens, modelFile,
                                    fileContent, token);
                        }
                    }
                }
            }
        }

        return new RobotExecutableRowView(specialTokens);
    }

    private static void extractAdditionalAssignmentAndPrettyAlignForCurrentToken(
            final ListMultimap<RobotToken, RobotToken> specialTokens, final RobotFile modelFile,
            final List<RobotLine> fileContent, final RobotToken token) {
        final int offset = token.getFilePosition().getOffset();
        if (offset > FilePosition.NOT_SET) {
            final Optional<Integer> elementLine = modelFile.getRobotLineIndexBy(offset);
            if (elementLine.isPresent()) {
                final RobotLine robotLine = fileContent.get(elementLine.get());
                collectPrettyAlignsAndAssignmentAfterToken(specialTokens, token, robotLine);
            }
        }
    }

    private static void collectPrettyAlignsAndAssignmentAfterToken(
            final ListMultimap<RobotToken, RobotToken> specialTokens, final RobotToken token,
            final RobotLine robotLine) {
        final Optional<Integer> elementPositionInLine = robotLine.getElementPositionInLine(token);
        if (elementPositionInLine.isPresent()) {
            int lastAssignment = -1;
            final List<IRobotLineElement> lineElements = robotLine.getLineElements();
            int lineElementsSize = lineElements.size();
            final List<RobotToken> tokensView = specialTokens.get(token);
            for (int i = elementPositionInLine.get() + 1; i < lineElementsSize; i++) {
                final IRobotLineElement lineElement = lineElements.get(i);
                final List<IRobotTokenType> elementTypes = lineElement.getTypes();
                if (lineElement.getClass() == RobotToken.class) {
                    RobotToken currentToken = (RobotToken) lineElement;
                    if (elementTypes.contains(RobotTokenType.ASSIGNMENT)
                            || elementTypes.contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                        specialTokens.put(token, currentToken);
                        if (elementTypes.contains(RobotTokenType.ASSIGNMENT)) {
                            lastAssignment = tokensView.size();
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }

            if (lastAssignment == -1) {
                specialTokens.removeAll(token);
            } else if (lastAssignment != tokensView.size()) {
                while (lastAssignment != tokensView.size()) {
                    tokensView.remove(lastAssignment);
                }
            }
        }
    }

    public String getTokenRepresentation(final RobotToken token) {
        final List<RobotToken> additionalTokens = specialTokens.get(token);
        if (additionalTokens != null && !additionalTokens.isEmpty()) {
            StringBuilder str = new StringBuilder(token.getText());
            for (final RobotToken tok : additionalTokens) {
                str.append(tok.getText());
            }

            return str.toString().intern();
        }

        return token.getText();
    }

    @VisibleForTesting
    public ImmutableListMultimap<RobotToken, RobotToken> getTokensWithSuffix() {
        return ImmutableListMultimap.copyOf(specialTokens);
    }
}
