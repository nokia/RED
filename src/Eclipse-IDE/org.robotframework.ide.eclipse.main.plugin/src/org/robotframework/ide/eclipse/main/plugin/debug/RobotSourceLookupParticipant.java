package org.robotframework.ide.eclipse.main.plugin.debug;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotStackFrame;
import org.robotframework.ide.eclipse.main.plugin.texteditor.TextEditorWrapper;

public class RobotSourceLookupParticipant extends AbstractSourceLookupParticipant {

    private IEventBroker broker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#getSourceName(java.
     * lang.Object)
     */
    public String getSourceName(Object object) throws CoreException {
        if (object instanceof RobotStackFrame) {

            HighlightLineJob job =new HighlightLineJob(object);
            job.schedule();
            try {
                job.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return ((RobotStackFrame) object).getSourceName();
        }
        return null;
    }

    private void sendHighlightLineEventToTextEditor(String file, int line) {

        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("file", file);
        eventMap.put("line", String.valueOf(line));
        broker.send("TextEditor/HighlightLine", eventMap);
    }

    private class HighlightLineJob extends UIJob {

        private RobotPartListener listener;

        public HighlightLineJob(Object stackFrame) {
            super("Highlight Line");
            setSystem(true);
            setPriority(Job.INTERACTIVE);

            RobotStackFrame robotStackFrame = (RobotStackFrame) stackFrame;
            try {
                listener = new RobotPartListener(robotStackFrame.getSourceName(), robotStackFrame.getLineNumber());
            } catch (DebugException e) {
                e.printStackTrace();
            }
        }

        /*
         * (non-Javadoc)
         * @see
         * org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public IStatus runInUIThread(IProgressMonitor monitor) {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(listener);

            return Status.OK_STATUS;
        }
    }

    private class RobotPartListener implements IPartListener {

        private String fileName;

        private int lineNumber;

        public RobotPartListener(String fileName, int lineNumber) {
            this.fileName = fileName;
            this.lineNumber = lineNumber;
        }

        @Override
        public void partOpened(IWorkbenchPart part) {
            if (part != null) {
                TextEditorWrapper texteditor = (TextEditorWrapper) part.getAdapter(org.robotframework.ide.eclipse.main.plugin.texteditor.TextEditorWrapper.class);
                if (texteditor != null) {
                    String editorInputName = texteditor.getEditorInput().getName();

                    if (editorInputName.equals(fileName)) {
                        sendHighlightLineEventToTextEditor(fileName, lineNumber);

                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().removePartListener(this);
                    }
                }
            }
        }

        @Override
        public void partDeactivated(IWorkbenchPart part) {
        }

        @Override
        public void partClosed(IWorkbenchPart part) {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().removePartListener(this);
        }

        @Override
        public void partBroughtToTop(IWorkbenchPart part) {
        }

        @Override
        public void partActivated(IWorkbenchPart part) {
        }
    }

}
