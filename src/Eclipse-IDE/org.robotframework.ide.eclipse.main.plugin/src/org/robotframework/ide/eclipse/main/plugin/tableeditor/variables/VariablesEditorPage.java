package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSectionPart;

public class VariablesEditorPage extends FormPage implements RobotSectionPart {

    public static final String ID = "org.robotframework.ide.eclipse.editor.variablesPage";
    private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.variables.context";
    static final String SECTION_NAME = "Variables";

    private RobotEditorCommandsStack commandsStack;

    private VariablesFormPart variablesPart;

    public VariablesEditorPage(final FormEditor editor) {
        super(editor, ID, SECTION_NAME);
    }

    @Override
    public boolean isEditor() {
        return true;
    }

    @Override
    protected void createFormContent(final IManagedForm managedForm) {
        super.createFormContent(managedForm);

        final ScrolledForm form = managedForm.getForm();
        form.setImage(getTitleImage());
        form.setText("Variables");
        managedForm.getToolkit().decorateFormHeading(form.getForm());

        GridLayoutFactory.fillDefaults().applyTo(form.getBody());
        GridDataFactory.fillDefaults().applyTo(form.getBody());

        variablesPart = new VariablesFormPart(getEditorSite());
        prepareEclipseContext();

        managedForm.addPart(variablesPart);

        getSite().setSelectionProvider(variablesPart.getViewer());

        final IContextService service = (IContextService) getSite().getService(IContextService.class);
        service.activateContext(CONTEXT_ID);
    }

    private void prepareEclipseContext() {
        commandsStack = new RobotEditorCommandsStack();
        final IEclipseContext context = ((IEclipseContext) getSite().getService(IEclipseContext.class)).getActiveLeaf();
        context.set(RobotEditorCommandsStack.class, commandsStack);
        ContextInjectionFactory.inject(variablesPart, context);
    }

    @Override
    public RobotFormEditor getEditor() {
        return (RobotFormEditor) super.getEditor();
    }

    @Override
    public void dispose() {
        super.dispose();

        if (commandsStack != null) {
            commandsStack.clear();
        }
        ContextInjectionFactory.uninject(variablesPart,
                ((IEclipseContext) getSite().getService(IEclipseContext.class)).getActiveLeaf());
    }

    @Override
    public Image getTitleImage() {
        return RobotImages.getRobotVariableImage().createImage();
    }

    public void revealVariable(final RobotVariable robotVariable) {
        variablesPart.revealVariable(robotVariable);
    }

    @Override
    public boolean isPartFor(final RobotSuiteFileSection section) {
        return section.getName().equals(SECTION_NAME);
    }
}
