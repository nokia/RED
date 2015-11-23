/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testData.model.table.setting.mapping.variables;

import java.util.Stack;

import org.rf.ide.core.testData.model.FilePosition;
import org.rf.ide.core.testData.model.RobotFileOutput;
import org.rf.ide.core.testData.model.table.mapping.ElementsUtility;
import org.rf.ide.core.testData.model.table.mapping.IParsingMapper;
import org.rf.ide.core.testData.model.table.setting.AImported;
import org.rf.ide.core.testData.model.table.setting.VariablesImport;
import org.rf.ide.core.testData.text.read.ParsingState;
import org.rf.ide.core.testData.text.read.RobotLine;
import org.rf.ide.core.testData.text.read.recognizer.RobotToken;
import org.rf.ide.core.testData.text.read.recognizer.RobotTokenType;


public class VariablesImportPathMapper implements IParsingMapper {

    private final ElementsUtility utility;


    public VariablesImportPathMapper() {
        this.utility = new ElementsUtility();
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        rt.getTypes().add(0, RobotTokenType.SETTING_VARIABLES_FILE_NAME);
        rt.setText(text);
        rt.setRaw(text);

        final AImported imported = utility.getNearestImport(robotFileOutput);
        VariablesImport vars;
        if (imported instanceof VariablesImport) {
            vars = (VariablesImport) imported;
        } else {
            vars = null;

            // FIXME: sth wrong - declaration of variables not inside setting
            // and
            // was not catch by previous variables declaration logic
        }
        vars.setPathOrName(rt);

        processingState.push(ParsingState.SETTING_VARIABLE_IMPORT_PATH);
        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            result = (processingState.get(processingState.size() - 1) == ParsingState.SETTING_VARIABLE_IMPORT);
        } else {
            result = false;
        }

        return result;
    }
}
