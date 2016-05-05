/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.List;

import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public abstract class AModelElement<T> implements IOptional, IChildElement<T> {

    private T parent;

    public abstract ModelType getModelType();

    public abstract FilePosition getBeginPosition();

    public abstract List<RobotToken> getElementTokens();

    public abstract RobotToken getDeclaration();

    public void setParent(T parent) {
        this.parent = parent;
    }

    @Override
    public T getParent() {
        return parent;
    }

    public FilePosition getEndPosition() {
        FilePosition pos = FilePosition.createNotSet();
        if (isPresent()) {
            List<RobotToken> elementTokens = getElementTokens();

            int size = elementTokens.size();
            for (int i = size - 1; i >= 0; i--) {
                RobotToken robotToken = elementTokens.get(i);
                if (robotToken.getStartOffset() >= 0) {
                    int endColumn = robotToken.getEndColumn();
                    int length = endColumn - robotToken.getStartColumn();
                    FilePosition fp = robotToken.getFilePosition();
                    pos = new FilePosition(fp.getLine(), robotToken.getEndColumn(), fp.getOffset() + length);
                    break;
                }
            }
        }

        return pos;
    }

    protected void fixComment(final List<RobotToken> comment, final RobotToken rt) {
        if (comment.isEmpty() && !rt.getText().startsWith("#")) {
            rt.setText("#" + rt.getText());
        }

        if (!rt.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                && !rt.getTypes().contains(RobotTokenType.COMMENT_CONTINUE)) {
            if (comment.isEmpty()) {
                rt.setType(RobotTokenType.START_HASH_COMMENT);
            } else {
                rt.setType(RobotTokenType.COMMENT_CONTINUE);
            }
        }
    }

    protected void fixForTheType(final RobotToken token, final IRobotTokenType expectedMainType) {
        final List<IRobotTokenType> tagTypes = token.getTypes();
        if (!tagTypes.contains(expectedMainType)) {
            if (tagTypes.isEmpty()) {
                tagTypes.add(expectedMainType);
            } else {
                tagTypes.add(0, expectedMainType);
            }
        }
    }
}
