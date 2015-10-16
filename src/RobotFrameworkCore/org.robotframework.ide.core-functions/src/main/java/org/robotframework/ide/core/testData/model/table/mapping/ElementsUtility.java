/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.mapping;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.model.AKeywordBaseSetting;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.LibraryImport;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.LineReader.Constant;
import org.robotframework.ide.core.testData.text.read.ParsingState;
import org.robotframework.ide.core.testData.text.read.ParsingState.TableType;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.ALineSeparator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class ElementsUtility {

    public void fixOnlyPrettyAlignLinesInSettings(final RobotLine line,
            final Stack<ParsingState> processingState) {
        ParsingState state = getCurrentStatus(processingState);
        if (state == ParsingState.SETTING_TABLE_INSIDE) {
            removeTokenWithoutTextFromSimpleTableLine(line);
        }
    }


    public void fixOnlyPrettyAlignLinesInVariables(final RobotLine line,
            final Stack<ParsingState> processingState) {
        ParsingState state = getCurrentStatus(processingState);
        if (state == ParsingState.VARIABLE_TABLE_INSIDE) {
            removeTokenWithoutTextFromSimpleTableLine(line);
        }
    }


    private void removeTokenWithoutTextFromSimpleTableLine(final RobotLine line) {
        boolean containsAnyValuableToken = false;
        List<Integer> emptyStrings = new LinkedList<>();
        List<IRobotLineElement> lineElements = line.getLineElements();
        int length = lineElements.size();
        for (int lineElementIndex = 0; lineElementIndex < length; lineElementIndex++) {
            IRobotLineElement elem = lineElements.get(lineElementIndex);
            if (elem instanceof RobotToken) {
                RobotToken rt = (RobotToken) elem;
                List<IRobotTokenType> types = rt.getTypes();
                for (IRobotTokenType type : types) {
                    if (type != RobotTokenType.UNKNOWN
                            && type != RobotTokenType.PRETTY_ALIGN_SPACE) {
                        containsAnyValuableToken = true;
                    }
                }

                String text = rt.getRaw().toString();
                if (text != null && text.trim().length() > 0) {
                    containsAnyValuableToken = true;
                } else if (!containsAnyValuableToken
                        && types.contains(RobotTokenType.UNKNOWN)) {
                    emptyStrings.add(lineElementIndex);
                }
            }
        }

        if (!containsAnyValuableToken) {
            Collections.sort(emptyStrings);
            int emptiesSize = emptyStrings.size();
            for (int index = emptiesSize - 1; index >= 0; index--) {
                lineElements.remove((int) emptyStrings.get(index));
            }
        }
    }


    public boolean isNewExecutableSection(final ALineSeparator separator,
            final RobotLine line) {
        boolean result = false;
        if (separator.getProducedType() == SeparatorType.PIPE) {
            List<IRobotLineElement> lineElements = line.getLineElements();
            if (lineElements.size() == 1) {
                result = lineElements.get(0).getTypes()
                        .contains(SeparatorType.PIPE);
            }
        } else {
            result = line.getLineElements().isEmpty();
        }
        return result;
    }


    public LibraryImport findNearestLibraryImport(
            final RobotFileOutput robotFileOutput) {
        AImported imported = getNearestImport(robotFileOutput);
        LibraryImport lib;
        if (imported instanceof LibraryImport) {
            lib = (LibraryImport) imported;
        } else {
            lib = null;

            // FIXME: sth wrong - declaration of library not inside setting
            // and
            // was not catch by previous library declaration logic
        }
        return lib;
    }


    public RobotToken computeCorrectRobotToken(RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final FilePosition fp,
            String text, boolean isNewLine, List<RobotToken> robotTokens,
            String fileName) {
        RobotToken correct = null;
        if (robotTokens.size() > 1) {
            List<RobotToken> headersPossible = findHeadersPossible(robotTokens);
            if (!headersPossible.isEmpty()) {
                if (headersPossible.size() == 1) {
                    correct = headersPossible.get(0);
                } else {
                    // FIXME: error
                }
            } else {
                ParsingState state = getCurrentStatus(processingState);

                RobotToken comment = findCommentToken(robotTokens, text);
                if (comment != null) {
                    correct = comment;
                } else {
                    for (RobotToken rt : robotTokens) {
                        if (isTypeForState(state, rt)) {
                            correct = rt;
                            break;
                        }
                    }
                }

                if (correct == null) {
                    if (ParsingState.getSettingsStates().contains(state)
                            || getCurrentStatus(processingState).getTable() == TableType.VARIABLES) {
                        RobotToken newRobotToken = new RobotToken();
                        newRobotToken.setLineNumber(fp.getLine());
                        newRobotToken.setStartColumn(fp.getColumn());
                        newRobotToken.setText(new StringBuilder(text));
                        newRobotToken.setRaw(new StringBuilder(text));
                        newRobotToken.setType(RobotTokenType.UNKNOWN);
                        correct = newRobotToken;
                    } else {
                        // FIXME: error no matching tokens to state
                        throw new IllegalStateException(
                                "Some problem to fix. Cannot find correct token for text \'"
                                        + text + "\' and found tokens "
                                        + robotTokens + " in file " + fileName);
                    }
                }
            }
        } else {
            correct = robotTokens.get(0);
        }

        return correct;
    }


    public boolean isTypeForState(final ParsingState state, final RobotToken rt) {
        RobotTokenType robotType = RobotTokenType.UNKNOWN;
        boolean result = false;

        List<RobotTokenType> typesForState = new LinkedList<>();
        if (state == ParsingState.TEST_CASE_TABLE_INSIDE
                || state == ParsingState.TEST_CASE_DECLARATION) {
            typesForState = robotType.getTypesForTestCasesTable();
        } else if (state == ParsingState.SETTING_TABLE_INSIDE) {
            typesForState = robotType.getTypesForSettingsTable();
        } else if (state == ParsingState.VARIABLE_TABLE_INSIDE) {
            typesForState = robotType.getTypesForVariablesTable();
        } else if (state == ParsingState.KEYWORD_TABLE_INSIDE
                || state == ParsingState.KEYWORD_DECLARATION) {
            typesForState = robotType.getTypesForKeywordsTable();
        }

        List<IRobotTokenType> types = rt.getTypes();
        for (IRobotTokenType type : types) {
            if (typesForState.contains(type)) {
                result = true;
                break;
            }
        }

        if (!result) {
            if (state == ParsingState.TEST_CASE_DECLARATION
                    || state == ParsingState.KEYWORD_DECLARATION
                    || state == ParsingState.UNKNOWN) {
                result = (types.contains(RobotTokenType.START_HASH_COMMENT) || types
                        .contains(RobotTokenType.COMMENT_CONTINUE));

            }
        }

        return result;
    }


    public RobotToken findCommentToken(List<RobotToken> robotTokens, String text) {
        RobotToken comment = null;
        for (RobotToken rt : robotTokens) {
            List<IRobotTokenType> types = rt.getTypes();
            if (types.contains(RobotTokenType.START_HASH_COMMENT)
                    || types.contains(RobotTokenType.COMMENT_CONTINUE)) {
                if (text.equals(rt.getRaw().toString())) {
                    comment = rt;
                    break;
                }
            }
        }

        return comment;
    }


    public List<RobotToken> findHeadersPossible(final List<RobotToken> tokens) {
        List<RobotToken> found = new LinkedList<>();
        for (RobotToken t : tokens) {
            if (isTableHeader(t)) {
                found.add(t);
            }
        }

        return found;
    }


    public boolean isComment(final RobotLine line) {
        boolean result = false;
        for (IRobotLineElement elem : line.getLineElements()) {
            List<IRobotTokenType> types = elem.getTypes();
            if (types.isEmpty()) {
                result = false;
                break;
            } else {
                IRobotTokenType tokenType = types.get(0);
                if (tokenType == SeparatorType.PIPE
                        || tokenType == SeparatorType.TABULATOR_OR_DOUBLE_SPACE) {
                    continue;
                } else if (tokenType == RobotTokenType.START_HASH_COMMENT) {
                    result = true;
                    break;
                } else {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }


    public boolean isTableSection(final RobotLine line) {
        boolean result = false;
        for (IRobotLineElement elem : line.getLineElements()) {
            if (isTableHeader(elem)) {
                result = true;
                break;
            }
        }

        return result;
    }


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
            } else if (isKeywordExecution(status)) {
                clean = false;
            } else if (isTestCaseExecution(status)) {
                clean = false;
            } else if (!processingState.isEmpty()) {
                processingState.pop();
            } else {
                clean = false;
            }
        }
    }


    public boolean isTestCaseExecution(ParsingState status) {
        return (status == ParsingState.TEST_CASE_DECLARATION);
    }


    public boolean isKeywordExecution(ParsingState status) {
        return (status == ParsingState.KEYWORD_DECLARATION);
    }


    public boolean isTheFirstColumn(RobotLine currentLine, RobotToken robotToken) {
        boolean result = false;
        if (robotToken.getStartColumn() == 0) {
            result = true;
        } else {
            List<IRobotLineElement> lineElements = currentLine
                    .getLineElements();
            int size = lineElements.size();
            if (size == 1) {
                IRobotLineElement lastElement = lineElements.get(0);
                result = (lastElement.getTypes().contains(SeparatorType.PIPE) && lastElement
                        .getStartColumn() == 0);
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


    public List<TableHeader<? extends ARobotSectionTable>> getKnownHeadersForTable(
            final RobotFileOutput robotFileOutput,
            final ParsingState tableHeaderState) {
        List<TableHeader<? extends ARobotSectionTable>> tableKnownHeaders = new LinkedList<>();
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


    public boolean isTableInsideStateInHierarchy(ParsingState state) {
        boolean result = false;
        if (!isTableInsideState(state)) {
            ParsingState parent = null;
            while((parent = state.getPreviousState()) != null) {
                if (isTableInsideState(parent)) {
                    result = true;
                    break;
                } else {
                    state = parent;
                }
            }
        } else {
            result = true;
        }

        return result;
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


    public boolean isTableHeader(IRobotTokenType type) {
        return (type == RobotTokenType.SETTINGS_TABLE_HEADER
                || type == RobotTokenType.VARIABLES_TABLE_HEADER
                || type == RobotTokenType.TEST_CASES_TABLE_HEADER || type == RobotTokenType.KEYWORDS_TABLE_HEADER);
    }


    public boolean isTableHeader(RobotToken t) {
        boolean result = false;
        List<IRobotTokenType> declaredTypes = t.getTypes();
        for (IRobotTokenType type : declaredTypes) {
            if (isTableHeader(type)) {
                result = true;
                break;
            }
        }

        return result;
    }


    public boolean isTableHeader(IRobotLineElement elem) {
        boolean result = false;
        if (elem instanceof RobotToken) {
            result = isTableHeader((RobotToken) elem);
        }

        return result;
    }


    public boolean isUserTableHeader(RobotToken t) {
        boolean result = false;

        StringBuilder raw = t.getRaw();
        if (raw != null) {
            String rawText = raw.toString();
            if (rawText != null) {
                result = rawText.trim().startsWith("*");
            }
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
            List<? extends AKeywordBaseSetting<?>> keywordBases) {
        boolean result = false;
        for (AKeywordBaseSetting<?> setting : keywordBases) {
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


    public void extractPrettyAlignWhitespaces(RobotLine line, RobotToken rt,
            String rawText) {
        String correctedString = rawText;
        if (rawText.startsWith(" ")) {
            RobotToken prettyLeftAlign = new RobotToken();
            prettyLeftAlign.setStartOffset(rt.getStartOffset());
            prettyLeftAlign.setLineNumber(rt.getLineNumber());
            prettyLeftAlign.setStartColumn(rt.getStartColumn());
            prettyLeftAlign.setRaw(new StringBuilder(" "));
            prettyLeftAlign.setText(new StringBuilder(" "));
            prettyLeftAlign.setType(RobotTokenType.PRETTY_ALIGN_SPACE);
            line.addLineElementAt(line.getLineElements().size() - 1,
                    prettyLeftAlign);

            rt.setStartColumn(rt.getStartColumn() + 1);
            rt.setStartOffset(rt.getStartOffset() + 1);
            correctedString = rawText.substring(1);
            rt.setText(new StringBuilder(correctedString));
            rt.setRaw(new StringBuilder(correctedString));
        }

        if (correctedString.endsWith(" ")) {
            int theLongestTextLength = Math.max(rt.getRaw().length(),
                    rawText.length());
            RobotToken prettyRightAlign = new RobotToken();
            prettyRightAlign.setStartOffset(rt.getStartOffset()
                    + theLongestTextLength - 1);
            prettyRightAlign.setLineNumber(rt.getLineNumber());
            prettyRightAlign.setStartColumn(theLongestTextLength - 1);
            prettyRightAlign.setRaw(new StringBuilder(" "));
            prettyRightAlign.setText(new StringBuilder(" "));
            prettyRightAlign.setType(RobotTokenType.PRETTY_ALIGN_SPACE);
            line.addLineElement(prettyRightAlign);

            correctedString = correctedString.substring(0,
                    correctedString.length() - 1);
            rt.setText(new StringBuilder(correctedString));
            rt.setRaw(new StringBuilder(correctedString));
        }
    }


    public boolean isNotOnlySeparatorOrEmptyLine(final RobotLine currentLine) {
        boolean anyValuableToken = false;
        List<IRobotLineElement> lineElements = currentLine.getLineElements();
        for (IRobotLineElement lineElem : lineElements) {
            if (lineElem instanceof RobotToken) {
                anyValuableToken = true;
                break;
            }
        }

        return anyValuableToken;
    }


    public boolean shouldGiveEmptyToProcess(final ALineSeparator separator,
            final Separator currentSeparator, final RobotLine robotLine,
            String line, final Stack<ParsingState> processingState) {
        boolean result = false;

        if (currentSeparator.getStartColumn() > 0) {
            if (separator.getProducedType() == SeparatorType.PIPE) {

            }
        }

        return result;
    }


    public int getEndOfLineLength(final List<Constant> eols) {
        int size = 0;
        for (Constant c : eols) {
            if (c != Constant.EOF) {
                size++;
            } else {
                break;
            }
        }

        return size;
    }


    public void fixNotSetPositions(final RobotToken token, final FilePosition fp) {
        if (token.getStartOffset() == IRobotLineElement.NOT_SET) {
            token.setStartOffset(fp.getOffset());
        }
        if (token.getLineNumber() == IRobotLineElement.NOT_SET) {
            token.setLineNumber(fp.getLine());
        }
        if (token.getStartColumn() == IRobotLineElement.NOT_SET) {
            token.setStartColumn(fp.getColumn());
        }
    }
}
