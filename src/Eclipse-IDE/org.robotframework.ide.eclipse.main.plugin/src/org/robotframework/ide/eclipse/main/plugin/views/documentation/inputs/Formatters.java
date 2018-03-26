/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static java.util.stream.Collectors.joining;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class Formatters {

    static String formatHyperlink(final URI href, final String label) {
        return formatHyperlink(href.toString(), label);
    }

    static String formatHyperlink(final String href, final String label) {
        return "<a href=\"" + href + "\">" + label + "</a>";
    }

    @SafeVarargs
    static String formatSimpleHeader(final Optional<URI> imgUri, final String title,
            final List<String>... simpleTable) {
        return formatSimpleHeader(imgUri, title, Arrays.asList(simpleTable));
    }

    static String formatSimpleHeader(final Optional<URI> imgUri, final String title,
            final List<List<String>> simpleTable) {
        
        final StringBuilder builder = new StringBuilder();
        builder.append("<h3>");
        imgUri.ifPresent(
                uri -> builder.append("<img style=\"vertical-align: top;\" src=\"" + uri.toString() + "\"/> "));
        builder.append(title);
        builder.append("</h3>");

        builder.append("<p>");
        for (final List<String> row : simpleTable) {
            if (row.isEmpty()) {
                continue;
            }
            final String rowTitle = row.get(0);
            final String restOfRow = row.subList(1, row.size()).stream().collect(joining(" "));

            builder.append("<b>" + rowTitle + ": </b>");
            builder.append("<span style=\"font-family: monospace;\">" + restOfRow + "</span>");
            builder.append("<br/>");
        }
        return builder.toString();

    }

}
