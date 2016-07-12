/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.rf.ide.core.testdata.mapping.collect.RobotTokensCollector;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ListMultimap;

/**
 * @author wypych
 */
public class FileOutputsMatchUpdater {

    private final RobotTokensCollector tokenCollector;

    public FileOutputsMatchUpdater() {
        this(new RobotTokensCollector());
    }

    @VisibleForTesting
    protected FileOutputsMatchUpdater(final RobotTokensCollector tokenCollector) {
        this.tokenCollector = tokenCollector;
    }

    public void update(final RobotFileOutput oldModifiedOutput, final RobotFileOutput alreadyDumpedContent) {
        update(oldModifiedOutput, alreadyDumpedContent, true);
    }

    public void update(final RobotFileOutput oldModifiedOutput, final RobotFileOutput alreadyDumpedContent,
            boolean fallbackAllowed) {
        validateBasicThatOutputFromSameFile(oldModifiedOutput, alreadyDumpedContent);

        final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens = tokenCollector
                .extractRobotTokens(oldModifiedOutput);
        final ListMultimap<RobotTokenType, RobotToken> newViewAboutTokens = tokenCollector
                .extractRobotTokens(alreadyDumpedContent);

        if (fallbackAllowed) {
            validateThatTheSameTokensInView(oldViewAboutTokens, newViewAboutTokens);
        }

        replaceNewReferenceByCorrespondingOld(oldModifiedOutput, oldViewAboutTokens, alreadyDumpedContent,
                newViewAboutTokens);
    }

    @VisibleForTesting
    protected void replaceNewReferenceByCorrespondingOld(final RobotFileOutput oldModifiedOutput,
            final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens,
            final RobotFileOutput alreadyDumpedContent,
            final ListMultimap<RobotTokenType, RobotToken> newViewAboutTokens) {

        // general idea: 1. 'old tokens contains content as expected, so we only updating in them
        // position with clear dirty flag'
        // 2. next we are searching new token in new output line position and replacing it by old
        // 3. last we removing old lines and adding new lines in old output object
        final List<RobotLine> newContentLines = alreadyDumpedContent.getFileModel().getFileContent();

        for (final RobotTokenType type : oldViewAboutTokens.keySet()) {
            final List<RobotToken> oldToUpdate = oldViewAboutTokens.get(type);
            final List<RobotToken> newToCopy = newViewAboutTokens.get(type);

            int tokSize = Math.min(oldToUpdate.size(), newToCopy.size());
            for (int index = 0; index < tokSize; index++) {
                final RobotToken oldToken = oldToUpdate.get(index);
                final RobotToken newToken = newToCopy.get(index);

                oldToken.setLineNumber(newToken.getLineNumber());
                oldToken.setStartColumn(newToken.getStartColumn());
                oldToken.setStartOffset(newToken.getStartOffset());
                oldToken.clearDirtyFlag();

                final RobotLine robotLine = newContentLines.get(newToken.getLineNumber() - 1);
                final Optional<Integer> posToken = robotLine.getElementPositionInLine(newToken);
                robotLine.setLineElementAt(posToken.get(), oldToken);
            }
        }

        final RobotFile oldFileModel = oldModifiedOutput.getFileModel();
        oldFileModel.removeLines();
        for (final RobotLine line : newContentLines) {
            oldFileModel.addNewLine(line);
        }
    }

    @VisibleForTesting
    protected void validateBasicThatOutputFromSameFile(final RobotFileOutput oldModifiedOutput,
            final RobotFileOutput alreadyDumpedContent) {
        try {
            final Path oldPathNormalized = oldModifiedOutput.getProcessedFile().toPath().normalize();
            final Path newContentFileNormalized = alreadyDumpedContent.getProcessedFile().toPath().normalize();
            if (!Files.isSameFile(oldPathNormalized, newContentFileNormalized)) {
                throw new NotSameOutputFiles(
                        "File " + newContentFileNormalized + " is not expected " + oldPathNormalized);
            }
        } catch (final IOException e) {
            throw new NotSameOutputFiles(e);
        }
    }

    @VisibleForTesting
    protected void validateThatTheSameTokensInView(final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens,
            final ListMultimap<RobotTokenType, RobotToken> newViewAboutTokens) {
        final Set<RobotTokenType> newKeySet = newViewAboutTokens.keySet();
        for (final RobotTokenType t : newKeySet) {
            List<RobotToken> oldToks = oldViewAboutTokens.get(t);
            List<RobotToken> newToks = newViewAboutTokens.get(t);

            int toksSize = -1;
            if (oldToks.size() == newToks.size()) {
                toksSize = oldToks.size();
            } else {
                int oldNotEmpty = findTheFirstNotEmpty(oldToks);
                int newNotEmpty = findTheFirstNotEmpty(newToks);
                if (oldNotEmpty == newNotEmpty) {
                    toksSize = oldNotEmpty;
                } else {
                    throw new NotSameOutputFiles("Type " + t + " has not the same number of elements in outputs.");
                }
            }

            for (int i = 0; i < toksSize; i++) {
                final RobotToken rtOld = oldToks.get(i);
                final RobotToken rtNew = newToks.get(i);
                if (!rtOld.getText().equals(rtNew.getText())) {
                    if (!(rtOld.getText().trim().isEmpty() && rtNew.getText().trim().equals("\\"))) {
                        throw new NotSameOutputFiles("Token type " + t + " with index " + i
                                + " doesn't contain the same content as old. Expected " + rtOld.getText() + " got "
                                + rtNew.getText());
                    }
                }
            }
        }

    }

    private int findTheFirstNotEmpty(final List<RobotToken> t) {
        int index = -1;
        for (int i = t.size() - 1; i >= 0; i--) {
            if (!t.get(i).getText().isEmpty()) {
                return i;
            }
        }

        return index;
    }

    public static class NotSameOutputFiles extends RuntimeException {

        private static final long serialVersionUID = -4734971783082313050L;

        public NotSameOutputFiles(final String errorMsg) {
            super(errorMsg);
        }

        public NotSameOutputFiles(final Exception e) {
            super(e);
        }
    }
}
