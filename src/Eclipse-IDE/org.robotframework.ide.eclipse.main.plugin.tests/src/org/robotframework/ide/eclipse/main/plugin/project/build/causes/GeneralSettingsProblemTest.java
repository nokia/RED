/*
 * Copyright 2019 Nokia Solutions and Networks
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
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RemoveSettingValuesExceptFirstFixer;

import com.google.common.collect.ImmutableMap;

public class GeneralSettingsProblemTest {

    @Test
    public void invalidNumberOfSettingValuesHasFix() {
        final IMarker marker = mock(IMarker.class);
        final List<? extends IMarkerResolution> fixers = GeneralSettingsProblem.INVALID_NUMBER_OF_SETTING_VALUES
                .createFixers(marker);
        assertThat(fixers).hasSize(1);
        assertThat(fixers.get(0)).isInstanceOf(RemoveSettingValuesExceptFirstFixer.class);
    }

    @Test
    public void unknownGeneralDocumentSetting_hasResolutionAndProvidesFixer() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn("Document");

        final GeneralSettingsProblem problem = GeneralSettingsProblem.UNKNOWN_SETTING;

        assertThat(problem.hasResolution()).isTrue();
        final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
        assertThat(fixers).extracting(IMarkerResolution::getLabel).containsExactly("Change to 'Documentation'");
    }

    @Test
    public void unknownGeneralSuitePreconditionSetting_hasResolutionAndProvidesFixer() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn("Suite Precondition");

        final GeneralSettingsProblem problem = GeneralSettingsProblem.UNKNOWN_SETTING;

        assertThat(problem.hasResolution()).isTrue();
        final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
        assertThat(fixers).extracting(IMarkerResolution::getLabel).containsExactly("Change to 'Suite Setup'");
    }

    @Test
    public void unknownGeneralSuitePostconditionSetting_hasResolutionAndProvidesFixer() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn("Suite Postcondition");

        final GeneralSettingsProblem problem = GeneralSettingsProblem.UNKNOWN_SETTING;

        assertThat(problem.hasResolution()).isTrue();
        final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
        assertThat(fixers).extracting(IMarkerResolution::getLabel).containsExactly("Change to 'Suite Teardown'");
    }

    @Test
    public void unknownGeneralTestPreconditionSetting_hasResolutionAndProvidesFixer() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn("Test Precondition");

        final GeneralSettingsProblem problem = GeneralSettingsProblem.UNKNOWN_SETTING;

        assertThat(problem.hasResolution()).isTrue();
        final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
        assertThat(fixers).extracting(IMarkerResolution::getLabel).containsExactly("Change to 'Test Setup'");
    }

    @Test
    public void unknownGeneralTestPostconditionSetting_hasResolutionAndProvidesFixer() {
        final IMarker marker = mock(IMarker.class);
        when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn("Test Postcondition");

        final GeneralSettingsProblem problem = GeneralSettingsProblem.UNKNOWN_SETTING;

        assertThat(problem.hasResolution()).isTrue();
        final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
        assertThat(fixers).extracting(IMarkerResolution::getLabel).containsExactly("Change to 'Test Teardown'");
    }

    @Test
    public void unknownGeneralSpaceSensitiveSetting_hasResolutionAndProvidesFixer() {
        final ImmutableMap<String, String> mapping = ImmutableMap.<String, String> builder()
                .put("Docu mentation", "Documentation")
                .put("Me tad ata", "Metadata")
                .put("De fault Tags", "Default Tags")
                .put("Li brar y", "Library")
                .put("Res ou rce", "Resource")
                .put("Vari ables", "Variables")
                .put("S uiteSetu p", "Suite Setup")
                .put("Suite T e a rdown", "Suite Teardown")
                .put("Test Setu p", "Test Setup")
                .put("T est Teard own", "Test Teardown")
                .put("TestTe mpl ate", "Test Template")
                .put("Test Time o u t", "Test Timeout")
                .put("T a s k Setup", "Task Setup")
                .put("T ask Teard ow n", "Task Teardown")
                .put("Task Te m plate", "Task Template")
                .put("Ta skTime out", "Task Timeout")
                .build();
        mapping.forEach((name, replacement) -> {
            final IMarker marker = mock(IMarker.class);
            when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn(name);

            final GeneralSettingsProblem problem = GeneralSettingsProblem.UNKNOWN_SETTING;

            assertThat(problem.hasResolution()).isTrue();
            final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
            assertThat(fixers).extracting(IMarkerResolution::getLabel)
                    .containsExactly("Change to '" + replacement + "'");
        });
    }

}
