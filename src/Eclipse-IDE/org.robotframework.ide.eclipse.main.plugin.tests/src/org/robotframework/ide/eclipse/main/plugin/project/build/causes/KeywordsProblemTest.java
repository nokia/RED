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
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;

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
    public void keywordFromNestedLibrary_hasResoulutionAndProvidesFixer() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn("myLib");

        final KeywordsProblem problem = KeywordsProblem.KEYWORD_FROM_NESTED_LIBRARY;

        assertThat(problem.hasResolution()).isTrue();
        final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
        assertThat(fixers).extracting(IMarkerResolution::getLabel).containsExactly("Import 'myLib' library");
    }

}
