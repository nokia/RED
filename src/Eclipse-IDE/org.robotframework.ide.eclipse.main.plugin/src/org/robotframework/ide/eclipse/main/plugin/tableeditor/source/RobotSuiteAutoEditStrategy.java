/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;

public class RobotSuiteAutoEditStrategy implements IAutoEditStrategy {

    private static final String VAR_WRAP_PATTERN = "\\w+";

    private final Supplier<String> separatorSupplier;

    private final boolean isSeparatorJumpModeEnabled;

    private final boolean isTsvFile;

    public RobotSuiteAutoEditStrategy(final Supplier<String> separatorSupplier,
            final boolean isSeparatorJumpModeEnabled, final boolean isTsvFile) {
        this.separatorSupplier = separatorSupplier;
        this.isSeparatorJumpModeEnabled = isSeparatorJumpModeEnabled;
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
        } catch (final BadLocationException | InterruptedException e) {
            // ok, no change in command then
        }
    }

    private void customizeZeroLengthDocumentCommand(final IDocument document, final DocumentCommand command)
            throws InterruptedException, BadLocationException {
        if ("\t".equals(command.text)) {
            replaceTabWithSpecifiedSeparatorOrJumpToNextCell(document, command);
        } else if (TextUtilities.endsWith(document.getLegalLineDelimiters(), command.text) != -1) {
            autoIndentAfterNewLine(document, command);
        } else if (AVariable.ROBOT_VAR_IDENTIFICATORS.contains(command.text)
                && (command.offset == document.getLength() || !"{".equals(document.get(command.offset, 1)))) {
            addVariableBrackets(command, "");
        }
    }

    private void customizePositiveLengthDocumentCommand(final IDocument document, final DocumentCommand command)
            throws BadLocationException {
        if (AVariable.ROBOT_VAR_IDENTIFICATORS.contains(command.text)) {
            final String selectedText = document.get(command.offset, command.length);
            if (selectedText.matches(VAR_WRAP_PATTERN)) {
                addVariableBrackets(command, selectedText);
            }
        } else if (command.length == 1 && command.text.isEmpty()
                && AVariable.ROBOT_VAR_IDENTIFICATORS.contains(document.get(command.offset - 1, 1))
                && document.get(command.offset, 2).equals("{}")) {
            // deleting empty variable brackets
            command.length = 2;
        }
    }

    private void replaceTabWithSpecifiedSeparatorOrJumpToNextCell(final IDocument document,
            final DocumentCommand command) throws BadLocationException {
        final Optional<Integer> jumpOffset = findRegionToJumpOf(document, command)
                .map(region -> region.getOffset() + region.getLength())
                .filter(offset -> offset > command.offset);
        if (jumpOffset.isPresent()) {
            command.text = null;
            command.shiftsCaret = false;
            command.caretOffset = jumpOffset.get();
        } else {
            command.text = getSeparator();
        }
    }

    private Optional<IRegion> findRegionToJumpOf(final IDocument document, final DocumentCommand command)
            throws BadLocationException {
        if (!isSeparatorJumpModeEnabled) {
            return Optional.empty();
        }
        final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(document, isTsvFile, command.offset);
        if (variableRegion.isPresent()) {
            return variableRegion;
        }
        return DocumentUtilities.findCellRegion(document, isTsvFile, command.offset);
    }

    private void addVariableBrackets(final DocumentCommand command, final String selectedText) {
        final String wrappedText = "{" + selectedText + "}";
        command.text += wrappedText;
        command.shiftsCaret = false;
        command.caretOffset = command.offset + wrappedText.length();
    }

    private void autoIndentAfterNewLine(final IDocument document, final DocumentCommand command)
            throws InterruptedException, BadLocationException {
        final RobotFileOutput rfo = ((RobotDocument) document).getNewestFileOutput();
        final int lineNumber = document.getLineOfOffset(command.offset);
        final List<RobotLine> contents = rfo.getFileModel().getFileContent();
        if (lineNumber < contents.size()) {
            final RobotLine currentLine = contents.get(lineNumber);
            autoIndentAfterNewLine(currentLine, DocumentUtilities.getDelimiter(document), command);
        }
    }

    private void autoIndentAfterNewLine(final RobotLine currentLine, final String lineDelimiter,
            final DocumentCommand command) {
        final Optional<IRobotLineElement> firstElem = getFirstElement(currentLine);
        if (!firstElem.isPresent()) {
            return;
        }

        final IRobotLineElement firstElement = firstElem.get();
        if (isDefinitionRequiringIndent(currentLine, command.offset)) {
            command.text += getSeparator();

        } else {
            String addedIndent = "";
            if (firstElement instanceof Separator || isEmptyCellToken(firstElement)) {
                final int length = Math.max(0,
                        Math.min(firstElement.getText().length(), command.offset - firstElement.getStartOffset()));
                addedIndent = firstElement.getText().substring(0, length);
                command.text += addedIndent;
            }

            final RobotToken firstMeaningfulToken = firstElement instanceof RobotToken ? (RobotToken) firstElement
                    : getSecondElement(currentLine).filter(RobotToken.class::isInstance)
                            .map(RobotToken.class::cast)
                            .orElse(RobotToken.create(""));

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
                command.text += "\\" + getSeparator();

            } else if (isRequiringContinuation(firstMeaningfulToken)) {
                command.text += "..." + getSeparator();

            }

        }
    }

    private Optional<IRobotLineElement> getFirstElement(final RobotLine line) {
        return line.elementsStream().findFirst();
    }

    private Optional<IRobotLineElement> getSecondElement(final RobotLine line) {
        return line.elementsStream().skip(1).findFirst();
    }

    private boolean isDefinitionRequiringIndent(final RobotLine currentLine, final int modificationStartOffset) {
        final Optional<RobotToken> firstToken = currentLine.tokensStream()
                .filter(token -> !token.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE))
                .findFirst();
        final boolean modificationStartsAfterTheToken = firstToken
                .filter(token -> token.getEndOffset() <= modificationStartOffset)
                .isPresent();
        if (modificationStartsAfterTheToken) {
            final List<IRobotTokenType> typesOfFirstToken = firstToken.map(RobotToken::getTypes)
                    .orElseGet(() -> new ArrayList<>());

            return typesOfFirstToken.contains(RobotTokenType.TEST_CASE_NAME)
                    || typesOfFirstToken.contains(RobotTokenType.TASK_NAME)
                    || typesOfFirstToken.contains(RobotTokenType.KEYWORD_NAME);
        }
        return false;
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
            final RobotToken firstToken = line.tokensStream().findFirst().orElseGet(() -> RobotToken.create(""));
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

    private String getSeparator() {
        return separatorSupplier.get();
    }
}
