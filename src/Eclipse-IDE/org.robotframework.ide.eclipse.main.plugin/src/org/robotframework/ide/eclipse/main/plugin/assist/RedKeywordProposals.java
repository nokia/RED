/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.robotframework.ide.eclipse.main.plugin.assist.AssistProposals.sortedByLabels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.assist.BddMatchesHelper.BddAwareProposalMatch;
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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;

public class RedKeywordProposals {

    private final RobotSuiteFile suiteFile;

    private final ProposalMatcher matcher;

    private final AssistProposalPredicate<LibrarySpecification> libraryPredicate;

    public RedKeywordProposals(final RobotSuiteFile suiteFile) {
        this(suiteFile, ProposalMatchers.embeddedKeywordsMatcher(),
                AssistProposalPredicates.reservedLibraryPredicate());
    }

    RedKeywordProposals(final RobotSuiteFile suiteFile, final ProposalMatcher matcher,
            final AssistProposalPredicate<LibrarySpecification> libraryPredicate) {
        this.suiteFile = suiteFile;
        this.matcher = matcher;
        this.libraryPredicate = libraryPredicate;
    }

    public List<RedKeywordProposal> getKeywordProposals(final String userContent) {
        return getKeywordProposals(userContent, sortedByLabels());
    }

    public List<RedKeywordProposal> getKeywordProposals(final String userContent,
            final Comparator<? super RedKeywordProposal> comparator) {
        final AccessibleKeywordsEntities keywordEntities = getAccessibleKeywordsEntities(suiteFile, userContent);
        final ListMultimap<KeywordScope, KeywordEntity> possible = keywordEntities.getPossibleKeywords();

        final List<RedKeywordProposal> entities = new ArrayList<>();
        for (final KeywordEntity entity : possible.values()) {
            entities.add((RedKeywordProposal) entity);
        }
        Collections.sort(entities, comparator);
        return entities;
    }

    public RedKeywordProposal getBestMatchingKeywordProposal(final String keywordName) {
        final AccessibleKeywordsEntities accessibleKeywordsEntities = getAccessibleKeywordsEntities(suiteFile, "");
        final ListMultimap<KeywordScope, KeywordEntity> keywords = accessibleKeywordsEntities
                .getPossibleKeywords(keywordName, false);

        for (final KeywordScope scope : KeywordScope.defaultOrder()) {
            for (final KeywordEntity keyword : keywords.get(scope)) {
                return (RedKeywordProposal) keyword;
            }
        }
        return null;
    }

    private AccessibleKeywordsEntities getAccessibleKeywordsEntities(final RobotSuiteFile suite, final String userContent) {
        final AccessibleKeywordsCollector collector = new ProposalsKeywordCollector(suiteFile, matcher,
                libraryPredicate, shouldUseQualifiedName(), userContent);
        return new AccessibleKeywordsEntities(suite.getFile().getFullPath(), collector);
    }

    private Predicate<RedKeywordProposal> shouldUseQualifiedName() {
        return new Predicate<RedKeywordProposal>() {

            @Override
            public boolean apply(final RedKeywordProposal proposal) {
                final boolean isAutoPrefixEnabled = RedPlugin.getDefault().getPreferences().isAssistantKeywordPrefixAutoAdditionEnabled();
                return keywordIsNotInLocalScope(proposal)
                        && (isAutoPrefixEnabled || keywordProposalIsConflicting(proposal));
            }
        };
    }

    private boolean keywordIsNotInLocalScope(final RedKeywordProposal keywordProposal) {
        return keywordProposal.getScope(suiteFile.getFile().getFullPath()) != KeywordScope.LOCAL;
    }

    private boolean keywordProposalIsConflicting(final RedKeywordProposal keywordEntity) {
        final AccessibleKeywordsEntities accessibleKeywordsEntities = getAccessibleKeywordsEntities(suiteFile, "");
        final ListMultimap<KeywordScope, KeywordEntity> keywords = accessibleKeywordsEntities
                .getPossibleKeywords(keywordEntity.getNameFromDefinition(), false);

        for (final KeywordScope scope : KeywordScope.defaultOrder()) {
            final List<KeywordEntity> kwsInScope = keywords.get(scope);

            if (kwsInScope.contains(keywordEntity)) {
                // current scope contain our proposal we only have conflict if there are more entities
                // in this scope
                return kwsInScope.size() > 1;
            } else if (!kwsInScope.isEmpty()) {
                // current scope does not contain our proposal, but there is one, so it is
                // conflicting with given and as a result given proposal would need to qualify its name
                return true;
            }
        }
        return false;
    }

    private static final class ProposalsKeywordCollector implements AccessibleKeywordsCollector {

        private final RobotSuiteFile suiteFile;

        private final ProposalMatcher matcher;

        private final Predicate<LibrarySpecification> libraryPredicate;

        private final Predicate<RedKeywordProposal> shouldUseQualifiedName;

        private final String userContent;

        private ProposalsKeywordCollector(final RobotSuiteFile suiteFile, final ProposalMatcher matcher,
                final Predicate<LibrarySpecification> libraryPredicate,
                final Predicate<RedKeywordProposal> shouldUseQualifiedName, final String userContent) {
            this.matcher = matcher;
            this.libraryPredicate = libraryPredicate;
            this.userContent = userContent;
            this.shouldUseQualifiedName = shouldUseQualifiedName;
            this.suiteFile = suiteFile;
        }

