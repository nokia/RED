/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.DefinitionPosition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourceEditor;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.swt.SwtThread;

/**
 * @author mmarzec
 */
public class DocumentationView {

    public static final String ID = "org.robotframework.ide.DocumentationView";

    public static final String REFRESH_DOC_EVENT_TOPIC = "DocumentationView/Refresh";

    private StyledText styledText;

    private DocumentationViewPartListener documentationViewPartListener;

    private CurrentlyDisplayedDocElement currentlyDisplayedDocElement;

    private String labelForWhichDocIsDisplayed;

    private final AtomicBoolean hasShowLibdocEnabled = new AtomicBoolean();

    private ShowLibdocAction showLibdocAction;

    @PostConstruct
    public void postConstruct(final Composite parent, final IViewPart part) {
        parent.setLayout(new FillLayout());

        styledText = new StyledText(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        styledText.setMargins(5, 5, 5, 5);
        styledText.setBackground(ColorsManager.getColor(SWT.COLOR_INFO_BACKGROUND));
        styledText.setFont(JFaceResources.getDefaultFont());
        styledText.setEditable(false);

        createToolbarActions(part.getViewSite().getActionBars().getToolBarManager());

        documentationViewPartListener = new DocumentationViewPartListener(this);
        PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow()
                .getActivePage()
                .addPartListener(documentationViewPartListener);
    }

    @Focus
    public void onFocus() {
        styledText.setFocus();
    }

    @PreDestroy
    public void dispose() {
        if (documentationViewPartListener != null) {
            documentationViewPartListener.dispose();
            PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow()
                    .getActivePage()
                    .removePartListener(documentationViewPartListener);
        }
    }

    public void showDocumentation(final RobotFileInternalElement element) {
        if (element == null) {
            clearView();
            return;
        }

        initCurrentlyDisplayedDocElement();

        if (!currentlyDisplayedDocElement.isEqualTo(element)) {
            currentlyDisplayedDocElement.setRobotFileInternalElement(element);
            showDocumentationText();
        }
    }

    public void showDocumentation(final IDocumentationHolder documentationHolder, final RobotSuiteFile suiteFile) {
        if (documentationHolder == null) {
            clearView();
            return;
        }

        initCurrentlyDisplayedDocElement();

        if (!currentlyDisplayedDocElement.isEqualTo(documentationHolder)) {
            currentlyDisplayedDocElement.setDocumentationHolder(documentationHolder, suiteFile);
            showDocumentationText();
        }
    }

    private void showDocumentationText() {
        if (currentlyDisplayedDocElement != null && currentlyDisplayedDocElement.getDocumentationHolder() != null) {
            final String documentationText = DocumentationServiceHandler
                    .toShowConsolidated(currentlyDisplayedDocElement.getDocumentationHolder());
            final String parentName = currentlyDisplayedDocElement.getParentName();
            final String fileName = currentlyDisplayedDocElement.getSuiteFileName();

            SwtThread.asyncExec(new DocTextSetter(documentationText, parentName, fileName));
        }
    }

    public void showLibdoc(final RobotFileInternalElement element, final String label) {
        if (element == null || label == null) {
            clearView();
            return;
        }

        initCurrentlyDisplayedDocElement();

        if (!currentlyDisplayedDocElement.isEqualTo(element) || !label.equals(labelForWhichDocIsDisplayed)) {
            currentlyDisplayedDocElement.setRobotFileInternalElement(element);
            labelForWhichDocIsDisplayed = label;
            showLibdocText(label, element.getSuiteFile());
        }
    }

    public void showLibdoc(final String elementName, final RobotSuiteFile suiteFile) {
        if (elementName == null) {
            clearView();
            return;
        }
        resetCurrentlyDisplayedElement();
        showLibdocText(elementName, suiteFile);
    }

    private void showLibdocText(final String elementName, final RobotSuiteFile suiteFile) {
        final Optional<RedKeywordProposal> bestMatch = new RedKeywordProposals(suiteFile)
                .getBestMatchingKeywordProposal(elementName);
        bestMatch.ifPresent(proposal -> SwtThread.asyncExec(new DocTextSetter(proposal.getDescription(), "", "")));
    }

    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    private void refreshEvent(@UIEventTopic(REFRESH_DOC_EVENT_TOPIC) final RobotFileInternalElement element) {
        resetCurrentlyDisplayedElement();
        showDocumentation(element);
    }

    private void initCurrentlyDisplayedDocElement() {
        if (currentlyDisplayedDocElement == null) {
            currentlyDisplayedDocElement = new CurrentlyDisplayedDocElement();
        }
    }

    private void clearView() {
        resetCurrentlyDisplayedElement();
        SwtThread.asyncExec(new DocTextSetter());
    }

    public void resetCurrentlyDisplayedElement() {
        if (currentlyDisplayedDocElement != null) {
            currentlyDisplayedDocElement.reset();
        }
        labelForWhichDocIsDisplayed = null;
    }

    public boolean hasShowLibdocEnabled() {
        return hasShowLibdocEnabled.get();
    }

    public void setShowLibdocEnabled() {
        hasShowLibdocEnabled.set(true);
        showLibdocAction.setChecked(true);
    }

    private void createToolbarActions(final IToolBarManager toolBarManager) {
        final ShowInSourceAction showInSourceAction = new ShowInSourceAction();
        showInSourceAction.setText("Show In Source");
        showInSourceAction.setImageDescriptor(RedImages.getGoToImage());
        toolBarManager.add(showInSourceAction);
        final ToggleWordWrapAction toggleWordWrapAction = new ToggleWordWrapAction();
        toggleWordWrapAction.setChecked(false);
        toggleWordWrapAction.setText("Word Wrap");
        toggleWordWrapAction.setImageDescriptor(RedImages.getWordwrapImage());
        toolBarManager.add(toggleWordWrapAction);
        showLibdocAction = new ShowLibdocAction();
        showLibdocAction.setChecked(false);
        showLibdocAction.setText("Show Libdoc");
        showLibdocAction.setImageDescriptor(RedImages.getBookImage());
        toolBarManager.add(showLibdocAction);
    }

    class DocTextSetter implements Runnable {

        private final String documentationText;

        private final String documentationSettingParentName;

        private final String fileName;

        public DocTextSetter() {
            this("", "", "");
        }

        public DocTextSetter(final String documentationText, final String documentationSettingParentName,
                final String fileName) {
            this.documentationText = documentationText;
            this.documentationSettingParentName = documentationSettingParentName;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            if (!styledText.isDisposed()) {
                styledText.setText("");

                if (!documentationSettingParentName.isEmpty() && !fileName.isEmpty()) {
                    styledText.append(documentationSettingParentName + "\n");
                    styledText.append(fileName + "\n\n");

                    styledText.setStyleRange(
                            new StyleRange(0, documentationSettingParentName.length(), null, null, SWT.BOLD));
                    styledText.setStyleRange(new StyleRange(documentationSettingParentName.length() + 1,
                            fileName.length(), null, null, SWT.ITALIC));
                }
                styledText.append(documentationText);
            }
        }

    }

    class CurrentlyDisplayedDocElement {

        private Optional<RobotFileInternalElement> robotFileInternalElement = Optional.empty();

        private Optional<IDocumentationHolder> documentationHolder = Optional.empty();

        private String suiteFileName = "";

        public void setRobotFileInternalElement(final RobotFileInternalElement robotFileInternalElement) {
            this.robotFileInternalElement = Optional.of(robotFileInternalElement);
            this.suiteFileName = robotFileInternalElement.getSuiteFile().getName();
            this.documentationHolder = Optional.empty();
        }

        public void setDocumentationHolder(final IDocumentationHolder documentationHolder,
                final RobotSuiteFile suiteFile) {
            this.documentationHolder = Optional.of(documentationHolder);
            this.suiteFileName = suiteFile.getName();
            this.robotFileInternalElement = Optional.empty();
        }

        public IDocumentationHolder getDocumentationHolder() {
            if (robotFileInternalElement.isPresent()) {
                final Object linkedElement = robotFileInternalElement.get().getLinkedElement();
                if (linkedElement != null && linkedElement instanceof IDocumentationHolder) {
                    return (IDocumentationHolder) linkedElement;
                }
            } else if (documentationHolder.isPresent()) {
                return documentationHolder.get();
            }

            return null;
        }

        public String getParentName() {
            if (robotFileInternalElement.isPresent()) {
                final RobotElement parent = robotFileInternalElement.get().getParent();
                if (parent != null) {
                    return parent.getName();
                }
            } else if (documentationHolder.isPresent()) {
                final AModelElement<?> modelElement = (AModelElement<?>) documentationHolder.get();
                if (modelElement.getParent() instanceof IExecutableStepsHolder) {
                    return ((IExecutableStepsHolder<?>) modelElement.getParent()).getName().getText();
                }
            }
            return "";
        }

        public String getSuiteFileName() {
            return suiteFileName;
        }

        public Optional<DefinitionPosition> getDefinitionPosition() {
            if (robotFileInternalElement.isPresent()) {
                return Optional.of(robotFileInternalElement.get().getDefinitionPosition());
            }
            return Optional.empty();
        }

        public boolean isEqualTo(final RobotFileInternalElement other) {
            if (robotFileInternalElement.isPresent()) {
                return robotFileInternalElement.get() == other;
            }
            return false;
        }

        public boolean isEqualTo(final IDocumentationHolder other) {
            if (documentationHolder.isPresent()) {
                return documentationHolder.get() == other;
            }
            return false;
        }

        public void reset() {
            this.robotFileInternalElement = Optional.empty();
            this.documentationHolder = Optional.empty();
        }
    }

    class ShowInSourceAction extends Action implements IWorkbenchAction {

        private static final String ID = "org.robotframework.action.documentationView.ShowInSourceAction";

        public ShowInSourceAction() {
            setId(ID);
        }

        @Override
        public void run() {
            if (currentlyDisplayedDocElement != null) {
                final IEditorPart activeEditor = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow()
                        .getActivePage()
                        .getActiveEditor();
                if (activeEditor instanceof RobotFormEditor) {
                    final RobotFormEditor editor = (RobotFormEditor) activeEditor;
                    final SuiteSourceEditor suiteEditor = editor.activateSourcePage();
                    final ISelectionProvider selectionProvider = suiteEditor.getSite().getSelectionProvider();

                    final Optional<DefinitionPosition> position = currentlyDisplayedDocElement.getDefinitionPosition();
                    if (position.isPresent()) {
                        final DefinitionPosition definitionPosition = position.get();
                        selectionProvider.setSelection(
                                new TextSelection(definitionPosition.getOffset(), definitionPosition.getLength()));
                    }
                }
            }
        }

        @Override
        public void dispose() {
        }
    }

    class ToggleWordWrapAction extends Action implements IWorkbenchAction {

        private static final String ID = "org.robotframework.action.documentationView.ToggleWordWrapAction";

        public ToggleWordWrapAction() {
            setId(ID);
        }

        @Override
        public void run() {
            styledText.setWordWrap(!styledText.getWordWrap());
        }

        @Override
        public void dispose() {
        }
    }

    class ShowLibdocAction extends Action implements IWorkbenchAction {

        private static final String ID = "org.robotframework.action.documentationView.ShowLibdocAction";

        public ShowLibdocAction() {
            setId(ID);
        }

        @Override
        public void run() {
            hasShowLibdocEnabled.set(!hasShowLibdocEnabled.get());
        }

        @Override
        public void dispose() {
        }
    }
}
