/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Iterables.transform;
import static org.robotframework.ide.eclipse.main.plugin.assist.AssistProposals.sortedByLabelsPrefixedFirst;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.ImmutableTable.Builder;
import com.google.common.collect.Table;

public class RedSettingProposals {

    private static final Table<SettingTarget, String, String> DESCRIBED_SETTINGS;
    static {
        final Builder<SettingTarget, String, String> builder = ImmutableTable.builder();

        builder.put(SettingTarget.TEST_CASE, "[tags]",
                "These tags are set to this test case and they possibly override Default Tags");
        builder.put(SettingTarget.TEST_CASE, "[documentation]",
                "Documentation of current test case");
        builder.put(SettingTarget.TEST_CASE, "[setup]",
                "The keyword %s is executed before other keywords inside the definition");
        builder.put(SettingTarget.TEST_CASE, "[template]",
                "The keyword %s is used as a template");
        builder.put(SettingTarget.TEST_CASE, "[timeout]",
                "Specifies maximum time this test case is allowed to execute before being aborted.\n"
                + "This setting overrides Test Timeout setting set on suite level\n"
                        + "Numerical values are interpreted as seconds but special syntax like '1min 15s' or '2 hours' can be used.");
        builder.put(SettingTarget.TEST_CASE, "[teardown]",
                "The keyword %s is executed after every other keyword inside the definition");

        builder.put(SettingTarget.KEYWORD, "[tags]",
                "These tags are set to this keyword and are not affected by Default Tags or Force Tags setting");
        builder.put(SettingTarget.KEYWORD, "[documentation]",
                "Documentation of current keyword");
        builder.put(SettingTarget.KEYWORD, "[teardown]",
                "The keyword %s is executed after every other keyword inside the definition");
        builder.put(SettingTarget.KEYWORD, "[arguments]",
                "Specifies arguments of current keyword");
        builder.put(SettingTarget.KEYWORD, "[timeout]",
                "Specifies maximum time this keyword is allowed to execute before being aborted.\n"
                + "This setting overrides Test Timeout setting set on suite level\n"
                        + "Numerical values are interpreted as seconds but special syntax like '1min 15s' or '2 hours' can be used.");
        builder.put(SettingTarget.KEYWORD, "[return]",
                "Specify the return value for this keyword. Multiple values can be used.");

        builder.put(SettingTarget.GENERAL, "library", "Imports library given by its name or path");
        builder.put(SettingTarget.GENERAL, "resource", "Imports resource file to be used in current suite");
        builder.put(SettingTarget.GENERAL, "variables", "Imports variables file to be used in current suite");
        builder.put(SettingTarget.GENERAL, "documentation", "Documentation of current suite");
        builder.put(SettingTarget.GENERAL, "metadata", "Metadata current suite hold");
        builder.put(SettingTarget.GENERAL, "suite setup", "The keyword %s is executed before executing any of the test cases or lower level suites");
        builder.put(SettingTarget.GENERAL, "suite teardown", "The keyword %s is executed after all test cases and lower level suites have been executed");
        builder.put(SettingTarget.GENERAL, "force tags", "Sets tags to all test cases in this suite. Inherited tags are not shown here.");
        builder.put(SettingTarget.GENERAL, "default tags", "Sets tags to all tests cases in this suite, unless test case specifies own tags");
        builder.put(SettingTarget.GENERAL, "test setup", "The keyword %s is executed before every test cases in this suite unless test cases override it");
        builder.put(SettingTarget.GENERAL, "test teardown", "The keyword %s is executed after every test cases in this suite unless test cases override it");
        builder.put(SettingTarget.GENERAL, "test template", "The keyword %s is used as default template keyword in this suite");
        builder.put(SettingTarget.GENERAL, "test timeout",
                "Specifies default timeout for each test case in this suite, which can be overridden by test case settings.\n"
                        + "Numerical values are interpreted as seconds but special syntax like '1min 15s' or '2 hours' can be used.");
        DESCRIBED_SETTINGS = builder.build();
    }

    public static boolean isSetting(final SettingTarget target, final String label) {
        return label != null && DESCRIBED_SETTINGS.contains(target, label.toLowerCase());
    }

    public static String getSettingDescription(final SettingTarget target, final String settingName,
            final String additionalArgument) {
        final String arg = additionalArgument.isEmpty() ? "given in first argument" : additionalArgument;
        return String.format(DESCRIBED_SETTINGS.get(target, settingName.toLowerCase()), arg);
    }

    private final SettingTarget target;
    private final ProposalMatcher matcher;

    public RedSettingProposals(final SettingTarget target) {
        this(target, ProposalMatchers.substringMatcher());
    }

    @VisibleForTesting
    RedSettingProposals(final SettingTarget target, final ProposalMatcher matcher) {
        this.target = target;
        this.matcher = matcher;
    }

    public List<? extends AssistProposal> getSettingsProposals(final String userContent) {
        return getSettingsProposals(userContent, sortedByLabelsPrefixedFirst(userContent));
    }

    public List<? extends AssistProposal> getSettingsProposals(final String userContent,
            final Comparator<? super RedSettingProposal> comparator) {

        final List<RedSettingProposal> proposals = new ArrayList<>();

        for (final String settingName : DESCRIBED_SETTINGS.row(target).keySet()) {
            final Optional<ProposalMatch> match = matcher.matches(userContent, settingName);

            if (match.isPresent()) {
                final String name = toCanonicalName(settingName);
                proposals.add(AssistProposals.createSettingProposal(name, target, match.get()));
            }
        }
        proposals.sort(comparator);
        return proposals;
    }

    private String toCanonicalName(final String settingName) {
        switch (target) {
            case GENERAL:
                final Iterable<String> upperCased = transform(Splitter.on(' ').split(settingName),
                        elem -> CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, elem));
                return Joiner.on(' ').join(upperCased);
            case TEST_CASE:
            case KEYWORD:
                final char firstLetter = settingName.charAt(1);
                return settingName.replaceAll("\\[" + firstLetter, "[" + Character.toUpperCase(firstLetter));
            default:
                throw new IllegalStateException("Unknown target value: " + target);
        }
    }

    public enum SettingTarget {
        TEST_CASE,
        KEYWORD,
        GENERAL
    }
}
