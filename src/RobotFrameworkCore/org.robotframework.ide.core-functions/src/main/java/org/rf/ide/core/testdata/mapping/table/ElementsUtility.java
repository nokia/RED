/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.PreviousLineHandler;
import org.rf.ide.core.testdata.model.AKeywordBaseSetting;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.ECompareResult;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.exec.descs.ForDescriptorInfo;
import org.rf.ide.core.testdata.model.table.exec.descs.VariableExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
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

@SuppressWarnings("PMD.GodClass")
public class ElementsUtility {

    private final ParsingStateHelper parsingStateHelper;

    private final VariableExtractor varExtractor;

    public ElementsUtility() {
        this.parsingStateHelper = new ParsingStateHelper();
        this.varExtractor = new VariableExtractor();
    }

    public List<RobotToken> filter(final List<RobotToken> toks, final IRobotTokenType type) {
        final List<RobotToken> filtered = new ArrayList<>(0);

        for (final RobotToken token : toks) {
            if (token.getTypes().contains(type)) {
                filtered.add(token);
            }
        }

        return filtered;
    }

    public boolean isNewExecutableSection(final ALineSeparator separator, final RobotLine line) {
        boolean result = false;
        if (separator.getProducedType() == SeparatorType.PIPE) {
            final List<IRobotLineElement> lineElements = line.getLineElements();
            if (lineElements.size() == 1) {
                result = lineElements.get(0).getTypes().contains(SeparatorType.PIPE);
            }
        } else {
            result = line.getLineElements().isEmpty();
        }
        return result;
    }

    public LibraryImport findNearestLibraryImport(final RobotFileOutput robotFileOutput) {
        final AImported imported = getNearestImport(robotFileOutput);
        LibraryImport lib;
        if (imported instanceof LibraryImport) {
            lib = (LibraryImport) imported;
        } else {
            lib = null;

            // FIXME: sth wrong - declaration of library not inside setting
            // and was not catch by previous library declaration logic
        }
        return lib;
    }

