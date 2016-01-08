/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.Environments;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput.RedXmlProblem;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.ElementsAddingEditingSupport.NewElementsCreator;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.StructuredContentProvider;
import org.robotframework.red.viewers.Viewers;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

class RemoteLibraryLocationsFormFragment implements ISectionFormFragment {

    private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.redxmleditor.remotelocations.context";

    @Inject
    private IEditorSite site;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private RedFormToolkit toolkit;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedProjectEditorInput editorInput;

    private RowExposingTableViewer viewer;

    TableViewer getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        final Section section = createSection(parent);

        final Composite internalComposite = toolkit.createComposite(section);
        section.setClient(internalComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(internalComposite);
        GridLayoutFactory.fillDefaults().applyTo(internalComposite);

        createViewer(internalComposite);
        createColumns();
        createContextMenu();

        setInput();
    }

    private Section createSection(final Composite parent) {
        final Section section = toolkit.createSection(parent,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        section.setText("Remote library locations");
        section.setDescription("Define locations of servers for Remote standard library. "
                + "Those remote libraries wille accessible from all suites within the project.");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(section);
        return section;
    }

    private void createViewer(final Composite parent) {
        viewer = new RowExposingTableViewer(parent,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).indent(0, 10).applyTo(viewer.getTable());
        viewer.setUseHashlookup(true);
        viewer.getTable().setEnabled(false);
        viewer.getTable().setLinesVisible(false);
        viewer.getTable().setHeaderVisible(false);

        viewer.setContentProvider(new RemoteLibraryLocationsContentProvider());

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
        Viewers.boundViewerWithContext(viewer, site, CONTEXT_ID);
    }

    private void createColumns() {
        ViewerColumnsFactory.newColumn("").withWidth(100)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(50)
            .editingEnabledOnlyWhen(editorInput.isEditable())
            .editingSupportedBy(new RemoteLibraryLocationEditingSupport(viewer, editorInput, newElementsCreator()))
            .labelsProvidedBy(new RemoteLibraryLocationsAddressLabelProvider())
            .createFor(viewer);
    }

    private void createContextMenu() {
        final String menuId = "org.robotframework.ide.eclipse.redxmleditor.remotelocations.contextMenu";

        final MenuManager manager = new MenuManager("Red.xml file editor remote locations context menu", menuId);
        final Table control = viewer.getTable();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    private NewElementsCreator<RemoteLocation> newElementsCreator() {
        return new NewElementsCreator<RobotProjectConfig.RemoteLocation>() {
            @Override
            public RemoteLocation createNew() {
                final RemoteLocationDialog dialog = new RemoteLocationDialog(viewer.getTable().getShell());
                if (dialog.open() == Window.OK) {
                    final RemoteLocation remoteLocation = dialog.getRemoteLocation();

                    final List<RemoteLocation> locations = editorInput.getProjectConfiguration().getRemoteLocations();
                    if (!locations.contains(remoteLocation)) {
                        editorInput.getProjectConfiguration().addRemoteLocation(remoteLocation);

                        final RedProjectConfigEventData<List<RemoteLocation>> eventData = new RedProjectConfigEventData<List<RemoteLocation>>(
                                editorInput.getRobotProject().getConfigurationFile(), newArrayList(remoteLocation));
                        eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_STRUCTURE_CHANGED, eventData);

                        return remoteLocation;
                    }
                }
                return null;
            }
        };
    }

    private void setInput() {
        final List<RemoteLocation> remoteLocations = editorInput.getProjectConfiguration().getRemoteLocations();
        viewer.setInput(remoteLocations);
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    private void setDirty(final boolean isDirty) {
        dirtyProviderService.setDirtyState(isDirty);
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

    @Inject
    @Optional
    private void whenEnvironmentLoadingStarted(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADING_STARTED) final RobotProjectConfig config) {
        setInput();
        viewer.getTable().setEnabled(false);
    }

    @Inject
    @Optional
    private void whenEnvironmentsWereLoaded(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED) final Environments envs) {
        viewer.getTable().setEnabled(editorInput.isEditable());
    }

    @Inject
    @Optional
    private void whenRemoteLocationDetailChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_PATH_CHANGED) final RedProjectConfigEventData<RemoteLocation> eventData) {
        if (editorInput.getRobotProject().getConfigurationFile().equals(eventData.getUnderlyingFile())) {
            setDirty(true);
            viewer.refresh();
        }
    }

    @Inject
    @Optional
    private void whenRemoteLocationChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_STRUCTURE_CHANGED) final RedProjectConfigEventData<List<RemoteLocation>> eventData) {
        if (editorInput.getRobotProject().getConfigurationFile().equals(eventData.getUnderlyingFile())) {
            setDirty(true);
            viewer.refresh();
        }
    }

    private class RemoteLibraryLocationsContentProvider extends StructuredContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            final Object[] elements = ((List<?>) inputElement).toArray();
            if (editorInput.isEditable()) {
                final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
                newElements[elements.length] = new ElementAddingToken("remote location", true);
                return newElements;
            } else {
                return elements;
            }
        }
    }

    private class RemoteLibraryLocationsAddressLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof RemoteLocation) {
                final RemoteLocation location = (RemoteLocation) element;
                final Styler styler = editorInput.getProblemsFor(location).isEmpty() ? Stylers.Common.EMPTY_STYLER
                        : Stylers.Common.ERROR_STYLER;
                return new StyledString(location.getUri(), styler);
            } else {
                return ((ElementAddingToken) element).getStyledText();
            }
        }

        @Override
        public Image getImage(final Object element) {
            if (element instanceof RemoteLocation) {
                final RemoteLocation location = (RemoteLocation) element;

                final List<RedXmlProblem> problems = editorInput.getProblemsFor(location);
                if (problems.isEmpty()) {
                    return ImagesManager.getImage(RedImages.getRemoteConnectedImage());
                } else {
                    return ImagesManager.getImage(RedImages.getRemoteDisconnectedImage());
                }
            } else if (element instanceof ElementAddingToken) {
                return ((ElementAddingToken) element).getImage();
            }
            return null;
        }

        @Override
        public String getToolTipText(final Object element) {
            if (element instanceof RemoteLocation) {
                final RemoteLocation location = (RemoteLocation) element;

                final List<RedXmlProblem> problems = editorInput.getProblemsFor(location);
                final String description = Joiner.on('\n')
                        .join(transform(problems, new Function<RedXmlProblem, String>() {
                            @Override
                            public String apply(final RedXmlProblem problem) {
                                return problem.getDescription();
                            }
                        }));
                return description.isEmpty() ? null : description;
            }
            return null;
        }

        @Override
        public Image getToolTipImage(final Object element) {
            if (element instanceof RemoteLocation) {
                final RemoteLocation location = (RemoteLocation) element;
                final List<RedXmlProblem> problems = editorInput.getProblemsFor(location);
                if (!problems.isEmpty()) {
                    return ImagesManager.getImage(RedImages.getErrorImage());
                }
            }
            return null;
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
