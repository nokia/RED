/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.testcases;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.LocalSettingDeclarationMapper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.ParsingState.TableType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseSettingDeclarationMapper extends LocalSettingDeclarationMapper {

    private final ParsingState newParsingState;

    private final ModelType settingModelType;

    public TestCaseSettingDeclarationMapper(final RobotTokenType declarationType, final ParsingState newParsingState,
            final ModelType settingModelType) {
        super(declarationType, TableType.TEST_CASE, RobotTokenType.TEST_CASE_NAME);
        this.newParsingState = newParsingState;
        this.settingModelType = settingModelType;
    }

    @Override
    public final RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, declarationType);
        rt.setText(text);

        final TestCase testCase = new TestCaseFinder().findOrCreateNearestTestCase(currentLine, robotFileOutput);
        addElement(rt, testCase);

        processingState.push(newParsingState);
        return rt;
    }

    protected void addElement(final RobotToken rt, final TestCase testCase) {
        testCase.addElement(new LocalSetting<>(settingModelType, rt));
    }
}
