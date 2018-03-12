/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.net.URI;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.debug.SourceInLibraryEditorInput.SourceOfStackFrameInLibrary;

public class SourceInLibraryEditorInputTest {

    @Test
    public void providedElementIsStored() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("keyword",
                URI.create("file:///file.robot"));
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(element);
        
        assertThat(input.getElement()).isSameAs(element);
    }

    @Test
    public void properControlsBuilderIsProvided() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("keyword",
                URI.create("file:///file.robot"));
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(element);

        assertThat(input.getControlsCreator(mock(IWorkbenchPage.class)))
                .isInstanceOf(SourceInLibraryEditorControls.class);
    }

    @Test
    public void inputAlwaysReturnFalse_whenAskedIfExists() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("keyword",
                URI.create("file:///file.robot"));
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(element);

        assertThat(input.exists()).isFalse();
    }

    @Test
    public void imageDescriptorIsFrameImage() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("keyword",
                URI.create("file:///file.robot"));
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(element);

        assertThat(input.getImageDescriptor()).isEqualTo(RedImages.getStackFrameImage());
    }

    @Test
    public void titleImageDescriptorIsBigKeywordImage() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("keyword",
                URI.create("file:///file.robot"));
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(element);

        assertThat(input.getTitleImageDescriptor()).isEqualTo(RedImages.getBigKeywordImage());
    }

    @Test
    public void inputIdTest() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("keyword",
                URI.create("file:///file.robot"));
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(element);

        assertThat(input.getId()).isEqualTo("assistant.editor.source.lib");
    }

    @Test
    public void titleTest() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("keyword",
                URI.create("file:///file.robot"));
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(element);

        assertThat(input.getTitle()).isEqualTo("Keyword keyword contained in library");
    }

    @Test
    public void detailedInfoTest() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("keyword",
                URI.create("file:///file.robot"));
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(element);

        assertThat(input.getDetailedInformation()).contains("The keyword <b>keyword</b> is not a User Keyword");
    }

    @Test
    public void nameTest() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("keyword",
                URI.create("file:///file.robot"));
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(element);

        assertThat(input.getName()).isEqualTo("keyword");
    }

    @Test
    public void persistableTest() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("keyword",
                URI.create("file:///file.robot"));
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(element);

        assertThat(input.getPersistable()).isNull();
    }

    @Test
    public void thereIsNoAdapter() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("keyword",
                URI.create("file:///file.robot"));
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(element);

        assertThat(input.getAdapter(IEditorInput.class)).isNull();
        assertThat(input.getAdapter(IEditorPart.class)).isNull();
        assertThat(input.getAdapter(Object.class)).isNull();
    }

    @Test
    public void tooltipIsTakenFromElementName() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("keyword",
                URI.create("file:///file.robot"));
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(element);

        assertThat(input.getToolTipText()).isEqualTo("keyword");
    }

    @Test
    public void keywordNameIsTakenFromElementName() {
        final SourceOfStackFrameInLibrary element = new SourceOfStackFrameInLibrary("keyword",
                URI.create("file:///file.robot"));
        final SourceInLibraryEditorInput input = new SourceInLibraryEditorInput(element);

        assertThat(input.getKeywordName()).isEqualTo("keyword");
    }

    @Test
    public void uriIsTakenFromElement() {
        final SourceInLibraryEditorInput input1 = new SourceInLibraryEditorInput(
                new SourceOfStackFrameInLibrary("keyword", URI.create("file:///file.robot")));
        final SourceInLibraryEditorInput input2 = new SourceInLibraryEditorInput(
                new SourceOfStackFrameInLibrary("keyword", null));

        assertThat(input1.getFileUri()).contains(URI.create("file:///file.robot"));
        assertThat(input2.getFileUri()).isEmpty();
    }

}
