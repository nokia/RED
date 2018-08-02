/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import java.util.List;

import org.rf.ide.core.testdata.model.ATags;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class DefaultTagsView extends DefaultTags {

    private static final long serialVersionUID = 737877218936160644L;

    private final List<DefaultTags> defaultTags;

    public DefaultTagsView(final List<DefaultTags> defaultTags) {
        super(defaultTags.get(0).getDeclaration());
        this.defaultTags = defaultTags;

        initialize();
    }

    private void initialize() {
        for (final ATags<?> defaultTag : defaultTags) {
            for (final RobotToken tag : defaultTag.getTags()) {
                super.addTag(tag);
            }
            for (final RobotToken comment : defaultTag.getComment()) {
                super.addCommentPart(comment);
            }
        }
    }

    @Override
    public void addTag(final String tag) {
        joinIfNeeded();
        super.addTag(tag);
    }

    @Override
    public void addTag(final RobotToken tag) {
        joinIfNeeded();
        super.addTag(tag);
    }

    @Override
    public void setTag(final int index, final String tag) {
        final List<RobotToken> tokens = super.getTags();
        if (tokens.size() <= index) {
            joinIfNeeded();
        }
        super.setTag(index, tag);
    }

    @Override
    public void setTag(final int index, final RobotToken tag) {
        final List<RobotToken> tokens = super.getTags();
        if (tokens.size() <= index) {
            joinIfNeeded();
        }
        super.setTag(index, tag);
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
        if (defaultTags.size() > 1) {
            defaultTags.clear();
            defaultTags.add(this);
        }
    }
}
