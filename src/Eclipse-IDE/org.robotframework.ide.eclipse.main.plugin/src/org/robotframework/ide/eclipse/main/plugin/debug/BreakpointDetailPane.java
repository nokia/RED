package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPane3;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

    private boolean isHitCountSelected, isConditionalSelected;

    private Text txtHitCount, txtConditional;

    private Button btnHitCount, btnConditional;

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
        control.setLayout(new GridLayout(2, false));
        control.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

        btnHitCount = new Button(control, SWT.CHECK);
        btnHitCount.setText("Hit count:");

        txtHitCount = new Text(control, SWT.BORDER);
        txtHitCount.setEnabled(false);

        btnConditional = new Button(control, SWT.CHECK);
        btnConditional.setText("Conditional");

        txtConditional = new Text(control, SWT.BORDER);
        txtConditional.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));

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
            btnHitCount.removeSelectionListener(selectionListener);
            txtHitCount.removeModifyListener(modifyListener);
            btnConditional.removeSelectionListener(conditionalSelectionListener);
            txtConditional.removeModifyListener(conditionalModifyListener);

            Object element = selection.getFirstElement();
            if (element instanceof RobotLineBreakpoint) {
                IMarker marker = ((RobotLineBreakpoint) element).getMarker();
                currentMarker = marker;
                int hitCount = marker.getAttribute(RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, 1);
                if (hitCount > 1) {
                    btnHitCount.setSelection(true);
                    txtHitCount.setEnabled(true);
                    txtHitCount.setText(Integer.toString(hitCount));
                } else {
                    btnHitCount.setSelection(false);
                    txtHitCount.setEnabled(false);
                    txtHitCount.setText("");
                }

                String condition = marker.getAttribute(RobotLineBreakpoint.CONDITIONAL_ATTRIBUTE, "");
                if (!"".equals(condition)) {
                    btnConditional.setSelection(true);
                    txtConditional.setEnabled(true);
                    txtConditional.setText(condition);
                } else {
                    btnConditional.setSelection(false);
                    txtConditional.setEnabled(false);
                    txtConditional.setText("");
                }
            }

            btnHitCount.addSelectionListener(selectionListener);
            txtHitCount.addModifyListener(modifyListener);
            btnConditional.addSelectionListener(conditionalSelectionListener);
            txtConditional.addModifyListener(conditionalModifyListener);
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
                if (btnHitCount.getSelection() && !"".equals(txtHitCount.getText())) {
                    String hitCount = txtHitCount.getText();
                    int count = Integer.parseInt(hitCount);
                    currentMarker.setAttribute(RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, count);
                } else {
                    currentMarker.setAttribute(RobotLineBreakpoint.HIT_COUNT_ATTRIBUTE, 1);
                }
            } catch (NumberFormatException | CoreException e) {
                e.printStackTrace();
            }

            try {
                if (btnConditional.getSelection() && !"".equals(txtConditional.getText())) {
                    String condition = txtConditional.getText();
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

    private class HitCountSelectionListener implements SelectionListener {

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (!isHitCountSelected && btnHitCount.getSelection()) {
                isHitCountSelected = true;
            } else if (isHitCountSelected && !btnHitCount.getSelection()) {
                isHitCountSelected = false;
            }
            txtHitCount.setEnabled(isHitCountSelected);
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
            if (!isConditionalSelected && btnConditional.getSelection()) {
                isConditionalSelected = true;
            } else if (isConditionalSelected && !btnConditional.getSelection()) {
                isConditionalSelected = false;
            }
            txtConditional.setEnabled(isConditionalSelected);
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
