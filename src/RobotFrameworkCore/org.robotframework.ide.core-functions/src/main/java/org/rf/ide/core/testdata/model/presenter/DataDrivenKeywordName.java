/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter;

import java.util.List;

import org.rf.ide.core.testdata.model.IDataDrivenSetting;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class DataDrivenKeywordName {

    public static <T extends IDataDrivenSetting> String createRepresentation(final List<T> templateArguments) {
        String templateKeywordName = null;
        if (!templateArguments.isEmpty()) {
            final StringBuilder templateName = new StringBuilder("");
            final int templatesNumber = templateArguments.size();
            for (int templateId = 0; templateId < templatesNumber; templateId++) {
                final StringBuilder templateText = new StringBuilder("");
                final IDataDrivenSetting template = templateArguments.get(templateId);
                final RobotToken keywordName = template.getKeywordName();
                if (keywordName != null) {
                    templateText.append(keywordName.getText().toString());
                }

                final List<RobotToken> unexpectedTrashArguments = template
                        .getUnexpectedTrashArguments();
                final int numberOfTrashArgumentsLength = unexpectedTrashArguments
                        .size();

                for (int trashArgumentId = 0; trashArgumentId < numberOfTrashArgumentsLength; trashArgumentId++) {
                    if (templateText.length() > 0) {
                        templateText.append(' ');
                    }
                    templateText.append(unexpectedTrashArguments.get(
                            trashArgumentId).getText());
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
