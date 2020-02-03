/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

public class RobotSuiteAutoEditStrategy implements IAutoEditStrategy {

    private final EditStrategyPreferences preferences;

    private final boolean isTsvFile;

    public RobotSuiteAutoEditStrategy(final EditStrategyPreferences preferences, final boolean isTsvFile) {
        this.preferences = preferences;
        this.isTsvFile = isTsvFile;
    }

    @Override
    public void customizeDocumentCommand(final IDocument document, final DocumentCommand command) {
        if (command.offset == -1 || command.text == null) {
            return;
        }

        try {
            if (command.length == 0) {
                customizeZeroLengthDocumentCommand(document, command);
            } else if (command.length > 0) {
                customizePositiveLengthDocumentCommand(document, command);
            }
        } catch (final BadLocationException e) {
            // ok, no change in command then
        }
    }

    private void customizeZeroLengthDocumentCommand(final IDocument document, final DocumentCommand command)
            throws BadLocationException {
        if ("\t".equals(command.text)) {
            replaceTabWithSpecifiedSeparatorOrJumpOutOfRegion(document, command);
        } else if (TextUtilities.endsWith(document.getLegalLineDelimiters(), command.text) != -1) {
            findCurrentLine(document, command.offset).ifPresent(currentLine -> autoIndentAfterNewLine(currentLine,
                    DocumentUtilities.getDelimiter(document), command));
        } else if (preferences.isVariablesBracketsInsertionEnabled()
                && AVariable.ROBOT_VAR_IDENTIFICATORS.contains(command.text)
                && (command.offset == document.getLength() || !"{".equals(document.get(command.offset, 1)))) {
            addVariableBrackets(command, "");
        }
    }

    private void customizePositiveLengthDocumentCommand(final IDocument document, final DocumentCommand command)
            throws BadLocationException {
        if (preferences.isVariablesBracketsInsertionWrappingEnabled()
                && AVariable.ROBOT_VAR_IDENTIFICATORS.contains(command.text)) {
            final String selectedText = document.get(command.offset, command.length);
            if (selectedText.matches(preferences.getVariablesBracketsInsertionWrappingPattern())) {
                addVariableBrackets(command, selectedText);
            }
        } else if (preferences.isVariablesBracketsInsertionEnabled() && command.length == 1 && command.text.isEmpty()
                && AVariable.ROBOT_VAR_IDENTIFICATORS.contains(document.get(command.offset - 1, 1))
                && document.get(command.offset, 2).equals("{}")) {
            // deleting empty variable brackets
            command.length = 2;
        }
    }

    private void replaceTabWithSpecifiedSeparatorOrJumpOutOfRegion(final IDocument document,
            final DocumentCommand command) throws BadLocationException {
        final Optional<Integer> jumpOffset = findRegionToJumpOf(document, command.offset)
                .map(region -> region.getOffset() + region.getLength())
                .filter(offset -> offset > command.offset);
        if (jumpOffset.isPresent()) {
            command.text = null;
            command.shiftsCaret = false;
            command.caretOffset = jumpOffset.get();
        } else {
            command.text = preferences.getSeparatorToUse(isTsvFile);
        }
    }

    private Optional<IRegion> findRegionToJumpOf(final IDocument document, final int offset)
            throws BadLocationException {
        if (!preferences.isSeparatorJumpModeEnabled()) {
            return Optional.empty();
        }
        final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(document, isTsvFile, offset)
                .filter(region -> region.getOffset() < offset);
        if (variableRegion.isPresent()) {
            return variableRegion;
        }
        return DocumentUtilities.findCellRegion(document, isTsvFile, offset)
                .filter(region -> region.getOffset() < offset);
    }

    private Optional<RobotLine> findCurrentLine(final IDocument document, final int offset)
            throws BadLocationException {
        try {
            return ((RobotDocument) document).getNewestModel().getRobotLineBy(offset);
        } catch (final InterruptedException e) {
            // ok, nothing found
        }
        return Optional.empty();
    }

