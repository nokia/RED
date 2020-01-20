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
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;

import com.google.common.collect.ImmutableMap;

public class TasksProblemTest {

    @Test
    public void unknownTaskSpaceSensitiveSetting_hasResolutionAndProvidesFixer() {
        final ImmutableMap<String, String> mapping = ImmutableMap.<String, String> builder()
                .put("[Documenta tion]", "[Documentation]")
                .put("[ S e tup]", "[Setup]")
                .put("[T a g s]", "[Tags]")
                .put("[ Tea r d own ]", "[Teardown]")
                .put("[Tem p late]", "[Template]")
                .put("[T imeou t]", "[Timeout]")
                .build();
        mapping.forEach((name, replacement) -> {
            final IMarker marker = mock(IMarker.class);
            when(marker.getAttribute(AdditionalMarkerAttributes.NAME, "")).thenReturn(name);

            final TasksProblem problem = TasksProblem.UNKNOWN_TASK_SETTING;

            assertThat(problem.hasResolution()).isTrue();
            final List<? extends IMarkerResolution> fixers = problem.createFixers(marker);
            assertThat(fixers).extracting(IMarkerResolution::getLabel)
                    .containsExactly("Change to '" + replacement + "'");
        });
    }
}
