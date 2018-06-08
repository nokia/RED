/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;

import com.google.common.collect.Range;

/**
 * @author Michal Anglart
 */
abstract class KeywordCallArgumentsValidator implements ModelUnitValidator {

    protected final IFile file;

    protected final RobotToken definingToken;

    protected final ValidationReportingStrategy reporter;

    protected final ArgumentsDescriptor descriptor;

    protected final List<RobotToken> arguments;

    KeywordCallArgumentsValidator(final IFile file, final RobotToken definingToken,
            final ValidationReportingStrategy reporter, final ArgumentsDescriptor descriptor,
            final List<RobotToken> arguments) {
        this.file = file;
        this.definingToken = definingToken;
        this.reporter = reporter;
        this.descriptor = descriptor;
        this.arguments = arguments;
    }

    @Override
    public abstract void validate(IProgressMonitor monitor);

    protected static String getRangesInfo(final Range<Integer> range, final String item) {
        final int minArgs = range.lowerEndpoint();
        if (!range.hasUpperBound()) {
            return "at least " + minArgs + toPluralIfNeeded(" " + item, minArgs);
        } else if (range.lowerEndpoint().equals(range.upperEndpoint())) {
            return minArgs + toPluralIfNeeded(" " + item, minArgs);
        } else {
            final int maxArgs = range.upperEndpoint();
            return "from " + minArgs + " to " + maxArgs + toPluralIfNeeded(" " + item, maxArgs);
        }
    }

    protected static String toBeInProperForm(final int amount) {
        return amount == 1 ? "is" : "are";
    }

    protected static String toPluralIfNeeded(final String noun, final int amount) {
        if (noun.isEmpty() || noun.equals(" ")) {
            return "";
        }
        return amount == 1 ? noun : noun + "s";
    }

}
