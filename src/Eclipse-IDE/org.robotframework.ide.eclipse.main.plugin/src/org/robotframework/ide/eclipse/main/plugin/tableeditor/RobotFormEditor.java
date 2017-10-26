/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.rf.ide.core.executor.RedSystemProperties;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.DumpContext;
import org.rf.ide.core.testdata.DumpedResultBuilder.DumpedResult;
import org.rf.ide.core.testdata.RobotFileDumper;
import org.rf.ide.core.testdata.mapping.QuickTokenListenerBaseTwoModelReferencesLinker;
import org.rf.ide.core.testdata.mapping.TwoModelReferencesLinker;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.ElementOpenMode;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteStreamFile;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsPreferencesPage;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.RobotSuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.TsvRobotSuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.KeywordsEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesEditorPart;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.dialogs.ErrorDialogWithLinkToPreferences;
import org.robotframework.red.swt.SwtThread;

public class RobotFormEditor extends FormEditor {

    private static final String EDITOR_CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.context";

    public static final String ID = "org.robotframework.ide.tableditor";

    private static IPartListener robotFormEditorPartListener;

    private final List<IEditorPart> parts = newArrayList();

    private RedClipboard clipboard;

    private RobotSuiteFile suiteModel;

    private boolean isEditable;

    private SuiteFileValidationListener validationListener;

    private final OnSaveLibrariesAutodiscoveryTrigger saveLibDiscoveryTrigger = new OnSaveLibrariesAutodiscoveryTrigger();

    public RedClipboard getClipboard() {
        return clipboard;
    }

