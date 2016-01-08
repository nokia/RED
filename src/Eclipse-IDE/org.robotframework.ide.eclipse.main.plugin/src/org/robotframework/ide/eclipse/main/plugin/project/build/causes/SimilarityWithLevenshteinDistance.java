/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.primitives.Ints;

/**
 * <p>With great power comes great responsibility. Use wisely since this is a naive implementation
 * (not a Levenshtein automaton), so performance is poor for really big (hundreds of thousands) 
 * candidates sets.</p>
 * 
 * <p>For the definition of Levenshtein distance and algorithm for calculating it see 
 * {@link https://en.wikipedia.org/wiki/Levenshtein_distance}</p>
 * 
 * <p>TODO : if this functionality is needed for bigger sets then either:
 *    <ol>
 *      <li>use Trie representation as described in {@link http://stevehanov.ca/blog/index.php?id=114}</li>
 *      <li>implement automaton instead</li>
 *    </ol>
 * </p>
 * 
 * @author Michal Anglart
 */
class SimilarityWithLevenshteinDistance {

    List<String> onlyWordsWithinDistance(final Collection<String> candidates, final String word,
            final int maxDistance) {
        final List<StringWithDistance> result = new ArrayList<>();

        for (final String candidate : candidates) {
            final int distance = distance(candidate, word);
            if (distance <= maxDistance) {
                result.add(new StringWithDistance(candidate, distance));
            }
        }
        Collections.sort(result);
        return newArrayList(transform(result, new Function<StringWithDistance, String>() {

            @Override
            public String apply(final StringWithDistance stringWithDistance) {
                return stringWithDistance.word;
            }
        }));
    }

    private int distance(final String word1, final String word2) {
        final int len1 = word1.length() + 1;
        final int len2 = word2.length() + 1;
        final int[][] dist = new int[len1][len2];
        for (int i = 0; i < len1; i++) {
            dist[i][0] = i;
        }
        for (int j = 0; j < len2; j++) {
            dist[0][j] = j;
        }
        
        for (int i = 1; i < len1; i++) {
            for (int j = 1; j < len2; j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dist[i][j] = dist[i - 1][j - 1];
                } else {
                    dist[i][j] = Ints.min(dist[i - 1][j] + 1, dist[i][j - 1] + 1, dist[i - 1][j - 1] + 1);
                }
            }
        }
        return dist[word1.length()][word2.length()];
    }
    
    private static final class StringWithDistance implements Comparable<StringWithDistance> {

        final String word;

        final int distance;

        StringWithDistance(final String word, final int distance) {
            this.word = word;
            this.distance = distance;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == StringWithDistance.class) {
                final StringWithDistance that = (StringWithDistance) obj;
                return Objects.equal(this.word, that.word) && Objects.equal(this.distance, that.distance);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(word, distance);
        }

        @Override
        public int compareTo(final StringWithDistance other) {
            if (distance != other.distance) {
                return distance - other.distance;
            }
            return word.compareToIgnoreCase(other.word);
        }
    }
}
