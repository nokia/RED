/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import static org.robotframework.red.swt.Listeners.focusLostAdapter;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.nattable.edit.AssistanceSupport;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;
import org.robotframework.red.nattable.edit.CellEditorValueValidator;
import org.robotframework.red.nattable.edit.DefaultRedCellEditorValueValidator;
import org.robotframework.red.nattable.edit.DetailCellEditorEntry;
import org.robotframework.red.swt.LabelsMeasurer;

/**
 * @author Michal Anglart
 */
class DictVariableDetailCellEditorEntry extends DetailCellEditorEntry<DictionaryKeyValuePair> {

    private final AssistanceSupport assistSupport;

    private String keyText;

    private String valueText;

    private Text textEdit;

    DictVariableDetailCellEditorEntry(final Composite parent, final int column, final int row,
            final AssistanceSupport assistSupport, final Color hoverColor, final Color selectionColor) {
        super(parent, column, row, hoverColor, selectionColor);
        this.assistSupport = assistSupport;

        addPaintListener(new DictElementPainter());
        GridLayoutFactory.fillDefaults().extendedMargins(0, HOVER_BLOCK_WIDTH, 0, 0).applyTo(this);
    }

    @Override
    protected CellEditorValueValidator<String> getValidator() {
        return new DefaultRedCellEditorValueValidator();
    }

    @Override
    public void openForEditing() {
        super.openForEditing();

        textEdit = new Text(this, SWT.NONE);
        final String toEdit = keyText + (valueText.isEmpty() ? "" : "=" + valueText);
        textEdit.setText(toEdit);
        textEdit.setSelection(textEdit.getText().length());
        textEdit.addFocusListener(focusLostAdapter(e -> commitEdit()));
        textEdit.addTraverseListener(e -> {
            if (assistSupport.areContentProposalsShown()) {
                return;
            }
            if (e.keyCode == SWT.ESC) {
                cancelEdit();
            } else if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
                commitEdit();
            }
        });
        textEdit.addPaintListener(e -> {
            e.gc.drawLine(0, 0, e.width, 0);
            e.gc.drawLine(e.width - 1, 0, e.width - 1, e.height);
            e.gc.drawLine(e.width - 1, e.height - 1, 0, e.height - 1);
            e.gc.drawLine(0, e.height - 1, 0, 0);
        });
        validationJobScheduler.armRevalidationOn(textEdit);
        final AssistantContext context = new NatTableAssistantContext(column, row);
        assistSupport.install(textEdit, context);
        GridDataFactory.fillDefaults().grab(true, false).indent(5, 2).hint(SWT.DEFAULT, 20).applyTo(textEdit);
        layout();

        select(true);
        textEdit.setFocus();
    }

    @Override
    protected String getNewValue() {
        return textEdit.getText();
    }

    @Override
    protected void closeEditing() {
        super.closeEditing();
        if (textEdit != null && !textEdit.isDisposed()) {
            textEdit.dispose();
        }
        redraw();
    }

    @Override
    public void update(final DictionaryKeyValuePair detail) {
        keyText = detail.getKey().getText();
        valueText = detail.getValue().getText();
        setToolTipText(keyText + " --> " + valueText);

        redraw();
    }

    private class DictElementPainter extends EntryControlPainter {

        @Override
        protected void paintForeground(final int width, final int height, final GC bufferGC) {
            final Color fgColor = bufferGC.getForeground();
            final int lineWidth = bufferGC.getLineWidth();

            if (isHovered()) {
                bufferGC.setForeground(
                        ColorsManager.getColor(ColorsManager.blend(fgColor.getRGB(), hoverColor.getRGB())));
            }
            final int mid = width / 2;
            final int spacingAroundLine = LINE_WIDTH;

            final int keyLimit = mid - 2 * 4 - spacingAroundLine;
            final int keyX = 4;
            if (bufferGC.textExtent(keyText).x < keyLimit) {
                bufferGC.drawText(keyText, keyX, 4);
            } else {
                final String suffix = "...";
                final int suffixLength = bufferGC.textExtent(suffix).x;
                bufferGC.drawText(LabelsMeasurer.cutTextToRender(bufferGC, keyText, keyLimit - suffixLength) + suffix,
                        keyX, 4);
            }

            if (!isEditorOpened()) {
                final Color fgColor2 = bufferGC.getForeground();
                bufferGC.setForeground(ColorsManager.getColor(220, 220, 220));
                bufferGC.setLineWidth(LINE_WIDTH * 2);
                bufferGC.drawLine(mid - LINE_WIDTH, 0, mid - LINE_WIDTH, height);
                bufferGC.setForeground(fgColor2);
            }

            final int valueLimit = mid - HOVER_BLOCK_WIDTH - 4 - spacingAroundLine;
            final int valueX = mid + spacingAroundLine + 4;
            if (bufferGC.textExtent(valueText).x < valueLimit) {
                bufferGC.drawText(valueText, valueX, 4);
            } else {
                final String suffix = "...";
                final int suffixLength = bufferGC.textExtent(suffix).x;
                bufferGC.drawText(
                        LabelsMeasurer.cutTextToRender(bufferGC, valueText, valueLimit - suffixLength) + suffix, valueX,
                        4);
            }
            bufferGC.setForeground(fgColor);
            bufferGC.setLineWidth(lineWidth);
        }
    }
}
