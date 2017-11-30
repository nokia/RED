/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.function.Function;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.red.graphics.ColorsManager;

import com.codeaffine.eclipse.swt.widget.scrollbar.FlatScrollBar;

/**
 * @author Michal Anglart
 *
 */
public class TableThemes {

    private static final int LIMIT = 130;

    public static TableTheme getTheme(final RGB backgroundInUse) {
        return isDarkColor(backgroundInUse) ? new DarkTheme() : new BrightTheme();
    }

    private static boolean isDarkColor(final RGB color) {
        return calculatePerceivedBrightness(color) < LIMIT;
    }

    // the formula is referenced in the internet in topics regarding perceived brightness
    private static int calculatePerceivedBrightness(final RGB color) {
        final int r = color.red;
        final int g = color.green;
        final int b = color.blue;
        return (int) Math.sqrt(r * r * .299 + g * g * .587 + b * b * .114);
    }

    public static abstract class TableTheme {

        public Font getFont() {
            return RedTheme.Fonts.getTablesEditorFont();
        }

        public Color getHeadersGridColor() {
            return RedTheme.Colors.getTableHeaderGridColor();
        }

        public Color getHeadersBackground() {
            return RedTheme.Colors.getTableHeaderBackgroundColor();
        }

        public Color getHeadersHighlightedBackground() {
            return RedTheme.Colors.getTableHeaderHihglightedBackgroundColor();
        }

        public Color getHeadersUnderlineColor() {
            return RedTheme.Colors.getTableHeaderUnderlineColor();
        }

        public Color getHeadersForeground() {
            return RedTheme.Colors.getTableHeaderForegroundColor();
        }

        public Color getBodyGridColor() {
            return RedTheme.Colors.getTableBodyGridColor();
        }

        public Color getBodySelectionGridColor() {
            return RedTheme.Colors.getTableBodySelectionGridColor();
        }

        public Color getBodySelectionBorderColor() {
            return RedTheme.Colors.getTableBodySelectionBorderColor();
        }

        public Color getBodyOddRowBackground() {
            return RedTheme.Colors.getTableBodyOddRowBackgroundColor();
        }

        public Color getBodyEvenRowBackground() {
            return RedTheme.Colors.getTableBodyEvenRowBackgroundColor();
        }

        public Color getBodyForeground() {
            return RedTheme.Colors.getTableBodyForegroundColor();
        }

        public Color getBodyHoveredCellBackground() {
            return RedTheme.Colors.getTableHighlightedCellColor();
        }

        public Color getBodyHoveredSelectedCellBackground() {
            return RedTheme.Colors.getTableHighlightedCellColor();
        }

        public Color getBodySelectedCellBackground() {
            return RedTheme.Colors.getTableHiglihtedRowColor();
        }

        public Color getBodyAnchoredCellBackground() {
            return RedTheme.Colors.getTableHiglihtedRowColor();
        }

        public Color getBodyInactiveCellBackground() {
            return RedTheme.Colors.getTableInactiveCellBackgroundColor();
        }

        public Color getBodyInactiveCellForeground() {
            return RedTheme.Colors.getTableInactiveCellForegroundColor();
        }

        public abstract NatTable configureScrollBars(Composite parent, final ViewportLayer viewportLayer,
                Function<Composite, NatTable> tableCreator);
    }

    private static class BrightTheme extends TableTheme {

        @Override
        public NatTable configureScrollBars(final Composite parent, final ViewportLayer viewportLayer,
                final Function<Composite, NatTable> tableCreator) {
            return tableCreator.apply(parent);
        }
    }

    private static class DarkTheme extends TableTheme {

        @Override
        public NatTable configureScrollBars(final Composite parent, final ViewportLayer viewportLayer,
                final Function<Composite, NatTable> tableCreator) {

            final Composite tableHolder = new Composite(parent, SWT.NONE);
            tableHolder.setBackground(parent.getBackground());
            GridDataFactory.fillDefaults().grab(true, true).applyTo(tableHolder);
            GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).numColumns(2).applyTo(tableHolder);

