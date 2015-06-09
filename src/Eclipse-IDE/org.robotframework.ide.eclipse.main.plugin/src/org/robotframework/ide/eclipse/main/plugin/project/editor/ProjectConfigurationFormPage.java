package org.robotframework.ide.eclipse.main.plugin.project.editor;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;

public class ProjectConfigurationFormPage extends FormPage {

    private static final String ID = "org.robotframework.ide.project.editor.mainPage";
    private final IProject project;
    private final RobotProjectConfig configuration;
    private final boolean isEditable;

    public ProjectConfigurationFormPage(final FormEditor editor, final IProject project,
            final RobotProjectConfig configuration, final boolean isEditable) {
        super(editor, ID, "RED Project");
        this.project = project;
        this.configuration = configuration;
        this.isEditable = isEditable;
    }

    @Override
    public boolean isEditor() {
        return true;
    }

    @Override
    protected void createFormContent(final IManagedForm managedForm) {
        super.createFormContent(managedForm);
        prepareManagedForm(managedForm);

        final List<? extends IFormPart> pageParts = createPageParts(getEditorSite());

        for (final IFormPart part : pageParts) {
            managedForm.addPart(part);
        }
    }

    private void prepareManagedForm(final IManagedForm managedForm) {
        final ScrolledForm form = managedForm.getForm();
        form.setImage(RobotImages.getRobotProjectConfigFile().createImage());
        form.setText(getPartName());
        managedForm.getToolkit().decorateFormHeading(form.getForm());

        GridDataFactory.fillDefaults().applyTo(form.getBody());
        GridLayoutFactory.fillDefaults().applyTo(form.getBody());
    }

    private List<? extends IFormPart> createPageParts(final IEditorSite editorSite) {
        return Arrays.asList(new FrameworksFormPart(project, configuration, isEditable));
    }
}
