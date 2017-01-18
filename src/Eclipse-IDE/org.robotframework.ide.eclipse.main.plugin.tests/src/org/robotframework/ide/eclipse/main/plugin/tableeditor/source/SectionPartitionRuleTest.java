/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SectionPartitionRule.Section;

public class SectionPartitionRuleTest {

    private final IToken token = mock(IToken.class);

    @Test
    public void variablesSectionIsExpectedlyRecognized() {
        final Section section = Section.VARIABLES;
        assertRecognizedInRobot(source("*** Variables ***"), section, 18);
        assertRecognizedInRobot(source("*** Variables ***", ""), section, 19);
        assertRecognizedInRobot(source("*** Variables ***", "a"), section, 20);
        assertRecognizedInRobot(source("*** Variables ***", "abc"), section, 22);

        assertRecognizedInRobot(source("* * * Variables * * *"), section, 22);
        assertRecognizedInRobot(source("*Variables "), section, 12);
        assertRecognizedInRobot(source("* Variables "), section, 13);

        assertRecognizedInRobot(source("*** Variables ***", "abc", "*** Test Cases ***"), section, 22);
        assertRecognizedInRobot(source("*** Variables ***", "abc", "*** Keywords ***"), section, 22);
        assertRecognizedInRobot(source("*** Variables ***", "abc", "*** Settings ***"), section, 22);
        assertRecognizedInRobot(source("*** Variables ***", "abc", "*** Variables ***"), section, 40);

        assertRecognizedInRobot(source("*** Variables ***\tcolumn"), section, 25);
        assertRecognizedInRobot(source("*** Variables ***  column"), section, 26);
        assertRecognizedInRobot(source(" *** Variables ***"), section, 19);
        assertRecognizedInRobot(source("| *** Variables ***"), section, 20);
        assertRecognizedInRobot(source("| *** Variables *** | column"), section, 29);
    }

    @Test
    public void variablesSectionIsExpectedlyNotRecognized() {
        assertNotRecognizedInRobot(source("*** Variables ***"), Section.TEST_CASES);
        assertNotRecognizedInRobot(source("*** Variables ***"), Section.SETTINGS);
        assertNotRecognizedInRobot(source("*** Variables ***"), Section.KEYWORDS);

        assertNotRecognizedInRobot(source("Variables"), Section.VARIABLES);
        assertNotRecognizedInRobot(source("*** Variables $***"), Section.VARIABLES);
        assertNotRecognizedInRobot(source("***  Variables ***"), Section.VARIABLES);
    }

    @Test
    public void variablesSectionIsExpectedlyRecognizedInTsv() {
        final Section section = Section.VARIABLES;
        assertRecognizedInTsv(source("*** Variables ***"), section, 18);
        assertRecognizedInTsv(source("*** Variables ***", ""), section, 19);
        assertRecognizedInTsv(source("*** Variables ***", "a"), section, 20);
        assertRecognizedInTsv(source("*** Variables ***", "abc"), section, 22);

        assertRecognizedInTsv(source("* * * Variables * * *"), section, 22);
        assertRecognizedInTsv(source("*Variables "), section, 12);
        assertRecognizedInTsv(source("* Variables "), section, 13);

        assertRecognizedInTsv(source("*** Variables ***", "abc", "*** Test Cases ***"), section, 22);
        assertRecognizedInTsv(source("*** Variables ***", "abc", "*** Keywords ***"), section, 22);
        assertRecognizedInTsv(source("*** Variables ***", "abc", "*** Settings ***"), section, 22);
        assertRecognizedInTsv(source("*** Variables ***", "abc", "*** Variables ***"), section, 40);

        assertRecognizedInTsv(source("***  Variables ***"), section, 19);
        assertRecognizedInTsv(source("*** Variables ***\tcolumn"), section, 25);
        assertRecognizedInTsv(source("*** Variables ***  column"), section, 26);
        assertRecognizedInTsv(source(" *** Variables ***"), section, 19);
        assertRecognizedInTsv(source("| *** Variables ***"), section, 20);
        assertRecognizedInTsv(source("| *** Variables *** | column"), section, 29);
    }

