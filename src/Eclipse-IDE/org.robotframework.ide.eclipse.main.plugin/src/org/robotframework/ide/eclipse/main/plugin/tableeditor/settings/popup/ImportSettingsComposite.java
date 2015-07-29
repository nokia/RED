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
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

public class ImportSettingsComposite extends InputLoadingFormComposite {

    private final RobotEditorCommandsStack commandsStack;

    private final RobotSuiteFile fileModel;

    private ImportLibraryComposite importLibrariesComposite;

    private ImportResourcesComposite importResourcesComposite;

    private ImportVariablesComposite importVariablesComposite;

    public ImportSettingsComposite(final Composite parent, final RobotEditorCommandsStack commandsStack,
            final RobotSuiteFile fileModel) {
        super(parent, SWT.NONE, "Import");
        this.commandsStack = commandsStack;
        this.fileModel = fileModel;
        createComposite();
    }

    @Override
    protected Composite createControl(final Composite parent) {
        setFormImage(RobotImages.getLibraryImage());

        final Composite mainComposite = getToolkit().createComposite(parent);
        final StackLayout stackLayout = new StackLayout();
        mainComposite.setLayout(stackLayout);

        final Composite librariesComposite = createLibrariesComposite(mainComposite);
        final Composite resourcesComposite = createResourcesComposite(mainComposite);
        final Composite variablesComposite = createVariablesComposite(mainComposite);
        stackLayout.topControl = librariesComposite;

        createImportTypeRadioButtons(parent, stackLayout, mainComposite, librariesComposite, resourcesComposite,
                variablesComposite);

        createDisposeListener();

        return mainComposite;
    }

    private void createImportTypeRadioButtons(final Composite parent, final StackLayout stackLayout,
            final Composite mainComposite, final Composite librariesComposite, final Composite resourcesComposite,
            final Composite variablesComposite) {
        final Composite importChooseComposite = getToolkit().createComposite(parent);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(3, 3).applyTo(importChooseComposite);
        final Button libBtn = getToolkit().createButton(importChooseComposite, "Libraries", SWT.RADIO);
        libBtn.setSelection(true);
        libBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                stackLayout.topControl = librariesComposite;
                mainComposite.layout();
            }
        });
        final Button resBtn = getToolkit().createButton(importChooseComposite, "Resources", SWT.RADIO);
        resBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                stackLayout.topControl = resourcesComposite;
                mainComposite.layout();
            }
        });
        final Button varsBtn = getToolkit().createButton(importChooseComposite, "Variables", SWT.RADIO);
        varsBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                stackLayout.topControl = variablesComposite;
                mainComposite.layout();
            }
        });
        super.addHeadClient(importChooseComposite);
    }

    private Composite createLibrariesComposite(Composite parent) {
        importLibrariesComposite = new ImportLibraryComposite(commandsStack, fileModel, getToolkit());
        return importLibrariesComposite.createImportResourcesComposite(parent);
    }

    private Composite createResourcesComposite(final Composite parent) {
        importResourcesComposite = new ImportResourcesComposite(commandsStack, fileModel, getToolkit(), getShell());
        return importResourcesComposite.createImportResourcesComposite(parent);
    }

    private Composite createVariablesComposite(final Composite parent) {
        importVariablesComposite = new ImportVariablesComposite(commandsStack, fileModel, getToolkit(), getShell());
        return importVariablesComposite.createImportLibrariesComposite(parent);
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
    }
}
