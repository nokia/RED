package org.robotframework.ide.core.testData.text.read;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
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


    public RobotFile parse(final File robotFile) throws IOException {
        RobotFile file = parse(new InputStreamReader(new FileInputStream(
                robotFile), Charset.forName("UTF-8")));

        return file;
    }


    private RobotFile parse(final Reader reader) throws IOException {
        RobotFile rf = new RobotFile();
        BufferedReader lineReader = new BufferedReader(reader);
        int lineNumber = 0;
        String currentLineText = null;
        while((currentLineText = lineReader.readLine()) != null) {
            RobotLine line = new RobotLine(lineNumber);
            StringBuilder text = new StringBuilder(currentLineText);
            int lastColumnProcessed = 0;
            ALineSeparator separator = tokenSeparatorBuilder.createSeparator(
                    lineNumber, currentLineText);

            int textLength = currentLineText.length();
            if (textLength > 0) {
                while(lastColumnProcessed < textLength) {
                    if (separator.hasNext()) {
                        Separator currentSeparator = separator.next();
                        int startColumn = currentSeparator.getStartColumn();
                        int remainingData = startColumn - lastColumnProcessed;
                        if (remainingData > 0) {
                            RobotToken rt = processLineElement(rf, lineNumber,
                                    startColumn, text.substring(
                                            lastColumnProcessed, startColumn));
                            line.addLineElement(rt);
                        }

                        line.addLineElement(currentSeparator);
                        lastColumnProcessed = currentSeparator.getEndColumn();
                    } else {
                        RobotToken rt = processLineElement(rf, lineNumber,
                                lastColumnProcessed,
                                text.substring(lastColumnProcessed));
                        line.addLineElement(rt);
                        lastColumnProcessed = textLength;
                    }
                }
            }

            lineNumber++;
            rf.addNewLine(line);
        }

        return rf;
    }


    private RobotToken processLineElement(final RobotFile robotFile, int line,
            int startColumn, String text) {
        RobotToken rt = new RobotToken();
        rt.setLineNumber(line);
        rt.setStartColumn(startColumn);
        rt.setText(new StringBuilder(text));

        return rt;
    }
}
