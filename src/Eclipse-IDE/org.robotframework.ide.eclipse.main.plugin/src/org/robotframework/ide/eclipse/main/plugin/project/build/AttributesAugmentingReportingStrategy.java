/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.validation.ProblemPosition;

public class AttributesAugmentingReportingStrategy extends ProblemsReportingStrategy {

    public static ProblemsReportingStrategy create(final ProblemsReportingStrategy wrappedStrategy,
            final Map<String, Object> additionalMarkerAttributes) {
        return new AttributesAugmentingReportingStrategy(wrappedStrategy, additionalMarkerAttributes);
    }

    private final ProblemsReportingStrategy wrappedStrategy;

    private final Map<String, Object> additionalMarkerAttributes;

    private AttributesAugmentingReportingStrategy(final ProblemsReportingStrategy wrappedStrategy,
            final Map<String, Object> additionalMarkerAttributes) {
        super(wrappedStrategy.shouldPanic);
        this.wrappedStrategy = wrappedStrategy;
        this.additionalMarkerAttributes = additionalMarkerAttributes;
    }

    @Override
    public void handleProblem(final RobotProblem problem, final IFile file, final ProblemPosition filePosition,
            final Map<String, Object> additionalAttributes) {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.putAll(additionalAttributes);
        attributes.putAll(additionalMarkerAttributes);
        wrappedStrategy.handleProblem(problem, file, filePosition, attributes);
    }
}
