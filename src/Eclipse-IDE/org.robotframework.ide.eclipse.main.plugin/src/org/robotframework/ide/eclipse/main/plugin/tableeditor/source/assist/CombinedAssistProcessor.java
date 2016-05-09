/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;


/**
 * @author Michal Anglart
 *
 */
public class CombinedAssistProcessor extends RedContentAssistProcessor {

    private final List<RedContentAssistProcessor> processors;

    public CombinedAssistProcessor(final RedContentAssistProcessor... assistProcessors) {
        this.processors = newArrayList(assistProcessors);
    }

    @Override
    protected List<String> getApplicableContentTypes() {
        return newArrayList(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION, SuiteSourcePartitionScanner.SETTINGS_SECTION,
                SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Override
    protected String getProposalsTitle() {
        return "Smart";
    }

    @Override
    protected List<ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
        final List<ICompletionProposal> proposals = newArrayList();
        boolean shouldBeNull = true;

        for (final RedContentAssistProcessor processor : processors) {
            final List<? extends ICompletionProposal> newProposals = processor.computeProposals(viewer, offset);
            if (newProposals != null) {
                shouldBeNull = false;
                proposals.addAll(newProposals);
            }
        }
        return shouldBeNull && proposals.isEmpty() ? null : proposals;
    }
}
