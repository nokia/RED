/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static java.util.stream.Collectors.joining;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.RGB;
import org.rf.ide.core.RedURI;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;

public class DocumentationsFormatter {

    private static final Pattern HEADER_PATTERN = Pattern.compile("<h(\\d)>([\\w\\d _]+)</h\\d>");

    private final RobotRuntimeEnvironment env;

    public DocumentationsFormatter(final RobotRuntimeEnvironment env) {
        this.env = env;
    }

    public static String createEmpty() {
        return create(() -> "");
    }

    public static String create(final Supplier<String> bodySupplier) {
        return "<!DOCTYPE html>\n<html>" + writeHead() + "<body>" + bodySupplier.get() + "</body></html>";
    }

    public static String createError(final String error, final String uri) {
        final Optional<URI> errorImgUri = RedImages.getBigErrorImageUri();
        final String errorHtml = Formatters.bold(error + ": ")
                + "<span style=\"border: none; font-family: monospace; font-size: 1.1em;\">" + uri + "</span>";
        return "<!DOCTYPE html>\n<html>" + writeHead() + "<body>" + Formatters.errorMessage(errorImgUri, errorHtml)
                + "</body></html>";
    }

    public String format(final Documentation documentation) {
        return format("", documentation, "", name -> "#");
    }

    public String format(final String header, final Documentation documentation,
            final Function<String, String> localKeywordsLinker) {
        return format(header, documentation, "", localKeywordsLinker);
    }

    public String format(final String header, final Documentation documentation, final String footer,
            final Function<String, String> localKeywordsLinker) {

        final String doc = documentation.provideFormattedDocumentation(env);
        final Collection<String> localSymbols = documentation.getLocalSymbols();
        return writeHtml(header, doc + footer, localSymbols, localKeywordsLinker);
    }

    private String writeHtml(final String header, final String doc, final Collection<String> localSymbols,
            final Function<String, String> localKeywordsLinker) {
        return "<html>" + writeHead() + writeBody(header, doc, localSymbols, localKeywordsLinker) + "</html>";
    }

    private static String writeHead() {
        final RGB bgRgb = RedTheme.Colors.getEclipseInfoBackgroundColor();
        final RGB fgRgb = RedTheme.Colors.getTableBodyForegroundColor().getRGB();

        return new StringBuilder().append("<head>")
                .append("<meta charset=\"utf-8\">")
                .append("<style>")
                .append("body {")
                .append("    color: rgb(" + fgRgb.red + "," + fgRgb.green + "," + fgRgb.blue + ");")
                .append("    background-color: rgb(" + bgRgb.red + "," + bgRgb.green + "," + bgRgb.blue + ");")
                .append("    font-size: small;")
                .append("    font-family: sans-serif;")
                .append("}")
                .append("code {")
                .append("    background-color: rgba(220, 220, 220, 0.5);")
                .append("    font-size: 1.1em;")
                .append("}")
                .append("a, a:link, a:visited {")
                .append("    color: #c30;")
                .append("}")
                .append("a:hover, a:active {")
                .append("    text-decoration: underline;")
                .append("    color: black;")
                .append("}")
                .append("a:hover {")
                .append("   text-decoration: underline !important;")
                .append("}")
                .append("pre {")
                .append("    margin-left: 0.7em;")
                .append("    background-color: rgba(220, 220, 220, 0.5);")
                .append("}")
                .append("table, th, td {")
                .append("    border: 1px solid #a6a6a6;")
                .append("    border-collapse: collapse;")
                .append("    border-spacing: 2px;")
                .append("    padding: 4px;")
                .append("    font-size: 0.9em;")
                .append("}")
                .append("</style>")
                .append("</head>")
                .toString();
    }

    private String writeBody(final String header, final String doc, final Collection<String> localSymbols,
            final Function<String, String> localKeywordsLinker) {
        final String localLinksEnabledDoc = createHyperlinks(doc, localSymbols,
                name -> "<a href=\"" + localKeywordsLinker.apply(name) + "\">" + name + "</a>");

        return "<body>" + header + identifyHeaders(localLinksEnabledDoc) + "</body>";
    }

    private static String identifyHeaders(final String doc) {
        final Map<String, String> headersIds = new HashMap<>();
        // we do not have Keywords header, but Shortcuts header can be used instead
        headersIds.put("Keywords", "Shortcuts");
        headersIds.put("keywords", "Shortcuts");

        final Matcher matcher = HEADER_PATTERN.matcher(doc);

        int previousEnd = 0;
        final StringBuilder docBuilder = new StringBuilder();
        while (matcher.find()) {
            docBuilder.append(doc.substring(previousEnd, matcher.start()));

            final String hLevel = matcher.group(1);
            final String headerName = matcher.group(2);
            headersIds.put(headerName, headerName);
            headersIds.put(headerName.toLowerCase(), headerName.toLowerCase());

            docBuilder.append("<h" + hLevel + " id=\"" + headerName + "\">");
            docBuilder.append(headerName);
            docBuilder.append("</h" + hLevel + ">");

            previousEnd = matcher.end();
        }
        docBuilder.append(doc.substring(previousEnd, doc.length()));
        return createHyperlinks(docBuilder.toString(), headersIds.keySet(), name -> "<a href=\"#"
                + RedURI.URI_SPECIAL_CHARS_ESCAPER.escape(headersIds.get(name)) + "\">" + name + "</a>");
    }

    private static String createHyperlinks(final String doc, final Collection<String> symbols,
            final Function<String, String> transformation) {
        if (symbols.isEmpty()) {
            return doc;
        }
        final String regex = symbols.stream().map(name -> "`\\Q" + name + "\\E`").collect(joining("|"));
        final Matcher matcher = Pattern.compile(regex).matcher(doc);

        int previousEnd = 0;
        final StringBuilder docBuilder = new StringBuilder();
        while (matcher.find()) {
            docBuilder.append(doc.substring(previousEnd, matcher.start()));

            String name = matcher.group(0);
            name = name.substring(1, name.length() - 1); // cut off enclosing ` characters

            docBuilder.append(transformation.apply(name));
            previousEnd = matcher.end();
        }
        docBuilder.append(doc.substring(previousEnd, doc.length()));
        return docBuilder.toString();
    }
}