    public RobotToken computeCorrectRobotToken(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final FilePosition fp, final String text, final boolean isNewLine,
            final List<RobotToken> robotTokens, final String fileName) {
        final ParsingState state = parsingStateHelper.getCurrentStatus(processingState);

        RobotToken correct = null;

        final List<VariableDeclaration> correctVariables = varExtractor
                .extract(FilePosition.createNotSet(), text, "fake")
                .getCorrectVariables();

        if (robotTokens.size() > 1) {
            final List<RobotToken> tokensExactlyOnPosition = getTokensExactlyOnPosition(robotTokens, fp);
            final TableType currentTable = state.getTable();
            if (tokensExactlyOnPosition.size() != 1 || currentTable == TableType.KEYWORD
                    || currentTable == TableType.TEST_CASE) {
                final List<RobotToken> headersPossible = findHeadersPossible(robotTokens);
                if (!headersPossible.isEmpty()) {
                    if (headersPossible.size() == 1) {
                        correct = headersPossible.get(0);
                    } else {
                        // FIXME: error
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
                    if (correct == null && (tableType == TableType.KEYWORD || tableType == TableType.TEST_CASE)) {
                        final ParsingState expected;
                        if (tableType == TableType.KEYWORD) {
                            expected = ParsingState.KEYWORD_DECLARATION;
                        } else {
                            expected = ParsingState.TEST_CASE_DECLARATION;
                        }
                        if (meetsState(state, expected) && tokensExactlyOnPosition.size() == 1) {
                            final RobotToken exactlyOne = tokensExactlyOnPosition.get(0);
                            final List<RobotTokenType> typesForVariablesTable = RobotTokenType
                                    .getTypesForVariablesTable();
                            boolean isVarDec = false;
                            for (final IRobotTokenType type : exactlyOne.getTypes()) {
                                if (type instanceof RobotTokenType) {
                                    final RobotTokenType tokenType = (RobotTokenType) type;
                                    if (typesForVariablesTable.contains(tokenType)
                                            && tokenType.isSettingDeclaration()) {
                                        if (!correctVariables.isEmpty()) {
                                            final VariableType typeByTokenType = VariableType.getTypeByTokenType(type);
                                            if (correctVariables.stream().anyMatch(c -> typeByTokenType
                                                    .getIdentificator().equals(c.getTypeIdentificator().getText()))) {
                                                isVarDec = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            if (isVarDec) {
                                correct = exactlyOne;
                            }
                        }
                    }

                    if (correct == null) {
                        if (ParsingState.getSettingsStates().contains(state) || currentTable == TableType.VARIABLES
                                || currentTable == TableType.KEYWORD || currentTable == TableType.TEST_CASE
                                || state == ParsingState.COMMENT) {
                            final RobotToken newRobotToken = new RobotToken();
                            newRobotToken.setLineNumber(fp.getLine());
                            newRobotToken.setStartColumn(fp.getColumn());
                            newRobotToken.setText(text);
                            newRobotToken.setType(RobotTokenType.UNKNOWN);
                            correct = newRobotToken;
                        } else {
                            // FIXME: info that nothing was found so token will
                            // be treat as UNKNOWN
                            final RobotToken newRobotToken = new RobotToken();
                            newRobotToken.setLineNumber(fp.getLine());
                            newRobotToken.setStartColumn(fp.getColumn());
                            newRobotToken.setText(text);
                            newRobotToken.setType(RobotTokenType.UNKNOWN);
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
                    final RobotToken newRobotToken = new RobotToken();
                    newRobotToken.setLineNumber(fp.getLine());
                    newRobotToken.setStartColumn(fp.getColumn());
                    newRobotToken.setText(text);
                    newRobotToken.setType(RobotTokenType.UNKNOWN);
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
                final RobotToken newRobotToken = new RobotToken();
                newRobotToken.setLineNumber(fp.getLine());
                newRobotToken.setStartColumn(fp.getColumn());
                newRobotToken.setText(text);
                if (text != null
                        && !(text.equals(token.getText()) || text.trim().equals(token.getText().trim()))) {
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
                                boolean notValidVar = true;
                                if (!correctVariables.isEmpty()) {
                                    final VariableType typeByTokenType = VariableType.getTypeByTokenType(type);
                                    for (final VariableDeclaration vd : correctVariables) {
                                        if (typeByTokenType.getIdentificator()
                                                .equals(vd.getTypeIdentificator().getText())) {
                                            notValidVar = false;
                                            break;
                                        }
                                    }
                                }

                                if (notValidVar) {
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

        boolean hasAnyProposalVariableInside = false;

        for (final RobotToken rt : robotTokens) {
            final List<IRobotTokenType> types = rt.getTypes();
            for (final IRobotTokenType type : types) {
                if (type == RobotTokenType.VARIABLES_DICTIONARY_DECLARATION
                        || type == RobotTokenType.VARIABLES_SCALAR_AS_LIST_DECLARATION
                        || type == RobotTokenType.VARIABLES_SCALAR_DECLARATION
                        || type == RobotTokenType.VARIABLES_LIST_DECLARATION
                        || type == RobotTokenType.VARIABLES_ENVIRONMENT_DECLARATION) {
                    if (!correctVariables.isEmpty()) {
                        final VariableType typeByTokenType = VariableType.getTypeByTokenType(type);
                        if (typeByTokenType.getIdentificator()
                                .equals(correctVariables.get(0).getTypeIdentificator().getText())) {
                            hasAnyProposalVariableInside = true;
                            break;
                        }
                    }
                }
            }
        }

        if (hasAnyProposalVariableInside && state != ParsingState.VARIABLE_TABLE_INSIDE) {
            correct.getTypes().add(RobotTokenType.VARIABLE_USAGE);
        }

        return correct;
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
        boolean result = false;
        for (final IRobotLineElement elem : line.getLineElements()) {
            if (isTableHeader(elem)) {
                result = true;
                break;
            }
        }

        return result;
    }

    public AImported getNearestImport(final RobotFileOutput robotFileOutput) {
        AImported result;
        final List<AImported> imports = robotFileOutput.getFileModel().getSettingTable().getImports();
        if (!imports.isEmpty()) {
            result = imports.get(imports.size() - 1);
        } else {
            result = null;
        }

        return result;
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
        } else if (tableHeaderState == ParsingState.KEYWORD_TABLE_HEADER) {
            tableKnownHeaders = fileModel.getKeywordTable().getHeaders();
        } else {
            // FIXME: error state not coherent
        }

        return tableKnownHeaders;
    }

    public boolean isTableHeader(final IRobotTokenType type) {
        return (type == RobotTokenType.SETTINGS_TABLE_HEADER || type == RobotTokenType.VARIABLES_TABLE_HEADER
                || type == RobotTokenType.TEST_CASES_TABLE_HEADER || type == RobotTokenType.KEYWORDS_TABLE_HEADER);
    }

    public boolean isTableHeader(final RobotToken t) {
        boolean result = false;
        final List<IRobotTokenType> declaredTypes = t.getTypes();
        for (final IRobotTokenType type : declaredTypes) {
            if (isTableHeader(type)) {
                result = true;
                break;
            }
        }

        if (!t.getText().trim().startsWith("*")) {
            result = false;
        }

        return result;
    }

    public boolean isTableHeader(final IRobotLineElement elem) {
        boolean result = false;
        if (elem instanceof RobotToken) {
            result = isTableHeader((RobotToken) elem);
        }

        return result;
    }

    public boolean isUserTableHeader(final RobotToken t) {
        boolean result = false;
        final String text = t.getText();
        if (text != null && text.length() > 1) {
            result = text.trim().startsWith("*");
        }

        return result;
    }

    public boolean checkIfFirstHasKeywordNameAlready(final List<? extends AKeywordBaseSetting<?>> keywordBased) {
        return !keywordBased.isEmpty() && keywordBased.get(0).getKeywordName() != null;
    }

    public boolean checkIfLastHasKeywordNameAlready(final List<? extends AKeywordBaseSetting<?>> keywordBased) {
        return !keywordBased.isEmpty() && keywordBased.get(keywordBased.size() - 1).getKeywordName() != null;
    }

    public boolean isNotOnlySeparatorOrEmptyLine(final RobotLine currentLine) {
        boolean anyValuableToken = false;
        final List<IRobotLineElement> lineElements = currentLine.getLineElements();
        for (final IRobotLineElement lineElem : lineElements) {
            if (lineElem instanceof RobotToken && !lineElem.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                anyValuableToken = true;
                break;
            }
        }
        return anyValuableToken;
    }

    public boolean shouldGiveEmptyToProcess(final RobotFileOutput parsingOutput, final ALineSeparator separator,
            final Separator currentSeparator, final RobotLine line, final Stack<ParsingState> processingState) {
        boolean result = false;

        final ParsingState state = parsingStateHelper.getCurrentStatus(processingState);
        final TableType tableType = state.getTable();
        final List<IRobotLineElement> splittedLine = separator.getSplittedLine();

        if (separator.getProducedType() == SeparatorType.PIPE && currentSeparator.getStartColumn() == 0) {
            result = false;
        } else if (separator.getProducedType() == SeparatorType.PIPE
                || separator instanceof StrictTsvTabulatorSeparator) {
            final LineTokenInfo lineTokenInfo = LineTokenInfo.build(splittedLine);
            if (!lineTokenInfo.getPositionsOfLineContinoue().isEmpty()
                    || !lineTokenInfo.getPositionsOfNotEmptyElements().isEmpty()) {
                // Logic: Grant valid to process empty elements in case:
                // SETTINGS or VARIABLES: always read empty the exclusion is
                // only that we have line continue and element is not the first
                // after header declaration
                // TEST CASES and KEYWORDS: read empty the exclusion is only
                // line continue in any case only first element is omitted
                // GRANT LOGIC main: always process start from beginning until
                // last not empty element
                final boolean isContinoue = lineTokenInfo.isLineContinoueTheFirst();
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
                } else if (tableType == TableType.TEST_CASE || tableType == TableType.KEYWORD) {
                    if (line.getLineElements().size() >= 2 || (line.getLineElements().size() == 1
                            && separator instanceof StrictTsvTabulatorSeparator)) {
                        if (isContinoue) {
                            result = lineTokenInfo.getDataStartIndex() <= separator.getCurrentElementIndex();
                        } else {
                            if (state == ParsingState.TEST_CASE_DECLARATION
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
                                            .getPositionsOfLineContinoue().get(0)
                                            || separator.getCurrentElementIndex() < lineTokenInfo.getDataStartIndex();
                                } else {
                                    result = true;
                                }
                            } else if (state == ParsingState.TEST_CASE_INSIDE_ACTION
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
            final boolean isContinoue = lineTokenInfo.isLineContinoueTheFirst();
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
            }
        }

        return result;
    }

    private boolean shouldTreatAsInlineContinue(final LineTokenInfo lineTokenInfo) {
        boolean result = false;

        if (!lineTokenInfo.getPositionsOfLineContinoue().isEmpty()
                && !lineTokenInfo.getPositionsOfNotEmptyElements().isEmpty()) {
            final int theFirstToken = lineTokenInfo.getPositionsOfNotEmptyElements().get(0);
            final int theFirstContinoue = lineTokenInfo.getPositionsOfLineContinoue().get(0);
            if (lineTokenInfo.getPositionsOfNotEmptyElements().size() > 1) {
                final int theSecondToken = lineTokenInfo.getPositionsOfNotEmptyElements().get(1);
                result = theFirstToken < theFirstContinoue && theFirstContinoue < theSecondToken;
            } else {
                result = theFirstToken < theFirstContinoue;
            }
        }

        return result;
    }

    public ARobotSectionTable getTable(final RobotFile robotModel, final TableType type) {
        ARobotSectionTable table = null;
        if (type == TableType.SETTINGS) {
            table = robotModel.getSettingTable();
        } else if (type == TableType.VARIABLES) {
            table = robotModel.getVariableTable();
        } else if (type == TableType.KEYWORD) {
            table = robotModel.getKeywordTable();
        } else if (type == TableType.TEST_CASE) {
            table = robotModel.getTestCaseTable();
        }

        return table;
    }

    private static class LineTokenInfo {

        private final List<Integer> positionsOfNotEmptyElements = new ArrayList<>();

        private final List<Integer> positionsOfLineContinoue = new ArrayList<>();

        private boolean isLineContinoue;

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
                        lti.positionsOfLineContinoue.add(elemIndex);
                        if (lti.positionsOfNotEmptyElements.isEmpty()) {
                            lti.isLineContinoue = true;
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

        public List<Integer> getPositionsOfLineContinoue() {
            return positionsOfLineContinoue;
        }

        public boolean isLineContinoueTheFirst() {
            return isLineContinoue;
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
