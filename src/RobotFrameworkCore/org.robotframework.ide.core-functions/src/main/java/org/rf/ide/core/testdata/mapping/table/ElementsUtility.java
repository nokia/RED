/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.mapping.PreviousLineHandler;
import org.rf.ide.core.testdata.model.ExecutableSetting;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.ECompareResult;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.exec.descs.ForDescriptorInfo;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.ParsingState.TableType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.ALineSeparator;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;
import org.rf.ide.core.testdata.text.read.separators.StrictTsvTabulatorSeparator;

public class ElementsUtility {

    private final ParsingStateHelper parsingStateHelper = new ParsingStateHelper();

    public List<RobotToken> filter(final List<RobotToken> toks, final IRobotTokenType type) {
        return toks.stream().filter(token -> token.getTypes().contains(type)).collect(toList());
    }

    public boolean isNewExecutableSection(final ALineSeparator separator, final RobotLine line) {
        if (separator.getProducedType() == SeparatorType.PIPE) {
            final List<IRobotLineElement> lineElements = line.getLineElements();
            return lineElements.size() == 1 && lineElements.get(0).getTypes().contains(SeparatorType.PIPE);
        } else {
            return line.getLineElements().isEmpty();
        }
    }

    public <T extends AImported> Optional<T> getCurrentImport(final RobotFileOutput robotFileOutput,
            final Class<T> expected) {
        final List<AImported> imports = robotFileOutput.getFileModel().getSettingTable().getImports();
        if (imports.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(imports.get(imports.size() - 1)).filter(expected::isInstance).map(expected::cast);
        }
    }

