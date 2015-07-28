package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken.RobotTokenType;


public class HashCommentRecognizer extends ATokenRecognizer {

    /**
     * must not start from '\' and contains at the beginning '#'
     */
    public static final Pattern EXPECTED = Pattern.compile("^(?!\\\\)#.*$");


    public HashCommentRecognizer() {
        super(EXPECTED, RobotTokenType.START_HASH_COMMENT);
    }
}
