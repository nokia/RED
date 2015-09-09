/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.userKeywords;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class KeywordReturn extends AModelElement {

    private final RobotToken declaration;
    private final List<RobotToken> values = new LinkedList<>();
    private final List<RobotToken> comment = new LinkedList<>();


    public KeywordReturn(final RobotToken declaration) {
        this.declaration = declaration;
    }


    @Override
    public boolean isPresent() {
        return (declaration != null);
    }


    public RobotToken getDeclaration() {
        return declaration;
    }


    public List<RobotToken> getReturnValues() {
        return Collections.unmodifiableList(values);
    }


    public void addReturnValue(final RobotToken returnValue) {
        values.add(returnValue);
    }


    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
    }


    public void addCommentPart(final RobotToken rt) {
        this.comment.add(rt);
    }


    @Override
    public ModelType getModelType() {
        return ModelType.USER_KEYWORD_RETURN;
    }


    @Override
    public FilePosition getBeginPosition() {
        return getDeclaration().getFilePosition();
    }


    @Override
    public List<RobotToken> getElementTokens() {
        List<RobotToken> tokens = new LinkedList<>();
        if (isPresent()) {
            tokens.add(getDeclaration());
            tokens.addAll(getReturnValues());
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
