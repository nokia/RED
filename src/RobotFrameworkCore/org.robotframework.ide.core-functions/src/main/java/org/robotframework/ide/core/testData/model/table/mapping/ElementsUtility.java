package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class ElementsUtility {

    public AImported getNearestImport(final RobotFileOutput robotFileOutput) {
        AImported result;
        List<AImported> imports = robotFileOutput.getFileModel()
                .getSettingTable().getImports();
        if (!imports.isEmpty()) {
            result = imports.get(imports.size() - 1);
        } else {
            result = null;
        }

        return result;
    }


    public boolean isTheFirstColumn(RobotLine currentLine, RobotToken robotToken) {
        boolean result = false;
        if (robotToken.getStartColumn() == 0) {
            result = true;
        } else {
            List<IRobotLineElement> lineElements = currentLine
                    .getLineElements();
            int size = lineElements.size();
            if (size > 0) {
                IRobotLineElement lastElement = lineElements.get(size - 1);
                result = (lastElement.getTypes().contains(SeparatorType.PIPE) && lastElement
                        .getStartColumn() == 0);
            } else {
                result = true;
            }
        }
        return result;
    }


    public List<TableHeader> getKnownHeadersForTable(
            final RobotFileOutput robotFileOutput,
            final ParsingState tableHeaderState) {
        List<TableHeader> tableKnownHeaders = new LinkedList<>();
        RobotFile fileModel = robotFileOutput.getFileModel();
        if (tableHeaderState == ParsingState.SETTING_TABLE_HEADER) {
            tableKnownHeaders = fileModel.getSettingTable().getHeaders();
        } else if (tableHeaderState == ParsingState.VARIABLE_TABLE_HEADER) {
            tableKnownHeaders = fileModel.getVariableTable().getHeaders();
        } else if (tableHeaderState == ParsingState.TEST_CASE_TABLE_HEADER) {
            tableKnownHeaders = fileModel.getTestCaseTable().getHeaders();
        } else if (tableHeaderState == ParsingState.KEYWORD_TABLE_HEADER) {
            tableKnownHeaders = fileModel.getKeywordTable().getHeaders();
        } else {
            // FIXME: error state not coherent
        }

        return tableKnownHeaders;
    }


    public ParsingState getNearestTableHeaderState(
            Stack<ParsingState> processingState) {
        ParsingState state = ParsingState.UNKNOWN;
        for (ParsingState s : processingState) {
            if (isTableState(s)) {
                state = s;
                break;
            }
        }

        return state;
    }


    public boolean isTableState(ParsingState state) {
        return state == ParsingState.TEST_CASE_TABLE_HEADER
                || state == ParsingState.SETTING_TABLE_HEADER
                || state == ParsingState.VARIABLE_TABLE_HEADER
                || state == ParsingState.KEYWORD_TABLE_HEADER;
    }


    public boolean isTableInsideState(ParsingState state) {
        return state == ParsingState.SETTING_TABLE_INSIDE
                || state == ParsingState.TEST_CASE_TABLE_INSIDE
                || state == ParsingState.KEYWORD_TABLE_INSIDE
                || state == ParsingState.VARIABLE_TABLE_INSIDE;
    }


    public ParsingState getCurrentStatus(Stack<ParsingState> processingState) {
        ParsingState state = ParsingState.UNKNOWN;

        if (!processingState.isEmpty()) {
            state = processingState.peek();
        }

        return state;
    }
}
