/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.setting.mapping.suite;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.model.table.setting.SuiteSetup;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class SuiteSetupKeywordMapper implements IParsingMapper {

    private final ElementsUtility utility;
    private final ParsingStateHelper stateHelper;


    public SuiteSetupKeywordMapper() {
        this.utility = new ElementsUtility();
        this.stateHelper = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.add(0, RobotTokenType.SETTING_SUITE_SETUP_KEYWORD_NAME);
        types.remove(RobotTokenType.UNKNOWN);
        rt.setText(text);
        rt.setRaw(text);

        final SettingTable settings = robotFileOutput.getFileModel()
                .getSettingTable();
        final List<SuiteSetup> setups = settings.getSuiteSetups();
        if (!setups.isEmpty()) {
            setups.get(setups.size() - 1).setKeywordName(rt);
        } else {
            // FIXME: some internal error
        }
        processingState.push(ParsingState.SETTING_SUITE_SETUP_KEYWORD);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = stateHelper.getCurrentStatus(processingState);

        if (state == ParsingState.SETTING_SUITE_SETUP) {
            final List<SuiteSetup> suiteSetups = robotFileOutput.getFileModel()
                    .getSettingTable().getSuiteSetups();
            result = !utility.checkIfHasAlreadyKeywordName(suiteSetups);
        }
        return result;
    }
}
