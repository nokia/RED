/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Sets.newHashSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidator;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

public abstract class RobotFileValidator implements ModelUnitValidator {

    protected final ValidationContext validationContext;

    protected final IFile file;

    protected final ProblemsReportingStrategy reporter;

    public RobotFileValidator(final ValidationContext context, final IFile file) {
        this.validationContext = context;
        this.file = file;
        this.reporter = new ProblemsReportingStrategy();
    }

    @Override
    public final void validate(final IProgressMonitor monitor) throws CoreException {
        validate(new RobotSuiteFile(null, file), monitor);
    }

    /**
     * This method does common validation for different file types (resources, inits, suites).
     * It should be overridden and called by subclasses
     * 
     * @param fileModel
     * @param monitor
     * @throws CoreException
     */
    public void validate(final RobotSuiteFile fileModel, final IProgressMonitor monitor) throws CoreException {
        // TODO : check output status and parsing messages

        validationContext.setLibrarySpecifications(collectLibraries(fileModel));
        validationContext.setReferencedLibrarySpecifications(collectReferencedLibraries(fileModel));
        validationContext.setAccessibleKeywords(collectAccessibleKeywordNames(fileModel));

        new UnknownTablesValidator(fileModel).validate(monitor);
        new TestCasesTableValidator(validationContext, fileModel.findSection(RobotCasesSection.class))
                .validate(monitor);
        new GeneralSettingsTableValidator(validationContext, fileModel.findSection(RobotSettingsSection.class))
                .validate(monitor);
        new KeywordTableValidator(validationContext, fileModel.findSection(RobotKeywordsSection.class))
                .validate(monitor);
        new VariablesTableValidator(validationContext, fileModel.findSection(RobotVariablesSection.class))
                .validate(monitor);
    }

    private static Map<String, LibrarySpecification> collectLibraries(final RobotSuiteFile robotSuiteFile) {
        final RobotProject robotProject = robotSuiteFile.getProject();
        final Set<LibrarySpecification> libs = newHashSet();
        libs.addAll(robotProject.getStandardLibraries());
        libs.addAll(robotProject.getReferencedLibraries());
        return robotSuiteFile.getProject().getLibrariesMapping();
    }

    private static Map<ReferencedLibrary, LibrarySpecification> collectReferencedLibraries(
            final RobotSuiteFile robotSuiteFile) {
        return robotSuiteFile.getProject().getReferencedLibrariesMapping();
    }

    private static Set<String> collectAccessibleKeywordNames(final RobotSuiteFile robotSuiteFile) {
        final Set<String> names = new HashSet<>();
        new KeywordDefinitionLocator(robotSuiteFile, false).locateKeywordDefinition(new KeywordDetector() {

            @Override
            public ContinueDecision libraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec) {
                names.add(kwSpec.getName().toLowerCase());
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision keywordDetected(final RobotSuiteFile file, final RobotKeywordDefinition keyword) {
                names.add(keyword.getName().toLowerCase());
                return ContinueDecision.CONTINUE;
            }
        });
        return names;
    }
}