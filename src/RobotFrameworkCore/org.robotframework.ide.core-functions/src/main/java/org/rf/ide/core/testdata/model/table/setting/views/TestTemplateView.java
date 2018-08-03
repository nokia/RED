/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import java.util.List;

import org.rf.ide.core.testdata.model.table.setting.TestTemplate;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TestTemplateView extends TestTemplate {

    private static final long serialVersionUID = 6978724647058934866L;

    private final List<TestTemplate> templates;

    public TestTemplateView(final List<TestTemplate> templates) {
        super(templates.get(0).getDeclaration());
        this.templates = templates;

        initialize();
    }

    private void initialize() {
        for (final TestTemplate template : templates) {
            if (template.getKeywordName() != null) {
                if (getKeywordName() != null) {
                    super.addUnexpectedTrashArgument(template.getKeywordName());
                } else {
                    super.setKeywordName(template.getKeywordName());
                }
            }
            for (final RobotToken arg : template.getUnexpectedTrashArguments()) {
                super.addUnexpectedTrashArgument(arg);
            }
            for (final RobotToken commentText : template.getComment()) {
                super.addCommentPart(commentText);
            }
        }

    }

    @Override
    public void addUnexpectedTrashArgument(final RobotToken trashArgument) {
        joinIfNeeded();
        super.addUnexpectedTrashArgument(trashArgument);
    }

    @Override
    public void addUnexpectedTrashArgument(final String trashArgument) {
        joinIfNeeded();
        super.addUnexpectedTrashArgument(trashArgument);
    }

    @Override
    public void setUnexpectedTrashArgument(final int index, final String argument) {
        final List<RobotToken> tokens = super.getUnexpectedTrashArguments();
        if (tokens.size() <= index) {
            joinIfNeeded();
        }
        super.setUnexpectedTrashArgument(index, argument);
    }

    @Override
    public void setUnexpectedTrashArgument(final int index, final RobotToken argument) {
        final List<RobotToken> tokens = super.getUnexpectedTrashArguments();
        if (tokens.size() <= index) {
            joinIfNeeded();
        }
        super.setUnexpectedTrashArgument(index, argument);
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

    @Override
    public void addCommentPart(final RobotToken rt) {
        joinIfNeeded();
        super.addCommentPart(rt);
    }

    private synchronized void joinIfNeeded() {
        if (templates.size() > 1) {
            templates.clear();
            templates.add(this);
        }
    }
}
