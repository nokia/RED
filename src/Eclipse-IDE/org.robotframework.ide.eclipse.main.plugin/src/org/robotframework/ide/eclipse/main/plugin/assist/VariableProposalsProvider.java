/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposals.variablesSortedByTypesAndNames;

import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

import com.google.common.primitives.Ints;

class VariableProposalsProvider implements IContentProposalProvider {

    private final RobotSuiteFile suiteFile;

    VariableProposalsProvider(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public IContentProposal[] getProposals(final String contents, final int position) {
        int varStart = calculateVariableStartOffset(contents, position);
        varStart = varStart == -1 ? position : varStart;
        final String prefix = contents.substring(varStart, position);

        final List<RedVariableProposal> variableEntities = new RedVariableProposals(suiteFile)
                .getVariableProposals(variablesSortedByTypesAndNames());

        final List<VariableContentProposal> proposals = newArrayList();
        for (final RedVariableProposal proposedVariable : variableEntities) {
            if (proposedVariable.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                proposals.add(new VariableContentProposal(proposedVariable, prefix));
            }
        }
        return proposals.toArray(new IContentProposal[0]);
    }

    private int calculateVariableStartOffset(final String content, final int position) {
        final String cuttedContent = content.substring(0, position);
        return Ints.max(cuttedContent.lastIndexOf('$'), cuttedContent.lastIndexOf('@'),
                cuttedContent.lastIndexOf('&'), cuttedContent.lastIndexOf('%'));
    }
}
