/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.base.Functions.identity;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toMap;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.red.viewers.ListInputStructuredContentProvider;
import org.robotframework.red.viewers.Selections;

import com.google.common.collect.Iterables;

/**
 * @author Michal Anglart
 */
public class SyntaxHighlightingPreferencePage extends RedPreferencePage {

    private final Map<SyntaxHighlightingCategory, ColoringPreference> currentPreferences;

    private StyledText previewText;

    private ColorSelector colorSelector;

    private Button boldButton;

    private Button italicButton;

    private Combo presetColors;

    private ListViewer viewer;

    private final IPropertyChangeListener preferenceListener;

    public SyntaxHighlightingPreferencePage() {
        this.currentPreferences = EnumSet.allOf(SyntaxHighlightingCategory.class).stream().collect(
                toMap(identity(), SyntaxHighlightingCategory::getPreference,
                        (c1, c2) -> {
                            throw new IllegalStateException();
                        },
                        () -> new EnumMap<>(SyntaxHighlightingCategory.class)));

        preferenceListener = event -> {
            final boolean isColoringPref = EnumSet.allOf(SyntaxHighlightingCategory.class)
                    .stream()
                    .map(SyntaxHighlightingCategory::getPreferenceId)
                    .anyMatch(id -> id.equals(event.getProperty()));
            if (isColoringPref) {
                final SyntaxHighlightingCategory category = SyntaxHighlightingCategory
                        .fromPreferenceId(event.getProperty());
                final ColoringPreference newPref = ColoringPreference.fromPreferenceString((String) event.getNewValue());

                currentPreferences.put(category, newPref);
                refreshPreview();
                setPresetLabel();
            }
        };
        getPreferenceStore().addPropertyChangeListener(preferenceListener);
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
        viewer.setContentProvider(new ListInputStructuredContentProvider());
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

        preparePresetsCombo(parentForSingleSettings);

        final Label previewLabel = new Label(parent, SWT.NONE);
        previewLabel.setText("Preview:");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(previewLabel);

        previewText = new StyledText(parent, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(previewText);
        previewText.setText(SyntaxHighlightingPreferencePageSource.source);
        previewText.setEditable(false);
        previewText.setFont(RedTheme.getTextEditorFont());

        new Label(parent, SWT.NONE);

        refreshPreview();
    }

    private void preparePresetsCombo(final Composite parent) {
        final Group presetsGroup = new Group(parent, SWT.NONE);
        GridDataFactory.fillDefaults().indent(0, 20).grab(true, false).span(2, 1).applyTo(presetsGroup);
        GridLayoutFactory.fillDefaults().applyTo(presetsGroup);
        final Label predefinedLabel = new Label(presetsGroup, SWT.NONE);
        predefinedLabel.setText("Use predefined syntax coloring:");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(predefinedLabel);

        presetColors = new Combo(presetsGroup, SWT.READ_ONLY);
        presetColors.add("default");
        presetColors.add("heliophobia");
        presetColors.add("custom");
        presetColors.addSelectionListener(new PresetSelectionListener());
        setPresetLabel();

    }

    private void setPresetLabel() {
        boolean possiblePreset = true;
        for (final SyntaxHighlightingCategory category : EnumSet.allOf(SyntaxHighlightingCategory.class)) {
            if (!currentPreferences.get(category).equals(category.getDefault())) {
                possiblePreset = false;
                break;
            }
        }
        if (possiblePreset) {
            presetColors.select(0);
            return;
        }

        for (final SyntaxHighlightingCategory category : EnumSet.allOf(SyntaxHighlightingCategory.class)) {
            if (!currentPreferences.get(category).equals(category.getDark())) {
                presetColors.select(2);
                return;
            }
        }
        presetColors.select(1);
    }

    private void setButtons() {
        final Optional<SyntaxHighlightingCategory> selected = Selections.getOptionalFirstElement(
                (IStructuredSelection) viewer.getSelection(), SyntaxHighlightingCategory.class);
        if (selected.isPresent()) {
            final SyntaxHighlightingCategory selectedCategory = selected.get();
            final ColoringPreference currentPreference = currentPreferences.get(selectedCategory);

            colorSelector.setColorValue(currentPreference.getRgb());
            boldButton.setSelection((currentPreference.getFontStyle() & SWT.BOLD) != 0);
            italicButton.setSelection((currentPreference.getFontStyle() & SWT.ITALIC) != 0);
        }
    }

    private void refreshPreview() {
        final List<StyleRange> ranges = newArrayList(Iterables.concat(getSectionHeaderRanges(), getCommentsRanges(),
                getSettingRanges(), getDefinitionRanges(), getKeywordCallRanges(), getVariableRanges(),
                getGherkinRanges(), getSpecialTokenRanges()));
        Collections.sort(ranges, (range1, range2) -> Integer.compare(range1.start, range2.start));
        previewText.setStyleRanges(ranges.toArray(new StyleRange[0]));
    }

    private List<StyleRange> getCommentsRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.COMMENT);

