/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.rf.ide.core.libraries.ArgumentsDescriptor.Argument;
import org.rf.ide.core.testdata.model.table.exec.descs.CallArgumentsBinder.ArgumentsProblemFoundException;
import org.rf.ide.core.testdata.model.table.exec.descs.CallArgumentsBinder.RobotTokenAsArgExtractor;
import org.rf.ide.core.testdata.model.table.exec.descs.CallArgumentsBinder.TaggedCallSiteArguments;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.project.build.ValidationReportingStrategy;

class KeywordCallArgumentsOfRunKwVariantValidator extends KeywordCallArgumentsValidator {

    KeywordCallArgumentsOfRunKwVariantValidator(final FileValidationContext validationContext,
            final RobotToken definingToken, final ValidationReportingStrategy reporter,
            final ArgumentsDescriptor argumentsDescriptor, final List<RobotToken> arguments) {
        super(validationContext, definingToken, reporter, argumentsDescriptor, arguments);
    }

    @Override
    public void validate(final IProgressMonitor monitor) {
        // descriptor is not validated, since currently run keyword variants comes only from
        // libraries and it's not possible to have libspec generated for libraries having methods
        // with invalid descriptors

        final Map<String, Argument> argsByNames = groupDescriptorArgumentsByNames();
        final TaggedCallSiteArguments<RobotToken> taggedArguments = tagArguments(new RobotTokenAsArgExtractor(),
                argsByNames);

        try {
            validateNumberOfArgs(taggedArguments);
        } catch (final ArgumentsProblemFoundException e) {
            // nothing to do just breaks one of validation step
        }
    }
}
