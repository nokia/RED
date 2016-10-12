/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.hyperlink.Conditions.shellWithText;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ShellProvider;

public class HyperlinkDialogTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();
    
    @Test
    public void afterOpeningDialogPresentsTableWithGivenHyperlinks() {
        final RedHyperlink link1 = mock(RedHyperlink.class);
        when(link1.getLabelForCompoundHyperlinksDialog()).thenReturn("first_link");
        when(link1.additionalLabelDecoration()).thenReturn("decoration");
        when(link1.getImage()).thenReturn(RedImages.getElementImage());

        final RedHyperlink link2 = mock(RedHyperlink.class);
        when(link2.getLabelForCompoundHyperlinksDialog()).thenReturn("snd_link");
        when(link2.additionalLabelDecoration()).thenReturn("");
        when(link2.getImage()).thenReturn(RedImages.getGoToImage());

        final List<RedHyperlink> hyperlinks = newArrayList(link1, link2);

        final HyperlinkDialog dialog = new HyperlinkDialog(shellProvider.getShell(), "name", hyperlinks);
        dialog.open();


        final Table table = (Table) dialog.getFocusControl();
        assertThat(table.getItems()).hasSize(2);

        final TableItem tableItem1 = table.getItems()[0];
        assertThat(tableItem1.getImage()).isSameAs(ImagesManager.getImage(RedImages.getElementImage()));
        assertThat(tableItem1.getText()).isEqualTo("first_link decoration");

        final TableItem tableItem2 = table.getItems()[1];
        assertThat(tableItem2.getImage()).isSameAs(ImagesManager.getImage(RedImages.getGoToImage()));
        assertThat(tableItem2.getText()).isEqualTo("snd_link");

        verify(link1, never()).open();
        verify(link2, never()).open();
    }

    @Test
    public void whenLinkIsSelectedFromTable_popupIsClosedAndLinkIsOpened() {
        final RedHyperlink link1 = mock(RedHyperlink.class);
        when(link1.getLabelForCompoundHyperlinksDialog()).thenReturn("first_link");
        when(link1.additionalLabelDecoration()).thenReturn("decoration");
        when(link1.getImage()).thenReturn(RedImages.getElementImage());

        final RedHyperlink link2 = mock(RedHyperlink.class);
        when(link2.getLabelForCompoundHyperlinksDialog()).thenReturn("snd_link");
        when(link2.additionalLabelDecoration()).thenReturn("");
        when(link2.getImage()).thenReturn(RedImages.getGoToImage());

        final List<RedHyperlink> hyperlinks = newArrayList(link1, link2);

        final HyperlinkDialog dialog = new HyperlinkDialog(shellProvider.getShell(), "name", hyperlinks);
        dialog.open();

        final Table table = (Table) dialog.getFocusControl();
        final TableItem tableItem2 = table.getItems()[1];
        final Event event = new Event();
        event.item = tableItem2;
        table.notifyListeners(SWT.DefaultSelection, event);

        final Display display = Display.getCurrent();
        while (display.readAndDispatch()) {} // handle all events coming to UI
        assertThat(display.getShells()).doesNotHave(shellWithText(HyperlinkDialog.POPUP_TEXT));
        
        verify(link1, never()).open();
        verify(link2).open();
    }
}
