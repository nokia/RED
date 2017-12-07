/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static org.robotframework.ide.eclipse.main.plugin.assist.AssistProposals.createLibraryKeywordProposal;
import static org.robotframework.ide.eclipse.main.plugin.assist.AssistProposals.createNotAccessibleLibraryKeywordProposal;
import static org.robotframework.ide.eclipse.main.plugin.assist.AssistProposals.createUserKeywordProposal;
import static org.robotframework.ide.eclipse.main.plugin.assist.AssistProposals.sortedByLabelsCamelCaseAndPrefixedFirst;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.assist.BddMatchesHelper.BddAwareProposalMatch;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities.AccessibleKeywordsCollector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ListMultimap;
import com.google.common.io.Files;

public class RedKeywordProposals {

    private final RobotModel model;

    private final RobotSuiteFile suiteFile;

    private final ProposalMatcher matcher;

    private final AssistProposalPredicate<LibrarySpecification> libraryPredicate;

    public RedKeywordProposals(final RobotSuiteFile suiteFile) {
        this(RedPlugin.getModelManager().getModel(), suiteFile, ProposalMatchers.keywordsMatcher(),
                AssistProposalPredicates.reservedLibraryPredicate());
    }

    @VisibleForTesting
    public RedKeywordProposals(final RobotModel model, final RobotSuiteFile suiteFile) {
        this(model, suiteFile, ProposalMatchers.keywordsMatcher(), AssistProposalPredicates.reservedLibraryPredicate());
    }

    @VisibleForTesting
    RedKeywordProposals(final RobotModel model, final RobotSuiteFile suiteFile, final ProposalMatcher matcher,
            final AssistProposalPredicate<LibrarySpecification> libraryPredicate) {
        this.model = model;
        this.suiteFile = suiteFile;
        this.matcher = matcher;
        this.libraryPredicate = libraryPredicate;
    }

