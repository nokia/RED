/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicate;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicates;
import org.robotframework.ide.eclipse.main.plugin.assist.DisableSettingReservedWordProposals;
import org.robotframework.ide.eclipse.main.plugin.assist.LibraryAliasReservedWordProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

import com.google.common.collect.Streams;

public class CodeReservedWordsInSettingsProposalsProvider implements RedContentProposalProvider {

    private final IRuntimeEnvironment environment;

    private final IRowDataProvider<?> dataProvider;

    public CodeReservedWordsInSettingsProposalsProvider(final IRuntimeEnvironment environment,
            final IRowDataProvider<?> dataProvider) {
        this.environment = environment;
        this.dataProvider = dataProvider;
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {

        final NatTableAssistantContext assistContext = (NatTableAssistantContext) context;
        final String prefix = contents.substring(0, position);

        final List<? extends AssistProposal> libraryAliasProposals = new LibraryAliasReservedWordProposals(
                createLibraryAliasPredicate(assistContext)).getReservedWordProposals(prefix);
        final List<? extends AssistProposal> disableSettingProposals = new DisableSettingReservedWordProposals(
                createDisableSettingPredicate(assistContext)).getReservedWordProposals(prefix);

        return Streams.concat(libraryAliasProposals.stream(), disableSettingProposals.stream())
                .map(proposal -> new AssistProposalAdapter(environment, proposal, p -> true,
                        () -> createOperationsToPerformAfterAccepting(proposal, (NatTableAssistantContext) context)))
                .toArray(RedContentProposal[]::new);
    }

    private AssistProposalPredicate<String> createLibraryAliasPredicate(final NatTableAssistantContext context) {
        final Object tableElement = dataProvider.getRowObject(context.getRow());
        if (tableElement instanceof RobotSetting) {
            final List<RobotToken> tokens = ((RobotKeywordCall) tableElement).getLinkedElement().getElementTokens();
            final Optional<RobotToken> firstTokenInLine = tokens.stream().findFirst();

            return AssistProposalPredicates.libraryAliasReservedWordPredicate(context.getColumn(), firstTokenInLine);
        } else {
            return AssistProposalPredicates.alwaysFalse();
        }
    }

    private AssistProposalPredicate<String> createDisableSettingPredicate(final NatTableAssistantContext context) {
        final Object tableElement = dataProvider.getRowObject(context.getRow());
        if (tableElement instanceof Entry) {
            final String settingName = (String) ((Entry<?, ?>) tableElement).getKey();
            final List<IRobotTokenType> firstTokenTypes = newArrayList(
                    RobotTokenType.findTypeOfDeclarationForSettingTable(settingName));

            return AssistProposalPredicates.disableSettingInSettingsReservedWordPredicate(context.getColumn(),
                    firstTokenTypes);
        } else {
            return AssistProposalPredicates.alwaysFalse();
        }
    }

    private List<Runnable> createOperationsToPerformAfterAccepting(final AssistProposal proposal,
            final NatTableAssistantContext tableContext) {
        final List<Runnable> operations = new ArrayList<>();

        if (LibraryAliasReservedWordProposals.WITH_NAME.equals(proposal.getLabel())) {
            final MultipleCellTableUpdater updater = new MultipleCellTableUpdater(tableContext, dataProvider);
            final List<String> valuesToInsert = newArrayList(proposal.getArguments());
            valuesToInsert.add(0, proposal.getLabel());
            if (updater.shouldInsertMultipleCellsWithoutColumnExceeding(valuesToInsert)) {
                operations.add(() -> updater.insertMultipleCells(valuesToInsert));
            }
        }

        return operations;
    }
}
