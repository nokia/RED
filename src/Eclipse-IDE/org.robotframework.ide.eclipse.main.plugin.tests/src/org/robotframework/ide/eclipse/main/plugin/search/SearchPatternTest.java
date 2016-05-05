/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class SearchPatternTest {

    @Test
    public void whenPatternIsBuilded_itIsMicrocached() {
        final SearchPattern pattern = new SearchPattern("pattern");

        final Pattern p1 = pattern.buildPattern();
        final Pattern p2 = pattern.buildPattern();

        pattern.setPattern("otherpattern");

        final Pattern p3 = pattern.buildPattern();

        assertThat(p1).isSameAs(p2);
        assertThat(p1).isNotSameAs(p3);
    }

    @Test
    public void simplifiedRegexesAreMatchingProperly() {
        assertThat(createMatcher("abc?2", "abc12").matches()).isTrue();
        assertThat(createMatcher("abc?2", "abc22").matches()).isTrue();
        assertThat(createMatcher("abc?2", "abc112").matches()).isFalse();

        assertThat(createMatcher("abc*2", "abc12").matches()).isTrue();
        assertThat(createMatcher("abc*2", "abc22").matches()).isTrue();
        assertThat(createMatcher("abc*2", "abc112").matches()).isTrue();

        assertThat(createMatcher("abc+*0+x", "abc+0+x").matches()).isTrue();
        assertThat(createMatcher("abc+*0+x", "abc+100+x").matches()).isTrue();
        assertThat(createMatcher("abc+*0+x", "abc+0+y").matches()).isFalse();
    }

    private static Matcher createMatcher(final String simpleRegex, final String input) {
        return new SearchPattern(simpleRegex).buildPattern().matcher(input);
    }

}
