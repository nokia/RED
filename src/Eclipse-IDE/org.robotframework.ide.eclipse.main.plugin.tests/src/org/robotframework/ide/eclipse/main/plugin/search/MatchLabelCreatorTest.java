/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.StyledString;
import org.junit.Test;

public class MatchLabelCreatorTest {

    private final MatchLabelCreator creator = new MatchLabelCreator();

    @Test
    public void singleLineMatchIsHighlighted() {
        final StyledString label = creator.create("single line", new Position(2, 3));

        assertThat(label.getString()).isEqualTo("single line");
        assertThat(label.getStyleRanges()).hasSize(1);
        assertThat(label.getStyleRanges()[0].start).isEqualTo(2);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(3);
    }

    @Test
    public void multilineMatchIsHighlighted_1() {
        final StyledString label = creator.create("1st line\n2nd line\n3rd line", new Position(10, 2));

        assertThat(label.getString()).isEqualTo("2nd line");
        assertThat(label.getStyleRanges()).hasSize(1);
        assertThat(label.getStyleRanges()[0].start).isEqualTo(1);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(2);
    }

    @Test
    public void multilineMatchIsHighlighted_2() {
        final StyledString label = creator.create("1st line\r2nd line\r\n3rd line", new Position(10, 2));

        assertThat(label.getString()).isEqualTo("2nd line");
        assertThat(label.getStyleRanges()).hasSize(1);
        assertThat(label.getStyleRanges()[0].start).isEqualTo(1);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(2);
    }
}
