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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.FileValidationContext.KeywordValidationContext.Scope;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class FileValidationContext {

    private final ValidationContext context;

    private final IFile file;

    private Map<String, Collection<KeywordValidationContext>> accessibleKeywords;

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

    private Map<String, Collection<KeywordValidationContext>> getAccessibleKeywords() {
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
        final List<KeywordValidationContext> keywords = findMatchingKeywordValidationContexts(keywordName);
        final Multimap<Scope, KeywordValidationContext> scopedKeywords = LinkedHashMultimap.create();
        
        for (final KeywordValidationContext contextKw : keywords) {
            scopedKeywords.put(contextKw.scope, contextKw);
        }
        
        for (final Scope scope : newArrayList(Scope.LOCAL, Scope.RESOURCE, Scope.REF_LIBRARY, Scope.STD_LIBRARY)) {
            final Collection<KeywordValidationContext> contextKws = scopedKeywords.get(scope);
            if (!contextKws.isEmpty()) {
                return newArrayList(transform(contextKws,
                        new Function<KeywordValidationContext, String>() {
                            @Override
                            public String apply(final KeywordValidationContext context) {
                                return context.getSourceNameInUse();
                            }
                        }));
            }
        }
        return new ArrayList<>();
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
        final Collection<KeywordValidationContext> keywordsContexts = getPossibleContexts(name);

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
        final Collection<KeywordValidationContext> keywordsContexts = getPossibleContexts(name);

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

    private Collection<KeywordValidationContext> getPossibleContexts(final String name) {
        final QualifiedKeywordName qualifedName = QualifiedKeywordName.from(name);
        final Collection<KeywordValidationContext> keywordsContexts = getAccessibleKeywords()
                .get(qualifedName.getKeywordName().toLowerCase());
        return keywordsContexts == null ? tryWithEmbeddedArguments(qualifedName.getKeywordName()) : keywordsContexts;
    }
    
    private Collection<KeywordValidationContext> tryWithEmbeddedArguments(final String keywordName) {
        for (final Entry<String, Collection<KeywordValidationContext>> entry : getAccessibleKeywords().entrySet()) {
            if (EmbeddedKeywordNamesSupport.matches(entry.getKey(), keywordName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    static final class KeywordValidationContext {

        private final Scope scope;

        private final String sourceName;

        private final String alias;

        private final boolean isDeprecated;

        private final boolean isFromNestedLibrary;

        KeywordValidationContext(final Scope scope, final String sourceName, final String alias,
                final boolean isDeprecated, final boolean isFromNestedLibrary) {
            this.scope = scope;
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

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass() == KeywordValidationContext.class) {
                final KeywordValidationContext that = (KeywordValidationContext) obj;
                return Objects.equal(this.alias, that.alias) && Objects.equal(this.sourceName, that.sourceName)
                        && this.scope == that.scope && this.isDeprecated == that.isDeprecated
                        && this.isFromNestedLibrary == that.isFromNestedLibrary;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(scope, sourceName, alias, isDeprecated, isFromNestedLibrary);
        }

        enum Scope {
            LOCAL,
            RESOURCE,
            REF_LIBRARY,
            STD_LIBRARY
        }
    }
}
