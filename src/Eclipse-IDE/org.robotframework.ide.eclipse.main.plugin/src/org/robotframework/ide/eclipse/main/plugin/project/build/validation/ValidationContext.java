/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator.VariableDetector;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.KeywordValidationContext;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Files;

/**
 * @author Michal Anglart
 *
 */
public class ValidationContext {

    private final RobotModel model;

    private final SuiteExecutor executorInUse;
    private final RobotVersion version;

    private final Map<String, LibrarySpecification> accessibleLibraries;
    private final Map<ReferencedLibrary, LibrarySpecification> referencedAccessibleLibraries;

    public ValidationContext(final IProject project) {
        this(RedPlugin.getModelManager().getModel(), project);
    }

    public ValidationContext(final RobotModel model, final IProject project) {
        this.model = model;
        final RobotProject robotProject = model.createRobotProject(project);
        final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();

        this.version = runtimeEnvironment != null ? RobotVersion.from(robotProject.getVersion()) : null;
        this.executorInUse = runtimeEnvironment.getInterpreter();

        this.accessibleLibraries = collectLibraries(robotProject);
        this.referencedAccessibleLibraries = newHashMap(robotProject.getReferencedLibraries());
    }

    @VisibleForTesting
    public ValidationContext(final RobotModel model, final RobotVersion version, final SuiteExecutor executor,
            final Map<String, LibrarySpecification> libs, final Map<ReferencedLibrary, LibrarySpecification> refLibs) {
        this.model = model;
        this.version = version;
        this.executorInUse = executor;
        this.accessibleLibraries = libs;
        this.referencedAccessibleLibraries = refLibs;
    }

    RobotModel getModel() {
        return model;
    }

    public SuiteExecutor getExecutorInUse() {
        return executorInUse;
    }

    public RobotVersion getVersion() {
        return version;
    }

    public LibrarySpecification getLibrarySpecification(final String libName) {
        return accessibleLibraries.get(libName);
    }

    public Map<ReferencedLibrary, LibrarySpecification> getReferencedLibrarySpecifications() {
        return referencedAccessibleLibraries;
    }

    public FileValidationContext createUnitContext(final IFile file) {
        return new FileValidationContext(this, file);
    }

    private static Map<String, LibrarySpecification> collectLibraries(final RobotProject robotProject) {
        final Map<String, LibrarySpecification> libs = newLinkedHashMap();
        libs.putAll(robotProject.getStandardLibraries());
        for (final Entry<ReferencedLibrary, LibrarySpecification> entry : robotProject.getReferencedLibraries()
                .entrySet()) {
            libs.put(entry.getKey().getName(), entry.getValue());
        }
        return libs;
    }

    public Set<String> collectAccessibleVariables(final IFile file) {
        final Set<String> variables = newHashSet();
        new VariableDefinitionLocator(file, model).locateVariableDefinition(new VariableDetector() {

            @Override
            public ContinueDecision variableDetected(final RobotSuiteFile file, final RobotVariable variable) {
                variables.add((variable.getPrefix() + variable.getName() + variable.getSuffix()).toLowerCase());
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision localVariableDetected(final RobotSuiteFile file, final RobotToken variable) {
                // local variables will be added to context during validation
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision globalVariableDetected(final String name, final Object value) {
                variables.add(name.toLowerCase());
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision varFileVariableDetected(final ReferencedVariableFile file, final String name,
                    final Object value) {
                variables.add(name.toLowerCase());
                return ContinueDecision.CONTINUE;
            }
        });
        return variables;
    }

    public Map<String, List<KeywordValidationContext>> collectAccessibleKeywordNames(final IFile file) {
        final Map<String, List<KeywordValidationContext>> accessibleKeywords = newHashMap();
        new KeywordDefinitionLocator(file, model).locateKeywordDefinition(new KeywordDetector() {

            @Override
            public ContinueDecision libraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final String libraryAlias, final boolean isFromNestedLibrary) {

                final KeywordValidationContext keywordValidationContext = new KeywordValidationContext(
                        kwSpec.getName().toLowerCase(), libSpec.getName(), libraryAlias, kwSpec.isDeprecated(),
                        isFromNestedLibrary);
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

            private void addAccessibleKeyword(final String keywordName,
                    final KeywordValidationContext keywordValidationContext) {
                if (accessibleKeywords.containsKey(keywordName)) {
                    accessibleKeywords.get(keywordName).add(keywordValidationContext);
                } else {
                    accessibleKeywords.put(keywordName, newArrayList(keywordValidationContext));
                }
            }

            private String extractResourceFileName(final RobotSuiteFile file) {
                return file.isResourceFile() ? Files.getNameWithoutExtension(file.getName()) : "";
            }
        });
        return accessibleKeywords;
    }

}
