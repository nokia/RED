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
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ElementAddingToken;

@RunWith(MockitoJUnitRunner.class)
public class ReferencedLibrariesLabelProviderTest {

    @Mock
    private RedProjectEditorInput editorInput;

    private ReferencedLibrariesLabelProvider provider;

    @Before
    public void before() {
        provider = new ReferencedLibrariesLabelProvider(editorInput);
    }

    @Test
    public void whenPythonReferencedLibraryWithoutProblemsIsGiven_pythonLibImageIsReturnedAsItsImage() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "/some/path.py");
        when(editorInput.getProblemsFor(library)).thenReturn(new ArrayList<>());
        assertThat(provider.getImage(library)).isSameAs(ImagesManager.getImage(RedImages.getPythonLibraryImage()));
    }

    @Test
    public void whenJavaReferencedLibraryWithoutProblemsIsGiven_javaLibImageIsReturnedAsItsImage() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.JAVA, "JavaLib", "/some/path.jar");
        when(editorInput.getProblemsFor(library)).thenReturn(new ArrayList<>());
        assertThat(provider.getImage(library)).isSameAs(ImagesManager.getImage(RedImages.getJavaLibraryImage()));
    }

    @Test
    public void whenVirtualReferencedLibraryWithoutProblemsIsGiven_virtualLibImageIsReturnedAsItsImage() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.VIRTUAL, "XmlLib", "/some/path.xml");
        when(editorInput.getProblemsFor(library)).thenReturn(new ArrayList<>());
        assertThat(provider.getImage(library)).isSameAs(ImagesManager.getImage(RedImages.getVirtualLibraryImage()));
    }

    @Test
    public void whenReferencedLibraryWithErrorIsGiven_errorLibImageIsReturnedAsItsImage() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "/some/path/PyLib.py");
        when(editorInput.getProblemsFor(library))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.ERROR, "error details")));
        assertThat(provider.getImage(library)).isSameAs(ImagesManager.getImage(RedImages.getRobotLibraryErrorImage()));
    }

    @Test
    public void whenReferencedLibraryWithWarningIsGiven_warningLibImageIsReturnedAsItsImage() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "/some/path/PyLib.py");
        when(editorInput.getProblemsFor(library))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.WARNING, "warning details")));
        assertThat(provider.getImage(library)).isSameAs(ImagesManager.getImage(RedImages.getRobotLibraryWarnImage()));
    }

    @Test
    public void whenAddingTokenIsGiven_itsImageIsReturned() {
        final ElementAddingToken addingToken = new ElementAddingToken("new path", true);
        assertThat(provider.getImage(addingToken)).isSameAs(addingToken.getImage());
    }

    @Test
    public void whenReferencedLibraryWithoutProblemsIsGiven_nonDecoratedLibNameIsReturnedAsLabel() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "/some/path/PyLib.py");
        when(editorInput.getProblemsFor(library)).thenReturn(new ArrayList<>());

        final StyledString styledLabel = provider.getStyledText(library);
        assertThat(styledLabel.getString()).isEqualTo("PyLib - /some/path/PyLib.py");
        assertThat(styledLabel.getStyleRanges()).containsExactly(new StyleRange(0, 5, null, null, SWT.NORMAL),
                new StyleRange(5, 22, RedTheme.Colors.getEclipseDecorationColor(), null, SWT.NORMAL));
    }

    @Test
    public void whenReferencedLibraryWithErrorIsGiven_errorDecoratedLibNameIsReturnedAsLabel() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "/some/path/PyLib.py");
        when(editorInput.getProblemsFor(library))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.ERROR, "error details")));

        final StyledString styledLabel = provider.getStyledText(library);
        assertThat(styledLabel.getString()).isEqualTo("PyLib - /some/path/PyLib.py");
        assertThat(styledLabel.getStyleRanges()).containsExactly(
                new StyleRange(0, 5, ColorsManager.getColor(255, 0, 0), null, SWT.NORMAL),
                new StyleRange(5, 22, RedTheme.Colors.getEclipseDecorationColor(), null, SWT.NORMAL));
    }

    @Test
    public void whenReferencedLibraryWithWarningIsGiven_warningDecoratedLibNameIsReturnedAsLabel() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "/some/path/PyLib.py");
        when(editorInput.getProblemsFor(library))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.WARNING, "warning details")));

        final StyledString styledLabel = provider.getStyledText(library);
        assertThat(styledLabel.getString()).isEqualTo("PyLib - /some/path/PyLib.py");
        assertThat(styledLabel.getStyleRanges()).containsExactly(
                new StyleRange(0, 5, ColorsManager.getColor(255, 165, 0), null, SWT.NORMAL),
                new StyleRange(5, 22, RedTheme.Colors.getEclipseDecorationColor(), null, SWT.NORMAL));
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
    public void whenReferencedLibraryWithProblemsIsGiven_tooltipTextWithAllProblemDescriptionsIsReturned() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "/some/path/PyLib.py");
        when(editorInput.getProblemsFor(library))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.ERROR, "error details"),
                        new RedXmlProblem(Severity.ERROR, "other error details"),
                        new RedXmlProblem(Severity.WARNING, "warning details")));

        assertThat(provider.getToolTipText(library))
                .isEqualTo(String.join("\n", newArrayList("error details", "other error details", "warning details")));
    }

    @Test
    public void whenReferencedLibraryWithoutProblemsIsGiven_nonTooltipTextIsReturned() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "/some/path/PyLib.py");
        when(editorInput.getProblemsFor(library)).thenReturn(new ArrayList<>());

        assertThat(provider.getToolTipText(library)).isNull();
    }

    @Test
    public void whenReferencedLibraryWithErrorIsGiven_errorTooltipImageIsReturned() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "/some/path/PyLib.py");
        when(editorInput.getProblemsFor(library))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.ERROR, "error details")));

        assertThat(provider.getToolTipImage(library)).isSameAs(ImagesManager.getImage(RedImages.getErrorImage()));
    }

    @Test
    public void whenReferencedLibraryWithWarningIsGiven_warningTooltipImageIsReturned() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "/some/path/PyLib.py");
        when(editorInput.getProblemsFor(library))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.WARNING, "warning details")));

        assertThat(provider.getToolTipImage(library)).isSameAs(ImagesManager.getImage(RedImages.getWarningImage()));
    }

    @Test
    public void whenReferencedLibraryWithoutProblemsIsGiven_nonTooltipImageIsReturned() {
        final ReferencedLibrary library = ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "/some/path/PyLib.py");
        when(editorInput.getProblemsFor(library)).thenReturn(new ArrayList<>());

        assertThat(provider.getToolTipImage(library)).isNull();
    }
}
