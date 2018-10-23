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


public class SuiteFileProblemTest {

    @Test
    public void unrecognizedHeaderProblemForOlderRf_hasItsOwnCategoryAndHasResolution() {
        assertThat(SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER.getProblemCategory())
                .isEqualTo(ProblemCategory.UNRECOGNIZED_HEADER);
        assertThat(SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER.hasResolution()).isTrue();
    }

    @Test
    public void unrecognizedHeaderProblemForOlderRf_providesChangeToComment() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.VALUE, "")).thenReturn("Header");

        final List<? extends IMarkerResolution> fixers = SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER.createFixers(marker);

        assertThat(fixers).extracting(IMarkerResolution::getLabel).containsExactly("Change to '*** Comments ***'");
    }

    @Test
    public void unrecognizedHeaderProblemForOlderRf_providesChangeToSimilarHeader() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.VALUE, "")).thenReturn("Tset Cases");

        final List<? extends IMarkerResolution> fixers = SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER
                .createFixers(marker);

        assertThat(fixers).extracting(IMarkerResolution::getLabel)
                .containsExactly("Change to '*** Test Cases ***'", "Change to '*** Comments ***'");
    }

    @Test
    public void unrecognizedHeaderProblemForNewerRf_hasItsRuntimeErrorCategoryAndHasResolution() {
        assertThat(SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER_RF31.getProblemCategory())
                .isEqualTo(ProblemCategory.RUNTIME_ERROR);
        assertThat(SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER_RF31.hasResolution()).isTrue();
    }

    @Test
    public void unrecognizedHeaderProblemForNewerRf_providesChangeToComment() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.VALUE, "")).thenReturn("Header");

        final List<? extends IMarkerResolution> fixers = SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER_RF31
                .createFixers(marker);

        assertThat(fixers).extracting(IMarkerResolution::getLabel).containsExactly("Change to '*** Comments ***'");
    }

    @Test
    public void unrecognizedHeaderProblemForNewerRf_providesChangeToSimilarHeader() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.VALUE, "")).thenReturn("Keyods");

        final List<? extends IMarkerResolution> fixers = SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER_RF31
                .createFixers(marker);

        assertThat(fixers).extracting(IMarkerResolution::getLabel)
                .containsExactly("Change to '*** Keywords ***'", "Change to '*** Comments ***'");
    }
}
