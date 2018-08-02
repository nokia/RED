/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read;

import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.IRobotFileParser;
import org.rf.ide.core.testdata.mapping.PreviousLineHandler;
import org.rf.ide.core.testdata.mapping.PreviousLineHandler.LineContinueType;
import org.rf.ide.core.testdata.mapping.keywords.KeywordExecutableRowActionMapper;
import org.rf.ide.core.testdata.mapping.keywords.KeywordExecutableRowArgumentMapper;
import org.rf.ide.core.testdata.mapping.setting.UnknownSettingArgumentMapper;
import org.rf.ide.core.testdata.mapping.setting.UnknownSettingMapper;
import org.rf.ide.core.testdata.mapping.setting.imports.LibraryAliasFixer;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver.PositionExpected;
import org.rf.ide.core.testdata.mapping.table.ElementsUtility;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.MetadataOldSyntaxUtility;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.mapping.table.PrettyAlignSpaceUtility;
import org.rf.ide.core.testdata.mapping.table.SettingsMapperProvider;
import org.rf.ide.core.testdata.mapping.table.TestCaseMapperProvider;
import org.rf.ide.core.testdata.mapping.table.UserKeywordMapperProvider;
import org.rf.ide.core.testdata.mapping.table.VariablesDeclarationMapperProvider;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseExecutableRowActionMapper;
import org.rf.ide.core.testdata.mapping.testcases.TestCaseExecutableRowArgumentMapper;
import org.rf.ide.core.testdata.mapping.variables.CommonVariableHelper;
import org.rf.ide.core.testdata.mapping.variables.UnknownVariableMapper;
import org.rf.ide.core.testdata.mapping.variables.UnknownVariableValueMapper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.RobotFileOutput.Status;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.LineReader.Constant;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.SettingsRecognizersProvider;
import org.rf.ide.core.testdata.text.read.recognizer.TestCaseRecognizersProvider;
import org.rf.ide.core.testdata.text.read.recognizer.UserKeywordRecognizersProvider;
import org.rf.ide.core.testdata.text.read.recognizer.VariablesDeclarationRecognizersProvider;
import org.rf.ide.core.testdata.text.read.separators.ALineSeparator;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder;

import com.google.common.annotations.VisibleForTesting;

@SuppressWarnings("PMD.GodClass")
public abstract class ATextualRobotFileParser implements IRobotFileParser {

    private final List<ATokenRecognizer> recognized = new ArrayList<>();

    private final List<IParsingMapper> mappers = new ArrayList<>();

    private final List<IParsingMapper> unknownTableElementsMapper = new ArrayList<>();

    private final ElementsUtility utility;

    private final PrettyAlignSpaceUtility alignUtility;

    private final MetadataOldSyntaxUtility metadataUtility;

    private final ParsingStateHelper parsingStateHelper;

    private final LibraryAliasFixer libraryFixer;

    private final PreviousLineHandler previousLineHandler;

    private final CommonVariableHelper variableHelper;

    private final ElementPositionResolver positionResolvers;

    protected final TokenSeparatorBuilder tokenSeparatorBuilder;

    private final PostProcessingFixActions postFixerActions;

    public ATextualRobotFileParser(final TokenSeparatorBuilder tokenSeparatorBuilder) {
        this.tokenSeparatorBuilder = tokenSeparatorBuilder;
        this.utility = new ElementsUtility();
        this.metadataUtility = new MetadataOldSyntaxUtility();
        this.alignUtility = new PrettyAlignSpaceUtility();
        this.variableHelper = new CommonVariableHelper();
        this.parsingStateHelper = new ParsingStateHelper();
        this.libraryFixer = new LibraryAliasFixer(utility, parsingStateHelper);
        this.previousLineHandler = new PreviousLineHandler();
        this.positionResolvers = new ElementPositionResolver();
        this.postFixerActions = new PostProcessingFixActions();

        unknownTableElementsMapper.add(new UnknownSettingMapper());
        unknownTableElementsMapper.add(new UnknownSettingArgumentMapper());
        unknownTableElementsMapper.add(new UnknownVariableMapper());
        unknownTableElementsMapper.add(new UnknownVariableValueMapper());
        unknownTableElementsMapper.add(new TestCaseExecutableRowActionMapper());
        unknownTableElementsMapper.add(new TestCaseExecutableRowArgumentMapper());
        unknownTableElementsMapper.add(new KeywordExecutableRowActionMapper());
        unknownTableElementsMapper.add(new KeywordExecutableRowArgumentMapper());

    }

