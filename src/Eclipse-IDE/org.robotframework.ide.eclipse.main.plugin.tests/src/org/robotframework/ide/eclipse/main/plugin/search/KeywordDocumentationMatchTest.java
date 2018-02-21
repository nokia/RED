/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.StyledString;
import org.junit.Test;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;

public class KeywordDocumentationMatchTest {

    @Test
    public void matchLabelIsProperlyHighlighted() {
        final KeywordSpecification keywordSpecification = new KeywordSpecification();
        keywordSpecification.setDocumentation("This is keyword documentation");

        final KeywordDocumentationMatch match = new KeywordDocumentationMatch(mock(IProject.class),
                mock(LibrarySpecification.class),
                keywordSpecification, 16, 4);

        final StyledString styledLabel = match.getStyledLabel();
        assertThat(styledLabel.getStyleRanges()).hasSize(1);
        assertThat(styledLabel.getStyleRanges()[0].start).isEqualTo(16);
        assertThat(styledLabel.getStyleRanges()[0].length).isEqualTo(4);
        assertThat(styledLabel.getStyleRanges()[0].background.getRGB())
                .isEqualTo(RedTheme.Colors.getEclipseSearchMatchColor().getRGB());
    }

}
