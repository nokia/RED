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
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.SuiteSetup;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class SuiteSetupKeywordArgumentMapper implements IParsingMapper {

    protected final ElementsUtility utility = new ElementsUtility();

    private final ParsingStateHelper stateHelper = new ParsingStateHelper();

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isNewerOrEqualTo(new RobotVersion(3, 0));
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        final ParsingState state = stateHelper.getCurrentStatus(processingState);
        if (state == ParsingState.SETTING_SUITE_SETUP) {
            final List<SuiteSetup> suiteSetups = robotFileOutput.getFileModel().getSettingTable().getSuiteSetups();
            return canBeMappedTo(suiteSetups);
        }
        return state == ParsingState.SETTING_SUITE_SETUP_KEYWORD
                || state == ParsingState.SETTING_SUITE_SETUP_KEYWORD_ARGUMENT;
    }

    protected boolean canBeMappedTo(final List<SuiteSetup> suiteSetups) {
        return utility.checkIfLastHasKeywordNameAlready(suiteSetups);
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        final List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_ARGUMENT);
        types.remove(RobotTokenType.UNKNOWN);
        rt.setText(text);

        final SettingTable settings = robotFileOutput.getFileModel().getSettingTable();
        final List<SuiteSetup> setups = settings.getSuiteSetups();
        if (!setups.isEmpty()) {
            setups.get(setups.size() - 1).addArgument(rt);
        }

        processingState.push(ParsingState.SETTING_SUITE_SETUP_KEYWORD_ARGUMENT);
        return rt;
    }
}
