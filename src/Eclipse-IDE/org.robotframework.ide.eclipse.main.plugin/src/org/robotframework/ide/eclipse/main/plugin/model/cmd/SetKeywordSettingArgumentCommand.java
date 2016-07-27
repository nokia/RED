/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.KeywordTableModelUpdater;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.views.DocumentationView;

public class SetKeywordSettingArgumentCommand extends SetKeywordCallArgumentCommand {

    public SetKeywordSettingArgumentCommand(final RobotKeywordCall keywordCall, final int index, final String value) {
        super(keywordCall, index, value);
    }

    @Override
    protected void updateModelElement() {

        final AModelElement<?> linkedElement = getKeywordCall().getLinkedElement();
        new KeywordTableModelUpdater().update(linkedElement, getIndex(), getValue());

        if (linkedElement.getModelType() == ModelType.USER_KEYWORD_DOCUMENTATION) {
            eventBroker.post(DocumentationView.REFRESH_DOC_EVENT_TOPIC, getKeywordCall());
        }
    }

    @Override
    protected boolean isKeywordBasedSetting() {
        final ModelType modelType = getKeywordCall().getLinkedElement().getModelType();
        return modelType == ModelType.USER_KEYWORD_TEARDOWN || modelType == ModelType.USER_KEYWORD_TIMEOUT;
    }
}
