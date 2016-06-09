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
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class MetadataModelOperation implements ISettingTableElementOperation {

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return (elementType == RobotTokenType.SETTING_METADATA_DECLARATION);
    }

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return (elementType == ModelType.METADATA_SETTING);
    }

    @Override
    public AModelElement<?> create(final SettingTable settingsTable, final int tableIndex, final List<String> args, final String comment) {
        final Metadata newMetadata = isValidTableIndex(settingsTable, tableIndex)
                ? settingsTable.newMetadata(tableIndex) : settingsTable.newMetadata();
        if (!args.isEmpty()) {
            newMetadata.setKey(args.get(0));
        }
        for (int i = 1; i < args.size(); i++) {
            newMetadata.addValue(args.get(i));
        }
        if (comment != null && !comment.isEmpty()) {
            newMetadata.setComment(comment);
        }
        return newMetadata;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final Metadata metadata = (Metadata) modelElement;
        if (index == 0) {
            metadata.setKey(value);
        } else if (index > 0) {
            metadata.setValues(index - 1, value);
        }
    }

    @Override
    public void remove(final SettingTable settingsTable, final AModelElement<?> modelElement) {
        settingsTable.removeMetadata((Metadata) modelElement);
    }
    
    private boolean isValidTableIndex(final SettingTable settingsTable, final int tableIndex) {
        return tableIndex >= 0 && tableIndex < settingsTable.getMetadatas().size();
    }
}
