/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.executableDescriptors.ForDescriptorInfo;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class ElementPositionResolver {

    public PositionInformation buildPositionDescription(final RobotFile model,
            final RobotLine currentLine, final RobotToken currentToken) {
        PositionInformation posInfo = new PositionInformation();

        List<IRobotLineElement> lineElements = new LinkedList<>(
                currentLine.getLineElements());
        lineElements.add(currentToken);
        int numberOfElements = lineElements.size();
        for (int elemIndex = 0; elemIndex < numberOfElements; elemIndex++) {
            IRobotLineElement elem = lineElements.get(elemIndex);
            if (elem instanceof Separator) {
                if (posInfo.getSeparatorsPosIndexes().isEmpty()) {
                    posInfo.setLineSeparator((SeparatorType) elem.getTypes()
                            .get(0));
                }

                posInfo.addSeparatorPosIndex(elemIndex);
            } else if (elem instanceof RobotToken) {
                if (elem.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                    posInfo.addPrettyAlignPosIndex(elemIndex);
                } else if (elem.getTypes().contains(
                        RobotTokenType.PREVIOUS_LINE_CONTINUE)) {
                    posInfo.addPreviousLineContinuePosIndex(elemIndex);
                } else {
                    posInfo.addRobotTokenPosIndex(elemIndex);
                }
            }
        }

        return posInfo;
    }

    public static class PositionInformation {

        private SeparatorType lineSeparator = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;
        private final List<Integer> robotTokensPosIndexes = new LinkedList<>();
        private final List<Integer> previousLineContinuePosIndexes = new LinkedList<>();
        private final List<Integer> prettyAlignPosIndexes = new LinkedList<>();
        private final List<Integer> separatorsPosIndexes = new LinkedList<>();
        private boolean wasLastSeparator = false;
        private boolean isFirstSeparator = false;


        private void setLineSeparator(SeparatorType lineSeparator) {
            this.lineSeparator = lineSeparator;
        }


        private void addRobotTokenPosIndex(final int robotTokenPosIndex) {
            robotTokensPosIndexes.add(robotTokenPosIndex);
            this.wasLastSeparator = false;
        }


        private void addPrettyAlignPosIndex(final int prettyAlignPosIndex) {
            prettyAlignPosIndexes.add(prettyAlignPosIndex);
        }


        private void addPreviousLineContinuePosIndex(final int prevLineContIndex) {
            previousLineContinuePosIndexes.add(prevLineContIndex);
        }


        private void addSeparatorPosIndex(final int separatorPosIndex) {
            if (robotTokensPosIndexes.isEmpty()
                    && separatorsPosIndexes.isEmpty()) {
                isFirstSeparator = true;
            }
            separatorsPosIndexes.add(separatorPosIndex);
            this.wasLastSeparator = true;
        }


        public SeparatorType getLineSeparator() {
            return lineSeparator;
        }


        public List<Integer> getRobotTokensPosIndexes() {
            return robotTokensPosIndexes;
        }


        public List<Integer> getSeparatorsPosIndexes() {
            return separatorsPosIndexes;
        }


        public List<Integer> getPrettyAlignPosIndexes() {
            return prettyAlignPosIndexes;
        }


        public List<Integer> getPreviousLineContinuePosIndexes() {
            return previousLineContinuePosIndexes;
        }


        public boolean isContinuePreviousLineTheFirstToken() {
            boolean result = false;
            if (!previousLineContinuePosIndexes.isEmpty()) {
                int theFirstContinue = previousLineContinuePosIndexes.get(0);
                if (!robotTokensPosIndexes.isEmpty()) {
                    int theFirstToken = robotTokensPosIndexes.get(0);
                    result = theFirstContinue < theFirstToken;
                } else {
                    result = true;
                }
            }
            return result;
        }


        public int getColumnIndex() {
            int column = 0;
            if (!robotTokensPosIndexes.isEmpty()
                    || !separatorsPosIndexes.isEmpty()) {
                int numberOfSeparators = separatorsPosIndexes.size();
                if (getLineSeparator() == SeparatorType.PIPE) {
                    if (wasLastSeparator) {
                        column = numberOfSeparators - 1;
                    } else {
                        column = numberOfSeparators;
                    }
                } else {
                    if (wasLastSeparator) {
                        column = numberOfSeparators + 1;
                    } else {
                        column = robotTokensPosIndexes.size();
                        if (isFirstSeparator()) {
                            column++;
                        }
                    }
                }
            }
            return column;
        }


        public boolean isLastSeparator() {
            return wasLastSeparator;
        }


        public boolean isFirstSeparator() {
            return isFirstSeparator;
        }
    }

    public enum PositionExpected implements IPositionCheckable {
        TABLE_HEADER {

            @Override
            public boolean isExpectedPosition(
                    final PositionInformation posInfo, final RobotFile model,
                    final RobotLine currentLine, final RobotToken currentToken) {
                return isReallyFirstElement(posInfo, currentToken);
            }
        },
        SETTING_TABLE_ELEMENT_DECLARATION {

            @Override
            public boolean isExpectedPosition(
                    final PositionInformation posInfo, final RobotFile model,
                    final RobotLine currentLine, final RobotToken currentToken) {
                return isReallyFirstElement(posInfo, currentToken);
            }
        },
        TEST_CASE_NAME {

            @Override
            public boolean isExpectedPosition(
                    final PositionInformation posInfo, final RobotFile model,
                    final RobotLine currentLine, final RobotToken currentToken) {
                return isReallyFirstElement(posInfo, currentToken);
            }
        },
        USER_KEYWORD_NAME {

            @Override
            public boolean isExpectedPosition(
                    final PositionInformation posInfo, final RobotFile model,
                    final RobotLine currentLine, final RobotToken currentToken) {
                return isReallyFirstElement(posInfo, currentToken);
            }
        },
        VARIABLE_DECLARATION_IN_VARIABLE_TABLE {

            @Override
            public boolean isExpectedPosition(
                    final PositionInformation posInfo, final RobotFile model,
                    final RobotLine currentLine, final RobotToken currentToken) {
                return isReallyFirstElement(posInfo, currentToken);
            }
        },
        LINE_CONTINUE_NEWLINE_FOR_SETTING_TABLE {

            @Override
            public boolean isExpectedPosition(
                    final PositionInformation posInfo, final RobotFile model,
                    final RobotLine currentLine, final RobotToken currentToken) {
                return posInfo.isContinuePreviousLineTheFirstToken();
            }
        },
        LINE_CONTINUE_NEWLINE_FOR_VARIABLE_TABLE {

            @Override
            public boolean isExpectedPosition(
                    final PositionInformation posInfo, final RobotFile model,
                    final RobotLine currentLine, final RobotToken currentToken) {
                return posInfo.isContinuePreviousLineTheFirstToken();
            }
        },
        LINE_CONTINUE_NEWLINE_FOR_TESTCASE_TABLE {

            @Override
            public boolean isExpectedPosition(
                    final PositionInformation posInfo, final RobotFile model,
                    final RobotLine currentLine, final RobotToken currentToken) {
                return posInfo.isContinuePreviousLineTheFirstToken();
            }
        },
        LINE_CONTINUE_NEWLINE_FOR_KEYWORD_TABLE {

            @Override
            public boolean isExpectedPosition(
                    final PositionInformation posInfo, final RobotFile model,
                    final RobotLine currentLine, final RobotToken currentToken) {
                return posInfo.isContinuePreviousLineTheFirstToken();
            }
        },
        LINE_CONTINUE_INLINED_FOR_TESTCASE_TABLE {

            @Override
            public boolean isExpectedPosition(
                    final PositionInformation posInfo, final RobotFile model,
                    final RobotLine currentLine, final RobotToken currentToken) {
                return isInlined(posInfo);
            }
        },
        LINE_CONTINUE_INLINED_FOR_KEYWORD_TABLE {

            @Override
            public boolean isExpectedPosition(
                    final PositionInformation posInfo, final RobotFile model,
                    final RobotLine currentLine, final RobotToken currentToken) {
                return isInlined(posInfo);
            }
        },
        LINE_CONTINUE_INLINED_IN_FOR_LOOP {

            @Override
            public boolean isExpectedPosition(
                    final PositionInformation posInfo, final RobotFile model,
                    final RobotLine currentLine, final RobotToken currentToken) {
                boolean isInlined = false;
                List<IRobotLineElement> elements = new LinkedList<>(
                        currentLine.getLineElements());
                if (currentToken != null) {
                    elements.add(currentToken);
                }
                ForDescriptorInfo forDescInfo = ForDescriptorInfo
                        .build(elements);
                if (forDescInfo.getForStartIndex() > -1) {
                    if (forDescInfo.getForLineContinueInlineIndex() == currentLine
                            .getLineElements().size()) {
                        isInlined = true;
                    }
                }

                return isInlined;
            }
        };

        private static boolean isInlined(final PositionInformation posInfo) {
            boolean result = false;
            if (posInfo.getPreviousLineContinuePosIndexes().size() == 1
                    && posInfo.getRobotTokensPosIndexes().size() == 1) {
                int theFirstPreviousContinue = posInfo
                        .getPreviousLineContinuePosIndexes().get(0);
                int theFirstToken = posInfo.getRobotTokensPosIndexes().get(0);
                if (theFirstToken < theFirstPreviousContinue) {
                    SeparatorType separatorType = posInfo.getLineSeparator();
                    List<Integer> separatorsPosIndexes = posInfo
                            .getSeparatorsPosIndexes();
                    if (separatorType == SeparatorType.PIPE) {
                        if (separatorsPosIndexes.size() >= 2) {
                            int theFirstSeparator = separatorsPosIndexes.get(0);
                            int theSecondSeparator = separatorsPosIndexes
                                    .get(1);
                            result = theFirstSeparator < theFirstToken
                                    && theFirstToken < theSecondSeparator
                                    && theSecondSeparator < theFirstPreviousContinue;
                        }
                    } else {
                        if (separatorsPosIndexes.size() == 1) {
                            int theFirstSeparator = separatorsPosIndexes.get(0);
                            result = theFirstToken < theFirstSeparator
                                    && theFirstSeparator < theFirstPreviousContinue;
                        }
                    }
                }
            }

            return result;
        }


        private static boolean isReallyFirstElement(
                final PositionInformation posInfo, final RobotToken currentToken) {
            boolean result = false;
            SeparatorType separator = posInfo.getLineSeparator();
            if (separator == SeparatorType.PIPE) {
                if (posInfo.getSeparatorsPosIndexes().size() == 1
                        && (posInfo.getRobotTokensPosIndexes().size() == 1 || posInfo
                                .getPreviousLineContinuePosIndexes().size() == 1)) {
                    result = posInfo.isFirstSeparator();
                }
            } else {
                result = (currentToken.getStartColumn() == 0)
                        && (posInfo.getRobotTokensPosIndexes().size() == 1 || posInfo
                                .getPreviousLineContinuePosIndexes().size() == 1)
                        && posInfo.getSeparatorsPosIndexes().isEmpty()
                        && !posInfo.isFirstSeparator();
            }
            return result;
        }
    }


    public boolean isCorrectPosition(final PositionExpected expected,
            final RobotFile model, final RobotLine currentLine,
            final RobotToken currentToken) {
        PositionInformation posInfo = buildPositionDescription(model,
                currentLine, currentToken);
        return expected.isExpectedPosition(posInfo, model, currentLine,
                currentToken);
    }

    public interface IPositionCheckable {

        boolean isExpectedPosition(final PositionInformation posInfo,
                final RobotFile model, final RobotLine currentLine,
                final RobotToken currentToken);
    }
}
