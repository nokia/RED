/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/

package org.robotframework.ide.eclipse.main.plugin.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.debug.SourceNotFoundEditorInput.SourceOfStackFrameNotFound;

public class SourceNotFoundEditorInputTest {

    @Test
    public void providedElementIsStored() {
        final SourceOfStackFrameNotFound element = new SourceOfStackFrameNotFound("frame", "message");
        final SourceNotFoundEditorInput input = new SourceNotFoundEditorInput(element);

        assertThat(input.getElement()).isSameAs(element);
    }

    @Test
    public void properControlsBuilderIsProvided() {
        final SourceOfStackFrameNotFound element = new SourceOfStackFrameNotFound("frame", "message");
        final SourceNotFoundEditorInput input = new SourceNotFoundEditorInput(element);

        assertThat(input.getControlsCreator(mock(IWorkbenchPage.class)))
                .isInstanceOf(SourceNotFoundEditorControls.class);
    }

    @Test
    public void inputAlwaysReturnFalse_whenAskedIfExists() {
        final SourceOfStackFrameNotFound element = new SourceOfStackFrameNotFound("frame", "message");
        final SourceNotFoundEditorInput input = new SourceNotFoundEditorInput(element);

        assertThat(input.exists()).isFalse();
    }

    @Test
    public void imageDescriptorIsFrameImage() {
        final SourceOfStackFrameNotFound element = new SourceOfStackFrameNotFound("frame", "message");
        final SourceNotFoundEditorInput input = new SourceNotFoundEditorInput(element);

        assertThat(input.getImageDescriptor()).isEqualTo(RedImages.getStackFrameImage());
    }

    @Test
    public void titleImageDescriptorIsBigKeywordImage() {
        final SourceOfStackFrameNotFound element = new SourceOfStackFrameNotFound("frame", "message");
        final SourceNotFoundEditorInput input = new SourceNotFoundEditorInput(element);

        assertThat(input.getTitleImageDescriptor()).isEqualTo(RedImages.getBigErrorImage());
    }

    @Test
    public void inputIdTest() {
        final SourceOfStackFrameNotFound element = new SourceOfStackFrameNotFound("frame", "message");
        final SourceNotFoundEditorInput input = new SourceNotFoundEditorInput(element);

        assertThat(input.getId()).isEqualTo("assistant.editor.source.notFound");
    }

    @Test
    public void titleTest() {
        final SourceNotFoundEditorInput input1 = new SourceNotFoundEditorInput(
                new SourceOfStackFrameNotFound("frame", "title"));
        final SourceNotFoundEditorInput input2 = new SourceNotFoundEditorInput(
                new SourceOfStackFrameNotFound("frame", "title\nmessage"));

        assertThat(input1.getTitle()).isEqualTo("title");
        assertThat(input2.getTitle()).isEqualTo("title");
    }

    @Test
    public void detailedInfoTest() {
        final SourceNotFoundEditorInput input1 = new SourceNotFoundEditorInput(
                new SourceOfStackFrameNotFound("frame", "title"));
        final SourceNotFoundEditorInput input2 = new SourceNotFoundEditorInput(
                new SourceOfStackFrameNotFound("frame", "title\nmessage"));

        assertThat(input1.getDetailedInformation()).isEmpty();
        assertThat(input2.getDetailedInformation()).isEqualTo("message");
    }

    @Test
    public void nameTest() {
        final SourceOfStackFrameNotFound element = new SourceOfStackFrameNotFound("frame", "message");
        final SourceNotFoundEditorInput input = new SourceNotFoundEditorInput(element);

        assertThat(input.getName()).isEqualTo("frame");
    }

    @Test
    public void persistableTest() {
        final SourceOfStackFrameNotFound element = new SourceOfStackFrameNotFound("frame", "message");
        final SourceNotFoundEditorInput input = new SourceNotFoundEditorInput(element);

        assertThat(input.getPersistable()).isNull();
    }

    @Test
    public void thereIsNoAdapter() {
        final SourceOfStackFrameNotFound element = new SourceOfStackFrameNotFound("frame", "message");
        final SourceNotFoundEditorInput input = new SourceNotFoundEditorInput(element);

        assertThat(input.getAdapter(IEditorInput.class)).isNull();
        assertThat(input.getAdapter(IEditorPart.class)).isNull();
        assertThat(input.getAdapter(Object.class)).isNull();
    }

    @Test
    public void tooltipIsTakenFromElementName() {
        final SourceOfStackFrameNotFound element = new SourceOfStackFrameNotFound("frame", "message");
        final SourceNotFoundEditorInput input = new SourceNotFoundEditorInput(element);

        assertThat(input.getToolTipText()).isEqualTo("frame");
    }
}
