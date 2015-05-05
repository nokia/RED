package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;
import org.robotframework.ide.eclipse.main.plugin.texteditor.TextEditorWrapper;

public class RobotPartListener implements IPartListener {

    private String fileName;

    private int lineNumber;

    private RobotEventBroker robotEventBroker;

    public RobotPartListener(RobotEventBroker robotEventBroker) {
        this.robotEventBroker = robotEventBroker;
    }

    @Override
    public void partOpened(IWorkbenchPart part) {
        if (part != null) {
            TextEditorWrapper texteditor = (TextEditorWrapper) part.getAdapter(org.robotframework.ide.eclipse.main.plugin.texteditor.TextEditorWrapper.class);
            if (texteditor != null) {
                String editorInputName = texteditor.getEditorInput().getName();

                if (editorInputName.equals(fileName)) {
                    robotEventBroker.sendHighlightLineEventToTextEditor(fileName, lineNumber);
                }
            }
        }
    }

    @Override
    public void partDeactivated(IWorkbenchPart part) {
    }

    @Override
    public void partClosed(IWorkbenchPart part) {
    }

    @Override
    public void partBroughtToTop(IWorkbenchPart part) {
    }

    @Override
    public void partActivated(IWorkbenchPart part) {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

}