    @Override
    public void parse(final RobotFileOutput parsingOutput, final InputStream inputStream, final File robotFile) {
        initalizeRecognizersAndMappers(parsingOutput.getRobotVersion());

        boolean wasProcessingError = false;
        try {
            parse(parsingOutput, robotFile, new InputStreamReader(inputStream, Charset.forName("UTF-8")));
        } catch (final Exception e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "Unknown problem during reading file " + robotFile + ".\nStack:" + e, "File " + robotFile));
            // FIXME: stack trace adding
            System.err.println("File: " + robotFile);
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
        initalizeRecognizersAndMappers(parsingOutput.getRobotVersion());

        boolean wasProcessingError = false;
        try {
            final FileInputStream fis = new FileInputStream(robotFile);
            parse(parsingOutput, fis, robotFile);
        } catch (final FileNotFoundException e) {
            parsingOutput.addBuildMessage(BuildMessage
                    .createErrorMessage("File " + robotFile + " was not found.\nStack:" + e, "File " + robotFile));
            wasProcessingError = true;
            // FIXME: position should be more descriptive
        } catch (final Exception e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "Unknown problem during reading file " + robotFile + ".\nStack:" + e, "File " + robotFile));
            // FIXME: stack trace adding
            System.err.println("File: " + robotFile);
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

    private void initalizeRecognizersAndMappers(final RobotVersion robotVersion) {
        mappers.clear();
        mappers.addAll(new SettingsMapperProvider().getMappers(robotVersion));
        mappers.addAll(new VariablesDeclarationMapperProvider().getMappers());
        mappers.addAll(new TestCaseMapperProvider().getMappers(robotVersion));
        mappers.addAll(new UserKeywordMapperProvider().getMappers(robotVersion));

        recognized.clear();
        recognized.addAll(new SettingsRecognizersProvider().getRecognizers(robotVersion));
        recognized.addAll(new VariablesDeclarationRecognizersProvider().getRecognizers());
        recognized.addAll(new TestCaseRecognizersProvider().getRecognizers(robotVersion));
        recognized.addAll(new UserKeywordRecognizersProvider().getRecognizers(robotVersion));
    }

    private RobotFileOutput parse(final RobotFileOutput parsingOutput, final File robotFile, final Reader reader) {
        boolean wasProcessingError = false;
        previousLineHandler.clear();

        parsingOutput.setProcessedFile(robotFile);
        final LineReader lineHolder = new LineReader(reader);
        final BufferedReader lineReader = new BufferedReader(lineHolder);
        int lineNumber = 1;
        int currentOffset = 0;
        String currentLineText = null;
        final Stack<ParsingState> processingState = new Stack<>();
        boolean isNewLine = false;
        try {
            final RobotFile fileModel = parsingOutput.getFileModel();
            while ((currentLineText = lineReader.readLine()) != null) {
                final RobotLine line = new RobotLine(lineNumber, fileModel);
                currentOffset = handleCRLFcaseSplittedBetweenBuffers(parsingOutput, lineHolder, lineNumber,
                        currentOffset);
                // removing BOM
                if (currentLineText.toCharArray().length > 0) {
                    if (currentLineText.charAt(0) == 0xFEFF) {
                        currentOffset++;
                    }
                }
                currentLineText = currentLineText.replace("\uFEFF", "");

                final StringBuilder text = new StringBuilder(currentLineText);
                int lastColumnProcessed = 0;
                // get separator for this line
                final ALineSeparator separator = tokenSeparatorBuilder.createSeparator(lineNumber, currentLineText);
                line.setSeparatorType(separator.getProducedType());
                RobotToken rt = null;

                final int textLength = currentLineText.length();
                // check if is any data to process
                if (isPrettyAlignLineOnly(currentLineText)) {

                    rt = processEmptyLine(line, processingState, parsingOutput,
                            new FilePosition(lineNumber, lastColumnProcessed, currentOffset), currentLineText,
                            robotFile.getName(), isNewLine);
                    rt.setStartOffset(currentOffset);
                    line.addLineElement(rt);

                    currentOffset += textLength;
                    lastColumnProcessed = textLength;

                } else {
                    // consume all data
                    while (lastColumnProcessed < textLength) {
                        if (separator.hasNext()) {
                            // iterate column-by-column in robot file
                            final Separator currentSeparator = separator.next();
                            final int startColumn = currentSeparator.getStartColumn();
                            final int remainingData = startColumn - lastColumnProcessed;
                            // {$a} | {$b} in this case we check if {$a} was
                            // before '|' pipe separator
                            if (remainingData > 0 || utility.shouldGiveEmptyToProcess(parsingOutput, separator,
                                    currentSeparator, line, processingState)) {
                                final String rawText = text.substring(lastColumnProcessed, startColumn);

                                rt = processLineElement(line, processingState, parsingOutput,
                                        new FilePosition(lineNumber, lastColumnProcessed, currentOffset), rawText,
                                        robotFile.getName(), isNewLine);

                                rt.setStartOffset(currentOffset);
                                currentOffset += rt.getText().length();
                                line.addLineElement(rt);

                                metadataUtility.fixSettingMetadata(parsingOutput, line, rt, processingState);
                                alignUtility.extractPrettyAlignWhitespaces(line, rt, rawText);

                                isNewLine = false;
                            }

                            currentSeparator.setStartOffset(currentOffset);
                            currentOffset += currentSeparator.getRaw().length();
                            line.addLineElement(currentSeparator);
                            lastColumnProcessed = currentSeparator.getEndColumn();
                        } else {
                            // last element in line
                            if (utility.isNewExecutableSection(separator, line)) {
                                processingState.remove(ParsingState.TEST_CASE_DECLARATION);
                                processingState.remove(ParsingState.KEYWORD_DECLARATION);
                            }

                            final String rawText = text.substring(lastColumnProcessed);

                            rt = processLineElement(line, processingState, parsingOutput,
                                    new FilePosition(lineNumber, lastColumnProcessed, currentOffset), rawText,
                                    robotFile.getName(), isNewLine);
                            rt.setStartOffset(currentOffset);
                            currentOffset += rt.getText().length();
                            line.addLineElement(rt);

                            metadataUtility.fixSettingMetadata(parsingOutput, line, rt, processingState);
                            alignUtility.extractPrettyAlignWhitespaces(line, rt, rawText);

                            lastColumnProcessed = textLength;
                            isNewLine = false;
                        }
                    }
                }

                alignUtility.fixOnlyPrettyAlignLinesInSettings(line, processingState);
                alignUtility.fixOnlyPrettyAlignLinesInVariables(line, processingState);

                final List<IRobotLineElement> lineElements = line.getLineElements();
                if (!lineElements.isEmpty()) {
                    final IRobotLineElement lineElem = lineElements.get(lineElements.size() - 1);
                    currentOffset = lineElem.getStartOffset() + lineElem.getText().length();
                }

                final List<Constant> endOfLine = lineHolder.getLineEnd(currentOffset);
                line.setEndOfLine(endOfLine, currentOffset, lastColumnProcessed);
                currentOffset += Constant.getEndOfLineLength(endOfLine);
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
                if (utility.isNotOnlySeparatorOrEmptyLine(line)) {
                    variableHelper.extractVariableAssignmentPart(line, processingState);
                    previousLineHandler.flushNew(processingState);
                }
                fileModel.addNewLine(line);

                parsingStateHelper.updateStatusesForNewLine(processingState);
                isNewLine = true;
            }

            boolean isEOL = false;
            final List<Constant> endOfLine = lineHolder.getLineEnd(currentOffset);
            if (endOfLine.contains(Constant.EOF)) {
                isEOL = true;
                final List<RobotLine> fileContent = fileModel.getFileContent();
                if (fileContent.size() > 1) {
                    final RobotLine robotLine = fileContent.get(fileContent.size() - 1);
                    if (robotLine.getEndOfLine().getFilePosition().isNotSet()) {
                        final List<IRobotLineElement> lastLineElements = robotLine.getLineElements();
                        robotLine.setEndOfLine(endOfLine, currentOffset,
                                lastLineElements.get(lastLineElements.size() - 1).getEndColumn());
                    } else {
                        if (!robotLine.getEndOfLine().getTypes().contains(EndOfLineTypes.EOF)) {
                            final RobotLine newLine = new RobotLine(lineNumber, fileModel);
                            newLine.setEndOfLine(endOfLine, currentOffset, 0);
                            fileModel.addNewLine(newLine);
                        }
                    }
                }
            }
            if (!isEOL) {
                currentOffset = handleCRLFcaseSplittedBetweenBuffers(parsingOutput, lineHolder, lineNumber,
                        currentOffset);
            }
        } catch (final FileNotFoundException e) {
            parsingOutput.addBuildMessage(BuildMessage
                    .createErrorMessage("File " + robotFile + " was not found.\nStack:" + e, "File " + robotFile));
            wasProcessingError = true;
            // FIXME: position should be more descriptive
        } catch (final IOException e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "Problem during file " + robotFile + " reading.\nStack:" + e.getLocalizedMessage(),
                    "File " + robotFile));
            wasProcessingError = true;
        } catch (final Exception e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "Unknown problem during reading file " + robotFile + ".\nStack:" + e, "File " + robotFile));
            // FIXME: stack trace adding
            System.err.println("File: " + robotFile + " line " + lineNumber);
            e.printStackTrace();
            wasProcessingError = true;
        } finally {
            try {
                lineReader.close();
            } catch (final IOException e) {
                parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                        "Error occurred, when file was closing. Stack trace\n\t" + e, robotFile.getAbsolutePath()));
            }
        }

        postFixerActions.applyFixes(parsingOutput);

        if (wasProcessingError) {
            parsingOutput.setStatus(Status.FAILED);
        } else {
            parsingOutput.setStatus(Status.PASSED);
            clearDirtyFlags(parsingOutput);
        }

        return parsingOutput;
    }

    private void clearDirtyFlags(final RobotFileOutput parsingOutput) {
        final List<RobotLine> fileContent = parsingOutput.getFileModel().getFileContent();
        for (final RobotLine line : fileContent) {
            for (final IRobotLineElement rle : line.getLineElements()) {
                if (rle instanceof RobotToken) {
                    ((RobotToken) rle).clearDirtyFlag();
                }
            }
        }
    }

    public boolean isPrettyAlignLineOnly(final String currentLineText) {
        return RobotEmptyRow.isEmpty(currentLineText);
    }

    @VisibleForTesting
    int handleCRLFcaseSplittedBetweenBuffers(final RobotFileOutput parsingOutput, final LineReader lineHolder,
            final int lineNumber, final int currentOffset) {
        int newOffset = currentOffset;
        if (lineNumber > 1) {
            final RobotLine prevLine = parsingOutput.getFileModel().getFileContent().get(lineNumber - 2);
            final IRobotLineElement prevEOL = prevLine.getEndOfLine();
            final List<Constant> lineEnd = lineHolder.getLineEnd(prevEOL.getStartOffset());
            final IRobotLineElement buildEOL = EndOfLineBuilder.newInstance()
                    .setEndOfLines(lineEnd)
                    .setStartColumn(prevEOL.getStartColumn())
                    .setStartOffset(prevEOL.getStartOffset())
                    .setLineNumber(prevEOL.getLineNumber())
                    .buildEOL();
            if (prevEOL.getTypes().get(0) != buildEOL.getTypes().get(0)) {
                prevLine.setEndOfLine(lineEnd, prevEOL.getStartOffset(), prevEOL.getStartColumn());
                newOffset++;
            }
        }

        return newOffset;
    }

    @VisibleForTesting
    protected RobotToken processLineElement(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final FilePosition fp, final String text, final String fileName,
            final boolean isNewLine) {
        final List<RobotToken> robotTokens = recognize(fp, text);
        RobotToken robotToken = utility.computeCorrectRobotToken(currentLine, processingState, robotFileOutput, fp,
                text, isNewLine, robotTokens, fileName);

        final LineContinueType lineContinueType = previousLineHandler.computeLineContinue(processingState, isNewLine,
                robotFileOutput.getFileModel(), currentLine, robotToken);

        boolean processThisElement = true;
        if (lineContinueType == LineContinueType.LINE_CONTINUE_INLINED) {
            processThisElement = false;
        } else if (previousLineHandler.isSomethingToDo(lineContinueType)) {
            previousLineHandler.restorePreviousStack(processingState);

            processThisElement = processingState.size() > 1
                    && !robotToken.getTypes().contains(RobotTokenType.PREVIOUS_LINE_CONTINUE);
        }

        if (processThisElement) {
            ParsingState newStatus = parsingStateHelper.getStatus(robotToken);
            boolean wasRecognizedCorrectly = true;
            if (robotToken != null) {
                if (!text.trim().equals(robotToken.getText().trim())) {
                    wasRecognizedCorrectly = false;
                    // FIXME: incorrect type
                    final RobotToken newRobotToken = new RobotToken();
                    newRobotToken.setLineNumber(fp.getLine());
                    newRobotToken.setStartColumn(fp.getColumn());
                    newRobotToken.setText(text);
                    newRobotToken.setType(RobotTokenType.UNKNOWN);
                    newRobotToken.getTypes().addAll(robotToken.getTypes());
                    robotToken = newRobotToken;
                    newStatus = ParsingState.UNKNOWN;
                }
            } else {
                robotToken = new RobotToken();
                robotToken.setLineNumber(fp.getLine());
                robotToken.setStartColumn(fp.getColumn());
                robotToken.setText(text);
                robotToken.setType(RobotTokenType.UNKNOWN);

                newStatus = ParsingState.UNKNOWN;
            }

            boolean useMapper = true;
            final RobotFile fileModel = robotFileOutput.getFileModel();
            if (utility.isTableHeader(robotToken)) {
                if (positionResolvers.isCorrectPosition(PositionExpected.TABLE_HEADER, fileModel, currentLine,
                        robotToken) && isCorrectTableHeader(robotToken)) {
                    if (wasRecognizedCorrectly) {
                        robotToken.getTypes().remove(RobotTokenType.UNKNOWN);
                        @SuppressWarnings("rawtypes")
                        final TableHeader<?> header = new TableHeader(robotToken);
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
                        // FIXME: add warning about incorrect table
                    }
                } else {
                    // FIXME: add warning about wrong place
                }
            }

            if (useMapper && utility.isUserTableHeader(robotToken)) {
                if (positionResolvers.isCorrectPosition(PositionExpected.TABLE_HEADER, fileModel, currentLine,
                        robotToken)) {
                    // FIXME: add warning about user trash table
                    robotToken.getTypes().add(0, RobotTokenType.USER_OWN_TABLE_HEADER);
                    robotToken.getTypes().remove(RobotTokenType.UNKNOWN);
                    processingState.clear();
                    processingState.push(ParsingState.TRASH);

                    useMapper = false;
                }
            }

            robotToken = alignUtility.applyPrettyAlignTokenIfIsValid(currentLine, processingState, robotFileOutput, fp,
                    text, fileName, robotToken);

            useMapper = useMapper && !robotToken.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE);

            if (useMapper) {
                robotToken = mapToCorrectTokenAndPutInCorrectPlaceInModel(currentLine, processingState, robotFileOutput,
                        fp, text, robotToken);
            }
        }

        utility.fixNotSetPositions(robotToken, fp);

        return robotToken;
    }

    @VisibleForTesting
    protected RobotToken processEmptyLine(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final FilePosition fp, final String text, final String fileName,
            final boolean isNewLine) {

        RobotToken robotToken = new RobotToken();
        robotToken.setFilePosition(fp);
        robotToken.setText(text);
        robotToken.setType(RobotTokenType.PRETTY_ALIGN_SPACE);

        robotToken = mapToCorrectTokenAndPutInCorrectPlaceInModel(currentLine, processingState, robotFileOutput, fp,
                text, robotToken);

        return robotToken;

    }

    private boolean isCorrectTableHeader(final RobotToken robotToken) {
        boolean result = false;

        final List<RobotTokenType> tableHeadersTypes = newArrayList(RobotTokenType.SETTINGS_TABLE_HEADER,
                RobotTokenType.VARIABLES_TABLE_HEADER, RobotTokenType.TEST_CASES_TABLE_HEADER,
                RobotTokenType.KEYWORDS_TABLE_HEADER);

        final String raw = robotToken.getText().replaceAll("\\s+|[*]", "");
        final List<IRobotTokenType> types = robotToken.getTypes();
        for (final IRobotTokenType type : types) {
            if (tableHeadersTypes.contains(type)) {
                final List<String> representations = type.getRepresentation();
                for (final String r : representations) {
                    if (r.replaceAll("\\s+", "").equalsIgnoreCase(raw)) {
                        result = true;
                        break;
                    }
                }
                break;
            }
        }

        return result;

    }

    private RobotToken mapToCorrectTokenAndPutInCorrectPlaceInModel(final RobotLine currentLine,
            final Stack<ParsingState> processingState, final RobotFileOutput robotFileOutput, final FilePosition fp,
            final String text, final RobotToken robotToken) {
        final List<IParsingMapper> matchedMappers = new ArrayList<>();
        for (final IParsingMapper mapper : mappers) {
            if (mapper.checkIfCanBeMapped(robotFileOutput, currentLine, robotToken, text, processingState)) {
                matchedMappers.add(mapper);
            }
        }

        // check for unknown setting
        int size = matchedMappers.size();
        if (size == 0) {
            for (final IParsingMapper mapper : unknownTableElementsMapper) {
                if (mapper.checkIfCanBeMapped(robotFileOutput, currentLine, robotToken, text, processingState)) {
                    matchedMappers.add(mapper);
                }
            }
        }

        size = matchedMappers.size();

        if (size == 1) {
            return matchedMappers.get(0).map(currentLine, processingState, robotFileOutput, robotToken, fp, text);
        }

        return robotToken;
    }

    @VisibleForTesting
    protected List<RobotToken> recognize(final FilePosition fp, final String text) {
        final List<RobotToken> possibleRobotTokens = new ArrayList<>();
        final StringBuilder sb = new StringBuilder(text);
        for (final ATokenRecognizer rec : recognized) {
            if (rec.hasNext(sb, fp.getLine(), fp.getColumn())) {
                final RobotToken t = rec.next();
                t.setStartColumn(t.getStartColumn() + fp.getColumn());
                possibleRobotTokens.add(t);
            }
        }

        if (possibleRobotTokens.isEmpty()) {
            final RobotToken rt = new RobotToken();
            rt.setLineNumber(fp.getLine());
            rt.setText(text);
            rt.setStartColumn(fp.getColumn());

            possibleRobotTokens.add(rt);
        }

        return possibleRobotTokens;
    }

}