    public List<RedKeywordProposal> getKeywordProposals(final String userContent) {
        return getKeywordProposals(userContent, sortedByLabelsCamelCaseAndPrefixedFirst(userContent));
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

    public Optional<RedKeywordProposal> getBestMatchingKeywordProposal(final String keywordName) {
        final AccessibleKeywordsEntities accessibleKeywordsEntities = getAccessibleKeywordsEntities(suiteFile, "");
        final ListMultimap<KeywordScope, KeywordEntity> keywords = accessibleKeywordsEntities
                .getPossibleKeywords(keywordName, false);

        for (final KeywordScope scope : KeywordScope.defaultOrder()) {
            for (final KeywordEntity keyword : keywords.get(scope)) {
                return Optional.of((RedKeywordProposal) keyword);
            }
        }
        return Optional.empty();
    }

    private AccessibleKeywordsEntities getAccessibleKeywordsEntities(final RobotSuiteFile suite,
            final String userContent) {
        final AccessibleKeywordsCollector collector = new ProposalsKeywordCollector(shouldUseQualifiedName(),
                shouldIncludeNotImportedLibraries(), userContent);
        return new AccessibleKeywordsEntities(suite.getFile().getFullPath(), collector);
    }

    private Predicate<RedKeywordProposal> shouldUseQualifiedName() {
        return proposal -> {
            final boolean isAutoPrefixEnabled = RedPlugin.getDefault()
                    .getPreferences()
                    .isAssistantKeywordPrefixAutoAdditionEnabled();
            return keywordIsNotInLocalScope(proposal)
                    && (isAutoPrefixEnabled || keywordProposalIsConflicting(proposal));
        };
    }

    private boolean shouldIncludeNotImportedLibraries() {
        return RedPlugin.getDefault().getPreferences().isAssistantKeywordFromNotImportedLibraryEnabled();
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
                // current scope contain our proposal we only have conflict if there are more
                // entities in this scope
                return kwsInScope.size() > 1;
            } else if (!kwsInScope.isEmpty()) {
                // current scope does not contain our proposal, but there is one, so it is
                // conflicting with given and as a result given proposal would need to qualify its
                // name
                return true;
            }
        }
        return false;
    }

    private final class ProposalsKeywordCollector implements AccessibleKeywordsCollector {

        private final Predicate<RedKeywordProposal> shouldUseQualifiedName;

        private final boolean includeNotImportedLibraries;

        private final String userContent;

        private ProposalsKeywordCollector(final Predicate<RedKeywordProposal> shouldUseQualifiedName,
                final boolean includeNotImportedLibraries, final String userContent) {
            this.shouldUseQualifiedName = shouldUseQualifiedName;
            this.includeNotImportedLibraries = includeNotImportedLibraries;
            this.userContent = userContent;
        }

        @Override
        public Map<String, Collection<KeywordEntity>> collect() {
            return collectAccessibleKeywords();
        }

        private Map<String, Collection<KeywordEntity>> collectAccessibleKeywords() {
            final Map<String, Collection<KeywordEntity>> accessibleKeywords = new HashMap<>();
            new KeywordDefinitionLocator(suiteFile.getFile(), model, includeNotImportedLibraries)
                    .locateKeywordDefinition(new KeywordDetector() {

                        @Override
                        public ContinueDecision nonAccessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                                final KeywordSpecification kwSpec, final RobotSuiteFile exposingFile) {
                            if (!libraryPredicate.test(libSpec)) {
                                return ContinueDecision.CONTINUE;
                            }

                            final KeywordScope scope = libSpec.isReferenced() ? KeywordScope.REF_LIBRARY
                                    : KeywordScope.STD_LIBRARY;
                            final String keywordName = kwSpec.getName();
                            final String sourcePrefix = libSpec.getName();

                            matchKeyword(keywordName, sourcePrefix,
                                    (bddPrefix, match) -> createNotAccessibleLibraryKeywordProposal(libSpec, kwSpec,
                                            bddPrefix, scope, Optional.empty(), exposingFile.getFile().getFullPath(),
                                            shouldUseQualifiedName, match),
                                    (bddPrefix, match) -> createNotAccessibleLibraryKeywordProposal(libSpec, kwSpec,
                                            bddPrefix, scope, Optional.empty(), exposingFile.getFile().getFullPath(),
                                            AssistProposalPredicates.alwaysTrue(), match));

                            return ContinueDecision.CONTINUE;
                        }

                        @Override
                        public ContinueDecision accessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                                final KeywordSpecification kwSpec, final Collection<Optional<String>> libraryAliases,
                                final RobotSuiteFile exposingFile) {
                            if (!libraryPredicate.test(libSpec)) {
                                return ContinueDecision.CONTINUE;
                            }

                            final KeywordScope scope = libSpec.isReferenced() ? KeywordScope.REF_LIBRARY
                                    : KeywordScope.STD_LIBRARY;
                            final String keywordName = kwSpec.getName();

                            for (final Optional<String> alias : libraryAliases) {
                                matchKeyword(keywordName, alias.orElse(libSpec.getName()),
                                        (bddPrefix, match) -> createLibraryKeywordProposal(libSpec, kwSpec, bddPrefix,
                                                scope, alias, exposingFile.getFile().getFullPath(),
                                                shouldUseQualifiedName, match),
                                        (bddPrefix, match) -> createLibraryKeywordProposal(libSpec, kwSpec, bddPrefix,
                                                scope, alias, exposingFile.getFile().getFullPath(),
                                                AssistProposalPredicates.alwaysTrue(), match));
                            }

                            return ContinueDecision.CONTINUE;
                        }

                        @Override
                        public ContinueDecision keywordDetected(final RobotSuiteFile file,
                                final RobotKeywordDefinition keyword) {
                            final KeywordScope scope = suiteFile == file ? KeywordScope.LOCAL : KeywordScope.RESOURCE;
                            final String keywordName = keyword.getName();
                            final String sourcePrefix = Files.getNameWithoutExtension(file.getName());

                            matchKeyword(keywordName, sourcePrefix,
                                    (bddPrefix, match) -> createUserKeywordProposal(keyword, bddPrefix, scope,
                                            shouldUseQualifiedName, match),
                                    (bddPrefix, match) -> createUserKeywordProposal(keyword, bddPrefix, scope,
                                            AssistProposalPredicates.alwaysTrue(), match));

                            return ContinueDecision.CONTINUE;
                        }

                        private void matchKeyword(final String keywordName, final String sourcePrefix,
                                final BiFunction<String, ProposalMatch, RedKeywordProposal> matchFunction,
                                final BiFunction<String, ProposalMatch, RedKeywordProposal> prefixMatchFunction) {
                            final BddMatchesHelper bddMatchesHelper = new BddMatchesHelper(matcher);
                            final BddAwareProposalMatch keywordMatch = bddMatchesHelper.findBddAwareMatch(userContent,
                                    keywordName);

                            if (keywordMatch.getMatch().isPresent()) {
                                final RedKeywordProposal proposal = matchFunction.apply(keywordMatch.getBddPrefix(),
                                        keywordMatch.getMatch().get());
                                addAccessibleKeyword(keywordName, proposal);
                            } else {
                                final String qualifiedName = sourcePrefix + "." + keywordName;
                                final BddAwareProposalMatch qualifiedKeywordMatch = bddMatchesHelper
                                        .findBddAwareMatch(userContent, qualifiedName);
                                if (qualifiedKeywordMatch.getMatch().isPresent()) {
                                    final ProposalMatch match = qualifiedKeywordMatch.getMatch().get();
                                    final Optional<ProposalMatch> inLabelMatch = match
                                            .mapAndShiftToFragment(sourcePrefix.length() + 1, keywordName.length());

                                    if (inLabelMatch.isPresent()) {
                                        final RedKeywordProposal proposal = prefixMatchFunction
                                                .apply(qualifiedKeywordMatch.getBddPrefix(), inLabelMatch.get());
                                        addAccessibleKeyword(keywordName, proposal);
                                    }
                                }
                            }
                        }

                        private void addAccessibleKeyword(final String keywordName, final RedKeywordProposal keyword) {
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
}
