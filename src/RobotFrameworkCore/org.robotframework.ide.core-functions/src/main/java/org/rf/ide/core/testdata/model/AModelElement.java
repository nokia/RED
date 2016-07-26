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

    public abstract boolean removeElementToken(final int index);

    protected boolean removeElementFromList(final List<?> l, final int index) {
        if (index >= 0 && index < l.size()) {
            l.remove(index);

            return true;
        }

        return false;
    }

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

    protected void updateOrCreateTokenInside(final List<RobotToken> toModify, final int index, final String value) {
        updateOrCreateTokenInside(toModify, index, value, null);
    }

    protected void updateOrCreateTokenInside(final List<RobotToken> toModify, final int index, final String value,
            final IRobotTokenType expectedType) {
        RobotToken token = new RobotToken();
        if (expectedType != null) {
            fixForTheType(token, expectedType);
        }
        token.setText(value);
        updateOrCreateTokenInside(toModify, index, token);
    }

    protected void updateOrCreateTokenInside(final List<RobotToken> toModify, final int index, final RobotToken token) {
        updateOrCreateTokenInside(toModify, index, token, null);
    }

    protected void updateOrCreateTokenInside(final List<RobotToken> toModify, final int index, final RobotToken token,
            final IRobotTokenType expectedType) {
        if (expectedType != null) {
            fixForTheType(token, expectedType);
        }
        int size = toModify.size();
        if (size > index) {
            toModify.get(index).setText(token.getText());
        } else if (size == index) {
            toModify.add(token);
        } else {
            for (int i = size; i < index + 1; i++) {
                RobotToken tempToken = new RobotToken();
                tempToken.getTypes().clear();
                if (expectedType == null) {
                    tempToken.getTypes().addAll(token.getTypes());
                } else {
                    tempToken.setType(expectedType);
                }
                toModify.add(tempToken);
            }

            toModify.set(index, token);
        }
    }

    protected RobotToken updateOrCreate(final RobotToken current, final RobotToken newValue,
            final IRobotTokenType expectedType) {
        if (newValue == null) {
            return null;
        } else if (current == null || current.getFilePosition().isNotSet()) {
            fixForTheType(newValue, expectedType, true);
            return newValue;
        } else {
            return updateOrCreate(current, newValue.getText(), expectedType);
        }
    }

    protected RobotToken updateOrCreate(final RobotToken current, final String newValue,
            final IRobotTokenType expectedType) {
        RobotToken toReturn = null;
        if (current == null) {
            toReturn = new RobotToken();
        } else {
            toReturn = current;
        }

        toReturn.setText(newValue);

        if (expectedType != null) {
            fixForTheType(toReturn, expectedType);
        }

        return toReturn;
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

    protected void fixForTheType(final RobotToken token, final IRobotTokenType expectedMainType,
            boolean shouldNullCheck) {
        if (shouldNullCheck && token == null) {
            return;
        }

        fixForTheType(token, expectedMainType);
    }

    protected void positionRevertToExpectedOrder(final List<RobotToken> listOfTokens, final List<RobotToken> order) {
        if (order.isEmpty()) {
            return;
        }

        int orderTokIndex = 0;
        int size = listOfTokens.size();
        for (int i = 0; i < size; i++) {
            if (order.contains(listOfTokens.get(i))) {
                listOfTokens.set(i, order.get(orderTokIndex));
                orderTokIndex++;
            }
        }
    }
}
