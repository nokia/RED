/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.ui.IWorkbenchPage;
import org.junit.Test;
import org.rf.ide.core.rflint.RfLintRule;
import org.rf.ide.core.rflint.RfLintViolationSeverity;

public class RfLintRuleInputTest {

    @Test
    public void properRuleDocUriIsProvidedForInput() throws URISyntaxException {
        final RfLintRuleInput input = new RfLintRuleInput("Rule");

        assertThat(input.getInputUri().toString()).isEqualTo("rflintrule:/Rule");
    }

    @Test
    public void theInputDoesNotContainAnything() {
        final RfLintRuleInput input = new RfLintRuleInput("Rule");

        assertThat(input.contains(new Object())).isFalse();
        assertThat(input.contains("Rule")).isFalse();
        assertThat(input.contains(new RfLintRule("Rule", RfLintViolationSeverity.ERROR, ""))).isFalse();
    }

    @Test
    public void properHtmlIsReturnedForExistingRule() {
        final Map<String, RfLintRule> allRules = new HashMap<>();
        allRules.put("FirstRule", new RfLintRule("FirstRule", RfLintViolationSeverity.ERROR, ""));
        allRules.put("Rule", new RfLintRule("Rule", RfLintViolationSeverity.ERROR,
                "this is the rule doc\n\nsecond paragraph\n\nhttp://www.somelink.com"));
        allRules.put("ThirdRule", new RfLintRule("ThirdRule", RfLintViolationSeverity.WARNING, ""));

        final RfLintRuleInput input = new RfLintRuleInput("Rule");
        final String html = input.provideHtml(null, allRules);

        assertThat(html).contains("Default rule priority");
        assertThat(html).contains("Error");

        assertThat(html).contains("<p>this is the rule doc</p>");
        assertThat(html).contains("<p>second paragraph</p>");
        assertThat(html).contains("<p><a href=\"http://www.somelink.com\">http://www.somelink.com</a></p>");

        assertThat(html).containsPattern("<h\\d.*>All Rules</h\\d>");
        assertThat(html).contains("<a href=\"rflintrule:/FirstRule\">FirstRule</a>");
        assertThat(html).doesNotContain("<a href=\"rflintrule:/Rule\">Rule</a>").contains("&middot; Rule &middot;");
        assertThat(html).contains("<a href=\"rflintrule:/ThirdRule\">ThirdRule</a>");
    }

    @Test
    public void properHtmlIsReturnedForNonExistingRule() {
        final Map<String, RfLintRule> allRules = new HashMap<>();
        allRules.put("FirstRule", new RfLintRule("FirstRule", RfLintViolationSeverity.ERROR, "doc1"));
        allRules.put("SecondRule", new RfLintRule("SecondRule", RfLintViolationSeverity.ERROR, "doc2"));
        allRules.put("ThirdRule", new RfLintRule("ThirdRule", RfLintViolationSeverity.WARNING, "doc3"));

        final RfLintRuleInput input = new RfLintRuleInput("Rule");
        final String html = input.provideHtml(null, allRules);

        assertThat(html).contains("Default rule priority");
        assertThat(html).contains("Unknown");

        assertThat(html).containsPattern(
                "<p><span style=\"color: #ff0000\">Missing rule: Rule is not available in currently active environment</span></p>");

        assertThat(html).containsPattern("<h\\d.*>All Rules</h\\d>");
        assertThat(html).contains("<a href=\"rflintrule:/FirstRule\">FirstRule</a>");
        assertThat(html).contains("<a href=\"rflintrule:/SecondRule\">SecondRule</a>");
        assertThat(html).contains("<a href=\"rflintrule:/ThirdRule\">ThirdRule</a>");
    }

    @Test
    public void properRawDocumentationIsReturnedForExistingRule() {
        final Optional<RfLintRule> rule = Optional
                .of(new RfLintRule("Rule", RfLintViolationSeverity.WARNING, "documentation"));
        final RfLintRuleInput input = new RfLintRuleInput("Rule");
        final String raw = input.provideRawText(rule);

        assertThat(raw).isEqualTo("documentation");
    }

    @Test
    public void properRawDocumentationIsReturnedForNonExistingRule() {
        final Optional<RfLintRule> rule = Optional.empty();
        final RfLintRuleInput input = new RfLintRuleInput("Rule");
        final String raw = input.provideRawText(rule);

        assertThat(raw).isEqualTo("Missing rule: Rule is not available in currently active environment");
    }

    @Test
    public void nothingHappensWhenTryingToShowTheInput() throws Exception {
        final RfLintRuleInput input = new RfLintRuleInput("Rule");

        final IWorkbenchPage page = mock(IWorkbenchPage.class);
        input.showInput(page);

        verifyZeroInteractions(page);
    }
}
