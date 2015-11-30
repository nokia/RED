/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Sets.newHashSet;

import java.util.Set;

/**
 * @author Michal Anglart
 *
 */
public class GherkinStyleUtilities {

    private static final Set<String> PREFIXES = newHashSet("given", "when", "and", "but", "then");

    public static boolean isGherkinStylePrefix(final String prefix) {
        return PREFIXES.contains(prefix);
    }

    public static String removeGherkinPrefix(final String name) {
        for (final String prefix : PREFIXES) {
            if (name.toLowerCase().startsWith(prefix)) {
                return name.substring(prefix.length()).trim();
            }
        }
        return name;
    }
}
