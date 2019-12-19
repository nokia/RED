/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IMarkerResolution;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ConvertToRobotFileFormat;

import com.google.common.collect.ImmutableMap;

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
    public void unrecognizedMetadataHeaderProblemForOlderRf_providesChangeToSettings() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.VALUE, "")).thenReturn("*** Metadata ***");

        final List<? extends IMarkerResolution> fixers = SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER
                .createFixers(marker);

        assertThat(fixers).extracting(IMarkerResolution::getLabel)
                .containsExactly("Change to '*** Settings ***'", "Change to '*** Comments ***'");
    }

    @Test
    public void unrecognizedUserKeywordsHeaderProblemForOlderRf_providesChangeToKeywords() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.VALUE, "")).thenReturn("*** User Keywords ***");

        final List<? extends IMarkerResolution> fixers = SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER
                .createFixers(marker);

        assertThat(fixers).extracting(IMarkerResolution::getLabel)
                .containsExactly("Change to '*** Keywords ***'", "Change to '*** Comments ***'");
    }

    @Test
    public void unrecognizedHeaderWithAdditionalSpacesProblemForOlderRf_providesChangeToCorrectHeader() {
        final ImmutableMap<String, String> mapping = ImmutableMap.<String, String> builder()
                .put("*** K eywords ***", "*** Keywords ***")
                .put("*** T e s t Cases ***", "*** Test Cases ***")
                .put("*** T a s k s ***", "*** Tasks ***")
                .put("*** S ett ing s ***", "*** Settings ***")
                .put("*** Var i a b l e s ***", "*** Variables ***")
                .put("*** Co mm en ts ***", "*** Comments ***")
                .build();
        mapping.forEach((value, replacement) -> {
            final IMarker marker = mock(IMarker.class);
            when(marker.getAttribute(AdditionalMarkerAttributes.VALUE, "")).thenReturn(value);

            final List<? extends IMarkerResolution> fixers = SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER
                    .createFixers(marker);

            assertThat(fixers).extracting(IMarkerResolution::getLabel)
                    .containsExactly("Change to '" + replacement + "'", "Change to '*** Comments ***'");
        });
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

    @Test
    public void unrecognizedMetadataHeaderProblemForNewerRf_providesChangeToSettings() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.VALUE, "")).thenReturn("*** Metadata ***");

        final List<? extends IMarkerResolution> fixers = SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER_RF31
                .createFixers(marker);

        assertThat(fixers).extracting(IMarkerResolution::getLabel)
                .containsExactly("Change to '*** Settings ***'", "Change to '*** Comments ***'");
    }

    @Test
    public void unrecognizedUserKeywordsHeaderProblemForNewerRf_providesChangeToKeywords() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.VALUE, "")).thenReturn("*** User Keywords ***");

        final List<? extends IMarkerResolution> fixers = SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER_RF31
                .createFixers(marker);

        assertThat(fixers).extracting(IMarkerResolution::getLabel)
                .containsExactly("Change to '*** Keywords ***'", "Change to '*** Comments ***'");
    }

    @Test
    public void unrecognizedHeaderWithAdditionalSpacesProblemForNewerRf_providesChangeToCorrectHeader() {
        final ImmutableMap<String, String> mapping = ImmutableMap.<String, String> builder()
                .put("*** K eywords ***", "*** Keywords ***")
                .put("*** T e s t Cases ***", "*** Test Cases ***")
                .put("*** T a s k s ***", "*** Tasks ***")
                .put("*** S ett ing s ***", "*** Settings ***")
                .put("*** Var i a b l e s ***", "*** Variables ***")
                .put("*** Co mm en ts ***", "*** Comments ***")
                .build();
        mapping.forEach((value, replacement) -> {
            final IMarker marker = mock(IMarker.class);
            when(marker.getAttribute(AdditionalMarkerAttributes.VALUE, "")).thenReturn(value);

            final List<? extends IMarkerResolution> fixers = SuiteFileProblem.UNRECOGNIZED_TABLE_HEADER_RF31
                    .createFixers(marker);

            assertThat(fixers).extracting(IMarkerResolution::getLabel)
                    .containsExactly("Change to '" + replacement + "'", "Change to '*** Comments ***'");
        });
    }

    @Test
    public void deprecatedSuiteExtensionProblemForRf31_hasDeprecatedApiCategoryAndHasResolution()
            throws URISyntaxException {
        final IResource resource = mock(IResource.class);
        when(resource.getLocationURI()).thenReturn(new URI("file", null, "/path", null));

        final IMarker marker = mock(IMarker.class);
        when(marker.getResource()).thenReturn(resource);

        assertThat(SuiteFileProblem.DEPRECATED_SUITE_FILE_EXTENSION.getProblemCategory())
                .isEqualTo(ProblemCategory.DEPRECATED_API);
        assertThat(SuiteFileProblem.DEPRECATED_SUITE_FILE_EXTENSION.hasResolution()).isTrue();
        assertThat(SuiteFileProblem.DEPRECATED_SUITE_FILE_EXTENSION.createFixers(marker)).hasSize(1)
                .allMatch(fixer -> fixer instanceof ConvertToRobotFileFormat);
    }

    @Test
    public void unsupportedSuiteExtensionProblemForRf32_hasRemovedApiCategoryAndHasResolution()
            throws URISyntaxException {
        final IResource resource = mock(IResource.class);
        when(resource.getLocationURI()).thenReturn(new URI("file", null, "/path", null));

        final IMarker marker = mock(IMarker.class);
        when(marker.getResource()).thenReturn(resource);

        assertThat(SuiteFileProblem.REMOVED_SUITE_FILE_EXTENSION.getProblemCategory())
                .isEqualTo(ProblemCategory.REMOVED_API);
        assertThat(SuiteFileProblem.REMOVED_SUITE_FILE_EXTENSION.hasResolution()).isTrue();
        assertThat(SuiteFileProblem.REMOVED_SUITE_FILE_EXTENSION.createFixers(marker)).hasSize(1)
                .allMatch(fixer -> fixer instanceof ConvertToRobotFileFormat);
    }
}
