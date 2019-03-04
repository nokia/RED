/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;
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
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ElementAddingToken;

@RunWith(MockitoJUnitRunner.class)
public class RemoteLocationsLabelProviderTest {

    @Mock
    private RedProjectEditorInput editorInput;

    private RemoteLocationsLabelProvider provider;

    @Before
    public void before() {
        provider = new RemoteLocationsLabelProvider(editorInput);
    }

    @Test
    public void whenRemoteLocationWithoutProblemsIsGiven_remoteConnectedIsReturnedAsItsImage() {
        final RemoteLocation location = RemoteLocation.create("http://127.0.0.1:8270/");
        when(editorInput.getProblemsFor(location)).thenReturn(new ArrayList<>());
        assertThat(provider.getImage(location)).isSameAs(ImagesManager.getImage(RedImages.getRemoteConnectedImage()));
    }

    @Test
    public void whenRemoteLocationWithProblemsIsGiven_remoteDisconnectedIsReturnedAsItsImage() {
        final RemoteLocation location = RemoteLocation.create("http://127.0.0.1:8270/");
        when(editorInput.getProblemsFor(location))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.ERROR, "error details")));
        assertThat(provider.getImage(location))
                .isSameAs(ImagesManager.getImage(RedImages.getRemoteDisconnectedImage()));
    }

    @Test
    public void whenAddingTokenIsGiven_itsImageIsReturned() {
        final ElementAddingToken addingToken = new ElementAddingToken("new path", true);
        assertThat(provider.getImage(addingToken)).isSameAs(addingToken.getImage());
    }

    @Test
    public void whenRemoteLocationWithoutProblemsIsGiven_nonDecoratedLocationIsReturnedAsLabel() {
        final RemoteLocation location = RemoteLocation.create("http://127.0.0.1:8270/");
        when(editorInput.getProblemsFor(location)).thenReturn(new ArrayList<>());

        final StyledString styledLabel = provider.getStyledText(location);
        assertThat(styledLabel.getString()).isEqualTo("http://127.0.0.1:8270/ - Remote");
        assertThat(styledLabel.getStyleRanges()).containsExactly(new StyleRange(0, 22, null, null, SWT.NORMAL),
                new StyleRange(22, 9, RedTheme.Colors.getEclipseDecorationColor(), null, SWT.NORMAL));
    }

    @Test
    public void whenRemoteLocationWithProblemsIsGiven_errorDecoratedLocationIsReturnedAsLabel() {
        final RemoteLocation location = RemoteLocation.create("http://127.0.0.1:8270/");
        when(editorInput.getProblemsFor(location))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.ERROR, "error details")));

        final StyledString styledLabel = provider.getStyledText(location);
        assertThat(styledLabel.getString()).isEqualTo("http://127.0.0.1:8270/ - Remote");
        assertThat(styledLabel.getStyleRanges()).containsExactly(
                new StyleRange(0, 22, ColorsManager.getColor(255, 0, 0), null, SWT.NORMAL),
                new StyleRange(22, 9, RedTheme.Colors.getEclipseDecorationColor(), null, SWT.NORMAL));
    }

    @Test
    public void whenAddingTokenIsGiven_itsStyledLabelIsReturned() {
        final ElementAddingToken addingToken = new ElementAddingToken("new path", true);
        final StyledString expected = addingToken.getStyledText();
        final StyledString actual = provider.getStyledText(addingToken);

        assertThat(actual.getString()).isEqualTo(expected.getString());
        assertThat(actual.getStyleRanges()).isEqualTo(expected.getStyleRanges());
    }

    @Test
    public void whenAddingTokenIsGiven_nonTooltipTextIsReturned() {
        final ElementAddingToken addingToken = new ElementAddingToken("new path", true);

        assertThat(provider.getToolTipText(addingToken)).isNull();
    }

    @Test
    public void whenRemoteLocationWithProblemsIsGiven_tooltipTextWithAllProblemDescriptionsIsReturned() {
        final RemoteLocation location = RemoteLocation.create("http://127.0.0.1:8270/");
        when(editorInput.getProblemsFor(location))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.ERROR, "error details"),
                        new RedXmlProblem(Severity.ERROR, "other error details")));

        assertThat(provider.getToolTipText(location))
                .isEqualTo(String.join("\n", newArrayList("error details", "other error details")));
    }

    @Test
    public void whenRemoteLocationWithoutProblemsIsGiven_nonTooltipTextIsReturned() {
        final RemoteLocation location = RemoteLocation.create("http://127.0.0.1:8270/");
        when(editorInput.getProblemsFor(location)).thenReturn(new ArrayList<>());

        assertThat(provider.getToolTipText(location)).isNull();
    }

    @Test
    public void whenRemoteLocationWithProblemsIsGiven_errorTooltipImageIsReturned() {
        final RemoteLocation location = RemoteLocation.create("http://127.0.0.1:8270/");
        when(editorInput.getProblemsFor(location))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.ERROR, "error details")));

        assertThat(provider.getToolTipImage(location)).isSameAs(ImagesManager.getImage(RedImages.getErrorImage()));
    }

    @Test
    public void whenRemoteLocationWithoutProblemsIsGiven_nonTooltipImageIsReturned() {
        final RemoteLocation location = RemoteLocation.create("http://127.0.0.1:8270/");
        when(editorInput.getProblemsFor(location)).thenReturn(new ArrayList<>());

        assertThat(provider.getToolTipImage(location)).isNull();
    }
}
