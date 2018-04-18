/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Optional;

import org.junit.Test;


public class FormattersTest {

    @Test
    public void givenTextIsProperlyInsertedIntoTitleOfGivenLevel() {
        assertThat(Formatters.title("title", 1)).isEqualTo("<h1>title</h1>");
        assertThat(Formatters.title("abc", 2)).isEqualTo("<h2>abc</h2>");
        assertThat(Formatters.title("xyz", 5)).isEqualTo("<h5>xyz</h5>");
    }

    @Test
    public void givenTextIsProperlyInsertedIntoParagraph() {
        assertThat(Formatters.paragraph("paragraph")).isEqualTo("<p>paragraph</p>");
        assertThat(Formatters.paragraph("multiline\nparagraph")).isEqualTo("<p>multiline\nparagraph</p>");
    }

    @Test
    public void givenHrefIsProperlyInsertedIntoAnchor() {
        assertThat(Formatters.hyperlink("#", "lbl")).isEqualTo("<a href=\"#\">lbl</a>");
        assertThat(Formatters.hyperlink(URI.create("http://www.robotframework.org"), "robot"))
                .isEqualTo("<a href=\"http://www.robotframework.org\">robot</a>");
    }

    @Test
    public void simpleHeaderIsMadeOfTitleAndSpans() {
        assertThat(Formatters.simpleHeader(Optional.empty(), "Header")).isEqualTo("<h3>Header</h3>");
        assertThat(Formatters.simpleHeader(Optional.of(URI.create("file:///image.png")), "Header"))
                .isEqualTo("<h3><img style=\"vertical-align: top;\" src=\"file:///image.png\"/> Header</h3>");

        assertThat(Formatters.simpleHeader(Optional.empty(), "Header", newArrayList("a", "b")))
                .isEqualTo("<h3>Header</h3><p><b>a: </b><span style=\"font-family: monospace;\">b</span><br/></p>");
        assertThat(Formatters.simpleHeader(Optional.of(URI.create("file:///image.png")), "Header",
                newArrayList("a", "b"), newArrayList(), newArrayList("c", "d")))
                        .isEqualTo("<h3><img style=\"vertical-align: top;\" src=\"file:///image.png\"/> Header</h3>"
                                + "<p><b>a: </b><span style=\"font-family: monospace;\">b</span><br/>"
                                + "<b>c: </b><span style=\"font-family: monospace;\">d</span><br/>"
                                + "</p>");
    }

}
