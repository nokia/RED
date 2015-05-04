package org.robotframework.ide.core.testData.parser.txt.table;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * Responsible for help to parse TXT table header i.e.: {@code *** Settings ***}
 * 
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class TxtTableHeaderHelper {

    /**
     * 
     * @param expectedMandatoryText
     * @param optionalTextAfterMandatory
     * @param data
     * @return
     */
    public TableHeader createTableHeader(String expectedMandatoryText,
            String optionalTextAfterMandatory, ByteBufferInputStream data) {
        if (expectedMandatoryText == null) {
            throw new IllegalArgumentException(
                    "Expected mandatory text to find, can't be null.");
        }
        char[] expectedCharsCommon = expectedMandatoryText.toLowerCase()
                .toCharArray();

        char[] optionalChars;
        if (optionalTextAfterMandatory == null) {
            optionalChars = new char[0];
        } else {
            optionalChars = optionalTextAfterMandatory.toLowerCase()
                    .toCharArray();
        }

        TableHeaderStateHandlerFactory stateFactory = new TableHeaderStateHandlerFactory();
        TableHeader header = new TableHeader(expectedCharsCommon, optionalChars);
        while(data.available() > 0) {
            char currentChar = (char) data.read();
            LoopNextStep nextStep = LoopNextStep.MOVE_TO_NEXT;
            while(nextStep == LoopNextStep.MOVE_TO_NEXT) {
                nextStep = stateFactory.getHandlerBaseOn(header.state)
                        .performAction(header, currentChar);
            }

            if (nextStep == LoopNextStep.LOOP_CONTINUE) {
                continue;
            } else if (nextStep == LoopNextStep.LOOP_BREAK) {
                break;
            }
        }

        return header;
    }

    /**
     * Container of information collected by helper during parsing TXT table
     * headers
     * 
     * @author wypych
     * @serial RobotFramework 2.8.6
     * @serial 1.0
     * 
     */
    public class TableHeader {

        // optional [condition ~ 1]
        private int numberOfStartPipes = 0;
        // conditional in case [condition ~ 1] is meet then mandatory
        private StringBuilder separatorsAfterPipes = new StringBuilder();
        // mandatory - minimum 1 expected
        private int numberOfBeginAsterisks = 0;
        // optional
        private StringBuilder separatorsAfterBeginAsterisks = new StringBuilder();
        // mandatory
        private StringBuilder expectedText = new StringBuilder();
        // optional in example it could be last letters in [expectedText] like
        // 's' in Settings table
        private StringBuilder optionalChars = new StringBuilder();
        // after it could be just 2 spaces separator, tabulator or end of
        // stream, new line [condition ~ 1] should be checked
        private StringBuilder optionalEndBeforeEndAsteriskOrSeparator = new StringBuilder();

        // in case no optional end:
        //
        //
        // mandatory
        private int numberOfEndAsteriks = 0;

        // optional end - it could be just 2 spaces separator, tabulator or end
        // of stream, new line [condition ~ 1] should be checked
        private StringBuilder optionalEndOrSeparatorAfterEndAsterisks = new StringBuilder();
        // conditional in case [condition ~ 1] is met
        private int numberOfEndPipes = 0;
        // final end of stream, 2 space separator, tabulator or new line
        private StringBuilder endOrSeperator = new StringBuilder();

        private ParsingHeaderState state = ParsingHeaderState.BEGIN_PIPES;

        private final char[] expectedCharsCommonArray;
        private final char[] optionalCharsArray;


        public TableHeader(final char[] expectedCharsCommon,
                final char[] optionalChars) {
            this.expectedCharsCommonArray = expectedCharsCommon;
            this.optionalCharsArray = optionalChars;
        }


        public int getNumberOfStartPipes() {
            return numberOfStartPipes;
        }


        public String getAfterPipeSeparators() {
            return separatorsAfterPipes.toString();
        }


        public int getNumberOfBeginAsterisks() {
            return numberOfBeginAsterisks;
        }


        public String getSeparatorsAfterBeginAsterisks() {
            return separatorsAfterBeginAsterisks.toString();
        }


        public String getExpectedText() {
            return expectedText.toString();
        }


        public String getOptionalChars() {
            return optionalChars.toString();
        }


        public String getOptionalEndBeforeEndAsterisksOrSeparator() {
            return optionalEndBeforeEndAsteriskOrSeparator.toString();
        }


        public int getNumberOfEndAsteriks() {
            return numberOfEndAsteriks;
        }


        public String getOptionalEndOrSeparatorAfterEndAsterisks() {
            return optionalEndOrSeparatorAfterEndAsterisks.toString();
        }


        public int getNumberOfEndPipes() {
            return numberOfEndPipes;
        }


        public String getEndOrSeperator() {
            return endOrSeperator.toString();
        }


        public boolean computeFinalResult() {
            boolean shouldParse = false;
            if (state == ParsingHeaderState.MEET_OK) {
                shouldParse = true;
            } else if (state == ParsingHeaderState.MEET_FAILED) {
                shouldParse = false;
            } else {
                if (state == ParsingHeaderState.EXPECTED_TEXT) {
                    shouldParse = (this.expectedText.length() == expectedCharsCommonArray.length);
                } else if (state == ParsingHeaderState.TABLE_END_ASTERISKS) {
                    shouldParse = (numberOfEndAsteriks > 0);
                } else if (state == ParsingHeaderState.OPTIONAL_TEXT) {
                    shouldParse = (optionalChars.length() == optionalCharsArray.length);
                }
            }

            return shouldParse;
        }
    }

    private class TableHeaderStateHandlerFactory {

        private final Map<ParsingHeaderState, IStateHandler> possibleHandlers = new LinkedHashMap<ParsingHeaderState, IStateHandler>();


        private TableHeaderStateHandlerFactory() {
            BeginPipes beginPipeHandler = new BeginPipes();
            SeparatorsAfterBeginPipes sepAfterBeginPipes = new SeparatorsAfterBeginPipes();
            TableBeginAsterisks tableBeginAsterisks = new TableBeginAsterisks();
            SeparatorAfterBeginAsterisks sepAfterBeginAsteriks = new SeparatorAfterBeginAsterisks();
            ExpectedTextHandler expectedText = new ExpectedTextHandler();
            OptionalTextHandler optionalText = new OptionalTextHandler();
            OptionalEndOfParsing optEndOfParsing = new OptionalEndOfParsing();
            TableEndAsterisks tableEndAsterisks = new TableEndAsterisks();
            OptionalEndBeforeEndPipes optEndOrSeparator = new OptionalEndBeforeEndPipes();
            ConditionalEndPipes optEndPipes = new ConditionalEndPipes();
            LastSeparatorOrEndStream end = new LastSeparatorOrEndStream();

            possibleHandlers.put(beginPipeHandler.getState(), beginPipeHandler);
            possibleHandlers.put(sepAfterBeginPipes.getState(),
                    sepAfterBeginPipes);
            possibleHandlers.put(tableBeginAsterisks.getState(),
                    tableBeginAsterisks);
            possibleHandlers.put(sepAfterBeginAsteriks.getState(),
                    sepAfterBeginAsteriks);
            possibleHandlers.put(expectedText.getState(), expectedText);
            possibleHandlers.put(optionalText.getState(), optionalText);
            possibleHandlers.put(optEndOfParsing.getState(), optEndOfParsing);
            possibleHandlers.put(tableEndAsterisks.getState(),
                    tableEndAsterisks);
            possibleHandlers.put(optEndOrSeparator.getState(),
                    optEndOrSeparator);
            possibleHandlers.put(optEndPipes.getState(), optEndPipes);
            possibleHandlers.put(end.getState(), end);
        }


        public IStateHandler getHandlerBaseOn(ParsingHeaderState state) {
            return possibleHandlers.get(state);
        }

    }

    private class LastSeparatorOrEndStream implements IStateHandler {

        @Override
        public ParsingHeaderState getState() {
            return ParsingHeaderState.LAST_SEPARATOR;
        }


        @Override
        public LoopNextStep performAction(TableHeader header, char currentChar) {
            LoopNextStep nextStep = null;
            if (currentChar == '\t') {
                header.endOrSeperator.append(currentChar);
                header.state = ParsingHeaderState.MEET_OK;
                nextStep = LoopNextStep.LOOP_BREAK;
            } else if (currentChar == ' ') {
                header.endOrSeperator.append(currentChar);
                if (header.endOrSeperator.length() == 2) {
                    header.state = ParsingHeaderState.MEET_OK;
                    nextStep = LoopNextStep.LOOP_BREAK;
                } else {
                    nextStep = LoopNextStep.LOOP_CONTINUE;
                }
            } else if (currentChar == '\r') {
                header.endOrSeperator.append(currentChar);
                nextStep = LoopNextStep.LOOP_CONTINUE;
            } else if (currentChar == '\n') {
                header.endOrSeperator.append(currentChar);
                header.state = ParsingHeaderState.MEET_OK;
                nextStep = LoopNextStep.LOOP_BREAK;
            } else {
                header.state = ParsingHeaderState.MEET_FAILED;
                nextStep = LoopNextStep.LOOP_BREAK;
            }

            return nextStep;
        }
    }

    private class ConditionalEndPipes implements IStateHandler {

        @Override
        public ParsingHeaderState getState() {
            return ParsingHeaderState.CONDITIONAL_END_PIPES;
        }


        @Override
        public LoopNextStep performAction(TableHeader header, char currentChar) {
            LoopNextStep nextStep = null;
            if (currentChar == '|') {
                header.numberOfEndPipes++;
                nextStep = LoopNextStep.LOOP_CONTINUE;
            } else {
                header.state = getState().nextParserState();
                nextStep = LoopNextStep.MOVE_TO_NEXT;
            }

            return nextStep;
        }
    }

    private class OptionalEndBeforeEndPipes implements IStateHandler {

        @Override
        public ParsingHeaderState getState() {
            return ParsingHeaderState.OPTIONAL_END_OR_SEPARATOR;
        }


        @Override
        public LoopNextStep performAction(TableHeader header, char currentChar) {
            LoopNextStep nextStep = null;
            if (currentChar == '\t') {
                header.optionalEndOrSeparatorAfterEndAsterisks
                        .append(currentChar);
                header.state = ParsingHeaderState.MEET_OK;
                nextStep = LoopNextStep.LOOP_BREAK;
            } else if (currentChar == ' ') {
                header.optionalEndOrSeparatorAfterEndAsterisks
                        .append(currentChar);
                if (header.optionalEndOrSeparatorAfterEndAsterisks.length() == 2) {
                    header.state = ParsingHeaderState.MEET_OK;
                    nextStep = LoopNextStep.LOOP_BREAK;
                } else {
                    nextStep = LoopNextStep.LOOP_CONTINUE;
                }
            } else if (currentChar == '\r') {
                header.optionalEndOrSeparatorAfterEndAsterisks
                        .append(currentChar);
                nextStep = LoopNextStep.LOOP_CONTINUE;
            } else if (currentChar == '\n') {
                header.optionalEndOrSeparatorAfterEndAsterisks
                        .append(currentChar);
                header.state = ParsingHeaderState.MEET_OK;
                nextStep = LoopNextStep.LOOP_BREAK;
            } else {
                if (header.numberOfStartPipes > 0) {
                    header.state = ParsingHeaderState.CONDITIONAL_END_PIPES;
                } else {
                    header.state = ParsingHeaderState.LAST_SEPARATOR;
                }

                nextStep = LoopNextStep.MOVE_TO_NEXT;
            }

            return nextStep;
        }
    }

    private class TableEndAsterisks implements IStateHandler {

        @Override
        public ParsingHeaderState getState() {
            return ParsingHeaderState.TABLE_END_ASTERISKS;
        }


        @Override
        public LoopNextStep performAction(TableHeader header, char currentChar) {
            LoopNextStep nextStep = null;
            if (currentChar == '*') {
                header.numberOfEndAsteriks++;
                nextStep = LoopNextStep.LOOP_CONTINUE;
            } else {
                header.state = getState().nextParserState();
                nextStep = LoopNextStep.MOVE_TO_NEXT;
            }

            return nextStep;
        }
    }

    private class OptionalEndOfParsing implements IStateHandler {

        @Override
        public ParsingHeaderState getState() {
            return ParsingHeaderState.OPTIONAL_END_OF_STREAM;
        }


        @Override
        public LoopNextStep performAction(TableHeader header, char currentChar) {
            LoopNextStep nextStep = null;
            if (currentChar == '\t') {
                header.optionalEndBeforeEndAsteriskOrSeparator
                        .append(currentChar);
                header.state = ParsingHeaderState.MEET_OK;
                nextStep = LoopNextStep.LOOP_BREAK;
            } else if (currentChar == ' ') {
                header.optionalEndBeforeEndAsteriskOrSeparator
                        .append(currentChar);
                if (header.optionalEndBeforeEndAsteriskOrSeparator.length() == 2) {
                    header.state = ParsingHeaderState.MEET_OK;
                    nextStep = LoopNextStep.LOOP_BREAK;
                } else {
                    nextStep = LoopNextStep.LOOP_CONTINUE;
                }
            } else if (currentChar == '\r') {
                header.optionalEndBeforeEndAsteriskOrSeparator
                        .append(currentChar);
                nextStep = LoopNextStep.LOOP_CONTINUE;
            } else if (currentChar == '\n') {
                header.optionalEndBeforeEndAsteriskOrSeparator
                        .append(currentChar);
                header.state = ParsingHeaderState.MEET_OK;
                nextStep = LoopNextStep.LOOP_BREAK;
            } else {
                header.state = getState().nextParserState();
                nextStep = LoopNextStep.MOVE_TO_NEXT;
            }

            return nextStep;
        }

    }

    private class OptionalTextHandler implements IStateHandler {

        @Override
        public ParsingHeaderState getState() {
            return ParsingHeaderState.OPTIONAL_TEXT;
        }


        @Override
        public LoopNextStep performAction(TableHeader header, char currentChar) {
            LoopNextStep nextStep = null;
            if (header.optionalCharsArray.length > 0) {
                if (header.optionalChars.length() == 0) {
                    if (Character.toLowerCase(currentChar) == header.optionalCharsArray[0]) {
                        header.optionalChars.append(currentChar);
                        nextStep = LoopNextStep.LOOP_CONTINUE;
                    } else {
                        header.state = getState().nextParserState();
                        nextStep = LoopNextStep.MOVE_TO_NEXT;
                    }
                } else if (header.optionalCharsArray.length < header.optionalChars
                        .length()) {
                    if (Character.toLowerCase(currentChar) == header.optionalCharsArray[header.optionalChars
                            .length()]) {
                        header.optionalChars.append(currentChar);
                        nextStep = LoopNextStep.LOOP_CONTINUE;
                    } else {
                        header.state = ParsingHeaderState.MEET_FAILED;
                        nextStep = LoopNextStep.LOOP_BREAK;
                    }
                } else if (header.optionalCharsArray.length == header.optionalChars
                        .length()) {
                    header.state = getState().nextParserState();
                    nextStep = LoopNextStep.MOVE_TO_NEXT;
                }
            } else {
                header.state = getState().nextParserState();
                nextStep = LoopNextStep.MOVE_TO_NEXT;
            }

            return nextStep;
        }

    }

    private class ExpectedTextHandler implements IStateHandler {

        @Override
        public ParsingHeaderState getState() {
            return ParsingHeaderState.EXPECTED_TEXT;
        }


        @Override
        public LoopNextStep performAction(TableHeader header, char currentChar) {
            LoopNextStep nextStep = null;
            int collectedTextLength = header.expectedText.length();
            int expectedTextLength = header.expectedCharsCommonArray.length;

            if (collectedTextLength < expectedTextLength) {
                if (Character.toLowerCase(currentChar) == header.expectedCharsCommonArray[collectedTextLength]) {
                    header.expectedText.append(currentChar);
                    nextStep = LoopNextStep.LOOP_CONTINUE;
                } else {
                    header.state = ParsingHeaderState.MEET_FAILED;
                    nextStep = LoopNextStep.LOOP_BREAK;
                }
            } else if (collectedTextLength == expectedTextLength) {
                header.state = getState().nextParserState();
                nextStep = LoopNextStep.MOVE_TO_NEXT;
            }

            return nextStep;
        }
    }

    private class SeparatorAfterBeginAsterisks implements IStateHandler {

        @Override
        public ParsingHeaderState getState() {
            return ParsingHeaderState.SEPARATORS_AFTER_BEGIN_ASTERISKS;
        }


        @Override
        public LoopNextStep performAction(TableHeader header, char currentChar) {
            LoopNextStep nextStep = null;
            if (currentChar == ' ' || currentChar == '\t') {
                header.separatorsAfterBeginAsterisks.append(currentChar);
                nextStep = LoopNextStep.LOOP_CONTINUE;
            } else {
                header.state = getState().nextParserState();
                nextStep = LoopNextStep.MOVE_TO_NEXT;
            }

            return nextStep;
        }
    }

    private class TableBeginAsterisks implements IStateHandler {

        @Override
        public ParsingHeaderState getState() {
            return ParsingHeaderState.TABLE_BEGIN_ASTERISKS;
        }


        @Override
        public LoopNextStep performAction(TableHeader header, char currentChar) {
            LoopNextStep nextStep = null;
            if (currentChar == '*') {
                header.numberOfBeginAsterisks++;
                nextStep = LoopNextStep.LOOP_CONTINUE;
            } else {
                if (header.numberOfBeginAsterisks > 0) {
                    header.state = getState().nextParserState();
                    nextStep = LoopNextStep.MOVE_TO_NEXT;
                } else {
                    header.state = ParsingHeaderState.MEET_FAILED;
                    nextStep = LoopNextStep.LOOP_BREAK;
                }
            }

            return nextStep;
        }
    }

    private class SeparatorsAfterBeginPipes implements IStateHandler {

        @Override
        public ParsingHeaderState getState() {
            return ParsingHeaderState.SEPARATORS_AFTER_BEGIN_PIPES;
        }


        @Override
        public LoopNextStep performAction(TableHeader header, char currentChar) {
            LoopNextStep nextStep = null;
            if (currentChar == ' ' || currentChar == '\t') {
                header.separatorsAfterPipes.append(currentChar);
            } else {
                header.state = getState().nextParserState();
                nextStep = LoopNextStep.MOVE_TO_NEXT;
            }

            return nextStep;
        }
    }

    private class BeginPipes implements IStateHandler {

        @Override
        public ParsingHeaderState getState() {
            return ParsingHeaderState.BEGIN_PIPES;
        }


        @Override
        public LoopNextStep performAction(TableHeader header, char currentChar) {
            LoopNextStep toReturn = null;
            if (currentChar == '|') {
                header.numberOfBeginAsterisks++;
                toReturn = LoopNextStep.LOOP_CONTINUE;
            } else {
                header.state = getState().nextParserState();
                toReturn = LoopNextStep.MOVE_TO_NEXT;
            }

            return toReturn;
        }
    }

    private interface IStateHandler {

        ParsingHeaderState getState();


        LoopNextStep performAction(TableHeader header, char currentChar);
    }

    private enum LoopNextStep {
        LOOP_CONTINUE, LOOP_BREAK, MOVE_TO_NEXT
    }

    private enum ParsingHeaderState {
        BEGIN_PIPES(1), SEPARATORS_AFTER_BEGIN_PIPES(2), TABLE_BEGIN_ASTERISKS(
                3), SEPARATORS_AFTER_BEGIN_ASTERISKS(4), EXPECTED_TEXT(5), OPTIONAL_TEXT(
                6), OPTIONAL_END_OF_STREAM(7), TABLE_END_ASTERISKS(8), OPTIONAL_END_OR_SEPARATOR(
                9), CONDITIONAL_END_PIPES(10), LAST_SEPARATOR(11), THROWN_END_CAUSE_ERROR(
                12), MEET_FAILED(Integer.MIN_VALUE), MEET_OK(Integer.MAX_VALUE);

        private final int positionInParsingChain;
        private static final Map<Integer, ParsingHeaderState> statuses = new HashMap<Integer, ParsingHeaderState>();

        static {
            ParsingHeaderState[] states = ParsingHeaderState.values();
            for (ParsingHeaderState state : states) {
                statuses.put(state.positionInParsingChain, state);
            }
        }


        private ParsingHeaderState(final int positionInParsingChain) {
            this.positionInParsingChain = positionInParsingChain;
        }


        public ParsingHeaderState nextParserState() {
            if (this == THROWN_END_CAUSE_ERROR) {
                throw new RuntimeException("Parsing should stopped here");
            }
            return statuses.get(this.positionInParsingChain + 1);
        }
    }
}