    public RobotToken computeCorrectRobotToken(final Stack<ParsingState> processingState, final FilePosition fp,
            final String text, final List<RobotToken> robotTokens) {

        final ParsingState state = parsingStateHelper.getCurrentState(processingState);
        RobotToken correct = null;

        final List<String> varIds = VariablesAnalyzer.analyzer(RobotVersion.UNKNOWN)
                .getVariablesUses(text)
                .map(use -> use.getType().getIdentificator())
                .collect(toList());

        if (robotTokens.size() > 1) {
            final List<RobotToken> tokensExactlyOnPosition = getTokensExactlyOnPosition(robotTokens, fp);
            final TableType currentTable = state.getTable();
            if (tokensExactlyOnPosition.size() != 1 || currentTable == TableType.KEYWORD
                    || currentTable == TableType.TEST_CASE || currentTable == TableType.TASKS) {
                final List<RobotToken> headersPossible = findHeadersPossible(robotTokens);
                if (!headersPossible.isEmpty()) {
                    if (headersPossible.size() == 1) {
                        correct = headersPossible.get(0);
                    } else {
                        final RobotToken newRobotToken = RobotToken.create(text, fp.getLine(), fp.getColumn());
                        correct = newRobotToken;
                    }
                } else {
                    final RobotToken comment = findCommentToken(robotTokens, text);
                    if (comment != null) {
                        correct = comment;
                    } else {
                        for (final RobotToken rt : robotTokens) {
                            if (parsingStateHelper.isTypeForState(state, rt)) {
                                correct = rt;
                                break;
                            }
                        }
                    }

                    final TableType tableType = state.getTable();
                    if (correct == null && (tableType == TableType.KEYWORD || tableType == TableType.TEST_CASE
                            || tableType == TableType.TASKS)) {
                        final ParsingState expected;
                        if (tableType == TableType.KEYWORD) {
                            expected = ParsingState.KEYWORD_DECLARATION;
                        } else if (tableType == TableType.TEST_CASE) {
                            expected = ParsingState.TEST_CASE_DECLARATION;
                        } else {
                            expected = ParsingState.TASK_DECLARATION;
                        }
                        if (meetsState(state, expected) && tokensExactlyOnPosition.size() == 1) {
                            final RobotToken exactlyOne = tokensExactlyOnPosition.get(0);
                            if (isVariableDeclaration(exactlyOne, varIds)) {
                                correct = exactlyOne;
                            }
                        }
                    }

                    if (correct == null) {
                        if (ParsingState.getSettingsStates().contains(state) || currentTable == TableType.VARIABLES
                                || currentTable == TableType.KEYWORD || currentTable == TableType.TEST_CASE
                                || currentTable == TableType.TASKS || state == ParsingState.COMMENT) {
                            final RobotToken newRobotToken = RobotToken.create(text, fp.getLine(), fp.getColumn());
                            correct = newRobotToken;
                        } else {
                            // FIXME: info that nothing was found so token will be treat as UNKNOWN
                            final RobotToken newRobotToken = RobotToken.create(text, fp.getLine(), fp.getColumn());
                            final List<IRobotTokenType> types = newRobotToken.getTypes();
                            for (final RobotToken currentProposal : robotTokens) {
                                types.addAll(currentProposal.getTypes());
                            }
                            correct = newRobotToken;
                        }
                    }
                }
            } else {
                final RobotToken exactlyOnPosition = tokensExactlyOnPosition.get(0);
                if (state.getPreviousState() != ParsingState.VARIABLE_TABLE_HEADER
                        && state.getPreviousState() != ParsingState.VARIABLE_TABLE_INSIDE
                        && state.getPreviousState() != ParsingState.SETTING_TABLE_HEADER
                        && state.getPreviousState() != ParsingState.SETTING_TABLE_INSIDE) {
                    final List<IRobotTokenType> types = exactlyOnPosition.getTypes();
                    for (final RobotToken currentProposal : robotTokens) {
                        if (exactlyOnPosition != currentProposal) {
                            types.addAll(currentProposal.getTypes());
                        }
                    }
                }
                if (exactlyOnPosition.getText().equals(text)) {
                    correct = exactlyOnPosition;
                } else {
                    final RobotToken newRobotToken = RobotToken.create(text, fp.getLine(), fp.getColumn());
                    final List<IRobotTokenType> types = newRobotToken.getTypes();
                    for (final RobotToken rt : robotTokens) {
                        types.addAll(rt.getTypes());
                    }
                    correct = newRobotToken;
                }
            }
        } else {
            final RobotToken token = robotTokens.get(0);
            if (!token.getTypes().contains(RobotTokenType.UNKNOWN)) {
                final RobotToken newRobotToken = RobotToken.create(text, fp.getLine(), fp.getColumn());
                if (text != null && !(text.equals(token.getText()) || text.trim().equals(token.getText().trim()))) {
                    newRobotToken.setType(RobotTokenType.UNKNOWN);
                } else {
                    newRobotToken.getTypes().clear();
                }
                // FIXME: decide what to do

                newRobotToken.getTypes().addAll(token.getTypes());
                if (state.getTable() != TableType.VARIABLES) {
                    final List<RobotTokenType> typesForVariablesTable = RobotTokenType.getTypesForVariablesTable();
                    for (final IRobotTokenType type : token.getTypes()) {
                        if (type instanceof RobotTokenType) {
                            final RobotTokenType tokenType = (RobotTokenType) type;
                            if (typesForVariablesTable.contains(tokenType) && tokenType.isSettingDeclaration()) {
                                boolean invalidVar = true;
                                if (!varIds.isEmpty()) {
                                    final VariableType typeByTokenType = VariableType.getTypeByTokenType(type);
                                    for (final String varId : varIds) {
                                        if (typeByTokenType.getIdentificator().equals(varId)) {
                                            invalidVar = false;
                                            break;
                                        }
                                    }
                                }

                                if (invalidVar) {
                                    newRobotToken.getTypes().remove(type);
                                    if (!newRobotToken.getTypes().contains(RobotTokenType.VARIABLES_WRONG_DEFINED)) {
                                        newRobotToken.getTypes().add(RobotTokenType.VARIABLES_WRONG_DEFINED);
                                    }
                                }
                            }
                        }
                    }
                }
                // or add warning about possible type
                if (newRobotToken.getTypes().isEmpty()) {
                    newRobotToken.setType(RobotTokenType.UNKNOWN);
                }
                correct = newRobotToken;
            } else {
                correct = token;
            }
        }

        if (hasAnyVariableUsageInside(robotTokens, varIds.stream().findFirst())
                && state != ParsingState.VARIABLE_TABLE_INSIDE) {
            correct.getTypes().add(RobotTokenType.VARIABLE_USAGE);
        }
        return correct;
    }

