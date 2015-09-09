/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Strings;

class RemoteLibraryLocationsFormFragment implements ISectionFormFragment {

    @Inject
    private RedFormToolkit toolkit;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedProjectEditorInput editorInput;

    private TableViewer viewer;

    private Button addLocationButton;

    private Button removeLocationButton;

    @Override
    public void initialize(final Composite parent) {
        final Section section = toolkit.createSection(parent, Section.EXPANDED | Section.TITLE_BAR
                | Section.DESCRIPTION | Section.TWISTIE);
        section.setText("Remote library locations");
        section.setDescription("In this section locations of servers for Remote library can be specified. "
                + "Those addresses will be available for all suites within project.");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(section);

        final Composite internalComposite = toolkit.createComposite(section);
        section.setClient(internalComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(internalComposite);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(internalComposite);

        createViewer(internalComposite);
        createButtons(internalComposite);

        setInput();
    }

    private void createViewer(final Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, true).span(1, 4).applyTo(viewer.getTable());
        viewer.getTable().setEnabled(false);

        viewer.setContentProvider(new RemoteLibraryLocationsContentProvider());
        
        ViewerColumnsFactory.newColumn("").withWidth(100)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(50)
            .labelsProvidedBy(new RemoteLibraryLocationsAddressLabelProvider())
            .createFor(viewer);

        final ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                removeLocationButton.setEnabled(!event.getSelection().isEmpty());
            }
        };
        viewer.addSelectionChangedListener(selectionChangedListener);
        viewer.getTable().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                viewer.removeSelectionChangedListener(selectionChangedListener);
            }
        });
    }

    private void createButtons(final Composite parent) {
        addLocationButton = toolkit.createButton(parent, "Add location", SWT.PUSH);
        addLocationButton.setEnabled(false);
        addLocationButton.addSelectionListener(createAddingHandler());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(addLocationButton);

        removeLocationButton = toolkit.createButton(parent, "Remove", SWT.PUSH);
        removeLocationButton.setEnabled(false);
        removeLocationButton.addSelectionListener(creteRemovingHandler());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(removeLocationButton);
    }

    private SelectionListener createAddingHandler() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final RemoteLocationDialog dialog = new RemoteLocationDialog(viewer.getTable().getShell());
                if (dialog.open() == Window.OK) {
                    final RemoteLocation remoteLocation = dialog.getRemoteLocation();
                    @SuppressWarnings("unchecked")
                    final List<RemoteLocation> locations = (List<RemoteLocation>) viewer.getInput();
                    if (!locations.contains(remoteLocation)) {
                        editorInput.getProjectConfiguration().addRemoteLocation(remoteLocation);
                    }

                    dirtyProviderService.setDirtyState(true);
                    viewer.refresh();
                }
            }
        };
    }

    private SelectionListener creteRemovingHandler() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final List<RemoteLocation> selectedLocations = Selections.getElements(
                        (IStructuredSelection) viewer.getSelection(), RemoteLocation.class);
                editorInput.getProjectConfiguration().removeRemoteLocations(selectedLocations);

                dirtyProviderService.setDirtyState(true);
                viewer.refresh();
            }
        };
    }

    void whenEnvironmentWasLoaded() {
        final boolean isEditable = editorInput.isEditable();

        addLocationButton.setEnabled(isEditable);
        removeLocationButton.setEnabled(false);
        viewer.getTable().setEnabled(isEditable);
    }

    void whenConfigurationFiledChanged() {
        addLocationButton.setEnabled(false);
        removeLocationButton.setEnabled(false);
        viewer.getTable().setEnabled(false);
    }

    private void setInput() {
        final List<RemoteLocation> remoteLocations = editorInput.getProjectConfiguration().getRemoteLocations();
        viewer.setInput(remoteLocations);
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    @Override
    public MatchesCollection collectMatches(final String filter) {
        return null;
    }

    private static URI createUriWithDefaultsIfMissing(final URI uri, final int defaultPort, final String defaultPath) {
        try {
            final int port = uri.getPort() != -1 ? uri.getPort() : defaultPort;
            final String uriPath = uri.getPath();
            final String path = !Strings.isNullOrEmpty(uriPath) ? uriPath : defaultPath;
            final URI newUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), port, path, uri.getQuery(),
                    uri.getFragment());
            return newUri;
        } catch (final URISyntaxException e) {
            return uri;
        }
    }

    private static class RemoteLocationDialog extends Dialog {

        private RemoteLocation location;
        private Label exceptionLabel;
        private Text uriText;

        protected RemoteLocationDialog(final Shell parentShell) {
            super(parentShell);
        }

        @Override
        public void create() {
            super.create();
            getShell().setText("Add Remote location");
        }

        @Override
        protected Control createDialogArea(final Composite parent) {
            final Composite dialogComposite = (Composite) super.createDialogArea(parent);
            GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(dialogComposite);

            final Label infoLabel = new Label(dialogComposite, SWT.WRAP);
            infoLabel.setText("Specify URI of XML-RPC server location. This server will be used"
                    + " for running keywords using Remote library.");
            GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).span(2, 1).applyTo(infoLabel);

            final Label uriLabel = new Label(dialogComposite, SWT.NONE);
            uriLabel.setText("URI");
            
            uriText = new Text(dialogComposite, SWT.SINGLE | SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(uriText);
            uriText.setText("http://127.0.0.1:8270/");
            uriText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent event) {
                    try {
                        final URI uri = new URI(uriText.getText());
                        getButton(IDialogConstants.OK_ID).setEnabled(true);

                        if (Strings.isNullOrEmpty(uri.getPath()) && uri.getPort() == -1) {
                            uriText.setBackground(uriText.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
                            exceptionLabel
                                    .setText("URI have an empty path and port. Path '/RPC2' and port 8270 will be used");
                        } else if (Strings.isNullOrEmpty(uri.getPath())) {
                            uriText.setBackground(uriText.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
                            exceptionLabel.setText("URI have an empty path. Path '/RPC2' will be used");
                        } else if (uri.getPort() == -1) {
                            uriText.setBackground(uriText.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
                            exceptionLabel.setText("URI have no port specified. Port 8270 will be used");
                        } else {
                            uriText.setBackground(uriText.getDisplay().getSystemColor(SWT.COLOR_WHITE));
                            exceptionLabel.setText("");
                        }
                    } catch (final URISyntaxException e) {
                        uriText.setBackground(uriText.getDisplay().getSystemColor(SWT.COLOR_RED));
                        getButton(IDialogConstants.OK_ID).setEnabled(false);
                        exceptionLabel.setText("URI problem " + e.getMessage().toLowerCase());
                    }
                }
            });

            exceptionLabel = new Label(dialogComposite, SWT.NONE);
            exceptionLabel.setText("");
            GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(exceptionLabel);

            uriText.setFocus();

            return dialogComposite;
        }

        @Override
        protected void okPressed() {
            location = new RemoteLocation();
            try {
                location.setUriAddress(createUriWithDefaultsIfMissing(new URI(uriText.getText()), 8270, "/RPC2"));
            } catch (final URISyntaxException e) {
                throw new IllegalStateException("Can't happen. It is not possible to click ok with invalid URI", e);
            }

            super.okPressed();
        }

        RemoteLocation getRemoteLocation() {
            return location;
        }
    }
}
