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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ListMultimap;

public class FileValidationContext extends AccessibleKeywordsEntities {

    private final ValidationContext context;

    private final IFile file;

    private Set<String> accessibleVariables;

    private final Map<String, ListMultimap<String, KeywordEntity>> alreadyUsedKeywords = new HashMap<>();

    private final Map<String, ListMultimap<KeywordScope, KeywordEntity>> possibleKeywords = new HashMap<>();

    private ListMultimap<KeywordScope, KeywordEntity> allPossibleKeywords;

    private Collection<KeywordEntity> hereKeywords;

    public FileValidationContext(final ValidationContext context, final IFile file) {
        this(context, file, new ValidationKeywordCollector(file, context), null);
    }

    @VisibleForTesting
    public FileValidationContext(final ValidationContext context, final IFile file,
            final AccessibleKeywordsCollector accessibleKeywordsCollector, final Set<String> accessibleVariables) {
        super(file.getFullPath(), accessibleKeywordsCollector);
        this.context = context;
        this.file = file;
        this.accessibleVariables = accessibleVariables;
    }

    public IFile getFile() {
        return file;
    }

    public RobotProjectConfig getProjectConfiguration() {
        return context.getProjectConfiguration();
    }

    public RobotVersion getVersion() {
        return context.getVersion();
    }

    LibrarySpecification getLibrarySpecifications(final String libName) {
        return context.getLibrarySpecification(libName);
    }

    public Map<String, LibrarySpecification> getAccessibleLibraries() {
        return context.getAccessibleLibraries();
    }

    Map<ReferencedLibrary, LibrarySpecification> getReferencedLibrarySpecifications() {
        return context.getReferencedLibrarySpecifications();
    }

    Set<String> getAccessibleVariables() {
        if (accessibleVariables == null) {
            accessibleVariables = context.collectAccessibleVariables(file);
        }
        return accessibleVariables;
    }

    public ListMultimap<String, KeywordEntity> findPossibleKeywords(final String keywordName) {
        ListMultimap<String, KeywordEntity> listMultimap = alreadyUsedKeywords
                .get(QualifiedKeywordName.unifyDefinition(keywordName));
        if (listMultimap == null) {
            listMultimap = super.findPossibleKeywords(keywordName, true);
            alreadyUsedKeywords.put(QualifiedKeywordName.unifyDefinition(keywordName), listMultimap);
        }

        return listMultimap;
    }

    @Override
    public ListMultimap<KeywordScope, KeywordEntity> getPossibleKeywords(
            final ListMultimap<String, KeywordEntity> foundKeywords, final String keywordName) {
        ListMultimap<KeywordScope, KeywordEntity> pos = possibleKeywords
                .get(QualifiedKeywordName.unifyDefinition(keywordName));
        if (pos == null) {
            pos = super.getPossibleKeywords(foundKeywords, keywordName);
            possibleKeywords.put(QualifiedKeywordName.unifyDefinition(keywordName), pos);
        }

        return pos;
    }

    @Override
    public ListMultimap<KeywordScope, KeywordEntity> getPossibleKeywords() {
        if (allPossibleKeywords == null) {
            allPossibleKeywords = super.getPossibleKeywords();
        }

        return allPossibleKeywords;
    }

    @Override
    protected Collection<KeywordEntity> getAccessibleKeywordsDeduplicated() {
        if (hereKeywords == null) {
            hereKeywords = super.getAccessibleKeywordsDeduplicated();
        }

        return hereKeywords;
    }

    public boolean isValidatingChangedFiles() {
        return context.isValidatingChangedFiles();
    }

    private static final class ValidationKeywordCollector implements AccessibleKeywordsCollector {

        private final IFile file;

        private final ValidationContext context;

        private ValidationKeywordCollector(final IFile file, final ValidationContext context) {
            this.file = file;
            this.context = context;
        }

        @Override
        public Map<String, Collection<KeywordEntity>> collect() {
            return context.collectAccessibleKeywords(file);
        }
    }

    public static final class ValidationKeywordEntity extends KeywordEntity {

        private final int position;

        @VisibleForTesting
        ValidationKeywordEntity(final KeywordScope scope, final String sourceName, final String keywordName,
                final String alias, final boolean isDeprecated, final IPath exposingFilepath, final int position,
                final ArgumentsDescriptor argumentsDescriptor) {
            super(scope, sourceName, keywordName, alias, isDeprecated, argumentsDescriptor, exposingFilepath);
            this.position = position;
        }

        public boolean hasInconsistentName(final String useplaceName) {
            return !QualifiedKeywordName.isOccurrenceEqualToDefinition(useplaceName, getNameFromDefinition());
        }

        boolean isFromNestedLibrary(final IFile useplaceFile) {
            final IPath path = useplaceFile.getFullPath();
            final KeywordScope scope = getScope(path);
            return (scope == KeywordScope.REF_LIBRARY || scope == KeywordScope.STD_LIBRARY)
                    && !path.equals(getExposingFilepath());
        }

        @Override
        public boolean isSameAs(final KeywordEntity other, final IPath useplaceFilepath) {
            return position == ((ValidationKeywordEntity) other).position && super.isSameAs(other, useplaceFilepath);
        }

        @Override
        public boolean equals(final Object obj) {
            return super.equals(obj) || position == ((ValidationKeywordEntity) obj).position;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), position);
        }
    }
}
