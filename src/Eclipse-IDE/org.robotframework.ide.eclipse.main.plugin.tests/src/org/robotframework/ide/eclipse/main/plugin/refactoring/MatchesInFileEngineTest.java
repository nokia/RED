/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.Position;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.refactoring.MatchingEngine.MatchAccess;
import org.robotframework.red.junit.ProjectProvider;

public class MatchesInFileEngineTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(MatchesInFileEngineTest.class);

    private static MatchesInFileEngine matchingEngine;

    @BeforeClass 
    public static void beforeSuite() throws Exception {
        final IFile file = projectProvider.createFile(new Path("file.txt"), "line 1", "line 2", "line 3", "line 4",
                "line 5", "line 6", "line 7", "line 8", "line 9", "line 10", "line 11");
        matchingEngine = new MatchesInFileEngine(file);
    }

    @AfterClass
    public static void afterSuite() {
        matchingEngine = null;
    }

    @Test
    public void noMatchesForNonExistingMatch() {
        final Map<Position, String> matches = searchForMatches("line 15");
        assertThat(matches).isEmpty();
    }

    @Test
    public void singleMatchIsFoundProperly() {
        final Map<Position, String> matches = searchForMatches("line 7");

        assertThat(matches).hasSize(1);
        assertThat(matches.keySet()).containsOnly(new Position(42, 6));
        assertThat(matches.values()).containsOnly("line 7");
    }

    @Test
    public void multipleMatchesAreFoundProperly() {
        final Map<Position, String> matches = searchForMatches("line 1");

        assertThat(matches).hasSize(3);
        assertThat(matches.keySet()).containsOnly(new Position(0, 6), new Position(63, 6), new Position(71, 6));
        assertThat(matches.values()).containsOnly("line 1", "line 1", "line 1");
    }

    @Test
    public void regexMatchesAreFoundProperly() {
        final Map<Position, String> matches = searchForMatches("line \\d\\d");

        assertThat(matches).hasSize(2);
        assertThat(matches.keySet()).containsOnly(new Position(63, 7), new Position(71, 7));
        assertThat(matches.values()).containsOnly("line 10", "line 11");

    }

    private static Map<Position, String> searchForMatches(final String regex) {
        final Map<Position, String> matches = new HashMap<>();
        matchingEngine.searchForMatches(regex, new MatchAccess() {
            @Override
            public void onMatch(final String matchingContent, final Position matchPosition) {
                matches.put(matchPosition, matchingContent);
            }
        });
        return matches;
    }

}
