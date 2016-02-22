/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.model.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities.AccessibleKeywordsCollector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;

public class RedKeywordProposals {

    private final RobotSuiteFile suiteFile;

    public RedKeywordProposals(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    public static Comparator<RedKeywordProposal> sortedBySourcesAndNames() {
        return new Comparator<RedKeywordProposal>() {
            @Override
            public int compare(final RedKeywordProposal proposal1, final RedKeywordProposal proposal2) {
                if (proposal1.getType() == proposal2.getType()) {
                    if (proposal1.getSourceName().equals(proposal2.getSourceName())) {
                        return proposal1.getLabel().compareTo(proposal2.getLabel());
                    } else {
                        return proposal1.getSourceName().compareTo(proposal2.getSourceName());
                    }
                } else {
                    return proposal1.getType().compareTo(proposal2.getType());
                }
            }
        };
    }

    public static Comparator<RedKeywordProposal> sortedByNames() {
        return new Comparator<RedKeywordProposal>() {
            @Override
            public int compare(final RedKeywordProposal proposal1, final RedKeywordProposal proposal2) {
                return proposal1.getLabel().compareTo(proposal2.getLabel());
            }
        };
    }

    public List<RedKeywordProposal> getKeywordProposals(final String prefix,
            final Comparator<? super RedKeywordProposal> comparator) {
        final AccessibleKeywordsEntities keywordEntities = getAccessibleKeywordsEntities(suiteFile, prefix);
        final ListMultimap<KeywordScope, KeywordEntity> possible = keywordEntities.getPossibleKeywords();

        final List<RedKeywordProposal> entities = new ArrayList<>();
        for (final KeywordEntity entity : possible.values()) {
            entities.add((RedKeywordProposal) entity);
        }
        if (comparator != null) {
            Collections.sort(entities, comparator);
        }
        return entities;
    }

    public RedKeywordProposal getBestMatchingKeywordProposal(final String keywordName) {
        final AccessibleKeywordsEntities accessibleKeywordsEntities = getAccessibleKeywordsEntities(suiteFile, "");
        final ListMultimap<KeywordScope, KeywordEntity> keywords = accessibleKeywordsEntities
                .getPossibleKeywords(keywordName);

        for (final KeywordScope scope : KeywordScope.defaultOrder()) {
            for (final KeywordEntity keyword : keywords.get(scope)) {
                return (RedKeywordProposal) keyword;
            }
        }
        return null;
    }

    private AccessibleKeywordsEntities getAccessibleKeywordsEntities(final RobotSuiteFile suite, final String prefix) {
        final AccessibleKeywordsCollector collector = new ProposalsKeywordCollector(suiteFile, prefix);
        return new AccessibleKeywordsEntities(suite.getFile().getFullPath(), collector);
    }

    private static final class ProposalsKeywordCollector implements AccessibleKeywordsCollector {

        private final RobotSuiteFile suiteFile;
        private final String prefix;

        private ProposalsKeywordCollector(final RobotSuiteFile suiteFile, final String prefix) {
            this.prefix = prefix;
            this.suiteFile = suiteFile;
        }

        @Override
        public Map<String, Collection<KeywordEntity>> collect() {
            return collectAccessibleKeywords(prefix);
        }

        private Map<String, Collection<KeywordEntity>> collectAccessibleKeywords(final String prefix) {
            final Map<String, Collection<KeywordEntity>> accessibleKeywords = newHashMap();
            new KeywordDefinitionLocator(suiteFile.getFile()).locateKeywordDefinition(new KeywordDetector() {

                @Override
                public ContinueDecision libraryKeywordDetected(final LibrarySpecification libSpec,
                        final KeywordSpecification kwSpec, final String libraryAlias,
                        final RobotSuiteFile exposingFile) {
                    final KeywordScope scope = libSpec.isReferenced() ? KeywordScope.REF_LIBRARY
                            : KeywordScope.STD_LIBRARY;
                    final String sourceName = libraryAlias.isEmpty() ? libSpec.getName() : libraryAlias;

                    addAccessibleKeyword(kwSpec.getName(), RedKeywordProposal.create(libSpec, kwSpec, scope, sourceName,
                            exposingFile.getFile().getFullPath()), prefix);
                    return ContinueDecision.CONTINUE;
                }

                @Override
                public ContinueDecision keywordDetected(final RobotSuiteFile file,
                        final RobotKeywordDefinition keyword) {
                    final KeywordScope scope = suiteFile == file ? KeywordScope.LOCAL : KeywordScope.RESOURCE;
                    final String sourceName = Files.getNameWithoutExtension(file.getName());
                    addAccessibleKeyword(keyword.getName(), RedKeywordProposal.create(file, keyword, scope, sourceName),
                            prefix);
                    return ContinueDecision.CONTINUE;
                }

                private void addAccessibleKeyword(final String keywordName, final RedKeywordProposal keyword,
                        final String prefix) {
                    if (!matchesPrefix(keyword, prefix)) {
                        return;
                    }
                    final String unifiedName = QualifiedKeywordName.unifyDefinition(keywordName);
                    if (accessibleKeywords.containsKey(unifiedName)) {
                        accessibleKeywords.get(unifiedName).add(keyword);
                    } else {
                        final LinkedHashSet<KeywordEntity> setOfKeywords = newLinkedHashSet();
                        setOfKeywords.add(keyword);
                        accessibleKeywords.put(unifiedName, setOfKeywords);
                    }
                }

                private boolean matchesPrefix(final RedKeywordProposal keyword, final String prefix) {
                    final String keywordName = keyword.getLabel();
                    final String keywordPrefix = keyword.getSourcePrefix() + ".";
                    final String wholeDefinition = keywordPrefix + keywordName;

                    return EmbeddedKeywordNamesSupport.startsWith(keywordName, prefix)
                            || EmbeddedKeywordNamesSupport.startsWith(wholeDefinition, prefix);
                }
            });
            return accessibleKeywords;
        }
    }
}
