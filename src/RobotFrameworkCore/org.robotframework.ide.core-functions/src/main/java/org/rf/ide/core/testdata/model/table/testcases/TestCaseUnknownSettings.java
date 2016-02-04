/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.testcases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TestCaseUnknownSettings extends AModelElement<TestCase> {

    private final RobotToken declaration;

    private final List<RobotToken> arguments = new ArrayList<>(0);

    private final List<RobotToken> comment = new ArrayList<>();

    public TestCaseUnknownSettings(final RobotToken declaration) {
        this.declaration = declaration;
    }

    public void addArgument(final RobotToken arg) {
        this.arguments.add(arg);
    }

    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    @Override
    public boolean isPresent() {
        return (declaration != null);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.TEST_CASE_SETTING_UNKNOWN;
    }

    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
    }

    public void addCommentPart(final RobotToken rt) {
        this.comment.add(rt);
    }

    @Override
    public FilePosition getBeginPosition() {
        return getDeclaration().getFilePosition();
    }

    @Override
    public List<RobotToken> getElementTokens() {
        List<RobotToken> elems = new LinkedList<>();
        if (isPresent()) {
            elems.add(declaration);
            elems.addAll(arguments);
            elems.addAll(comment);
        }
        return elems;
    }

    @Override
    public RobotToken getDeclaration() {
        return this.declaration;
    }
}
