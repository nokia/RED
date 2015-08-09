package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.AKeywordBaseSetting;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class ElementsUtility {

    public ParsingState findNearestNotCommentState(
            final Stack<ParsingState> processingState) {
        ParsingState state = ParsingState.UNKNOWN;
        for (ParsingState s : processingState) {
            if (s != ParsingState.COMMENT) {
                state = s;
            }
        }
        return state;
    }


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


    public void updateStatusesForNewLine(
            final Stack<ParsingState> processingState) {

        boolean clean = true;
        while(clean) {
            ParsingState status = getCurrentStatus(processingState);
            if (isTableHeader(status)) {
                processingState.pop();
                if (status == ParsingState.SETTING_TABLE_HEADER) {
                    processingState.push(ParsingState.SETTING_TABLE_INSIDE);
                } else if (status == ParsingState.VARIABLE_TABLE_HEADER) {
                    processingState.push(ParsingState.VARIABLE_TABLE_INSIDE);
                } else if (status == ParsingState.TEST_CASE_TABLE_HEADER) {
                    processingState.push(ParsingState.TEST_CASE_TABLE_INSIDE);
                } else if (status == ParsingState.KEYWORD_TABLE_HEADER) {
                    processingState.push(ParsingState.KEYWORD_TABLE_INSIDE);
                }

                clean = false;
            } else if (isTableInsideState(status)) {
                clean = false;
            } else if (!processingState.isEmpty()) {
                processingState.pop();
            } else {
                clean = false;
            }
        }
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


    public boolean isTheFirstColumnAfterSeparator(RobotLine currentLine,
            RobotToken robotToken) {
        boolean result = false;
        if (robotToken.getStartColumn() > 0) {
            List<IRobotLineElement> lineElements = currentLine
                    .getLineElements();
            int size = lineElements.size();
            if (size > 0) {
                IRobotLineElement lastElement = lineElements.get(size - 1);
                List<IRobotTokenType> types = lastElement.getTypes();
                result = ((types.contains(SeparatorType.PIPE) || types
                        .contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE)) && lastElement
                        .getStartColumn() == 0);
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


    public boolean isTableHeader(ParsingState state) {
        boolean result = false;
        if (state == ParsingState.SETTING_TABLE_HEADER
                || state == ParsingState.VARIABLE_TABLE_HEADER
                || state == ParsingState.TEST_CASE_TABLE_HEADER
                || state == ParsingState.KEYWORD_TABLE_HEADER) {
            result = true;
        }

        return result;
    }


    public boolean isTableHeader(RobotToken t) {
        boolean result = false;
        List<IRobotTokenType> declaredTypes = t.getTypes();
        if (declaredTypes.contains(RobotTokenType.SETTINGS_TABLE_HEADER)) {
            result = true;
        } else if (declaredTypes
                .contains(RobotTokenType.VARIABLES_TABLE_HEADER)) {
            result = true;
        } else if (declaredTypes
                .contains(RobotTokenType.TEST_CASES_TABLE_HEADER)) {
            result = true;
        } else if (declaredTypes.contains(RobotTokenType.KEYWORDS_TABLE_HEADER)) {
            result = true;
        }

        return result;
    }


    public ParsingState getCurrentStatus(Stack<ParsingState> processingState) {
        ParsingState state = ParsingState.UNKNOWN;

        if (!processingState.isEmpty()) {
            state = processingState.peek();
        }

        return state;
    }


    public boolean checkIfHasAlreadyKeywordName(
            List<? extends AKeywordBaseSetting> keywordBases) {
        boolean result = false;
        for (AKeywordBaseSetting setting : keywordBases) {
            result = (setting.getKeywordName() != null);
            result = result || !setting.getArguments().isEmpty();
            if (result) {
                break;
            }
        }

        return result;
    }


    public ParsingState getStatus(RobotToken t) {
        ParsingState status = ParsingState.UNKNOWN;
        List<IRobotTokenType> types = t.getTypes();
        if (types.contains(RobotTokenType.SETTINGS_TABLE_HEADER)) {
            status = ParsingState.SETTING_TABLE_HEADER;
        } else if (types.contains(RobotTokenType.VARIABLES_TABLE_HEADER)) {
            status = ParsingState.VARIABLE_TABLE_HEADER;
        } else if (types.contains(RobotTokenType.TEST_CASES_TABLE_HEADER)) {
            status = ParsingState.TEST_CASE_TABLE_HEADER;
        } else if (types.contains(RobotTokenType.KEYWORDS_TABLE_HEADER)) {
            status = ParsingState.KEYWORD_TABLE_HEADER;
        }

        return status;
    }
}
