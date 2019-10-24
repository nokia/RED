/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.google.common.base.Joiner;

public class ASuiteFileDescriberTest {

    @Test
    public void testSuitesAreRecognizedProperly() {
        assertIsTestsSuite(content("*** Test Cases ***"));
        assertIsTestsSuite(content("*** Test Cases ***          "));
        assertIsTestsSuite(content("*** Test Cases ***", "*** Tasks ***"));
        assertIsTestsSuite(content("| *** Test Cases ***"));
        assertIsTestsSuite(content("| *** Test Cases ***          "));
        assertIsTestsSuite(content("| *** Test Cases ***", "| *** Tasks ***"));
        assertIsTsvTestsSuite(content("*** Test Cases ***"));
        assertIsTsvTestsSuite(content("*** Test Cases ***          "));
        assertIsTsvTestsSuite(content("*** Test Cases ***", "| *** Tasks ***"));
    }

    @Test
    public void tasksSuitesAreRecognizedProperly() {
        assertIsTasksSuite(content("*** Tasks ***"));
        assertIsTasksSuite(content("*** Tasks ***          "));
        assertIsTasksSuite(content("*** Tasks ***", "*** Test Cases ***"));
        assertIsTasksSuite(content("| *** Tasks ***"));
        assertIsTasksSuite(content("| *** Tasks ***          "));
        assertIsTasksSuite(content("| *** Tasks ***", "| *** Test Cases ***"));
        assertIsTsvTasksSuite(content("*** Tasks ***"));
        assertIsTsvTasksSuite(content("*** Tasks ***          "));
        assertIsTsvTasksSuite(content("*** Tasks ***", "| *** Test Cases ***"));
    }

    @Test
    public void resourceFilesAreRecognizedProperly() {
        assertIsResource(content());
        assertIsResource(content("*** Settings ***"));
        assertIsResource(content("*** Variables ***"));
        assertIsTsvResource(content());
        assertIsTsvResource(content("*** Settings ***"));
        assertIsTsvResource(content("*** Variables ***"));
    }

    private static void assertIsTestsSuite(final String content) {
        assertThat(ASuiteFileDescriber.getContentType("suite.robot", content))
                .isEqualTo(ASuiteFileDescriber.SUITE_FILE_ROBOT_CONTENT_ID);
    }

    private static void assertIsTsvTestsSuite(final String content) {
        assertThat(ASuiteFileDescriber.getContentType("suite.tsv", content))
                .isEqualTo(ASuiteFileDescriber.SUITE_FILE_TSV_CONTENT_ID);
    }

    private static void assertIsTasksSuite(final String content) {
        assertThat(ASuiteFileDescriber.getContentType("suite.robot", content))
                .isEqualTo(ASuiteFileDescriber.RPA_SUITE_FILE_ROBOT_CONTENT_ID);
    }

    private static void assertIsTsvTasksSuite(final String content) {
        assertThat(ASuiteFileDescriber.getContentType("suite.tsv", content))
                .isEqualTo(ASuiteFileDescriber.RPA_SUITE_FILE_TSV_CONTENT_ID);
    }

    private static void assertIsResource(final String content) {
        assertThat(ASuiteFileDescriber.getContentType("suite.robot", content))
                .isEqualTo(ASuiteFileDescriber.RESOURCE_FILE_CONTENT_ID);
    }

    private static void assertIsTsvResource(final String content) {
        assertThat(ASuiteFileDescriber.getContentType("suite.tsv", content))
                .isEqualTo(ASuiteFileDescriber.RESOURCE_FILE_CONTENT_ID);
    }

    private static String content(final String... lines) {
        return Joiner.on('\n').join(lines) + "\n";
    }
}
