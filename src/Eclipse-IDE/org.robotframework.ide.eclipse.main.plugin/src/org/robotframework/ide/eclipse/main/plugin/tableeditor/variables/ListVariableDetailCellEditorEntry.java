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
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.jface.assist.AssistantContext;
import org.robotframework.red.nattable.edit.AssistanceSupport;
import org.robotframework.red.nattable.edit.AssistanceSupport.NatTableAssistantContext;
import org.robotframework.red.nattable.edit.CellEditorValueValidator;
import org.robotframework.red.nattable.edit.DefaultRedCellEditorValueValidator;
import org.robotframework.red.nattable.edit.DetailCellEditorEntry;
import org.robotframework.red.swt.LabelsMeasurer;

import com.google.common.base.Strings;

/**
 * @author Michal Anglart
 */
class ListVariableDetailCellEditorEntry extends DetailCellEditorEntry<RobotToken> {

    private final AssistanceSupport assistSupport;

    private String text;

    private String indexText;

    private Text textEdit;

    ListVariableDetailCellEditorEntry(final Composite parent, final int column, final int row,
            final AssistanceSupport assistSupport, final Color hoverColor, final Color selectionColor) {
        super(parent, column, row, hoverColor, selectionColor);
        this.assistSupport = assistSupport;

        addPaintListener(new ListElementPainter());
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
        textEdit.setText(text);
        textEdit.setSelection(text.length());
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
        GridDataFactory.fillDefaults()
                .grab(true, false)
                .indent(calculateControlIndent(), 2)
                .hint(SWT.DEFAULT, 20)
                .applyTo(textEdit);
        layout();

        select(true);

        textEdit.setFocus();
    }

    private int calculateControlIndent() {
        final GC gc = new GC(this);
        final int indexLen = gc.textExtent(indexText).x;
        gc.dispose();
        final int indent = indexLen + 2 * SPACING_AROUND_LINE + LINE_WIDTH;
        return indent;
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
            textEdit = null;
        }
        redraw();
    }

    @Override
    public void update(final RobotToken detail) {
        text = detail.getText();
        setToolTipText(text);

        redraw();
    }

    void setIndex(final int allElements, final int index) {
        final int maxElementLength = (int) Math.ceil(Math.log10(allElements));
        indexText = "[" + Strings.padStart(Integer.toString(index), maxElementLength, '0') + "]";
    }

    private class ListElementPainter extends EntryControlPainter {

        @Override
        protected void paintForeground(final int width, final int height, final GC bufferGC) {
            final Color fgColor = bufferGC.getForeground();
            final int lineWidth = bufferGC.getLineWidth();

            if (isHovered()) {
                bufferGC.setForeground(
                        ColorsManager.getColor(ColorsManager.blend(fgColor.getRGB(), hoverColor.getRGB())));
            }

            int x = 3;
            bufferGC.drawText(indexText, x, 4);
            x += bufferGC.textExtent(indexText).x + SPACING_AROUND_LINE;

            final Color fgColor2 = bufferGC.getForeground();
            bufferGC.setForeground(ColorsManager.getColor(220, 220, 220));
            bufferGC.setLineWidth(LINE_WIDTH);
            bufferGC.drawLine(x, 0, x, height);
            bufferGC.setForeground(fgColor2);

            x += SPACING_AROUND_LINE + LINE_WIDTH;

            final int limit = width - 10 - x;
            if (bufferGC.textExtent(text).x < limit) {
                bufferGC.drawText(text, x, 4);
            } else {
                // text is too long to be drawn; we will add ... suffix and will look for
                // longest possible prefix which will fit;
                final String suffix = "...";
                final int suffixLength = bufferGC.textExtent(suffix).x;
                bufferGC.drawText(LabelsMeasurer.cutTextToRender(bufferGC, text, limit - suffixLength) + suffix, x, 4);
            }
            bufferGC.setForeground(fgColor);
            bufferGC.setLineWidth(lineWidth);
        }
    }
}
