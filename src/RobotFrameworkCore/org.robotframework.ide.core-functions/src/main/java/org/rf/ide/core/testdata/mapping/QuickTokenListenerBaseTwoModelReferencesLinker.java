/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping;

import java.util.List;
import java.util.Map;

import org.rf.ide.core.testdata.DumpedResultBuilder.DumpedResult;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

/**
 * @author wypych
 */
public class QuickTokenListenerBaseTwoModelReferencesLinker {

    public void update(final RobotFileOutput output, final DumpedResult dumpResult) {
        updateTokensPosition(dumpResult.mappingBetweenOldAndNewTokens());
        updateOldOutputTokenLines(output, dumpResult.newProducedLines());
    }

    private void updateTokensPosition(final Map<RobotToken, RobotToken> mappingBetweenOldAndNewTokens) {
        for (final RobotToken oldToken : mappingBetweenOldAndNewTokens.keySet()) {
            final RobotToken newToken = mappingBetweenOldAndNewTokens.get(oldToken);

            oldToken.setText(newToken.getText());
            oldToken.setRaw(newToken.getRaw());
            oldToken.setLineNumber(newToken.getLineNumber());
            oldToken.setStartColumn(newToken.getStartColumn());
            oldToken.setStartOffset(newToken.getStartOffset());
            oldToken.clearDirtyFlag();
        }
    }

    private void updateOldOutputTokenLines(RobotFileOutput output, List<RobotLine> newProducedLines) {
        final RobotFile oldFileModel = output.getFileModel();
        oldFileModel.removeLines();
        for (final RobotLine line : newProducedLines) {
            oldFileModel.addNewLine(line);
        }
    }
}
