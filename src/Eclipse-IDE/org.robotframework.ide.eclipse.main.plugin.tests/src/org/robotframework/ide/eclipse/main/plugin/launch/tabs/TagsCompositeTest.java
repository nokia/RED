/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.Condition;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.TagsComposite.TagsListener;
import org.robotframework.red.junit.ShellProvider;

public class TagsCompositeTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void whenCompositeIsCreated_itHasNoTagsControls() {
        final TagsComposite composite = new TagsComposite(shellProvider.getShell(), new TagsProposalsSupport());

        assertThat(composite).has(noTagControls());
        assertThat(composite.getInput()).isEmpty();
    }

    @Test
    public void whenCompositeHasInputProvided_tagsControlsAreCreated() {
        final TagsComposite composite = new TagsComposite(shellProvider.getShell(), new TagsProposalsSupport());
        composite.setInput(newArrayList("tag1", "tag2", "tag3"));

        assertThat(composite).has(tagControls("tag1", "tag2", "tag3"));
        assertThat(composite.getInput()).containsExactly("tag1", "tag2", "tag3");
    }

    @Test
    public void whenInputChanges_tagsControlsAreRecreated() {
        final TagsComposite composite = new TagsComposite(shellProvider.getShell(), new TagsProposalsSupport());
        composite.setInput(newArrayList("tag1", "tag2", "tag3"));
        composite.setInput(newArrayList("tag3", "tag4", "tag5"));

        assertThat(composite).has(tagControls("tag3", "tag4", "tag5"));
        assertThat(composite.getInput()).containsExactly("tag3", "tag4", "tag5");
    }

    @Test
    public void whenTagIsAddedUsingButton_tagControlIsCreatedAndListenerGetsNotified() {
        final TagsListener listener = mock(TagsListener.class);
        final TagsComposite composite = new TagsComposite(shellProvider.getShell(), new TagsProposalsSupport(),
                listener);
        composite.setInput(newArrayList("tag1", "tag2"));

        tagsText(composite).setText("new_tag");
        tagsAddingButton(composite).notifyListeners(SWT.Selection, new Event());

        assertThat(composite).has(tagControls("tag1", "tag2", "new_tag"));
        assertThat(composite.getInput()).containsExactly("tag1", "tag2", "new_tag");
        assertThat(tagsText(composite).getText()).isEmpty();
        verify(listener).tagAdded("new_tag");
    }

    @Test
    public void whenTagIsAddedUsingEnterKeyPress_tagControlIsCreatedAndListenerGetsNotified() {
        final TagsListener listener = mock(TagsListener.class);
        final TagsComposite composite = new TagsComposite(shellProvider.getShell(), new TagsProposalsSupport(),
                listener);
        composite.setInput(newArrayList("tag1", "tag2"));

        tagsText(composite).setText("new_tag");
        final Event event = new Event();
        event.character = SWT.CR;
        tagsText(composite).notifyListeners(SWT.KeyUp, event);

        assertThat(composite).has(tagControls("tag1", "tag2", "new_tag"));
        assertThat(composite.getInput()).containsExactly("tag1", "tag2", "new_tag");
        assertThat(tagsText(composite).getText()).isEmpty();
        verify(listener).tagAdded("new_tag");
    }

    @Test
    public void whenAlreadyExistingTagIsAdded_tagControlAreNotChanged() {
        final TagsListener listener = mock(TagsListener.class);
        final TagsComposite composite = new TagsComposite(shellProvider.getShell(), new TagsProposalsSupport(),
                listener);
        composite.setInput(newArrayList("tag1", "tag2"));

        tagsText(composite).setText("tag2");
        tagsAddingButton(composite).notifyListeners(SWT.Selection, new Event());

        assertThat(composite).has(tagControls("tag1", "tag2"));
        assertThat(composite.getInput()).containsExactly("tag1", "tag2");
        assertThat(tagsText(composite).getText()).isEmpty();
        verify(listener, never()).tagAdded(Matchers.anyString());
    }

    @Test
    public void whenEmptyStringIsAdded_tagControlAreNotChanged() {
        final TagsListener listener = mock(TagsListener.class);
        final TagsComposite composite = new TagsComposite(shellProvider.getShell(), new TagsProposalsSupport(),
                listener);
        composite.setInput(newArrayList("tag1", "tag2"));

        tagsText(composite).setText("");
        tagsAddingButton(composite).notifyListeners(SWT.Selection, new Event());

        assertThat(composite).has(tagControls("tag1", "tag2"));
        assertThat(composite.getInput()).containsExactly("tag1", "tag2");
        assertThat(tagsText(composite).getText()).isEmpty();
        verify(listener, never()).tagAdded(Matchers.anyString());
    }

    @Test
    public void whenTagIsRemoved_tagControlIsDisposedAndListenerGetsNotified() {
        final TagsListener listener = mock(TagsListener.class);
        final TagsComposite composite = new TagsComposite(shellProvider.getShell(), new TagsProposalsSupport(),
                listener);
        composite.setInput(newArrayList("tag1", "tag2", "tag3"));

        removeButton(composite, "tag2").notifyListeners(SWT.Selection, new Event());

        assertThat(composite).has(tagControls("tag1", "tag3"));
        assertThat(composite.getInput()).containsExactly("tag1", "tag3");
        verify(listener).tagRemoved("tag2");
    }

    private static Condition<? super TagsComposite> noTagControls() {
        return tagControls();
    }

    private static Condition<? super TagsComposite> tagControls(final String... tags) {
        return new Condition<TagsComposite>() {
            @Override
            public boolean matches(final TagsComposite composite) {
                final Iterable<Composite> tagComposites = filter(newArrayList(composite.getChildren()),
                        Composite.class);
                final Set<String> tagLabels = new HashSet<>();
                for (final Composite tagComposite : tagComposites) {
                    final Iterable<CLabel> labels = filter(newArrayList(tagComposite.getChildren()), CLabel.class);
                    final CLabel label = getFirst(labels, null);
                    tagLabels.add(label.getText());
                }
                return newHashSet(tags).equals(tagLabels);
            }
        };
    }

    private static Button removeButton(final TagsComposite composite, final String tag) {
        final Iterable<Composite> tagComposites = filter(newArrayList(composite.getChildren()), Composite.class);
        for (final Composite tagComposite : tagComposites) {
            final Iterable<CLabel> labels = filter(newArrayList(tagComposite.getChildren()), CLabel.class);
            final CLabel label = getFirst(labels, null);
            if (label.getText().equals(tag)) {
                return getFirst(filter(newArrayList(tagComposite.getChildren()), Button.class), null);
            }
        }
        return null;
    }

    private static Text tagsText(final TagsComposite composite) {
        return getFirst(filter(newArrayList(composite.getChildren()), Text.class), null);
    }

    private static Button tagsAddingButton(final TagsComposite composite) {
        return getFirst(filter(newArrayList(composite.getChildren()), Button.class), null);
    }
}
