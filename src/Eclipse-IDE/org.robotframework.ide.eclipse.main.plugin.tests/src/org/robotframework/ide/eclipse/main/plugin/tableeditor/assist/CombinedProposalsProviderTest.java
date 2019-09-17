/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedContentProposalProvider;

public class CombinedProposalsProviderTest {

    @Test
    public void emptyArrayIsProvided_whenThereAreNoProvidersCombined() {
        final CombinedProposalsProvider provider = new CombinedProposalsProvider();
        final RedContentProposal[] proposals = provider.computeProposals("", 0, mock(AssistantContext.class));

        assertThat(proposals).isEmpty();
    }

    @Test
    public void emptyArrayIsProvided_whenCombinedProvidersDoNotProvideAnything_1() {
        final RedContentProposal[] nestedProposals = new RedContentProposal[0];

        final RedContentProposalProvider nested = mock(RedContentProposalProvider.class);
        when(nested.computeProposals(anyString(), anyInt(), any(AssistantContext.class))).thenReturn(nestedProposals);

        final CombinedProposalsProvider provider = new CombinedProposalsProvider(nested);
        final RedContentProposal[] proposals = provider.computeProposals("", 0, mock(AssistantContext.class));

        assertThat(proposals).isEmpty();
    }

    @Test
    public void emptyArrayIsProvided_whenCombinedProvidersDoNotProvideAnything_2() {
        final RedContentProposal[] nestedProposals1 = null;
        final RedContentProposal[] nestedProposals2 = new RedContentProposal[0];
        final RedContentProposal[] nestedProposals3 = new RedContentProposal[0];

        final RedContentProposalProvider nested1 = mock(RedContentProposalProvider.class);
        when(nested1.computeProposals(anyString(), anyInt(), any(AssistantContext.class))).thenReturn(nestedProposals1);

        final RedContentProposalProvider nested2 = mock(RedContentProposalProvider.class);
        when(nested1.computeProposals(anyString(), anyInt(), any(AssistantContext.class))).thenReturn(nestedProposals2);

        final RedContentProposalProvider nested3 = mock(RedContentProposalProvider.class);
        when(nested1.computeProposals(anyString(), anyInt(), any(AssistantContext.class))).thenReturn(nestedProposals3);

        final CombinedProposalsProvider provider = new CombinedProposalsProvider(nested1, nested2, nested3);
        final RedContentProposal[] proposals = provider.computeProposals("", 0, mock(AssistantContext.class));

        assertThat(proposals).isEmpty();
    }

    @Test
    public void proposalsAreProvided_whenNestedProvidersDoesProvideSomething() {
        final RedContentProposal p1 = mock(RedContentProposal.class);
        final RedContentProposal p2 = mock(RedContentProposal.class);
        final RedContentProposal p3 = mock(RedContentProposal.class);
        final RedContentProposal p4 = mock(RedContentProposal.class);
        final RedContentProposal p5 = mock(RedContentProposal.class);

        final RedContentProposal[] nestedProposals1 = new RedContentProposal[] { p1, p2 };
        final RedContentProposal[] nestedProposals2 = new RedContentProposal[0];
        final RedContentProposal[] nestedProposals3 = new RedContentProposal[] { p3, p4, p5 };

        final RedContentProposalProvider nested1 = mock(RedContentProposalProvider.class);
        when(nested1.computeProposals(anyString(), anyInt(), any(AssistantContext.class))).thenReturn(nestedProposals1);

        final RedContentProposalProvider nested2 = mock(RedContentProposalProvider.class);
        when(nested2.computeProposals(anyString(), anyInt(), any(AssistantContext.class))).thenReturn(nestedProposals2);

        final RedContentProposalProvider nested3 = mock(RedContentProposalProvider.class);
        when(nested3.computeProposals(anyString(), anyInt(), any(AssistantContext.class))).thenReturn(nestedProposals3);

        final CombinedProposalsProvider provider = new CombinedProposalsProvider(nested1, nested2, nested3);
        final RedContentProposal[] proposals = provider.computeProposals("", 0, mock(AssistantContext.class));

        assertThat(proposals).containsExactly(p1, p2, p3, p4, p5);
    }

    @Test
    public void onlyProposalsThatShouldBeShownAreProvided_whenNestedProvidersDoesProvideSomething() {
        final RedContentProposal p1 = mock(RedContentProposal.class);
        final RedContentProposal p2 = mock(RedContentProposal.class);
        final RedContentProposal p3 = mock(RedContentProposal.class);

        final RedContentProposal[] nestedProposals1 = new RedContentProposal[] { p1, p2 };
        final RedContentProposal[] nestedProposals2 = new RedContentProposal[0];
        final RedContentProposal[] nestedProposals3 = null;
        final RedContentProposal[] nestedProposals4 = new RedContentProposal[] { p3 };

        final RedContentProposalProvider nested1 = mock(RedContentProposalProvider.class);
        when(nested1.computeProposals(anyString(), anyInt(), any(AssistantContext.class))).thenReturn(nestedProposals1);

        final RedContentProposalProvider nested2 = mock(RedContentProposalProvider.class);
        when(nested2.computeProposals(anyString(), anyInt(), any(AssistantContext.class))).thenReturn(nestedProposals2);

        final RedContentProposalProvider nested3 = mock(RedContentProposalProvider.class);
        when(nested3.computeProposals(anyString(), anyInt(), any(AssistantContext.class))).thenReturn(nestedProposals3);

        final RedContentProposalProvider nested4 = mock(RedContentProposalProvider.class);
        when(nested4.computeProposals(anyString(), anyInt(), any(AssistantContext.class))).thenReturn(nestedProposals4);

        final CombinedProposalsProvider provider = new CombinedProposalsProvider(nested1, nested2, nested3, nested4);
        final RedContentProposal[] proposals = provider.computeProposals("", 0, mock(AssistantContext.class));

        assertThat(proposals).containsExactly(p1, p2, p3);
    }

}
