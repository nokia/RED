/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.setting.mapping.test;

import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.ParsingStateHelper;
import org.robotframework.ide.core.testData.model.table.setting.TestTimeout;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class TestTimeoutValueMapper implements IParsingMapper {

    private final ParsingStateHelper utility;


    public TestTimeoutValueMapper() {
        this.utility = new ParsingStateHelper();
    }


    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        rt.getTypes().add(0, RobotTokenType.SETTING_TEST_TIMEOUT_VALUE);
        rt.setText(text);
        rt.setRaw(text);

        final SettingTable settings = robotFileOutput.getFileModel()
                .getSettingTable();
        final List<TestTimeout> timeouts = settings.getTestTimeouts();
        if (!timeouts.isEmpty()) {
            timeouts.get(timeouts.size() - 1).setTimeout(rt);
        } else {
            // FIXME: some internal error
        }
        processingState.push(ParsingState.SETTING_TEST_TIMEOUT_VALUE);

        return rt;
    }


    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = utility.getCurrentStatus(processingState);

        if (state == ParsingState.SETTING_TEST_TIMEOUT) {
            final List<TestTimeout> testTimeouts = robotFileOutput.getFileModel()
                    .getSettingTable().getTestTimeouts();
            result = !checkIfHasAlreadyValue(testTimeouts);
        }

        return result;
    }


    @VisibleForTesting
    protected boolean checkIfHasAlreadyValue(final List<TestTimeout> testTimeouts) {
        boolean result = false;
        for (final TestTimeout setting : testTimeouts) {
            result = (setting.getTimeout() != null);
            result = result || !setting.getMessageArguments().isEmpty();
            if (result) {
                break;
            }
        }

        return result;
    }
}
