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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordNameSplitter;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

public class FileValidationContext {

    private final ValidationContext context;

    private final IFile file;

    private Map<String, List<KeywordValidationContext>> accessibleKeywords;

    private Set<String> accessibleVariables;

    public FileValidationContext(final ValidationContext context, final IFile file) {
        this.context = context;
        this.file = file;
    }

    public RobotVersion getVersion() {
        return context.getVersion();
    }

    public LibrarySpecification getLibrarySpecifications(final String libName) {
        return context.getLibrarySpecification(libName);
    }

    public Map<ReferencedLibrary, LibrarySpecification> getReferencedLibrarySpecifications() {
        return context.getReferencedLibrarySpecifications();
    }

    private Map<String, List<KeywordValidationContext>> getAccessibleKeywords() {
        if (accessibleKeywords == null) {
            accessibleKeywords = context.collectAccessibleKeywordNames(file);
        }
        return accessibleKeywords;
    }

    public Set<String> getAccessibleVariables() {
        if (accessibleVariables == null) {
            accessibleVariables = context.collectAccessibleVariables(file);
        }
        return accessibleVariables;
    }

    public boolean isKeywordAccessible(final String keywordName) {
        return findKeywordValidationContext(keywordName) != null;
    }

    public boolean isKeywordDeprecated(final String keywordName) {
        final KeywordValidationContext keywordValidationContext = findKeywordValidationContext(keywordName);
        return keywordValidationContext != null && keywordValidationContext.isDeprecated();
    }

    public boolean isKeywordFromNestedLibrary(final String keywordName) {
        final KeywordValidationContext keywordValidationContext = findKeywordValidationContext(keywordName);
        return keywordValidationContext != null && keywordValidationContext.isFromNestedLibrary();
    }

    private KeywordValidationContext findKeywordValidationContext(final String name) {
        return extractKeywordValidationContext(KeywordNameSplitter.splitKeywordName(name));
    }
    
    private KeywordValidationContext extractKeywordValidationContext(final KeywordNameSplitter typedKeywordNameSplitter) {
        final List<KeywordValidationContext> keywordsContextList = getAccessibleKeywords()
                .get(typedKeywordNameSplitter.getKeywordName().toLowerCase());
        final String typedKeywordSourceName = typedKeywordNameSplitter.getKeywordSource();
        if (keywordsContextList != null) {
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

    static class KeywordValidationContext {

        private final String keywordName;

        private final String sourceName;

        private final String alias;

        private final boolean isDeprecated;

        private final boolean isFromNestedLibrary;

        KeywordValidationContext(final String keywordName, final String sourceName, final String alias,
                final boolean isDeprecated, final boolean isFromNestedLibrary) {
            this.keywordName = keywordName;
            this.sourceName = sourceName;
            this.alias = alias;
            this.isDeprecated = isDeprecated;
            this.isFromNestedLibrary = isFromNestedLibrary;
        }

        String getKeywordName() {
            return keywordName;
        }

        String getSourceName() {
            return sourceName;
        }

        boolean isDeprecated() {
            return isDeprecated;
        }

        boolean isFromNestedLibrary() {
            return isFromNestedLibrary;
        }

        String getAlias() {
            return alias;
        }
    }
}
