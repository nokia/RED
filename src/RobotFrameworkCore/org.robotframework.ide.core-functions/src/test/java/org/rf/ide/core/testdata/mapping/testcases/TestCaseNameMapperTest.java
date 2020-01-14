/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.testcases;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Stack;

import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class TestCaseNameMapperTest {

    @Test
    public void whenMapped_newTestIsCreatedWithSingleTokenTypeAndParsingStateIsUpdated_inRf31() {
        final TestCaseNameMapper mapper = new TestCaseNameMapper();

        final RobotFileOutput output = createOutputModel(new RobotVersion(3, 1));
        final Stack<ParsingState> states = stack(ParsingState.TEST_CASE_TABLE_INSIDE);
        final RobotToken token = mapper.map(null, states, output, RobotToken.create("", RobotTokenType.VARIABLE_USAGE),
                null, "test ${var}");

        final TestCase test = getTest(output);
        assertThat(test.getDeclaration()).isSameAs(token);
        assertThat(token.getTypes()).containsOnly(RobotTokenType.TEST_CASE_NAME);
        assertThat(token.getText()).isEqualTo("test ${var}");

        assertThat(states).containsExactly(ParsingState.TEST_CASE_TABLE_INSIDE, ParsingState.TEST_CASE_DECLARATION);
    }

    @Test
    public void whenMapped_newTestIsCreatedWithPreviousTokenTypesAndParsingStateIsUpdated_inRf32() {
        final TestCaseNameMapper mapper = new TestCaseNameMapper();

        final RobotFileOutput output = createOutputModel(new RobotVersion(3, 2));
        final Stack<ParsingState> states = stack(ParsingState.TEST_CASE_TABLE_INSIDE);
        final RobotToken token = mapper.map(null, states, output, RobotToken.create("", RobotTokenType.VARIABLE_USAGE),
                null, "test ${var}");

        final TestCase test = getTest(output);
        assertThat(test.getDeclaration()).isSameAs(token);
        assertThat(token.getTypes()).containsOnly(RobotTokenType.TEST_CASE_NAME, RobotTokenType.VARIABLE_USAGE);
        assertThat(token.getText()).isEqualTo("test ${var}");

        assertThat(states).containsExactly(ParsingState.TEST_CASE_TABLE_INSIDE, ParsingState.TEST_CASE_DECLARATION);
    }

    private static Stack<ParsingState> stack(final ParsingState... states) {
        final Stack<ParsingState> statesStack = new Stack<>();
        for (final ParsingState state : states) {
            statesStack.push(state);
        }
        return statesStack;
    }

    private static final RobotFileOutput createOutputModel(final RobotVersion version) {
        final RobotFileOutput output = new RobotFileOutput(version);
        final RobotFile fileModel = output.getFileModel();
        fileModel.includeTestCaseTableSection();
        return output;
    }

    private TestCase getTest(final RobotFileOutput output) {
        return output.getFileModel().getTestCaseTable().getTestCases().get(0);
    }
}
