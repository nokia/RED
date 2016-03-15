/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

    private Map<String, Composite> tagComposites = new HashMap<>();

    private final List<TagsListener> tagsListeners;

    private Text tagNameText;

    private Button addTagButton;

    private TagsProposalsSupport tagsSupport;

    private boolean shouldRebuildProposals = false;

    TagsComposite(final Composite parent) {
        super(parent, SWT.NONE);
        this.tagComposites = new HashMap<>();
        this.tagsListeners = new ArrayList<>();

        RowLayoutFactory.fillDefaults().fill(true).type(SWT.HORIZONTAL).spacing(2).wrap(true).applyTo(this);

        createTags(new ArrayList<String>());
        createDefinitionText();
        createAddingButton();
    }

    private void createDefinitionText() {
        tagNameText = new Text(this, SWT.BORDER);
        tagNameText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent e) {
                if (e.character == SWT.CR) {
                    addTag();
                }
            }
        });
        tagNameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                for (final TagsListener tagsListener : tagsListeners) {
                    tagsListener.newTagIsEdited();
                }
            }
        });
        RowDataFactory.swtDefaults().hint(60, 17).applyTo(tagNameText);
    }

    private void createTags(final Collection<String> tags) {
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
        newTagRemoveBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                for (final TagsListener listener : tagsListeners) {
                    listener.removeTagRequested(tag);
                }
            }
        });
        
        return newTag;
    }

    private void createAddingButton() {
        addTagButton = new Button(this, SWT.PUSH);
        addTagButton.setImage(ImagesManager.getImage(RedImages.getAddImage()));
        addTagButton.setToolTipText("Add new tag");
        RowDataFactory.swtDefaults().hint(22, 22).applyTo(addTagButton);
        addTagButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                addTag();
            }
        });
    }

    private void addTag() {
        final String text = tagNameText.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        for (final TagsListener listener : tagsListeners) {
            listener.addTagRequested(text);
        }
        tagNameText.setText("");
        tagNameText.setFocus();
    }

    @Override
    public void dispose() {
        super.dispose();

        tagComposites.clear();
        tagsListeners.clear();
    }

    void installTagsProposalsSupport(final Map<IResource, List<String>> suitesToRun) {
        if (tagsSupport == null || shouldRebuildProposals) {
            tagsSupport = new TagsProposalsSupport(suitesToRun);
            tagsSupport.install(tagNameText);
        }
        shouldRebuildProposals = false;
    }

    void markProposalsToRebuild() {
        this.shouldRebuildProposals = true;
    }

    void setInput(final Collection<String> tags) {
        try {
            setRedraw(false);
            for (final Composite tagComp : tagComposites.values()) {
                tagComp.dispose();
            }
            createTags(tags);
            
            
            final Control[] childrens = getChildren();
            final Control currentLast = childrens[childrens.length - 1];

            tagNameText.moveBelow(currentLast);
            addTagButton.moveBelow(tagNameText);
        } finally {
            setRedraw(true);
        }
    }

    void addTagsListener(final TagsListener listener) {
        tagsListeners.add(listener);
    }

    void removeTagsListener(final TagsListener listener) {
        tagComposites.remove(listener);
    }

    boolean userIsFocusingOnNewTab() {
        return tagNameText.isFocusControl() && !tagNameText.getText().isEmpty();
    }

    void applyCurrentTag() {
        if (!tagNameText.getText().isEmpty()) {
            addTag();
        }
    }

    interface TagsListener {

        void newTagIsEdited();

        void addTagRequested(String tag);

        void removeTagRequested(String tag);
    }
}
