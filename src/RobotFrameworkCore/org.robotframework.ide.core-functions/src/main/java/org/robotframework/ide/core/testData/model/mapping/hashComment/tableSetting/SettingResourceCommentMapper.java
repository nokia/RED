/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.mapping.hashComment.tableSetting;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.mapping.IHashCommentMapper;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.ResourceImport;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class SettingResourceCommentMapper implements IHashCommentMapper {

    @Override
    public boolean isApplicable(ParsingState state) {
        return (state == ParsingState.SETTING_RESOURCE_IMPORT);
    }


    @Override
    public void map(RobotToken rt, ParsingState currentState,
            RobotFile fileModel) {
        List<AImported> imports = fileModel.getSettingTable().getImports();
        if (!imports.isEmpty()) {
            AImported aImported = imports.get(imports.size() - 1);
            if (aImported instanceof ResourceImport) {
                ResourceImport res = (ResourceImport) aImported;
                res.addCommentPart(rt);
            } else {
                // FIXME: error
            }
        } else {
            // FIXME: errors
        }
    }

}
