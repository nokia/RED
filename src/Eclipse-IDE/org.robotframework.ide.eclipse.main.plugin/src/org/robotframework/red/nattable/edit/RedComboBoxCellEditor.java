/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.red.nattable.edit;

import static org.robotframework.red.swt.Listeners.focusLostAdapter;
import static org.robotframework.red.swt.Listeners.keyPressedAdapter;
import static org.robotframework.red.swt.Listeners.mouseUpAdapter;
import static org.robotframework.red.swt.Listeners.shellClosedAdapter;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.nebula.widgets.nattable.widget.NatCombo;
import org.eclipse.swt.SWT;


public class RedComboBoxCellEditor extends ComboBoxCellEditor {

    public RedComboBoxCellEditor(final List<?> canonicalValues) {
        super(canonicalValues);
    }

    @Override
    protected void addNatComboListener(final NatCombo combo) {
        // overridden in order to have move on commit depending on users preference

        combo.addKeyListener(keyPressedAdapter(e -> {
            if ((e.keyCode == SWT.CR) || (e.keyCode == SWT.KEYPAD_CR)) {
                commit(RedTextCellEditor.getMoveDirection(editMode, e), editMode == EditModeEnum.INLINE);

            } else if (e.keyCode == SWT.ESC && editMode == EditModeEnum.INLINE) {
                close();

            } else if (e.keyCode == SWT.ESC) {
                combo.hideDropdownControl();
            }
        }));

        combo.addMouseListener(mouseUpAdapter(e -> {
            commit(MoveDirectionEnum.NONE,
                    (!RedComboBoxCellEditor.this.multiselect && editMode == EditModeEnum.INLINE));
            if (!RedComboBoxCellEditor.this.multiselect && editMode == EditModeEnum.DIALOG) {
                // hide the dropdown after a value was selected in the combo
                // in a dialog
                combo.hideDropdownControl();
            }
        }));

        if (this.editMode == EditModeEnum.INLINE) {
            combo.addShellListener(shellClosedAdapter(e -> close()));

        } else if (this.editMode == EditModeEnum.DIALOG) {
            combo.addFocusListener(focusLostAdapter(e -> combo.hideDropdownControl()));
        }
    }
}
