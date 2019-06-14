/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbenchPage;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.rflint.RfLintRule;
import org.rf.ide.core.rflint.RfLintRules;
import org.rf.ide.core.rflint.RfLintViolationSeverity;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.RfLintRuleDocUri;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;

public class RfLintRuleInput implements DocumentationViewInput {

    private final String ruleName;

    public RfLintRuleInput(final String ruleName) {
        this.ruleName = ruleName;
    }

    @Override
    public URI getInputUri() throws URISyntaxException {
        return RfLintRuleDocUri.createRuleDocUri(ruleName);
    }

    @Override
    public boolean contains(final Object wrappedInput) {
        return false;
    }

    @Override
    public String provideHtml() throws DocumentationInputGenerationException {
        return provideHtml(RedPlugin.getDefault().getActiveRobotInstallation());
    }

    @Override
    public String provideHtml(final IRuntimeEnvironment environment) throws DocumentationInputGenerationException {
        final List<String> rulesFiles = RedPlugin.getDefault().getPreferences().getRfLintRulesFiles();
        final Map<String, RfLintRule> allRules = RfLintRules.getInstance()
                .loadRules(() -> environment.getRfLintRules(rulesFiles));

        return provideHtml(environment, allRules);
    }

    @VisibleForTesting
    String provideHtml(final IRuntimeEnvironment environment, final Map<String, RfLintRule> allRules)
            throws DocumentationInputGenerationException {
        final RfLintRule rule = allRules.get(ruleName);

        final String header = createHeader(rule);
        final String body = rule == null ? createUnknownRuleBody() : createRuleBody(rule);
        final String footer = createFooter(allRules.keySet());
        return new DocumentationsFormatter(environment).format(header + body + footer);
    }

    private String createHeader(final RfLintRule rule) {
        final Optional<URI> imgUri = RedImages.getRobotImageUri();

        final String severity = Optional.ofNullable(rule)
                .map(RfLintRule::getSeverity)
                .map(RfLintViolationSeverity::name)
                .orElse("UNKNOWN");
        final String filepath = Optional.ofNullable(rule)
                .map(RfLintRule::getFilepath)
                .map(path -> Formatters.hyperlink(new File(path).toURI(), path))
                .orElse("unknown");
        final String defaultSeverity = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, severity);
        return Formatters.simpleHeader(imgUri, ruleName, newArrayList("Source", filepath),
                newArrayList("Default rule priority", defaultSeverity));
    }

    private String createUnknownRuleBody() {
        return Formatters.paragraph(Formatters.span(
                "Missing rule: " + ruleName + " is not available in currently active environment", new RGB(255, 0, 0)));
    }

    private String createRuleBody(final RfLintRule rule) {
        final Matcher matcher = Pattern.compile("https?://[^\\s]+").matcher(rule.getDocumentation());
        final StringBuffer processedDoc = new StringBuffer();
        while (matcher.find()) {
            final String found = matcher.group(0);
            matcher.appendReplacement(processedDoc, "<a href=\"" + found + "\">" + found + "</a>");
        }
        matcher.appendTail(processedDoc);

        final String doc = processedDoc.toString();
        return Splitter.onPattern("\n\n+").splitToList(doc).stream().map(Formatters::paragraph).collect(joining(""));
    }

    private String createFooter(final Collection<String> allRules) {
        final String shortcuts = allRules
                .stream()
                .sorted()
                .map(this::createRuleLink)
                .collect(joining(" &middot; "));

        return Formatters.title("All Rules", 2) + Formatters.paragraph(shortcuts);
    }

    private String createRuleLink(final String name) {
        if (ruleName.equals(name)) {
            return name;
        } else {
            try {
                return Formatters.hyperlink(RfLintRuleDocUri.createRuleDocUri(name), name);
            } catch (final URISyntaxException e) {
                return name;
            }
        }
    }

    @Override
    public String provideRawText() throws DocumentationInputGenerationException {
        final Optional<RfLintRule> rule = Optional.ofNullable(RfLintRules.getInstance().getRule(ruleName));
        return provideRawText(rule);
    }

    @VisibleForTesting
    String provideRawText(final Optional<RfLintRule> rule) {
        return rule.map(RfLintRule::getDocumentation)
                .orElseGet(() -> "Missing rule: " + ruleName + " is not available in currently active environment");
    }

    @Override
    public void showInput(final IWorkbenchPage page) throws DocumentationInputOpenException {
        // nothing to do
    }

    @Override
    public IFile generateHtmlLibdoc() {
        throw new IllegalStateException("Unable to generate HTML doucmentation file for " + ruleName + " rule");
    }

}
