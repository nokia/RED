/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.forms;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.Twistie;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;

public class SectionsTest {

    private static RedFormToolkit toolkit;

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @BeforeClass
    public static void beforeSuite() {
        toolkit = new RedFormToolkit(Display.getDefault());
    }

    @AfterClass
    public static void afterSuite() {
        toolkit.dispose();
        toolkit = null;
    }

    @Test
    public void whenOneSectionIsMaximized_theOthersAreMinimized() {
        final List<Section> sections = prepareSections(shellProvider.getShell(), 3);

        assertThat(sections.get(0).isExpanded()).isTrue();
        assertThat(sections.get(1).isExpanded()).isTrue();
        assertThat(sections.get(2).isExpanded()).isTrue();

        Sections.maximizeChosenSectionAndMinimalizeOthers(sections.get(1));

        assertThat(sections.get(0).isExpanded()).isFalse();
        assertThat(sections.get(1).isExpanded()).isTrue();
        assertThat(sections.get(2).isExpanded()).isFalse();
    }

    @Test
    public void whenMaximizingPossibillityIsInstalled_theSectionHasToolbarButton() {
        final List<Section> sections = prepareSections(shellProvider.getShell(), 2);

        Sections.installMaximazingPossibility(sections.get(1));

        assertThat(sections.get(0).getTextClient()).isNull();

        final Control textClient = sections.get(1).getTextClient();
        assertThat(textClient).isInstanceOf(ToolBar.class);
        final ToolBar toolBar = (ToolBar) textClient;
        assertThat(toolBar.getItemCount()).isEqualTo(1);
    }

    @Test
    public void whenMaximizingButtonIsClicked_theSectionIsMaximizedAndOtherAreMinimized() {
        final List<Section> sections = prepareSections(shellProvider.getShell(), 3);

        Sections.installMaximazingPossibility(sections.get(1));

        assertThat(sections.get(0).isExpanded()).isTrue();
        assertThat(sections.get(1).isExpanded()).isTrue();
        assertThat(sections.get(2).isExpanded()).isTrue();

        final ToolBar toolBar = (ToolBar) sections.get(1).getTextClient();
        toolBar.getItem(0).notifyListeners(SWT.Selection, new Event());

        assertThat(sections.get(0).isExpanded()).isFalse();
        assertThat(sections.get(1).isExpanded()).isTrue();
        assertThat(sections.get(2).isExpanded()).isFalse();
    }

    @Test
    public void cellGrabbingIsProperlyUpdatedOnExpansionChange_whenSuchFeatureIsInstalled() {
        final List<Section> sections = prepareSections(shellProvider.getShell(), 1);

        Sections.switchGridCellGrabbingOnExpansion(sections.get(0));
        final GridData gridData = (GridData) sections.get(0).getLayoutData();

        assertThat(gridData.grabExcessVerticalSpace).isTrue();

        Twistie twistie = null;
        for (final Control control : sections.get(0).getChildren()) {
            if (control instanceof Twistie) {
                twistie = (Twistie) control;
                break;
            }
        }

        twistie.notifyListeners(SWT.KeyDown, createKeyEvent());
        assertThat(gridData.grabExcessVerticalSpace).isFalse();

        twistie.notifyListeners(SWT.KeyDown, createKeyEvent());
        assertThat(gridData.grabExcessVerticalSpace).isTrue();
    }

    private Event createKeyEvent() {
        final Event keyEvent = new Event();
        keyEvent.character = '\r';
        return keyEvent; 
    }

    private static List<Section> prepareSections(final Shell shell, final int noOfSections) {
        final Composite composite = new Composite(shell, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(composite);

        final List<Section> sections = new ArrayList<>();
        for (int i = 0; i < noOfSections; i++) {
            final Section section = toolkit.createSection(composite,
                    ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
            final Control internalClient = new Composite(section, SWT.NONE);
            section.setClient(internalClient);
            GridDataFactory.fillDefaults().grab(false, true).applyTo(section);
            sections.add(section);
        }
        return sections;
    }
}
