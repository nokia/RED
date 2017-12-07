/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.library.SourceOpeningSupport;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.TreeContentProvider;

import com.google.common.base.Strings;

/**
 * @author mmarzec
 * @author bembenek
 */
public class LibrariesAutoDiscovererWindow extends Dialog {

    private static final String STATUS_ELEMENT_NAME = "Status";

    private static final String SOURCE_ELEMENT_NAME = "Source";

    private static final String IMPORTERS_ELEMENT_NAME = "Importers";

    private static final String ADDITIONAL_INFO_ELEMENT_NAME = "Additional info";

    private static final String ELEMENT_SEPARATOR = ":";

    private static final Comparator<RobotDryRunLibraryImport> LIBRARY_IMPORT_COMPARATOR = (import1, import2) -> {
        if (import1.getStatus() == import2.getStatus()) {
            return import1.getName().compareToIgnoreCase(import2.getName());
        }
        if (import1.getStatus() == DryRunLibraryImportStatus.ADDED
                && import2.getStatus() != DryRunLibraryImportStatus.ADDED) {
            return -1;
        }
        if (import1.getStatus() == DryRunLibraryImportStatus.NOT_ADDED
                && import2.getStatus() != DryRunLibraryImportStatus.NOT_ADDED) {
            return 1;
        }
        if (import1.getStatus() == DryRunLibraryImportStatus.ALREADY_EXISTING
                && import2.getStatus() == DryRunLibraryImportStatus.ADDED) {
            return 1;
        }
        if (import1.getStatus() == DryRunLibraryImportStatus.ALREADY_EXISTING
                && import2.getStatus() == DryRunLibraryImportStatus.NOT_ADDED) {
            return -1;
        }

        return 0;
    };

    private TreeViewer discoveredLibrariesViewer;

    private StyledText detailsText;

    private final Collection<RobotDryRunLibraryImport> importedLibraries;

    private final RedClipboard clipboard;

