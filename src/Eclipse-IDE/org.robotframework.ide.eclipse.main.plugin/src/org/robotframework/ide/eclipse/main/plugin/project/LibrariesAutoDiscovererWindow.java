/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.executor.RobotDryRunOutputParser.DryRunLibraryImport;
import org.rf.ide.core.executor.RobotDryRunOutputParser.DryRunLibraryImportStatus;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.TreeContentProvider;

/**
 * @author mmarzec
 */
public class LibrariesAutoDiscovererWindow extends Dialog {

    private static final String STATUS_ELEMENT_NAME = "Status";

    private static final String SOURCE_ELEMENT_NAME = "Source";

    private static final String IMPORTERS_ELEMENT_NAME = "Importers";

    private static final String ADDITIONAL_INFO_ELEMENT_NAME = "Additional info";

    private static final String ELEMENT_SEPARATOR = ":";

    private TreeViewer discoveredLibrariesViewer;

    private Text detailsText;

    private List<DryRunLibraryImport> importedLibraries;

    public LibrariesAutoDiscovererWindow(final Shell parent, final List<DryRunLibraryImport> importedLibraries) {
        super(parent);
        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
        setBlockOnOpen(false);
        this.importedLibraries = importedLibraries;
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

        final Composite mainComposite = new Composite(parent, SWT.NONE);

        GridLayoutFactory.fillDefaults().numColumns(1).margins(3, 3).applyTo(mainComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(mainComposite);

        final Label libsLabel = new Label(mainComposite, SWT.NONE);
        libsLabel.setText("Discovered libraries (" + importedLibraries.size() + "):");

        createLibrariesViewer(mainComposite);

        createDetailsComposite(mainComposite);

        return mainComposite;
    }

    private void createLibrariesViewer(final Composite mainComposite) {
        discoveredLibrariesViewer = new TreeViewer(mainComposite);
        discoveredLibrariesViewer.getTree().setHeaderVisible(false);
        GridDataFactory.fillDefaults()
                .grab(true, true)
                .minSize(SWT.DEFAULT, 300)
                .applyTo(discoveredLibrariesViewer.getTree());
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo(discoveredLibrariesViewer.getTree());

        discoveredLibrariesViewer.setContentProvider(new DiscoveredLibrariesViewerContentProvider());
        discoveredLibrariesViewer.setLabelProvider(new DiscoveredLibrariesViewerLabelProvider());

        Collections.sort(importedLibraries, new DiscoveredLibrariesComparator());
        discoveredLibrariesViewer
                .setInput(importedLibraries.toArray(new DryRunLibraryImport[importedLibraries.size()]));

        discoveredLibrariesViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final Object selection = ((TreeSelection) event.getSelection()).getFirstElement();
                if (selection != null && selection instanceof String) {
                    final String selectionText = (String) selection;
                    if (selectionText != null) {
                        detailsText.setText(selectionText);
                        detailsText.setVisible(true);
                        return;
                    }
                }
                detailsText.setVisible(false);
            }
        });
    }

    private void createDetailsComposite(final Composite mainComposite) {
        final Composite detailsComposite = new Composite(mainComposite, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(1).margins(3, 3).applyTo(detailsComposite);
        GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 100).grab(true, false).applyTo(detailsComposite);

        detailsText = new Text(detailsComposite, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 80).grab(true, false).applyTo(detailsText);
        detailsText.setVisible(false);
    }

    private class DiscoveredLibrariesViewerContentProvider extends TreeContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            return (DryRunLibraryImport[]) inputElement;
        }

        @Override
        public Object[] getChildren(final Object parentElement) {
            if (parentElement instanceof DryRunLibraryImport) {
                final DryRunLibraryImport libraryImport = (DryRunLibraryImport) parentElement;
                final List<Object> children = new ArrayList<>();

                if (libraryImport.getStatus() != null) {
                    children.add(STATUS_ELEMENT_NAME + ELEMENT_SEPARATOR + libraryImport.getStatus().getMessage());
                }
                if (libraryImport.getSourcePath() != null && !libraryImport.getSourcePath().isEmpty()) {
                    children.add(SOURCE_ELEMENT_NAME + ELEMENT_SEPARATOR + libraryImport.getSourcePath());
                } else {
                    children.add(SOURCE_ELEMENT_NAME + ELEMENT_SEPARATOR + "Unknown");
                }
                final List<String> importersPaths = libraryImport.getImportersPaths();
                if (importersPaths.size() == 1) {
                    children.add(IMPORTERS_ELEMENT_NAME + ELEMENT_SEPARATOR + importersPaths.get(0));
                } else {
                    children.add(importersPaths);
                }
                final String additionalInfo = libraryImport.getAdditionalInfo();
                if (additionalInfo != null && !additionalInfo.isEmpty()) {
                    children.add(ADDITIONAL_INFO_ELEMENT_NAME + ELEMENT_SEPARATOR + additionalInfo);
                }
                return children.toArray(new Object[children.size()]);
            } else if (parentElement instanceof List<?>) {
                @SuppressWarnings("unchecked")
                final List<String> list = (List<String>) parentElement;
                return list.toArray(new String[list.size()]);
            }
            return null;
        }

        @Override
        public Object getParent(final Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(final Object element) {
            if (element instanceof String) {
                return false;
            }
            return true;
        }

    }

    class DiscoveredLibrariesViewerLabelProvider extends StyledCellLabelProvider {

        @Override
        public void update(final ViewerCell cell) {
            final StyledString label = getStyledString(cell.getElement());
            cell.setText(label.getString());
            cell.setStyleRanges(label.getStyleRanges());
            cell.setImage(getImage(cell.getElement()));

            super.update(cell);
        }

        private StyledString getStyledString(final Object element) {

            StyledString label = new StyledString("");

            if (element instanceof DryRunLibraryImport) {
                label = new StyledString(((DryRunLibraryImport) element).getName());
            } else if (element instanceof String) {
                String text = (String) element;
                String[] textSplit = text.split(ELEMENT_SEPARATOR);
                if (textSplit[0].length() == 1) {
                    label.append(text);
                } else {
                    StyledString firstElement = new StyledString(textSplit[0], new Styler() {

                        @Override
                        public void applyStyles(final TextStyle textStyle) {
                            textStyle.font = getFont(textStyle.font, SWT.BOLD);
                        }
                    });
                    label.append(firstElement);
                    if (textSplit.length > 1) {
                        StringBuilder secElement = new StringBuilder("");
                        for (int i = 1; i < textSplit.length; i++) {
                            secElement.append(textSplit[i]);
                        }
                        label.append(" " + ELEMENT_SEPARATOR + " " + secElement.toString());
                    }
                }
            } else if (element instanceof List<?>) {
                label = new StyledString(IMPORTERS_ELEMENT_NAME + ELEMENT_SEPARATOR, new Styler() {

                    @Override
                    public void applyStyles(final TextStyle textStyle) {
                        textStyle.font = getFont(textStyle.font, SWT.BOLD);
                    }
                });
            }

            return label;
        }

        private Font getFont(final Font fontToReuse, final int style) {
            final Font currentFont = fontToReuse == null ? Display.getCurrent().getSystemFont() : fontToReuse;
            final FontDescriptor fontDescriptor = FontDescriptor.createFrom(currentFont).setStyle(style);
            return FontsManager.getFont(fontDescriptor);
        }

        private Image getImage(final Object element) {
            if (element instanceof DryRunLibraryImport) {
                final DryRunLibraryImport libraryImport = (DryRunLibraryImport) element;
                if (libraryImport.getStatus() == null) {
                    return null;
                }
                if (libraryImport.getStatus() == DryRunLibraryImportStatus.ADDED) {
                    return ImagesManager.getImage(RedImages.getBigSuccessImage());
                } else if (libraryImport.getStatus() == DryRunLibraryImportStatus.ALREADY_EXISTING) {
                    return ImagesManager.getImage(RedImages.getBigWarningImage());
                } else if (libraryImport.getStatus() == DryRunLibraryImportStatus.NOT_ADDED) {
                    return ImagesManager.getImage(RedImages.getFatalErrorImage());
                }
            } else if (element instanceof String || element instanceof List<?>) {
                return ImagesManager.getImage(RedImages.getElementImage());
            }
            return null;
        }
    }

    private static class DiscoveredLibrariesComparator implements Comparator<DryRunLibraryImport> {

        @Override
        public int compare(final DryRunLibraryImport import1, final DryRunLibraryImport import2) {
            final DryRunLibraryImportStatus firstStatus = import1.getStatus();
            final DryRunLibraryImportStatus secStatus = import2.getStatus();
            if(firstStatus == secStatus) {
                return import1.getName().compareToIgnoreCase(import2.getName());
            }
            if (firstStatus == DryRunLibraryImportStatus.ADDED && secStatus != DryRunLibraryImportStatus.ADDED) {
                return -1;
            } else if (firstStatus == DryRunLibraryImportStatus.NOT_ADDED
                    && secStatus != DryRunLibraryImportStatus.NOT_ADDED) {
                return 1;
            } else if (firstStatus == DryRunLibraryImportStatus.ALREADY_EXISTING
                    && secStatus == DryRunLibraryImportStatus.ADDED) {
                return 1;
            } else if (firstStatus == DryRunLibraryImportStatus.ALREADY_EXISTING
                    && secStatus == DryRunLibraryImportStatus.NOT_ADDED) {
                return -1;
            }

            return 0;
        }
    }
}
