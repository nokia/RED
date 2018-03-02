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

public class AttributesAugmentingReportingStrategy extends ValidationReportingStrategy {

    public static ValidationReportingStrategy create(final ValidationReportingStrategy wrappedStrategy,
            final Map<String, Object> additionalMarkerAttributes) {
        return new AttributesAugmentingReportingStrategy(wrappedStrategy, additionalMarkerAttributes);
    }

    private final ValidationReportingStrategy wrappedStrategy;

    private final Map<String, Object> additionalMarkerAttributes;

    private AttributesAugmentingReportingStrategy(final ValidationReportingStrategy wrappedStrategy,
            final Map<String, Object> additionalMarkerAttributes) {
        super(wrappedStrategy.shouldPanic);
        this.wrappedStrategy = wrappedStrategy;
        this.additionalMarkerAttributes = additionalMarkerAttributes;
    }

    @Override
    public void handleTask(final RobotTask task, final IFile file) {
        wrappedStrategy.handleTask(task, file);
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
