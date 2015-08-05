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

import org.robotframework.ide.core.testData.model.FilePosition;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotFileOutput.BuildMessage;
import org.robotframework.ide.core.testData.model.mapping.PreviousLineHandler;
import org.robotframework.ide.core.testData.model.mapping.PreviousLineHandler.LineContinueType;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.SettingsMapperProvider;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.LibraryAlias;
import org.robotframework.ide.core.testData.model.table.setting.LibraryImport;
import org.robotframework.ide.core.testData.model.table.setting.mapping.UnknownSettingArgumentMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.UnknownSettingMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.library.LibraryAliasFixer;
import org.robotframework.ide.core.testData.text.read.columnSeparators.ALineSeparator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.TokenSeparatorBuilder;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.SettingsRecognizersProvider;
import org.robotframework.ide.core.testData.text.read.recognizer.settings.LibraryAliasRecognizer;

import com.google.common.annotations.VisibleForTesting;


public class TxtRobotFileParser {

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
        mappers.addAll(new SettingsMapperProvider().getMappers());
        unknownTableElementsMapper.add(new UnknownSettingMapper());
        unknownTableElementsMapper.add(new UnknownSettingArgumentMapper());
    }


    public RobotFileOutput parse(final File robotFile) {
        RobotFileOutput parsingOutput = new RobotFileOutput();
        try {
            parsingOutput = parse(new InputStreamReader(new FileInputStream(
                    robotFile), Charset.forName("UTF-8")));
        } catch (FileNotFoundException e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "File " + robotFile + " was not found.\nStack:" + e,
                    "File " + robotFile));
            // FIXME: position should be more descriptive
        } catch (IOException e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "Problem during file " + robotFile + " reading.\nStack:"
                            + e.getLocalizedMessage(), "File " + robotFile));
        } catch (Exception e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "Unknown problem during reading file " + robotFile
                            + ".\nStack:" + e, "File " + robotFile));
            // FIXME: stack trace adding
            e.printStackTrace();
        }

        parsingOutput.setProcessedFile(robotFile);

        return parsingOutput;
    }


    private RobotFileOutput parse(final Reader reader) throws IOException {
        previousLineHandler.clear();

        RobotFileOutput parsingOutput = new RobotFileOutput();
        RobotFile rf = new RobotFile();
        parsingOutput.setFileModel(rf);

        BufferedReader lineReader = new BufferedReader(reader);
        int lineNumber = 0;
        String currentLineText = null;
        final Stack<ParsingState> processingState = new Stack<>();
        boolean isNewLine = false;
        while((currentLineText = lineReader.readLine()) != null) {
            RobotLine line = new RobotLine(lineNumber);
            StringBuilder text = new StringBuilder(currentLineText);
            int lastColumnProcessed = 0;
            // get separator for this line
            ALineSeparator separator = tokenSeparatorBuilder.createSeparator(
                    lineNumber, currentLineText);
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
                        int remainingData = startColumn - lastColumnProcessed;
                        // {$a} | {$b} in this case we check if {$a} was before
                        // '|' pipe separator
                        if (remainingData > 0) {
                            rt = processLineElement(line, processingState,
                                    parsingOutput, new FilePosition(lineNumber,
                                            lastColumnProcessed),
                                    text.substring(lastColumnProcessed,
                                            startColumn), isNewLine);
                            line.addLineElement(rt);
                            isNewLine = false;
                        }

                        line.addLineElement(currentSeparator);
                        lastColumnProcessed = currentSeparator.getEndColumn();
                    } else {
                        // last element in line
                        rt = processLineElement(line, processingState,
                                parsingOutput, new FilePosition(lineNumber,
                                        lastColumnProcessed),
                                text.substring(lastColumnProcessed), isNewLine);
                        line.addLineElement(rt);

                        lastColumnProcessed = textLength;
                        isNewLine = false;
                    }
                }
            }

            lineNumber++;
            lastColumnProcessed = 0;
            checkAndFixLine(parsingOutput, processingState);
            previousLineHandler.flushNew(processingState);
            rf.addNewLine(line);
            utility.updateStatusesForNewLine(processingState);
            isNewLine = true;
        }

        for (RobotLine line : rf.getFileContent()) {
            System.out.println(line);
        }

        return parsingOutput;
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
        RobotToken robotToken = recognize(fp, text);
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
                    robotToken.setType(RobotTokenType.UNKNOWN);
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
    protected RobotToken recognize(final FilePosition fp, String text) {
        RobotToken robotToken = null;
        StringBuilder sb = new StringBuilder(text);
        for (ATokenRecognizer rec : recognized) {
            if (rec.hasNext(sb, fp.getLine())) {
                robotToken = rec.next();
                break;
            }
        }

        if (robotToken == null) {
            robotToken = new RobotToken();
            robotToken.setLineNumber(fp.getLine());
            robotToken.setText(new StringBuilder(text));
        }

        robotToken.setStartColumn(fp.getColumn());

        return robotToken;
    }
}
