/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.recognizer.keywords;

import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.AExecutableElementSettingsRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public class KeywordTagsRecognizer extends AExecutableElementSettingsRecognizer {

    public KeywordTagsRecognizer() {
        super(RobotTokenType.KEYWORD_SETTING_TAGS);
    }

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion != null && robotVersion.isNewerOrEqualTo(new RobotVersion(2, 9));
    }

    @Override
    public ATokenRecognizer newInstance() {
        return new KeywordTagsRecognizer();
    }
}
