/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ShellProvider;

import com.google.common.base.Joiner;

public class RegionsHyperlinkTest {

    @ClassRule
    public static ShellProvider shellProvider = new ShellProvider();

    @Test
    public void testRegionsHyperlinkProperties_1() {
        final ITextViewer textViewer = mock(ITextViewer.class);

        final RegionsHyperlink link = new RegionsHyperlink(textViewer, new Region(20, 50), new Region(100, 10));
        assertThat(link.getTypeLabel()).isNull();
        assertThat(link.getHyperlinkRegion()).isEqualTo(new Region(20, 50));
        assertThat(link.getDestinationRegion()).isEqualTo(new Region(100, 10));
        assertThat(link.getHyperlinkText()).isEqualTo("Open Definition");
        assertThat(link.getLabelForCompoundHyperlinksDialog()).isEqualTo("[local definition in current file]");
        assertThat(link.additionalLabelDecoration()).isEmpty();
        assertThat(link.getImage()).isEqualTo(RedImages.getImageForFileWithExtension(""));
    }

    @Test
    public void testRegionsHyperlinkProperties_2() {
        final ITextViewer textViewer = mock(ITextViewer.class);
        final RobotSuiteFile suiteFile = new RobotSuiteFileCreator().build();

        final RegionsHyperlink link = new RegionsHyperlink(textViewer, suiteFile, new Region(20, 50),
                new Region(100, 10), "decoration");
        assertThat(link.getTypeLabel()).isNull();
        assertThat(link.getHyperlinkRegion()).isEqualTo(new Region(20, 50));
        assertThat(link.getDestinationRegion()).isEqualTo(new Region(100, 10));
        assertThat(link.getHyperlinkText()).isEqualTo("Open Definition");
        assertThat(link.getLabelForCompoundHyperlinksDialog()).isEqualTo("file.robot");
        assertThat(link.additionalLabelDecoration()).isEqualTo("decoration");
        assertThat(link.getImage()).isEqualTo(RedImages.getImageForFileWithExtension("robot"));
    }

    @Test
    public void testIfDestinationRegionIsCorrectlySelected() throws Exception {
        final Shell shell = shellProvider.getShell();
        final StyledText textControl = new StyledText(shell, SWT.MULTI);
        textControl.setText(Joiner.on('\n').join(newArrayList("aaaaaaaaaa", "bbbbbbbbbb", "cccccccccc", "dddddddddd",
                "eeeeeeeeee", "ffffffffff", "gggggggggg", "hhhhhhhhhh")));

        final int targetOffset = 60;

        final IDocument document = mock(IDocument.class);
        when(document.getLineOfOffset(targetOffset)).thenReturn(5);

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);
        when(textViewer.getTextWidget()).thenReturn(textControl);

        final RegionsHyperlink link = new RegionsHyperlink(textViewer, new Region(20, 50), new Region(targetOffset, 3));
        link.open();

        assertThat(textControl.getTopIndex()).isEqualTo(5);
        assertThat(textControl.getSelectionText()).isEqualTo("fff");
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrown_whenTryingToGetLineOfInvalidOffset() throws Exception {
        final int targetOffset = -10;

        final IDocument document = mock(IDocument.class);
        when(document.getLineOfOffset(targetOffset)).thenThrow(BadLocationException.class);

        final ITextViewer textViewer = mock(ITextViewer.class);
        when(textViewer.getDocument()).thenReturn(document);

        final RegionsHyperlink link = new RegionsHyperlink(textViewer, new Region(20, 50), new Region(targetOffset, 3));
        link.open();
    }
}
