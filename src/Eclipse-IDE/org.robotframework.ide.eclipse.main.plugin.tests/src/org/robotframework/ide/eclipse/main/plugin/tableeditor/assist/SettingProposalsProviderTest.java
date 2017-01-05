/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

public class SettingProposalsProviderTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void thereAreNoTestCaseSettingProposalsProvided_whenColumnIsDifferentThanFirst() {
        final SettingProposalsProvider provider = new SettingProposalsProvider(SettingTarget.TEST_CASE);

        for (int column = 0; column < 10; column++) {
            if (column == 0) { // first column is omitted
                continue;
            }
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            final RedContentProposal[] proposals = provider.getProposals("foo", 0, context);
            assertThat(proposals).isEmpty();
        }
    }

    @Test
    public void thereAreNoTestCaseSettingProposalsProvided_whenThereIsNoKeywordMatchingCurrentPrefix()
            throws Exception {
        final SettingProposalsProvider provider = new SettingProposalsProvider(SettingTarget.TEST_CASE);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.getProposals("xyz", 1, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreTestCaseProposalsProvided_whenPrefixIsMatchingAndProperContentIsInserted() throws Exception {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("[dx");

        final SettingProposalsProvider provider = new SettingProposalsProvider(SettingTarget.TEST_CASE);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.getProposals(text.getText(), 2, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("[Documentation]");
    }

    @Test
    public void thereAreNoKeywordSettingProposalsProvided_whenColumnIsDifferentThanFirst() {
        final SettingProposalsProvider provider = new SettingProposalsProvider(SettingTarget.KEYWORD);

        for (int column = 0; column < 10; column++) {
            if (column == 0) { // first column is omitted
                continue;
            }
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            final RedContentProposal[] proposals = provider.getProposals("foo", 0, context);
            assertThat(proposals).isEmpty();
        }
    }

    @Test
    public void thereAreNoKeywordSettingProposalsProvided_whenThereIsNoKeywordMatchingCurrentPrefix() throws Exception {
        final SettingProposalsProvider provider = new SettingProposalsProvider(SettingTarget.KEYWORD);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.getProposals("xyz", 1, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreKeywordSettingsProposalsProvided_whenPrefixIsMatchingAndProperContentIsInserted()
            throws Exception {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("[dx");

        final SettingProposalsProvider provider = new SettingProposalsProvider(SettingTarget.KEYWORD);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.getProposals(text.getText(), 2, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("[Documentation]");
    }

    @Test
    public void thereAreNoGeneralSettingProposalsProvided_whenColumnIsDifferentThanFirst() {
        final SettingProposalsProvider provider = new SettingProposalsProvider(SettingTarget.GENERAL);

        for (int column = 0; column < 10; column++) {
            if (column == 0) { // first column is omitted
                continue;
            }
            final AssistantContext context = new NatTableAssistantContext(column, 0);
            final RedContentProposal[] proposals = provider.getProposals("foo", 0, context);
            assertThat(proposals).isEmpty();
        }
    }

    @Test
    public void thereAreNoGeneralSettingProposalsProvided_whenThereIsNoKeywordMatchingCurrentPrefix()
            throws Exception {
        final SettingProposalsProvider provider = new SettingProposalsProvider(SettingTarget.GENERAL);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.getProposals("xyz", 1, context);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void thereAreGeneralSettingsProposalsProvided_whenPrefixIsMatchingAndProperContentIsInserted()
            throws Exception {
        final Text text = new Text(shellProvider.getShell(), SWT.SINGLE);
        text.setText("dox");

        final SettingProposalsProvider provider = new SettingProposalsProvider(SettingTarget.GENERAL);

        final AssistantContext context = new NatTableAssistantContext(0, 0);
        final RedContentProposal[] proposals = provider.getProposals(text.getText(), 2, context);
        assertThat(proposals).hasSize(1);

        proposals[0].getModificationStrategy().insert(text, proposals[0]);
        assertThat(text.getText()).isEqualTo("Documentation");
    }
}
