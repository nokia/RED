/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.RGB;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;

public class DocumentationsFormatter {

    private static final Pattern HEADER_PATTERN = Pattern.compile("<h(\\d)>([\\w\\d _]+)</h\\d>");

    private final RobotRuntimeEnvironment env;

    public DocumentationsFormatter(final RobotRuntimeEnvironment env) {
        this.env = env;
    }

    public String format(final String header, final Documentation documentation) {
        final String doc = documentation.provideFormattedDocumentation(env);
        return writeHtml(header, doc);
    }

    private String writeHtml(final String header, final String doc) {
        return "<html>" + writeHead() + writeBody(header, doc) + "</html>";
    }

    private String writeHead() {
        final RGB bgRgb = RedTheme.Colors.getEclipseInfoBackgroundColor();

        return new StringBuilder().append("<head>")
                .append("<meta charset=\"utf-8\">")
                .append("<style>")
                .append("body {")
                .append("    background-color: rgb(" + bgRgb.red + "," + bgRgb.green + "," + bgRgb.blue + ");")
                .append("    font-size: small;")
                .append("    font-family: sans-serif;")
                .append("}")
                .append("code {")
                .append("    background-color: #eeeeee;")
                .append("}")
                .append("table, th, td {")
                .append("    border: 1px solid #a6a6a6;")
                .append("    border-collapse: collapse;")
                .append("    border-spacing: 2px;")
                .append("    padding: 4px;")
                .append("}")
                .append("</style>")
                .append("</head>")
                .toString();
    }

    private String writeBody(final String header, final String doc) {
        return "<body>" + header + identifyHeaders(doc) + "</body>";
    }

    private static String identifyHeaders(final String doc) {
        final Collection<String> headersIds = new HashSet<>();

        final Matcher matcher = HEADER_PATTERN.matcher(doc);

        int previousEnd = 0;
        final StringBuilder docBuilder = new StringBuilder();
        while (matcher.find()) {
            docBuilder.append(doc.substring(previousEnd, matcher.start()));

            final String hLevel = matcher.group(1);
            final String headerName = matcher.group(2);
            headersIds.add(headerName);

            docBuilder.append("<h" + hLevel + " id=\"" + headerName + "\">");
            docBuilder.append(headerName);
            docBuilder.append("</h" + hLevel + ">");

            previousEnd = matcher.end();
        }
        docBuilder.append(doc.substring(previousEnd, doc.length()));
        return createHyperlinks(docBuilder.toString(), headersIds);
    }

    private static String createHyperlinks(final String doc, final Collection<String> headersIds) {
        if (headersIds.isEmpty()) {
            return doc;
        }
        final String regex = headersIds.stream().map(name -> "`\\Q" + name + "\\E`").collect(joining("|"));
        final Matcher matcher = Pattern.compile(regex).matcher(doc);

        int previousEnd = 0;
        final StringBuilder docBuilder = new StringBuilder();
        while (matcher.find()) {
            docBuilder.append(doc.substring(previousEnd, matcher.start()));

            String name = matcher.group(0);
            name = name.substring(1, name.length() - 1); // cut off enclosing ` characters
            
            docBuilder.append("<a href=\"#" + name + "\">");
            docBuilder.append(name);
            docBuilder.append("</a>");
            
            previousEnd = matcher.end();
        }
        docBuilder.append(doc.substring(previousEnd, doc.length()));
        return docBuilder.toString();
    }
}
