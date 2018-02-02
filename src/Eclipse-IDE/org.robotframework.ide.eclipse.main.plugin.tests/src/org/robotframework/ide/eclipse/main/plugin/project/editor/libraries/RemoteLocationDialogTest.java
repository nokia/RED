/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ShellProvider;
import org.robotframework.red.swt.SwtThread;

public class RemoteLocationDialogTest {

    @Rule
    public ShellProvider shell = new ShellProvider();

    @Test
    public void entryDialogProperlyGeneratesRemoteLocation_whenUriIsProvidedInTextBox() throws Exception {
        final AtomicBoolean finished = new AtomicBoolean(false);
        final AtomicReference<RemoteLocationDialog> dialog = new AtomicReference<>(null);

        final Thread guiChangesRequestingThread = new Thread(() -> {
            SwtThread.asyncExec(() -> {
                dialog.set(new RemoteLocationDialog(shell.getShell()));
                dialog.get().open();
                finished.set(true);
            });
            SwtThread.asyncExec(() -> {
                dialog.get().getUriText().setText("http://1.2.3.4");
                dialog.get().getOkButton().notifyListeners(SWT.Selection, new Event());
            });
        });
        guiChangesRequestingThread.start();
        guiChangesRequestingThread.join();

        while (!finished.get()) {
            Thread.sleep(100);
            while (shell.getShell().getDisplay().readAndDispatch()) {
                // wait for events
            }
        }

        assertThat(dialog.get().getRemoteLocation().getUri()).isEqualTo("http://1.2.3.4:8270/RPC2");
    }

    @Test
    public void defaultPortAndPathAreUsedWhenNotSpecified() throws Exception {
        assertThat(RemoteLocationDialog.createUriWithDefaultsIfMissing(new URI("http://1.2.3.4"), 123, "/def"))
                .isEqualTo(new URI("http://1.2.3.4:123/def"));
        assertThat(RemoteLocationDialog.createUriWithDefaultsIfMissing(new URI("http://1.2.3.4/"), 123, "/def"))
                .isEqualTo(new URI("http://1.2.3.4:123/"));
        assertThat(RemoteLocationDialog.createUriWithDefaultsIfMissing(new URI("http://1.2.3.4/path"), 123, "/def"))
                .isEqualTo(new URI("http://1.2.3.4:123/path"));
        assertThat(RemoteLocationDialog.createUriWithDefaultsIfMissing(new URI("http://1.2.3.4:456"), 123, "/def"))
                .isEqualTo(new URI("http://1.2.3.4:456/def"));
        assertThat(RemoteLocationDialog.createUriWithDefaultsIfMissing(new URI("http://1.2.3.4:456/"), 123, "/def"))
                .isEqualTo(new URI("http://1.2.3.4:456/"));
        assertThat(RemoteLocationDialog.createUriWithDefaultsIfMissing(new URI("http://1.2.3.4:456/path"), 123, "/def"))
                .isEqualTo(new URI("http://1.2.3.4:456/path"));
    }
}
