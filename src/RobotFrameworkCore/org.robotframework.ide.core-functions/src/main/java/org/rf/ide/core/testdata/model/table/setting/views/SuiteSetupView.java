/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.setting.views;

import java.util.List;

import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class SuiteSetupView extends SuiteSetup implements ISingleElementViewer {

    private static final long serialVersionUID = 500336715462459117L;

    private final List<SuiteSetup> setups;

    private final boolean changeForceRebuild;

    public SuiteSetupView(final List<SuiteSetup> setups) {
        this(setups, false);
    }

    public SuiteSetupView(final List<SuiteSetup> setups, boolean changeForceRebuild) {
        super(setups.get(0).getDeclaration());
        this.setups = setups;
        // join setup for this view
        final SuiteSetup setup = new SuiteSetup(getDeclaration());
        OneSettingJoinerHelper.joinKeywordBase(setup, setups);
        copyWithoutJoinIfNeededExecution(setup);
        this.changeForceRebuild = changeForceRebuild;
    }

    @Override
    public boolean isForceRebuild() {
        return changeForceRebuild;
    }

    private void copyWithoutJoinIfNeededExecution(final SuiteSetup setup) {
        super.setKeywordName(setup.getKeywordName());
        for (final RobotToken arg : setup.getArguments()) {
            super.addArgument(arg);
        }

        for (final RobotToken commentText : setup.getComment()) {
            super.addCommentPart(commentText);
        }
    }

    @Override
    public void setKeywordName(final String keywordName) {
        OneSettingJoinerHelper.applyJoinBeforeModificationIfNeeded(this, null, 0);
        super.setKeywordName(keywordName);
    }

    @Override
    public void setKeywordName(final RobotToken keywordName) {
        OneSettingJoinerHelper.applyJoinBeforeModificationIfNeeded(this, null, 0);
        super.setKeywordName(keywordName);
    }

    @Override
    public void addArgument(final String argument) {
        joinIfNeeded();
        super.addArgument(argument);
    }

    @Override
    public void addArgument(final RobotToken argument) {
        joinIfNeeded();
        super.addArgument(argument);
    }

    @Override
    public void setArgument(final int index, final String argument) {
        OneSettingJoinerHelper.applyJoinBeforeModificationIfNeeded(this, super.getArguments(), index);
        super.setArgument(index, argument);
    }

    @Override
    public void setArgument(final int index, final RobotToken argument) {
        OneSettingJoinerHelper.applyJoinBeforeModificationIfNeeded(this, super.getArguments(), index);
        super.setArgument(index, argument);
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

    @Override
    public synchronized void joinIfNeeded() {
        if (setups.size() > 1) {
            SuiteSetup joined = new SuiteSetup(getDeclaration());
            OneSettingJoinerHelper.joinKeywordBase(joined, setups);
            setups.clear();
            setups.add(this);
        }
    }
}
