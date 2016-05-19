/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class Metadata extends AModelElement<SettingTable> {

    private final RobotToken declaration;

    private RobotToken key;

    private final List<RobotToken> values = new ArrayList<>();

    private final List<RobotToken> comment = new ArrayList<>();

    public Metadata(final RobotToken declaration) {
        this.declaration = declaration;
    }

    public void setKey(final RobotToken key) {
        this.key = updateOrCreate(this.key, key, RobotTokenType.SETTING_METADATA_KEY);
    }
    
    public void setKey(final String key) {
        this.key = updateOrCreate(this.key, key, RobotTokenType.SETTING_METADATA_KEY);
    }

    public RobotToken getKey() {
        return key;
    }
    
    public void addValue(final String value) {
        RobotToken rt = new RobotToken();
        rt.setText(value);

        addValue(rt);
    }

    public void addValue(final RobotToken value) {
        fixForTheType(value, RobotTokenType.SETTING_METADATA_VALUE, true);
        this.values.add(value);
    }

    public List<RobotToken> getValues() {
        return Collections.unmodifiableList(values);
    }
    
    public void setValues(final int index, final String value) {
        updateOrCreateTokenInside(values, index, value, RobotTokenType.SETTING_METADATA_VALUE);
    }
    
    public void setValues(final int index, final RobotToken value) {
        updateOrCreateTokenInside(values, index, value, RobotTokenType.SETTING_METADATA_VALUE);
    }

    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
    }

    public void addCommentPart(final RobotToken rt) {
        fixComment(getComment(), rt);
        this.comment.add(rt);
    }
    
    public void setComment(final String comment) {
        RobotToken token = new RobotToken();
        token.setText(comment);

        setComment(token);
    }
    
    public void setComment(final RobotToken rt) {
        this.comment.clear();
        addCommentPart(rt);
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
        final List<RobotToken> tokens = new ArrayList<>();
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
