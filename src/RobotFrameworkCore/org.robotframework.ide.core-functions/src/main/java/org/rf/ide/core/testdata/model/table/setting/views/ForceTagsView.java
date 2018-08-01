/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import java.util.List;

import org.rf.ide.core.testdata.model.ATags;
import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ForceTagsView extends ForceTags {

    private static final long serialVersionUID = 5413048519586897277L;

    private final List<ForceTags> forceTags;

    public ForceTagsView(final List<ForceTags> forceTags) {
        super(forceTags.get(0).getDeclaration());
        this.forceTags = forceTags;

        initialize();
    }

    private void initialize() {
        for (final ATags<?> forceTag : forceTags) {
            for (final RobotToken tag : forceTag.getTags()) {
                super.addTag(tag);
            }
            for (final RobotToken comment : forceTag.getComment()) {
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
        if (forceTags.size() > 1) {
            forceTags.clear();
            forceTags.add(this);
        }
    }
}
