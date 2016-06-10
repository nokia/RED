/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class VariablesContentProposingSupportTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSupportProperties() {
        final VariablesContentProposingSupport support = new VariablesContentProposingSupport(
                mock(RobotSuiteFile.class));
        assertThat(support.getActivationKeys()).containsOnly('$', '@', '%', '&');
        assertThat(support.getKeyStroke()).isEqualTo(KeyStroke.getInstance(SWT.CTRL, ' '));
        assertThat(support.getLabelProvider()).isInstanceOf(VariableProposalsLabelProvider.class);
        assertThat(support.getProposalProvider()).isInstanceOf(VariableProposalsProvider.class);
        assertThat(support.getControlAdapter(mock(Text.class))).isInstanceOf(VariablesTextContentAdapter.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrownForNotSupportedControl() {
        final VariablesContentProposingSupport support = new VariablesContentProposingSupport(
                mock(RobotSuiteFile.class));
        support.getControlAdapter(mock(Combo.class));
    }
}
