/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.search.keyword;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    public <T> List<T> getBestMatchingKeyword(final ListMultimap<String, T> foundKeywords, final Extractor<T> extractor,
            final String keywordName) {
        Set<T> keywords = new LinkedHashSet<T>(0);
        final List<String> namesToCheck = getNamesToCheck(keywordName);
        for (final String name : namesToCheck) {
            keywords.addAll(foundKeywords.get(name.toLowerCase()));
            keywords.addAll(foundKeywords.get(name));
            keywords.addAll(foundKeywords.get(QualifiedKeywordName.unifyDefinition(name)));

            if (!keywords.isEmpty()) {
                final Set<T> matchNameWhole = new LinkedHashSet<>(0);
                Iterator<T> iterator = keywords.iterator();
                for (int i = 0; i < keywords.size() && iterator.hasNext(); i++) {
                    final T keyword = iterator.next();
                    final String keywordNameFromKeywordDefinition = extractor.keywordName(keyword);
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

    public <T> ListMultimap<String, T> findKeywords(final Collection<T> keywords, final Extractor<T> extractor,
            final String usageName) {
        final ListMultimap<String, T> foundByMatch = ArrayListMultimap.create();

        final List<String> possibleNameCombination = getNamesToCheck(usageName);
        for (final T keyword : keywords) {
            final String alias = extractor.alias(keyword).toLowerCase();
            final String sourceName = extractor.sourceName(keyword).toLowerCase();
            final String fileNameWithoutExtension = getFileNameWithoutExtension(extractor, keyword);

            final String keywordName = QualifiedKeywordName.unifyDefinition(extractor.keywordName(keyword))
                    .toLowerCase();
            final boolean isEmbeddedKeywordName = EmbeddedKeywordNamesSupport.hasEmbeddedArguments(keywordName);
            for (String nameCombination : possibleNameCombination) {
                if (!isEmbeddedKeywordName) {
                    nameCombination = QualifiedKeywordName.unifyDefinition(nameCombination);
                }

                if (matchNameDirectlyOrAsEmbeddedName(foundByMatch, keyword, keywordName, null, isEmbeddedKeywordName,
                        nameCombination)) {
                    continue;
                }

                if (!alias.isEmpty()) {
                    if (matchNameDirectlyOrAsEmbeddedName(foundByMatch, keyword, keywordName, alias,
                            isEmbeddedKeywordName, nameCombination)) {
                        continue;
                    }
                }

                if (!sourceName.isEmpty()) {
                    if (matchNameDirectlyOrAsEmbeddedName(foundByMatch, keyword, keywordName, sourceName,
                            isEmbeddedKeywordName, nameCombination)) {
                        continue;
                    }
                }

                if (!fileNameWithoutExtension.isEmpty() && !sourceName.equals(fileNameWithoutExtension)
                        && !alias.equals(fileNameWithoutExtension)) {
                    if (matchNameDirectlyOrAsEmbeddedName(foundByMatch, keyword, keywordName, fileNameWithoutExtension,
                            isEmbeddedKeywordName, nameCombination)) {
                        continue;
                    }
                }
            }
        }

        return foundByMatch;
    }

    private <T> boolean matchNameDirectlyOrAsEmbeddedName(final ListMultimap<String, T> foundByMatch, final T keyword,
            final String keywordName, final String prefixName, boolean isEmbeddedKeywordName,
            final String nameCombination) {
        String withPrefix;
        if (prefixName != null) {
            withPrefix = prefixName + "." + keywordName;
        } else {
            withPrefix = keywordName;
        }
        if (!isEmbeddedKeywordName) {
            withPrefix = QualifiedKeywordName.unifyDefinition(withPrefix);
        }
        if (nameCombination.equalsIgnoreCase(withPrefix)) {
            foundByMatch.put(nameCombination, keyword);
            return true;
        } else if (EmbeddedKeywordNamesSupport.matchesWithLowerCase(withPrefix, nameCombination,
                nameCombination.toLowerCase())) {
            foundByMatch.put(nameCombination, keyword);
            return true;
        }

        return false;
    }

    private <T> String getFileNameWithoutExtension(final Extractor<T> extractor, final T keyword) {
        final String fullFileName = extractor.path(keyword).getFileName().toString();
        final int lastDotIndex = fullFileName.lastIndexOf('.');
        final String fileNameWithoutExtension;
        if (lastDotIndex > -1) {
            fileNameWithoutExtension = fullFileName.substring(0, lastDotIndex).toLowerCase();
        } else {
            fileNameWithoutExtension = "";// fullFileName;
        }
        return fileNameWithoutExtension;
    }

    public static interface Extractor<T> {

        KeywordScope scope(final T keyword);

        Path path(final T keyword);

        String alias(final T keyword);

        String keywordName(final T keyword);

        String sourceName(final T keyword);

    }

    private List<String> getNamesToCheck(final String usageName) {
        final List<String> possibleNameCombination = new ArrayList<>(possibleNameCombination(usageName));
        Collections.sort(possibleNameCombination, new FromLongestLengthComparator());

        return possibleNameCombination;
    }

    private class FromLongestLengthComparator implements Comparator<String> {

        @Override
        public int compare(final String o1, final String o2) {
            int o1Length = (o1 != null) ? o1.length() : 0;
            int o2Length = (o2 != null) ? o2.length() : 0;

            return -(Integer.compare(o1Length, o2Length)); // desc order
        }
    }

    private Set<String> possibleNameCombination(final String usageName) {
        final Set<String> names = new HashSet<>(1);
        names.add(usageName); // original name

        // gherking BDD syntax extraction if any
        String lastNameUsage = gherkinSyntaxCombination(names, usageName);

        // dot resource or library syntax
        lastNameUsage = extractResourceAndLibraryNameCombination(names, lastNameUsage);

        return names;
    }

    private String extractResourceAndLibraryNameCombination(final Set<String> names, final String usageName) {
        final String[] splitted = usageName.split("[.]");
        final List<String> asList = Arrays.asList(splitted);

        final int splittedLength = splitted.length;
        for (int dotIndex = 0; dotIndex < splittedLength; dotIndex++) {
            names.add(Joiner.on('.').join(asList.subList(dotIndex, splittedLength)));
        }

        return splitted[splittedLength - 1];
    }

    private String gherkinSyntaxCombination(final Set<String> names, final String usageName) {
        boolean shouldExtract = true;
        String currentName = usageName;
        while (shouldExtract) {
            String newName = GherkinStyleSupport.getTextAfterGherkinPrefixIfExists(currentName);
            if (newName.equals(currentName)) {
                shouldExtract = false;
            } else {
                names.add(newName);
                currentName = newName;
            }
        }

        return currentName;
    }
}
