/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.header;

import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class CommentsTableHeaderRecognizer extends ATokenRecognizer {

    public static final Pattern EXPECTED = Pattern
            .compile("[ ]?([*][\\s]*)+[\\s]*(" + createUpperLowerCaseWordWithSpacesInside("Comments") + "|"
                    + createUpperLowerCaseWordWithSpacesInside("Comment") + ")([\\s]*[*])*");

    public CommentsTableHeaderRecognizer() {
        super(EXPECTED, RobotTokenType.COMMENTS_TABLE_HEADER);
    }

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isNewerOrEqualTo(new RobotVersion(3, 1));
    }

    @Override
    public ATokenRecognizer newInstance() {
        return new CommentsTableHeaderRecognizer();
    }
}
