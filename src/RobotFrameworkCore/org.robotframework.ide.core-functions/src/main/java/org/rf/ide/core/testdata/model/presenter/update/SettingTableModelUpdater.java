/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SettingTableModelUpdater {

    private final List<ISettingTableElementOperation> elementUpdaters = new ArrayList<>(0);

    public ISettingTableElementOperation getOperationHandler(final String settingName) {
        return getOperationHandler(RobotTokenType.findTypeOfDeclarationForSettingTable(settingName));
    }

    public ISettingTableElementOperation getOperationHandler(final AModelElement<?> elem) {
        ISettingTableElementOperation oper = null;

        for (final ISettingTableElementOperation cOper : elementUpdaters) {
            if (cOper.isApplicable(elem.getModelType())) {
                oper = cOper;
                break;
            }
        }

        return oper;
    }

    public ISettingTableElementOperation getOperationHandler(final IRobotTokenType type) {
        ISettingTableElementOperation oper = null;

        for (final ISettingTableElementOperation cOper : elementUpdaters) {
            if (cOper.isApplicable(type)) {
                oper = cOper;
                break;
            }
        }

        return oper;
    }
}
