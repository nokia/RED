package org.robotframework.ide.core.testData.text.read;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.text.read.columnSeparators.ALineSeparator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.TokenSeparatorBuilder;


public class TxtRobotFileParser {

    private final TokenSeparatorBuilder tokenSeparatorBuilder;


    public TxtRobotFileParser() {
        this.tokenSeparatorBuilder = new TokenSeparatorBuilder();
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
        int lastColumnProcessed = 0;
        String currentLineText = null;
        while((currentLineText = lineReader.readLine()) != null) {
            ALineSeparator separator = tokenSeparatorBuilder.createSeparator(
                    lineNumber, currentLineText);
            while(separator.hasNext()) {

            }

            lineNumber++;
        }

        return rf;
    }

}
