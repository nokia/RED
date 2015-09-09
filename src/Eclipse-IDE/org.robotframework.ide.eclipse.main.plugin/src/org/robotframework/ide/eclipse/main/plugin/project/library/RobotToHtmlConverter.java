/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.library;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

class RobotToHtmlConverter {

    public String convert(final String robotStyleString) {
        final List<String> splitted1 = Splitter.on('\n').splitToList(robotStyleString);
        final List<String> wrapped = Lists.transform(splitted1, new Function<String, String>() {
            @Override
            public String apply(final String line) {
                final String escaped = escapeGtLtAmp(line);
                return wrapLineIntoTags(escaped);
            }
        });
        final String consecutiveParagraphsJoined = Joiner.on('\n').join(joinPargraphsToLists(wrapped))
                .replaceAll("</p>\n<p>", "\n");

        final List<String> splitted2 = Splitter.on('\n').splitToList(consecutiveParagraphsJoined);
        final List<String> spansWrappedIntoParagraphs = Lists.transform(splitted2, new Function<String, String>() {
            @Override
            public String apply(final String line) {
                return isSpan(line) ? "<p>" + line + "</p>" : line;
            }
        });
        return Joiner.on('\n').join(spansWrappedIntoParagraphs);
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

    private List<String> joinPargraphsToLists(final List<String> lines) {
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