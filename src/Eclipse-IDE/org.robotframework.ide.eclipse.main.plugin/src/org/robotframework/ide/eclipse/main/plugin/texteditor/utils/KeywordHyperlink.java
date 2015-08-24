package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

/**
 * @author mmarzec
 */
public class KeywordHyperlink implements IHyperlink {

    private IRegion hyperlinkRegion;

    private IRegion result;

    private ITextViewer viewer;

    public KeywordHyperlink(final ITextViewer viewer, final IRegion hyperlinkRegion, final IRegion result) {
        this.hyperlinkRegion = hyperlinkRegion;
        this.result = result;
        this.viewer = viewer;
    }

    @Override
    public IRegion getHyperlinkRegion() {
        return hyperlinkRegion;
    }

    @Override
    public String getTypeLabel() {
        return null;
    }

    @Override
    public String getHyperlinkText() {
        return null;
    }

    @Override
    public void open() {

        int topIndexPosition;
        try {
            topIndexPosition = viewer.getDocument().getLineOfOffset(result.getOffset());
            viewer.getTextWidget().setTopIndex(topIndexPosition);
            viewer.getTextWidget().setSelection(result.getOffset(), result.getOffset() + result.getLength());

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

    }

    private void show() {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        workbench.getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                // TODO: get file location from model
                final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testProject");
                IEditorInput input = new FileEditorInput(project.getFile("suiteFolder/resource1.robot"));

                try {
                    workbench.getActiveWorkbenchWindow().getActivePage().openEditor(input, RobotFormEditor.ID);
                } catch (PartInitException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
