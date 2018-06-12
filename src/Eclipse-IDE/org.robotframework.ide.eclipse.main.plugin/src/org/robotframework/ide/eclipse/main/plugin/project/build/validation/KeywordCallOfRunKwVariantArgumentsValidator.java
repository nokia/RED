/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.ArgumentsDescriptor.Argument;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;

import com.google.common.collect.Streams;

class KeywordCallOfRunKwVariantArgumentsValidator extends KeywordCallArgumentsValidator {

    KeywordCallOfRunKwVariantArgumentsValidator(final IFile file, final RobotToken definingToken,
            final ValidationReportingStrategy reporter, final ArgumentsDescriptor argumentsDescriptor,
            final List<RobotToken> arguments) {
        super(file, definingToken, reporter, argumentsDescriptor, arguments);
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        // descriptor is not validated, since currently run keyword variants comes only from
        // libraries and it's not possible to have libspec generated for libraries having methods
        // with invalid descriptors

        final Map<String, Argument> argsByNames = Streams.stream(descriptor)
                .filter(arg -> arg.isRequired() || arg.isDefault())
                .collect(toMap(Argument::getName, arg -> arg));
        final TaggedCallSiteArguments taggedArguments = TaggedCallSiteArguments.tagArguments(arguments, argsByNames,
                descriptor.supportsKwargs());

        try {
            validateNumberOfArgs(taggedArguments);
        } catch (final ArgumentsProblemFoundException e) {
            // nothing to do just breaks one of validation step
        }
    }
}
