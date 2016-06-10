/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.assist.RedContentProposalAdapter;
import org.robotframework.red.jface.assist.RedContentProposalAdapter.RedContentProposalListener;
import org.robotframework.red.nattable.edit.AssistanceSupport;
import org.robotframework.red.nattable.edit.CellEditorValueValidator;
import org.robotframework.red.nattable.edit.DefaultRedCellEditorValueValidator;
import org.robotframework.red.nattable.edit.DetailCellEditorEntry;
import org.robotframework.red.swt.LabelsMeasurer;

import com.google.common.base.Optional;

/**
 * @author Michal Anglart
 *
 */
class DictVariableDetailCellEditorEntry extends DetailCellEditorEntry<DictionaryKeyValuePair> {

    private final AssistanceSupport assistSupport;

    private String keyText;
    private String valueText;

    private Text textEdit;


    DictVariableDetailCellEditorEntry(final Composite parent, final AssistanceSupport assistSupport,
            final Color hoverColor, final Color selectionColor) {
        super(parent, hoverColor, selectionColor);
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
                if (assistSupport.areContentProposalsShown()) {
                    return;
                }
                if (e.keyCode == SWT.ESC) {
                    cancelEdit();
                } else if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
                    commitEdit();
                }
            }
        });
        validationJobScheduler.armRevalidationOn(textEdit);
        assistSupport.install(textEdit, Optional.<RedContentProposalListener> absent(),
                RedContentProposalAdapter.PROPOSAL_SHOULD_INSERT);
        GridDataFactory.fillDefaults().grab(true, false).indent(5, 2).applyTo(textEdit);
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
            final int mid = width / 2;

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
                bufferGC.drawImage(ImagesManager.getImage(RedImages.getDictionaryMappingImage()), mid - spacingAroundImage, 4);
            } else {
                bufferGC.drawImage(ImagesManager.getImage(RedImages.getGreyedImage(RedImages.getDictionaryMappingImage())),
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
