/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ElementAddingToken;

public class VariableFilesLabelProviderTest {

    private RedProjectEditorInput editorInput;

    private VariableFilesLabelProvider provider;

    @BeforeEach
    public void before() {
        editorInput = mock(RedProjectEditorInput.class);
        provider = new VariableFilesLabelProvider(editorInput);
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
    public void whenVariableFileWithoutProblemsIsGiven_nonDecoratedPathIsReturnedAsLabel() {
        final ReferencedVariableFile varFile = ReferencedVariableFile.create("path/to/file.py");
        when(editorInput.getProblemsFor(varFile)).thenReturn(new ArrayList<>());

        final StyledString styledLabel = provider.getStyledText(varFile);
        assertThat(styledLabel.getString()).isEqualTo("path/to/file.py");
        assertThat(styledLabel.getStyleRanges()).containsExactly(new StyleRange(0, 15, null, null, SWT.NORMAL));
    }

    @Test
    public void whenVariableFileWithErrorIsGiven_errorDecoratedPathIsReturnedAsLabel() {
        final ReferencedVariableFile varFile = ReferencedVariableFile.create("path/to/file.py");
        when(editorInput.getProblemsFor(varFile))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.ERROR, "error details")));

        final StyledString styledLabel = provider.getStyledText(varFile);
        assertThat(styledLabel.getString()).isEqualTo("path/to/file.py");
        assertThat(styledLabel.getStyleRanges())
                .containsExactly(new StyleRange(0, 15, ColorsManager.getColor(255, 0, 0), null, SWT.NORMAL));
    }

    @Test
    public void whenVariableFileWithWarningIsGiven_warningDecoratedPathIsReturnedAsLabel() {
        final ReferencedVariableFile varFile = ReferencedVariableFile.create("path/to/file.py");
        when(editorInput.getProblemsFor(varFile))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.WARNING, "warning details")));

        final StyledString styledLabel = provider.getStyledText(varFile);
        assertThat(styledLabel.getString()).isEqualTo("path/to/file.py");
        assertThat(styledLabel.getStyleRanges())
                .containsExactly(new StyleRange(0, 15, ColorsManager.getColor(255, 165, 0), null, SWT.NORMAL));
    }

    @Test
    public void whenAddingTokenIsGiven_itsImageIsReturned() {
        final ElementAddingToken addingToken = new ElementAddingToken("new file", true);

        assertThat(provider.getImage(addingToken)).isSameAs(addingToken.getImage());
    }

    @Test
    public void whenVariableFileWithoutProblemsIsGiven_robotScalarVariableImageIsReturned() {
        final ReferencedVariableFile varFile = ReferencedVariableFile.create("path");
        when(editorInput.getProblemsFor(varFile)).thenReturn(new ArrayList<>());

        assertThat(provider.getImage(varFile))
                .isSameAs(ImagesManager.getImage(RedImages.getRobotScalarVariableImage()));
    }

    @Test
    public void whenVariableFileWithErrorIsGiven_robotUnknownVariableImageIsReturned() {
        final ReferencedVariableFile varFile = ReferencedVariableFile.create("path");
        when(editorInput.getProblemsFor(varFile))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.ERROR, "error details")));

        assertThat(provider.getImage(varFile))
                .isSameAs(ImagesManager.getImage(RedImages.getRobotUnknownVariableImage()));
    }

    @Test
    public void whenVariableFileWithWarningIsGiven_robotWarnedVariableImageIsReturned() {
        final ReferencedVariableFile varFile = ReferencedVariableFile.create("path");
        when(editorInput.getProblemsFor(varFile))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.WARNING, "warning details")));

        assertThat(provider.getImage(varFile))
                .isSameAs(ImagesManager.getImage(RedImages.getRobotWarnedVariableImage()));
    }

    @Test
    public void whenAddingTokenIsGiven_nonTooltipTextIsReturned() {
        final ElementAddingToken addingToken = new ElementAddingToken("new file", true);

        assertThat(provider.getToolTipText(addingToken)).isNull();
    }

    @Test
    public void whenVariableFileWithoutProblemsIsGiven_nonTooltipTextIsReturned() {
        final ReferencedVariableFile varFile = ReferencedVariableFile.create("path");
        when(editorInput.getProblemsFor(varFile)).thenReturn(new ArrayList<>());

        assertThat(provider.getToolTipText(varFile)).isNull();
    }

    @Test
    public void whenVariableFileWithProblemsIsGiven_tooltipTextWithAllProblemDescriptionsIsReturned() {
        final ReferencedVariableFile varFile = ReferencedVariableFile.create("path");
        when(editorInput.getProblemsFor(varFile))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.ERROR, "error details"),
                        new RedXmlProblem(Severity.ERROR, "other error details"),
                        new RedXmlProblem(Severity.WARNING, "warning details")));

        assertThat(provider.getToolTipText(varFile))
                .isEqualTo(String.join("\n", newArrayList("error details", "other error details", "warning details")));
    }

    @Test
    public void whenVariableFileWithoutProblemsIsGiven_nonTooltipImageIsReturned() {
        final ReferencedVariableFile varFile = ReferencedVariableFile.create("path");
        when(editorInput.getProblemsFor(varFile)).thenReturn(new ArrayList<>());

        assertThat(provider.getToolTipImage(varFile)).isNull();
    }

    @Test
    public void whenVariableFileWithErrorIsGiven_errorTooltipImageIsReturned() {
        final ReferencedVariableFile varFile = ReferencedVariableFile.create("path");
        when(editorInput.getProblemsFor(varFile))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.ERROR, "error details")));

        assertThat(provider.getToolTipImage(varFile)).isSameAs(ImagesManager.getImage(RedImages.getErrorImage()));
    }

    @Test
    public void whenVariableFileWithWarningIsGiven_warningTooltipImageIsReturned() {
        final ReferencedVariableFile varFile = ReferencedVariableFile.create("path");
        when(editorInput.getProblemsFor(varFile))
                .thenReturn(newArrayList(new RedXmlProblem(Severity.WARNING, "warning details")));

        assertThat(provider.getToolTipImage(varFile)).isSameAs(ImagesManager.getImage(RedImages.getWarningImage()));
    }

}
