/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import org.eclipse.swt.graphics.RGB;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;

public class DocumentationsFormatter {

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
        return "<body>" + header + doc + "</body>";
    }
}
