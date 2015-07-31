package org.robotframework.ide.core.testData.text.read.recognizer.settings;

import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.recognizer.ATokenRecognizer;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class LibraryAliasRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern
            .compile(createUpperLowerCaseWord("WITH") + "[\\s]+"
                    + createUpperLowerCaseWord("NAME"));


    public LibraryAliasRecognizer() {
        super(EXPECTED, RobotTokenType.SETTING_LIBRARY_ALIAS);
    }
}
