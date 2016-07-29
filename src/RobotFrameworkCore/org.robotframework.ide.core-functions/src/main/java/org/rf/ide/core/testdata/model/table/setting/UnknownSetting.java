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

public class UnknownSetting extends AModelElement<SettingTable> {

    private final RobotToken declaration;

    private final List<RobotToken> trashs = new ArrayList<>();

    public UnknownSetting(final RobotToken declaration) {
        this.declaration = declaration;
        fixForTheType(declaration, RobotTokenType.SETTING_UNKNOWN);
    }

    @Override
    public boolean isPresent() {
        return (declaration != null);
    }

    @Override
    public RobotToken getDeclaration() {
        return declaration;
    }

    public List<RobotToken> getTrashs() {
        return Collections.unmodifiableList(trashs);
    }

    public void addTrash(final RobotToken trash) {
        fixForTheType(trash, RobotTokenType.SETTING_UNKNOWN_ARGUMENT, true);
        trashs.add(trash);
    }

    @Override
    public ModelType getModelType() {
        return ModelType.SETTINGS_UNKNOWN;
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
            tokens.addAll(getTrashs());
        }

        return tokens;
    }

    @Override
    public boolean removeElementToken(int index) {
        return super.removeElementFromList(trashs, index);
    }
}