    @Test
    public void variablesSectionIsExpectedlyNotRecognizedInTsv() {
        assertNotRecognizedInTsv(source("*** Variables ***"), Section.TEST_CASES);
        assertNotRecognizedInTsv(source("*** Variables ***"), Section.SETTINGS);
        assertNotRecognizedInTsv(source("*** Variables ***"), Section.KEYWORDS);

        assertNotRecognizedInTsv(source("Variables"), Section.VARIABLES);
        assertNotRecognizedInTsv(source("*** Variables $***"), Section.VARIABLES);
    }

    @Test
    public void settingsSectionIsExpectedlyRecognized() {
        final Section section = Section.SETTINGS;
        assertRecognizedInRobot(source("*** Settings ***"), section, 17);
        assertRecognizedInRobot(source("*** Settings ***", ""), section, 18);
        assertRecognizedInRobot(source("*** Settings ***", "a"), section, 19);
        assertRecognizedInRobot(source("*** Settings ***", "abc"), section, 21);

        assertRecognizedInRobot(source("* * * Settings * * *"), section, 21);
        assertRecognizedInRobot(source("*Settings "), section, 11);
        assertRecognizedInRobot(source("* Settings "), section, 12);

        assertRecognizedInRobot(source("*** Settings ***", "abc", "*** Test Cases ***"), section, 21);
        assertRecognizedInRobot(source("*** Settings ***", "abc", "*** Keywords ***"), section, 21);
        assertRecognizedInRobot(source("*** Settings ***", "abc", "*** Variables ***"), section, 21);
        assertRecognizedInRobot(source("*** Settings ***", "abc", "*** Settings ***"), section, 38);

        assertRecognizedInRobot(source("*** Settings ***\tcolumn"), section, 24);
        assertRecognizedInRobot(source("*** Settings ***  column"), section, 25);
        assertRecognizedInRobot(source(" *** Settings ***"), section, 18);
        assertRecognizedInRobot(source("| *** Settings ***"), section, 19);
        assertRecognizedInRobot(source("| *** Settings *** | column"), section, 28);
    }

    @Test
    public void settingsSectionIsExpectedlyNotRecognized() {
        assertNotRecognizedInRobot(source("*** Settings ***"), Section.TEST_CASES);
        assertNotRecognizedInRobot(source("*** Settings ***"), Section.VARIABLES);
        assertNotRecognizedInRobot(source("*** Settings ***"), Section.KEYWORDS);

        assertNotRecognizedInRobot(source("Settings"), Section.SETTINGS);
        assertNotRecognizedInRobot(source("*** Settings $***"), Section.SETTINGS);
        assertNotRecognizedInRobot(source("***  Settings ***"), Section.SETTINGS);
    }

    @Test
    public void settingsSectionIsExpectedlyRecognizedInTsv() {
        final Section section = Section.SETTINGS;
        assertRecognizedInTsv(source("*** Settings ***"), section, 17);
        assertRecognizedInTsv(source("*** Settings ***", ""), section, 18);
        assertRecognizedInTsv(source("*** Settings ***", "a"), section, 19);
        assertRecognizedInTsv(source("*** Settings ***", "abc"), section, 21);

        assertRecognizedInTsv(source("* * * Settings * * *"), section, 21);
        assertRecognizedInTsv(source("*Settings "), section, 11);
        assertRecognizedInTsv(source("* Settings "), section, 12);

        assertRecognizedInTsv(source("*** Settings ***", "abc", "*** Test Cases ***"), section, 21);
        assertRecognizedInTsv(source("*** Settings ***", "abc", "*** Keywords ***"), section, 21);
        assertRecognizedInTsv(source("*** Settings ***", "abc", "*** Variables ***"), section, 21);
        assertRecognizedInTsv(source("*** Settings ***", "abc", "*** Settings ***"), section, 38);

        assertRecognizedInTsv(source("***  Settings ***"), section, 18);
        assertRecognizedInTsv(source("*** Settings ***\tcolumn"), section, 24);
        assertRecognizedInTsv(source("*** Settings ***  column"), section, 25);
        assertRecognizedInTsv(source(" *** Settings ***"), section, 18);
        assertRecognizedInTsv(source("| *** Settings ***"), section, 19);
        assertRecognizedInTsv(source("| *** Settings *** | column"), section, 28);
    }

