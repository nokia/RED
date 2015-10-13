/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.presenter;

import java.util.List;

import org.robotframework.ide.core.testData.model.IDataDrivenSetting;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class DataDrivenKeywordName<T extends IDataDrivenSetting> {

    public String createRepresentation(final List<T> templateArguments) {
        String templateKeywordName = null;
        if (!templateArguments.isEmpty()) {
            StringBuilder templateName = new StringBuilder("");
            int templatesNumber = templateArguments.size();
            for (int templateId = 0; templateId < templatesNumber; templateId++) {
                StringBuilder templateText = new StringBuilder("");
                IDataDrivenSetting template = templateArguments.get(templateId);
                RobotToken keywordName = template.getKeywordName();
                if (keywordName != null) {
                    templateText.append(keywordName.getRaw().toString());
                }

                List<RobotToken> unexpectedTrashArguments = template
                        .getUnexpectedTrashArguments();
                int numberOfTrashArgumentsLength = unexpectedTrashArguments
                        .size();

                for (int trashArgumentId = 0; trashArgumentId < numberOfTrashArgumentsLength; trashArgumentId++) {
                    if (templateText.length() > 0) {
                        templateText.append(' ');
                    }
                    templateText.append(unexpectedTrashArguments.get(
                            trashArgumentId).getRaw());
                }

                if (templateName.length() > 0) {
                    templateName.append(' ');
                    templateName.append(templateText.toString());
                } else if (templateText.length() > 0) {
                    templateName.append(templateText.toString());
                }
            }

            templateKeywordName = templateName.toString();
        }

        return templateKeywordName;
    }
}
