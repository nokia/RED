/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.ValidationContext.KeywordValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.io.Files;

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
        validationContext.setReferencedLibrarySpecifications(fileModel.getProject().getReferencedLibraries());
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
        final Map<String, LibrarySpecification> libs = newLinkedHashMap();
        libs.putAll(robotProject.getStandardLibraries());
        for (final Entry<ReferencedLibrary, LibrarySpecification> entry : robotProject.getReferencedLibraries()
                .entrySet()) {
            libs.put(entry.getKey().getName(), entry.getValue());
        }
        return libs;
    }

    private static Map<String, List<KeywordValidationContext>> collectAccessibleKeywordNames(final RobotSuiteFile robotSuiteFile) {
        final Map<String, List<KeywordValidationContext>> accessibleKeywords = newHashMap();
        new KeywordDefinitionLocator(robotSuiteFile, false).locateKeywordDefinition(new KeywordDetector() {

            @Override
            public ContinueDecision libraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final String libraryAlias, final boolean isFromNestedLibrary) {
                
                final KeywordValidationContext keywordValidationContext = new KeywordValidationContext(kwSpec.getName()
                        .toLowerCase(), libSpec.getName(), libraryAlias, kwSpec.isDeprecated(), isFromNestedLibrary);
                addAccessibleKeyword(kwSpec.getName().toLowerCase(), keywordValidationContext);
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision keywordDetected(final RobotSuiteFile file, final RobotKeywordDefinition keyword) {
                
                final KeywordValidationContext keywordValidationContext = new KeywordValidationContext(
                        keyword.getName().toLowerCase(), extractResourceFileName(file), "", keyword.isDeprecated(),
                        false);
                addAccessibleKeyword(keyword.getName().toLowerCase(), keywordValidationContext);
                return ContinueDecision.CONTINUE;
            }
            
            private void addAccessibleKeyword(final String keywordName, final KeywordValidationContext keywordValidationContext) {
                if(accessibleKeywords.containsKey(keywordName)) {
                    accessibleKeywords.get(keywordName).add(keywordValidationContext);
                } else {
                    accessibleKeywords.put(keywordName, newArrayList(keywordValidationContext));
                }
            }

            private String extractResourceFileName(final RobotSuiteFile file) {
                String keywordSourceFileName = "";
                if (file.isResourceFile()) {
                    keywordSourceFileName = Files.getNameWithoutExtension(file.getName());
                }
                return keywordSourceFileName;
            }
        });
        return accessibleKeywords;
    }
    
    
}