    public LibrariesAutoDiscovererWindow(final Shell parent,
            final Collection<RobotDryRunLibraryImport> importedLibraries) {
        super(parent);
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
        setBlockOnOpen(false);
        this.importedLibraries = importedLibraries;
        this.clipboard = new RedClipboard(parent.getDisplay());
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        final Composite container = (Composite) super.createDialogArea(parent);
        createMainComposite(container);
        return container;
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Discovering libraries summary");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(600, 550);
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    private Composite createMainComposite(final Composite parent) {
        final Label libsLabel = new Label(parent, SWT.NONE);
        libsLabel.setText("Discovered libraries (" + importedLibraries.size() + "):");

        final SashForm mainComposite = new SashForm(parent, SWT.VERTICAL);
        GridLayoutFactory.fillDefaults().applyTo(mainComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(mainComposite);

        createLibrariesViewer(mainComposite);

        createDetailsComposite(mainComposite);

        mainComposite.setWeights(new int[] { 5, 2 });

        return mainComposite;
    }

    private void createLibrariesViewer(final Composite mainComposite) {
        discoveredLibrariesViewer = new TreeViewer(mainComposite);
        discoveredLibrariesViewer.getTree().setHeaderVisible(false);
        GridDataFactory.fillDefaults().grab(true, true).minSize(SWT.DEFAULT, 300).applyTo(
                discoveredLibrariesViewer.getTree());
        GridLayoutFactory.fillDefaults().applyTo(discoveredLibrariesViewer.getTree());

        discoveredLibrariesViewer.setContentProvider(new DiscoveredLibrariesViewerContentProvider());
        discoveredLibrariesViewer
                .setLabelProvider(new DelegatingStyledCellLabelProvider(new DiscoveredLibrariesViewerLabelProvider()));

        discoveredLibrariesViewer.setInput(
                importedLibraries.stream().sorted(LIBRARY_IMPORT_COMPARATOR).toArray(RobotDryRunLibraryImport[]::new));

        registerLibrariesViewerListeners();
    }

    private void registerLibrariesViewerListeners() {
        discoveredLibrariesViewer.addSelectionChangedListener(event -> {
            final Object selection = ((TreeSelection) event.getSelection()).getFirstElement();
            if (selection instanceof DryRunLibraryImportChildElement) {
                detailsText.setText(selection.toString());
            } else if (selection instanceof RobotDryRunLibraryImport) {
                detailsText.setText(convertDryRunLibraryImportToText((RobotDryRunLibraryImport) selection));
            } else {
                detailsText.setText("");
            }
        });

        discoveredLibrariesViewer.getTree().addKeyListener(KeyListener.keyReleasedAdapter(e -> {
            if (e.keyCode == SWT.F3 && discoveredLibrariesViewer.getTree().getSelectionCount() == 1) {
                handleFileOpeningEvent();
            } else if (e.keyCode == 'c' && e.stateMask == SWT.CTRL) {
                handleLibraryCopyEvent();
            }
        }));

        final Menu menu = createContextMenu();
        discoveredLibrariesViewer.getTree().addMouseListener(MouseListener.mouseDownAdapter(e -> {
            if (e.button == 3) {
                if (discoveredLibrariesViewer.getTree().getSelectionCount() == 1 && getOpenableFilePath().isPresent()) {
                    menu.setVisible(true);
                    menu.getItem(0).setEnabled(false);
                    menu.getItem(1).setEnabled(true);
                } else if (Stream.of(discoveredLibrariesViewer.getStructuredSelection().toArray())
                        .anyMatch(element -> element instanceof RobotDryRunLibraryImport)) {
                    menu.setVisible(true);
                    menu.getItem(0).setEnabled(true);
                    menu.getItem(1).setEnabled(false);
                }
            }
        }));

        discoveredLibrariesViewer.getTree().addMouseListener(MouseListener.mouseDoubleClickAdapter(e -> {
            if (e.button == 1 && discoveredLibrariesViewer.getTree().getSelectionCount() == 1) {
                handleFileOpeningEvent();
            }
        }));
    }

    private Menu createContextMenu() {
        final Menu menu = new Menu(discoveredLibrariesViewer.getTree());

        final MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
        copyItem.setText("Copy");
        copyItem.setImage(ImagesManager.getImage(RedImages.getCopyImage()));
        copyItem.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> handleLibraryCopyEvent()));

        final MenuItem gotoItem = new MenuItem(menu, SWT.PUSH);
        gotoItem.setText("Go to File");
        gotoItem.setImage(ImagesManager.getImage(RedImages.getGoToImage()));
        gotoItem.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> handleFileOpeningEvent()));

        return menu;
    }

    private void handleLibraryCopyEvent() {
        final String copied = Stream.of(discoveredLibrariesViewer.getStructuredSelection().toArray())
                .filter(element -> element instanceof RobotDryRunLibraryImport)
                .map(element -> ((RobotDryRunLibraryImport) element).getName())
                .collect(Collectors.joining("\n"));
        if (!copied.isEmpty()) {
            clipboard.insertContent(copied);
        }
    }

    private void handleFileOpeningEvent() {
        getOpenableFilePath().flatMap(LibrariesAutoDiscovererWindow::getOpenableFile).ifPresent(file -> {
            final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            SourceOpeningSupport.tryToOpenInEditor(page, file);
        });
    }

    private Optional<String> getOpenableFilePath() {
        final ITreeSelection selection = discoveredLibrariesViewer.getStructuredSelection();
        if (selection != null && selection.getFirstElement() instanceof DryRunLibraryImportChildElement) {
            final DryRunLibraryImportChildElement childElement = (DryRunLibraryImportChildElement) selection
                    .getFirstElement();
            if (childElement.isOpenableFilePath()) {
                return Optional.of(childElement.getValue());
            }
        }
        return Optional.empty();
    }

    private static Optional<IFile> getOpenableFile(final String filePath) {
        final IPath path = new Path(filePath);
        final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
        if (file != null && file.exists()) {
            return Optional.of(file);
        }
        return Optional.empty();
    }

    private void createDetailsComposite(final Composite mainComposite) {
        final Composite detailsComposite = new Composite(mainComposite, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(detailsComposite);
        GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 100).grab(true, false).applyTo(detailsComposite);

        detailsText = new StyledText(detailsComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
        detailsText.setFont(JFaceResources.getTextFont());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(detailsText);
        GridLayoutFactory.fillDefaults().applyTo(detailsText);
        detailsText.setEditable(false);
        detailsText.setAlwaysShowScrollBars(false);
    }

    private static String convertDryRunLibraryImportToText(final RobotDryRunLibraryImport libraryImport) {
        final StringBuilder libraryImportTxtBuilder = new StringBuilder("");
        for (final Object child : extractDryRunLibraryImportChildren(libraryImport)) {
            if (child instanceof DryRunLibraryImportChildElement) {
                libraryImportTxtBuilder.append(child + "\n");
            } else if (child instanceof DryRunLibraryImportListChildElement) {
                libraryImportTxtBuilder.append(((DryRunLibraryImportListChildElement) child).getName() + "\n");
                for (final DryRunLibraryImportChildElement listChild : ((DryRunLibraryImportListChildElement) child)
                        .getList()) {
                    libraryImportTxtBuilder.append(listChild + "\n");
                }
            }
        }
        return libraryImportTxtBuilder.toString();
    }

    private static List<Object> extractDryRunLibraryImportChildren(final RobotDryRunLibraryImport libraryImport) {
        final List<Object> children = new ArrayList<>();
        if (libraryImport.getStatus() != null) {
            children.add(
                    new DryRunLibraryImportChildElement(STATUS_ELEMENT_NAME, libraryImport.getStatus().getMessage()));
        }
        if (libraryImport.getSourcePath() != null) {
            children.add(new DryRunLibraryImportChildElement(SOURCE_ELEMENT_NAME,
                    new File(libraryImport.getSourcePath()).getAbsolutePath(), true));
        } else {
            children.add(new DryRunLibraryImportChildElement(SOURCE_ELEMENT_NAME, "Unknown"));
        }
        final List<String> importersPaths = libraryImport.getImportersPaths()
                .stream()
                .map(File::new)
                .map(File::getAbsolutePath)
                .collect(toList());
        if (importersPaths.size() == 1) {
            children.add(new DryRunLibraryImportChildElement(IMPORTERS_ELEMENT_NAME, importersPaths.get(0), true));
        } else {
            children.add(new DryRunLibraryImportListChildElement(IMPORTERS_ELEMENT_NAME, importersPaths));
        }
        final String additionalInfo = libraryImport.getAdditionalInfo();
        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            children.add(new DryRunLibraryImportChildElement(ADDITIONAL_INFO_ELEMENT_NAME, additionalInfo));
        }
        return children;
    }

    private static class DiscoveredLibrariesViewerContentProvider extends TreeContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            return (RobotDryRunLibraryImport[]) inputElement;
        }

        @Override
        public Object[] getChildren(final Object parentElement) {
            if (parentElement instanceof RobotDryRunLibraryImport) {
                final List<Object> children = extractDryRunLibraryImportChildren(
                        (RobotDryRunLibraryImport) parentElement);
                return children.toArray(new Object[children.size()]);
            } else if (parentElement instanceof DryRunLibraryImportListChildElement) {
                final List<DryRunLibraryImportChildElement> childElementList = ((DryRunLibraryImportListChildElement) parentElement)
                        .getList();
                return childElementList.toArray(new DryRunLibraryImportChildElement[childElementList.size()]);
            }
            return null;
        }

        @Override
        public Object getParent(final Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(final Object element) {
            return !(element instanceof DryRunLibraryImportChildElement);
        }
    }

    private static class DiscoveredLibrariesViewerLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof RobotDryRunLibraryImport) {
                final String name = ((RobotDryRunLibraryImport) element).getName().replaceAll("\\n", "/n");
                return new StyledString(name);

            } else if (element instanceof DryRunLibraryImportChildElement) {
                final DryRunLibraryImportChildElement libraryImportChildElement = (DryRunLibraryImportChildElement) element;

                final StyledString label = new StyledString("");
                final String childElementName = libraryImportChildElement.getName();
                if (!Strings.isNullOrEmpty(childElementName)) {
                    label.append(childElementName, Stylers.Common.BOLD_STYLER);
                    label.append(" ");
                }
                final String childElementValue = libraryImportChildElement.getValue();
                if (!Strings.isNullOrEmpty(childElementValue)) {
                    if (libraryImportChildElement.isOpenableFilePath()) {
                        label.append(childElementValue, Stylers.Common.HYPERLINK_STYLER);
                    } else {
                        label.append(childElementValue);
                    }
                }
                return label;

            } else if (element instanceof DryRunLibraryImportListChildElement) {
                return new StyledString(((DryRunLibraryImportListChildElement) element).getName(),
                        Stylers.Common.BOLD_STYLER);
            }

            return new StyledString("");
        }

        @Override
        public Image getImage(final Object element) {
            if (element instanceof RobotDryRunLibraryImport) {
                final RobotDryRunLibraryImport libraryImport = (RobotDryRunLibraryImport) element;

                if (libraryImport.getStatus() == DryRunLibraryImportStatus.NOT_ADDED) {
                    return ImagesManager.getImage(RedImages.getFatalErrorImage());

                } else if (libraryImport.getStatus() == DryRunLibraryImportStatus.ADDED) {
                    return ImagesManager.getImage(RedImages.getBigSuccessImage());

                } else if (libraryImport.getStatus() == DryRunLibraryImportStatus.ALREADY_EXISTING) {
                    return ImagesManager.getImage(RedImages.getBigWarningImage());
                }
            } else if (element instanceof DryRunLibraryImportChildElement
                    || element instanceof DryRunLibraryImportListChildElement) {
                return ImagesManager.getImage(RedImages.getElementImage());
            }
            return null;
        }
    }

    private static class DryRunLibraryImportChildElement {

        private String name;

        private final String value;

        private final boolean isOpenableFilePath;

        public DryRunLibraryImportChildElement(final String name, final String value) {
            this(name, value, false);
        }

        public DryRunLibraryImportChildElement(final String name, final String value, final boolean isFilePath) {
            if (name != null) {
                this.name = name + ELEMENT_SEPARATOR;
            }
            this.value = value;
            this.isOpenableFilePath = isFilePath && getOpenableFile(value).isPresent();
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public boolean isOpenableFilePath() {
            return isOpenableFilePath;
        }

        @Override
        public String toString() {
            if (value == null && name != null) {
                return name;
            }
            if (value != null && name == null) {
                return value;
            }
            if (value == null && name == null) {
                return "";
            }
            return name + " " + value;
        }

    }

    private static class DryRunLibraryImportListChildElement {

        private String name;

        private final List<DryRunLibraryImportChildElement> list = new ArrayList<>();

        public DryRunLibraryImportListChildElement(final String name, final List<String> list) {
            if (name != null) {
                this.name = name + ELEMENT_SEPARATOR;
            }
            if (list != null) {
                for (final String listElement : list) {
                    this.list.add(new DryRunLibraryImportChildElement(null, listElement, true));
                }
            }
        }

        public List<DryRunLibraryImportChildElement> getList() {
            return list;
        }

        public String getName() {
            return name;
        }

    }
}
