/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.resource;

import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.ElementsUtility;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class ResourceImportPathMapper implements IParsingMapper {

    private final ElementsUtility utility;

    public ResourceImportPathMapper() {
        this.utility = new ElementsUtility();
    }

    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        rt.getTypes().add(0, RobotTokenType.SETTING_RESOURCE_FILE_NAME);
        rt.setText(text);
        final AImported imported = utility.getNearestImport(robotFileOutput);
        ResourceImport resource;
        if (imported instanceof ResourceImport) {
            resource = (ResourceImport) imported;
        } else {
            resource = null;

            // FIXME: sth wrong - declaration of variables not inside setting
            // and
            // was not catch by previous variables declaration logic
        }
        resource.setPathOrName(rt);

        processingState.push(ParsingState.SETTING_RESOURCE_IMPORT_PATH);
        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            result = (processingState.get(processingState.size() - 1) == ParsingState.SETTING_RESOURCE_IMPORT);
        } else {
            result = false;
        }

        return result;
    }
}
