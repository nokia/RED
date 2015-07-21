package org.robotframework.ide.core.testData.text.read.recognizer.header;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;


public class KeywordsTableHeaderRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern.compile("[*]+[\\s]*("
            + createUpperLowerCaseWord("User") + "[\\s]+)?("
            + createUpperLowerCaseWord("Keywords") + "|"
            + createUpperLowerCaseWord("Keyword") + ")[\\s]*[*]*");


    public KeywordsTableHeaderRecognizer() {
        super(EXPECTED, RobotTokenType.KEYWORDS_TABLE_HEADER);
    }
}
