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
import org.robotframework.ide.core.testData.model.mapping.HashCommentMapper;
import org.robotframework.ide.core.testData.model.mapping.PreviousLineHandler;
import org.robotframework.ide.core.testData.model.mapping.PreviousLineHandler.LineContinueType;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.model.table.mapping.GarbageBeforeFirstTableMapper;
import org.robotframework.ide.core.testData.model.table.mapping.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.mapping.TableHeaderColumnMapper;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.LibraryAlias;
import org.robotframework.ide.core.testData.model.table.setting.LibraryImport;
import org.robotframework.ide.core.testData.model.table.setting.mapping.DefaultTagsMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.DefaultTagsTagNameMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.ForceTagsMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.ForceTagsTagNameMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.MetadataKeyMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.MetadataMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.MetadataValueMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.SettingDocumentationMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.SettingDocumentationTextMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.SuiteSetupKeywordArgumentMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.SuiteSetupKeywordMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.SuiteSetupMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.SuiteTeardownKeywordArgumentMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.SuiteTeardownKeywordMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.SuiteTeardownMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.library.LibraryAliasDeclarationMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.library.LibraryAliasFixer;
import org.robotframework.ide.core.testData.model.table.setting.mapping.library.LibraryAliasMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.library.LibraryArgumentsMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.library.LibraryDeclarationMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.library.LibraryNameOrPathMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.resource.ResourceDeclarationMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.resource.ResourceImportPathMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.resource.ResourceTrashDataMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.variables.VariablesArgumentsMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.variables.VariablesDeclarationMapper;
import org.robotframework.ide.core.testData.model.table.setting.mapping.variables.VariablesImportPathMapper;
import org.robotframework.ide.core.testData.text.read.columnSeparators.ALineSeparator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.TokenSeparatorBuilder;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.HashCommentRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.PreviousLineContinueRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.header.KeywordsTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.header.SettingsTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.header.TestCasesTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.header.VariablesTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.settings.DefaultTagsRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.settings.ForceTagsRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.settings.LibraryAliasRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.settings.LibraryDeclarationRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.settings.MetadataRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.settings.ResourceDeclarationRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.settings.SettingDocumentationRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.settings.SuiteSetupRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.settings.SuiteTeardownRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.settings.VariableDeclarationRecognizer;

import com.google.common.annotations.VisibleForTesting;


public class TxtRobotFileParser {

    private final TokenSeparatorBuilder tokenSeparatorBuilder;
    private final List<ATokenRecognizer> recognized = new LinkedList<>();
    private final List<IParsingMapper> mappers = new LinkedList<>();
    private final ElementsUtility utility;
    private final LibraryAliasFixer libraryFixer;
    private final PreviousLineHandler previousLineHandler;


