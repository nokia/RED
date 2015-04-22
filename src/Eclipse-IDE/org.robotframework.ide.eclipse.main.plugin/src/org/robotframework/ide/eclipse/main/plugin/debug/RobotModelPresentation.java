package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;

/**
 * @author mmarzec
 *
 */
public class RobotModelPresentation extends LabelProvider implements IDebugModelPresentation {

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(java.lang.String,
     * java.lang.Object)
     */
    public void setAttribute(String attribute, Object value) {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        try {
            if (element instanceof IThread) {
                return ((IThread) element).getName();
            } else if (element instanceof IDebugTarget) {
                return ((IDebugTarget) element).getName();
            } else if (element instanceof IStackFrame) {
                return ((IStackFrame) element).getName();
            } else if (element instanceof RobotLineBreakpoint) {
                IMarker breakpointMarker = ((RobotLineBreakpoint) element).getMarker();
                String file = breakpointMarker.getAttribute(IMarker.LOCATION, "");
                Integer line = (Integer) breakpointMarker.getAttribute(IMarker.LINE_NUMBER);
                return file + " [line: " + line + "]";
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }

        return "RED";
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(org.eclipse.debug.core.model.IValue
     * , org.eclipse.debug.ui.IValueDetailListener)
     */
    public void computeDetail(IValue value, IValueDetailListener listener) {
        String detail = "";
        try {
            detail = value.getValueString();
        } catch (DebugException e) {
        }
        listener.detailComputed(value, detail);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(java.lang.Object)
     */
    public IEditorInput getEditorInput(Object element) {
        if (element instanceof IFile) {
            return new FileEditorInput((IFile) element);
        }
        if (element instanceof ILineBreakpoint) {
            return new FileEditorInput((IFile) ((ILineBreakpoint) element).getMarker().getResource());
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(org.eclipse.ui.IEditorInput,
     * java.lang.Object)
     */
    public String getEditorId(IEditorInput input, Object element) {
        if (element instanceof IFile || element instanceof ILineBreakpoint) {
            return "org.robotframework.ide.eclipse.main.plugin.texteditor.TextEditorWrapper";
        }
        return null;
    }
}
