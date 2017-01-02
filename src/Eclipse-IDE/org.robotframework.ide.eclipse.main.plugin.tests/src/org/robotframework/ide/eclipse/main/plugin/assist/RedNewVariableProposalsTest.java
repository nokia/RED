/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.eclipse.main.plugin.assist.Commons.toLabels;

import java.util.List;

import org.junit.Test;

public class RedNewVariableProposalsTest {

    @Test
    public void allProposalsAreProvided_noMatterWhatUserWouldProvideAsInput() {
        final List<? extends AssistProposal> proposals = new RedNewVariableProposals().getNewVariableProposals();

        assertThat(transform(proposals, toLabels())).containsExactly("Fresh scalar", "Fresh list",
                "Fresh dictionary");
    }
}
