package org.robotframework.ide.eclipse.main.plugin.debug;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.robotframework.ide.eclipse.main.plugin.texteditor.TextEditorWrapper;

public class RobotPartListener implements IPartListener {

    private String fileName;

    private int lineNumber;

    private IEventBroker broker;

    public RobotPartListener(IEventBroker broker) {
        this.broker = broker;
    }

    @Override
    public void partOpened(IWorkbenchPart part) {
        if (part != null) {
            TextEditorWrapper texteditor = (TextEditorWrapper) part.getAdapter(org.robotframework.ide.eclipse.main.plugin.texteditor.TextEditorWrapper.class);
            if (texteditor != null) {
                String editorInputName = texteditor.getEditorInput().getName();

                if (editorInputName.equals(fileName)) {
                    sendHighlightLineEventToTextEditor(fileName, lineNumber);
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

    private void sendHighlightLineEventToTextEditor(String file, int line) {

        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("file", file);
        eventMap.put("line", String.valueOf(line));
        broker.send("TextEditor/HighlightLine", eventMap);
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
