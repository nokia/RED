/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import java.util.List;

import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class SuiteDocumentationView extends SuiteDocumentation implements ISingleElementViewer {

    private final List<SuiteDocumentation> suiteDocs;

    private final boolean changeForceRebuild;

    public SuiteDocumentationView(final List<SuiteDocumentation> suiteDocs) {
        this(suiteDocs, false);
    }

    public SuiteDocumentationView(final List<SuiteDocumentation> suiteDocs, final boolean changeForceRebuild) {
        super(suiteDocs.get(0).getDeclaration());
        this.suiteDocs = suiteDocs;
        this.changeForceRebuild = changeForceRebuild;

        // join tags for this view
        final SuiteDocumentation doc = new SuiteDocumentation(getDeclaration());
        joinDoc(doc, suiteDocs);
        copyWithoutJoinIfNeededExecution(doc);
    }

    public boolean isForceRebuild() {
        return changeForceRebuild;
    }

    private void copyWithoutJoinIfNeededExecution(final SuiteDocumentation doc) {
        for (final RobotToken token : doc.getDocumentationText()) {
            super.addDocumentationText(token);
        }

        for (final RobotToken comment : doc.getComment()) {
            super.addCommentPart(comment);
        }
    }

    @Override
    public void addDocumentationText(final String text) {
        joinIfNeeded();
        super.addDocumentationText(text);
    }

    @Override
    public void addDocumentationText(final RobotToken token) {
        joinIfNeeded();
        super.addDocumentationText(token);
    }

    @Override
    public void setDocumentationText(final int index, final String docText) {
        OneSettingJoinerHelper.applyJoinBeforeModificationIfNeeded(this, super.getDocumentationText(), index);
        super.setDocumentationText(index, docText);
    }

    @Override
    public void setDocumentationText(final int index, final RobotToken docText) {
        OneSettingJoinerHelper.applyJoinBeforeModificationIfNeeded(this, super.getDocumentationText(), index);
        super.setDocumentationText(index, docText);
    }

    @Override
    public void addCommentPart(final RobotToken rt) {
        joinIfNeeded();
        super.addCommentPart(rt);
    }

    @Override
    public void setComment(final String comment) {
        joinIfNeeded();
        super.setComment(comment);
    }

    @Override
    public void setComment(final RobotToken rt) {
        joinIfNeeded();
        super.setComment(rt);
    }

    public synchronized void joinIfNeeded() {
        if (suiteDocs.size() > 1) {
            SuiteDocumentation joined = new SuiteDocumentation(getDeclaration());
            joinDoc(joined, suiteDocs);
            suiteDocs.clear();
            suiteDocs.add(joined);
        }
    }

    private void joinDoc(final SuiteDocumentation target, final List<SuiteDocumentation> suiteDocs) {
        for (final SuiteDocumentation sd : suiteDocs) {
            for (final RobotToken text : sd.getDocumentationText()) {
                target.addDocumentationText(text);
            }

            for (final RobotToken comment : sd.getComment()) {
                target.addCommentPart(comment);
            }
        }
    }
}
