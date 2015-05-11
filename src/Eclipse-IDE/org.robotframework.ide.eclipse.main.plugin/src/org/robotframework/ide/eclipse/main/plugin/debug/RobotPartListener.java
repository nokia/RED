package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;
import org.robotframework.ide.eclipse.main.plugin.texteditor.TextEditorWrapper;

/**
 * @author mmarzec
 */
public class RobotPartListener implements IPartListener {

    private RobotEventBroker robotEventBroker;

    private BreakpointContext breakpointContext;

    public RobotPartListener(RobotEventBroker robotEventBroker) {
        this.robotEventBroker = robotEventBroker;
    }

    @Override
    public void partOpened(IWorkbenchPart part) {
        if (part != null) {
            TextEditorWrapper texteditor = (TextEditorWrapper) part.getAdapter(org.robotframework.ide.eclipse.main.plugin.texteditor.TextEditorWrapper.class);
            if (texteditor != null) {
                String editorInputName = texteditor.getEditorInput().getName();

                String fileName = breakpointContext.getFile();
                int line = breakpointContext.getLine();
                if (editorInputName.equals(fileName)) {
                    robotEventBroker.sendHighlightLineEventToTextEditor(fileName, line);
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

    public void setBreakpointContext(BreakpointContext context) {
        this.breakpointContext = context;
    }

}