    private void addVariableBrackets(final DocumentCommand command, final String selectedText) {
        final String wrappedText = "{" + selectedText + "}";
        command.text += wrappedText;
        command.shiftsCaret = false;
        command.caretOffset = command.offset + wrappedText.length();
    }

    private void autoIndentAfterNewLine(final RobotLine currentLine, final String lineDelimiter,
            final DocumentCommand command) {
        getFirstElement(currentLine).ifPresent(firstElement -> {
            if (isDefinitionRequiringIndent(currentLine, command.offset)) {
                command.text += preferences.getSeparatorToUse(isTsvFile);

            } else {
                final String addedIndent = countAddedIndent(firstElement, command.offset);
                command.text += addedIndent;

                final RobotToken firstMeaningfulToken = firstElement instanceof RobotToken ? (RobotToken) firstElement
                        : getSecondElement(currentLine).filter(RobotToken.class::isInstance)
                                .map(RobotToken.class::cast)
                                .orElseGet(RobotToken::new);

                if (command.offset < firstMeaningfulToken.getEndOffset()) {
                    // we're modifying region before the end of the token so we don't want to add
                    // anything more
                    return;
                }

                if (isEndTerminatedForLoopStyle(firstMeaningfulToken)) {
                    command.text += addedIndent;

                    if (endTerminatedForLoopRequiresEnd(currentLine)) {
                        command.shiftsCaret = false;
                        command.caretOffset = command.offset + command.text.length();
                        command.text += lineDelimiter + addedIndent + "END";
                    }

                } else if (isIndentedForLoopStyle(firstMeaningfulToken)) {
                    command.text += "\\" + preferences.getSeparatorToUse(isTsvFile);

                } else if (!isPrettyAlignSpace(firstMeaningfulToken)) {
                    final int nextElementEndOffset = getNextElement(currentLine, command.offset)
                            .map(IRobotLineElement::getEndOffset)
                            .orElse(-1);
                    getCurrentElement(currentLine, command.offset).ifPresent(currentElement -> {
                        if (nextElementEndOffset > 0 && currentElement instanceof Separator) {
                            command.text += "..." + currentElement.getText();
                            command.offset = currentElement.getStartOffset();
                            command.length = currentElement.getEndOffset() - currentElement.getStartOffset();
                        } else if (nextElementEndOffset > 0 && currentElement.getEndOffset() == command.offset
                                && getNextElement(currentLine, nextElementEndOffset).isPresent()) {
                            command.text += "...";
                            command.caretOffset = nextElementEndOffset;
                        } else if (isRequiringContinuation(firstMeaningfulToken)) {
                            command.text += "..." + preferences.getSeparatorToUse(isTsvFile);
                        }
                    });

                }

            }
        });
    }

    private String countAddedIndent(final IRobotLineElement firstElement, final int offset) {
        if (firstElement instanceof Separator || isEmptyCellToken(firstElement)) {
            final int length = Math.max(0,
                    Math.min(firstElement.getText().length(), offset - firstElement.getStartOffset()));
            return firstElement.getText().substring(0, length);
        }
        return "";
    }

    private Optional<IRobotLineElement> getFirstElement(final RobotLine line) {
        return line.elementsStream().findFirst();
    }

    private Optional<IRobotLineElement> getSecondElement(final RobotLine line) {
        return line.elementsStream().skip(1).findFirst();
    }

    private Optional<IRobotLineElement> getCurrentElement(final RobotLine line, final int offset) {
        return line.elementsStream().filter(e -> offset <= e.getEndOffset()).findFirst();
    }

    private Optional<IRobotLineElement> getNextElement(final RobotLine line, final int offset) {
        return line.elementsStream().filter(e -> offset <= e.getStartOffset()).findFirst();
    }

    private boolean isDefinitionRequiringIndent(final RobotLine currentLine, final int offset) {
        final Optional<RobotToken> firstToken = currentLine.tokensStream()
                .filter(token -> !isPrettyAlignSpace(token))
                .findFirst();
        if (firstToken.filter(token -> token.getEndOffset() <= offset).isPresent()) {
            final List<IRobotTokenType> typesOfFirstToken = firstToken.map(RobotToken::getTypes)
                    .orElseGet(ArrayList::new);

            return typesOfFirstToken.contains(RobotTokenType.TEST_CASE_NAME)
                    || typesOfFirstToken.contains(RobotTokenType.TASK_NAME)
                    || typesOfFirstToken.contains(RobotTokenType.KEYWORD_NAME);
        }
        return false;
    }

