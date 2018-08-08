/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Stack;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

@RunWith(Enclosed.class)
public class ParsingStateHelperTest {

    private static final ParsingStateHelper helper = new ParsingStateHelper();

    public static class IsTableInsideStateTest {

        private static final List<ParsingState> TABLE_INSIDE_STATES = asList(ParsingState.SETTING_TABLE_INSIDE,
                ParsingState.TEST_CASE_TABLE_INSIDE, ParsingState.TASKS_TABLE_INSIDE, ParsingState.KEYWORD_TABLE_INSIDE,
                ParsingState.VARIABLE_TABLE_INSIDE);

        @Test
        public void allTableInsideStatesAreRecognized() {
            final List<ParsingState> states = TABLE_INSIDE_STATES;
            assertThat(states).allSatisfy(state -> assertThat(helper.isTableInsideState(state)).isTrue());
        }

        @Test
        public void allNotTableInsideStatesAreRecognized() {
            final EnumSet<ParsingState> states = EnumSet.allOf(ParsingState.class);
            states.removeAll(TABLE_INSIDE_STATES);
            assertThat(states).allSatisfy(state -> assertThat(helper.isTableInsideState(state)).isFalse());
        }
    }

    public static class IsTableHeaderTest {

        private static final List<ParsingState> TABLE_HEADER_STATES = asList(ParsingState.SETTING_TABLE_HEADER,
                ParsingState.VARIABLE_TABLE_HEADER, ParsingState.TEST_CASE_TABLE_HEADER,
                ParsingState.TASKS_TABLE_HEADER, ParsingState.KEYWORD_TABLE_HEADER);

        @Test
        public void allTableHeaderStatesAreRecognized() {
            final List<ParsingState> states = TABLE_HEADER_STATES;
            assertThat(states).allSatisfy(state -> assertThat(helper.isTableHeaderState(state)).isTrue());
        }

        @Test
        public void allNotTableHeaderStatesAreRecognized() {
            final EnumSet<ParsingState> states = EnumSet.allOf(ParsingState.class);
            states.removeAll(TABLE_HEADER_STATES);
            assertThat(states).allSatisfy(state -> assertThat(helper.isTableHeaderState(state)).isFalse());
        }
    }

    public static class GetCurrentStateTest {

        @Test
        public void unknownStateIsReturned_whenProcessingStateIsEmpty() {
            final Stack<ParsingState> processingState = new Stack<>();
            final ParsingState state = helper.getCurrentState(processingState);
            assertThat(state).isEqualTo(ParsingState.UNKNOWN);
        }

        @Test
        public void topStateIsReturnedWithoutModifyingStack_whenProcessingStateIsNotEmpty() {
            final Stack<ParsingState> processingState = new Stack<>();
            processingState.addAll(asList(ParsingState.KEYWORD_EMPTY_LINE, ParsingState.TRASH,
                    ParsingState.SETTING_UNKNOWN, ParsingState.COMMENT, ParsingState.VARIABLE_TABLE_INSIDE));
            final ParsingState state = helper.getCurrentState(processingState);
            assertThat(state).isEqualTo(ParsingState.VARIABLE_TABLE_INSIDE);
            assertThat(processingState).hasSize(5);
        }
    }

    public static class GetStateTest {

        private static final List<RobotTokenType> TABLE_HEADER_TYPES = asList(RobotTokenType.KEYWORDS_TABLE_HEADER,
                RobotTokenType.SETTINGS_TABLE_HEADER, RobotTokenType.TEST_CASES_TABLE_HEADER,
                RobotTokenType.TASKS_TABLE_HEADER, RobotTokenType.VARIABLES_TABLE_HEADER);

