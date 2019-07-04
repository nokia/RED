/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.lang.reflect.Method;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

@SuppressWarnings("deprecation")
public class EditorTemplatePreferencePage extends TemplatePreferencePage {

    public EditorTemplatePreferencePage() {
        setPreferenceStore(getPreferenceStore());
        setTemplateStore(getTemplateStore());
        setContextTypeRegistry(getContextTypeRegistry());
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return RedPlugin.getDefault().getPreferenceStore();
    }

    @Override
    protected boolean isShowFormatterSetting() {
        return false;
    }

    @Override
    public TemplateStore getTemplateStore() {
        return RedPlugin.getDefault().getPreferences().getTemplateStore();
    }

    @Override
    public ContributionContextTypeRegistry getContextTypeRegistry() {
        return RedPlugin.getDefault().getPreferences().getTemplateContextTypeRegistry();
    }

    @Override
    protected Template editTemplate(final Template template, final boolean edit, final boolean isNameModifiable) {
        final EditRedTemplateDialog dialog = new EditRedTemplateDialog(getShell(), template, edit, isNameModifiable,
                getContextTypeRegistry());
        if (dialog.open() == Window.OK) {
            return dialog.getTemplate();
        }
        return null;
    }
    
    private static class EditRedTemplateDialog extends EditTemplateDialog {

        private final EditorTemplateVariableProcessor processor;

        public EditRedTemplateDialog(final Shell parent, final Template template, final boolean edit,
                final boolean isNameModifiable, final ContextTypeRegistry registry) {
            super(parent, template, edit, isNameModifiable, registry);
            this.processor = new EditorTemplateVariableProcessor(() -> getContextType(getTemplateProcessor()));
        }

        private TemplateContextType getContextType(final IContentAssistProcessor originalAssistant) {
            // This is ugly but overall it seems to be the simplest way to workaround closed eclipse
            // APIs in order to properly show template variable proposals without copying whole
            // possible code for template editing
            try {
                final Method method = originalAssistant.getClass().getMethod("getContextType");
                method.setAccessible(true);
                return (TemplateContextType) method.invoke(originalAssistant);
            } catch (final Exception e) {
                return null;
            }
        }

        @Override
        protected SourceViewer createViewer(final Composite parent) {
            final SourceViewer viewer = new SourceViewer(parent, null, null, false,
                    SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
            final SourceViewerConfiguration configuration = new SourceViewerConfiguration() {

                @Override
                public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer) {
                    final ContentAssistant assistant = new ContentAssistant();
                    assistant.enableAutoActivation(true);
                    assistant.enableAutoInsert(true);
                    assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
                    assistant.setInformationControlCreator(new AbstractReusableInformationControlCreator() {

                        @Override
                        protected IInformationControl doCreateInformationControl(final Shell parent) {
                            return new DefaultInformationControl(parent, true);
                        }
                    });
                    return assistant;
                }
            };
            viewer.configure(configuration);
            return viewer;
        }
    }
}
