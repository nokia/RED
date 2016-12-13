/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.Conditions.containing;

import org.junit.Test;

import com.google.common.collect.Range;

public class ProposalMatchTest {

    @Test
    public void testToFragmentsMapping() {
        final ProposalMatch match = new ProposalMatch(Range.closedOpen(0, 10));
        
        assertThat(match.mapToFragment(5, 15)).is(containing(new ProposalMatch(Range.closedOpen(0, 5))));
    }

}
