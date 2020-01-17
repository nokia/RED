/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

public class RfLintRuleDocUriTest {

    @Test
    public void isRfLintRuleDocUriTests() {
        assertThat(RfLintRuleDocUri.isRuleDocUri(URI.create("http://www.rf.org"))).isFalse();
        assertThat(RfLintRuleDocUri.isRuleDocUri(URI.create("file:///file/location"))).isFalse();
        assertThat(RfLintRuleDocUri.isRuleDocUri(URI.create("rflintrule:/"))).isFalse();

        assertThat(RfLintRuleDocUri.isRuleDocUri(URI.create("rflintrule:/RuleName"))).isTrue();
        assertThat(RfLintRuleDocUri.isRuleDocUri(URI.create("rflintrule:/RuleName?whatever"))).isTrue();
    }

    @Test
    public void docOfRuleUriIsProperlyCreatedFromRuleName() throws Exception {
        final URI uri = RfLintRuleDocUri.createRuleDocUri("Rule");
        assertThat(uri.getPath()).isEqualTo("/Rule");
        assertThat(uri.getQuery()).isNull();
    }

    @Test
    public void whenRuleDocUriIsBeingOpen_itsNameIsPassedToTheOpener() throws URISyntaxException {
        @SuppressWarnings("unchecked")
        final Consumer<String> opener = mock(Consumer.class);
        final RfLintRuleDocUri ruleUri = new RfLintRuleDocUri(RfLintRuleDocUri.createRuleDocUri("Rule"), opener);
        ruleUri.open();

        verify(opener).accept("Rule");
    }
}
