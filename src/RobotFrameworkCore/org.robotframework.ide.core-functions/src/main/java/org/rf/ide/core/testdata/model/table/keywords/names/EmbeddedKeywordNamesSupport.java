/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.keywords.names;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Michal Anglart
 */
public class EmbeddedKeywordNamesSupport {

    private static final Pattern VARIABLE_MATCHER = Pattern.compile("\\$\\{([^\\}]+)\\}");

    public static boolean matches(final String definitionName, final QualifiedKeywordName occurrenceQualifiedName) {
        return matchesWithLowerCase(definitionName.toLowerCase(), occurrenceQualifiedName.getKeywordName()
                .toLowerCase(), occurrenceQualifiedName.getEmbeddedKeywordName().toLowerCase());
    }

    public static boolean startsWith(final String definitionName, final String potentialUsageName) {
        return startsWithLowerCase(definitionName.toLowerCase(), potentialUsageName.toLowerCase());
    }

    private static boolean startsWithLowerCase(final String definitionName, final String potentialUsageNamePrefix) {
        if (definitionName.startsWith(potentialUsageNamePrefix)) {
            return true;
        }
        // TODO implement
        return false;
    }

    private static boolean matchesWithLowerCase(final String definitionName, final String occurrenceNameInNormalForm,
            final String occurrenceNameInEmbeddedForm) {
        if (definitionName.equals(occurrenceNameInNormalForm)) {
            return true;
        }
        if (definitionName.indexOf('$') == -1) {
            return false;
        }
        final String regex = "^" + substituteVariablesWithRegex(definitionName) + "$";
        try {
            return Pattern.matches(regex, occurrenceNameInEmbeddedForm);
        } catch (final PatternSyntaxException e) {
            return false;
        }
    }

    private static String substituteVariablesWithRegex(final String definitionName) {
        final Matcher matcher = VARIABLE_MATCHER.matcher(definitionName);

        final StringBuilder wholeRegex = new StringBuilder();

        int previousIndex = 0;
        while (matcher.find()) {
            final String exactPatternToMatch = Pattern.quote(definitionName.substring(previousIndex, matcher.start()));
            wholeRegex.append(exactPatternToMatch);

            final String varContent = definitionName.substring(matcher.start() + 2, matcher.end() - 1);
            final String internalRegex = varContent.indexOf(':') != -1
                    ? varContent.substring(varContent.lastIndexOf(':') + 1) : ".+";

            wholeRegex.append(internalRegex);
            previousIndex = matcher.end();
        }
        wholeRegex.append(Pattern.quote(definitionName.substring(previousIndex)));

        return wholeRegex.toString();
    }
    
    public static boolean hasEmbeddedArguments(final String definitionName) {
        return VARIABLE_MATCHER.matcher(definitionName).find();
    }

}
