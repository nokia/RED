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

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotFileOutput.BuildMessage;
import org.robotframework.ide.core.testData.text.read.columnSeparators.ALineSeparator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.TokenSeparatorBuilder;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.header.KeywordsTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.header.SettingsTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.header.TestCasesTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.header.VariablesTableHeaderRecognizer;

import com.google.common.annotations.VisibleForTesting;


public class TxtRobotFileParser {

    private final TokenSeparatorBuilder tokenSeparatorBuilder;
    private final List<ATokenRecognizer> recognized = new LinkedList<>();


    public TxtRobotFileParser() {
        this.tokenSeparatorBuilder = new TokenSeparatorBuilder();
        recognized.add(new SettingsTableHeaderRecognizer());
        recognized.add(new VariablesTableHeaderRecognizer());
        recognized.add(new TestCasesTableHeaderRecognizer());
        recognized.add(new KeywordsTableHeaderRecognizer());
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
        } catch (IOException e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "Problem during file " + robotFile + " reading.\nStack:"
                            + e, "File " + robotFile));
        } catch (Exception e) {
            parsingOutput.addBuildMessage(BuildMessage.createErrorMessage(
                    "Unknown problem during reading file " + robotFile
                            + ".\nStack:" + e, "File " + robotFile));
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
                            rt = processLineElement(processingState, rf,
                                    lineNumber, startColumn, text.substring(
                                            lastColumnProcessed, startColumn));
                            line.addLineElement(rt);
                        }

                        line.addLineElement(currentSeparator);
                        lastColumnProcessed = currentSeparator.getEndColumn();
                    } else {
                        // last element in line
                        rt = processLineElement(processingState, rf,
                                lineNumber, lastColumnProcessed,
                                text.substring(lastColumnProcessed));
                        line.addLineElement(rt);
                        lastColumnProcessed = textLength;
                    }
                }
            }

            lineNumber++;
            rf.addNewLine(line);
            updateStatus(processingState, null);
        }

        return parsingOutput;
    }


    private void updateStatus(final Stack<ParsingState> processingState,
            final RobotToken currentToken) {
        ParsingState status = getCurrentParsingState(processingState);

        if (currentToken == null) {
            // line end
        } else {

        }
    }


    private RobotToken processLineElement(
            final Stack<ParsingState> processingState,
            final RobotFile robotFile, int line, int startColumn, String text) {
        RobotToken rt = new RobotToken();
        rt.setLineNumber(line);
        rt.setStartColumn(startColumn);
        rt.setText(new StringBuilder(text));

        ParsingState newStatus = ParsingState.UNKNOWN;
        RobotToken robotToken = recognize(line, text);

        newStatus = getStatus(robotToken);

        if (robotToken != null) {
            if (text.equals(robotToken.getText())) {
                System.out.println("D");
            } else {
                System.out.println("P");
            }
        } else {
            System.out.println("DUPA");
        }

        return rt;
    }


    @VisibleForTesting
    protected RobotToken recognize(int line, String text) {
        RobotToken robotToken = null;
        StringBuilder sb = new StringBuilder(text);
        for (ATokenRecognizer rec : recognized) {
            if (rec.hasNext(sb, line)) {
                robotToken = rec.next();
                break;
            }
        }
        return robotToken;
    }


    private ParsingState getCurrentParsingState(
            final Stack<ParsingState> processingState) {
        ParsingState status;
        if (!processingState.isEmpty()) {
            status = processingState.peek();
        } else {
            status = ParsingState.TRASH;
        }

        return status;
    }


    private ParsingState getStatus(RobotToken t) {
        ParsingState status = ParsingState.UNKNOWN;
        IRobotTokenType type = t.getType();
        if (type == RobotTokenType.SETTINGS_TABLE_HEADER) {
            status = ParsingState.SETTING_TABLE_HEADER;
        } else if (type == RobotTokenType.VARIABLES_TABLE_HEADER) {
            // status = StateStatus.VARIABLE_TABLE_HEADER;
        } else if (type == RobotTokenType.TEST_CASES_TABLE_HEADER) {
            // status = StateStatus.TEST_CASE_TABLE_HEADER;
        } else if (type == RobotTokenType.KEYWORDS_TABLE_HEADER) {
            // status = StateStatus.KEYWORD_TABLE_HEADER;
        }

        return status;
    }

    private static enum ParsingState {
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
        SETTING_TABLE_HEADER,
        /**
         * 
         */
        SETTING_TABLE_INSIDE;
    }
}
