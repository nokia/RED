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

    public interface TableTheme {

        public Font getFont();
        public Color getGridBorderColor();
        public Color getGridSelectionBorderColor();

        public Color getHeadersGridBorderColor();
        public Color getHeadersBackground();
        public Color getHeadersForeground();
        public Color getHeadersUnderlineColor();
        public Color getHeadersHighlightedBackground();

        public Color getBodyBackgroundOddRowBackground();
        public Color getBodyBackgroundEvenRowBackground();
        public Color getBodyForeground();
        
        public Color getBodySelectionBorderColor();
        public Color getBodyHoveredCellBackground();
        public Color getBodySelectedCellBackground();
        public Color getBodyHoveredSelectedCellBackground();
        public Color getBodyAnchoredCellBackground();
        public Color getBodyInactiveCellBackground();
        public Color getBodyInactiveCellForeground();

        public NatTable configureScrollBars(Composite parent, final ViewportLayer viewportLayer,
                Function<Composite, NatTable> tableCreator);
    }

    private static class BrightTheme implements TableTheme {

        @Override
        public Font getFont() {
            return RedTheme.getTablesEditorFont();
        }

        @Override
        public Color getGridBorderColor() {
            return ColorsManager.getColor(240, 240, 240);
        }

        @Override
        public Color getGridSelectionBorderColor() {
            return ColorsManager.getColor(128, 128, 128);
        }

        @Override
        public Color getHeadersGridBorderColor() {
            return ColorsManager.getColor(210, 210, 210);
        }

        @Override
        public Color getHeadersBackground() {
            return ColorsManager.getColor(250, 250, 250);
        }

        @Override
        public Color getHeadersForeground() {
            return ColorsManager.getColor(SWT.COLOR_BLACK);
        }

        @Override
        public Color getHeadersUnderlineColor() {
            return ColorsManager.getColor(240, 240, 240);
        }

        @Override
        public Color getHeadersHighlightedBackground() {
            return ColorsManager.getColor(240, 240, 240);
        }

        @Override
        public Color getBodyBackgroundOddRowBackground() {
            return ColorsManager.getColor(SWT.COLOR_WHITE);
        }

        @Override
        public Color getBodyBackgroundEvenRowBackground() {
            return ColorsManager.getColor(250, 250, 250);
        }

        @Override
        public Color getBodyForeground() {
            return ColorsManager.getColor(SWT.COLOR_BLACK);
        }

        @Override
        public Color getBodySelectionBorderColor() {
            return ColorsManager.getColor(SWT.COLOR_BLACK);
        }

        @Override
        public Color getBodyHoveredCellBackground() {
            return RedTheme.getHighlightedCellColor();
        }

        @Override
        public Color getBodyHoveredSelectedCellBackground() {
            return RedTheme.getHighlightedCellColor();
        }

        @Override
        public Color getBodySelectedCellBackground() {
            return RedTheme.getHiglihtedRowColor();
        }

        @Override
        public Color getBodyAnchoredCellBackground() {
            return RedTheme.getHiglihtedRowColor();
        }

        @Override
        public Color getBodyInactiveCellBackground() {
            return ColorsManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
        }

        @Override
        public Color getBodyInactiveCellForeground() {
            return ColorsManager.getColor(SWT.COLOR_GRAY);
        }

        @Override
        public NatTable configureScrollBars(final Composite parent, final ViewportLayer viewportLayer,
                final Function<Composite, NatTable> tableCreator) {
            return tableCreator.apply(parent);
        }
    }

    private static class DarkTheme implements TableTheme {

        @Override
        public Font getFont() {
            return RedTheme.getTablesEditorFont();
        }

        @Override
        public Color getGridBorderColor() {
            return ColorsManager.getColor(60, 60, 60);
        }

        @Override
        public Color getGridSelectionBorderColor() {
            return ColorsManager.getColor(127, 100, 0);
        }

        @Override
        public Color getHeadersGridBorderColor() {
            return ColorsManager.getColor(50, 52, 55);
        }

        @Override
        public Color getHeadersBackground() {
            return ColorsManager.getColor(82, 87, 91);
        }

        @Override
        public Color getHeadersForeground() {
            return ColorsManager.getColor(204, 204, 204);
        }

        @Override
        public Color getHeadersUnderlineColor() {
            return ColorsManager.getColor(127, 100, 0);
        }

        @Override
        public Color getHeadersHighlightedBackground() {
            return ColorsManager.getColor(44, 47, 50);
        }

        @Override
        public Color getBodyBackgroundOddRowBackground() {
            return ColorsManager.getColor(47, 47, 47);
        }

        @Override
        public Color getBodyBackgroundEvenRowBackground() {
            return ColorsManager.getColor(58, 62, 66);
        }

        @Override
        public Color getBodyForeground() {
            return ColorsManager.getColor(204, 204, 204);
        }

        @Override
        public Color getBodySelectionBorderColor() {
            return ColorsManager.getColor(SWT.COLOR_BLACK);
        }

        @Override
        public Color getBodyHoveredCellBackground() {
            return ColorsManager.getColor(128, 100, 45);
        }

        @Override
        public Color getBodyHoveredSelectedCellBackground() {
            return ColorsManager.getColor(128, 100, 45);
        }

        @Override
        public Color getBodySelectedCellBackground() {
            return ColorsManager.getColor(90, 80, 32);
        }

        @Override
        public Color getBodyAnchoredCellBackground() {
            return ColorsManager.getColor(90, 80, 32);
        }

        @Override
        public Color getBodyInactiveCellBackground() {
            return ColorsManager.getColor(30, 30, 30);
        }

        @Override
        public Color getBodyInactiveCellForeground() {
            return ColorsManager.getColor(80, 80, 80);
        }

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
