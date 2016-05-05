/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import java.util.regex.Pattern;

/**
 * Class for simplified search regexes (where * and ? are possible as any string or any character
 * respectively). This object micro-caches the compiled pattern in order to be quickly re-use during
 * search.
 * 
 * @author Michal Anglart
 */
public final class SearchPattern {

    private String pattern;

    private Pattern compiledPattern;

    public SearchPattern(final String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(final String pattern) {
        this.pattern = pattern;
        this.compiledPattern = null;
    }

    public Pattern buildPattern() {
        if (compiledPattern == null) {
            final StringBuilder translatedPattern = new StringBuilder();
            StringBuilder currentFragment = new StringBuilder();
            
            for (final char character : pattern.toCharArray()) {
                if (character == '*') {
                    translatedPattern.append(Pattern.quote(currentFragment.toString()));
                    translatedPattern.append(".*");
                    
                    currentFragment = new StringBuilder();
                } else if (character == '?') {
                    translatedPattern.append(Pattern.quote(currentFragment.toString()));
                    translatedPattern.append(".");
                    
                    currentFragment = new StringBuilder();
                } else {
                    currentFragment.append(character);
                }
            }
            
            if (currentFragment.length() > 0) {
                translatedPattern.append(Pattern.quote(currentFragment.toString()));
            }
            compiledPattern = Pattern.compile(translatedPattern.toString());
        }
        return compiledPattern;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == SearchPattern.class) {
            final SearchPattern that = (SearchPattern) obj;
            return this.pattern.equals(that.pattern);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }
}
