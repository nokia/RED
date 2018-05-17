/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting.suite;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.ElementsUtility;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteTeardown;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SuiteTeardownKeywordArgumentMapper implements IParsingMapper {

    private final ElementsUtility utility;
    private final ParsingStateHelper stateHelper;

    public SuiteTeardownKeywordArgumentMapper() {
        this.utility = new ElementsUtility();
        this.stateHelper = new ParsingStateHelper();
    }

    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT);
        types.remove(RobotTokenType.UNKNOWN);
        rt.setText(text);

        final SettingTable settings = robotFileOutput.getFileModel()
                .getSettingTable();
        final List<SuiteTeardown> teardowns = settings.getSuiteTeardowns();
        if (!teardowns.isEmpty()) {
            teardowns.get(teardowns.size() - 1).addArgument(rt);
        } else {
            // FIXME: some error
        }
        processingState
                .push(ParsingState.SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT);

        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = stateHelper.getCurrentStatus(processingState);
        if (state == ParsingState.SETTING_SUITE_TEARDOWN) {
            final List<SuiteTeardown> suiteTeardowns = robotFileOutput.getFileModel()
                    .getSettingTable().getSuiteTeardowns();
            result = utility.checkIfHasAlreadyKeywordName(suiteTeardowns);
        } else if (state == ParsingState.SETTING_SUITE_TEARDOWN_KEYWORD
                || state == ParsingState.SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT) {
            result = true;
        }

        return result;
    }

}