    @Test
    public void settingsSectionIsExpectedlyNotRecognizedInTsv() {
        assertNotRecognizedInTsv(source("*** Settings ***"), Section.TEST_CASES);
        assertNotRecognizedInTsv(source("*** Settings ***"), Section.VARIABLES);
        assertNotRecognizedInTsv(source("*** Settings ***"), Section.KEYWORDS);

        assertNotRecognizedInTsv(source("Settings"), Section.SETTINGS);
        assertNotRecognizedInTsv(source("*** Settings $***"), Section.SETTINGS);
    }

    @Test
    public void keywordsSectionIsExpectedlyRecognized() {
        final Section section = Section.KEYWORDS;
        assertRecognizedInRobot(source("*** Keywords ***"), section, 17);
        assertRecognizedInRobot(source("*** Keywords ***", ""), section, 18);
        assertRecognizedInRobot(source("*** Keywords ***", "a"), section, 19);
        assertRecognizedInRobot(source("*** Keywords ***", "abc"), section, 21);

        assertRecognizedInRobot(source("* * * Keywords * * *"), section, 21);
        assertRecognizedInRobot(source("*Keywords "), section, 11);
        assertRecognizedInRobot(source("* Keywords "), section, 12);

        assertRecognizedInRobot(source("*** Keywords ***", "abc", "*** Test Cases ***"), section, 21);
        assertRecognizedInRobot(source("*** Keywords ***", "abc", "*** Settings ***"), section, 21);
        assertRecognizedInRobot(source("*** Keywords ***", "abc", "*** Variables ***"), section, 21);
        assertRecognizedInRobot(source("*** Keywords ***", "abc", "*** Keywords ***"), section, 38);

        assertRecognizedInRobot(source("*** Keywords ***\tcolumn"), section, 24);
        assertRecognizedInRobot(source("*** Keywords ***  column"), section, 25);
        assertRecognizedInRobot(source(" *** Keywords ***"), section, 18);
        assertRecognizedInRobot(source("| *** Keywords ***"), section, 19);
        assertRecognizedInRobot(source("| *** Keywords *** | column"), section, 28);
    }

    @Test
    public void keywordsSectionIsExpectedlyNotRecognized() {
        assertNotRecognizedInRobot(source("*** Keywords ***"), Section.TEST_CASES);
        assertNotRecognizedInRobot(source("*** Keywords ***"), Section.VARIABLES);
        assertNotRecognizedInRobot(source("*** Keywords ***"), Section.SETTINGS);

        assertNotRecognizedInRobot(source("Keywords"), Section.KEYWORDS);
        assertNotRecognizedInRobot(source("*** Keywords $***"), Section.KEYWORDS);
        assertNotRecognizedInRobot(source("***  Keywords ***"), Section.KEYWORDS);
    }

