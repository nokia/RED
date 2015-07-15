package org.robotframework.ide.eclipse.main.plugin.project.editor;

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
import org.robotframework.forms.RedFormToolkit;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.viewers.Selections;

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
                + "Those adresses will be available for all suites within project.");
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
        viewer.getTable().setHeaderVisible(true);

        viewer.setContentProvider(new RemoteLibraryLocationsContentProvider());
        
        ViewerColumnsFactory.newColumn("Path").withWidth(100)
            .shouldGrabAllTheSpaceLeft(false).withMinWidth(50)
            .labelsProvidedBy(new RemoteLibraryLocationsAddressLabelProvider())
            .createFor(viewer);
        ViewerColumnsFactory.newColumn("Port").withWidth(40)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(30)
            .labelsProvidedBy(new RemoteLibraryLocationsPortLabelProvider())
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

    private static class RemoteLocationDialog extends Dialog {

        private Text locationText;
        private Text portText;
        private RemoteLocation location;

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
            infoLabel.setText("Specify address and port of XML-RPC server location. This server will be used"
                    + " for running keywords using Remote library.");
            GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).span(2, 1).applyTo(infoLabel);
            
            final Label locationLabel = new Label(dialogComposite, SWT.NONE);
            locationLabel.setText("Location");
            
            locationText = new Text(dialogComposite, SWT.SINGLE | SWT.BORDER);
            locationText.setText("127.0.0.1");
            GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(locationText);

            final Label portLabel = new Label(dialogComposite, SWT.NONE);
            portLabel.setText("Port");
            
            portText = new Text(dialogComposite, SWT.SINGLE | SWT.BORDER);
            portText.setText("8270");
            GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(portText);
            portText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent event) {
                    final String portString = portText.getText();
                    try {
                        Integer.parseInt(portString);
                        portText.setBackground(portText.getDisplay().getSystemColor(SWT.COLOR_WHITE));
                        getButton(IDialogConstants.OK_ID).setEnabled(true);
                    } catch (final NumberFormatException e) {
                        portText.setBackground(portText.getDisplay().getSystemColor(SWT.COLOR_RED));
                        getButton(IDialogConstants.OK_ID).setEnabled(false);
                    }

                }
            });

            locationText.setFocus();

            return dialogComposite;
        }

        @Override
        protected void okPressed() {
            location = new RemoteLocation();
            location.setPath(locationText.getText());
            location.setPort(Integer.parseInt(portText.getText()));

            super.okPressed();
        }

        RemoteLocation getRemoteLocation() {
            return location;
        }
    }
}
