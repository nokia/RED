/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.setting;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class Metadata extends AModelElement<SettingTable> {

    private final RobotToken declaration;
    private RobotToken key;
    private final List<RobotToken> values = new LinkedList<>();
    private final List<RobotToken> comment = new LinkedList<>();


    public Metadata(final RobotToken declaration) {
        this.declaration = declaration;
    }


    public void setKey(final RobotToken key) {
        this.key = key;
    }


    public RobotToken getKey() {
        return key;
    }


    public void addValue(final RobotToken value) {
        this.values.add(value);
    }


    public List<RobotToken> getValues() {
        return Collections.unmodifiableList(values);
    }


    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
    }


    public void addCommentPart(final RobotToken rt) {
        this.comment.add(rt);
    }


    public RobotToken getDeclaration() {
        return declaration;
    }


    @Override
    public boolean isPresent() {
        return (getDeclaration() != null);
    }


    @Override
    public ModelType getModelType() {
        return ModelType.METADATA_SETTING;
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
            if (getKey() != null) {
                tokens.add(getKey());
            }
            tokens.addAll(getValues());
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