        return newArrayList(new StyleRange(689, 54, preference.getColor(), null, preference.getFontStyle()));
    }

    private List<StyleRange> getSectionHeaderRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.SECTION_HEADER);

        return newArrayList(new StyleRange(0, 16, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(142, 17, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(263, 16, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(640, 18, preference.getColor(), null, preference.getFontStyle()));
    }

    private List<StyleRange> getSettingRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.SETTING);

        return newArrayList(new StyleRange(17, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(63, 10, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(103, 13, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(291, 11, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(312, 15, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(619, 8, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(672, 9, preference.getColor(), null, preference.getFontStyle()));
    }

    private List<StyleRange> getDefinitionRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.DEFINITION);

        return newArrayList(new StyleRange(280, 9, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(659, 11, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(832, 13, preference.getColor(), null, preference.getFontStyle()));
    }

    private List<StyleRange> getKeywordCallRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.KEYWORD_CALL);

        return newArrayList(new StyleRange(80, 3, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(120, 3, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(377, 12, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(413, 12, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(437, 4, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(471, 1, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(490, 12, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(515, 1, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(534, 12, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(562, 1, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(581, 8, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(755, 9, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(772, 15, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(808, 3, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(853, 19, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(879, 19, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(904, 25, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(936, 17, preference.getColor(), null, preference.getFontStyle()));
    }

    private List<StyleRange> getVariableRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.VARIABLE);

        return newArrayList(new StyleRange(160, 26, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(196, 27, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(235, 26, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(306, 4, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(363, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(393, 4, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(399, 10, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(429, 4, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(445, 4, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(465, 4, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(476, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(506, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(520, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(550, 10, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(567, 10, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(597, 10, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(610, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(631, 7, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(745, 6, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(791, 5, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(800, 6, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(815, 15, preference.getColor(), null, preference.getFontStyle()));
    }

    private List<StyleRange> getGherkinRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.GHERKIN);

        return newArrayList(new StyleRange(847, 5, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(874, 4, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(900, 3, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(931, 4, preference.getColor(), null, preference.getFontStyle()));
    }

    private List<StyleRange> getSpecialTokenRanges() {
        final ColoringPreference preference = currentPreferences.get(SyntaxHighlightingCategory.SPECIAL);

        return newArrayList(new StyleRange(44, 9, preference.getColor(), null, preference.getFontStyle()),
                new StyleRange(453, 8, preference.getColor(), null, preference.getFontStyle()));
    }

    @Override
    protected void performDefaults() {
        for (final SyntaxHighlightingCategory category : EnumSet.allOf(SyntaxHighlightingCategory.class)) {
            currentPreferences.put(category, category.getDefault());
        }
        refreshPreview();
        presetColors.select(0);
        setButtons();

        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
        currentPreferences.entrySet().forEach(
                entry -> store.setValue(entry.getKey().getPreferenceId(), entry.getValue().toPreferenceString()));

        return super.performOk();
    }

    @Override
    public void dispose() {
        getPreferenceStore().removePropertyChangeListener(preferenceListener);

        super.dispose();
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
            final Optional<SyntaxHighlightingCategory> selected = Selections.getOptionalFirstElement(
                    (IStructuredSelection) event.getSelection(), SyntaxHighlightingCategory.class);
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
                setPresetLabel();
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
            final Optional<SyntaxHighlightingCategory> selected = Selections.getOptionalFirstElement(
                    (IStructuredSelection) viewer.getSelection(), SyntaxHighlightingCategory.class);
            if (selected.isPresent()) {
                final SyntaxHighlightingCategory selectedCategory = selected.get();
                final ColoringPreference currentPreference = currentPreferences.get(selectedCategory);

                currentPreferences.put(selectedCategory,
                        new ColoringPreference(currentPreference.getRgb(), currentPreference.getFontStyle() ^ style));

                refreshPreview();
                setPresetLabel();
            } else {
                throw new IllegalStateException("This button should be disabled when there is no category selected!");
            }
        }
    }

    private class PresetSelectionListener extends SelectionAdapter {

        @Override
        public void widgetSelected(final SelectionEvent e) {
            // set preset preferences
            final int selectedId = presetColors.getSelectionIndex();
            if (selectedId == 0) {
                setLightPreset();
            } else if (selectedId == 1) {
                setDarkPreset();
            }
            setButtons();
            refreshPreview();
        }

        private void setLightPreset() {
            performDefaults(); // light is default
        }

        private void setDarkPreset() {
            for (final SyntaxHighlightingCategory category : EnumSet.allOf(SyntaxHighlightingCategory.class)) {
                currentPreferences.put(category, category.getDark());
            }
        }

    }
}
