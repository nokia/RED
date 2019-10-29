/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.imports;

import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.ElementsUtility;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class LibraryArgumentsMapper implements IParsingMapper {

    private final ElementsUtility utility = new ElementsUtility();

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {
        rt.getTypes().add(0, RobotTokenType.SETTING_LIBRARY_ARGUMENT);
        rt.setText(text);

        final LibraryImport lib = utility.getCurrentImport(robotFileOutput, LibraryImport.class)
                .orElseThrow(IllegalStateException::new);
        lib.addArgument(rt);

        processingState.push(ParsingState.SETTING_LIBRARY_ARGUMENTS);
        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            final ParsingState currentState = stateHelper.getCurrentState(processingState);
            if (currentState == ParsingState.SETTING_LIBRARY_NAME_OR_PATH
                    || currentState == ParsingState.SETTING_LIBRARY_ARGUMENTS) {
                if (rt.getTypes().contains(RobotTokenType.SETTING_LIBRARY_ALIAS)) {
                    result = false;
                } else {
                    result = true;
                }
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        return result;
    }

}