    @Test
    public void keywordsSectionIsExpectedlyRecognizedInTsv() {
        final Section section = Section.KEYWORDS;
        assertRecognizedInTsv(source("*** Keywords ***"), section, 17);
        assertRecognizedInTsv(source("*** Keywords ***", ""), section, 18);
        assertRecognizedInTsv(source("*** Keywords ***", "a"), section, 19);
        assertRecognizedInTsv(source("*** Keywords ***", "abc"), section, 21);

        assertRecognizedInTsv(source("* * * Keywords * * *"), section, 21);
        assertRecognizedInTsv(source("*Keywords "), section, 11);
        assertRecognizedInTsv(source("* Keywords "), section, 12);

        assertRecognizedInTsv(source("*** Keywords ***", "abc", "*** Test Cases ***"), section, 21);
        assertRecognizedInTsv(source("*** Keywords ***", "abc", "*** Settings ***"), section, 21);
        assertRecognizedInTsv(source("*** Keywords ***", "abc", "*** Variables ***"), section, 21);
        assertRecognizedInTsv(source("*** Keywords ***", "abc", "*** Keywords ***"), section, 38);

        assertRecognizedInTsv(source("***  Keywords ***"), section, 18);
        assertRecognizedInTsv(source("*** Keywords ***\tcolumn"), section, 24);
        assertRecognizedInTsv(source("*** Keywords ***  column"), section, 25);
        assertRecognizedInTsv(source(" *** Keywords ***"), section, 18);
        assertRecognizedInTsv(source("| *** Keywords ***"), section, 19);
        assertRecognizedInTsv(source("| *** Keywords *** | column"), section, 28);
    }

    @Test
    public void keywordsSectionIsExpectedlyNotRecognizedInTsv() {
        assertNotRecognizedInTsv(source("*** Keywords ***"), Section.TEST_CASES);
        assertNotRecognizedInTsv(source("*** Keywords ***"), Section.VARIABLES);
        assertNotRecognizedInTsv(source("*** Keywords ***"), Section.SETTINGS);

        assertNotRecognizedInTsv(source("Keywords"), Section.KEYWORDS);
        assertNotRecognizedInTsv(source("*** Keywords $***"), Section.KEYWORDS);
    }

    @Test
    public void testCasesSectionIsExpectedlyRecognized() {
        final Section section = Section.TEST_CASES;
        assertRecognizedInRobot(source("*** Test Cases ***"), section, 19);
        assertRecognizedInRobot(source("*** Test Cases ***", ""), section, 20);
        assertRecognizedInRobot(source("*** Test Cases ***", "a"), section, 21);
        assertRecognizedInRobot(source("*** Test Cases ***", "abc"), section, 23);

        assertRecognizedInRobot(source("* * * Test Cases * * *"), section, 23);
        assertRecognizedInRobot(source("*Test Cases "), section, 13);
        assertRecognizedInRobot(source("* Test Cases "), section, 14);

        assertRecognizedInRobot(source("*** Test Cases ***", "abc", "*** Settings ***"), section, 23);
        assertRecognizedInRobot(source("*** Test Cases ***", "abc", "*** Variables ***"), section, 23);
        assertRecognizedInRobot(source("*** Test Cases ***", "abc", "*** Keywords ***"), section, 23);
        assertRecognizedInRobot(source("*** Test Cases ***", "abc", "*** Test Cases ***"), section, 42);

        assertRecognizedInRobot(source("*** Test Cases ***\tcolumn"), section, 26);
        assertRecognizedInRobot(source("*** Test Cases ***  column"), section, 27);
        assertRecognizedInRobot(source(" *** Test Cases ***"), section, 20);
        assertRecognizedInRobot(source("| *** Test Cases ***"), section, 21);
        assertRecognizedInRobot(source("| *** Test Cases *** | column"), section, 30);
    }

    @Test
    public void testCasesSectionIsExpectedlyNotRecognized() {
        assertNotRecognizedInRobot(source("*** Test Cases ***"), Section.KEYWORDS);
        assertNotRecognizedInRobot(source("*** Test Cases ***"), Section.VARIABLES);
        assertNotRecognizedInRobot(source("*** Test Cases ***"), Section.SETTINGS);

        assertNotRecognizedInRobot(source("Test Cases"), Section.TEST_CASES);
        assertNotRecognizedInRobot(source("*** Test Cases $***"), Section.TEST_CASES);
        assertNotRecognizedInRobot(source("***  Test Cases ***"), Section.TEST_CASES);
    }

