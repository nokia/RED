/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

/**
 * @author wypych
 */
public class ForContinueStartWithCommentFixer implements IForceFixBeforeDumpTask {

    @Override
    public void fixBeforeDump(final AModelElement<? extends IExecutableStepsHolder<?>> currentElement,
            final List<RobotToken> tokens) {
        if (currentElement instanceof RobotExecutableRow<?> && !tokens.isEmpty()) {
            final RobotToken firstToken = tokens.get(0);
            if (currentElement.getDeclaration() != firstToken
                    && firstToken.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                tokens.clear();
                final List<RobotToken> tokensInOrderFromModel = currentElement.getElementTokens();
                int size = tokensInOrderFromModel.size();
                for (int tokenId = 0; tokenId < size; tokenId++) {
                    RobotToken rt = tokensInOrderFromModel.get(tokenId);
                    rt.setStartOffset(FilePosition.NOT_SET);
                    rt.setLineNumber(FilePosition.NOT_SET);
                    rt.setStartColumn(FilePosition.NOT_SET);
                }

                tokens.addAll(tokensInOrderFromModel);
            }
        }
    }
}
