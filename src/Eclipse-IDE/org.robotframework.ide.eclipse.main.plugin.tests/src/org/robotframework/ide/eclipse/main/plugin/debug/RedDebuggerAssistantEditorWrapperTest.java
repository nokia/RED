/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.debug.RedDebuggerAssistantEditorWrapper.RedDebuggerAssistantEditorControls;
import org.robotframework.ide.eclipse.main.plugin.debug.RedDebuggerAssistantEditorWrapper.RedDebuggerAssistantEditorInput;
import org.robotframework.red.graphics.ImagesManager;

public class RedDebuggerAssistantEditorWrapperTest {

    @Test
    public void exceptionIsThrown_whenTryingToSetInputOtherThanDebuggerAssistantInput() {
        final IEditorInput input = mock(IEditorInput.class);
        when(input.getName()).thenReturn("title");
        when(input.getImageDescriptor()).thenReturn(RedImages.getElementImage());

        final RedDebuggerAssistantEditorWrapper editor = new RedDebuggerAssistantEditorWrapper();

        assertThatIllegalStateException().isThrownBy(() -> editor.setInput(input))
                .withMessage("Source Not Found Editor can only have "
                        + RedDebuggerAssistantEditorInput.class.getSimpleName() + " input")
                .withNoCause();
    }

    @Test
    public void nameImageAndInputIsProperlySet_whenSettingDebuggerAssistantInput() {
        final RedDebuggerAssistantEditorInput input = mock(RedDebuggerAssistantEditorInput.class);
        when(input.getName()).thenReturn("title");
        when(input.getImageDescriptor()).thenReturn(RedImages.getElementImage());

        final RedDebuggerAssistantEditorWrapper editor = new RedDebuggerAssistantEditorWrapper();
        editor.setInput(input);

        assertThat(editor.getEditorInput()).isSameAs(input);
        assertThat(editor.getPartName()).isEqualTo("title");
        assertThat(editor.getTitleImage()).isEqualTo(ImagesManager.getImage(RedImages.getElementImage()));
    }

    @Test
    public void controlsAreCreatedByCreatorProvidedByInput() throws Exception {
        final SingleLabelControlCreator controlsCreator = new SingleLabelControlCreator();
        final RedDebuggerAssistantEditorInput input = simpleInput("id", "title", "label", controlsCreator);

        final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IEditorPart editor = IDE.openEditor(activePage, input, RedDebuggerAssistantEditorWrapper.ID);

        final Control createdControl = controlsCreator.getParent().getChildren()[0];
        assertThat(createdControl).isInstanceOf(Label.class);
        assertThat(((Label) createdControl).getText()).isEqualTo("label");

        activePage.closeEditor(editor, false);
    }

    @Test
    public void editorIsReusableAndInputCanBeReplacedWithDifferentOneOfTheSameType() throws Exception {
        final SingleLabelControlCreator controlsCreator = new SingleLabelControlCreator();
        final RedDebuggerAssistantEditorInput input = simpleInput("id", "title", "label", controlsCreator);

        final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final RedDebuggerAssistantEditorWrapper editor = (RedDebuggerAssistantEditorWrapper) IDE.openEditor(activePage,
                input, RedDebuggerAssistantEditorWrapper.ID);

        assertThat(editor).isInstanceOf(IReusableEditor.class);

        final Control createdControl = controlsCreator.getParent().getChildren()[0];
        assertThat(createdControl).isInstanceOf(Label.class);
        assertThat(((Label) createdControl).getText()).isEqualTo("label");

        final IEditorInput newerInput = simpleInput("id", "title", "different", controlsCreator);
        editor.setInput(newerInput);

        assertThat(((Label) createdControl).getText()).isEqualTo("different");

        activePage.closeEditor(editor, false);
    }

    @Test
    public void editorIsReusableAndInputCanBeReplacedWithDifferentOneWithDifferentType_controlsAreCreatedAndFilledOnceAgain()
            throws Exception {
        final SingleLabelControlCreator controlsCreator1 = new SingleLabelControlCreator();
        final RedDebuggerAssistantEditorInput input = simpleInput("id", "title", "label", controlsCreator1);

        final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final RedDebuggerAssistantEditorWrapper editor = (RedDebuggerAssistantEditorWrapper) IDE.openEditor(activePage,
                input, RedDebuggerAssistantEditorWrapper.ID);

        assertThat(editor).isInstanceOf(IReusableEditor.class);

        Control createdControl = controlsCreator1.getParent().getChildren()[0];
        assertThat(createdControl).isInstanceOf(Label.class);
        assertThat(((Label) createdControl).getText()).isEqualTo("label");

        final SingleTextControlCreator controlsCreator2 = new SingleTextControlCreator();
        final IEditorInput newerInput = simpleInput("id_2", "title", "different", controlsCreator2);
        editor.setInput(newerInput);

        assertThat(createdControl.isDisposed()).isTrue();

        createdControl = controlsCreator2.getParent().getChildren()[0];
        assertThat(createdControl).isInstanceOf(Text.class);
        assertThat(((Text) createdControl).getText()).isEqualTo("different");

        activePage.closeEditor(editor, false);
    }

    private static RedDebuggerAssistantEditorInput simpleInput(final String id, final String name, final String info,
            final RedDebuggerAssistantEditorControls controlsCreator) {
        final RedDebuggerAssistantEditorInput input = mock(RedDebuggerAssistantEditorInput.class);
        when(input.getId()).thenReturn(id);
        when(input.getName()).thenReturn(name);
        when(input.getImageDescriptor()).thenReturn(RedImages.getElementImage());
        when(input.getControlsCreator(any(IWorkbenchPage.class))).thenReturn(controlsCreator);
        when(input.getDetailedInformation()).thenReturn(info);
        return input;
    }

    private static class SingleLabelControlCreator implements RedDebuggerAssistantEditorControls {

        private Composite parent;

        private Label label;

        @Override
        public Composite getParent() {
            return parent;
        }

        @Override
        public void construct(final Composite parent) {
            this.parent = parent;

            this.label = new Label(parent, SWT.NONE);
        }

        @Override
        public void dispose() {
            label.dispose();
        }

        @Override
        public void setFocus() {
            label.setFocus();
        }

        @Override
        public void setInput(final RedDebuggerAssistantEditorInput editorInput) {
            label.setText(editorInput.getDetailedInformation());
        }
    }

    private static class SingleTextControlCreator implements RedDebuggerAssistantEditorControls {

        private Composite parent;

        private Text text;

        @Override
        public Composite getParent() {
            return parent;
        }

        @Override
        public void construct(final Composite parent) {
            this.parent = parent;

            this.text = new Text(parent, SWT.SINGLE);
        }

        @Override
        public void dispose() {
            text.dispose();
        }

        @Override
        public void setFocus() {
            text.setFocus();
        }

        @Override
        public void setInput(final RedDebuggerAssistantEditorInput editorInput) {
            text.setText(editorInput.getDetailedInformation());
        }
    }
}
