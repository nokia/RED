/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import java.util.List;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.nattable.edit.DetailCellEditorEntry;
import org.robotframework.red.swt.LabelsMeasurer;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * @author Michal Anglart
 *
 */
class DictVariableDetailCellEditorEntry extends DetailCellEditorEntry<DictionaryKeyValuePair> {

    private String keyText;
    private String valueText;

    private Text textEdit;

    private ControlDecoration decoration;

    DictVariableDetailCellEditorEntry(final Composite parent, final Color hoverColor,
            final Color selectionColor) {
        super(parent, hoverColor, selectionColor);

        addPaintListener(new DictElementPainter());
        GridLayoutFactory.fillDefaults().extendedMargins(0, HOVER_BLOCK_WIDTH, 0, 0).applyTo(this);
    }

    @Override
    public void openForEditing() {
        super.openForEditing();

        textEdit = new Text(this, SWT.BORDER);
        final String toEdit = keyText + (valueText.isEmpty() ? "" : "=" + valueText);
        textEdit.setText(toEdit);
        textEdit.setSelection(textEdit.getText().length());
        textEdit.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent e) {
                commitEdit();
            }
        });
        textEdit.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(final TraverseEvent e) {
                if (e.keyCode == SWT.ESC) {
                    cancelEdit();
                } else if (e.keyCode == SWT.CR) {
                    commitEdit();
                }
            }
        });
        textEdit.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                rescheduleValidation();
            }
        });
        GridDataFactory.fillDefaults().grab(true, false).indent(5, 2).applyTo(textEdit);
        layout();

        select(true);
        textEdit.setFocus();
    }

    @Override
    protected void validate() {
        final String value = textEdit.getText();
        if (value.contains("  ")) {
            blockClosing();

            textEdit.setForeground(ColorsManager.getColor(255, 0, 0));
            decoration = new ControlDecoration(textEdit, SWT.LEFT | SWT.TOP);
            decoration.setDescriptionText("The dictionary map-value entry cannot contain two spaces");
            decoration.setImage(FieldDecorationRegistry.getDefault()
                    .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
                    .getImage());
        } else {
            unblockClosing();

            textEdit.setForeground(getForeground());
            if (decoration != null) {
                decoration.dispose();
                decoration = null;
            }
        }
    }

    @Override
    protected DictionaryKeyValuePair createNewValue() {
        final List<String> splittedContent = Splitter.on('=').splitToList(textEdit.getText());
        final String key = splittedContent.get(0);
        final String value = Joiner.on('=').join(splittedContent.subList(1, splittedContent.size()));

        final RobotToken raw = new RobotToken();
        raw.setRaw(textEdit.getText());
        raw.setText(textEdit.getText());

        final RobotToken keyToken = new RobotToken();
        keyToken.setRaw(key);
        keyToken.setText(key);

        final RobotToken valueToken = new RobotToken();
        valueToken.setRaw(value);
        valueToken.setText(value);

        return new DictionaryKeyValuePair(raw, keyToken, valueToken);
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
        protected void paintForeground(final PaintEvent e, final GC bufferGC) {

            final int mid = e.width / 2;

            final int spacingAroundImage = 8;

            final int keyLimit = mid - 2 * 4 - spacingAroundImage;
            final int keyX = 4;
            if (bufferGC.textExtent(keyText).x < keyLimit) {
                bufferGC.drawText(keyText, keyX, 4);
            } else {
                final String suffix = "...";
                final int suffixLength = bufferGC.textExtent(suffix).x;
                bufferGC.drawText(LabelsMeasurer.cutTextToRender(bufferGC, keyText, keyLimit - suffixLength) + suffix,
                        keyX, 4);
            }

            if (isHovered()) {
                bufferGC.drawImage(ImagesManager.getImage(RedImages.getArrowImage()), mid - spacingAroundImage, 4);
            } else {
                bufferGC.drawImage(ImagesManager.getImage(RedImages.getGreyedImage(RedImages.getArrowImage())),
                        mid - spacingAroundImage, 4);
            }

            final int valueLimit = mid - HOVER_BLOCK_WIDTH - 4 - spacingAroundImage;
            final int valueX = mid + spacingAroundImage + 4;
            if (bufferGC.textExtent(valueText).x < valueLimit) {
                bufferGC.drawText(valueText, valueX, 4);
            } else {
                final String suffix = "...";
                final int suffixLength = bufferGC.textExtent(suffix).x;
                bufferGC.drawText(
                        LabelsMeasurer.cutTextToRender(bufferGC, valueText, valueLimit - suffixLength) + suffix, valueX,
                        4);
            }
        }
    }
}
