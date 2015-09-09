/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table.setting;

import org.robotframework.ide.core.testData.model.ATags;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class DefaultTags extends ATags {

    public DefaultTags(RobotToken declaration) {
        super(declaration);
    }


    @Override
    public ModelType getModelType() {
        return ModelType.DEFAULT_TAGS_SETTING;
    }
}
