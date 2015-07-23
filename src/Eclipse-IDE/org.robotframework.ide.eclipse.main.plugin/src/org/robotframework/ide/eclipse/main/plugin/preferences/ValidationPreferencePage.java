package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.util.Collection;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProjectConfigurationProblem;

public class ValidationPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.validation";

    public ValidationPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, RobotFramework.PLUGIN_ID));
        setDescription("Robot validation settings");
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        createProjectConfigurationSection(parent);
    }

    private void createProjectConfigurationSection(Composite parent) {
        final ExpandableComposite redExpandableComposite = new ExpandableComposite(parent,
                ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR);
        redExpandableComposite.setExpanded(true);
        redExpandableComposite.setText("Project configuration file (" + RobotProjectConfig.FILENAME + ")");
        GridDataFactory.fillDefaults().grab(true, true).hint(900, SWT.DEFAULT).applyTo(redExpandableComposite);
        
        final Composite client = new Composite(redExpandableComposite, SWT.NONE);
        redExpandableComposite.setClient(client);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(client);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(client);
        
        final Collection<ProblemCategory> categoriesOfProjectProblems = ProjectConfigurationProblem.getCategories();
        for (final ProblemCategory category : categoriesOfProjectProblems) {
            addField(new ComboBoxFieldEditor(category.getId(), category.getName(), category.getDescription(), 20,
                    onlyFatalEntries(), client));
        }
    }

    private static String[][] onlyFatalEntries() {
        return entries(entry("FATAL"));
    }

    private static String[][] defaultEntries() {
        return entries(entry("Error"), entry("Warning"), entry("Info"), entry("Ignore"));
    }

    private static String[][] entries(final String[]... entries) {
        return entries;
    }

    private static String[] entry(final String value) {
        return new String[] { value, value };
    }

}
