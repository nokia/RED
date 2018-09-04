/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;

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
                final ColoringPreference newPref = ColoringPreference
                        .fromPreferenceString((String) event.getNewValue());

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
        viewer.setInput(new ArrayList<>(currentPreferences.keySet()));

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
        previewText.setFont(RedTheme.Fonts.getTextEditorFont());

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
        final StyleRange[] sortedRanges = createStyleRanges()
                .sorted((range1, range2) -> Integer.compare(range1.start, range2.start))
                .toArray(StyleRange[]::new);
        previewText.setStyleRanges(sortedRanges);
    }

    @VisibleForTesting
    Stream<StyleRange> createStyleRanges() {
        return Stream.of(
                createStyleRanges(SyntaxHighlightingCategory.SECTION_HEADER,
                        SyntaxHighlightingPreferencePageSource.sectionHeaderStartIndexes,
                        SyntaxHighlightingPreferencePageSource.sectionHeaderRangeLengths),
                createStyleRanges(SyntaxHighlightingCategory.SETTING,
                        SyntaxHighlightingPreferencePageSource.settingStartIndexes,
                        SyntaxHighlightingPreferencePageSource.settingRangeLengths),
                createStyleRanges(SyntaxHighlightingCategory.DEFINITION,
                        SyntaxHighlightingPreferencePageSource.definitionStartIndexes,
                        SyntaxHighlightingPreferencePageSource.definitionRangeLengths),
                createStyleRanges(SyntaxHighlightingCategory.VARIABLE,
                        SyntaxHighlightingPreferencePageSource.variableStartIndexes,
                        SyntaxHighlightingPreferencePageSource.variableRangeLengths),
                createStyleRanges(SyntaxHighlightingCategory.KEYWORD_CALL,
                        SyntaxHighlightingPreferencePageSource.keywordCallStartIndexes,
                        SyntaxHighlightingPreferencePageSource.keywordCallRangeLengths),
                createStyleRanges(SyntaxHighlightingCategory.KEYWORD_CALL_QUOTE,
                        SyntaxHighlightingPreferencePageSource.keywordCallQuoteStartIndexes,
                        SyntaxHighlightingPreferencePageSource.keywordCallQuoteRangeLengths),
                createStyleRanges(SyntaxHighlightingCategory.COMMENT,
                        SyntaxHighlightingPreferencePageSource.commentStartIndexes,
                        SyntaxHighlightingPreferencePageSource.commentRangeLengths),
                createStyleRanges(SyntaxHighlightingCategory.GHERKIN,
                        SyntaxHighlightingPreferencePageSource.gherkinStartIndexes,
                        SyntaxHighlightingPreferencePageSource.gherkinRangeLengths),
                createStyleRanges(SyntaxHighlightingCategory.TASKS,
                        SyntaxHighlightingPreferencePageSource.taskStartIndexes,
                        SyntaxHighlightingPreferencePageSource.taskRangeLengths),
                createStyleRanges(SyntaxHighlightingCategory.SPECIAL,
                        SyntaxHighlightingPreferencePageSource.specialStartIndexes,
                        SyntaxHighlightingPreferencePageSource.specialRangeLengths))
                .flatMap(identity());
    }

    private Stream<StyleRange> createStyleRanges(final SyntaxHighlightingCategory category, final String startIndexes,
            final String rangeLengths) {
        final ColoringPreference preference = currentPreferences.get(category);
        final Pattern separatorPattern = Pattern.compile("\\s*,\\s*");
        final Stream<Integer> startIndexesStream = separatorPattern.splitAsStream(startIndexes).map(Integer::valueOf);
        final Stream<Integer> rangeLengthsStream = separatorPattern.splitAsStream(rangeLengths).map(Integer::valueOf);
        return Streams.zip(startIndexesStream, rangeLengthsStream, (start, length) -> new StyleRange(start, length,
                preference.getColor(), null, preference.getFontStyle()));
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
        currentPreferences.forEach(
                (category, preference) -> store.setValue(category.getPreferenceId(), preference.toPreferenceString()));

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
