/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.environment.SuiteExecutor;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator.VariableDetector;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.ValidationKeywordEntity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.io.Files;

/**
 * @author Michal Anglart
 */
public class ValidationContext {

    private final RobotModel model;

    private final SuiteExecutor executorInUse;

    private final RobotVersion version;

    private final RobotProjectConfig projectConfig;

    private final ListMultimap<String, LibrarySpecification> accessibleLibraries;

    private BuildLogger logger;

    private boolean isValidatingChangedFiles;

    public ValidationContext(final RobotProject robotProject, final BuildLogger logger) {
        this.model = (RobotModel) robotProject.getParent();
        this.logger = logger;
        this.projectConfig = robotProject.getRobotProjectConfig();
        this.version = robotProject.getRobotParserComplianceVersion();
        this.executorInUse = robotProject.getRuntimeEnvironment().getInterpreter();
        this.accessibleLibraries = collectLibraries(robotProject);
    }

    @VisibleForTesting
    public ValidationContext(final RobotProjectConfig config, final RobotModel model, final RobotVersion version,
            final SuiteExecutor executor, final ListMultimap<String, LibrarySpecification> libs) {
        this.projectConfig = config;
        this.model = model;
        this.version = version;
        this.executorInUse = executor;
        this.accessibleLibraries = libs;
    }

    private static ListMultimap<String, LibrarySpecification> collectLibraries(final RobotProject robotProject) {
        return Multimaps.index(robotProject.getLibrarySpecifications(), LibrarySpecification::getName);
    }

    BuildLogger getLogger() {
        return logger;
    }

    @VisibleForTesting
    public RobotModel getModel() {
        return model;
    }

    public RobotProjectConfig getProjectConfiguration() {
        return projectConfig;
    }

    public SuiteExecutor getExecutorInUse() {
        return executorInUse;
    }

    public RobotVersion getVersion() {
        return version;
    }

    public ListMultimap<String, LibrarySpecification> getSpecifications() {
        return accessibleLibraries;
    }

    public boolean isValidatingChangedFiles() {
        return isValidatingChangedFiles;
    }

    public void setIsValidatingChangedFiles(final boolean isValidatingChangedFiles) {
        this.isValidatingChangedFiles = isValidatingChangedFiles;
    }

    public FileValidationContext createUnitContext(final IFile file) {
        return new FileValidationContext(this, file);
    }

    public Set<String> collectAccessibleVariables(final IFile file) {
        final Set<String> variables = new HashSet<>();
        new VariableDefinitionLocator(file, model).locateVariableDefinition(new VariableDetector() {

            @Override
            public ContinueDecision variableDetected(final RobotVariable variable) {
                variables.add(VariablesAnalyzer.normalizeName(variable.getActualName()));
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision localVariableDetected(final RobotFileInternalElement element,
                    final RobotToken variable) {
                // local variables will be added to context during validation
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision globalVariableDetected(final String name, final Object value) {
                variables.add(VariablesAnalyzer.normalizeName(name));
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision varFileVariableDetected(final ReferencedVariableFile file, final String name,
                    final Object value) {
                variables.add(VariablesAnalyzer.normalizeName(name));
                return ContinueDecision.CONTINUE;
            }
        });
        return variables;
    }

    public Map<String, Collection<KeywordEntity>> collectAccessibleKeywords(final IFile file) {
        final Map<String, Collection<KeywordEntity>> accessibleKeywords = new HashMap<>();
        new KeywordDefinitionLocator(file, model).locateKeywordDefinition(new KeywordDetector() {

            @Override
            public ContinueDecision nonAccessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final RobotSuiteFile exposingFile) {
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision accessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final Collection<Optional<String>> libraryAliases,
                    final RobotSuiteFile exposingFile) {

                final KeywordScope scope = libSpec.getDescriptor().getKeywordsScope();
                for (final Optional<String> libraryAlias : libraryAliases) {
                    final ValidationKeywordEntity keyword = new ValidationKeywordEntity(scope, libSpec.getName(),
                            kwSpec.getName(), libraryAlias, kwSpec.isDeprecated(), exposingFile.getFile().getFullPath(),
                            0, kwSpec.createArgumentsDescriptor());

                    addAccessibleKeyword(kwSpec.getName(), keyword);
                }
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision keywordDetected(final RobotSuiteFile suiteFile,
                    final RobotKeywordDefinition kwDefinition) {
                final ValidationKeywordEntity keyword = new ValidationKeywordEntity(null,
                        Files.getNameWithoutExtension(suiteFile.getName()), kwDefinition.getName(), Optional.empty(),
                        kwDefinition.isDeprecated(), suiteFile.getFile().getFullPath(),
                        kwDefinition.getDefinitionPosition().getOffset(), kwDefinition.createArgumentsDescriptor());

                addAccessibleKeyword(kwDefinition.getName(), keyword);
                return ContinueDecision.CONTINUE;
            }

            private void addAccessibleKeyword(final String keywordName, final ValidationKeywordEntity keyword) {
                final String unifiedName = QualifiedKeywordName.unifyDefinition(keywordName);
                if (!accessibleKeywords.containsKey(unifiedName)) {
                    accessibleKeywords.put(unifiedName, new LinkedHashSet<>());
                }
                accessibleKeywords.get(unifiedName).add(keyword);
            }
        });
        return accessibleKeywords;
    }
}
