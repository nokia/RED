/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.StructuredContentProvider;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

/**
 * @author Michal Anglart
 */
public class SyntaxHighlightingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private final Map<SyntaxHighlightingCategory, ColoringPreference> currentPreferences;

    private StyledText previewText;

    private ColorSelector colorSelector;

    private Button boldButton;

    private Button italicButton;

    private ListViewer viewer;

    public SyntaxHighlightingPreferencePage() {
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, RedPlugin.PLUGIN_ID));

        this.currentPreferences = new EnumMap<>(SyntaxHighlightingCategory.class);
        for (final SyntaxHighlightingCategory category : EnumSet.allOf(SyntaxHighlightingCategory.class)) {
            currentPreferences.put(category, category.getPreference());
        }
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected Control createContents(final Composite parent) {
        createInsertionGroup(parent);
        return parent;
    }

    private void createInsertionGroup(final Composite parent) {
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);

        viewer = new ListViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getControl());
        viewer.setContentProvider(new SyntaxHighlightingCategoriesContentProvider());
        viewer.setLabelProvider(new SyntaxHighlightingCategoriesLabelProvider());
        viewer.addSelectionChangedListener(new SyntaxHighlightingCategoriesSelectionListener());
        viewer.setInput(newArrayList(currentPreferences.keySet()));

        final Composite parentForSingleSettings = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(false, true).applyTo(parentForSingleSettings);
        GridLayoutFactory.fillDefaults().applyTo(parentForSingleSettings);
        colorSelector = new ColorSelector(parentForSingleSettings);
        colorSelector.setColorValue(new RGB(230, 230, 230));
        colorSelector.setEnabled(false);
        colorSelector.addListener(new ColorSelectorChangedListener());
        boldButton = new Button(parentForSingleSettings, SWT.CHECK);
        boldButton.setText("Bold");
        boldButton.setEnabled(false);
        boldButton.addSelectionListener(new FontStyleButtonSelectionListener(SWT.BOLD));
        italicButton = new Button(parentForSingleSettings, SWT.CHECK);
        italicButton.setText("Italic");
        italicButton.setEnabled(false);
        italicButton.addSelectionListener(new FontStyleButtonSelectionListener(SWT.ITALIC));

        final Label label = new Label(parent, SWT.NONE);
        label.setText("Preview:");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(label);

        previewText = new StyledText(parent, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(previewText);
        previewText.setText(SyntaxHighlightingPreferencePageSource.source);
        previewText.setEditable(false);
        previewText.setFont(RedTheme.getTextEditorFont());

        new Label(parent, SWT.NONE);

        refreshPreview();
    }

    private void refreshPreview() {
        @SuppressWarnings("unchecked")
        final List<StyleRange> ranges = newArrayList(Iterables.concat(getSectionHeaderRanges(), getCommentsRanges(),
                getSettingRanges(), getDefinitionRanges(), getKeywordCallRanges(), getVariableRanges(), getGherkinRanges()));
        Collections.sort(ranges, new Comparator<StyleRange>() {

            @Override
            public int compare(final StyleRange range1, final StyleRange range2) {
                return Integer.compare(range1.start, range2.start);
            }
        });
        previewText.setStyleRanges(ranges.toArray(new StyleRange[0]));
    }

    private List<StyleRange> getCommentsRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.COMMENT);

        return newArrayList(new StyleRange(664, 54, preference.getColor(), null, preference.getFontStyle()));
    }

    private List<StyleRange> getSectionHeaderRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.SECTION_HEADER);

        return newArrayList(new StyleRange(0, 16, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(120, 17, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(241, 16, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(615, 18, preference.getColor(), null, preference.getFontStyle())
        );
    }

    private List<StyleRange> getSettingRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.SETTING);

        return newArrayList(new StyleRange(17, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(41, 10, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(81, 13, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(269, 11, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(290, 15, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(594, 8, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(647, 9, preference.getColor(), null, preference.getFontStyle())
        );
    }

    private List<StyleRange> getDefinitionRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.DEFINITION);

        return newArrayList(new StyleRange(258, 9, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(634, 11, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(807, 13, preference.getColor(), null, preference.getFontStyle())
        );
    }

    private List<StyleRange> getKeywordCallRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.KEYWORD_CALL);

        return newArrayList(new StyleRange(58, 3, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(98, 3, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(355, 12, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(391, 12, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(467, 12, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(510, 12, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(556, 8, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(730, 9, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(747, 15, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(783, 3, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(828, 19, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(854, 19, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(879, 25, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(911, 17, preference.getColor(), null, preference.getFontStyle())
        );
    }

    private List<StyleRange> getVariableRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.VARIABLE);

        return newArrayList(new StyleRange(138, 26, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(174, 27, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(213, 26, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(284, 4, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(341, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(371, 4, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(377, 10, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(407, 4, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(423, 4, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(443, 4, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(453, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(483, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(496, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(526, 10, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(542, 10, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(572, 10, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(585, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(606, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(720, 6, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(766, 5, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(775, 6, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(790, 15, preference.getColor(), null, preference.getFontStyle())
        );
    }

    private List<StyleRange> getGherkinRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.GHERKIN);

        return newArrayList(new StyleRange(822, 5, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(849, 4, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(875, 3, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(906, 4, preference.getColor(), null, preference.getFontStyle())
        );
    }

    @Override
    protected void performDefaults() {
        for (final SyntaxHighlightingCategory category : EnumSet.allOf(SyntaxHighlightingCategory.class)) {
            currentPreferences.put(category, category.getDefault());
        }
        refreshPreview();
        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();

        for (final Entry<SyntaxHighlightingCategory, ColoringPreference> entry : currentPreferences.entrySet()) {
            final SyntaxHighlightingCategory category = entry.getKey();
            final ColoringPreference pref = entry.getValue();

            store.setValue(RedPreferencesInitializer.getFontStyleIdentifierFor(category), pref.getFontStyle());
            store.setValue(RedPreferencesInitializer.getRedFactorIdentifierFor(category), pref.getRgb().red);
            store.setValue(RedPreferencesInitializer.getGreenFactorIdentifierFor(category), pref.getRgb().green);
            store.setValue(RedPreferencesInitializer.getBlueFactorIdentifierFor(category), pref.getRgb().blue);
        }

        return super.performOk();
    }

    private class SyntaxHighlightingCategoriesContentProvider extends StructuredContentProvider {
        @Override
        public Object[] getElements(final Object inputElement) {
            return ((List<?>) inputElement).toArray();
        }
    }

    private class SyntaxHighlightingCategoriesLabelProvider extends LabelProvider {
        @Override
        public String getText(final Object element) {
            return ((SyntaxHighlightingCategory) element).getShortDescription();
        }
    }

    private class SyntaxHighlightingCategoriesSelectionListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(final SelectionChangedEvent event) {
            final Optional<SyntaxHighlightingCategory> selected = Selections.getOptionalFirstElement((IStructuredSelection) event.getSelection(),
                    SyntaxHighlightingCategory.class);
            if (selected.isPresent()) {
                final SyntaxHighlightingCategory selectedCategory = selected.get();
                final ColoringPreference currentPreference = currentPreferences.get(selectedCategory);

                colorSelector.setEnabled(true);
                colorSelector.setColorValue(currentPreference.getRgb());
                boldButton.setEnabled(true);
                boldButton.setSelection((currentPreference.getFontStyle() & SWT.BOLD) != 0);
                italicButton.setEnabled(true);
                italicButton.setSelection((currentPreference.getFontStyle() & SWT.ITALIC) != 0);
            } else {
                colorSelector.setEnabled(false);
                colorSelector.setColorValue(new RGB(230, 230, 230));
                boldButton.setEnabled(false);
                boldButton.setSelection(false);
                italicButton.setEnabled(false);
                italicButton.setSelection(false);
            }
            refreshPreview();
        }
    }

    public class ColorSelectorChangedListener implements IPropertyChangeListener {

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            final Optional<SyntaxHighlightingCategory> selected = Selections.getOptionalFirstElement(
                    (IStructuredSelection) viewer.getSelection(), SyntaxHighlightingCategory.class);
            if (selected.isPresent()) {
                final SyntaxHighlightingCategory selectedCategory = selected.get();
                final ColoringPreference currentPreference = currentPreferences.get(selectedCategory);

                final RGB newColor = (RGB) event.getNewValue();
                currentPreferences.put(selectedCategory,
                        new ColoringPreference(newColor, currentPreference.getFontStyle()));

                refreshPreview();
            } else {
                throw new IllegalStateException("This button should be disabled when there is no category selected!");
            }
        }
    }

    private class FontStyleButtonSelectionListener extends SelectionAdapter {

        private final int style;

        public FontStyleButtonSelectionListener(final int style) {
            this.style = style;
        }

        @Override
        public void widgetSelected(final SelectionEvent e) {
            final Optional<SyntaxHighlightingCategory> selected = 
                    Selections.getOptionalFirstElement((IStructuredSelection) viewer.getSelection(), 
                            SyntaxHighlightingCategory.class);
            if (selected.isPresent()) {
                final SyntaxHighlightingCategory selectedCategory = selected.get();
                final ColoringPreference currentPreference = currentPreferences.get(selectedCategory);

                currentPreferences.put(selectedCategory,
                        new ColoringPreference(currentPreference.getRgb(), currentPreference.getFontStyle() ^ style));

                refreshPreview();
            } else {
                throw new IllegalStateException("This button should be disabled when there is no category selected!");
            }
        }
    }
}
