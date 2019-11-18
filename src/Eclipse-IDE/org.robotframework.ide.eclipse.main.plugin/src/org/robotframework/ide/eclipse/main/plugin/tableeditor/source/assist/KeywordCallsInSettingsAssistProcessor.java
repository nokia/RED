/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;

/**
 * @author Michal Anglart
 */
public class KeywordCallsInSettingsAssistProcessor extends KeywordCallsAssistProcessor {

    public KeywordCallsInSettingsAssistProcessor(final AssistantContext assist) {
        super(assist);
    }

    @Override
    public List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        return isInApplicableContentType(document, offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) == 1
                && ModelUtilities.isKeywordBasedGeneralSetting(assist.getModel(), offset);
    }

    @Override
    protected boolean isTemplateSetting(final int offset) {
        return ModelUtilities.isTemplateGeneralSetting(assist.getModel(), offset);
    }
}
