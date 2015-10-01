/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.robotframework.ide.core.testData.IRobotFileParser;
import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotFileOutput.BuildMessage;
import org.robotframework.ide.core.testData.model.RobotFileOutput.Status;
import org.robotframework.ide.core.testData.model.mapping.PreviousLineHandler;
import org.robotframework.ide.core.testData.model.mapping.PreviousLineHandler.LineContinueType;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.SettingsMapperProvider;
import org.robotframework.ide.core.testData.model.table.mapping.TestCaseMapperProvider;
import org.robotframework.ide.core.testData.model.table.mapping.UserKeywordMapperProvider;
import org.robotframework.ide.core.testData.model.table.mapping.VariablesDeclarationMapperProvider;
import org.robotframework.ide.core.testData.model.table.setting.mapping.UnknownSettingArgumentMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.UnknownSettingMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.library.LibraryAliasFixer;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseExecutableRowActionMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseExecutableRowArgumentMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordExecutableRowActionMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordExecutableRowArgumentMapper;
import org.robotframework.ide.core.testData.model.table.variables.mapping.UnknownVariableMapper;
import org.robotframework.ide.core.testData.model.table.variables.mapping.UnknownVariableValueMapper;
import org.robotframework.ide.core.testData.text.read.LineReader.Constant;
import org.robotframework.ide.core.testData.text.read.columnSeparators.ALineSeparator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.TokenSeparatorBuilder;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.SettingsRecognizersProvider;
import org.robotframework.ide.core.testData.text.read.recognizer.TestCaseRecognizersProvider;
import org.robotframework.ide.core.testData.text.read.recognizer.UserKeywordRecognizersProvider;
import org.robotframework.ide.core.testData.text.read.recognizer.VariablesDeclarationRecognizersProvider;

import com.google.common.annotations.VisibleForTesting;


public class TxtRobotFileParser implements IRobotFileParser {

    private final TokenSeparatorBuilder tokenSeparatorBuilder;
    private final List<ATokenRecognizer> recognized = new LinkedList<>();
    private final List<IParsingMapper> mappers = new LinkedList<>();
    private final List<IParsingMapper> unknownTableElementsMapper = new LinkedList<>();
    private final ElementsUtility utility;
    private final LibraryAliasFixer libraryFixer;
    private final PreviousLineHandler previousLineHandler;


    public TxtRobotFileParser() {
        this.utility = new ElementsUtility();
        this.tokenSeparatorBuilder = new TokenSeparatorBuilder();
        this.libraryFixer = new LibraryAliasFixer(utility);
        this.previousLineHandler = new PreviousLineHandler();

        recognized.addAll(new SettingsRecognizersProvider().getRecognizers());
        recognized.addAll(new VariablesDeclarationRecognizersProvider()
                .getRecognizers());
        recognized.addAll(new TestCaseRecognizersProvider().getRecognizers());
        recognized
                .addAll(new UserKeywordRecognizersProvider().getRecognizers());

        mappers.addAll(new SettingsMapperProvider().getMappers());
        mappers.addAll(new VariablesDeclarationMapperProvider().getMappers());
        mappers.addAll(new TestCaseMapperProvider().getMappers());
        mappers.addAll(new UserKeywordMapperProvider().getMappers());

        unknownTableElementsMapper.add(new UnknownSettingMapper());
        unknownTableElementsMapper.add(new UnknownSettingArgumentMapper());
        unknownTableElementsMapper.add(new UnknownVariableMapper());
        unknownTableElementsMapper.add(new UnknownVariableValueMapper());
        unknownTableElementsMapper.add(new TestCaseExecutableRowActionMapper());
        unknownTableElementsMapper
                .add(new TestCaseExecutableRowArgumentMapper());
        unknownTableElementsMapper.add(new KeywordExecutableRowActionMapper());
        unknownTableElementsMapper
                .add(new KeywordExecutableRowArgumentMapper());
    }


    @Override
    public boolean canParseFile(File file) {
        boolean result = false;

        if (file != null && file.isFile()) {
            String fileName = file.getName().toLowerCase();
            result = (fileName.endsWith(".txt") || fileName.endsWith(".robot"));
        }

        return result;
    }


