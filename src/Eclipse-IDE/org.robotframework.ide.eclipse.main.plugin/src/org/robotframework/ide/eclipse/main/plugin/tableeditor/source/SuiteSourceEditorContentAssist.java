/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals.sortedByNames;
import static org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposals.variablesSortedByTypesAndNames;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.ContentAssistKeywordContext;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.TextEditorContentAssist;


/**
 * @author Michal Anglart
 *
 */
public class SuiteSourceEditorContentAssist extends TextEditorContentAssist {

    private final RobotSuiteFile suiteModel;

    public SuiteSourceEditorContentAssist(final RobotSuiteFile robotSuiteFile) {
        super(null, null);
        this.suiteModel = robotSuiteFile;
    }

    @Override
    public List<RedVariableProposal> getVariables() {
        return new RedVariableProposals(suiteModel).getVariableProposals(variablesSortedByTypesAndNames());
    }

    @Override
    public List<RedVariableProposal> getVariables(final int offset) {
        return new RedVariableProposals(suiteModel).getVariableProposals(variablesSortedByTypesAndNames(), offset);
    }

    @Override
    public Map<String, ContentAssistKeywordContext> getKeywordMap() {
        final RedKeywordProposals proposals = new RedKeywordProposals(suiteModel);
        final List<RedKeywordProposal> keywordProposals = proposals.getKeywordProposals(sortedByNames());

        final Map<String, ContentAssistKeywordContext> mapping = new LinkedHashMap<>();
        for (final RedKeywordProposal proposal : keywordProposals) {
            mapping.put(proposal.getLabel(), new ContentAssistKeywordContext(proposal));
        }
        return mapping;
    }
}
