/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;

import com.google.common.collect.ImmutableMap;

public class KeywordsProblemTest {

    @Test
    public void forInExpressionWronglyTypedIsInDeprecatedApiCategory() {
        assertThat(KeywordsProblem.FOR_IN_EXPR_WRONGLY_TYPED.getProblemCategory())
                .isEqualTo(ProblemCategory.DEPRECATED_API);
    }

    @Test
    public void forInExpressionWronglyTyped_hasResolutionAndProvidesFixer() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn("IN RANGE");

        final KeywordsProblem problem = KeywordsProblem.FOR_IN_EXPR_WRONGLY_TYPED;

        assertThat(problem.hasResolution()).isTrue();
        final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
        assertThat(fixers).extracting(IMarkerResolution::getLabel).containsExactly("Change to 'IN RANGE'");
    }

    @Test
    public void keywordFromNestedLibrary_hasResolutionAndProvidesFixer() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn("myLib");

        final KeywordsProblem problem = KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY;

        assertThat(problem.hasResolution()).isTrue();
        final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
        assertThat(fixers).extracting(IMarkerResolution::getLabel).containsExactly("Import 'myLib' library");
    }

    @Test
    public void keywordNameWithDots_hasResolutionAndProvidesFixer() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn("Some.Keyword.Name");

        final KeywordsProblem problem = KeywordsProblem.KEYWORD_NAME_WITH_DOTS;

        assertThat(problem.hasResolution()).isTrue();
        final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
        assertThat(fixers).extracting(IMarkerResolution::getLabel).containsExactly("Change to 'Some_Keyword_Name'");
    }

    @Test
    public void maskedKeywordUsage_hasResolutionAndProvidesFixer() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn("Keyword Name");
        when(marker.getAttribute(AdditionalMarkerAttributes.SOURCES, "")).thenReturn("LibA;LibB;LibC");

        final KeywordsProblem problem = KeywordsProblem.MASKED_KEYWORD_USAGE;

        assertThat(problem.hasResolution()).isTrue();
        final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
        assertThat(fixers).extracting(IMarkerResolution::getLabel)
                .containsExactly("Add 'LibA' prefix to keyword call", "Add 'LibB' prefix to keyword call",
                        "Add 'LibC' prefix to keyword call");
    }

    @Test
    public void unknownKeywordDocumentSetting_hasResolutionAndProvidesFixer() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn("[Document]");

        final KeywordsProblem problem = KeywordsProblem.UNKNOWN_KEYWORD_SETTING;

        assertThat(problem.hasResolution()).isTrue();
        final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
        assertThat(fixers).extracting(IMarkerResolution::getLabel).containsExactly("Change to '[Documentation]'");
    }

    @Test
    public void unknownKeywordPostconditionSetting_hasResolutionAndProvidesFixer() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn("[Postcondition]");

        final KeywordsProblem problem = KeywordsProblem.UNKNOWN_KEYWORD_SETTING;

        assertThat(problem.hasResolution()).isTrue();
        final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
        assertThat(fixers).extracting(IMarkerResolution::getLabel).containsExactly("Change to '[Teardown]'");
    }

    @Test
    public void unknownKeywordSpaceSensitiveSetting_hasResolutionAndProvidesFixer() {
        final ImmutableMap<String, String> mapping = ImmutableMap.<String, String> builder()
                .put("[Ar gu me nts ]", "[Arguments]")
                .put("[ D oc umentati on]", "[Documentation]")
                .put("[ R e t u r n ]", "[Return]")
                .put("[ Tag s ]", "[Tags]")
                .put("[ Teard o w n]", "[Teardown]")
                .put("[ T imeou t ]", "[Timeout]")
                .build();
        mapping.forEach((name, replacement) -> {
            final IMarker marker = mock(IMarker.class);
            when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn(name);

            final KeywordsProblem problem = KeywordsProblem.UNKNOWN_KEYWORD_SETTING;

            assertThat(problem.hasResolution()).isTrue();
            final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
            assertThat(fixers).extracting(IMarkerResolution::getLabel)
                    .containsExactly("Change to '" + replacement + "'");
        });
    }

}
