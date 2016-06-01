/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

/**
 * Modified version of {@link org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor} which
 * will move left/right after commits and validate entries asynchronously.
 * 
 * @author Michal Anglart
 */
public class RedTextCellEditor extends TextCellEditor {

    private final int selectionStartShift;
    private final int selectionEndShift;

    private final CellEditorValueValidationJobScheduler<String> validationJobScheduler;

    public RedTextCellEditor() {
        this(0, 0, new DefaultRedCellEditorValueValidator());
    }

    public RedTextCellEditor(final CellEditorValueValidator<String> validator) {
        this(0, 0, validator);
    }

    public RedTextCellEditor(final int selectionStartShift, final int selectionEndShift) {
        this(selectionStartShift, selectionEndShift, new DefaultRedCellEditorValueValidator());
    }

    public RedTextCellEditor(final int selectionStartShift, final int selectionEndShift,
            final CellEditorValueValidator<String> validator) {
        super(true, true);
        this.selectionStartShift = selectionStartShift;
        this.selectionEndShift = selectionEndShift;
        this.validationJobScheduler = new CellEditorValueValidationJobScheduler<>(validator);
    }

    @Override
    protected Text createEditorControl(final Composite parent, final int style) {
        final Text textControl = new Text(parent, style);

        textControl.setBackground(this.cellStyle.getAttributeValue(CellStyleAttributes.BACKGROUND_COLOR));
        textControl.setForeground(this.cellStyle.getAttributeValue(CellStyleAttributes.FOREGROUND_COLOR));
        textControl.setFont(this.cellStyle.getAttributeValue(CellStyleAttributes.FONT));
        textControl.setCursor(new Cursor(Display.getDefault(), SWT.CURSOR_IBEAM));

        textControl.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent event) {
                if (commitOnEnter && (event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR)) {
                    final boolean commit = (event.stateMask == SWT.ALT) ? false : true;
                    MoveDirectionEnum move = MoveDirectionEnum.NONE;
                    if (RedTextCellEditor.this.editMode == EditModeEnum.INLINE) {
                        if (event.stateMask == 0) {
                            move = MoveDirectionEnum.RIGHT;
                        } else if (event.stateMask == SWT.SHIFT) {
                            move = MoveDirectionEnum.LEFT;
                        }
                    }

                    if (commit) {
                        commit(move);
                    }

                    if (RedTextCellEditor.this.editMode == EditModeEnum.DIALOG) {
                        parent.forceFocus();
                    }
                } else if (event.keyCode == SWT.ESC && event.stateMask == 0) {
                    close();
                } else if (RedTextCellEditor.this.editMode == EditModeEnum.INLINE) {
                    if (event.keyCode == SWT.ARROW_UP) {
                        commit(MoveDirectionEnum.UP);
                    } else if (event.keyCode == SWT.ARROW_DOWN) {
                        commit(MoveDirectionEnum.DOWN);
                    }
                }
            }

            @Override
            public void keyReleased(final KeyEvent e) {
                try {
                    final Object canonicalValue = getCanonicalValue(getInputConversionErrorHandler());
                    validateCanonicalValue(canonicalValue, getInputValidationErrorHandler());
                } catch (final Exception ex) {
                    // do nothing
                }
            }
        });
        validationJobScheduler.armRevalidationOn(textControl);

        return textControl;
    }

    @Override
    protected Control activateCell(final Composite parent, final Object originalCanonicalValue) {
        final Control control = super.activateCell(parent, originalCanonicalValue);
        if (selectionStartShift > 0 || selectionEndShift > 0) {
            if (getEditorControl().getText().length() >= selectionStartShift + selectionEndShift) {
                getEditorControl().setSelection(selectionStartShift,
                        getEditorControl().getText().length() - selectionEndShift);
            }
        }
        return control;
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
}
