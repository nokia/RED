/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.setting.mapping.resource;

import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.ResourceImport;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class ResourceTrashDataMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public ResourceTrashDataMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(RobotLine currentLine,
            Stack<ParsingState> processingState,
            RobotFileOutput robotFileOutput, RobotToken rt, FilePosition fp,
            String text) {
        rt.setType(RobotTokenType.SETTING_RESOURCE_UNWANTED_ARGUMENT);
        rt.setText(new StringBuilder(text));
        AImported imported = utility.getNearestImport(robotFileOutput);
        ResourceImport resource;
        if (imported instanceof ResourceImport) {
            resource = (ResourceImport) imported;
        } else {
            resource = null;

            // FIXME: sth wrong - declaration of library not inside setting and
            // was not catch by previous library declaration logic
        }

        resource.addUnexpectedTrashArgument(rt);

        processingState.push(ParsingState.SETTING_RESOURCE_UNWANTED_ARGUMENTS);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(RobotFileOutput robotFileOutput,
            RobotLine currentLine, RobotToken rt, String text,
            Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            ParsingState currentState = utility
                    .getCurrentStatus(processingState);
            if (currentState == ParsingState.SETTING_RESOURCE_IMPORT_PATH
                    || currentState == ParsingState.SETTING_RESOURCE_UNWANTED_ARGUMENTS) {
                result = true;
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }

}
