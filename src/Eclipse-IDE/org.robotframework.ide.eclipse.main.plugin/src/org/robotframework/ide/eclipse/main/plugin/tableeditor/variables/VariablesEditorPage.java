package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSectionPart;

public class VariablesEditorPage extends FormPage implements RobotSectionPart {

    public static final String ID = "org.robotframework.ide.eclipse.editor.variablesPage";
    static final String SECTION_NAME = "Variables";

    private VariablesFormPart variablesPart;

    public VariablesEditorPage(final FormEditor editor) {
        super(editor, ID, SECTION_NAME);
    }

    @Override
    protected void createFormContent(final IManagedForm managedForm) {
        super.createFormContent(managedForm);
        ContextInjectionFactory.inject(this, (IEclipseContext) getSite().getService(IEclipseContext.class));

        final ScrolledForm form = managedForm.getForm();
        form.setImage(getTitleImage());
        form.setText("Variables");
        managedForm.getToolkit().decorateFormHeading(form.getForm());

        GridLayoutFactory.fillDefaults().applyTo(form.getBody());
        GridDataFactory.fillDefaults().applyTo(form.getBody());

        variablesPart = new VariablesFormPart();
        ContextInjectionFactory.inject(variablesPart, (IEclipseContext) getSite().getService(IEclipseContext.class));
        managedForm.addPart(variablesPart);
    }

    @Override
    public RobotFormEditor getEditor() {
        return (RobotFormEditor) super.getEditor();
    }

    @Override
    public void dispose() {
        super.dispose();
        ContextInjectionFactory.uninject(this, (IEclipseContext) getSite().getService(IEclipseContext.class));
        ContextInjectionFactory.uninject(variablesPart, (IEclipseContext) getSite().getService(IEclipseContext.class));
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
