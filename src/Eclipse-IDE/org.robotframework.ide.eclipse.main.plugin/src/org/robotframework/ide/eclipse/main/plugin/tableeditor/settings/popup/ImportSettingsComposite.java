package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.popup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.InputLoadingFormComposite;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

public class ImportSettingsComposite extends InputLoadingFormComposite {

    private final RobotEditorCommandsStack commandsStack;

    private final RobotSuiteFile fileModel;

    private RobotSetting initialSetting;

    private ImportLibraryComposite importLibrariesComposite;

    private ImportResourcesComposite importResourcesComposite;

    private ImportVariablesComposite importVariablesComposite;

    private Composite librariesComposite;

    private Composite resourcesComposite;

    private Composite variablesComposite;

    private Button variablesSwitcher;

    private Button resourcesSwitcher;

    private Button librariesSwitcher;

    public ImportSettingsComposite(final Composite parent, final RobotEditorCommandsStack commandsStack,
            final RobotSuiteFile fileModel, final RobotSetting initialSetting) {
        super(parent, SWT.NONE, "Import");
        this.commandsStack = commandsStack;
        this.fileModel = fileModel;
        this.initialSetting = initialSetting;
        createComposite();
    }

    @Override
    protected Control createHeadClient(final Composite head) {
        final Composite importChooseComposite = getToolkit().createComposite(head);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(3, 3).applyTo(importChooseComposite);
        importChooseComposite.setBackground(null);

        createLibrariesSwitcher(importChooseComposite);
        createResourcesSwitcher(importChooseComposite);
        createVariablesSwitcher(importChooseComposite);
        return importChooseComposite;
    }

    private void createVariablesSwitcher(final Composite importChooseComposite) {
        variablesSwitcher = getToolkit().createButton(importChooseComposite, "Variables", SWT.RADIO);
        variablesSwitcher.setBackground(null);
        variablesSwitcher.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Composite parent = librariesComposite.getParent();
                ((StackLayout) parent.getLayout()).topControl = variablesComposite;
                parent.layout();
            }
        });
    }

    private void createResourcesSwitcher(final Composite importChooseComposite) {
        resourcesSwitcher = getToolkit().createButton(importChooseComposite, "Resources", SWT.RADIO);
        resourcesSwitcher.setBackground(null);
        resourcesSwitcher.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Composite parent = librariesComposite.getParent();
                ((StackLayout) parent.getLayout()).topControl = resourcesComposite;
                parent.layout();
            }
        });
    }

    private void createLibrariesSwitcher(final Composite importChooseComposite) {
        librariesSwitcher = getToolkit().createButton(importChooseComposite, "Libraries", SWT.RADIO);
        librariesSwitcher.setBackground(null);
        librariesSwitcher.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final Composite parent = librariesComposite.getParent();
                ((StackLayout) parent.getLayout()).topControl = librariesComposite;
                parent.layout();
            }
        });
    }

    @Override
    protected Composite createControl(final Composite parent) {
        setFormImage(RedImages.getLibraryImage());

        final Composite mainComposite = getToolkit().createComposite(parent);
        final StackLayout stackLayout = new StackLayout();
        mainComposite.setLayout(stackLayout);

        librariesComposite = createLibrariesComposite(mainComposite);
        resourcesComposite = createResourcesComposite(mainComposite);
        variablesComposite = createVariablesComposite(mainComposite);
        setTopControl(stackLayout);

        createDisposeListener();

        return mainComposite;
    }

    private Composite createLibrariesComposite(final Composite parent) {
        importLibrariesComposite = new ImportLibraryComposite(commandsStack, fileModel, getToolkit(), getShell());
        return importLibrariesComposite.createImportResourcesComposite(parent);
    }

    private Composite createResourcesComposite(final Composite parent) {
        importResourcesComposite = new ImportResourcesComposite(commandsStack, fileModel, getToolkit(), getShell());
        return importResourcesComposite.createImportResourcesComposite(parent);
    }

    private Composite createVariablesComposite(final Composite parent) {
        importVariablesComposite = new ImportVariablesComposite(commandsStack, fileModel, getToolkit(), getShell());
        return importVariablesComposite.createImportVariablesComposite(parent);
    }
    
    private void setTopControl(StackLayout stackLayout) {
        if (initialSetting == null) {
            stackLayout.topControl = librariesComposite;
            librariesSwitcher.setSelection(true);
        } else {
            if (initialSetting.getGroup() == SettingsGroup.RESOURCES) {
                stackLayout.topControl = resourcesComposite;
                resourcesSwitcher.setSelection(true);
            } else if (initialSetting.getGroup() == SettingsGroup.VARIABLES) {
                stackLayout.topControl = variablesComposite;
                variablesSwitcher.setSelection(true);
            } else {
                stackLayout.topControl = librariesComposite;
                librariesSwitcher.setSelection(true);
            }
        }
    }

    private void createDisposeListener() {

        addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(final DisposeEvent e) {
                importLibrariesComposite.getLeftViewer().removeSelectionChangedListener(
                        importLibrariesComposite.getLeftViewerSelectionChangedListener());
                importLibrariesComposite.getRightViewer().removeSelectionChangedListener(
                        importLibrariesComposite.getRightViewerSelectionChangedListener());
                importResourcesComposite.getResourcesViewer().removeSelectionChangedListener(
                        importResourcesComposite.getSelectionChangedListener());
                importVariablesComposite.getVariablesViewer().removeSelectionChangedListener(
                        importVariablesComposite.getSelectionChangedListener());
            }
        });
    }

    @Override
    protected InputLoadingFormComposite.InputJob provideInputCollectingJob() {
        return new InputJob("Gathering resources for import") {

            @Override
            protected Object createInput(final IProgressMonitor monitor) {
                setStatus(Status.OK_STATUS);
                return Settings.create(fileModel);
            }
        };
    }

    @Override
    protected void fillControl(final Object jobResult) {
        final Settings libs = (Settings) jobResult;
        importLibrariesComposite.getLeftViewer().setInput(libs);
        importLibrariesComposite.getRightViewer().setInput(libs);
        importResourcesComposite.getResourcesViewer().setInput(libs);
        importVariablesComposite.getVariablesViewer().setInput(libs);
        
        setInitialSelection();
    }
    
    private void setInitialSelection() {
        if (initialSetting != null) {
            if (initialSetting.getGroup() == SettingsGroup.RESOURCES) {
                importResourcesComposite.setInitialSelection(initialSetting);
            } else if (initialSetting.getGroup() == SettingsGroup.VARIABLES) {
                importVariablesComposite.setInitialSelection(initialSetting);
            } else {
                importLibrariesComposite.setInitialSelection(initialSetting);
            }
        }
    }
}
