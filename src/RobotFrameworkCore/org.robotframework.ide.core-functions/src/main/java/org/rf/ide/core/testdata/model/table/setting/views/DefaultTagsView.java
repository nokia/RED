/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import java.util.List;

import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class DefaultTagsView extends DefaultTags {

    private final List<DefaultTags> defaultTags;

    private final boolean changeForceRebuild;

    public DefaultTagsView(final List<DefaultTags> defaultTags) {
        this(defaultTags, false);
    }

    public DefaultTagsView(final List<DefaultTags> defaultTags, final boolean changeForceRebuild) {
        super(defaultTags.get(0).getDeclaration());
        this.defaultTags = defaultTags;
        this.changeForceRebuild = changeForceRebuild;

        // join tags for this view
        final DefaultTags tags = new DefaultTags(getDeclaration());
        OneSettingJoinerHelper.joinATag(tags, defaultTags);
        copyWithoutJoinIfNeededExecution(tags);
    }

    protected boolean isForceRebuild() {
        return changeForceRebuild;
    }

    private void copyWithoutJoinIfNeededExecution(final DefaultTags tags) {
        for (final RobotToken token : tags.getTags()) {
            super.addTag(token);
        }

        for (final RobotToken comment : tags.getComment()) {
            super.addCommentPart(comment);
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
        if (super.getTags().size() <= index || isForceRebuild()) {
            joinIfNeeded();
        }
        super.setTag(index, tag);
    }

    @Override
    public void setTag(final int index, final RobotToken tag) {
        if (super.getTags().size() <= index || isForceRebuild()) {
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
            DefaultTags joined = new DefaultTags(getDeclaration());
            OneSettingJoinerHelper.joinATag(joined, defaultTags);
            defaultTags.clear();
            defaultTags.add(joined);
        }
    }

}
