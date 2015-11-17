/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
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
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.VariableMapping;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.viewers.Selections;

class VariableMappingsFormFragment implements ISectionFormFragment {

    @Inject
    private RedFormToolkit toolkit;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedProjectEditorInput editorInput;

    private TableViewer viewer;

    private Button addMappingButton;

    private Button removeMappingButton;

    @Override
    public void initialize(final Composite parent) {
        final Section section = toolkit.createSection(parent, ExpandableComposite.EXPANDED
                | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.TWISTIE);
        section.setText("Variables mappings");
        section.setDescription("In this section variable values can be defined. Those mappings will "
                + "be used by RED in order to resolve parameterized paths in Library, Resource and "
                + "Variable settings.");
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

        viewer.setContentProvider(new VariableMappingsContentProvider());
        
        ViewerColumnsFactory.newColumn("Variable name").withWidth(100)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(50)
            .labelsProvidedBy(new VariableMappingsNameLabelProvider())
            .createFor(viewer);

        ViewerColumnsFactory.newColumn("Value").withWidth(100)
            .shouldGrabAllTheSpaceLeft(true).withMinWidth(50)
            .labelsProvidedBy(new VariableMappingsValueLabelProvider())
            .createFor(viewer);

        final ISelectionChangedListener selectionChangedListener = new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                removeMappingButton.setEnabled(!event.getSelection().isEmpty());
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
        addMappingButton = toolkit.createButton(parent, "Add mapping", SWT.PUSH);
        addMappingButton.setEnabled(false);
        addMappingButton.addSelectionListener(createAddingHandler());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(addMappingButton);

        removeMappingButton = toolkit.createButton(parent, "Remove", SWT.PUSH);
        removeMappingButton.setEnabled(false);
        removeMappingButton.addSelectionListener(creteRemovingHandler());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(removeMappingButton);
    }

    private SelectionListener createAddingHandler() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final VariableMappingDialog dialog = new VariableMappingDialog(viewer.getTable().getShell());
                if (dialog.open() == Window.OK) {
                    final VariableMapping mapping = dialog.getMapping();
                    @SuppressWarnings("unchecked")
                    final List<VariableMapping> mappings = (List<VariableMapping>) viewer.getInput();
                    if (!mappings.contains(mapping)) {
                        editorInput.getProjectConfiguration().addVariableMapping(mapping);
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
                final List<VariableMapping> selectedMappings = Selections
                        .getElements((IStructuredSelection) viewer.getSelection(), VariableMapping.class);
                editorInput.getProjectConfiguration().removeVariableMappings(selectedMappings);

                dirtyProviderService.setDirtyState(true);
                viewer.refresh();
            }
        };
    }

    void whenEnvironmentWasLoaded() {
        final boolean isEditable = editorInput.isEditable();

        addMappingButton.setEnabled(isEditable);
        removeMappingButton.setEnabled(false);
        viewer.getTable().setEnabled(isEditable);
    }

    void whenConfigurationFileChanged() {
        addMappingButton.setEnabled(false);
        removeMappingButton.setEnabled(false);
        viewer.getTable().setEnabled(false);
    }

    private void setInput() {
        final List<VariableMapping> variableMappings = editorInput.getProjectConfiguration().getVariableMappings();
        viewer.setInput(variableMappings);
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    @Override
    public MatchesCollection collectMatches(final String filter) {
        return null;
    }

    private static class VariableMappingDialog extends Dialog {

        private VariableMapping mapping;
        private Label exceptionLabel;

        private Text nameText;
        private Text valueText;

        protected VariableMappingDialog(final Shell parentShell) {
            super(parentShell);
        }

        @Override
        public void create() {
            super.create();
            getShell().setText("Add variable mapping");
        }

        @Override
        protected Control createDialogArea(final Composite parent) {
            final Composite dialogComposite = (Composite) super.createDialogArea(parent);
            GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(dialogComposite);

            final Label infoLabel = new Label(dialogComposite, SWT.WRAP);
            infoLabel.setText("Specify name and valu of variable which will be used in parameterized imports.");
            GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).span(2, 1).applyTo(infoLabel);

            final Label nameLabel = new Label(dialogComposite, SWT.NONE);
            nameLabel.setText("Name");
            
            nameText = new Text(dialogComposite, SWT.SINGLE | SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(nameText);
            nameText.setText("${var}");
            nameText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent event) {
                    validate();
                }
            });

            final Label valueLabel = new Label(dialogComposite, SWT.NONE);
            valueLabel.setText("Value");

            valueText = new Text(dialogComposite, SWT.SINGLE | SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).hint(300, SWT.DEFAULT).applyTo(valueText);
            valueText.setText("value");
            valueText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent event) {
                    validate();
                }
            });

            exceptionLabel = new Label(dialogComposite, SWT.NONE);
            exceptionLabel.setText("");
            GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(exceptionLabel);

            nameText.setFocus();

            return dialogComposite;
        }

        private void validate() {
            boolean hasError = false;
            String errorMsg = "";
            if (nameText.getText().isEmpty()) {
                errorMsg += "Name cannot be empty";
                nameText.setBackground(nameText.getDisplay().getSystemColor(SWT.COLOR_RED));
                hasError = true;
            } else {
                nameText.setBackground(nameText.getDisplay().getSystemColor(SWT.COLOR_WHITE));
            }
            
            if (valueText.getText().isEmpty()) {
                errorMsg += "\nValue cannot be empty";
                valueText.setBackground(valueText.getDisplay().getSystemColor(SWT.COLOR_RED));
                hasError = true;
            } else {
                valueText.setBackground(valueText.getDisplay().getSystemColor(SWT.COLOR_WHITE));
            }

            exceptionLabel.setText(errorMsg);
            getButton(IDialogConstants.OK_ID).setEnabled(!hasError);
        }

        @Override
        protected void okPressed() {
            mapping = new VariableMapping();
            mapping.setName(nameText.getText());
            mapping.setValue(valueText.getText());

            super.okPressed();
        }

        VariableMapping getMapping() {
            return mapping;
        }
    }
}