        @Override
        public Map<String, Collection<KeywordEntity>> collect() {
            return collectAccessibleKeywords();
        }

        private Map<String, Collection<KeywordEntity>> collectAccessibleKeywords() {
            final Map<String, Collection<KeywordEntity>> accessibleKeywords = newHashMap();
            new KeywordDefinitionLocator(suiteFile.getFile()).locateKeywordDefinition(new KeywordDetector() {

                @Override
                public ContinueDecision libraryKeywordDetected(final LibrarySpecification libSpec,
                        final KeywordSpecification kwSpec, final Set<String> libraryAliases,
                        final RobotSuiteFile exposingFile) {
                    final KeywordScope scope = libSpec.isReferenced() ? KeywordScope.REF_LIBRARY
                            : KeywordScope.STD_LIBRARY;

                    if (!libraryPredicate.apply(libSpec)) {
                        return ContinueDecision.CONTINUE;
                    }

                    final Set<String> aliases = libraryAliases.isEmpty() ? newHashSet(libSpec.getName())
                            : libraryAliases;

                    for (final String alias : aliases) {
                        final String aliasToUse = alias.isEmpty() ? libSpec.getName() : alias;
                        final String keywordName = kwSpec.getName();

                        final BddMatchesHelper bddMatchesHelper = new BddMatchesHelper(matcher);
                        final BddAwareProposalMatch keywordMatch = bddMatchesHelper.findBddAwareMatch(userContent,
                                keywordName);

                        if (keywordMatch.getMatch().isPresent()) {
                            final RedKeywordProposal proposal = AssistProposals.createLibraryKeywordProposal(libSpec,
                                    kwSpec, keywordMatch.getBddPrefix(), scope, aliasToUse,
                                    exposingFile.getFile().getFullPath(), shouldUseQualifiedName,
                                    keywordMatch.getMatch().get());
                            addAccessibleKeyword(keywordName, proposal);
                        } else {
                            final String qualifiedName = aliasToUse + "." + keywordName;
                            final BddAwareProposalMatch qualifiedKeywordMatch = bddMatchesHelper
                                    .findBddAwareMatch(userContent, qualifiedName);
                            if (qualifiedKeywordMatch.getMatch().isPresent()) {
                                final ProposalMatch match = qualifiedKeywordMatch.getMatch().get();
                                final Optional<ProposalMatch> inLabelMatch = match
                                        .mapToFragment(aliasToUse.length() + 1, keywordName.length());

                                if (inLabelMatch.isPresent()) {
                                    final RedKeywordProposal proposal = AssistProposals.createLibraryKeywordProposal(
                                            libSpec, kwSpec, qualifiedKeywordMatch.getBddPrefix(), scope, aliasToUse,
                                            exposingFile.getFile().getFullPath(),
                                            AssistProposalPredicates.<RedKeywordProposal> alwaysTrue(),
                                            inLabelMatch.get());
                                    addAccessibleKeyword(keywordName, proposal);
                                }
                            }
                        }
                    }
                    return ContinueDecision.CONTINUE;
                }

                @Override
                public ContinueDecision keywordDetected(final RobotSuiteFile file,
                        final RobotKeywordDefinition keyword) {
                    final KeywordScope scope = suiteFile == file ? KeywordScope.LOCAL : KeywordScope.RESOURCE;

                    final String alias = Files.getNameWithoutExtension(file.getName());
                    final String keywordName = keyword.getName();

                    final BddMatchesHelper bddMatchesHelper = new BddMatchesHelper(matcher);

                    final BddAwareProposalMatch keywordMatch = bddMatchesHelper.findBddAwareMatch(userContent,
                            keywordName);
                    if (keywordMatch.getMatch().isPresent()) {
                        final RedKeywordProposal proposal = AssistProposals.createUserKeywordProposal(keyword,
                                keywordMatch.getBddPrefix(), scope, alias, shouldUseQualifiedName,
                                keywordMatch.getMatch().get());
                        addAccessibleKeyword(keywordName, proposal);
                    } else {
                        final String qualifiedName = alias + "." + keywordName;
                        final BddAwareProposalMatch qualifiedKeywordMatch = bddMatchesHelper
                                .findBddAwareMatch(userContent, qualifiedName);
                        if (qualifiedKeywordMatch.getMatch().isPresent()) {
                            final ProposalMatch match = qualifiedKeywordMatch.getMatch().get();
                            final Optional<ProposalMatch> inLabelMatch = match.mapToFragment(alias.length() + 1,
                                    keywordName.length());
                            
                            if (inLabelMatch.isPresent()) {
                                final RedKeywordProposal proposal = AssistProposals.createUserKeywordProposal(keyword,
                                        qualifiedKeywordMatch.getBddPrefix(), scope, alias,
                                        AssistProposalPredicates.<RedKeywordProposal> alwaysTrue(), inLabelMatch.get());
                                addAccessibleKeyword(keywordName, proposal);
                            }
                        }
                    }
                    return ContinueDecision.CONTINUE;
                }

                private void addAccessibleKeyword(final String keywordName, final RedKeywordProposal keyword) {
                    final String unifiedName = QualifiedKeywordName.unifyDefinition(keywordName);

                    if (!accessibleKeywords.containsKey(unifiedName)) {
                        accessibleKeywords.put(unifiedName, new LinkedHashSet<KeywordEntity>());
                    }
                    accessibleKeywords.get(unifiedName).add(keyword);
                }
            });
            return accessibleKeywords;
        }
    }
}
