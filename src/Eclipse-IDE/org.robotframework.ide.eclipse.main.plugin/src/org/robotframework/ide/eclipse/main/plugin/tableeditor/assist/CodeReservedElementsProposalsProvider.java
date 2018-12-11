/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.environment.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicate;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicates;
import org.robotframework.ide.eclipse.main.plugin.assist.ForLoopReservedWordsProposals;
import org.robotframework.ide.eclipse.main.plugin.assist.GherkinReservedWordProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class CodeReservedElementsProposalsProvider implements RedContentProposalProvider {

    private final RobotRuntimeEnvironment environment;

    private final IRowDataProvider<?> dataProvider;

    public CodeReservedElementsProposalsProvider(final RobotRuntimeEnvironment environment,
            final IRowDataProvider<?> dataProvider) {
        this.environment = environment;
        this.dataProvider = dataProvider;
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {

        final NatTableAssistantContext assistContext = (NatTableAssistantContext) context;
        final String prefix = contents.substring(0, position);

        final List<? extends AssistProposal> loopsProposals = new ForLoopReservedWordsProposals(
                createForLoopsPredicate(assistContext)).getReservedWordProposals(prefix);
        final List<? extends AssistProposal> gherkinProposals = new GherkinReservedWordProposals(
                createGherkinPredicate(assistContext)).getReservedWordProposals(prefix);

        return Stream.concat(loopsProposals.stream(), gherkinProposals.stream())
                .map(proposal -> GherkinReservedWordProposals.GHERKIN_ELEMENTS.contains(proposal.getLabel())
                        ? new AssistProposalAdapter(environment, proposal, " ")
                        : new AssistProposalAdapter(environment, proposal, p -> true))
                .toArray(RedContentProposal[]::new);
    }

    private AssistProposalPredicate<String> createForLoopsPredicate(final NatTableAssistantContext context) {
        final Object tableElement = dataProvider.getRowObject(context.getRow());
        if (tableElement instanceof RobotKeywordCall) {
            final int cellIndex = context.getColumn();
            final List<RobotToken> tokens = ((RobotKeywordCall) tableElement).getLinkedElement().getElementTokens();

            final Optional<RobotToken> firstTokenInLine = tokens.isEmpty() ? Optional.empty()
                    : Optional.of(tokens.get(0));
            return AssistProposalPredicates.forLoopReservedWordsPredicate(cellIndex + 1, firstTokenInLine);
        } else {
            return AssistProposalPredicates.alwaysFalse();
        }
    }

    private AssistProposalPredicate<String> createGherkinPredicate(final NatTableAssistantContext context) {
        final Object tableElement = dataProvider.getRowObject(context.getRow());
        return tableElement instanceof RobotKeywordCall
                ? AssistProposalPredicates.gherkinReservedWordsPredicate(context.getColumn() + 1)
                : AssistProposalPredicates.alwaysFalse();
    }
}
