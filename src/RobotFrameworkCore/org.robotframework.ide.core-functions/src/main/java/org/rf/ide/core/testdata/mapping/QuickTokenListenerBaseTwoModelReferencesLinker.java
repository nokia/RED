/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.testdata.DumpedResultBuilder.DumpedResult;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class QuickTokenListenerBaseTwoModelReferencesLinker {

    public void update(final RobotFileOutput output, final DumpedResult dumpResult) {
        updateTokensPosition(dumpResult.mappingBetweenOldAndNewTokens());
        updateOldOutputTokenLines(dumpResult, output);
    }

    private void updateTokensPosition(final Map<RobotToken, RobotToken> mappingBetweenOldAndNewTokens) {
        for (final RobotToken oldToken : mappingBetweenOldAndNewTokens.keySet()) {
            final RobotToken newToken = mappingBetweenOldAndNewTokens.get(oldToken);

            oldToken.setText(newToken.getText());
            oldToken.setLineNumber(newToken.getLineNumber());
            oldToken.setStartColumn(newToken.getStartColumn());
            oldToken.setStartOffset(newToken.getStartOffset());
            oldToken.clearDirtyFlag();
        }
    }

    private void updateOldOutputTokenLines(final DumpedResult dumpResult, final RobotFileOutput output) {
        final Map<RobotToken, RobotToken> valuesToKeys = reverseValuesWithKeys(
                dumpResult.mappingBetweenOldAndNewTokens());

        final RobotFile oldFileModel = output.getFileModel();
        oldFileModel.removeLines();
        for (final RobotLine line : dumpResult.newProducedLines()) {
            oldFileModel.addNewLine(replaceNewTokenWithCorrespondingOld(valuesToKeys, line));
        }

        valuesToKeys.clear();
    }

    private RobotLine replaceNewTokenWithCorrespondingOld(final Map<RobotToken, RobotToken> valuesToKeys,
            final RobotLine line) {
        final RobotLine lineWithPositionCopied = line.deepCopy();
        final List<IRobotLineElement> newElements = lineWithPositionCopied.getLineElements();
        final List<IRobotLineElement> elements = line.getLineElements();
        final int size = elements.size();
        for (int i = 0; i < size; i++) {
            final IRobotLineElement element = elements.get(i);
            if (element.getClass() == RobotToken.class) {
                final RobotToken prevToken = valuesToKeys.get(element);
                newElements.set(i, prevToken);
            }
        }
        return lineWithPositionCopied;
    }

    private <T, P> Map<P, T> reverseValuesWithKeys(final Map<T, P> toReverse) {
        final Map<P, T> reversed = new IdentityHashMap<>(toReverse.size());
        for (final T key : toReverse.keySet()) {
            reversed.put(toReverse.get(key), key);
        }

        return reversed;
    }
}
