/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static java.util.stream.Collectors.toList;
import static org.robotframework.red.swt.Listeners.keyReleasedAdapter;
import static org.robotframework.red.swt.Listeners.mouseDoubleClickAdapter;
import static org.robotframework.red.swt.Listeners.mouseDownAdapter;
import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.TreeContentProvider;

import com.google.common.annotations.VisibleForTesting;
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

    @VisibleForTesting
    static final Comparator<RobotDryRunLibraryImport> LIBRARY_IMPORT_COMPARATOR = (import1, import2) -> {
        if (import1.getStatus() == import2.getStatus()) {
            return import1.getName().compareToIgnoreCase(import2.getName());
        } else if (import1.getStatus() == DryRunLibraryImportStatus.ADDED
                || import1.getStatus() == DryRunLibraryImportStatus.ALREADY_EXISTING
                        && import2.getStatus() == DryRunLibraryImportStatus.NOT_ADDED) {
            return -1;
        } else {
            return 1;
        }
    };

    private TreeViewer discoveredLibrariesViewer;

    private StyledText detailsText;

    private final Collection<RobotDryRunLibraryImport> importedLibraries;

    private final RedClipboard clipboard;

    public static Consumer<Collection<RobotDryRunLibraryImport>> openSummary() {
        final Shell parent = Display.getCurrent().getActiveShell();
        return libraryImports -> SwtThread
                .syncExec(() -> new LibrariesAutoDiscovererWindow(parent, libraryImports).open());
    }

    private LibrariesAutoDiscovererWindow(final Shell parent,
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
    public void create() {
        super.create();
        getShell().setText("Discovering libraries summary");
        getShell().setMinimumSize(300, 300);
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
            detailsText.setText(convertToText(selection));
        });

        discoveredLibrariesViewer.getTree().addKeyListener(keyReleasedAdapter(e -> {
            if (e.keyCode == SWT.F3 && discoveredLibrariesViewer.getTree().getSelectionCount() == 1) {
                handleFileOpeningEvent();
            } else if (e.keyCode == 'c' && e.stateMask == SWT.CTRL) {
                handleLibraryCopyEvent();
            }
        }));

        final Menu menu = createContextMenu();
        discoveredLibrariesViewer.getTree().addMouseListener(mouseDownAdapter(e -> {
            if (e.button == 3) {
                if (discoveredLibrariesViewer.getTree().getSelectionCount() == 1
                        && getSelectionOpenableFile().isPresent()) {
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

        discoveredLibrariesViewer.getTree().addMouseListener(mouseDoubleClickAdapter(e -> {
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
        copyItem.addSelectionListener(widgetSelectedAdapter(e -> handleLibraryCopyEvent()));

        final MenuItem gotoItem = new MenuItem(menu, SWT.PUSH);
        gotoItem.setText("Go to File");
        gotoItem.setImage(ImagesManager.getImage(RedImages.getGoToImage()));
        gotoItem.addSelectionListener(widgetSelectedAdapter(e -> handleFileOpeningEvent()));

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
        getSelectionOpenableFile().ifPresent(file -> {
            final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            SourceOpeningSupport.tryToOpenInEditor(page, file);
        });
    }

    private Optional<IFile> getSelectionOpenableFile() {
        final ITreeSelection selection = discoveredLibrariesViewer.getStructuredSelection();
        if (selection != null && selection.getFirstElement() instanceof DryRunLibraryImportChildElement) {
            final DryRunLibraryImportChildElement childElement = (DryRunLibraryImportChildElement) selection
                    .getFirstElement();
            return getOpenableFile(childElement.getValue());
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

    @VisibleForTesting
    static String convertToText(final Object selection) {
        if (selection instanceof RobotDryRunLibraryImport) {
            return createChildElements((RobotDryRunLibraryImport) selection).stream()
                    .map(LibrariesAutoDiscovererWindow::convertToText)
                    .collect(Collectors.joining("\n"));
        } else if (selection instanceof DryRunLibraryImportChildElement) {
            final String name = ((DryRunLibraryImportChildElement) selection).getName();
            final String value = ((DryRunLibraryImportChildElement) selection).getValue();
            return name != null ? name + ": " + value : value;
        } else if (selection instanceof DryRunLibraryImportListChildElement) {
            final String importerPaths = ((DryRunLibraryImportListChildElement) selection).getList()
                    .stream()
                    .map(LibrariesAutoDiscovererWindow::convertToText)
                    .collect(Collectors.joining("\n"));
            return ((DryRunLibraryImportListChildElement) selection).getName() + ":\n" + importerPaths;
        }
        return "";
    }

    private static List<Object> createChildElements(final RobotDryRunLibraryImport libraryImport) {
        final List<Object> children = new ArrayList<>();

        if (libraryImport.getStatus() != null) {
            children.add(
                    new DryRunLibraryImportChildElement(STATUS_ELEMENT_NAME, libraryImport.getStatus().getMessage()));
        }

        if (libraryImport.getSourcePath() != null) {
            children.add(new DryRunLibraryImportChildElement(SOURCE_ELEMENT_NAME,
                    new File(libraryImport.getSourcePath()).getAbsolutePath()));
        } else {
            children.add(new DryRunLibraryImportChildElement(SOURCE_ELEMENT_NAME, "Unknown"));
        }

        final List<String> importersPaths = libraryImport.getImportersPaths()
                .stream()
                .map(File::new)
                .map(File::getAbsolutePath)
                .collect(toList());
        if (importersPaths.isEmpty()) {
            children.add(new DryRunLibraryImportChildElement(IMPORTERS_ELEMENT_NAME, "Unknown"));
        } else if (importersPaths.size() == 1) {
            children.add(new DryRunLibraryImportChildElement(IMPORTERS_ELEMENT_NAME, importersPaths.get(0)));
        } else {
            final List<DryRunLibraryImportChildElement> list = importersPaths.stream()
                    .map(value -> new DryRunLibraryImportChildElement(null, value))
                    .collect(Collectors.toList());
            children.add(new DryRunLibraryImportListChildElement(IMPORTERS_ELEMENT_NAME, list));
        }

        if (!Strings.isNullOrEmpty(libraryImport.getAdditionalInfo())) {
            children.add(new DryRunLibraryImportChildElement(ADDITIONAL_INFO_ELEMENT_NAME,
                    libraryImport.getAdditionalInfo()));
        }
        return children;
    }

    @VisibleForTesting
    static class DiscoveredLibrariesViewerContentProvider extends TreeContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            return (RobotDryRunLibraryImport[]) inputElement;
        }

        @Override
        public Object[] getChildren(final Object parentElement) {
            if (parentElement instanceof RobotDryRunLibraryImport) {
                final List<Object> children = createChildElements((RobotDryRunLibraryImport) parentElement);
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

    @VisibleForTesting
    static class DiscoveredLibrariesViewerLabelProvider extends RedCommonLabelProvider {

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
                    if (getOpenableFile(libraryImportChildElement.getValue()).isPresent()) {
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

    @VisibleForTesting
    static class DryRunLibraryImportChildElement {

        private final String name;

        private final String value;

        DryRunLibraryImportChildElement(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        String getName() {
            return name;
        }

        String getValue() {
            return value;
        }

    }

    @VisibleForTesting
    static class DryRunLibraryImportListChildElement {

        private final String name;

        private final List<DryRunLibraryImportChildElement> list;

        DryRunLibraryImportListChildElement(final String name, final List<DryRunLibraryImportChildElement> list) {
            this.name = name;
            this.list = list;
        }

        List<DryRunLibraryImportChildElement> getList() {
            return list;
        }

        String getName() {
            return name;
        }

    }
}
