/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.rf.ide.core.testdata.mapping.collect.RobotTokensCollector;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ListMultimap;

/**
 * @author wypych
 */
public class TwoModelReferencesLinker {

    private final RobotTokensCollector tokenCollector;

    public TwoModelReferencesLinker() {
        this(new RobotTokensCollector());
    }

    @VisibleForTesting
    protected TwoModelReferencesLinker(final RobotTokensCollector tokenCollector) {
        this.tokenCollector = tokenCollector;
    }

    public void update(final RobotFileOutput oldModifiedOutput, final RobotFileOutput alreadyDumpedContent) {
        update(oldModifiedOutput, alreadyDumpedContent, true);
    }

    public void update(final RobotFileOutput oldModifiedOutput, final RobotFileOutput alreadyDumpedContent,
            final boolean fallbackAllowed) {
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

        // general idea: 1. 'old tokens contains content as expected, so we only
        // updating in them position with clear dirty flag'
        // 2. next we are searching new token in new output line position and replacing
        // it by old
        // 3. last we removing old lines and adding new lines in old output object
        final List<RobotLine> newContentLines = alreadyDumpedContent.getFileModel().getFileContent();

        for (final RobotTokenType type : oldViewAboutTokens.keySet()) {
            final List<RobotToken> oldToUpdate = oldViewAboutTokens.get(type);
            final List<RobotToken> newToCopy = newViewAboutTokens.get(type);

            final int tokSize = Math.min(oldToUpdate.size(), newToCopy.size());
            for (int index = 0; index < tokSize; index++) {
                final RobotToken oldToken = oldToUpdate.get(index);
                final RobotToken newToken = newToCopy.get(index);

                oldToken.setText(newToken.getText());
                oldToken.setLineNumber(newToken.getLineNumber());
                oldToken.setStartColumn(newToken.getStartColumn());
                oldToken.setStartOffset(newToken.getStartOffset());
                oldToken.clearDirtyFlag();

                if (newToken.getLineNumber() >= 0) {
                    final RobotLine robotLine = newContentLines.get(newToken.getLineNumber() - 1);
                    final Optional<Integer> posToken = robotLine.getElementPositionInLine(newToken);
                    if (posToken.isPresent()) {
                        robotLine.setLineElementAt(posToken.get(), oldToken);
                    }
                }
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
                throw new DifferentOutputFile(
                        "File " + newContentFileNormalized + " is not expected, old path: " + oldPathNormalized);
            }
        } catch (final IOException e) {
            throw new DifferentOutputFile(e);
        }
    }

    @VisibleForTesting
    protected void validateThatTheSameTokensInView(final ListMultimap<RobotTokenType, RobotToken> oldViewAboutTokens,
            final ListMultimap<RobotTokenType, RobotToken> newViewAboutTokens) {
        final Set<RobotTokenType> newKeySet = newViewAboutTokens.keySet();
        for (final RobotTokenType t : newKeySet) {
            final List<RobotToken> oldToks = new ArrayList<>(oldViewAboutTokens.get(t));
            final List<RobotToken> newToks = newViewAboutTokens.get(t);

            int toksSize = -1;
            if (oldToks.size() == newToks.size()) {
                toksSize = oldToks.size();
            } else {
                removePreviousLineContinue(oldToks);
                final int oldNotEmpty = findTheFirstNotEmpty(oldToks);
                final int newNotEmpty = findTheFirstNotEmpty(newToks);
                if (oldNotEmpty == newNotEmpty) {
                    toksSize = oldNotEmpty;
                } else {
                    throw new DifferentOutputFile("Type " + t + " has not the same number of elements in outputs.");
                }
            }

            for (int i = 0; i < toksSize; i++) {
                final RobotToken rtOld = oldToks.get(i);
                final RobotToken rtNew = newToks.get(i);
                if (!rtOld.getText().equals(rtNew.getText())) {
                    if (!isAcceptableContent(rtOld, rtNew)) {
                        throw new DifferentOutputFile("Token type " + t + " with index " + i
                                + " doesn't contain the same content as old. Expected " + rtOld.getText() + " got "
                                + rtNew.getText());
                    }
                }
            }
        }

    }

    private boolean isAcceptableContent(final RobotToken rtOld, final RobotToken rtNew) {
        final String oldTrimmed = rtOld.getText().trim();
        final String newTrimmed = rtNew.getText().trim();

        if ((oldTrimmed.isEmpty() && newTrimmed.equals("\\"))) {
            return true;
        }

        if (oldTrimmed.equals("...") && newTrimmed.equals("...")) {
            return true;
        }

        if (rtOld.getTypes().contains(RobotTokenType.VARIABLE_USAGE)) {
            final String oldText = oldTrimmed;
            final String newText = newTrimmed;

            if (newText.endsWith("=")) {
                if (oldText.equals(newText.substring(0, newText.length() - 1).trim())) {
                    return true;
                }
            }
        }

        if (rtNew.getTypes().contains(RobotTokenType.VARIABLE_USAGE)) {
            final String oldText = oldTrimmed;
            final String newText = newTrimmed;

            if (oldText.endsWith("=")) {
                if (newText.equals(oldText.substring(0, oldText.length() - 1).trim())) {
                    return true;
                }
            }
        }

        if (!newTrimmed.isEmpty() && newTrimmed.equals(oldTrimmed)) {
            return true;
        }

        return false;
    }

    private void removePreviousLineContinue(final List<RobotToken> toks) {
        for (int index = 0; index < toks.size(); index++) {
            final RobotToken rTokCurrent = toks.get(index);
            final String tokCurrent = rTokCurrent.getText();
            if (tokCurrent.equals("\n...") || tokCurrent.equals("\r\n...") || tokCurrent.equals("\r...")) {
                toks.remove(index);
                index--;
            }
        }
    }

    private int findTheFirstNotEmpty(final List<RobotToken> t) {
        final int index = -1;
        for (int i = t.size() - 1; i >= 0; i--) {
            if (!t.get(i).getText().isEmpty()) {
                return i;
            }
        }

        return index;
    }

    public static class DifferentOutputFile extends RuntimeException {

        private static final long serialVersionUID = -4734971783082313050L;

        public DifferentOutputFile(final String errorMsg) {
            super(errorMsg);
        }

        public DifferentOutputFile(final Exception e) {
            super(e);
        }
    }
}