        @Test
        public void unknownStateIsReturned_whenTokenDoesNotContainTableHeaderTypes() {
            final Collection<RobotTokenType> types = EnumSet.allOf(RobotTokenType.class);
            types.removeAll(TABLE_HEADER_TYPES);
            final List<ParsingState> states = types.stream()
                    .map(type -> RobotToken.create("", asList(type)))
                    .map(helper::getState)
                    .collect(toList());
            assertThat(states).containsOnly(ParsingState.UNKNOWN);
        }

        @Test
        public void tableHeaderStateIsReturned_whenTokenContainsTableHeaderTypes() {
            final Collection<RobotTokenType> types = TABLE_HEADER_TYPES;
            final List<ParsingState> states = types.stream()
                    .map(type -> RobotToken.create("", asList(type)))
                    .map(helper::getState)
                    .collect(toList());
            assertThat(states).containsExactly(ParsingState.KEYWORD_TABLE_HEADER, ParsingState.SETTING_TABLE_HEADER,
                    ParsingState.TEST_CASE_TABLE_HEADER, ParsingState.TASKS_TABLE_HEADER,
                    ParsingState.VARIABLE_TABLE_HEADER);
        }
    }

    public static class GetFirstTableHeaderStateTest {

        @Test
        public void unknownStateIsReturned_whenProcessingStateIsEmpty() {
            final Stack<ParsingState> processingState = new Stack<>();
            final ParsingState state = helper.getFirstTableHeaderState(processingState);
            assertThat(state).isEqualTo(ParsingState.UNKNOWN);
        }

        @Test
        public void unknownStateIsReturned_whenProcessingStateDoesNotContainTableHeaderStates() {
            final Stack<ParsingState> processingState = new Stack<>();
            processingState.addAll(asList(ParsingState.COMMENT, ParsingState.KEYWORD_EMPTY_LINE,
                    ParsingState.SETTING_UNKNOWN, ParsingState.TRASH));
            final ParsingState state = helper.getFirstTableHeaderState(processingState);
            assertThat(state).isEqualTo(ParsingState.UNKNOWN);
        }

        @Test
        public void firstTableHeaderStateIsReturned_whenProcessingStateContainsTableHeaderStates() {
            final Stack<ParsingState> processingState = new Stack<>();
            processingState.addAll(asList(ParsingState.COMMENT, ParsingState.VARIABLE_TABLE_HEADER,
                    ParsingState.SETTING_UNKNOWN, ParsingState.KEYWORD_TABLE_HEADER, ParsingState.TRASH));
            final ParsingState state = helper.getFirstTableHeaderState(processingState);
            assertThat(state).isEqualTo(ParsingState.VARIABLE_TABLE_HEADER);
        }
    }

    public static class GetLastNotCommentStateTest {

        @Test
        public void unknownStateIsReturned_whenProcessingStateIsEmpty() {
            final Stack<ParsingState> processingState = new Stack<>();
            final ParsingState state = helper.getLastNotCommentState(processingState);
            assertThat(state).isEqualTo(ParsingState.UNKNOWN);
        }

        @Test
        public void unknownStateIsReturned_whenProcessingStateContainsOnlyCommentStates() {
            final Stack<ParsingState> processingState = new Stack<>();
            processingState.addAll(asList(ParsingState.COMMENT, ParsingState.COMMENT, ParsingState.COMMENT));
            final ParsingState state = helper.getLastNotCommentState(processingState);
            assertThat(state).isEqualTo(ParsingState.UNKNOWN);
        }

        @Test
        public void lastNotCommentStateIsReturned_whenProcessingStateContainsNotCommentStates() {
            final Stack<ParsingState> processingState = new Stack<>();
            processingState.addAll(asList(ParsingState.COMMENT, ParsingState.TRASH, ParsingState.COMMENT,
                    ParsingState.KEYWORD_TABLE_HEADER, ParsingState.SETTING_UNKNOWN, ParsingState.COMMENT));
            final ParsingState state = helper.getLastNotCommentState(processingState);
            assertThat(state).isEqualTo(ParsingState.SETTING_UNKNOWN);
        }
    }

}
