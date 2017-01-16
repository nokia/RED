/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;

/**
 * @author Michal Anglart
 */
public class KeywordCallsInSettingsAssistProcessor extends KeywordCallsAssistProcessor {

    public KeywordCallsInSettingsAssistProcessor(final SuiteSourceAssistantContext assist) {
        super(assist);
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        return isInApplicableContentType(document, offset)
                && DocumentUtilities.getNumberOfCellSeparators(lineContent, assist.isTsvFile()) == 1
                && isKeywordBasedSetting(lineContent);
    }

    private boolean isKeywordBasedSetting(final String lineContent) {
        return startsWithOptionalSpace(lineContent, "test template")
                || startsWithOptionalSpace(lineContent, "suite setup")
                || startsWithOptionalSpace(lineContent, "suite teardown")
                || startsWithOptionalSpace(lineContent, "test setup")
                || startsWithOptionalSpace(lineContent, "test teardown");
    }

    private boolean startsWithOptionalSpace(final String string, final String potentialPrefix) {
        return string.toLowerCase().startsWith(potentialPrefix.toLowerCase())
                || string.toLowerCase().startsWith(" " + potentialPrefix.toLowerCase());
    }

    @Override
    protected List<String> getArguments(final AssistProposal proposal, final String lineContent) {
        if (startsWithOptionalSpace(lineContent, "test template")) {
            return new ArrayList<>();
        } else {
            return super.getArguments(proposal, lineContent);
        }
    }
}