    private boolean isVariableDeclaration(final RobotToken robotToken, final List<String> varIds) {
        final List<RobotTokenType> typesForVariablesTable = RobotTokenType.getTypesForVariablesTable();
        for (final IRobotTokenType type : robotToken.getTypes()) {
            if (type instanceof RobotTokenType) {
                final RobotTokenType tokenType = (RobotTokenType) type;
                if (typesForVariablesTable.contains(tokenType) && tokenType.isSettingDeclaration()) {
                    if (!varIds.isEmpty()) {
                        final VariableType typeByTokenType = VariableType.getTypeByTokenType(type);
                        if (varIds.stream().anyMatch(varId -> typeByTokenType.getIdentificator().equals(varId))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasAnyVariableUsageInside(final List<RobotToken> robotTokens,
            final Optional<String> varIdentifier) {
        for (final RobotToken rt : robotTokens) {
            for (final IRobotTokenType type : rt.getTypes()) {
                if (type == RobotTokenType.VARIABLES_DICTIONARY_DECLARATION
                        || type == RobotTokenType.VARIABLES_SCALAR_DECLARATION
                        || type == RobotTokenType.VARIABLES_LIST_DECLARATION
                        || type == RobotTokenType.VARIABLES_ENVIRONMENT_DECLARATION) {
                    if (varIdentifier.isPresent()) {
                        final VariableType typeByTokenType = VariableType.getTypeByTokenType(type);
                        if (typeByTokenType.getIdentificator().equals(varIdentifier.get())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean meetsState(final ParsingState state, final ParsingState expected) {
        boolean result = false;
        ParsingState currentState = state;
        while (currentState.getPreviousState() != null) {
            if (currentState.getPreviousState() == expected) {
                result = true;
                break;
            } else {
                currentState = currentState.getPreviousState();
            }
        }

        return result;
    }

    private List<RobotToken> getTokensExactlyOnPosition(final List<RobotToken> robotTokens,
            final FilePosition currentPosition) {
        final List<RobotToken> tokens = new ArrayList<>();
        for (final RobotToken rt : robotTokens) {
            if (currentPosition.compare(rt.getFilePosition(), false) == ECompareResult.EQUAL_TO.getValue()) {
                tokens.add(rt);
            }
        }

        return tokens;
    }

    public RobotToken findCommentToken(final List<RobotToken> robotTokens, final String text) {
        RobotToken comment = null;
        for (final RobotToken rt : robotTokens) {
            final List<IRobotTokenType> types = rt.getTypes();
            if (types.contains(RobotTokenType.START_HASH_COMMENT) || types.contains(RobotTokenType.COMMENT_CONTINUE)) {
                if (text.equals(rt.getText())) {
                    comment = rt;
                    break;
                }
            }
        }

        return comment;
    }

    public List<RobotToken> findHeadersPossible(final List<RobotToken> tokens) {
        final List<RobotToken> found = new ArrayList<>();
        for (final RobotToken t : tokens) {
            if (isTableHeader(t)) {
                found.add(t);
            }
        }

        return found;
    }

    public boolean isComment(final RobotLine line) {
        boolean result = false;
        for (final IRobotLineElement elem : line.getLineElements()) {
            final List<IRobotTokenType> types = elem.getTypes();
            if (types.isEmpty()) {
                result = false;
                break;
            } else {
                final IRobotTokenType tokenType = types.get(0);
                if (tokenType == SeparatorType.PIPE || tokenType == SeparatorType.TABULATOR_OR_DOUBLE_SPACE) {
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
        return line.getLineElements().stream().anyMatch(this::isTableHeader);
    }

    public List<TableHeader<? extends ARobotSectionTable>> getKnownHeadersForTable(
            final RobotFileOutput robotFileOutput, final ParsingState tableHeaderState) {
        List<TableHeader<? extends ARobotSectionTable>> tableKnownHeaders = new ArrayList<>();
        final RobotFile fileModel = robotFileOutput.getFileModel();
        if (tableHeaderState == ParsingState.SETTING_TABLE_HEADER) {
            tableKnownHeaders = fileModel.getSettingTable().getHeaders();
        } else if (tableHeaderState == ParsingState.VARIABLE_TABLE_HEADER) {
            tableKnownHeaders = fileModel.getVariableTable().getHeaders();
        } else if (tableHeaderState == ParsingState.TEST_CASE_TABLE_HEADER) {
            tableKnownHeaders = fileModel.getTestCaseTable().getHeaders();
        } else if (tableHeaderState == ParsingState.TASKS_TABLE_HEADER) {
            tableKnownHeaders = fileModel.getTasksTable().getHeaders();
        } else if (tableHeaderState == ParsingState.KEYWORD_TABLE_HEADER) {
            tableKnownHeaders = fileModel.getKeywordTable().getHeaders();
        } else {
            // FIXME: error state not coherent
        }

        return tableKnownHeaders;
    }

    public boolean isTableHeader(final IRobotTokenType type) {
        return type == RobotTokenType.SETTINGS_TABLE_HEADER || type == RobotTokenType.VARIABLES_TABLE_HEADER
                || type == RobotTokenType.TEST_CASES_TABLE_HEADER || type == RobotTokenType.TASKS_TABLE_HEADER
                || type == RobotTokenType.KEYWORDS_TABLE_HEADER || type == RobotTokenType.COMMENTS_TABLE_HEADER;
    }

    public boolean isTableHeader(final RobotToken t) {
        return t.getText().trim().startsWith("*") && t.getTypes().stream().anyMatch(this::isTableHeader);
    }

    public boolean isTableHeader(final IRobotLineElement elem) {
        return elem instanceof RobotToken && isTableHeader((RobotToken) elem);
    }

    public boolean isUserTableHeader(final RobotToken token) {
        final String text = token.getText();
        return text != null && text.length() > 1 && text.trim().startsWith("*");
    }

    public boolean checkIfFirstHasKeywordNameAlready(final List<? extends ExecutableSetting> keywordBased) {
        return !keywordBased.isEmpty() && keywordBased.get(0).getKeywordName() != null;
    }

    public boolean checkIfLastHasKeywordNameAlready(final List<? extends ExecutableSetting> keywordBased) {
        return !keywordBased.isEmpty() && keywordBased.get(keywordBased.size() - 1).getKeywordName() != null;
    }

    public boolean isNotOnlySeparatorOrEmptyLine(final RobotLine currentLine) {
        final List<IRobotLineElement> lineElements = currentLine.getLineElements();
        for (final IRobotLineElement lineElem : lineElements) {
            if (lineElem instanceof RobotToken && !lineElem.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldGiveEmptyToProcess(final RobotFileOutput parsingOutput, final ALineSeparator separator,
            final Separator currentSeparator, final RobotLine line, final Stack<ParsingState> processingState) {
        boolean result = false;

        final ParsingState state = parsingStateHelper.getCurrentState(processingState);
        final TableType tableType = state.getTable();
        final List<IRobotLineElement> splittedLine = separator.getSplittedLine();

        if (separator.getProducedType() == SeparatorType.PIPE && currentSeparator.getStartColumn() == 0) {
            result = false;
        } else if (separator.getProducedType() == SeparatorType.PIPE
                || separator instanceof StrictTsvTabulatorSeparator) {
            final LineTokenInfo lineTokenInfo = LineTokenInfo.build(splittedLine);
            if (!lineTokenInfo.getPositionsOfLineContinue().isEmpty()
                    || !lineTokenInfo.getPositionsOfNotEmptyElements().isEmpty()) {
                // Logic: Grant valid to process empty elements in case:
                // SETTINGS or VARIABLES: always read empty the exclusion is
                // only that we have line continue and element is not the first
                // after header declaration
                // TEST CASES and KEYWORDS: read empty the exclusion is only
                // line continue in any case only first element is omitted
                // GRANT LOGIC main: always process start from beginning until
                // last not empty element
                final boolean isContinoue = lineTokenInfo.isLineContinueTheFirst();
                if (tableType == TableType.SETTINGS || tableType == TableType.VARIABLES) {
                    if (isContinoue) {
                        final RobotFile model = parsingOutput.getFileModel();
                        final PreviousLineHandler prevLineHandler = new PreviousLineHandler();
                        if (prevLineHandler.isSomethingToContinue(model)) {
                            result = lineTokenInfo.getDataStartIndex() <= separator.getCurrentElementIndex();
                        } else {
                            result = true;
                        }
                    } else {
                        result = true;
                    }

                    result = result && lineTokenInfo.getDataEndIndex() >= separator.getCurrentElementIndex();
                } else if (tableType == TableType.TEST_CASE || tableType == TableType.TASKS
                        || tableType == TableType.KEYWORD) {
                    if (line.getLineElements().size() >= 2 || (line.getLineElements().size() == 1
                            && separator instanceof StrictTsvTabulatorSeparator)) {
                        if (isContinoue) {
                            result = lineTokenInfo.getDataStartIndex() <= separator.getCurrentElementIndex();
                        } else {
                            if (state == ParsingState.TEST_CASE_DECLARATION || state == ParsingState.TASK_DECLARATION
                                    || state == ParsingState.KEYWORD_DECLARATION) {
                                /**
                                 * <pre>
                                 *  *** Test Cases ***
                                 *  | x | | ... | Log | ... |
                                 *
                                 *  is not inline:
                                 *
                                 * ** Test Cases ***
                                 * | | x | | ... | Log | ... |
                                 * </pre>
                                 */
                                if (shouldTreatAsInlineContinue(lineTokenInfo)) {
                                    result = separator.getCurrentElementIndex() > lineTokenInfo
                                            .getPositionsOfLineContinue()
                                            .get(0)
                                            || separator.getCurrentElementIndex() < lineTokenInfo.getDataStartIndex();
                                } else {
                                    result = true;
                                }
                            } else if (state == ParsingState.TEST_CASE_INSIDE_ACTION
                                    || state == ParsingState.TASK_INSIDE_ACTION
                                    || state == ParsingState.KEYWORD_INSIDE_ACTION) {
                                final ForDescriptorInfo forInfo = ForDescriptorInfo.build(splittedLine);
                                if (forInfo.getForStartIndex() > -1) {
                                    if (forInfo.getForLineContinueInlineIndex() > -1) {
                                        result = (separator.getCurrentElementIndex() > forInfo
                                                .getForLineContinueInlineIndex());
                                    } else {
                                        result = true;
                                    }
                                } else {
                                    result = true;
                                }
                            } else {
                                result = true;
                            }
                        }

                        result = result && lineTokenInfo.getDataEndIndex() >= separator.getCurrentElementIndex();
                    }
                }
            }
        } else {
            final LineTokenInfo lineTokenInfo = LineTokenInfo.build(splittedLine);
            final boolean isContinue = lineTokenInfo.isLineContinueTheFirst();
            if (tableType == TableType.SETTINGS || tableType == TableType.VARIABLES) {
                if (isContinue) {
                    final RobotFile model = parsingOutput.getFileModel();
                    final PreviousLineHandler prevLineHandler = new PreviousLineHandler();
                    if (prevLineHandler.isSomethingToContinue(model)) {
                        result = lineTokenInfo.getDataStartIndex() <= separator.getCurrentElementIndex();
                    } else {
                        result = true;
                    }
                } else {
                    result = true;
                }

                result = result && lineTokenInfo.getDataEndIndex() >= separator.getCurrentElementIndex();
            }
        }

        return result;
    }

    private boolean shouldTreatAsInlineContinue(final LineTokenInfo lineTokenInfo) {
        boolean result = false;

        if (!lineTokenInfo.getPositionsOfLineContinue().isEmpty()
                && !lineTokenInfo.getPositionsOfNotEmptyElements().isEmpty()) {
            final int theFirstToken = lineTokenInfo.getPositionsOfNotEmptyElements().get(0);
            final int theFirstContinue = lineTokenInfo.getPositionsOfLineContinue().get(0);
            if (lineTokenInfo.getPositionsOfNotEmptyElements().size() > 1) {
                final int theSecondToken = lineTokenInfo.getPositionsOfNotEmptyElements().get(1);
                result = theFirstToken < theFirstContinue && theFirstContinue < theSecondToken;
            } else {
                result = theFirstToken < theFirstContinue;
            }
        }

        return result;
    }

    public ARobotSectionTable getTable(final RobotFile robotModel, final TableType type) {
        if (type == TableType.SETTINGS) {
            return robotModel.getSettingTable();
        } else if (type == TableType.VARIABLES) {
            return robotModel.getVariableTable();
        } else if (type == TableType.KEYWORD) {
            return robotModel.getKeywordTable();
        } else if (type == TableType.TEST_CASE) {
            return robotModel.getTestCaseTable();
        } else if (type == TableType.TASKS) {
            return robotModel.getTasksTable();
        }
        return null;
    }

    private static class LineTokenInfo {

        private final List<Integer> positionsOfNotEmptyElements = new ArrayList<>();

        private final List<Integer> positionsOfLineContinue = new ArrayList<>();

        private boolean isLineContinue;

        private int dataStartIndex = -1;

        private int dataEndIndex = -1;

        public static LineTokenInfo build(final List<IRobotLineElement> elements) {
            final LineTokenInfo lti = new LineTokenInfo();
            final int numberOfElements = elements.size();
            for (int elemIndex = 0; elemIndex < numberOfElements; elemIndex++) {
                final IRobotLineElement elem = elements.get(elemIndex);
                if (elem instanceof RobotToken) {
                    final RobotToken token = (RobotToken) elem;
                    final String tokenText = token.getText();
                    if (RobotTokenType.PREVIOUS_LINE_CONTINUE.getRepresentation().get(0).equals(tokenText)) {
                        lti.positionsOfLineContinue.add(elemIndex);
                        if (lti.positionsOfNotEmptyElements.isEmpty()) {
                            lti.isLineContinue = true;
                        }

                        if (lti.dataStartIndex == -1) {
                            lti.dataStartIndex = elemIndex;
                        }
                        lti.dataEndIndex = elemIndex;
                    } else if (tokenText != null && !"".equals(tokenText.trim())) {
                        lti.positionsOfNotEmptyElements.add(elemIndex);

                        if (lti.dataStartIndex == -1) {
                            lti.dataStartIndex = elemIndex;
                        }
                        lti.dataEndIndex = elemIndex;
                    }
                }
            }

            return lti;
        }

        public List<Integer> getPositionsOfNotEmptyElements() {
            return positionsOfNotEmptyElements;
        }

        public List<Integer> getPositionsOfLineContinue() {
            return positionsOfLineContinue;
        }

        public boolean isLineContinueTheFirst() {
            return isLineContinue;
        }

        public int getDataStartIndex() {
            return dataStartIndex;
        }

        public int getDataEndIndex() {
            return dataEndIndex;
        }
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
