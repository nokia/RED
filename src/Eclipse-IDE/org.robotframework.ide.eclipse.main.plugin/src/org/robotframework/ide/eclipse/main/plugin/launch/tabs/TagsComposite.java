/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.red.swt.Listeners.keyReleasedAdapter;
import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ImagesManager;

/**
 * @author Michal Anglart
 *
 */
class TagsComposite extends Composite {

    private final TagsProposalsSupport tagsSupport;
    private final TagsListener listener;

    private final Map<String, Composite> tagComposites;
    private Text tagNameText;
    private Button addTagButton;

    TagsComposite(final Composite parent, final TagsProposalsSupport tagsSupport) {
        this(parent, tagsSupport, null);
    }

    TagsComposite(final Composite parent, final TagsProposalsSupport tagsSupport, final TagsListener listener) {
        super(parent, SWT.NONE);
        this.listener = listener;
        this.tagComposites = new LinkedHashMap<>();
        this.tagsSupport = tagsSupport;

        RowLayoutFactory.fillDefaults().fill(true).type(SWT.HORIZONTAL).spacing(2).wrap(true).applyTo(this);
        createTagsControls(new ArrayList<String>());
        createDefinitionText();
        createAddingButton();
    }

    private void createDefinitionText() {
        tagNameText = new Text(this, SWT.BORDER);
        RowDataFactory.swtDefaults().hint(60, 17).applyTo(tagNameText);
        tagNameText.addKeyListener(keyReleasedAdapter(e -> {
            if (e.character == SWT.CR) {
                addTag();
            }
        }));
        tagNameText.addModifyListener(e -> listener.newTagIsEdited());
        tagsSupport.install(tagNameText);
    }

    private void createTagsControls(final Collection<String> tags) {
        for (final String tag : tags) {
            tagComposites.put(tag, createTagControlFor(tag));
        }
    }

    private Composite createTagControlFor(final String tag) {
        final Composite newTag = new Composite(this, SWT.BORDER);
        newTag.setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        newTag.setBackgroundMode(SWT.INHERIT_FORCE);
        RowDataFactory.swtDefaults().applyTo(newTag);
        GridLayoutFactory.fillDefaults().numColumns(2).spacing(1, 0).applyTo(newTag);

        final CLabel newTagLabel = new CLabel(newTag, SWT.NONE);
        newTagLabel.setImage(ImagesManager.getImage(RedImages.getTagImage()));
        newTagLabel.setText(tag);
        GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 20).applyTo(newTagLabel);

        final Button newTagRemoveBtn = new Button(newTag, SWT.PUSH);
        newTagRemoveBtn.setImage(ImagesManager.getImage(RedImages.getRemoveTagImage()));
        newTagRemoveBtn.setToolTipText("Remove tag");
        GridDataFactory.fillDefaults().hint(18, 16).applyTo(newTagRemoveBtn);
        newTagRemoveBtn.addSelectionListener(widgetSelectedAdapter(e -> removeTag(tag)));
        return newTag;
    }

    private void createAddingButton() {
        addTagButton = new Button(this, SWT.PUSH);
        addTagButton.setImage(ImagesManager.getImage(RedImages.getAddImage()));
        addTagButton.setToolTipText("Add new tag");
        RowDataFactory.swtDefaults().hint(22, 22).applyTo(addTagButton);
        addTagButton.addSelectionListener(widgetSelectedAdapter(e -> addTag()));
    }

    @Override
    public void dispose() {
        tagComposites.clear();
        super.dispose();
    }

    void setInput(final Collection<String> tags) {
        try {
            setRedraw(false);
            for (final Composite tagComp : tagComposites.values()) {
                tagComp.dispose();
            }
            tagComposites.clear();
            createTagsControls(tags);

            final Control[] children = getChildren();
            final Control currentLast = children[children.length - 1];

            tagNameText.moveBelow(currentLast);
            addTagButton.moveBelow(tagNameText);
        } finally {
            setRedraw(true);
        }
    }

    List<String> getInput() {
        return newArrayList(tagComposites.keySet());
    }

    private void addTag() {
        final String text = tagNameText.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        tagNameText.setText("");
        tagNameText.setFocus();
        if (tagComposites.keySet().contains(text)) {
            return;
        }
        try {
            setRedraw(false);
            tagComposites.put(text, createTagControlFor(text));

            final Control[] children = getChildren();
            final Control currentLast = children[children.length - 1];

            tagNameText.moveBelow(currentLast);
            addTagButton.moveBelow(tagNameText);
        } finally {
            setRedraw(true);
            listener.tagAdded(text);
        }
    }

    private void removeTag(final String tag) {
        try {
            setRedraw(false);
            tagComposites.remove(tag).dispose();
        } finally {
            setRedraw(true);
            listener.tagRemoved(tag);
        }
    }

    boolean userIsFocusingOnNewTab() {
        return tagNameText.isFocusControl() && !tagNameText.getText().isEmpty();
    }

    public interface TagsListener {

        void newTagIsEdited();

        void tagAdded(String tag);

        void tagRemoved(String tag);
    }
}
