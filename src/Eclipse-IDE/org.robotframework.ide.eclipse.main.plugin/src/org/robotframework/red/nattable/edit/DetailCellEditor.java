/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import java.util.List;

import org.eclipse.jface.viewers.IContentProposingSupport;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractCellEditor;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.robotframework.red.jface.assist.RedContentProposalAdapter;
import org.robotframework.red.jface.assist.RedContentProposalAdapter.RedContentProposalListener;

import com.google.common.base.Optional;

/**
 * Cell editor which shows list of detailed entries which can be edited
 * <D> type of detail model elements to edit
 * 
 * @author Michal Anglart
 */
public class DetailCellEditor<D> extends AbstractCellEditor {

    private DetailCellEditorComposite<D> composite;

    private final DetailCellEditorEditingSupport<D> editSupport;

    private final CellEditorValueValidationJobScheduler<String> validationJobScheduler;

    private final AssistanceSupport assistSupport;

    private IContextActivation contextActivation;

    public DetailCellEditor(final DetailCellEditorEditingSupport<D> editSupport,
            final IContentProposingSupport support) {
        this(editSupport, new DefaultRedCellEditorValueValidator(), support);
    }

    public DetailCellEditor(final DetailCellEditorEditingSupport<D> editSupport,
            final CellEditorValueValidator<String> validator, final IContentProposingSupport support) {
        this.editSupport = editSupport;
        this.assistSupport = new AssistanceSupport(support);
        this.validationJobScheduler = new CellEditorValueValidationJobScheduler<>(validator);
    }

    @Override
    public boolean supportMultiEdit(final IConfigRegistry configRegistry, final List<String> configLabels) {
        return false;
    }

    @Override
    protected Control activateCell(final Composite parent, final Object originalCanonicalValue) {
        editMode = EditModeEnum.DIALOG;

        composite = createEditorControl(parent);
        composite.setInput(getColumnIndex(), getRowIndex());

        assistSupport.install(composite.getText(), Optional.<RedContentProposalListener> absent(),
                RedContentProposalAdapter.PROPOSAL_SHOULD_INSERT);
        parent.redraw();

        final IContextService service = PlatformUI.getWorkbench().getService(IContextService.class);
        contextActivation = service.activateContext(RedTextCellEditor.DETAILS_EDITING_CONTEXT_ID);
        return composite;
    }

    @Override
    public Rectangle calculateControlBounds(final Rectangle cellBounds) {
        final Point realSize = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        return new Rectangle(cellBounds.x, cellBounds.y, cellBounds.width, Math.min(200, realSize.y));
    }

    @Override
    public DetailCellEditorComposite<D> createEditorControl(final Composite parent) {
        final DetailCellEditorComposite<D> composite = new DetailCellEditorComposite<>(parent, editSupport,
                assistSupport, validationJobScheduler);
        composite.setBackground(parent.getBackground());

        composite.getText().setFocus();
        composite.getText().addTraverseListener(new TraverseListener() {
            @Override
            public void keyTraversed(final TraverseEvent event) {
                if (assistSupport.areContentProposalsShown()) {
                    return;
                }

                boolean committed = false;
                if (event.keyCode == SWT.TAB && event.stateMask == SWT.SHIFT) {
                    committed = commit(MoveDirectionEnum.LEFT);
                } else if (event.keyCode == SWT.TAB && event.stateMask == 0) {
                    committed = commit(MoveDirectionEnum.RIGHT);
                } else if (event.keyCode == SWT.CR && event.stateMask == SWT.SHIFT
                        && composite.getText().getText().isEmpty()) {
                    committed = commit(MoveDirectionEnum.LEFT);
                } else if (event.keyCode == SWT.CR && event.stateMask == 0 && composite.getText().getText().isEmpty()) {
                    committed = commit(MoveDirectionEnum.RIGHT);
                } else if ((event.keyCode == SWT.ESC || event.keyCode == SWT.KEYPAD_CR)
                        && composite.getText().getText().isEmpty()) {
                    close();
                } else if (event.keyCode == SWT.ESC && !composite.getText().getText().isEmpty()) {
                    composite.getText().setText("");
                }

                if (!committed) {
                    event.doit = false;
                }
            }
        });
        composite.setVisible(true);
        validationJobScheduler.armRevalidationOn(composite.getText());

        return composite;
    }

    @Override
    public boolean commit(final MoveDirectionEnum direction, final boolean closeAfterCommit,
            final boolean skipValidation) {
        if (validationJobScheduler.canCloseCellEditor()) {
            return super.commit(direction, closeAfterCommit, skipValidation);
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        super.close();

        final IContextService service = PlatformUI.getWorkbench().getService(IContextService.class);
        service.deactivateContext(contextActivation);
    }

    @Override
    public Control getEditorControl() {
        return composite;
    }

    @Override
    public Object getEditorValue() {
        return null;
    }

    @Override
    public void setEditorValue(final Object value) {
        // nothing
    }
}