    public TxtRobotFileParser() {
        this.utility = new ElementsUtility();
        this.tokenSeparatorBuilder = new TokenSeparatorBuilder();
        this.libraryFixer = new LibraryAliasFixer();
        this.previousLineHandler = new PreviousLineHandler();
        recognized.add(new SettingsTableHeaderRecognizer());
        recognized.add(new VariablesTableHeaderRecognizer());
        recognized.add(new TestCasesTableHeaderRecognizer());
        recognized.add(new KeywordsTableHeaderRecognizer());
        recognized.add(new HashCommentRecognizer());
        recognized.add(new PreviousLineContinueRecognizer());

        recognized.add(new LibraryDeclarationRecognizer());
        recognized.add(new LibraryAliasRecognizer());
        recognized.add(new VariableDeclarationRecognizer());
        recognized.add(new ResourceDeclarationRecognizer());

        recognized.add(new SettingDocumentationRecognizer());
        recognized.add(new MetadataRecognizer());
        recognized.add(new SuiteSetupRecognizer());
        recognized.add(new SuiteTeardownRecognizer());
        recognized.add(new ForceTagsRecognizer());
        recognized.add(new DefaultTagsRecognizer());

        mappers.add(new GarbageBeforeFirstTableMapper());
        mappers.add(new TableHeaderColumnMapper());
        mappers.add(new HashCommentMapper());

        mappers.add(new LibraryDeclarationMapper());
        mappers.add(new LibraryNameOrPathMapper());
        mappers.add(new LibraryArgumentsMapper());
        mappers.add(new LibraryAliasDeclarationMapper());
        mappers.add(new LibraryAliasMapper());

        mappers.add(new VariablesDeclarationMapper());
        mappers.add(new VariablesImportPathMapper());
        mappers.add(new VariablesArgumentsMapper());

        mappers.add(new ResourceDeclarationMapper());
        mappers.add(new ResourceImportPathMapper());
        mappers.add(new ResourceTrashDataMapper());

        mappers.add(new SettingDocumentationMapper());
        mappers.add(new SettingDocumentationTextMapper());
        mappers.add(new MetadataMapper());
        mappers.add(new MetadataKeyMapper());
        mappers.add(new MetadataValueMapper());

        mappers.add(new SuiteSetupMapper());
        mappers.add(new SuiteSetupKeywordMapper());
        mappers.add(new SuiteSetupKeywordArgumentMapper());
        mappers.add(new SuiteTeardownMapper());
        mappers.add(new SuiteTeardownKeywordMapper());
        mappers.add(new SuiteTeardownKeywordArgumentMapper());
        mappers.add(new ForceTagsMapper());
        mappers.add(new ForceTagsTagNameMapper());
        mappers.add(new DefaultTagsMapper());
        mappers.add(new DefaultTagsTagNameMapper());
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
            updateStatusesForNewLine(processingState);
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
        ParsingState state = findNearestNotCommentState(processingState);
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
    protected ParsingState findNearestNotCommentState(
            final Stack<ParsingState> processingState) {
        ParsingState state = ParsingState.UNKNOWN;
        for (ParsingState s : processingState) {
            if (s != ParsingState.COMMENT) {
                state = s;
            }
        }
        return state;
    }


    @VisibleForTesting
    protected RobotToken processLineElement(RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final FilePosition fp,
            String text, boolean isNewLine) {
        RobotToken robotToken = recognize(fp, text);
        LineContinueType lineContinueType = previousLineHandler
                .computeLineContinue(processingState, isNewLine, currentLine,
                        robotToken);
        if (previousLineHandler.isSomethingToDo(lineContinueType)) {
            previousLineHandler.restorePreviousStack(lineContinueType,
                    processingState, currentLine, robotToken);
        } else {
            ParsingState newStatus = getStatus(robotToken);
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
            if (isTableHeader(robotToken)) {
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

                int size = matchedMappers.size();
                if (size == 1) {

                    robotToken = matchedMappers.get(0).map(currentLine,
                            processingState, robotFileOutput, robotToken, fp,
                            text);

                } else {
                    // TODO: implement - error
                    System.out.println("ERR [" + processingState + "");
                    System.out.println("ERR [" + text + "]");
                }
            }
        }

        return robotToken;
    }


    @VisibleForTesting
    protected void updateStatusesForNewLine(
            final Stack<ParsingState> processingState) {

        boolean clean = true;
        while(clean) {
            ParsingState status = utility.getCurrentStatus(processingState);
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
            } else if (utility.isTableInsideState(status)) {
                clean = false;
            } else if (!processingState.isEmpty()) {
                processingState.pop();
            } else {
                clean = false;
            }
        }
    }


    @VisibleForTesting
    protected boolean isTableHeader(ParsingState state) {
        boolean result = false;
        if (state == ParsingState.SETTING_TABLE_HEADER
                || state == ParsingState.VARIABLE_TABLE_HEADER
                || state == ParsingState.TEST_CASE_TABLE_HEADER
                || state == ParsingState.KEYWORD_TABLE_HEADER) {
            result = true;
        }

        return result;
    }


    @VisibleForTesting
    protected boolean isTableHeader(RobotToken t) {
        boolean result = false;
        List<IRobotTokenType> declaredTypes = t.getTypes();
        if (declaredTypes.contains(RobotTokenType.SETTINGS_TABLE_HEADER)) {
            result = true;
        } else if (declaredTypes
                .contains(RobotTokenType.VARIABLES_TABLE_HEADER)) {
            result = true;
        } else if (declaredTypes
                .contains(RobotTokenType.TEST_CASES_TABLE_HEADER)) {
            result = true;
        } else if (declaredTypes.contains(RobotTokenType.KEYWORDS_TABLE_HEADER)) {
            result = true;
        }

        return result;
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


    private ParsingState getStatus(RobotToken t) {
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
}
