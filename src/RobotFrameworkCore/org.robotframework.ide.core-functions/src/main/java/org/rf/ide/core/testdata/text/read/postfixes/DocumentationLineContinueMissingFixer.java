/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.postfixes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.setting.SuiteDocumentation;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.model.table.testcases.TestDocumentation;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

/**
 * @author wypych
 */
public class DocumentationLineContinueMissingFixer implements IPostProcessFixAction {

    @Override
    public void applyFix(final RobotFileOutput parsingOutput) {
        final RobotFile model = parsingOutput.getFileModel();
        final List<RobotLine> fileContent = model.getFileContent();
        final SettingTable settingTable = model.getSettingTable();
        if (settingTable.isPresent()) {
            suiteDocumentationApplyContinueTokens(fileContent, settingTable);
        }

        final KeywordTable keywordTable = model.getKeywordTable();
        if (keywordTable.isPresent()) {
            keywordsDocumentationApplyContinueTokens(fileContent, keywordTable);
        }

        final TestCaseTable testCaseTable = model.getTestCaseTable();
        if (testCaseTable.isPresent()) {
            testsDocumentationApplyContinueTokens(fileContent, testCaseTable);
        }
    }

    private void suiteDocumentationApplyContinueTokens(final List<RobotLine> fileContent,
            final SettingTable settingTable) {
        final List<SuiteDocumentation> docDeclarations = settingTable.getDocumentation();
        for (final SuiteDocumentation docDec : docDeclarations) {
            final RobotToken declaration = docDec.getDeclaration();
            docDec.clearDocumentation();
            final List<RobotToken> docTokens = tokensBelongs(fileContent, declaration,
                    RobotTokenType.SETTING_DOCUMENTATION_TEXT, RobotTokenType.PRETTY_ALIGN_SPACE,
                    RobotTokenType.PREVIOUS_LINE_CONTINUE);
            for (final RobotToken textDoc : docTokens) {
                docDeclarations.get(0).addDocumentationText(textDoc);
            }
        }
    }

    private void keywordsDocumentationApplyContinueTokens(final List<RobotLine> fileContent,
            final KeywordTable keywordTable) {
        final List<UserKeyword> keywords = keywordTable.getKeywords();
        for (final UserKeyword keyword : keywords) {
            final List<KeywordDocumentation> documentation = keyword.getDocumentation();
            for (final KeywordDocumentation keywordDocumentation : documentation) {
                final RobotToken declaration = keywordDocumentation.getDeclaration();
                keywordDocumentation.clearDocumentation();
                final List<RobotToken> docTokens = tokensBelongs(fileContent, declaration,
                        RobotTokenType.KEYWORD_SETTING_DOCUMENTATION_TEXT, RobotTokenType.PRETTY_ALIGN_SPACE,
                        RobotTokenType.PREVIOUS_LINE_CONTINUE);
                for (final RobotToken textDoc : docTokens) {
                    documentation.get(0).addDocumentationText(textDoc);
                }
            }
        }
    }

    private void testsDocumentationApplyContinueTokens(final List<RobotLine> fileContent,
            final TestCaseTable testCaseTable) {
        final List<TestCase> testCases = testCaseTable.getTestCases();
        for (final TestCase testCase : testCases) {
            final List<TestDocumentation> documentation = testCase.getDocumentation();
            for (final TestDocumentation testDocumentation : documentation) {
                final RobotToken declaration = testDocumentation.getDeclaration();
                testDocumentation.clearDocumentation();
                final List<RobotToken> docTokens = tokensBelongs(fileContent, declaration,
                        RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION_TEXT, RobotTokenType.PRETTY_ALIGN_SPACE,
                        RobotTokenType.PREVIOUS_LINE_CONTINUE);
                for (final RobotToken textDoc : docTokens) {
                    documentation.get(0).addDocumentationText(textDoc);
                }
            }
        }
    }

    @VisibleForTesting
    protected List<RobotToken> tokensBelongs(final List<RobotLine> fileContent, final RobotToken declaration,
            final IRobotTokenType... acceptable) {
        final FilePosition declarationPosition = declaration.getFilePosition();

        if (declarationPosition.isNotSet()) {
            return tokensBelongsByToken(fileContent, 0, declaration, acceptable);
        } else {
            return tokensBelongsByToken(fileContent, declarationPosition.getLine() - 1, declaration, acceptable);
        }
    }

    private List<RobotToken> tokensBelongsByToken(final List<RobotLine> fileContent, final int searchStartNumber,
            final RobotToken declaration, final IRobotTokenType... acceptable) {
        final List<RobotToken> toks = new ArrayList<>(0);

        Optional<Integer> elementPositionInLine = Optional.absent();
        int lines = fileContent.size();
        boolean fetchMode = false;
        for (int lineNumber = searchStartNumber; lineNumber < lines; lineNumber++) {
            final RobotLine line = fileContent.get(lineNumber);
            if (fetchMode) {

                final List<IRobotLineElement> lineElements = line.getLineElements();
                for (int i = 0; i < lineElements.size(); i++) {
                    final IRobotLineElement elem = lineElements.get(i);
                    final List<IRobotTokenType> elemTypes = elem.getTypes();
                    if (isAnyOfType(elemTypes, SeparatorType.PIPE, SeparatorType.TABULATOR_OR_DOUBLE_SPACE,
                            RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE)) {
                        continue;
                    } else if (isAnyOfType(elemTypes, acceptable)) {
                        if (elemTypes.contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)
                                && !elem.getText().trim().equals("...")) {
                            return toks;
                        }
                        toks.add((RobotToken) elem);
                    } else {
                        return toks;
                    }
                }

            } else if (elementPositionInLine.isPresent()) {
                // line with declaration
                final List<IRobotLineElement> lineElements = line.getLineElements();
                for (int i = elementPositionInLine.get() + 1; i < lineElements.size(); i++) {
                    final IRobotLineElement elem = lineElements.get(i);
                    final List<IRobotTokenType> elemTypes = elem.getTypes();
                    if (isAnyOfType(elemTypes, SeparatorType.PIPE, SeparatorType.TABULATOR_OR_DOUBLE_SPACE,
                            RobotTokenType.START_HASH_COMMENT, RobotTokenType.COMMENT_CONTINUE)) {
                        continue;
                    } else if (isAnyOfType(elemTypes, acceptable)) {
                        toks.add((RobotToken) elem);
                    } else {
                        return toks;
                    }
                }

                fetchMode = true;
            } else {
                elementPositionInLine = line.getElementPositionInLine(declaration);
                if (elementPositionInLine.isPresent()) {
                    lineNumber--;
                }
            }
        }
        return toks;
    }

    private boolean isAnyOfType(final List<IRobotTokenType> elemTypes, final IRobotTokenType... acceptable) {
        final List<IRobotTokenType> accept = Arrays.asList(acceptable);

        for (final IRobotTokenType e : elemTypes) {
            if (accept.contains(e)) {
                return true;
            }
        }

        return false;
    }
}
