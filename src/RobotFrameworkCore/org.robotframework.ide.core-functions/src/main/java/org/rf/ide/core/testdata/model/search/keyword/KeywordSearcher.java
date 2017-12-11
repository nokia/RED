/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.search.keyword;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.rf.ide.core.testdata.model.table.keywords.names.EmbeddedKeywordNamesSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.GherkinStyleSupport;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author wypych
 */
public class KeywordSearcher {

    public <T extends SearchableKeyword> List<T> getBestMatchingKeyword(final ListMultimap<String, T> foundKeywords,
            final String keywordName) {
        Set<T> keywords = new LinkedHashSet<>(0);
        final List<String> namesToCheck = getNamesToCheck(keywordName);
        for (final String name : namesToCheck) {
            keywords.addAll(foundKeywords.get(name.toLowerCase()));
            keywords.addAll(foundKeywords.get(name));
            keywords.addAll(foundKeywords.get(QualifiedKeywordName.unifyDefinition(name)));

            if (!keywords.isEmpty()) {
                final Set<T> matchNameWhole = new LinkedHashSet<>(0);
                final Iterator<T> iterator = keywords.iterator();
                for (int i = 0; i < keywords.size() && iterator.hasNext(); i++) {
                    final T keyword = iterator.next();
                    final String keywordNameFromKeywordDefinition = QualifiedKeywordName
                            .unifyDefinition(keyword.getKeywordName());
                    if (name.equalsIgnoreCase(keywordNameFromKeywordDefinition)) {
                        matchNameWhole.add(keyword);
                    }
                }
                if (!matchNameWhole.isEmpty()) {
                    keywords = matchNameWhole;
                }

                break;
            }
        }
        return new ArrayList<>(keywords);
    }

    public <T extends SearchableKeyword> ListMultimap<String, T> findKeywords(
            final Map<String, Collection<T>> accessibleKeywords, final Collection<T> keywords, final String usageName,
            final boolean stopIfOneWasMatching) {
        final ListMultimap<String, T> foundByMatch = ArrayListMultimap.create();

        if (stopIfOneWasMatching) {
            final Collection<T> collection = accessibleKeywords.get(QualifiedKeywordName.unifyDefinition(usageName));
            if (collection != null && collection.size() == 1) {
                foundByMatch.putAll(QualifiedKeywordName.unifyDefinition(usageName), collection);

                return foundByMatch;
            }
        }

        final List<String> possibleNameCombinations = getNamesToCheck(usageName);
        for (final T keyword : keywords) {
            final String keywordName = QualifiedKeywordName.unifyDefinition(keyword.getKeywordName()).toLowerCase();
            final boolean isEmbeddedKeywordName = EmbeddedKeywordNamesSupport.hasEmbeddedArguments(keywordName);

            final List<String> unifiedNameCombinations = isEmbeddedKeywordName
                    ? possibleNameCombinations
                    : possibleNameCombinations.stream().map(QualifiedKeywordName::unifyDefinition).collect(toList());

            for (final String nameCombination : unifiedNameCombinations) {
                final Predicate<String> matcher = qualifier -> matchNameDirectlyOrAsEmbeddedName(keywordName, qualifier,
                        isEmbeddedKeywordName, nameCombination);

                if (matcher.test(null) || matcher.test(keyword.getSourceNameInUse())) {
                    foundByMatch.put(nameCombination, keyword);
                    if (stopIfOneWasMatching) {
                        break;
                    } else {
                        continue;
                    }
                }
            }
        }
        return foundByMatch;
    }

    private boolean matchNameDirectlyOrAsEmbeddedName(final String keywordName, final String prefixName,
            final boolean isEmbeddedKeywordName, final String nameCombination) {

        String prefixedKeywordName = prefixName != null ? prefixName.toLowerCase() + "." + keywordName : keywordName;
        if (!isEmbeddedKeywordName) {
            prefixedKeywordName = QualifiedKeywordName.unifyDefinition(prefixedKeywordName);
        }
        return EmbeddedKeywordNamesSupport.matchesIgnoreCase(prefixedKeywordName, nameCombination);
    }

    private List<String> getNamesToCheck(final String usageName) {
        final List<String> possibleNameCombination = new ArrayList<>(possibleNameCombination(usageName));
        Collections.sort(possibleNameCombination, new FromLongestLengthComparator());

        return possibleNameCombination;
    }

    private Set<String> possibleNameCombination(final String usageName) {
        final Set<String> names = new HashSet<>(1);
        names.add(usageName); // original name

        int beforeSize = 0;
        String lastNameUsage = usageName;

        while (beforeSize < names.size()) {
            beforeSize = names.size();
            // gherking BDD syntax extraction if any
            lastNameUsage = gherkinSyntaxCombination(names, lastNameUsage);

            // dot resource or library syntax
            lastNameUsage = extractResourceAndLibraryNameCombination(names, lastNameUsage);
        }

        return names;
    }

    private String extractResourceAndLibraryNameCombination(final Set<String> names, final String usageName) {
        final String[] splitted = usageName.split("[.]");
        final List<String> asList = Arrays.asList(splitted);

        final int splittedLength = splitted.length;
        for (int dotIndex = 0; dotIndex < splittedLength; dotIndex++) {
            names.add(Joiner.on('.').join(asList.subList(dotIndex, splittedLength)));
        }

        return (splittedLength > 0) ? splitted[splittedLength - 1] : "";
    }

    private String gherkinSyntaxCombination(final Set<String> names, final String usageName) {
        boolean shouldExtract = true;
        String currentName = usageName;
        while (shouldExtract) {
            final String newName = GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(currentName);
            if (newName.equals(currentName)) {
                shouldExtract = false;
            } else {
                names.add(newName);
                currentName = newName;
            }
        }

        return currentName;
    }

    public static interface SearchableKeyword {

        String getSourceNameInUse();

        String getKeywordName();
    }

    private class FromLongestLengthComparator implements Comparator<String> {

        @Override
        public int compare(final String o1, final String o2) {
            final int o1Length = (o1 != null) ? o1.length() : 0;
            final int o2Length = (o2 != null) ? o2.length() : 0;

            return -(Integer.compare(o1Length, o2Length)); // desc order
        }
    }
}
