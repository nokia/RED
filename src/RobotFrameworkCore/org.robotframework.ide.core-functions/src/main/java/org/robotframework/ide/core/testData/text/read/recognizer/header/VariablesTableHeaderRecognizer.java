package org.robotframework.ide.core.testData.text.read.recognizer.header;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class VariablesTableHeaderRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[ ]?[*]+[\\s]*("
            + createUpperLowerCaseWord("Variables") + "|"
            + createUpperLowerCaseWord("Variable") + ")[\\s]*[*]*");


    public VariablesTableHeaderRecognizer() {
        super(EXPECTED, RobotTokenType.VARIABLES_TABLE_HEADER);
    }
}
