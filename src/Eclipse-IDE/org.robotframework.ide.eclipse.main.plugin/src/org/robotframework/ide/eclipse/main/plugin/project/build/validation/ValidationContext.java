/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordNameSplitter;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;

public class ValidationContext {

    private final SuiteExecutor executorInUse;

    private final RobotVersion version;

    private final Map<String, List<KeywordValidationContext>> accessibleKeywords;

    private final Map<String, LibrarySpecification> librarySpecifications;

    private final Map<ReferencedLibrary, LibrarySpecification> referencedLibrarySpecifications;

    public static ValidationContext createFor(final IFile file) {
        final RobotProject project = RedPlugin.getModelManager().getModel().createRobotProject(file.getProject());
        final RobotRuntimeEnvironment runtimeEnvironment = project.getRuntimeEnvironment();
        
        final RobotVersion version = runtimeEnvironment != null ? RobotVersion.from(project.getVersion()) : null;
        return new ValidationContext(version, runtimeEnvironment.getInterpreter());
    }

    @VisibleForTesting
    public ValidationContext(final RobotVersion version, final SuiteExecutor executorInUse) {
        this.executorInUse = executorInUse;
        this.version = version;
        this.accessibleKeywords = newHashMap();
        this.librarySpecifications = newHashMap();
        this.referencedLibrarySpecifications = newHashMap();
    }

    public SuiteExecutor getExecutorInUse() {
        return executorInUse;
    }

    public RobotVersion getVersion() {
        return version;
    }

    public void setAccessibleKeywords(final Map<String, List<KeywordValidationContext>> keywords) {
        accessibleKeywords.putAll(keywords);
    }

    public Set<String> getAccessibleKeywords() {
        return ImmutableSet.copyOf(accessibleKeywords.keySet());
    }
    
    public KeywordValidationContext findKeywordValidationContext(final String name) {
        return extractKeywordValidationContext(KeywordNameSplitter.splitKeywordName(name));
    }

    public boolean isKeywordAccessible(final KeywordValidationContext keywordValidationContext) {
        return keywordValidationContext != null;
    }

    public boolean isKeywordDeprecated(final KeywordValidationContext keywordValidationContext) {
        return keywordValidationContext != null && keywordValidationContext.isDeprecated();
    }

    public boolean isKeywordFromNestedLibrary(final KeywordValidationContext keywordValidationContext) {
        return keywordValidationContext != null && keywordValidationContext.isFromNestedLibrary();
    }
    
    public void setLibrarySpecifications(final Map<String, LibrarySpecification> specs) {
        librarySpecifications.putAll(specs);
    }

    public Map<String, LibrarySpecification> getLibrarySpecificationsAsMap() {
        return librarySpecifications;
    }

    public void setReferencedLibrarySpecifications(final Map<ReferencedLibrary, LibrarySpecification> mapping) {
        referencedLibrarySpecifications.putAll(mapping);
    }

    public Map<ReferencedLibrary, LibrarySpecification> getReferencedLibrarySpecifications() {
        return referencedLibrarySpecifications;
    }
    
    private KeywordValidationContext extractKeywordValidationContext(final KeywordNameSplitter typedKeywordNameSplitter) {
        final List<KeywordValidationContext> keywordsContextList = accessibleKeywords.get(typedKeywordNameSplitter.getKeywordName()
                .toLowerCase());
        if (keywordsContextList != null) {
            final String typedKeywordSourceName = typedKeywordNameSplitter.getKeywordSource();
            for (final KeywordValidationContext keywordValidationContext : keywordsContextList) {
                if (hasEqualSources(typedKeywordSourceName, keywordValidationContext)) {
                    return keywordValidationContext;
                }
            }
        }
        return null;
    }
    
    private boolean hasEqualSources(final String typedKeywordSourceName,
            final KeywordValidationContext keywordValidationContext) {
        if (!typedKeywordSourceName.isEmpty()) {
            return !keywordValidationContext.getAlias().isEmpty() ? typedKeywordSourceName.equalsIgnoreCase(keywordValidationContext.getAlias())
                    : typedKeywordSourceName.equalsIgnoreCase(keywordValidationContext.getSourceName());
        }
        return true;
    }

    public static class KeywordValidationContext {

        private final String keywordName;

        private final String sourceName;

        private final String alias;

        private final boolean isDeprecated;

        private final boolean isFromNestedLibrary;

        public KeywordValidationContext(final String keywordName, final String sourceName, final String alias,
                final boolean isDeprecated, final boolean isFromNestedLibrary) {
            this.keywordName = keywordName;
            this.sourceName = sourceName;
            this.alias = alias;
            this.isDeprecated = isDeprecated;
            this.isFromNestedLibrary = isFromNestedLibrary;
        }

        public String getKeywordName() {
            return keywordName;
        }

        public String getSourceName() {
            return sourceName;
        }

        public boolean isDeprecated() {
            return isDeprecated;
        }

        public boolean isFromNestedLibrary() {
            return isFromNestedLibrary;
        }

        public String getAlias() {
            return alias;
        }
    }
}
