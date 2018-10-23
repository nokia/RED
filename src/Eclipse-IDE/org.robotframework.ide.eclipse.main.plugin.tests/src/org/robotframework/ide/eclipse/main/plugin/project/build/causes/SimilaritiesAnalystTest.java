/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class SimilaritiesAnalystTest {

    private final SimilaritiesAnalyst analyst = new SimilaritiesAnalyst();

    @Test
    public void similarSectionNamesAreProvided_whenThereIsASmallTypoInName() {
        assertThat(analyst.provideSimilarSectionNames("TestCases")).containsExactly("Test Cases");
        assertThat(analyst.provideSimilarSectionNames("Task")).containsExactly("Tasks");
        assertThat(analyst.provideSimilarSectionNames("Kyewords")).containsExactly("Keywords");
        assertThat(analyst.provideSimilarSectionNames("Vaiables")).containsExactly("Variables");
        assertThat(analyst.provideSimilarSectionNames("Settting")).containsExactly("Settings");
        assertThat(analyst.provideSimilarSectionNames("Comentss")).containsExactly("Comments");
    }

    @Test
    public void noSectionNamesAreProvided_whenTypoIsBigOrNothingSimilarAtAll() {
        assertThat(analyst.provideSimilarSectionNames("Tesses")).isEmpty();
        assertThat(analyst.provideSimilarSectionNames("Tasks Cases")).isEmpty();
        assertThat(analyst.provideSimilarSectionNames("Keyyyywords")).isEmpty();
        assertThat(analyst.provideSimilarSectionNames("Vairab")).isEmpty();
        assertThat(analyst.provideSimilarSectionNames("Setimgss")).isEmpty();
        assertThat(analyst.provideSimilarSectionNames("Vommment")).isEmpty();
        assertThat(analyst.provideSimilarSectionNames("Header")).isEmpty();
    }

}
