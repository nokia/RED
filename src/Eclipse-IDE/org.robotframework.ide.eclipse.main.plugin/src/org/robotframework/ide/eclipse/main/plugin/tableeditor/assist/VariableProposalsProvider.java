/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.text.IRegion;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicate;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposalPredicates;
import org.robotframework.ide.eclipse.main.plugin.assist.RedVariableProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposal.ModificationStrategy;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class VariableProposalsProvider implements RedContentProposalProvider {

    private final RobotSuiteFile suiteFile;

    private final IRowDataProvider<?> dataProvider;

    public VariableProposalsProvider(final RobotSuiteFile suiteFile, final IRowDataProvider<?> dataProvider) {
        this.suiteFile = suiteFile;
        this.dataProvider = dataProvider;
    }

    @Override
    public RedContentProposal[] getProposals(final String contents, final int position,
            final AssistantContext context) {
        final Optional<IRegion> liveVarRegion = DocumentUtilities.findLiveVariable(contents, position);
        final String liveVarPrefix = liveVarRegion.isPresent()
                ? contents.substring(liveVarRegion.get().getOffset(), position)
                : "";

        final String userContentToReplace;
        final ModificationStrategy modificationStrategy;
        if (liveVarPrefix.isEmpty()) {
            userContentToReplace = contents.substring(0, position);
            modificationStrategy = null;
        } else {
            userContentToReplace = liveVarPrefix;
            modificationStrategy = new VariableTextModificationStrategy();
        }

        final Object rowElement = dataProvider.getRowObject(((NatTableAssistantContext) context).getRow());
        final AssistProposalPredicate<String> predicate = createGlobalVarPredicate(rowElement);
        final List<? extends AssistProposal> variableProposals = new RedVariableProposals(suiteFile, predicate)
                .getVariableProposals(userContentToReplace, getModelElement(rowElement));

        return variableProposals.stream()
                .map(proposal -> new AssistProposalAdapter(proposal, modificationStrategy))
                .toArray(RedContentProposal[]::new);
    }

    private AssistProposalPredicate<String> createGlobalVarPredicate(final Object rowElement) {
        return rowElement instanceof RobotElement
                ? AssistProposalPredicates.globalVariablePredicate((RobotElement) rowElement)
                : AssistProposalPredicates.alwaysTrue();
    }

    private RobotFileInternalElement getModelElement(final Object rowElement) {
        if (rowElement instanceof RobotFileInternalElement) {
            return (RobotFileInternalElement) rowElement;
        } else if (rowElement instanceof Entry) {
            final RobotFileInternalElement element = (RobotFileInternalElement) ((Entry<?, ?>) rowElement).getValue();
            return element != null ? element : suiteFile.findSection(RobotSettingsSection.class).get();
        }
        throw new IllegalStateException("Unrecognized element in table");
    }

    private static class VariableTextModificationStrategy implements ModificationStrategy {

        @Override
        public void insert(final Text text, final IContentProposal proposal) {
            final String content = proposal.getContent();

            final String currentTextBeforeSelection = text.getText().substring(0, text.getSelection().x);
            final String currentTextAfterSelection = text.getText().substring(text.getSelection().x);

            final int maxCommon = calculateMaxCommon(content, currentTextBeforeSelection);

            String toInsert = currentTextBeforeSelection.substring(0, currentTextBeforeSelection.length() - maxCommon)
                    + content;
            final int newSelection = toInsert.length();
            toInsert += findSuffix(currentTextAfterSelection);

            text.setText(toInsert);
            text.setSelection(newSelection);
        }

        @Override
        public void insert(final Combo combo, final IContentProposal proposal) {
            throw new IllegalStateException("Not implemented");
        }

        private int calculateMaxCommon(final String content, final String currentTextBeforeSelection) {
            int maxCommon = 0;
            for (int i = 0; i < content.length() && i <= currentTextBeforeSelection.length(); i++) {
                final String currentSuffix = currentTextBeforeSelection
                        .substring(currentTextBeforeSelection.length() - i, currentTextBeforeSelection.length());
                final String toInsertPrefix = content.substring(0, i);

                if (currentSuffix.equalsIgnoreCase(toInsertPrefix)) {
                    maxCommon = toInsertPrefix.length();
                }
            }
            return maxCommon;
        }

        private String findSuffix(final String currentTextAfterSelection) {
            for (int i = 0; i < currentTextAfterSelection.length(); i++) {
                if (currentTextAfterSelection.charAt(i) == '}') {
                    return i == currentTextAfterSelection.length() - 1 ? ""
                            : currentTextAfterSelection.substring(i + 1);
                }
            }
            return currentTextAfterSelection;
        }
    }
}
