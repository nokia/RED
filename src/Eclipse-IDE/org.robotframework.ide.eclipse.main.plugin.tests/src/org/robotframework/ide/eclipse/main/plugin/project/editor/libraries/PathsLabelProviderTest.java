/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.viewers.ElementAddingToken;

@RunWith(MockitoJUnitRunner.class)
public class PathsLabelProviderTest {

    @Mock
    private RedProjectEditorInput editorInput;

    private PathsLabelProvider provider;

    @Before
    public void before() {
        provider = new PathsLabelProvider("VAR", editorInput);
    }

    @Test
    public void whenSearchPathIsGiven_nullIsReturnedAsItsImage() {
        assertThat(provider.getImage(SearchPath.create("path", true))).isNull();
        assertThat(provider.getImage(SearchPath.create("path", false))).isNull();
    }

    @Test
    public void whenAddingTokenIsGiven_itsImageIsReturned() {
        final ElementAddingToken addingToken = new ElementAddingToken("new path", true);
        assertThat(provider.getImage(addingToken)).isSameAs(addingToken.getImage());
    }

    @Test
    public void whenCustomSearchPathIsGiven_onlyNonDecoratedPathIsReturnedAsLabel() {
        final SearchPath searchPath = SearchPath.create("path");
        when(editorInput.getProblemsFor(searchPath)).thenReturn(new ArrayList<RedXmlProblem>());

        final StyledString styledLabel = provider.getStyledText(searchPath);
        assertThat(styledLabel.getString()).isEqualTo("path");
        assertThat(styledLabel.getStyleRanges()).containsExactly(new StyleRange(0, 4, null, null, SWT.NORMAL));
    }

    @Test
    public void whenSystemPathIsGiven_decoratedPathIsReturnedAsLabel() {
        final StyledString styledLabel = provider.getStyledText(SearchPath.create("path", true));
        assertThat(styledLabel.getString()).isEqualTo("path [already defined in VAR variable]");
        assertThat(styledLabel.getStyleRanges()).hasSize(2);

        assertThat(styledLabel.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(styledLabel.getStyleRanges()[0].length).isEqualTo(4);
        assertThat(styledLabel.getStyleRanges()[0].foreground).isEqualTo(ColorsManager.getColor(150, 150, 150));

        assertThat(styledLabel.getStyleRanges()[1].start).isEqualTo(4);
        assertThat(styledLabel.getStyleRanges()[1].length).isEqualTo(34);
        assertThat(styledLabel.getStyleRanges()[1].foreground).isEqualTo(RedTheme.Colors.getEclipseDecorationColor());
    }

    @Test
    public void whenAddingTokenIsGiven_itsStyledLabelIsReturned() {
        final ElementAddingToken addingToken = new ElementAddingToken("new path", true);
        final StyledString expected = addingToken.getStyledText();
        final StyledString actual = provider.getStyledText(addingToken);

        assertThat(actual.getString()).isEqualTo(expected.getString());
        assertThat(actual.getStyleRanges()).isEqualTo(expected.getStyleRanges());
    }

}