    private boolean isPrettyAlignSpace(final RobotToken token) {
        return token.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE);
    }

    private boolean isEmptyCellToken(final IRobotLineElement firstElement) {
        return firstElement.getTypes().contains(RobotTokenType.EMPTY_CELL);
    }

    private boolean isEndTerminatedForLoopStyle(final RobotToken token) {
        return token.getTypes().contains(RobotTokenType.FOR_WITH_END);
    }

    private boolean endTerminatedForLoopRequiresEnd(final RobotLine currentLine) {
        final List<RobotLine> allLines = currentLine.getParent().getFileContent();
        for (int i = allLines.indexOf(currentLine) + 1; i < allLines.size(); i++) {
            final RobotLine line = allLines.get(i);
            final IRobotLineElement firstElement = line.elementsStream().findFirst().orElse(null);
            if (firstElement instanceof RobotToken && !isEmptyCellToken(firstElement)) {
                return true;
            }
            final RobotToken firstToken = line.tokensStream().findFirst().orElseGet(RobotToken::new);
            if (firstToken.getTypes().contains(RobotTokenType.FOR_END_TOKEN)) {
                return false;
            }
        }
        return true;
    }

    private boolean isIndentedForLoopStyle(final RobotToken token) {
        return token.getTypes().contains(RobotTokenType.FOR_TOKEN) && !isEndTerminatedForLoopStyle(token)
                || token.getTypes().contains(RobotTokenType.FOR_CONTINUE_TOKEN);
    }

    private boolean isRequiringContinuation(final RobotToken token) {
        final List<IRobotTokenType> types = token.getTypes();
        return types.contains(RobotTokenType.PREVIOUS_LINE_CONTINUE)
                || types.contains(RobotTokenType.SETTING_DOCUMENTATION_DECLARATION)
                || types.contains(RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION)
                || types.contains(RobotTokenType.TASK_SETTING_DOCUMENTATION)
                || types.contains(RobotTokenType.KEYWORD_SETTING_DOCUMENTATION);
    }

    static class EditStrategyPreferences {

        // caches interesting preferences

        private final RedPreferences preferences;

        private String separatorToUseInTsv;

        private String separatorToUseInRobot;

        private boolean isSeparatorJumpModeEnabled;

        private boolean isVariablesBracketsInsertionEnabled;

        private boolean isVariablesBracketsInsertionWrappingEnabled;

        private String variablesBracketsInsertionWrappingPattern;

        EditStrategyPreferences(final RedPreferences preferences) {
            this.preferences = preferences;
        }

        void refresh() {
            separatorToUseInRobot = preferences.getSeparatorToUse(false);
            separatorToUseInTsv = preferences.getSeparatorToUse(true);
            isSeparatorJumpModeEnabled = preferences.isSeparatorJumpModeEnabled();
            isVariablesBracketsInsertionEnabled = preferences.isVariablesBracketsInsertionEnabled();
            isVariablesBracketsInsertionWrappingEnabled = preferences.isVariablesBracketsInsertionWrappingEnabled();
            variablesBracketsInsertionWrappingPattern = preferences.getVariablesBracketsInsertionWrappingPattern();
        }

        String getSeparatorToUse(final boolean isTsvFile) {
            return isTsvFile ? separatorToUseInTsv : separatorToUseInRobot;
        }

        boolean isSeparatorJumpModeEnabled() {
            return isSeparatorJumpModeEnabled;
        }

        boolean isVariablesBracketsInsertionEnabled() {
            return isVariablesBracketsInsertionEnabled;
        }

        boolean isVariablesBracketsInsertionWrappingEnabled() {
            return isVariablesBracketsInsertionWrappingEnabled;
        }

        String getVariablesBracketsInsertionWrappingPattern() {
            return variablesBracketsInsertionWrappingPattern;
        }
    }
}
