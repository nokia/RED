/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.TagsComposite.TagsListener;

class IncludeExcludeTagsComposite extends Composite {

    private final Button includeTagsBtn;

    private final TagsComposite includedTagsComposite;

    private final Button excludeTagsBtn;

    private final TagsComposite excludedTagsComposite;

    private final SelectionListener checkBoxListener;

    private final TagsListener tagsListener;

    private final TagsProposalsSupport tagsSupport;

    IncludeExcludeTagsComposite(final Composite parent, final SelectionListener checkBoxListener,
            final TagsListener tagsListener) {
        super(parent, SWT.NONE);
        this.checkBoxListener = checkBoxListener;
        this.tagsListener = tagsListener;
        tagsSupport = new TagsProposalsSupport();

        GridLayoutFactory.fillDefaults().numColumns(3).applyTo(this);

        includeTagsBtn = createCheckBoxButton(this, "Only run tests with these tags:");
        includedTagsComposite = createTagsComposite(this, tagsSupport);

        excludeTagsBtn = createCheckBoxButton(this, "Skip tests with these tags:");
        excludedTagsComposite = createTagsComposite(this, tagsSupport);
    }

    private Button createCheckBoxButton(final Composite parent, final String text) {
        final Button button = new Button(parent, SWT.CHECK);
        GridDataFactory.fillDefaults().indent(5, 3).align(SWT.BEGINNING, SWT.BEGINNING).applyTo(button);
        button.setText(text);
        button.addSelectionListener(checkBoxListener);
        return button;
    }

    private TagsComposite createTagsComposite(final Composite parent, final TagsProposalsSupport tagsSupport) {
        final TagsComposite composite = new TagsComposite(parent, tagsSupport, tagsListener);
        GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).grab(true, true).span(2, 1).applyTo(composite);
        return composite;
    }

    void setInput(final boolean includeTagsSelected, final Collection<String> includedTags,
            final boolean excludeTagsSelected, final Collection<String> excludedTags) {
        includeTagsBtn.setSelection(includeTagsSelected);
        includedTagsComposite.setInput(includedTags);
        excludeTagsBtn.setSelection(excludeTagsSelected);
        excludedTagsComposite.setInput(excludedTags);
    }

    boolean isIncludeTagsEnabled() {
        return includeTagsBtn.getSelection();
    }

    List<String> getIncludedTags() {
        return includedTagsComposite.getInput();
    }

    boolean isExcludeTagsEnabled() {
        return excludeTagsBtn.getSelection();
    }

    List<String> getExcludedTags() {
        return excludedTagsComposite.getInput();
    }

    boolean userDoNotWriteNewTagCurrently() {
        // we don't want to Enter key launch whole configuration when user is editing tags
        return !excludedTagsComposite.userIsFocusingOnNewTab() && !includedTagsComposite.userIsFocusingOnNewTab();
    }

    void switchTo(final String projectName, final Map<IResource, List<String>> suitesToRun) {
        tagsSupport.switchTo(projectName, suitesToRun);
    }

}
