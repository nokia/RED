/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
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
                } else {
                    posInfo.addRobotTokenPosIndex(elemIndex);
                }
            }
        }

        return posInfo;
    }

    public static class PositionInformation {

        private SeparatorType lineSeparator = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;
        private List<Integer> robotTokensPosIndexes = new LinkedList<>();
        private List<Integer> prettyAlignPosIndexes = new LinkedList<>();
        private List<Integer> separatorsPosIndexes = new LinkedList<>();
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
        VARIABLE_DECLARATION_IN_VARIABLE_TABLE {

            @Override
            public boolean isExpectedPosition(
                    final PositionInformation posInfo, final RobotFile model,
                    final RobotLine currentLine, final RobotToken currentToken) {
                return isReallyFirstElement(posInfo, currentToken);
            }
        };

        private static boolean isReallyFirstElement(
                final PositionInformation posInfo, final RobotToken currentToken) {
            boolean result = false;
            SeparatorType separator = posInfo.getLineSeparator();
            if (separator == SeparatorType.PIPE) {
                if (posInfo.getSeparatorsPosIndexes().size() == 1
                        && posInfo.getRobotTokensPosIndexes().size() == 1) {
                    result = posInfo.isFirstSeparator();
                }
            } else {
                result = (currentToken.getStartColumn() == 0)
                        && posInfo.getRobotTokensPosIndexes().size() == 1
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
