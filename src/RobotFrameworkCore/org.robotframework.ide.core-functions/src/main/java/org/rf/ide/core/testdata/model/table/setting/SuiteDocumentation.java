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

public class SuiteDocumentation extends AModelElement<SettingTable> {

    private final RobotToken declaration;

    private final List<RobotToken> text = new ArrayList<>();

    private final List<RobotToken> comment = new ArrayList<>();

    public SuiteDocumentation(final RobotToken declaration) {
        this.declaration = declaration;
    }
    
    public void addDocumentationText(final String text) {
        RobotToken rt = new RobotToken();
        rt.setText(text);

        addDocumentationText(rt);
    }

    public void addDocumentationText(final RobotToken token) {
        fixForTheType(token, RobotTokenType.SETTING_DOCUMENTATION_TEXT);
        text.add(token);
    }

    public List<RobotToken> getDocumentationText() {
        return Collections.unmodifiableList(text);
    }
    
    public void setDocumentationText(final int index, final String docText) {
        updateOrCreateTokenInside(text, index, docText, RobotTokenType.SETTING_DOCUMENTATION_TEXT);
    }

    public void setDocumentationText(final int index, final RobotToken docText) {
        updateOrCreateTokenInside(text, index, docText, RobotTokenType.SETTING_DOCUMENTATION_TEXT);
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
        return ModelType.SUITE_DOCUMENTATION;
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
            tokens.addAll(getDocumentationText());
            tokens.addAll(getComment());
        }

        return tokens;
    }
}
