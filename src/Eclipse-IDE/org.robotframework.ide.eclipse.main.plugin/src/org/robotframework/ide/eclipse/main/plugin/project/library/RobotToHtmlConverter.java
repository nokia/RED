/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.library;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

class RobotToHtmlConverter {

    public String convert(final String robotStyleString) {
        final List<String> wrapped = Splitter.on('\n')
                .splitToList(robotStyleString)
                .stream()
                .map(this::escapeGtLtAmp)
                .map(this::wrapLineIntoTags)
                .collect(toList());

        final String consecutiveParagraphsJoined = Joiner.on('\n').join(joinParagraphsToLists(wrapped)).replaceAll(
                "</p>\n<p>", "\n");

        return Splitter.on('\n')
                .splitToList(consecutiveParagraphsJoined)
                .stream()
                .map(line -> isSpan(line) ? "<p>" + line + "</p>" : line)
                .collect(joining("\n"));
    }

    private String wrapLineIntoTags(final String line) {
        if (line.isEmpty()) {
            return line;
        } else if (line.startsWith("|") || line.startsWith(" |")) {
            return "<span font=\"monospace\">" + line + "</span>";
        } else if (line.startsWith("- ") || line.startsWith(" - ")) {
            return "<li>" + line.substring(2) + "</li>";
        } else if (line.startsWith("=") || line.startsWith(" =")) {
            return "<span font=\"header\" color=\"header\">"
                    + line.replaceAll("=", "").trim()
                    + "</span>";
        } else {
            return "<p>" + line + "</p>";
        }
    }

    private List<String> joinParagraphsToLists(final List<String> lines) {
        final List<String> result = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            final String prevLine = i == 0 ? null : result.get(i - 1);
            final String currLine = lines.get(i);

            if (isLi(prevLine) && isParagraph(currLine)) {
                final String last = result.remove(result.size() - 1);
                result.add(last.substring(0, last.length() - 5));
                result.add(currLine.substring(3, currLine.length() - 4) + "</li>");
            } else {
                result.add(currLine);
            }
        }
        return result;
    }

    private String escapeGtLtAmp(final String line) {
        return line.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    private boolean isParagraph(final String line) {
        return line != null && line.startsWith("<p>") && line.endsWith("</p>");
    }

    private boolean isSpan(final String line) {
        return line != null && line.startsWith("<span") && line.endsWith("</span>");
    }

    private boolean isLi(final String line) {
        return line != null && line.endsWith("</li>");
    }
}