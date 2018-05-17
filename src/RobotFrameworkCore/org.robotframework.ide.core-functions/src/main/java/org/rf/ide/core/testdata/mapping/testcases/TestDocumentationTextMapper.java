/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.testcases;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestDocumentationTextMapper implements IParsingMapper {

    private final ParsingStateHelper utility;

    public TestDocumentationTextMapper() {
        this.utility = new ParsingStateHelper();
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION_TEXT);

        rt.setText(text);
        final List<TestCase> testCases = robotFileOutput.getFileModel().getTestCaseTable().getTestCases();
        final TestCase testCase = testCases.get(testCases.size() - 1);
        final List<TestDocumentation> documentations = testCase.getDocumentation();
        if (documentations.size() == 1) {
            documentations.get(0).addDocumentationText(rt);
        } else {
            for (final TestDocumentation doc : documentations) {
                if (!doc.getDocumentationText().isEmpty()) {
                    doc.addDocumentationText(rt);
                    break;
                }
            }
        }
        processingState.push(ParsingState.TEST_CASE_SETTING_DOCUMENTATION_TEXT);

        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {
        boolean result = false;
        final ParsingState state = utility.getCurrentStatus(processingState);
        if (state == ParsingState.TEST_CASE_SETTING_DOCUMENTATION_DECLARATION
                || state == ParsingState.TEST_CASE_SETTING_DOCUMENTATION_TEXT) {
            result = true;
        }

        return result;
    }

}
