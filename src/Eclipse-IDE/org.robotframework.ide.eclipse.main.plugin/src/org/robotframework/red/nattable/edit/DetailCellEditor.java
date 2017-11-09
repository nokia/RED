/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.editor.AbstractCellEditor;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.CellCommitBehavior;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.jface.assist.RedContentProposalProvider;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;

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
            final RedContentProposalProvider proposalProvider) {
        this(editSupport, new DefaultRedCellEditorValueValidator(), proposalProvider);
    }

    public DetailCellEditor(final DetailCellEditorEditingSupport<D> editSupport,
            final CellEditorValueValidator<String> validator, final RedContentProposalProvider proposalProvider) {
        this.editSupport = editSupport;
        this.assistSupport = new AssistanceSupport(proposalProvider);
        this.validationJobScheduler = new CellEditorValueValidationJobScheduler<>(validator);
    }

    @Override
    public boolean supportMultiEdit(final IConfigRegistry configRegistry, final List<String> configLabels) {
        return false;
    }

    @Override
    protected Control activateCell(final Composite parent, final Object originalCanonicalValue) {
        editMode = EditModeEnum.DIALOG;

        final int column = getColumnIndex();
        final int row = getRowIndex();

        composite = createEditorControl(parent);
        composite.setInput(column, row);

        final AssistantContext context = new NatTableAssistantContext(column, row);
        assistSupport.install(composite.getText(), context);
        parent.redraw();

        final IContextService service = PlatformUI.getWorkbench().getService(IContextService.class);
        contextActivation = service.activateContext(RedPlugin.DETAILS_EDITING_CONTEXT_ID);
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

        ((GridData) composite.getText().getLayoutData()).heightHint = this.layerCell.getBounds().height;
        composite.setColors(composite.getBackground(),
                this.cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
        composite.getText().setFont(this.cellStyle.getAttributeValue(CellStyleAttributes.FONT));
        composite.getText().setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_IBEAM));

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
                    committed = commit(getCommitMoveDirection(MoveDirectionEnum.LEFT));
                } else if (event.keyCode == SWT.CR && event.stateMask == 0 && composite.getText().getText().isEmpty()) {
                    committed = commit(getCommitMoveDirection(MoveDirectionEnum.RIGHT));
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

            private MoveDirectionEnum getCommitMoveDirection(final MoveDirectionEnum defaultDirection) {
                return RedPlugin.getDefault()
                        .getPreferences()
                        .getCellCommitBehavior() == CellCommitBehavior.STAY_IN_SAME_CELL ? MoveDirectionEnum.NONE
                                : defaultDirection;
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
