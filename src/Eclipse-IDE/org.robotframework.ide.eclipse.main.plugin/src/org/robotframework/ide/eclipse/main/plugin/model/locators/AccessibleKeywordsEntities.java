/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author Michal Anglart
 */
public class AccessibleKeywordsEntities {

    private final IPath filepath;

    private final AccessibleKeywordsCollector collector;

    private Map<String, Collection<KeywordEntity>> accessibleKeywords;

    public AccessibleKeywordsEntities(final IPath filepath, final AccessibleKeywordsCollector collector) {
        this.filepath = filepath;
        this.collector = collector;
    }

    public Map<String, Collection<KeywordEntity>> getAccessibleKeywords() {
        if (accessibleKeywords == null) {
            accessibleKeywords = collector.collect();
        }
        return accessibleKeywords;
    }

    protected IPath getFilepath() {
        return this.filepath;
    }

    public boolean isKeywordAccessible(final String keywordName) {
        return findKeyword(keywordName).isPresent();
    }

    private Optional<KeywordEntity> findKeyword(final String name) {
        final Collection<KeywordEntity> keywords = getFilteredPossibleEntities(name);
        if (keywords == null) {
            return Optional.absent();
        }

        final QualifiedKeywordName qualifedName = QualifiedKeywordName.fromOccurrence(name);
        for (final KeywordEntity keyword : keywords) {
            final QualifiedKeywordName candidateQualifiedName = QualifiedKeywordName
                    .create(qualifedName.getKeywordName(), keyword.getSourceNameInUse());
            if (qualifedName.matchesIgnoringCase(candidateQualifiedName)) {
                return Optional.of(keyword);
            }
        }
        if (name.contains(".")) {
            // try to find keyword name with dots, ignore keyword source
            final QualifiedKeywordName qualifedNameWithDots = QualifiedKeywordName.fromOccurrenceWithDots(name);
            for (final KeywordEntity keyword : keywords) {
                final QualifiedKeywordName candidateQualifiedName = QualifiedKeywordName.create(
                        qualifedNameWithDots.getKeywordName(), keyword.getSourceNameInUse());
                if (qualifedNameWithDots.matchesIgnoringCase(candidateQualifiedName)) {
                    return Optional.of(keyword);
                }
            }
        }
        return Optional.absent();
    }

    public ListMultimap<KeywordScope, KeywordEntity> getPossibleKeywords() {
        final Map<String, Collection<KeywordEntity>> allKeywords = getAccessibleKeywords();
        final ListMultimap<KeywordScope, KeywordEntity> scopedKeywords = ArrayListMultimap.create();

        for (final Collection<KeywordEntity> entities : allKeywords.values()) {
            for (final KeywordEntity entity : filterDuplicates(entities)) {
                scopedKeywords.put(entity.getScope(getFilepath()), entity);
            }
        }
        return scopedKeywords;
    }

    public ListMultimap<KeywordScope, KeywordEntity> getPossibleKeywords(final String keywordName) {
        final List<KeywordEntity> keywords = findMatchingKeywords(keywordName);
        final ListMultimap<KeywordScope, KeywordEntity> scopedKeywords = ArrayListMultimap.create();

        for (final KeywordEntity keyword : keywords) {
            scopedKeywords.put(keyword.getScope(getFilepath()), keyword);
        }
        return scopedKeywords;
    }

    private List<KeywordEntity> findMatchingKeywords(final String name) {
        final Collection<KeywordEntity> keywords = getFilteredPossibleEntities(name);
        if (keywords == null) {
            return new ArrayList<>();
        }

        final List<KeywordEntity> matchingKeywords = new ArrayList<>();
        final QualifiedKeywordName qualifedName = QualifiedKeywordName.fromOccurrence(name);
        for (final KeywordEntity keyword : keywords) {
            final QualifiedKeywordName candidateQualifiedName = QualifiedKeywordName
                    .create(keyword.getNameFromDefinition(), keyword.getSourceNameInUse());
            if (qualifedName.matchesIgnoringCase(candidateQualifiedName)) {
                matchingKeywords.add(keyword);
            }
        }
        return matchingKeywords;
    }

    private Collection<KeywordEntity> getFilteredPossibleEntities(final String name) {
        final Collection<? extends KeywordEntity> candidates = getPossibleEntities(name);
        if (candidates == null) {
            return null;
        }

        return filterDuplicates(candidates);
    }

    private Collection<KeywordEntity> filterDuplicates(final Collection<? extends KeywordEntity> candidates) {
        final LinkedHashSet<KeywordEntity> entities = new LinkedHashSet<>();
        for (final KeywordEntity entity : candidates) {
            final KeywordEntity onListEntity = getSameEntity(entities, entity);
            if (onListEntity == null) {
                entities.add(entity);
            } else if (!onListEntity.getExposingFilepath().equals(getFilepath())
                    && entity.getExposingFilepath().equals(getFilepath())) {
                entities.remove(onListEntity);
                entities.add(entity);
            }
        }
        return entities;
    }

    private KeywordEntity getSameEntity(final LinkedHashSet<KeywordEntity> entities,
            final KeywordEntity candidateEntity) {
        for (final KeywordEntity entity : entities) {
            if (entity == candidateEntity || entity.isSameAs(candidateEntity, getFilepath())) {
                return entity;
            }
        }
        return null;
    }

    private Collection<? extends KeywordEntity> getPossibleEntities(final String name) {
        final QualifiedKeywordName qualifiedName = QualifiedKeywordName.fromOccurrence(name);
        Collection<? extends KeywordEntity> keywords = getAccessibleKeywords()
                .get(qualifiedName.getKeywordName());
        
        if (keywords == null && name.contains(".")) { 
            // try to find keyword name with dots, ignore keyword source
            keywords = getAccessibleKeywords().get(QualifiedKeywordName.fromOccurrenceWithDots(name).getKeywordName());
        }
        
        if (keywords != null) {
            final LinkedHashSet<KeywordEntity> result = newLinkedHashSet(keywords);
            result.addAll(tryWithEmbeddedArguments(qualifiedName));
            return result;
        } else {
            return tryWithEmbeddedArguments(qualifiedName);
        }
    }

    private Collection<? extends KeywordEntity> tryWithEmbeddedArguments(final QualifiedKeywordName qualifiedName) {
        final List<KeywordEntity> matchingKeywordsWithEmbeddedArguments = new ArrayList<>();
        for (final Entry<String, Collection<KeywordEntity>> entry : getAccessibleKeywords().entrySet()) {
            if (EmbeddedKeywordNamesSupport.matches(entry.getKey(), qualifiedName)) {
                matchingKeywordsWithEmbeddedArguments.addAll(entry.getValue());
            }
        }
        return matchingKeywordsWithEmbeddedArguments;
    }

    public interface AccessibleKeywordsCollector {

        Map<String, Collection<KeywordEntity>> collect();
    }
}