    @Override
    public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
        try {
            super.init(site, input);

            clipboard = new RedClipboard(site.getShell().getDisplay());
            validationListener = new SuiteFileValidationListener();

            prepareEclipseContext();

            site.getService(ICommandService.class).addExecutionListener(saveLibDiscoveryTrigger);

            initRobotFormEditorPartListener(site.getPage());

        } catch (final IllegalRobotEditorInputException e) {
            throw new PartInitException("Unable to open editor", e);
        }
    }

    private void prepareEclipseContext() {
        final IEclipseContext parentContext = getSite().getService(IEclipseContext.class);
        final IEclipseContext eclipseContext = parentContext.getActiveLeaf();
        eclipseContext.set(RobotEditorSources.SUITE_FILE_MODEL, new ContextFunction() {

            @Override
            public Object compute(final IEclipseContext context, final String contextKey) {
                return provideSuiteModel();
            }
        });
        eclipseContext.set(RedClipboard.class, clipboard);
        eclipseContext.set(SuiteFileMarkersContainer.class, validationListener);
        ContextInjectionFactory.inject(this, eclipseContext);
        ContextInjectionFactory.inject(validationListener, eclipseContext);
    }

    private void initRobotFormEditorPartListener(final IWorkbenchPage page) {
        if (robotFormEditorPartListener == null) {
            robotFormEditorPartListener = new RobotFormEditorPartListener();
            page.addPartListener(robotFormEditorPartListener);
        }
    }

    @Override
    protected void setInput(final IEditorInput input) {
        if (input instanceof FileEditorInput) {
            final IFile file = ((FileEditorInput) input).getFile();
            isEditable = !file.isReadOnly();
            setPartName(RedPlugin.getDefault().getPreferences().isParentDirectoryNameInTabEnabled()
                    ? file.getParent().getName() + "/" + input.getName()
                    : input.getName());
        } else {
            final IStorage storage = input.getAdapter(IStorage.class);
            if (storage != null) {
                isEditable = !storage.isReadOnly();
                setPartName(storage.getName() + " [" + storage.getFullPath() + "]");
            } else {
                throw new IllegalRobotEditorInputException(
                        "Unable to open editor: unrecognized input of class: " + input.getClass().getName());
            }
        }
        super.setInput(input);
    }

    public boolean isEditable() {
        return isEditable;
    }

    @Override
    protected void addPages() {
        try {
            prepareCommandsContext();

            if (provideSuiteModel().isSuiteFile()) {
                addEditorPart(new CasesEditorPart(), "Test Cases");
            }
            addEditorPart(new KeywordsEditorPart(), "Keywords");
            addEditorPart(new SettingsEditorPart(), "Settings");
            addEditorPart(new VariablesEditorPart(), "Variables");
            addEditorPart(new SuiteSourceEditor(), "Source", ImagesManager.getImage(RedImages.getSourceImage()));

            activateProperPage();
            initializeMarkersSupportForTables();
        } catch (final Exception e) {
            throw new RobotEditorOpeningException("Unable to initialize Suite editor", e);
        }
    }

    private void initializeMarkersSupportForTables() {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(validationListener, IResourceChangeEvent.POST_CHANGE);
        validationListener.init();
    }

    private void activateProperPage() {
        final String pageToActivate = RobotFormEditorActivePageSaver.getLastActivePageId(getEditorInput());
        if (pageToActivate == null) {
            final ElementOpenMode openMode = RedPlugin.getDefault().getPreferences().getElementOpenMode();
            if (openMode == ElementOpenMode.OPEN_IN_SOURCE) {
                setActivePart("");
            } else {
                activateFirstPage();
            }
        } else {
            setActivePart(pageToActivate);
        }
    }

    private void setActivePart(final String pageIdToActivate) {
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editorPart = getEditor(i);
            if (editorPart instanceof ISectionEditorPart
                    && ((ISectionEditorPart) editorPart).getId().equals(pageIdToActivate)) {
                setActivePage(i);
                return;
            }
        }
        setActivePage(getPageCount() - 1);
    }

    private void addEditorPart(final IEditorPart editorPart, final String partName) throws PartInitException {
        addEditorPart(editorPart, partName, editorPart.getTitleImage());
    }

    private void addEditorPart(final IEditorPart editorPart, final String partName, final Image image)
            throws PartInitException {
        parts.add(editorPart);
        final IEclipseContext parentContext = getSite().getService(IEclipseContext.class);
        final IEclipseContext eclipseContext = parentContext.getActiveLeaf();
        ContextInjectionFactory.inject(editorPart, eclipseContext);

        final int newEditorPart = addPage(editorPart, getEditorInput());
        setPageImage(newEditorPart, image);
        setPageText(newEditorPart, partName);
    }

    private void prepareCommandsContext() {
        final IContextService commandsContext = getSite().getService(IContextService.class);
        commandsContext.activateContext(EDITOR_CONTEXT_ID);
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        waitForPendingEditorJobs();

        boolean shouldSave = true;
        boolean shouldClose = false;
        final RobotSuiteFile currentModel = provideSuiteModel();

        if (!(getActiveEditor() instanceof SuiteSourceEditor)) {
            updateSourceFromModel();
        }

        final int description = determineContentDescription();
        if (currentModel.isSuiteFile() && description == IContentDescriber.INVALID) {
            shouldSave = MessageDialog.openConfirm(getSite().getShell(), "File content mismatch",
                    "The file " + currentModel.getFile().getName() + " is a Suite file, but after "
                            + "changes there is no Test Cases section. From now on this file will be recognized as "
                            + "Resource file.\n\nClick OK to save and reopen editor or cancel saving");
            shouldClose = true;
        } else if (currentModel.isResourceFile() && description == IContentDescriber.VALID) {
            shouldSave = MessageDialog.openConfirm(getSite().getShell(), "File content mismatch",
                    "The file " + currentModel.getFile().getName() + " is a Resource file, but after "
                            + "changes there is a Test Cases section defined. From now on this file will be recognized "
                            + "as Suite file.\n\nClick OK to save and reopen editor or cancel saving");
            shouldClose = true;
        }
        if (!shouldSave) {
            monitor.setCanceled(true);
            return;
        }

        for (final IEditorPart dirtyEditor : getDirtyEditors()) {
            dirtyEditor.doSave(monitor);
        }
        updateActivePage();

        saveLibDiscoveryTrigger.startLibrariesAutoDiscoveryIfRequired(currentModel);

        if (shouldClose) {
            reopenEditor();
        }
    }

    private int determineContentDescription() {
        try {
            final StringReader reader = new StringReader(getSourceEditor().getDocument().get());
            final String fileExt = suiteModel.getFileExtension();

            final ASuiteFileDescriber desc = fileExt != null && fileExt.toLowerCase().equals("tsv")
                    ? new TsvRobotSuiteFileDescriber() : new RobotSuiteFileDescriber();

            return desc.describe(reader, null);
        } catch (final IOException e) {
            // nothing to do
        }
        return IContentDescriber.INDETERMINATE;
    }

    private void waitForPendingEditorJobs() {
        // jobs are sending model modification events, so it has to be done before dumping model to
        // source
        for (final IEditorPart part : parts) {
            if (part instanceof ISectionEditorPart) {
                ((ISectionEditorPart) part).waitForPendingJobs();
            }
        }
    }

    private List<? extends IEditorPart> getDirtyEditors() {
        final List<IEditorPart> dirtyEditors = newArrayList();
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editorPart = getEditor(i);
            if (editorPart != null && editorPart.isDirty()) {
                dirtyEditors.add(editorPart);
            }
        }
        return dirtyEditors;
    }

    private void reopenEditor() {
        close(false);
        getSite().getShell().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                final IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
                final IEditorDescriptor desc = editorRegistry.findEditor(RobotFormEditor.ID);
                final IWorkbenchPage page = RobotFormEditor.this.getSite().getPage();
                try {
                    page.openEditor(new FileEditorInput(suiteModel.getFile()), desc.getId());
                } catch (final PartInitException e) {
                    throw new IllegalStateException("Unable to reopen editor", e);
                }
            }
        });
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSaveAs() {
        // it is not allowed currently
    }

    @Override
    public void dispose() {
        final IEclipseContext parentContext = getSite().getService(IEclipseContext.class);
        final IEclipseContext context = parentContext.getActiveLeaf();
        ContextInjectionFactory.uninject(this, context);
        ContextInjectionFactory.uninject(validationListener, context);
        for (final IEditorPart part : parts) {
            ContextInjectionFactory.uninject(part, context);
        }

        super.dispose();

        ResourcesPlugin.getWorkspace().removeResourceChangeListener(validationListener);

        getSite().getService(ICommandService.class).removeExecutionListener(saveLibDiscoveryTrigger);

        clipboard.dispose();
        suiteModel.dispose();

        final IEventBroker eventBroker = PlatformUI.getWorkbench().getService(IEventBroker.class);
        eventBroker.post(RobotModelEvents.SUITE_MODEL_DISPOSED, RobotElementChange.createChangedElement(suiteModel));
        RobotArtifactsValidator.revalidate(suiteModel);
    }

    public SelectionLayerAccessor getSelectionLayerAccessor() {
        final IEditorPart activeEditor = getActiveEditor();
        if (activeEditor instanceof ISectionEditorPart) {
            return ((ISectionEditorPart) activeEditor).getSelectionLayerAccessor();
        }
        return null;
    }

    public java.util.Optional<TreeLayerAccessor> getTreeLayerAccessor() {
        final IEditorPart activeEditor = getActiveEditor();
        if (activeEditor instanceof ISectionEditorPart) {
            return ((ISectionEditorPart) activeEditor).getTreeLayerAccessor();
        }
        return java.util.Optional.empty();
    }

    public RobotSuiteFile provideSuiteModel() {
        if (suiteModel != null) {
            return suiteModel;
        }
        if (getEditorInput() instanceof FileEditorInput) {
            suiteModel = RedPlugin.getModelManager().createSuiteFile(((FileEditorInput) getEditorInput()).getFile());
            checkRuntimeEnvironment(suiteModel);
        } else {
            final IStorage storage = getEditorInput().getAdapter(IStorage.class);
            try {
                suiteModel = new RobotSuiteStreamFile(storage.getName(), storage.getContents(), storage.isReadOnly());
            } catch (final CoreException e) {
                throw new IllegalRobotEditorInputException("Unable to provide model for given input", e);
            }
        }

        return suiteModel;
    }

    public SuiteSourceEditor getSourceEditor() {
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart editorPart = getEditor(i);
            if (editorPart instanceof SuiteSourceEditor) {
                return (SuiteSourceEditor) editorPart;
            }
        }
        return null;
    }

    @Override
    protected void pageChange(final int newPageIndex) {
        if (newPageIndex != getCurrentPage() && getActiveEditor() instanceof ISectionEditorPart) {
            ((ISectionEditorPart) getActiveEditor()).aboutToChangeToOtherPage();
        }

        super.pageChange(newPageIndex);

        updateActivePage();
        final IEditorPart activeEditor = getActiveEditor();
        final String activePageId = activeEditor instanceof ISectionEditorPart
                ? ((ISectionEditorPart) activeEditor).getId() : "";
        RobotFormEditorActivePageSaver.saveActivePageId(getEditorInput(), activePageId);
    }

    private void updateActivePage() {
        if (getActiveEditor() instanceof ISectionEditorPart) {

            final ISectionEditorPart page = (ISectionEditorPart) getActiveEditor();
            page.updateOnActivation();

            if (isDirty()) {
                SwtThread.asyncExec(new Runnable() {

                    // there are some locking threads involved which results in blocking
                    // main thread for hundreds of milliseconds thus giving stops when switching
                    // from source part to some section editor part
                    @Override
                    public void run() {
                        getSourceEditor().disableReconcilation();
                    }
                });
            } else {
                getSourceEditor().disableReconcilation();
            }

        } else if (getActiveEditor() instanceof SuiteSourceEditor) {
            getSourceEditor().enableReconcilation();

            updateSourceFromModel();
        }
    }

    private void updateSourceFromModel() {
        waitForPendingEditorJobs();
        final SuiteSourceEditor editor = getSourceEditor();

        if (!getDirtyEditors().isEmpty()) {
            final IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
            final RobotFile model = provideSuiteModel().getLinkedElement();
            final RobotFileOutput currentRobotOutputFile = model.getParent();
            final String separatorFromPreference = RedPlugin.getDefault()
                    .getPreferences()
                    .getSeparatorToUse(currentRobotOutputFile.getFileFormat() == FileFormat.TSV);
            final DumpContext ctx = new DumpContext();
            ctx.setPreferedSeparator(separatorFromPreference);
            ctx.setDirtyFlag(true);

            final RobotFileDumper dumper = new RobotFileDumper();
            dumper.setContext(ctx);
            final String content;
            if (RedSystemProperties.shouldUseOldReparsedLinkMode()) {
                content = dumper.dump(currentRobotOutputFile);
                RobotFileOutput alreadyDumpedContent = suiteModel.getProject()
                        .getRobotParser()
                        .parseEditorContent(content, currentRobotOutputFile.getProcessedFile());
                new TwoModelReferencesLinker().update(currentRobotOutputFile, alreadyDumpedContent);
                alreadyDumpedContent = null;
            } else {
                final DumpedResult dumpResult = dumper.dumpToResultObject(currentRobotOutputFile);
                content = dumpResult.newContent();
                new QuickTokenListenerBaseTwoModelReferencesLinker().update(currentRobotOutputFile, dumpResult);
            }

            document.set(content);
        }
    }

    public SuiteSourceEditor activateSourcePage() {
        if (getActiveEditor() instanceof SuiteSourceEditor) {
            return (SuiteSourceEditor) getActiveEditor();
        }
        final SuiteSourceEditor editor = getSourceEditor();
        setActiveEditor(editor);
        return editor;
    }

    public void activateFirstPage() {
        setActivePage(0);
    }

    public ISectionEditorPart activatePage(final RobotSuiteFileSection section) {
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart part = (IEditorPart) pages.get(i);
            if (part instanceof ISectionEditorPart && ((ISectionEditorPart) part).isPartFor(section)) {
                setActivePage(i);
                return (ISectionEditorPart) part;
            }
        }
        return null;
    }

    public <T> T getPage(final Class<T> pageClass) {
        for (int i = 0; i < getPageCount(); i++) {
            final IEditorPart part = (IEditorPart) pages.get(i);
            if (part.getClass().equals(pageClass)) {
                return pageClass.cast(part);
            }
        }
        return null;
    }

    private void checkRuntimeEnvironment(final RobotSuiteFile suiteFile) {
        if (suiteFile != null) {
            final RobotProject robotProject = suiteFile.getProject();
            if (robotProject != null) {
                final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
                if (runtimeEnvironment == null || !runtimeEnvironment.isValidPythonInstallation()
                        || !runtimeEnvironment.hasRobotInstalled()) {
                    final Shell shell = getSite().getShell();
                    if (shell != null && shell.isVisible()) {
                        new ErrorDialogWithLinkToPreferences(shell, "Runtime Environment Error",
                                "Unable to provide valid RED runtime environment. Check python/robot installation and set it in Preferences.",
                                InstalledRobotsPreferencesPage.ID, "Installed Robot Frameworks").open();
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
        if (adapter == IContentOutlinePage.class) {
            return new RobotOutlinePage(this, suiteModel);
        } else if (adapter == IToggleBreakpointsTarget.class) {
            return new ToggleBreakpointTarget();
        }
        return super.getAdapter(adapter);
    }

    @Inject
    @Optional
    private void closeEditorWhenResourceBecomesNotAvailable(
            @UIEventTopic(RobotModelEvents.EXTERNAL_MODEL_CHANGE) final RobotElementChange change) {
        if (change.getKind() == Kind.REMOVED && getEditorInput() instanceof FileEditorInput) {
            final IFile file = ((FileEditorInput) getEditorInput()).getFile();
            final RobotElement element = change.getElement();

            if (element instanceof RobotProject && ((RobotProject) element).getProject().equals(file.getProject())) {
                close(true);
            } else if (element instanceof RobotFolder
                    && ((RobotFolder) element).getFolder().getLocation().isPrefixOf(file.getLocation())) {
                close(true);
            } else if (element instanceof RobotSuiteFile && ((RobotSuiteFile) element).getFile().equals(file)) {
                close(true);
            }
        }
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (suiteModel == file) {
            updateActivePage();
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (suiteModel == file) {
            updateActivePage();
        }
    }

    private static class IllegalRobotEditorInputException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public IllegalRobotEditorInputException(final String message) {
            super(message);
        }

        public IllegalRobotEditorInputException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    public static class RobotEditorOpeningException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public RobotEditorOpeningException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
