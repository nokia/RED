/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.assist.RedSettingProposals.SettingTarget;


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

    @Test
    public void similarSettingNamesAreProvided_whenThereIsASmallTypoInName() {
        assertThat(analyst.provideSimilarSettingNames(SettingTarget.TEST_CASE, "[Timeup]"))
                .containsExactly("[Timeout]");
        assertThat(analyst.provideSimilarSettingNames(SettingTarget.TASK, "[Tourdown]")).containsExactly("[Teardown]");
        assertThat(analyst.provideSimilarSettingNames(SettingTarget.KEYWORD, "[Togs]")).containsExactly("[Tags]");
        assertThat(analyst.provideSimilarSettingNames(SettingTarget.GENERAL_TESTS, "Tost Template"))
                .containsExactly("Test Template");
        assertThat(analyst.provideSimilarSettingNames(SettingTarget.GENERAL_TASKS, "Vuriablis"))
                .containsExactly("Variables");
    }

    @Test
    public void noSettingNamesAreProvided_whenTypoIsBigOrNothingSimilarAtAll() {
        assertThat(analyst.provideSimilarSettingNames(SettingTarget.TEST_CASE, "[Timelaps]")).isEmpty();
        assertThat(analyst.provideSimilarSettingNames(SettingTarget.TASK, "[Tourdrop]")).isEmpty();
        assertThat(analyst.provideSimilarSettingNames(SettingTarget.KEYWORD, "[Tagasas]")).isEmpty();
        assertThat(analyst.provideSimilarSettingNames(SettingTarget.GENERAL_TESTS, "Tost Plate")).isEmpty();
        assertThat(analyst.provideSimilarSettingNames(SettingTarget.GENERAL_TASKS, "Vulnerables")).isEmpty();
    }

}
