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
 *
 */
public class KeywordsInSettingsAssistProcessor extends KeywordCallsAssistProcessor {

    public KeywordsInSettingsAssistProcessor(final SuiteSourceAssistantContext assist) {
        super(assist);
    }

    @Override
    protected List<String> getValidContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Override
    protected boolean shouldShowProposals(final String lineContent, final IDocument document, final int offset)
            throws BadLocationException {
        return isInProperContentType(document, offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) == 1
                && (lineContent.toLowerCase().startsWith("suite setup")
                || lineContent.toLowerCase().startsWith("suite teardown")
                || lineContent.toLowerCase().startsWith("test setup")
                || lineContent.toLowerCase().startsWith("test teardown")
                || lineContent.toLowerCase().startsWith("test template"));
    }
}
