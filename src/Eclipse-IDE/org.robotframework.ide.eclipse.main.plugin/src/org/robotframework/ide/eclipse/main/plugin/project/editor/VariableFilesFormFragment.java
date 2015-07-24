package org.robotframework.ide.eclipse.main.plugin.project.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.Path;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.viewers.Selections;

/**
 * @author mmarzec
 */
class VariableFilesFormFragment implements ISectionFormFragment {

    @Inject
    private RedFormToolkit toolkit;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedProjectEditorInput editorInput;

    private TableViewer viewer;

    private Button addFileBtn;

    private Button removeFileBtn;

    @Override
    public void initialize(final Composite parent) {
        final Section section = toolkit.createSection(parent, Section.EXPANDED | Section.TITLE_BAR
                | Section.DESCRIPTION | Section.TWISTIE);
        section.setText("Variable files");
        section.setDescription("In this section variable files can be specified. Those variables will "
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
        viewer.getTable().setEnabled(false);

        viewer.setContentProvider(new VariableFilesContentProvider());

        ViewerColumnsFactory.newColumn("")
                .withWidth(100)
                .shouldGrabAllTheSpaceLeft(true)
                .withMinWidth(100)
                .labelsProvidedBy(new VariableFilesLabelProvider())
                .createFor(viewer);

        final ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                removeFileBtn.setEnabled(!event.getSelection().isEmpty());
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
        addFileBtn = toolkit.createButton(parent, "Add Variable File", SWT.PUSH);
        addFileBtn.setEnabled(false);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(addFileBtn);
        addFileBtnHandler();

        removeFileBtn = toolkit.createButton(parent, "Remove", SWT.PUSH);
        removeFileBtn.setEnabled(false);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(removeFileBtn);
        addRemoveBtnHandler();
    }

    private void addFileBtnHandler() {
        addFileBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {

                final String startingPath = editorInput.getRobotProject().getProject().getLocation().toOSString();
                final VariableFileDialog dialog = new VariableFileDialog(viewer.getTable().getShell(), startingPath);
                if (dialog.open() == Window.OK) {
                    final ReferencedVariableFile variableFile = dialog.getVariableFile();

                    final String name = new File(variableFile.getPath()).getName();
                    variableFile.setName(name);

                    editorInput.getProjectConfiguration().addReferencedVariableFile(variableFile);

                    dirtyProviderService.setDirtyState(true);
                    viewer.refresh();
                }
            }
        });
    }

    private void addRemoveBtnHandler() {
        removeFileBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final List<ReferencedVariableFile> selectedFiles = Selections.getElements(
                        (IStructuredSelection) viewer.getSelection(), ReferencedVariableFile.class);
                editorInput.getProjectConfiguration().removeReferencedVariableFiles(selectedFiles);
                dirtyProviderService.setDirtyState(true);
                viewer.refresh();
            }
        });
    }

    void whenEnvironmentWasLoaded() {
        final boolean isEditable = editorInput.isEditable();

        addFileBtn.setEnabled(isEditable);
        removeFileBtn.setEnabled(false);
        viewer.getTable().setEnabled(isEditable);

    }

    void whenConfigurationFiledChanged() {
        addFileBtn.setEnabled(false);
        removeFileBtn.setEnabled(false);
        viewer.getTable().setEnabled(false);
    }

    private void setInput() {
        final List<ReferencedVariableFile> files = editorInput.getProjectConfiguration().getReferencedVariableFiles();
        viewer.setInput(files);
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    private static class VariableFileDialog extends Dialog {

        private ReferencedVariableFile variableFile;

        private Text pathText;

        private Text argsText;

        private String startingPath;

        protected VariableFileDialog(final Shell parentShell, String startingPath) {
            super(parentShell);
            this.startingPath = startingPath;
        }

        @Override
        public void create() {
            super.create();
            getShell().setText("Add Variable File");
        }

        @Override
        protected Control createDialogArea(final Composite parent) {
            final Composite dialogComposite = (Composite) super.createDialogArea(parent);
            GridLayoutFactory.fillDefaults().numColumns(3).margins(10, 10).applyTo(dialogComposite);

            final Label pathLabel = new Label(dialogComposite, SWT.WRAP);
            pathLabel.setText("Path:");

            pathText = new Text(dialogComposite, SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, true).applyTo(pathText);
            
            final Button browseFileBtn = new Button(dialogComposite, SWT.NONE);
            browseFileBtn.setText("Browse...");
            browseFileBtn.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(final SelectionEvent e) {
                    final FileDialog dialog = new FileDialog(dialogComposite.getShell(), SWT.OPEN);
                    dialog.setFilterPath(startingPath);
                    dialog.setFilterExtensions(new String[] { "*.py" });
                    final String chosenFilePath = dialog.open();
                    if (chosenFilePath != null) {
                        pathText.setText(chosenFilePath);
                    }
                }
            });

            final Label argsLabel = new Label(dialogComposite, SWT.NONE);
            argsLabel.setText("Arguments:");

            argsText = new Text(dialogComposite, SWT.SINGLE | SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(argsText);
            
            return dialogComposite;
        }

        @Override
        protected void okPressed() {
            variableFile = new ReferencedVariableFile();

            variableFile.setPath(new Path(pathText.getText()).toPortableString());
            String args = argsText.getText();
            if (args != null && !args.equals("")) {
                String[] argsArray = args.split("\\s*[|]\\s*");
                List<String> argsList = new ArrayList<String>();
                for (int i = 0; i < argsArray.length; i++) {
                    argsList.add(argsArray[i]);
                }
                variableFile.setArguments(argsList);
            }

            super.okPressed();
        }

        ReferencedVariableFile getVariableFile() {
            return variableFile;
        }
    }
}
