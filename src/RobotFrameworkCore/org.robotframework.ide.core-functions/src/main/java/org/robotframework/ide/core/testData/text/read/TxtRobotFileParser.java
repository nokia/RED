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
import org.robotframework.ide.core.testData.model.HashCommentMapper;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotFileOutput.BuildMessage;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.GarbageBeforeFirstTableMapper;
import org.robotframework.ide.core.testData.model.table.IParsingMapper;
import org.robotframework.ide.core.testData.model.table.TableColumnMapper;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.text.read.columnSeparators.ALineSeparator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.columnSeparators.TokenSeparatorBuilder;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.header.HashCommentRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.header.KeywordsTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.header.SettingsTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.header.TestCasesTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.header.VariablesTableHeaderRecognizer;

import com.google.common.annotations.VisibleForTesting;


public class TxtRobotFileParser {

    private final TokenSeparatorBuilder tokenSeparatorBuilder;
    private final List<ATokenRecognizer> recognized = new LinkedList<>();
    private final List<IParsingMapper> mappers = new LinkedList<>();


    public TxtRobotFileParser() {
        this.tokenSeparatorBuilder = new TokenSeparatorBuilder();
        recognized.add(new SettingsTableHeaderRecognizer());
        recognized.add(new VariablesTableHeaderRecognizer());
        recognized.add(new TestCasesTableHeaderRecognizer());
        recognized.add(new KeywordsTableHeaderRecognizer());
        recognized.add(new HashCommentRecognizer());

        mappers.add(new GarbageBeforeFirstTableMapper());
        mappers.add(new TableColumnMapper());
        mappers.add(new HashCommentMapper());
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
        RobotFileOutput parsingOutput = new RobotFileOutput();
        RobotFile rf = new RobotFile();
        parsingOutput.setFileModel(rf);

        BufferedReader lineReader = new BufferedReader(reader);
        int lineNumber = 0;
        String currentLineText = null;
        final Stack<ParsingState> processingState = new Stack<>();

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
                                            startColumn));
                            line.addLineElement(rt);
                        }

                        line.addLineElement(currentSeparator);
                        lastColumnProcessed = currentSeparator.getEndColumn();
                    } else {
                        // last element in line
                        rt = processLineElement(line, processingState,
                                parsingOutput, new FilePosition(lineNumber,
                                        lastColumnProcessed),
                                text.substring(lastColumnProcessed));
                        line.addLineElement(rt);

                        lastColumnProcessed = textLength;
                    }
                }
            }

            lineNumber++;
            lastColumnProcessed = 0;
            rf.addNewLine(line);
            updateStatus(processingState, null);
        }

        for (RobotLine line : rf.getFileContent()) {
            System.out.println(line);
        }
        return parsingOutput;
    }


    @VisibleForTesting
    protected RobotToken processLineElement(RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final FilePosition fp,
            String text) {
        RobotToken robotToken = recognize(fp, text);
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
            if (isTableHeaderInCorrectPlace(currentLine, robotToken)) {
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
                if (mapper.checkIfCanBeMapped(robotFileOutput, robotToken,
                        processingState)) {
                    matchedMappers.add(mapper);
                }
            }

            int size = matchedMappers.size();
            if (size == 1) {

                robotToken = matchedMappers.get(0).map(currentLine,
                        processingState, robotFileOutput, robotToken, fp, text);
            } else {
                // TODO: implement - error
                System.out.println("ERR " + text + " matchers: " + size);
            }
        }

        return robotToken;
    }


    @VisibleForTesting
    protected boolean isTableHeaderInCorrectPlace(RobotLine currentLine,
            RobotToken robotToken) {
        boolean result = false;
        if (robotToken.getStartColumn() == 0) {
            result = true;
        } else {
            List<IRobotLineElement> lineElements = currentLine
                    .getLineElements();
            int size = lineElements.size();
            if (size > 0) {
                IRobotLineElement lastElement = lineElements.get(size - 1);
                result = (lastElement.getType() == SeparatorType.PIPE && lastElement
                        .getStartColumn() == 0);
            } else {
                result = true;
            }
        }
        return result;
    }


    @VisibleForTesting
    protected void updateStatus(final Stack<ParsingState> processingState,
            final RobotToken currentToken) {
        ParsingState status = getCurrentParsingState(processingState);

        if (currentToken == null) {
            // line end
        } else {

        }
    }


    @VisibleForTesting
    protected boolean isTableHeader(RobotToken t) {
        boolean result = false;
        IRobotTokenType type = t.getType();
        if (type == RobotTokenType.SETTINGS_TABLE_HEADER) {
            result = true;
        } else if (type == RobotTokenType.VARIABLES_TABLE_HEADER) {
            result = true;
        } else if (type == RobotTokenType.TEST_CASES_TABLE_HEADER) {
            result = true;
        } else if (type == RobotTokenType.KEYWORDS_TABLE_HEADER) {
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


    private ParsingState getCurrentParsingState(
            final Stack<ParsingState> processingState) {
        ParsingState status;
        if (processingState.isEmpty()) {
            status = ParsingState.TRASH;
        } else {
            status = processingState.peek();
        }

        return status;
    }


    private ParsingState getStatus(RobotToken t) {
        ParsingState status = ParsingState.UNKNOWN;
        IRobotTokenType type = t.getType();
        if (type == RobotTokenType.SETTINGS_TABLE_HEADER) {
            status = ParsingState.SETTING_TABLE_HEADER;
        } else if (type == RobotTokenType.VARIABLES_TABLE_HEADER) {
            status = ParsingState.VARIABLE_TABLE_HEADER;
        } else if (type == RobotTokenType.TEST_CASES_TABLE_HEADER) {
            status = ParsingState.TEST_CASE_TABLE_HEADER;
        } else if (type == RobotTokenType.KEYWORDS_TABLE_HEADER) {
            status = ParsingState.KEYWORD_TABLE_HEADER;
        }

        return status;
    }

    public static enum ParsingState {
        /**
         * 
         */
        UNKNOWN,
        /**
         * 
         */
        TRASH,
        /**
         * 
         */
        TABLE_HEADER_COLUMN,
        /**
         * 
         */
        COMMENT,
        /**
         * 
         */
        SETTING_TABLE_HEADER,
        /**
         * 
         */
        SETTING_TABLE_INSIDE,
        /**
         * 
         */
        VARIABLE_TABLE_HEADER,
        /**
         * 
         */
        VARIABLE_TABLE_INSIDE,
        /**
         * 
         */
        TEST_CASE_TABLE_HEADER,
        /**
         * 
         */
        TEST_CASE_TABLE_INSIDE,
        /**
         * 
         */
        KEYWORD_TABLE_HEADER,
        /**
         * 
         */
        KEYWORD_TABLE_INSIDE;
    }
}
