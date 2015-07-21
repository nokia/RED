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
import org.robotframework.ide.core.testData.text.read.columnSeparators.ALineSeparator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.TokenSeparatorBuilder;
import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.header.KeywordsTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.header.SettingsTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.header.TestCasesTableHeaderRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.header.VariablesTableHeaderRecognizer;


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

        } catch (IOException e) {

        } catch (Exception e) {

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
        final Stack<State> processingState = new Stack<>();
        processingState.push(State.UNKNOWN);

        while((currentLineText = lineReader.readLine()) != null) {
            RobotLine line = new RobotLine(lineNumber);
            StringBuilder text = new StringBuilder(currentLineText);
            int lastColumnProcessed = 0;
            // get separator for this line
            ALineSeparator separator = tokenSeparatorBuilder.createSeparator(
                    lineNumber, currentLineText);

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
                            RobotToken rt = processLineElement(processingState,
                                    rf, lineNumber, startColumn,
                                    text.substring(lastColumnProcessed,
                                            startColumn));
                            line.addLineElement(rt);
                        }

                        line.addLineElement(currentSeparator);
                        lastColumnProcessed = currentSeparator.getEndColumn();
                    } else {
                        // last element in line
                        RobotToken rt = processLineElement(processingState, rf,
                                lineNumber, lastColumnProcessed,
                                text.substring(lastColumnProcessed));
                        line.addLineElement(rt);
                        lastColumnProcessed = textLength;
                    }
                }
            }

            lineNumber++;
            rf.addNewLine(line);
        }

        return parsingOutput;
    }


    private RobotToken processLineElement(final Stack<State> processingState,
            final RobotFile robotFile, int line, int startColumn, String text) {
        RobotToken rt = new RobotToken();
        rt.setLineNumber(line);
        rt.setStartColumn(startColumn);
        rt.setText(new StringBuilder(text));

        return rt;
    }

    private static enum State {
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
        SETTING_TABLE,
        /**
         * 
         */
        VARIABLE_TABLE,
        /**
         * 
         */
        TEST_CASE_TABLE,
        /**
         * 
         */
        TEST_CASE,
        /**
         * 
         */
        KEYWORD_TABLE,
        /**
         * 
         */
        KEYWORD,
        /**
         * 
         */
        FOR_LOOP
    }
}
