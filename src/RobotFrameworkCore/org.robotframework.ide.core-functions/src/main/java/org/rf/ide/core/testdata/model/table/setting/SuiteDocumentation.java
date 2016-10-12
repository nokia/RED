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
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SuiteDocumentation extends AModelElement<SettingTable> implements ICommentHolder, IDocumentationHolder {

    private final RobotToken declaration;

    private final List<RobotToken> text = new ArrayList<>();

    private final List<RobotToken> comment = new ArrayList<>();

    public SuiteDocumentation(final RobotToken declaration) {
        this.declaration = declaration;
        fixForTheType(declaration, RobotTokenType.SETTING_DOCUMENTATION_DECLARATION);
    }

    public void addDocumentationText(final String text) {
        RobotToken rt = new RobotToken();
        rt.setText(text);

        addDocumentationText(rt);
    }

    @Override
    public void addDocumentationText(final RobotToken token) {
        fixForTheType(token, RobotTokenType.SETTING_DOCUMENTATION_TEXT);
        text.add(token);
    }

    @Override
    public List<RobotToken> getDocumentationText() {
        return Collections.unmodifiableList(text);
    }

    @Override
    public void setDocumentationText(final int index, final String docText) {
        updateOrCreateTokenInside(text, index, docText, RobotTokenType.SETTING_DOCUMENTATION_TEXT);
    }

    @Override
    public void setDocumentationText(final int index, final RobotToken docText) {
        updateOrCreateTokenInside(text, index, docText, RobotTokenType.SETTING_DOCUMENTATION_TEXT);
    }

    @Override
    public void removeDocumentationPart(int index) {
        this.text.remove(index);
    }

    @Override
    public void clearDocumentation() {
        this.text.clear();
    }

    @Override
    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comment);
    }

    @Override
    public void addCommentPart(final RobotToken rt) {
        fixComment(getComment(), rt);
        this.comment.add(rt);
    }

    @Override
    public void setComment(String comment) {
        RobotToken tok = new RobotToken();
        tok.setText(comment);
        setComment(tok);
    }

    @Override
    public void setComment(RobotToken comment) {
        this.comment.clear();
        addCommentPart(comment);
    }

    @Override
    public void removeCommentPart(int index) {
        this.comment.remove(index);
    }

    @Override
    public void clearComment() {
        this.comment.clear();
    }

    @Override
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

    @Override
    public boolean removeElementToken(int index) {
        throw new UnsupportedOperationException("Please see " + DocumentationServiceHandler.class);
    }

    @Override
    public List<FileRegion> getContinuousRegions() {
        return new FileRegion.FileRegionSplitter().splitContinuousRegions(getElementTokens());
    }

    @Override
    public IDocumentationHolder getCached() {
        return this;
    }
}
