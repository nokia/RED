/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
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
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Files;

/**
 * @author Michal Anglart
 */
public class ValidationContext {

    private final RobotModel model;

    private final SuiteExecutor executorInUse;

    private final RobotVersion version;

    private RobotProjectConfig projectConfig;

    private final Map<String, LibrarySpecification> accessibleLibraries;

    private final Map<ReferencedLibrary, LibrarySpecification> referencedAccessibleLibraries;

    private BuildLogger logger;

    private boolean isValidatingChangedFiles;

    public ValidationContext(final RobotProject robotProject, final BuildLogger logger) {
        this.model = (RobotModel) robotProject.getParent();
        this.logger = logger;
        this.projectConfig = robotProject.getRobotProjectConfig();
        final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();

        final String versionGot = robotProject.getVersion();
        this.version = (runtimeEnvironment != null && versionGot != null) ? RobotVersion.from(versionGot) : null;
        this.executorInUse = runtimeEnvironment != null ? runtimeEnvironment.getInterpreter() : null;

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

    private static Map<String, LibrarySpecification> collectLibraries(final RobotProject robotProject) {
        final Map<String, LibrarySpecification> libs = newLinkedHashMap();
        libs.putAll(robotProject.getStandardLibraries());
        for (final Entry<ReferencedLibrary, LibrarySpecification> entry : robotProject.getReferencedLibraries()
                .entrySet()) {
            if (entry.getKey().provideType() == LibraryType.VIRTUAL && entry.getValue() != null) {
                libs.put(entry.getValue().getName(), entry.getValue());
            } else {
                libs.put(entry.getKey().getName(), entry.getValue());
            }
        }
        return libs;
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

    public LibrarySpecification getLibrarySpecification(final String libName) {
        return accessibleLibraries.get(libName);
    }

    public Map<String, LibrarySpecification> getAccessibleLibraries() {
        return accessibleLibraries;
    }

    public Map<ReferencedLibrary, LibrarySpecification> getReferencedLibrarySpecifications() {
        return referencedAccessibleLibraries;
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
        final Set<String> variables = newHashSet();
        new VariableDefinitionLocator(file, model).locateVariableDefinition(new VariableDetector() {

            @Override
            public ContinueDecision variableDetected(final RobotVariable variable) {
                final String name = variable.getPrefix()
                        + VariableNamesSupport.extractUnifiedVariableName(variable.getName()) + variable.getSuffix();
                variables.add(name.toLowerCase());
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
                variables.add(VariableNamesSupport.extractUnifiedVariableName(name));
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision varFileVariableDetected(final ReferencedVariableFile file, final String name,
                    final Object value) {
                variables.add(VariableNamesSupport.extractUnifiedVariableName(name));
                return ContinueDecision.CONTINUE;
            }
        });
        return variables;
    }

    public Map<String, Collection<KeywordEntity>> collectAccessibleKeywords(final IFile file) {
        final Map<String, Collection<KeywordEntity>> accessibleKeywords = newHashMap();
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

                final KeywordScope scope = libSpec.isReferenced() ? KeywordScope.REF_LIBRARY : KeywordScope.STD_LIBRARY;
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
