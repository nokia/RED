/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.testcases;

import java.io.Serializable;
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
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestDocumentation extends AModelElement<TestCase>
        implements ICommentHolder, IDocumentationHolder, Serializable {

    private static final long serialVersionUID = 3688028228608204488L;

    private final RobotToken declaration;

    private final List<RobotToken> text = new ArrayList<>();

    private final List<RobotToken> comment = new ArrayList<>();

    public TestDocumentation(final RobotToken declaration) {
        this.declaration = declaration;
        fixForTheType(declaration, RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION);
    }

    @Override
    public void addDocumentationText(final RobotToken token) {
        fixForTheType(token, RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION_TEXT, true);
        text.add(token);
    }

    public void addDocumentationText(final int index, final String value) {
        updateOrCreateTokenInside(text, index, value, RobotTokenType.KEYWORD_SETTING_DOCUMENTATION_TEXT);
    }

    @Override
    public List<RobotToken> getDocumentationText() {
        return Collections.unmodifiableList(text);
    }

    @Override
    public void setDocumentationText(final int index, final String docText) {
        updateOrCreateTokenInside(text, index, docText, RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION_TEXT);
    }

    @Override
    public void setDocumentationText(final int index, final RobotToken docText) {
        updateOrCreateTokenInside(text, index, docText, RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION_TEXT);
    }

    @Override
    public void removeDocumentationPart(final int index) {
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
        addCommentPart(comment.size(), rt);
    }

    private void addCommentPart(final int index, final RobotToken rt) {
        fixComment(getComment(), rt);
        this.comment.add(index, rt);
    }

    @Override
    public void setComment(final String comment) {
        final RobotToken tok = new RobotToken();
        tok.setText(comment);
        setComment(tok);
    }

    @Override
    public void setComment(final RobotToken comment) {
        this.comment.clear();
        addCommentPart(comment);
    }

    @Override
    public void removeCommentPart(final int index) {
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
        return ModelType.TEST_CASE_DOCUMENTATION;
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
    public boolean removeElementToken(final int index) {
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
