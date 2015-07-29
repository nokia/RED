package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

import com.google.common.base.Optional;


public class ImportVariablesComposite {

    private RobotEditorCommandsStack commandsStack;
    private FormToolkit formToolkit;
    private Shell shell;
    private RobotSettingsSection settingsSection;
    
    public ImportVariablesComposite(final RobotEditorCommandsStack commandsStack, final RobotSuiteFile fileModel,
            final FormToolkit formToolkit, final Shell shell) {
        this.commandsStack = commandsStack;
        this.formToolkit = formToolkit;
        this.shell = shell;
        
        final Optional<RobotElement> section = fileModel.findSection(RobotSettingsSection.class);
        this.settingsSection = (RobotSettingsSection) section.get();
    }
    
    public Composite createImportLibrariesComposite(final Composite parent) {
        final Composite librariesComposite = formToolkit.createComposite(parent);
        GridLayoutFactory.fillDefaults()
                .numColumns(1)
                .margins(3, 3)
                .extendedMargins(0, 0, 0, 3)
                .applyTo(librariesComposite);
        final Label titleLabel = formToolkit.createLabel(librariesComposite, "Imported libraries:");

        return librariesComposite;
    }
    
}
