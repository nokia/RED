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
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class ResourceImportModelOperation implements ISettingTableElementOperation {

    @Override
    public boolean isApplicable(final IRobotTokenType elementType) {
        return (elementType == RobotTokenType.SETTING_RESOURCE_DECLARATION);
    }

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return (elementType == ModelType.RESOURCE_IMPORT_SETTING);
    }

    @Override
    public AModelElement<?> create(final SettingTable settingsTable, final List<String> args, final String comment) {
        final ResourceImport newResourceImport = settingsTable.newResourceImport();
        if (!args.isEmpty()) {
            newResourceImport.setPathOrName(args.get(0));
        }
        for (int i = 1; i < args.size(); i++) {
            newResourceImport.addUnexpectedTrashArgument(args.get(i));
        }
        if (comment != null && !comment.isEmpty()) {
            newResourceImport.setComment(comment);
        }
        return newResourceImport;
    }

    @Override
    public void update(final AModelElement<?> modelElement, final int index, final String value) {
        final ResourceImport resourceImport = (ResourceImport) modelElement;
        if (index == 0) {
            resourceImport.setPathOrName(value);
        } else if (index > 0) {
            resourceImport.setUnexpectedTrashArguments(index - 1, value);
        } else {
            resourceImport.setComment(value);
        }
    }

    @Override
    public void remove(final SettingTable settingsTable, final AModelElement<?> modelElement) {
        settingsTable.removeImported((AImported) modelElement);
    }
}