    @Override
    public void parse(final RobotFileOutput parsingOutput,
            final InputStream inputStream, final File robotFile) {
        boolean wasProcessingError = false;
        try {
            parse(parsingOutput, robotFile, new InputStreamReader(inputStream,
                    Charset.forName("UTF-8")));
        } catch (Exception e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "Unknown problem during reading file " + robotFile
                            + ".\nStack:" + e, "File " + robotFile));
            // FIXME: stack trace adding
            e.printStackTrace();
            wasProcessingError = true;
        }

        if (wasProcessingError || parsingOutput.getStatus() == Status.FAILED) {
            parsingOutput.setStatus(Status.FAILED);
        } else {
            parsingOutput.setStatus(Status.PASSED);
        }

        parsingOutput.setProcessedFile(robotFile);
    }


    @Override
    public void parse(final RobotFileOutput parsingOutput, final File robotFile) {
        boolean wasProcessingError = false;
        try {
            FileInputStream fis = new FileInputStream(robotFile);
            parse(parsingOutput, fis, robotFile);
        } catch (FileNotFoundException e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "File " + robotFile + " was not found.\nStack:" + e,
                    "File " + robotFile));
            wasProcessingError = true;
            // FIXME: position should be more descriptive
        } catch (Exception e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "Unknown problem during reading file " + robotFile
                            + ".\nStack:" + e, "File " + robotFile));
            // FIXME: stack trace adding
            e.printStackTrace();
            wasProcessingError = true;
        }

        if (wasProcessingError || parsingOutput.getStatus() == Status.FAILED) {
            parsingOutput.setStatus(Status.FAILED);
        } else {
            parsingOutput.setStatus(Status.PASSED);
        }

        parsingOutput.setProcessedFile(robotFile);
    }


    private RobotFileOutput parse(final RobotFileOutput parsingOutput,
            final File robotFile, final Reader reader) {
        boolean wasProcessingError = false;
        previousLineHandler.clear();

        LineReader lineHolder = new LineReader(reader);
        BufferedReader lineReader = new BufferedReader(lineHolder);
        int lineNumber = 1;
        int currentOffset = 0;
        String currentLineText = null;
        final Stack<ParsingState> processingState = new Stack<>();
        boolean isNewLine = false;
        try {
            while((currentLineText = lineReader.readLine()) != null) {
                RobotLine line = new RobotLine(lineNumber,
                        parsingOutput.getFileModel());
                StringBuilder text = new StringBuilder(currentLineText);
                int lastColumnProcessed = 0;
                // get separator for this line
                ALineSeparator separator = tokenSeparatorBuilder
                        .createSeparator(lineNumber, currentLineText);
                RobotToken rt = null;

                int textLength = currentLineText.length();
                // check if is any data to process
                if (textLength > 0) {
                    // consume all data
                    while(lastColumnProcessed < textLength) {
                        if (separator.hasNext()) {
                            // iterate column-by-column in robot file
                            Separator currentSeparator = separator.next();
                            int startColumn = currentSeparator.getStartColumn();
                            int remainingData = startColumn
                                    - lastColumnProcessed;
                            // {$a} | {$b} in this case we check if {$a} was
                            // before
                            // '|' pipe separator
                            if (remainingData > 0
                                    || shouldGiveEmptyToProcess(line,
                                            processingState)) {
                                String rawText = text.substring(
                                        lastColumnProcessed, startColumn);

                                rt = processLineElement(line, processingState,
                                        parsingOutput, new FilePosition(
                                                lineNumber,
                                                lastColumnProcessed,
                                                currentOffset), rawText,
                                        robotFile.getName(), isNewLine);

                                rt.setStartOffset(currentOffset);
                                currentOffset += rt.getRaw().length();
                                line.addLineElement(rt);

                                extractPrettyAlignWhitespaces(line, rt, rawText);

                                isNewLine = false;
                            }

                            currentSeparator.setStartOffset(currentOffset);
                            currentOffset += currentSeparator.getRaw().length();
                            line.addLineElement(currentSeparator);
                            lastColumnProcessed = currentSeparator
                                    .getEndColumn();
                        } else {
                            // last element in line
                            if (utility.isNewExecutableSection(separator, line)) {
                                processingState
                                        .remove(ParsingState.TEST_CASE_DECLARATION);
                                processingState
                                        .remove(ParsingState.KEYWORD_DECLARATION);
                            }

                            String rawText = text
                                    .substring(lastColumnProcessed);

                            rt = processLineElement(
                                    line,
                                    processingState,
                                    parsingOutput,
                                    new FilePosition(lineNumber,
                                            lastColumnProcessed, currentOffset),
                                    rawText, robotFile.getName(), isNewLine);
                            rt.setStartOffset(currentOffset);
                            currentOffset += rt.getRaw().length();
                            line.addLineElement(rt);

                            extractPrettyAlignWhitespaces(line, rt, rawText);

                            lastColumnProcessed = textLength;
                            isNewLine = false;
                        }
                    }
                }

                List<Constant> endOfLine = lineHolder.getLineEnd(currentOffset);
                line.setEndOfLine(endOfLine, currentOffset, lastColumnProcessed);
                currentOffset += getEndOfLineLength(endOfLine);
                lineNumber++;
                lastColumnProcessed = 0;
                libraryFixer.checkAndFixLine(parsingOutput, processingState);
                /**
                 * special for case
                 * 
                 * <pre>
                 * *** Settings
                 * Suite Setup      Keyword
                 * 
                 * ...              argument_x
                 * </pre>
                 */
                if (isNotOnlySeparatorOrEmptyLine(line)) {
                    previousLineHandler.flushNew(processingState);
                }
                parsingOutput.getFileModel().addNewLine(line);
                utility.updateStatusesForNewLine(processingState);
                isNewLine = true;
            }
        } catch (FileNotFoundException e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "File " + robotFile + " was not found.\nStack:" + e,
                    "File " + robotFile));
            wasProcessingError = true;
            // FIXME: position should be more descriptive
        } catch (IOException e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "Problem during file " + robotFile + " reading.\nStack:"
                            + e.getLocalizedMessage(), "File " + robotFile));
            wasProcessingError = true;
        } catch (Exception e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "Unknown problem during reading file " + robotFile
                            + ".\nStack:" + e, "File " + robotFile));
            // FIXME: stack trace adding
            e.printStackTrace();
            wasProcessingError = true;
        } finally {
            try {
                lineReader.close();
            } catch (IOException e) {
                parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                        "Error occured, when file was closing. Stack trace\n\t"
                                + e, robotFile.getAbsolutePath()));
            }
        }

        if (wasProcessingError) {
            parsingOutput.setStatus(Status.FAILED);
        } else {
            parsingOutput.setStatus(Status.PASSED);
        }

        return parsingOutput;
    }


    @VisibleForTesting
    protected void extractPrettyAlignWhitespaces(RobotLine line, RobotToken rt,
            String rawText) {
        if (rawText.trim().length() > 0) {
            String correctedString = rawText;
            if (rawText.startsWith(" ")) {
                RobotToken prettyLeftAlign = new RobotToken();
                prettyLeftAlign.setStartOffset(rt.getStartOffset());
                prettyLeftAlign.setLineNumber(rt.getLineNumber());
                prettyLeftAlign.setStartColumn(rt.getStartColumn());
                prettyLeftAlign.setRaw(new StringBuilder(" "));
                prettyLeftAlign.setText(new StringBuilder(" "));
                prettyLeftAlign.setType(RobotTokenType.UNKNOWN);
                line.addLineElementAt(line.getLineElements().size() - 1,
                        prettyLeftAlign);

                rt.setStartColumn(rt.getStartColumn() + 1);
                rt.setStartOffset(rt.getStartOffset() + 1);
                correctedString = rawText.substring(1);
                rt.setText(new StringBuilder(correctedString));
                rt.setRaw(new StringBuilder(correctedString));
            }

            if (rawText.endsWith(" ")) {
                RobotToken prettyRightAlign = new RobotToken();
                prettyRightAlign.setStartOffset(rt.getStartOffset()
                        + rt.getRaw().length() - 1);
                prettyRightAlign.setLineNumber(rt.getLineNumber());
                prettyRightAlign.setStartColumn(rt.getEndColumn() - 1);
                prettyRightAlign.setRaw(new StringBuilder(" "));
                prettyRightAlign.setText(new StringBuilder(" "));
                prettyRightAlign.setType(RobotTokenType.UNKNOWN);
                line.addLineElement(prettyRightAlign);

                correctedString = correctedString.substring(0,
                        correctedString.length() - 1);
                rt.setText(new StringBuilder(correctedString));
                rt.setRaw(new StringBuilder(correctedString));
            }
        }
    }


    @VisibleForTesting
    protected boolean isNotOnlySeparatorOrEmptyLine(final RobotLine currentLine) {
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


    @VisibleForTesting
    protected boolean shouldGiveEmptyToProcess(final RobotLine line,
            final Stack<ParsingState> processingState) {
        boolean result = false;

        List<IRobotLineElement> lineElements = line.getLineElements();
        result = lineElements.size() >= 2;

        return result;
    }


    @VisibleForTesting
    protected int getEndOfLineLength(final List<Constant> eols) {
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


    @VisibleForTesting
    protected RobotToken processLineElement(RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final FilePosition fp,
            String text, String fileName, boolean isNewLine) {
        List<RobotToken> robotTokens = recognize(fp, text);
        RobotToken robotToken = utility.computeCorrectRobotToken(currentLine,
                processingState, robotFileOutput, fp, text, isNewLine,
                robotTokens);

        LineContinueType lineContinueType = previousLineHandler
                .computeLineContinue(processingState, isNewLine,
                        robotFileOutput.getFileModel(), currentLine, robotToken);

        boolean processThisElement = true;
        if (previousLineHandler.isSomethingToDo(lineContinueType)) {
            previousLineHandler.restorePreviousStack(lineContinueType,
                    processingState, currentLine, robotToken);

            processThisElement = (processingState.size() > 1)
                    && !robotToken.getTypes().contains(
                            RobotTokenType.PREVIOUS_LINE_CONTINUE);
        }

        if (processThisElement) {
            ParsingState newStatus = utility.getStatus(robotToken);
            if (robotToken != null) {
                if (!text.trim().equals(robotToken.getText().toString().trim())) {
                    // FIXME: add information that type is incorrect missing
                    // separator
                    RobotToken newRobotToken = new RobotToken();
                    newRobotToken.setText(new StringBuilder(text));
                    newRobotToken.setRaw(new StringBuilder(text));
                    newRobotToken.setType(RobotTokenType.UNKNOWN);
                    newRobotToken.getTypes().addAll(robotToken.getTypes());
                    robotToken = newRobotToken;
                    newStatus = ParsingState.UNKNOWN;
                }
            } else {
                robotToken = new RobotToken();
                robotToken.setLineNumber(fp.getLine());
                robotToken.setStartColumn(fp.getColumn());
                robotToken.setText(new StringBuilder(text));
                robotToken.setRaw(new StringBuilder(text));
                robotToken.setType(RobotTokenType.UNKNOWN);

                newStatus = ParsingState.UNKNOWN;
            }

            boolean useMapper = true;
            RobotFile fileModel = robotFileOutput.getFileModel();
            if (utility.isTableHeader(robotToken)) {
                if (utility.isTheFirstColumn(currentLine, robotToken)) {
                    @SuppressWarnings("rawtypes")
                    TableHeader header = new TableHeader(robotToken);
                    ARobotSectionTable table = null;
                    if (newStatus == ParsingState.SETTING_TABLE_HEADER) {
                        table = fileModel.getSettingTable();
                    } else if (newStatus == ParsingState.VARIABLE_TABLE_HEADER) {
                        table = fileModel.getVariableTable();
                    } else if (newStatus == ParsingState.TEST_CASE_TABLE_HEADER) {
                        table = fileModel.getTestCaseTable();
                    } else if (newStatus == ParsingState.KEYWORD_TABLE_HEADER) {
                        table = fileModel.getKeywordTable();
                    }

                    table.addHeader(header);
                    processingState.clear();
                    processingState.push(newStatus);

                    useMapper = false;
                } else {
                    // FIXME: add warning about wrong place
                }
            }

            if (useMapper) {
                List<IParsingMapper> matchedMappers = new LinkedList<>();
                for (IParsingMapper mapper : mappers) {
                    if (mapper.checkIfCanBeMapped(robotFileOutput, currentLine,
                            robotToken, text, processingState)) {
                        matchedMappers.add(mapper);
                    }
                }

                // check for unknown setting
                int size = matchedMappers.size();
                if (size == 0) {
                    for (IParsingMapper mapper : unknownTableElementsMapper) {
                        if (mapper.checkIfCanBeMapped(robotFileOutput,
                                currentLine, robotToken, text, processingState)) {
                            matchedMappers.add(mapper);
                        }
                    }
                }

                size = matchedMappers.size();
                if (size == 1) {
                    robotToken = matchedMappers.get(0).map(currentLine,
                            processingState, robotFileOutput, robotToken, fp,
                            text);
                } else {
                    robotFileOutput.addBuildMessage(BuildMessage
                            .createErrorMessage("Unknown data \'" + text
                                    + "\' appears in " + fp
                                    + ", during state: " + processingState,
                                    fileName));
                }
            }
        }

        fixNotSetPositions(robotToken, fp);

        return robotToken;
    }


    @VisibleForTesting
    protected void fixNotSetPositions(final RobotToken token,
            final FilePosition fp) {
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


    @VisibleForTesting
    protected List<RobotToken> recognize(final FilePosition fp, String text) {
        List<RobotToken> possibleRobotTokens = new LinkedList<>();
        StringBuilder sb = new StringBuilder(text);
        for (ATokenRecognizer rec : recognized) {
            if (rec.hasNext(sb, fp.getLine())) {
                RobotToken t = rec.next();
                t.setStartColumn(t.getStartColumn() + fp.getColumn());
                possibleRobotTokens.add(t);
            }
        }

        if (possibleRobotTokens.isEmpty()) {
            RobotToken rt = new RobotToken();
            rt.setLineNumber(fp.getLine());
            rt.setText(new StringBuilder(text));
            rt.setRaw(new StringBuilder(text));
            rt.setStartColumn(fp.getColumn());

            possibleRobotTokens.add(rt);
        }

        return possibleRobotTokens;
    }
}