    @Test
    public void testCasesSectionIsExpectedlyRecognizedInTsv() {
        final Section section = Section.TEST_CASES;
        assertRecognizedInTsv(source("*** Test Cases ***"), section, 19);
        assertRecognizedInTsv(source("*** Test Cases ***", ""), section, 20);
        assertRecognizedInTsv(source("*** Test Cases ***", "a"), section, 21);
        assertRecognizedInTsv(source("*** Test Cases ***", "abc"), section, 23);

        assertRecognizedInTsv(source("* * * Test Cases * * *"), section, 23);
        assertRecognizedInTsv(source("*Test Cases "), section, 13);
        assertRecognizedInTsv(source("* Test Cases "), section, 14);

        assertRecognizedInTsv(source("*** Test Cases ***", "abc", "*** Settings ***"), section, 23);
        assertRecognizedInTsv(source("*** Test Cases ***", "abc", "*** Variables ***"), section, 23);
        assertRecognizedInTsv(source("*** Test Cases ***", "abc", "*** Keywords ***"), section, 23);
        assertRecognizedInTsv(source("*** Test Cases ***", "abc", "*** Test Cases ***"), section, 42);

        assertRecognizedInTsv(source("***  Test Cases ***"), section, 20);
        assertRecognizedInTsv(source("*** Test Cases ***\tcolumn"), section, 26);
        assertRecognizedInTsv(source("*** Test Cases ***  column"), section, 27);
        assertRecognizedInTsv(source(" *** Test Cases ***"), section, 20);
        assertRecognizedInTsv(source("| *** Test Cases ***"), section, 21);
        assertRecognizedInTsv(source("| *** Test Cases *** | column"), section, 30);
    }

    @Test
    public void testCasesSectionIsExpectedlyNotRecognizedInTsv() {
        assertNotRecognizedInTsv(source("*** Test Cases ***"), Section.KEYWORDS);
        assertNotRecognizedInTsv(source("*** Test Cases ***"), Section.VARIABLES);
        assertNotRecognizedInTsv(source("*** Test Cases ***"), Section.SETTINGS);

        assertNotRecognizedInTsv(source("Test Cases"), Section.TEST_CASES);
        assertNotRecognizedInTsv(source("*** Test Cases $***"), Section.TEST_CASES);
    }

    private static String[] source(final String... content) {
        return content;
    }

    private void assertRecognizedInTsv(final String[] content, final Section section, final int endOffset) {
        assertRecognized(content, section, true, endOffset);
    }

    private void assertNotRecognizedInTsv(final String[] content, final Section section) {
        assertNotRecognized(content, section, true);
    }

    private void assertRecognizedInRobot(final String[] content, final Section section, final int endOffset) {
        assertRecognized(content, section, false, endOffset);
    }

    private void assertNotRecognizedInRobot(final String[] content, final Section section) {
        assertNotRecognized(content, section, false);
    }

    private void assertRecognized(final String[] content, final Section section, final boolean isTsv,
            final int endOffset) {
        final SectionPartitionRule partitionRule = new SectionPartitionRule(section, token, isTsv);

        final CharacterScanner scanner = new CharacterScanner(content);
        final IToken evaluation = partitionRule.evaluate(scanner);

        assertThat(evaluation).isSameAs(token);
        assertThat(scanner.getOffset()).isEqualTo(endOffset);
    }

    private void assertNotRecognized(final String[] content, final Section section, final boolean isTsv) {
        final SectionPartitionRule partitionRule = new SectionPartitionRule(section, token, isTsv);

        final CharacterScanner scanner = new CharacterScanner(content);
        final IToken evaluation = partitionRule.evaluate(scanner);

        assertThat(evaluation).isSameAs(Token.UNDEFINED);
        assertThat(scanner.getOffset()).isEqualTo(0);
    }
}
