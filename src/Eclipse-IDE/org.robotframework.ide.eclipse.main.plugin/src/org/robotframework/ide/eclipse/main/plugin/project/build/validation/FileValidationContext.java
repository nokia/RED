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

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Function;

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

    public List<String> getKeywordSourceNames(final String keywordName) {
        return newArrayList(transform(findMatchingKeywordValidationContexts(keywordName),
                new Function<KeywordValidationContext, String>() {

                    @Override
                    public String apply(final KeywordValidationContext context) {
                        return context.getSourceNameInUse();
                    }
                }));
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
        final List<KeywordValidationContext> keywordsContexts = getPossibleContexts(name);

        if (keywordsContexts != null) {
            final QualifiedKeywordName qualifedName = QualifiedKeywordName.from(name);
            for (final KeywordValidationContext context : keywordsContexts) {
                final QualifiedKeywordName candidateQualifiedName = QualifiedKeywordName
                        .create(qualifedName.getKeywordName(), context.getSourceNameInUse());
                if (qualifedName.matchesIgnoringCase(candidateQualifiedName)) {
                    return context;
                }
            }
        }
        return null;
    }

    private List<KeywordValidationContext> findMatchingKeywordValidationContexts(final String name) {
        final List<KeywordValidationContext> keywordsContexts = getPossibleContexts(name);

        final List<KeywordValidationContext> matchingContexts = new ArrayList<>();
        if (keywordsContexts != null) {
            final QualifiedKeywordName qualifedName = QualifiedKeywordName.from(name);
            for (final KeywordValidationContext context : keywordsContexts) {
                final QualifiedKeywordName candidateQualifiedName = QualifiedKeywordName
                        .create(qualifedName.getKeywordName(), context.getSourceNameInUse());
                if (qualifedName.matchesIgnoringCase(candidateQualifiedName)) {
                    matchingContexts.add(context);
                }
            }
        }
        return matchingContexts;
    }

    private List<KeywordValidationContext> getPossibleContexts(final String name) {
        final QualifiedKeywordName qualifedName = QualifiedKeywordName.from(name);
        final List<KeywordValidationContext> keywordsContexts = getAccessibleKeywords()
                .get(qualifedName.getKeywordName().toLowerCase());
        return keywordsContexts == null ? tryWithEmbeddedArguments(qualifedName.getKeywordName()) : keywordsContexts;
    }
    
    private List<KeywordValidationContext> tryWithEmbeddedArguments(final String keywordName) {
        for (final Entry<String, List<KeywordValidationContext>> entry : getAccessibleKeywords()
                .entrySet()) {
            if (EmbeddedKeywordNamesSupport.matches(entry.getKey(), keywordName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    static class KeywordValidationContext {

        private final String sourceName;

        private final String alias;

        private final boolean isDeprecated;

        private final boolean isFromNestedLibrary;

        KeywordValidationContext(final String sourceName, final String alias,
                final boolean isDeprecated, final boolean isFromNestedLibrary) {
            this.sourceName = sourceName;
            this.alias = alias;
            this.isDeprecated = isDeprecated;
            this.isFromNestedLibrary = isFromNestedLibrary;
        }

        String getSourceNameInUse() {
            return alias.isEmpty() ? sourceName : alias;
        }

        boolean isDeprecated() {
            return isDeprecated;
        }

        boolean isFromNestedLibrary() {
            return isFromNestedLibrary;
        }
    }
}
