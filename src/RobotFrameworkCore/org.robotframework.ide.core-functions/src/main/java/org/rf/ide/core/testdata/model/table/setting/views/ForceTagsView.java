/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import java.util.List;

import org.rf.ide.core.testdata.model.table.setting.ForceTags;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class ForceTagsView extends ForceTags {

    private final List<ForceTags> forceTags;

    public ForceTagsView(final List<ForceTags> forceTags) {
        super(forceTags.get(0).getDeclaration());
        this.forceTags = forceTags;
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
        joinIfNeeded();
        super.setTag(index, tag);
    }

    @Override
    public void setTag(final int index, final RobotToken tag) {
        joinIfNeeded();
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
            ForceTags joined = new ForceTags(getDeclaration());
            OneSettingJoinerHelper.joinATag(joined, forceTags);
            forceTags.clear();
            forceTags.add(joined);
        }
    }

}
