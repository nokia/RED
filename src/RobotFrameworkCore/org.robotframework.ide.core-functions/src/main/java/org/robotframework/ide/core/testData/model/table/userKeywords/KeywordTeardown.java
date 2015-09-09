/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.userKeywords;

import org.robotframework.ide.core.testData.model.AKeywordBaseSetting;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class KeywordTeardown extends AKeywordBaseSetting {

    public KeywordTeardown(RobotToken declaration) {
        super(declaration);
    }


    @Override
    public ModelType getModelType() {
        return ModelType.USER_KEYWORD_TEARDOWN;
    }
}
