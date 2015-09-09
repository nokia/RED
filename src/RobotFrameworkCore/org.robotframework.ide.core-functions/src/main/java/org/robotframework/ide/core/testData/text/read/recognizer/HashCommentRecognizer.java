/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.recognizer;

import java.util.regex.Pattern;


public class HashCommentRecognizer extends ATokenRecognizer {

    /**
     * must not start from '\' and contains at the beginning '#'
     */
    public static final Pattern EXPECTED = Pattern.compile("^(?!\\\\)#.*$");


    public HashCommentRecognizer() {
        super(EXPECTED, RobotTokenType.START_HASH_COMMENT);
    }
}
