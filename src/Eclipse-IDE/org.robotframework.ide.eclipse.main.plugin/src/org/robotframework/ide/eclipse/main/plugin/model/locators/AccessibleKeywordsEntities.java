/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.search.keyword.KeywordSearcher;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author Michal Anglart
 */
public class AccessibleKeywordsEntities {

    private final IPath filepath;

    private final AccessibleKeywordsCollector collector;

    private final KeywordSearcher keywordSearcher;

    private Map<String, Collection<KeywordEntity>> accessibleKeywords;

    public AccessibleKeywordsEntities(final IPath filepath, final AccessibleKeywordsCollector collector) {
        this.filepath = filepath;
        this.collector = collector;
        this.keywordSearcher = new KeywordSearcher();
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

    public boolean isKeywordAccessible(final ListMultimap<String, KeywordEntity> foundKeywords,
            final String keywordName) {
        return (foundKeywords.containsKey(keywordName) || foundKeywords.containsKey(keywordName.toLowerCase())
                || foundKeywords.containsKey(QualifiedKeywordName.unifyDefinition(keywordName)));
    }

    public ListMultimap<String, KeywordEntity> findPossibleKeywords(final String keywordName,
            final boolean stopIfOneWasMatching) {
        final Collection<KeywordEntity> filterAgainstDuplications = getAccessibleKeywordsDeduplicated();

        return keywordSearcher.findKeywords(getAccessibleKeywords(), filterAgainstDuplications,
                keywordName, stopIfOneWasMatching);
    }

    protected Collection<KeywordEntity> getAccessibleKeywordsDeduplicated() {
        final List<KeywordEntity> hereKeywords = new ArrayList<>();
        for (final Collection<KeywordEntity> k : getAccessibleKeywords().values()) {
            hereKeywords.addAll(k);
        }
        return filterDuplicates(hereKeywords);
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

    public ListMultimap<KeywordScope, KeywordEntity> getPossibleKeywords(
            final ListMultimap<String, KeywordEntity> foundKeywords, final String keywordName) {

        final List<KeywordEntity> keywords = new ArrayList<>(filterDuplicates(
                keywordSearcher.getBestMatchingKeyword(foundKeywords, keywordName)));

        final ListMultimap<KeywordScope, KeywordEntity> scopedKeywords = ArrayListMultimap.create();

        for (final KeywordEntity keyword : keywords) {
            scopedKeywords.put(keyword.getScope(getFilepath()), keyword);
        }
        return scopedKeywords;
    }

    public ListMultimap<KeywordScope, KeywordEntity> getPossibleKeywords(final String keywordName,
            final boolean stopIfOneWasMatching) {

        final List<KeywordEntity> hereKeywords = new ArrayList<>();
        hereKeywords.addAll(getPossibleKeywords().values());

        final ListMultimap<String, KeywordEntity> foundKeywords = keywordSearcher.findKeywords(getAccessibleKeywords(),
                filterDuplicates(hereKeywords), keywordName, stopIfOneWasMatching);

        return getPossibleKeywords(foundKeywords, keywordName);
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

    public interface AccessibleKeywordsCollector {

        Map<String, Collection<KeywordEntity>> collect();
    }
}
