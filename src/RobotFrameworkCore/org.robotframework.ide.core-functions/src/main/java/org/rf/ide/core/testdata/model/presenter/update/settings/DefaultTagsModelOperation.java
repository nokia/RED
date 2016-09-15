/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.settings;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ISettingTableElementOperation;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.DefaultTags;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class DefaultTagsModelOperation implements ISettingTableElementOperation {

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return (elementType == RobotTokenType.SETTING_DEFAULT_TAGS_DECLARATION);
    }

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return (elementType == ModelType.DEFAULT_TAGS_SETTING);
    }

    @Override
    public AModelElement<?> create(final SettingTable settingsTable, final int tableIndex, final List<String> args, final String comment) {
        final DefaultTags newDefaultTags = settingsTable.newDefaultTag();

        for (int i = 0; i < args.size(); i++) {
            newDefaultTags.addTag(args.get(i));
        }
        if (comment != null && !comment.isEmpty()) {
            newDefaultTags.setComment(comment);
        }
        return newDefaultTags;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final DefaultTags defaultTags = (DefaultTags) modelElement;
        if (index >= 0) {
            if (value != null) {
                defaultTags.setTag(index, value);
            } else {
                defaultTags.removeElementToken(index);
            }
        }
    }

    @Override
    public void remove(final SettingTable settingsTable, final AModelElement<?> modelElements) {
        settingsTable.removeDefaultTags();
    }

    @Override
    public void insert(final SettingTable settingsTable, final int index, final AModelElement<?> modelElement) {
        settingsTable.addDefaultTags((DefaultTags) modelElement);;
    }
}
