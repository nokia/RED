package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.regex.Pattern;


public class PreviousLineContinueRecognizer extends ATokenRecognizer {

    /**
     * must not start from '\' and contains at the beginning '#'
     */
    public static final Pattern EXPECTED = Pattern.compile("^[.]{3,}");


    public PreviousLineContinueRecognizer() {
        super(EXPECTED, RobotTokenType.PREVIOUS_LINE_CONTINUE);
    }
}
