package org.robotframework.ide.core.testData.text.read;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.LibraryAlias;
import org.robotframework.ide.core.testData.model.table.setting.LibraryImport;
import org.robotframework.ide.core.testData.model.table.setting.mapping.UnknownSettingArgumentMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.UnknownSettingMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.library.LibraryAliasFixer;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseExecutableRowActionMapper;
import org.robotframework.ide.core.testData.model.table.testCases.mapping.TestCaseExecutableRowArgumentMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordExecutableRowActionMapper;
import org.robotframework.ide.core.testData.model.table.userKeywords.mapping.KeywordExecutableRowArgumentMapper;
import org.robotframework.ide.core.testData.model.table.variables.mapping.UnknownVariableMapper;
import org.robotframework.ide.core.testData.model.table.variables.mapping.UnknownVariableValueMapper;
import org.robotframework.ide.core.testData.text.read.columnSeparators.ALineSeparator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.columnSeparators.TokenSeparatorBuilder;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.SettingsRecognizersProvider;
import org.robotframework.ide.core.testData.text.read.recognizer.TestCaseRecognizersProvider;
import org.robotframework.ide.core.testData.text.read.recognizer.UserKeywordRecognizersProvider;
import org.robotframework.ide.core.testData.text.read.recognizer.VariablesDeclarationRecognizersProvider;
import org.robotframework.ide.core.testData.text.read.recognizer.settings.LibraryAliasRecognizer;

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
        this.libraryFixer = new LibraryAliasFixer();
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
    public RobotFileOutput parse(final File robotFile) {
        RobotFileOutput parsingOutput = new RobotFileOutput();
        boolean wasProcessingError = false;
        try {
            parsingOutput = parse(robotFile, new InputStreamReader(
                    new FileInputStream(robotFile), Charset.forName("UTF-8")));
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

        return parsingOutput;
    }


    private RobotFileOutput parse(final File robotFile, final Reader reader) {
        boolean wasProcessingError = false;
        previousLineHandler.clear();

        RobotFileOutput parsingOutput = new RobotFileOutput();
        RobotFile rf = new RobotFile();
        parsingOutput.setFileModel(rf);

        BufferedReader lineReader = new BufferedReader(reader);
        int lineNumber = 1;
        int currentOffset = 0;
        String currentLineText = null;
        final Stack<ParsingState> processingState = new Stack<>();
        boolean isNewLine = false;
        try {
            while((currentLineText = lineReader.readLine()) != null) {
                RobotLine line = new RobotLine(lineNumber);
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
                            if (remainingData > 0) {
                                rt = processLineElement(line, processingState,
                                        parsingOutput,
                                        new FilePosition(lineNumber,
                                                lastColumnProcessed),
                                        text.substring(lastColumnProcessed,
                                                startColumn), isNewLine);
                                rt.setStartOffset(currentOffset);
                                currentOffset += rt.getRaw().length();
                                line.addLineElement(rt);
                                isNewLine = false;
                            }

                            currentSeparator.setStartOffset(currentOffset);
                            currentOffset += currentSeparator.getRaw().length();
                            line.addLineElement(currentSeparator);
                            lastColumnProcessed = currentSeparator
                                    .getEndColumn();
                        } else {
                            // last element in line
                            if (isNewExecutableSection(separator, line)) {
                                processingState
                                        .remove(ParsingState.TEST_CASE_DECLARATION);
                                processingState
                                        .remove(ParsingState.KEYWORD_DECLARATION);
                            }

                            rt = processLineElement(line, processingState,
                                    parsingOutput, new FilePosition(lineNumber,
                                            lastColumnProcessed),
                                    text.substring(lastColumnProcessed),
                                    isNewLine);
                            rt.setStartOffset(currentOffset);
                            currentOffset += rt.getRaw().length();
                            line.addLineElement(rt);

                            lastColumnProcessed = textLength;
                            isNewLine = false;
                        }
                    }
                }

                currentOffset++;
                lineNumber++;
                lastColumnProcessed = 0;
                checkAndFixLine(parsingOutput, processingState);
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
                if (!line.getLineElements().isEmpty()) {
                    previousLineHandler.flushNew(processingState);
                }
                rf.addNewLine(line);
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
        }

        if (wasProcessingError) {
            parsingOutput.setStatus(Status.FAILED);
        } else {
            parsingOutput.setStatus(Status.PASSED);
        }

        return parsingOutput;
    }


    @VisibleForTesting
    protected boolean isNewExecutableSection(final ALineSeparator separator,
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


    @VisibleForTesting
    protected void checkAndFixLine(final RobotFileOutput robotFileOutput,
            final Stack<ParsingState> processingState) {
        ParsingState state = utility
                .findNearestNotCommentState(processingState);
        if (state == ParsingState.SETTING_LIBRARY_IMPORT_ALIAS) {
            LibraryImport lib = findNearestLibraryImport(robotFileOutput);

            libraryFixer.applyFixes(lib, null, processingState);
        } else if (state == ParsingState.SETTING_LIBRARY_ARGUMENTS) {
            LibraryImport lib = findNearestLibraryImport(robotFileOutput);

            List<RobotToken> arguments = lib.getArguments();
            int argumentsSize = arguments.size();
            if (argumentsSize >= 2) {
                RobotToken argumentPossibleAlias = arguments
                        .get(argumentsSize - 2);
                ATokenRecognizer rec = new LibraryAliasRecognizer();
                if (rec.hasNext(argumentPossibleAlias.getText(),
                        argumentPossibleAlias.getLineNumber())) {
                    argumentPossibleAlias
                            .setType(RobotTokenType.SETTING_LIBRARY_ALIAS);
                    LibraryAlias alias = new LibraryAlias(argumentPossibleAlias);
                    RobotToken aliasValue = arguments.get(argumentsSize - 1);
                    aliasValue
                            .setType(RobotTokenType.SETTING_LIBRARY_ALIAS_VALUE);
                    alias.setLibraryAlias(aliasValue);

                    lib.setAlias(alias);
                    arguments.remove(argumentsSize - 1);
                    arguments.remove(argumentsSize - 2);
                    replaceArgumentsByAliasDeclaration(processingState);
                }
            }
        }
    }


    private void replaceArgumentsByAliasDeclaration(
            final Stack<ParsingState> processingState) {
        int removedArguments = 0;
        for (int i = processingState.size() - 1; i >= 0; i--) {
            ParsingState state = processingState.get(i);
            if (state == ParsingState.SETTING_LIBRARY_ARGUMENTS) {
                if (removedArguments == 0) {
                    // it is value
                    processingState.set(i,
                            ParsingState.SETTING_LIBRARY_IMPORT_ALIAS_VALUE);
                    removedArguments++;
                } else if (removedArguments == 1) {
                    // it is alias
                    processingState.set(i,
                            ParsingState.SETTING_LIBRARY_IMPORT_ALIAS);
                    break;
                }
            }
        }
    }


    private LibraryImport findNearestLibraryImport(
            final RobotFileOutput robotFileOutput) {
        AImported imported = utility.getNearestImport(robotFileOutput);
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


    @VisibleForTesting
    protected RobotToken processLineElement(RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final FilePosition fp,
            String text, boolean isNewLine) {
        List<RobotToken> robotTokens = recognize(fp, text);
        RobotToken robotToken = computeCorrectRobotToken(currentLine,
                processingState, robotFileOutput, fp, text, isNewLine,
                robotTokens);

        LineContinueType lineContinueType = previousLineHandler
                .computeLineContinue(processingState, isNewLine,
                        robotFileOutput.getFileModel(), currentLine, robotToken);

        if (previousLineHandler.isSomethingToDo(lineContinueType)) {
            previousLineHandler.restorePreviousStack(lineContinueType,
                    processingState, currentLine, robotToken);
        } else {
            ParsingState newStatus = utility.getStatus(robotToken);
            if (robotToken != null) {
                if (!text.equals(robotToken.getText().toString())) {
                    // FIXME: add information that type is incorrect missing
                    // separator
                    robotToken.getTypes().add(0, RobotTokenType.UNKNOWN);
                    robotToken.setText(new StringBuilder(text));
                    newStatus = ParsingState.UNKNOWN;
                }
            } else {
                robotToken = new RobotToken();
                robotToken.setLineNumber(fp.getLine());
                robotToken.setStartColumn(fp.getColumn());
                robotToken.setText(new StringBuilder(text));
                robotToken.setType(RobotTokenType.UNKNOWN);

                newStatus = ParsingState.UNKNOWN;
            }

            boolean useMapper = true;
            RobotFile fileModel = robotFileOutput.getFileModel();
            if (utility.isTableHeader(robotToken)) {
                if (utility.isTheFirstColumn(currentLine, robotToken)) {
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
                    // TODO: implement - error to many
                    System.out.println("ERR [" + processingState + "");
                    System.out.println("ERR [" + text + "]");
                }
            }
        }

        return robotToken;
    }


    @VisibleForTesting
    protected RobotToken computeCorrectRobotToken(RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final FilePosition fp,
            String text, boolean isNewLine, List<RobotToken> robotTokens) {
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
                ParsingState state = utility.getCurrentStatus(processingState);
                for (RobotToken rt : robotTokens) {
                    if (isTypeForState(state, rt)) {
                        correct = rt;
                        break;
                    }
                }

                if (correct == null) {
                    // FIXME: error no matching tokens to state
                    throw new IllegalStateException("Some problem to fix.");
                }
            }
        } else {
            correct = robotTokens.get(0);
        }

        return correct;
    }


    @VisibleForTesting
    protected List<RobotToken> findHeadersPossible(final List<RobotToken> tokens) {
        List<RobotToken> found = new LinkedList<>();
        for (RobotToken t : tokens) {
            if (utility.isTableHeader(t)) {
                found.add(t);
            }
        }

        return found;
    }


    @VisibleForTesting
    protected boolean isTypeForState(final ParsingState state,
            final RobotToken rt) {
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

        if (!result
                && (state == ParsingState.TEST_CASE_DECLARATION || state == ParsingState.KEYWORD_DECLARATION)) {
            result = (types.contains(RobotTokenType.START_HASH_COMMENT) || types
                    .contains(RobotTokenType.COMMENT_CONTINUE));
        }

        return result;
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
