/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.validation.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.IProblemCause;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

@ExtendWith(ProjectExtension.class)
public class AttributesAugmentingReportingStrategyTest {

    @Project(files = "file.txt")
    static IProject project;

    @Test
    public void additionalAttributesAreAppendedToCreatedMarker() throws CoreException {
        final Map<String, Object> additionalMarkerAttributes = ImmutableMap.of("abc", "def");
        final ValidationReportingStrategy augmentingReportingStrategy = AttributesAugmentingReportingStrategy
                .create(ValidationReportingStrategy.reportOnly(), additionalMarkerAttributes);

        final IFile file = getFile(project, "file.txt");

        final RobotProblem problem = RobotProblem.causedBy(new ProblemCause());

        final RobotToken token = RobotToken.create("ff");
        token.setLineNumber(2);
        token.setStartColumn(0);
        token.setStartOffset(42);
        final ProblemPosition filePosition = new ProblemPosition(7, Range.closed(1, 3));
        final Map<String, Object> additionalAttributes = new HashMap<>();

        augmentingReportingStrategy.handleProblem(problem, file, 0);
        augmentingReportingStrategy.handleProblem(problem, file, filePosition);
        augmentingReportingStrategy.handleProblem(problem, file, token);
        augmentingReportingStrategy.handleProblem(problem, file, 0, additionalAttributes);
        augmentingReportingStrategy.handleProblem(problem, file, filePosition, additionalAttributes);
        augmentingReportingStrategy.handleProblem(problem, file, token, additionalAttributes);

        final IMarker[] markers = file.findMarkers(RobotProblem.TYPE_ID, true, IResource.DEPTH_ONE);
        assertThat(markers).hasSize(6);
        for (final IMarker marker : markers) {
            assertThat(marker.getAttribute("abc")).isEqualTo("def");
        }
    }

    private static class ProblemCause implements IProblemCause {

        @Override
        public boolean hasResolution() {
            return false;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return new ArrayList<>();
        }

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.UNSUPPORTED_API;
        }

        @Override
        public String getProblemDescription() {
            return "desc";
        }

        @Override
        public String getEnumClassName() {
            return ProblemCause.class.getName();
        }
    }
}
