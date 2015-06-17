package org.robotframework.ide.eclipse.main.plugin.project.editor;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.robotframework.forms.RedFormToolkit;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;

public class ReferencedLibrariesFormFragment implements ISectionFormFragment {

    @Inject
    private RedFormToolkit toolkit;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private Form form;

    @Inject
    private RedProjectEditorInput editorInput;

    private TableViewer viewer;

    private Button addPythonLibButton;

    private Button addJavaLibButton;

    private Button addLibspecButton;

    private Button removeButton;

    @Override
    public void initialize(final Composite parent) {
        final Section section = toolkit.createSection(parent, Section.EXPANDED | Section.TITLE_BAR
                | Section.DESCRIPTION | Section.TWISTIE);
        section.setText("Referenced libraries");
        section.setDescription("In this section referenced libraries can be specified. Those libraries will "
                + "be available for all suites within project.");
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
        viewer = new TableViewer(parent);
        GridDataFactory.fillDefaults().grab(true, true).span(1, 4).applyTo(viewer.getTable());

        viewer.setContentProvider(new ReferencedLibrariesContentProvider());
        
        ViewerColumnsFactory.newColumn("").withWidth(100)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(100)
            .labelsProvidedBy(new ReferencedLibrariesLabelProvider())
            .createFor(viewer);

        final ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                removeButton.setEnabled(!event.getSelection().isEmpty());
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
        addPythonLibButton = toolkit.createButton(parent, "Add Python library", SWT.PUSH);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(addPythonLibButton);

        addJavaLibButton = toolkit.createButton(parent, "Add Java library", SWT.PUSH);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(addJavaLibButton);

        addLibspecButton = toolkit.createButton(parent, "Add libspec file", SWT.PUSH);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(addLibspecButton);
        addLibspecHandler();
        
        removeButton = toolkit.createButton(parent, "Remove", SWT.PUSH);
        removeButton.setEnabled(false);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(removeButton);
    }

    private void addLibspecHandler() {
        addLibspecButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(addLibspecButton.getShell(),
                        new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
                dialog.setAllowMultiple(true);
                dialog.setTitle("Select library specification");
                dialog.setMessage("Select the library specification file:");
                dialog.addFilter(new ViewerFilter() {
                    @Override
                    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
                        return element instanceof IResource
                                && ((IResource) element).getProject().getName()
                                        .equals(editorInput.getRobotProject().getName());
                    }
                });
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
                if (dialog.open() == Window.OK) {
                    final IResource resource = (IResource) dialog.getFirstResult();
                    editorInput.getProjectConfiguration().addReferencedLibrarySpecification(
                            resource.getProjectRelativePath());
                    dirtyProviderService.setDirtyState(true);
                    viewer.refresh();
                }
            }
        });
    }

    private void setInput() {
        final List<String> libspecs = editorInput.getProjectConfiguration().getLibrarySpecifications();
        viewer.setInput(libspecs);
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }
}
