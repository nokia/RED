/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

@ExtendWith(FreshShellExtension.class)
public class SettingProposalsProviderTest {

    @FreshShell
    Shell shell;

    @Test
    public void thereAreNoTestCaseSettingProposalsProvided_whenColumnIsDifferentThanFirst() {
        final SettingProposalsProvider provider = new SettingProposalsProvider(null, SettingTarget.TEST_CASE);

        for (int column = 0; column < 10; column++) {
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            if (column == 0) {
                assertThat(provider.computeProposals("foo", 0, context)).isNotNull();
            } else {
                assertThat(provider.computeProposals("foo", 0, context)).isNull();
            }
        }
    }

    @Test
    public void thereAreNoTestCaseSettingProposalsProvided_whenThereIsNoKeywordMatchingCurrentInput() throws Exception {
        final SettingProposalsProvider provider = new SettingProposalsProvider(null, SettingTarget.TEST_CASE);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.computeProposals("xyz", 1, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreTestCaseProposalsProvided_whenInputIsMatchingAndProperContentIsInserted() throws Exception {
        final Text text = new Text(shell, SWT.SINGLE);
        text.setText("[dx");

        final SettingProposalsProvider provider = new SettingProposalsProvider(null, SettingTarget.TEST_CASE);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 2, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("[Documentation]");
    }

    @Test
    public void thereAreNoKeywordSettingProposalsProvided_whenColumnIsDifferentThanFirst() {
        final SettingProposalsProvider provider = new SettingProposalsProvider(null, SettingTarget.KEYWORD);

        for (int column = 0; column < 10; column++) {
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            if (column == 0) {
                assertThat(provider.computeProposals("foo", 0, context)).isNotNull();
            } else {
                assertThat(provider.computeProposals("foo", 0, context)).isNull();
            }
        }
    }

    @Test
    public void thereAreNoKeywordSettingProposalsProvided_whenThereIsNoKeywordMatchingCurrentInput() throws Exception {
        final SettingProposalsProvider provider = new SettingProposalsProvider(null, SettingTarget.KEYWORD);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.computeProposals("xyz", 1, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreKeywordSettingsProposalsProvided_whenInputIsMatchingAndProperContentIsInserted()
            throws Exception {
        final Text text = new Text(shell, SWT.SINGLE);
        text.setText("cut");

        final SettingProposalsProvider provider = new SettingProposalsProvider(null, SettingTarget.KEYWORD);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 2, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("[Documentation]");
    }

    @Test
    public void thereAreNoGeneralSettingProposalsProvided_whenColumnIsDifferentThanFirst() {
        final SettingProposalsProvider provider = new SettingProposalsProvider(null, SettingTarget.GENERAL_TESTS);

        for (int column = 0; column < 10; column++) {
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            if (column == 0) {
                assertThat(provider.computeProposals("foo", 0, context)).isNotNull();
            } else {
                assertThat(provider.computeProposals("foo", 0, context)).isNull();
            }
        }
    }

    @Test
    public void thereAreNoGeneralSettingProposalsProvided_whenThereIsNoKeywordMatchingCurrentInput() throws Exception {
        final SettingProposalsProvider provider = new SettingProposalsProvider(null, SettingTarget.GENERAL_TESTS);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.computeProposals("xyz", 1, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreGeneralSettingsProposalsProvided_whenInputIsMatchingAndProperContentIsInserted()
            throws Exception {
        final Text text = new Text(shell, SWT.SINGLE);
        text.setText("cut");

        final SettingProposalsProvider provider = new SettingProposalsProvider(null, SettingTarget.GENERAL_TESTS);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.computeProposals(text.getText(), 2, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("Documentation");
    }
}
