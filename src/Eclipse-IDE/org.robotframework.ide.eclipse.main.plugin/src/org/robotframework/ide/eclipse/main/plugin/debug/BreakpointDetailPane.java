package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPane3;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.wb.swt.SWTResourceManager;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;

/**
 * @author mmarzec
 */
public class BreakpointDetailPane implements IDetailPane, IDetailPane3 {

    public static final String BREAKPOINT_DETAIL_PANE_ID = "robot.breakpoint.detail.pane.id";

    public static final String BREAKPOINT_DETAIL_PANE_NAME = "Breakpoint Detail Pane";

    public static final String BREAKPOINT_DETAIL_PANE_DESC = "Breakpoint Detail Pane";

    private boolean isDirty;

    private Text hitCountTxt, conditionalTxt;

    private Button hitCountBtn, conditionalBtn;

    private IMarker currentMarker;

    private ListenerList listenersList = new ListenerList();

    private HitCountSelectionListener selectionListener;

    private HitCountModifyListener modifyListener;

    private ConditionalSelectionListener conditionalSelectionListener;

    private ConditionalModifyListener conditionalModifyListener;

    @Override
    public void init(IWorkbenchPartSite partSite) {
        isDirty = false;
        listenersList.clear();

        selectionListener = new HitCountSelectionListener();
        modifyListener = new HitCountModifyListener();
        conditionalSelectionListener = new ConditionalSelectionListener();
        conditionalModifyListener = new ConditionalModifyListener();
    }

    @Override
    public Control createControl(Composite parent) {
        parent.setBackground(SWTResourceManager.getColor(255, 255, 255));

        Composite control = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(3, 3).applyTo(control);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(control);

        hitCountBtn = new Button(control, SWT.CHECK);
        hitCountBtn.setText("Hit count:");

        hitCountTxt = new Text(control, SWT.BORDER);
        hitCountTxt.setEnabled(false);
        GridDataFactory.fillDefaults()
                .align(SWT.BEGINNING, SWT.CENTER)
                .grab(true, false)
                .minSize(80, 20)
                .applyTo(hitCountTxt);

        conditionalBtn = new Button(control, SWT.CHECK);
        conditionalBtn.setText("Conditional");

        conditionalTxt = new Text(control, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(conditionalTxt);

        isDirty = false;

        return control;
    }

    @Override
    public void dispose() {
        listenersList.clear();
    }

    @Override
    public void display(IStructuredSelection selection) {
        if (selection != null && selection.size() > 0) {
            removeAllListeners();

            Object element = selection.getFirstElement();
            if (element instanceof RobotLineBreakpoint) {
                IMarker marker = ((RobotLineBreakpoint) element).getMarker();
                currentMarker = marker;

                int hitCount = marker.getAttribute(RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, 1);
                setupHitCountControls(hitCount);

                String condition = marker.getAttribute(RobotLineBreakpoint.CONDITIONAL_ATTRIBUTE, "");
                setupConditionalControls(condition);
            }

            addAllListeners();
        }
    }

    @Override
    public boolean setFocus() {
        return false;
    }

    @Override
    public String getID() {
        return BREAKPOINT_DETAIL_PANE_ID;
    }

    @Override
    public String getName() {
        return BREAKPOINT_DETAIL_PANE_NAME;
    }

    @Override
    public String getDescription() {
        return BREAKPOINT_DETAIL_PANE_DESC;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        isDirty = false;
        fireDirty();

        if (currentMarker != null) {
            try {
                if (hitCountBtn.getSelection() && !"".equals(hitCountTxt.getText())) {
                    int hitCount = Integer.parseInt(hitCountTxt.getText());
                    currentMarker.setAttribute(RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, hitCount);
                } else {
                    currentMarker.setAttribute(RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, 1);
                }
            } catch (NumberFormatException | CoreException e) {
                e.printStackTrace();
            }

            try {
                if (conditionalBtn.getSelection() && !"".equals(conditionalTxt.getText())) {
                    String condition = conditionalTxt.getText();
                    currentMarker.setAttribute(RobotLineBreakpoint.CONDITIONAL_ATTRIBUTE, condition);
                } else {
                    currentMarker.setAttribute(RobotLineBreakpoint.CONDITIONAL_ATTRIBUTE, "");
                }
            } catch (CoreException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public boolean isSaveOnCloseNeeded() {
        return false;
    }

    @Override
    public void addPropertyListener(IPropertyListener listener) {
        listenersList.add(listener);
    }

    @Override
    public void removePropertyListener(IPropertyListener listener) {
        listenersList.remove(listener);
    }

    private void fireDirty() {
        Object[] listeners = listenersList.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            ((IPropertyListener) listeners[i]).propertyChanged(this, PROP_DIRTY);
        }
    }

    private void setupHitCountControls(int hitCount) {
        boolean isEnabled = hitCount > 1;
        hitCountBtn.setSelection(isEnabled);
        hitCountTxt.setEnabled(isEnabled);
        if (isEnabled) {
            hitCountTxt.setText(Integer.toString(hitCount));
        } else {
            hitCountTxt.setText("");
        }
    }

    private void setupConditionalControls(String condition) {
        boolean isEnabled = !"".equals(condition);
        conditionalBtn.setSelection(isEnabled);
        conditionalTxt.setEnabled(isEnabled);
        if (isEnabled) {
            conditionalTxt.setText(condition);
        } else {
            conditionalTxt.setText("");
        }
    }

    private void removeAllListeners() {
        hitCountBtn.removeSelectionListener(selectionListener);
        hitCountTxt.removeModifyListener(modifyListener);
        conditionalBtn.removeSelectionListener(conditionalSelectionListener);
        conditionalTxt.removeModifyListener(conditionalModifyListener);
    }

    private void addAllListeners() {
        hitCountBtn.addSelectionListener(selectionListener);
        hitCountTxt.addModifyListener(modifyListener);
        conditionalBtn.addSelectionListener(conditionalSelectionListener);
        conditionalTxt.addModifyListener(conditionalModifyListener);
    }

    private class HitCountSelectionListener implements SelectionListener {

        @Override
        public void widgetSelected(SelectionEvent e) {
            hitCountTxt.setEnabled(hitCountBtn.getSelection());
            isDirty = true;
            fireDirty();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }
    }

    private class HitCountModifyListener implements ModifyListener {

        @Override
        public void modifyText(ModifyEvent e) {
            isDirty = true;
            fireDirty();
        }
    }

    private class ConditionalSelectionListener implements SelectionListener {

        @Override
        public void widgetSelected(SelectionEvent e) {
            conditionalTxt.setEnabled(conditionalBtn.getSelection());
            isDirty = true;
            fireDirty();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
        }
    }

    private class ConditionalModifyListener implements ModifyListener {

        @Override
        public void modifyText(ModifyEvent e) {
            isDirty = true;
            fireDirty();
        }
    }
}
