/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.debug.RedDebuggerAssistantEditorWrapper.RedDebuggerAssistantEditorControls;
import org.robotframework.ide.eclipse.main.plugin.debug.RedDebuggerAssistantEditorWrapper.RedDebuggerAssistantEditorInput;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordDefinitionLocator.KeywordDetector;
import org.robotframework.ide.eclipse.main.plugin.preferences.DebuggerPreferencePage;
import org.robotframework.ide.eclipse.main.plugin.project.library.SourceOpeningSupport;
import org.robotframework.red.graphics.ColorsManager;
import org.robotframework.red.graphics.ImagesManager;

class SourceInLibraryEditorControls implements RedDebuggerAssistantEditorControls {

    private final IWorkbenchPage page;

    private Composite innerParent;
    private CLabel titleLabel;

    private Optional<URI> uri;
    private String keywordName;

    private ScrolledFormText formText;

    public SourceInLibraryEditorControls(final IWorkbenchPage page) {
        this.page = page;
    }

    @Override
    public void construct(final Composite parent) {
        final FillLayout parentLayout = (FillLayout) parent.getLayout();
        parentLayout.marginHeight = 0;
        parentLayout.marginWidth = 0;

        innerParent = new Composite(parent, SWT.NONE);
        innerParent.setBackground(ColorsManager.getColor(SWT.COLOR_WHITE));
        GridLayoutFactory.fillDefaults().applyTo(innerParent);

        titleLabel = new CLabel(innerParent, SWT.NONE);
        titleLabel.setFont(JFaceResources.getBannerFont());
        titleLabel.setBackground((Color) null);
        titleLabel.setForeground(ColorsManager.getColor(SWT.COLOR_DARK_GRAY));
        titleLabel.addPaintListener(e -> {
            final int oldThickness = e.gc.getLineWidth();
            try {
                e.gc.setLineWidth(2);
                e.gc.drawLine(0, e.y + e.height - 1, e.x + e.width, e.y + e.height - 1);
            } finally {
                e.gc.setLineWidth(oldThickness);
            }

        });
        GridDataFactory.fillDefaults().grab(true, false).applyTo(titleLabel);

        formText = new ScrolledFormText(innerParent, SWT.V_SCROLL | SWT.H_SCROLL, true);
        formText.setBackground(innerParent.getBackground());
        formText.setFont(JFaceResources.getTextFont());
        formText.getFormText().setImage("source", ImagesManager.getImage(RedImages.getSourceImage()));
        GridDataFactory.fillDefaults().indent(10, 0).grab(true, true).applyTo(formText);

        formText.getFormText().addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(final HyperlinkEvent e) {
                if (e.data.equals("source")) {
                    openKeywordSource();
                } else if (e.data.equals("preferences")) {
                    PreferencesUtil.createPreferenceDialogOn(formText.getShell(), DebuggerPreferencePage.ID, null,
                            null).open();
                }
            }
        });
    }

    private void openKeywordSource() {
        final RedWorkspace workspace = new RedWorkspace(ResourcesPlugin.getWorkspace().getRoot());
        final Optional<IFile> file = uri.flatMap(u -> workspace.fileForUri(u));
        if (!file.isPresent()) {
            final String message = "Unable to open editor for keyword:\n" + keywordName;
            final Status status = new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message);
            StatusManager.getManager().handle(status, StatusManager.SHOW);
            return;
        }
        final RobotModel model = RedPlugin.getModelManager().getModel();
        new KeywordDefinitionLocator(file.get(), model).locateKeywordDefinition(new KeywordDetector() {

            @Override
            public ContinueDecision accessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final Collection<Optional<String>> libraryAliases,
                    final RobotSuiteFile exposingFile) {

                if (QualifiedKeywordName.create(kwSpec.getName(), libSpec.getName())
                        .matchesIgnoringCase(QualifiedKeywordName.fromOccurrence(keywordName))) {

                    SourceOpeningSupport.open(page, model, file.get().getProject(), libSpec, kwSpec);
                    return ContinueDecision.STOP;
                }
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision nonAccessibleLibraryKeywordDetected(final LibrarySpecification libSpec,
                    final KeywordSpecification kwSpec, final RobotSuiteFile exposingFile) {
                return ContinueDecision.CONTINUE;
            };

            @Override
            public ContinueDecision keywordDetected(final RobotSuiteFile file, final RobotKeywordDefinition keyword) {
                return ContinueDecision.CONTINUE;
            }
        });
    }

    @Override
    public void setFocus() {
        formText.setFocus();
    }

    @Override
    public void setInput(final RedDebuggerAssistantEditorInput input) {
        titleLabel.setImage(ImagesManager.getImage(input.getTitleImageDescriptor()));
        titleLabel.setText(input.getTitle());

        formText.getFormText().setText(input.getDetailedInformation(), true, false);

        keywordName = ((SourceInLibraryEditorInput) input).getKeywordName();
        uri = ((SourceInLibraryEditorInput) input).getFileUri();
    }

    @Override
    public Composite getParent() {
        return innerParent.getParent();
    }

    @Override
    public void dispose() {
        innerParent.dispose();
    }
}