            final NatTable table = tableCreator.apply(tableHolder);

            final Composite verticalComposite = new Composite(tableHolder, SWT.NONE);
            verticalComposite.setBackground(tableHolder.getBackground());
            final GridData verticalData = GridDataFactory.fillDefaults()
                    .hint(8, SWT.DEFAULT)
                    .align(SWT.BEGINNING, SWT.FILL)
                    .grab(false, true)
                    .create();
            verticalComposite.setLayoutData(verticalData);
            GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).applyTo(verticalComposite);
            final FlatScrollBar verticalScrollBar = new FlatScrollBar(verticalComposite, SWT.VERTICAL);
            verticalScrollBar.setBackground(verticalComposite.getBackground());
            verticalScrollBar.setPageIncrementColor(ColorsManager.getColor(62, 70, 76));
            verticalScrollBar.setThumbColor(ColorsManager.getColor(115, 130, 140));
            GridDataFactory.fillDefaults().grab(true, true).applyTo(verticalScrollBar);
            viewportLayer.setVerticalScroller(new FlatScrollBarScroller(verticalScrollBar));

            final Composite horizontalComposite = new Composite(tableHolder, SWT.NONE);
            final GridData horizontalData = GridDataFactory.fillDefaults()
                    .hint(SWT.DEFAULT, 8)
                    .align(SWT.FILL, SWT.BEGINNING)
                    .grab(true, false)
                    .create();
            horizontalComposite.setLayoutData(horizontalData);
            GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).applyTo(horizontalComposite);
            final FlatScrollBar horizontalScrollBar = new FlatScrollBar(horizontalComposite, SWT.HORIZONTAL);
            horizontalScrollBar.setBackground(verticalComposite.getBackground());
            horizontalScrollBar.setPageIncrementColor(ColorsManager.getColor(62, 70, 76));
            horizontalScrollBar.setThumbColor(ColorsManager.getColor(115, 130, 140));
            GridDataFactory.fillDefaults().grab(true, true).applyTo(horizontalScrollBar);
            viewportLayer.setHorizontalScroller(new FlatScrollBarScroller(horizontalScrollBar));

            verticalScrollBar.addListener(SWT.Hide, e -> {
                final GridData hData = (GridData) horizontalComposite.getLayoutData();
                final GridData vData = (GridData) verticalComposite.getLayoutData();

                vData.exclude = true;
                hData.horizontalSpan = 2;
                tableHolder.layout();
            });
            verticalScrollBar.addListener(SWT.Show, e -> {
                final GridData hData = (GridData) horizontalComposite.getLayoutData();
                final GridData vData = (GridData) verticalComposite.getLayoutData();

                vData.exclude = false;
                hData.horizontalSpan = 1;
                tableHolder.layout();
            });

            horizontalScrollBar.addListener(SWT.Hide, e -> {
                final GridData hData = (GridData) horizontalComposite.getLayoutData();
                final GridData vData = (GridData) verticalComposite.getLayoutData();

                vData.verticalSpan = 2;
                hData.exclude = true;
                tableHolder.layout();
            });
            horizontalScrollBar.addListener(SWT.Show, e -> {
                final GridData hData = (GridData) horizontalComposite.getLayoutData();
                final GridData vData = (GridData) verticalComposite.getLayoutData();

                vData.verticalSpan = 1;
                hData.exclude = false;
                tableHolder.layout();
            });

            table.addMouseWheelListener(e -> {
                verticalScrollBar.setSelection(verticalScrollBar.getSelection()
                        - ((int) Math.signum(e.count) * verticalScrollBar.getIncrement()));
                verticalScrollBar.notifyListeners(SWT.NONE);
            });
            return table;
        }
    }
}
