package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tempmodel.cmd.CreateSectionCommand;

public class VariablesFormPart extends AbstractFormPart {

    @Inject
    @Named("suiteFileModel")
    private RobotSuiteFile fileModel;

    @Inject
    private RobotEditorCommandsStack commandsStack;

    private TableViewer viewer;
    private HyperlinkAdapter createSectionLinkListener;

    @Override
    public final void initialize(final IManagedForm managedForm) {
        super.initialize(managedForm);
        createContent(managedForm.getForm().getBody());
    }

    private void createContent(final Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);

        addActivationStrategy();
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        viewer.setContentProvider(new VariablesContentProvider());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());

        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);

        createColumn("Variable", 270, new DelegatingStyledCellLabelProvider(new VariableNameLabelProvider()),
                new VariableNameEditingSupport(viewer));

        createColumn("Value", 270, new VariableValueLabelProvider(), new VariableValueEditingSupport(viewer));

        createColumn("Comment", 400, new DelegatingStyledCellLabelProvider(new VariableCommentLabelProvider()),
                new VariableCommentEditingSupport(viewer));

        setInput();

    }

    private void setInput() {
        final RobotSuiteFileSection variableSection = findVariableSection(fileModel.getSections());
        final Form form = getManagedForm().getForm().getForm();
        if (variableSection == null && fileModel.isEditable()) {
            createSectionLinkListener = createHyperlinkListener(fileModel);
            form.addMessageHyperlinkListener(createSectionLinkListener);
            form.setMessage("There is no Variables section defined, do you want to define it?", IMessageProvider.ERROR);
        } else {
            form.removeMessageHyperlinkListener(createSectionLinkListener);
            if (variableSection.isReadOnly()) {
                form.setMessage("Variable section is read-only!", IMessageProvider.WARNING);
            } else {
                form.setMessage(null, 0);
            }
            viewer.setInput(variableSection);
        }
    }

    private HyperlinkAdapter createHyperlinkListener(final RobotSuiteFile suite) {
        final HyperlinkAdapter createSectionLinkListener = new HyperlinkAdapter() {
            @Override
            public void linkEntered(final HyperlinkEvent e) {
                ((Hyperlink) e.getSource()).setToolTipText("Click to create variables section");
            }

            @Override
            public void linkActivated(final HyperlinkEvent e) {
                commandsStack.execute(new CreateSectionCommand(suite, VariablesEditorPage.SECTION_NAME));
            }
        };
        return createSectionLinkListener;
    }

    private RobotSuiteFileSection findVariableSection(final List<RobotElement> sections) {
        for (final RobotElement section : sections) {
            if (section.getName().equals(VariablesEditorPage.SECTION_NAME)) {
                return (RobotSuiteFileSection) section;
            }
        }
        return null;
    }

    private void createColumn(final String columnName, final int width, final CellLabelProvider labelProvider,
            final EditingSupport editingSupport) {
        final TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
        column.getColumn().setText(columnName);
        column.getColumn().setWidth(width);
        column.setLabelProvider(labelProvider);
        column.setEditingSupport(editingSupport);
    }

    private void addActivationStrategy() {
        final TableViewerFocusCellManager fcm = new TableViewerFocusCellManager(viewer, new VariableCellsHighlighter(
                viewer));
        final ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(viewer) {

            @Override
            protected boolean isEditorActivationEvent(final ColumnViewerEditorActivationEvent event) {
                if (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED) {
                    if (event.character == SWT.CR || isPrintableChar(event.character)) {
                        return true;
                    }
                } else if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION) {
                    if (event.sourceEvent instanceof MouseEvent) {
                        return true;
                    }
                } else if (event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL) {
                    return true;
                } else if (event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC) {
                    return true;
                }
                return false;
            }

            private boolean isPrintableChar(final char character) {
                return ' ' <= character && character <= '~';
            }
        };
        activationSupport.setEnableEditorActivationWithKeyboard(true);
        TableViewerEditor.create(viewer, fcm, activationSupport, ColumnViewerEditor.KEYBOARD_ACTIVATION
                | ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_VERTICAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR);
    }
    
    @Inject
    @Optional
    private void whenFileModelChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_FILE_ALL) final RobotSuiteFile affectedFile) {
        if (affectedFile == fileModel) {
            setInput();
            markDirty();
        }
    }

    public void revealVariable(final RobotVariable robotVariable) {
        viewer.setSelection(new StructuredSelection(new Object[] { robotVariable }));
    }
}
