/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.Rules.createCommentRule;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.Rules.createDefinitionRule;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.Rules.createKeywordCallRule;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.Rules.createKeywordUsageInSettings;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.Rules.createLocalSettingRule;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.Rules.createSectionHeaderRule;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.Rules.createVariableRule;

import java.util.List;

import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlinkPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CombinedAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CycledContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CycledContentAssistProcessor.AssitantCallbacks;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.LibrariesImportAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.ResourcesImportAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SectionsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SettingsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.VariablesAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.VariablesImportAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks.HyperlinkToKeywordsDetector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.hyperlinks.HyperlinkToVariablesDetector;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.DefaultContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.KeywordsContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.TestCasesSectionContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.TextEditorContentAssist;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.VariablesSectionContentAssistProcessor;
import org.robotframework.red.graphics.ColorsManager;

class SuiteSourceEditorConfiguration extends SourceViewerConfiguration {
    
    private final SuiteSourceEditor editor;

    public SuiteSourceEditorConfiguration(final SuiteSourceEditor editor) {
        this.editor = editor;
    }

    @Override
    public IAnnotationHover getAnnotationHover(final ISourceViewer sourceViewer) {
        return new DefaultAnnotationHover();
    }

    @Override
    public ITextHover getTextHover(final ISourceViewer sourceViewer, final String contentType) {
        return new SuiteSourceHoverSupport(editor.getFileModel());
    }

    @Override
    public IHyperlinkPresenter getHyperlinkPresenter(final ISourceViewer sourceViewer) {
        return super.getHyperlinkPresenter(sourceViewer);
    }

    @Override
    public IHyperlinkDetector[] getHyperlinkDetectors(final ISourceViewer sourceViewer) {
        final RobotSuiteFile model = editor.getFileModel();
        return new IHyperlinkDetector[] { new HyperlinkToVariablesDetector(model),
                new HyperlinkToKeywordsDetector(model) };
    }

    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(final ISourceViewer sourceViewer, final String contentType) {
        return new IAutoEditStrategy[] { new SuiteSourceIndentLineEditStrategy() };
    }

    @Override
    public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer) {
        final ContentAssistant contentAssistant = new ContentAssistant();
        contentAssistant.enableColoredLabels(true);
        contentAssistant.enableAutoInsert(false);
        contentAssistant.enableAutoActivation(true);
        contentAssistant.setEmptyMessage("No proposals");
        contentAssistant.setShowEmptyList(true);
        contentAssistant.setStatusLineVisible(true);
        contentAssistant.setRepeatedInvocationMode(true);
        contentAssistant
                .setRepeatedInvocationTrigger(KeySequence.getInstance(KeyStroke.getInstance(SWT.CTRL, SWT.SPACE)));

        setupOldAssistantProcessors(contentAssistant);
        setupNewAssistantProcessors(contentAssistant);

        contentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
        contentAssistant.setInformationControlCreator(new AbstractReusableInformationControlCreator() {

            @Override
            protected IInformationControl doCreateInformationControl(final Shell parent) {
                return new DefaultInformationControl(parent, true);
            }
        });
        return contentAssistant;
    }

    private void setupOldAssistantProcessors(final ContentAssistant contentAssistant) {
        final TextEditorContentAssist textEditorContentAssist = new SuiteSourceEditorContentAssist(
                editor.getFileModel());
        contentAssistant.setContentAssistProcessor(new TestCasesSectionContentAssistProcessor(textEditorContentAssist),
                SuiteSourcePartitionScanner.TEST_CASES_SECTION);
        contentAssistant.setContentAssistProcessor(new KeywordsContentAssistProcessor(textEditorContentAssist),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);
        contentAssistant.setContentAssistProcessor(new VariablesSectionContentAssistProcessor(textEditorContentAssist),
                SuiteSourcePartitionScanner.VARIABLES_SECTION);
        contentAssistant.setContentAssistProcessor(new DefaultContentAssistProcessor(), IDocument.DEFAULT_CONTENT_TYPE);
    }

    private void setupNewAssistantProcessors(final ContentAssistant contentAssistant) {
        final AssitantCallbacks assistantAccessor = new AssitantCallbacks() {

            @Override
            public void setStatus(final String title) {
                contentAssistant.setStatusMessage(String.format("Press Ctrl+Space to show %s proposals", title));
            }

            @Override
            public void openCompletionProposals() {
                contentAssistant.showPossibleCompletions();
            }
        };
        createSettingsAssist(contentAssistant, assistantAccessor);
    }

    private void createSettingsAssist(final ContentAssistant contentAssistant,
            final AssitantCallbacks assistantAccessor) {
        final SuiteSourceEditorContentAssist assist = new SuiteSourceEditorContentAssist(editor.getFileModel());

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistantAccessor);

        final SectionsAssistProcessor sectionsAssistProcessor = new SectionsAssistProcessor();
        final SettingsAssistProcessor settingNamesProcessor = new SettingsAssistProcessor();
        final LibrariesImportAssistProcessor librariesProcessor = new LibrariesImportAssistProcessor(assist);
        final VariablesImportAssistProcessor variableImportsProcessor = new VariablesImportAssistProcessor(assist);
        final ResourcesImportAssistProcessor resourceImportsProcessor = new ResourcesImportAssistProcessor(assist);
        final VariablesAssistProcessor variablesAssistProcessor = new VariablesAssistProcessor(assist);

        final CombinedAssistProcessor combinedProcessor = new CombinedAssistProcessor(librariesProcessor,
                variableImportsProcessor, resourceImportsProcessor, sectionsAssistProcessor, settingNamesProcessor,
                variablesAssistProcessor);
        cycledProcessor.addProcessor(combinedProcessor);
        cycledProcessor.addProcessor(variablesAssistProcessor);
        contentAssistant.setContentAssistProcessor(cycledProcessor, SuiteSourcePartitionScanner.SETTINGS_SECTION);
        contentAssistant.addCompletionListener(cycledProcessor);
    }

    @Override
    public IQuickAssistAssistant getQuickAssistAssistant(final ISourceViewer sourceViewer) {
        final IQuickAssistAssistant assistant = new QuickAssistAssistant();
        assistant.setQuickAssistProcessor(new SuiteSourceQuickAssistProcessor(editor.getFileModel()));
        assistant.setInformationControlCreator(new AbstractReusableInformationControlCreator() {

            @Override
            protected IInformationControl doCreateInformationControl(final Shell parent) {
                return new DefaultInformationControl(parent, true);
            }
        });
        return assistant;
    }

    @Override
    public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer) {
        final List<String> legal = newArrayList(SuiteSourcePartitionScanner.LEGAL_CONTENT_TYPES);
        legal.add(0, IDocument.DEFAULT_CONTENT_TYPE);
        return legal.toArray(new String[0]);
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(final ISourceViewer sourceViewer) {
        final PresentationReconciler reconciler = new PresentationReconciler();

        final IToken section = new Token(new TextAttribute(ColorsManager.getColor(255, 0, 0)));
        final IToken comment = new Token(new TextAttribute(ColorsManager.getColor(192, 192, 192)));
        final IToken definition = new Token(new TextAttribute(ColorsManager.getColor(0, 0, 0), null, SWT.BOLD));
        final IToken variable = new Token(new TextAttribute(ColorsManager.getColor(0, 128, 0)));
        final IToken call = new Token(new TextAttribute(ColorsManager.getColor(0, 128, 192), null, SWT.BOLD));
        final IToken setting = new Token(new TextAttribute(ColorsManager.getColor(149, 0, 85)));
        
        final IRule[] defaultRules = new IRule[] {};
        createDamageRepairer(reconciler, IDocument.DEFAULT_CONTENT_TYPE, defaultRules);

        final IRule[] testCasesRules = new IRule[] { createVariableRule(variable), createSectionHeaderRule(section),
                createDefinitionRule(definition), createLocalSettingRule(setting), createKeywordCallRule(call),
                createCommentRule(comment) };
        createDamageRepairer(reconciler, SuiteSourcePartitionScanner.TEST_CASES_SECTION, testCasesRules);

        final IRule[] keywordsRules = new IRule[] { createVariableRule(variable), createSectionHeaderRule(section),
                createDefinitionRule(definition), createLocalSettingRule(setting), createKeywordCallRule(call),
                createCommentRule(comment) };
        createDamageRepairer(reconciler, SuiteSourcePartitionScanner.KEYWORDS_SECTION, keywordsRules);

        final IRule[] settingsRules = new IRule[] { createVariableRule(variable), createSectionHeaderRule(section),
                createDefinitionRule(setting), createKeywordUsageInSettings(call), createCommentRule(comment) };
        createDamageRepairer(reconciler, SuiteSourcePartitionScanner.SETTINGS_SECTION, settingsRules);

        final IRule[] variablesRules = new IRule[] { createVariableRule(variable), createSectionHeaderRule(section),
                createDefinitionRule(variable), createCommentRule(comment) };
        createDamageRepairer(reconciler, SuiteSourcePartitionScanner.VARIABLES_SECTION, variablesRules);

        return reconciler;
    }

    private static void createDamageRepairer(final PresentationReconciler reconciler, final String contentType,
            final IRule[] rules) {
        final DefaultDamagerRepairer damagerRepairer = new DefaultDamagerRepairer(new SingleTokenScanner(rules));
        reconciler.setDamager(damagerRepairer, contentType);
        reconciler.setRepairer(damagerRepairer, contentType);
    }

    @Override
    public IReconciler getReconciler(final ISourceViewer sourceViewer) {
        return new MonoReconciler(getReconcilingStrategy(), true);
    }

    private IReconcilingStrategy getReconcilingStrategy() {
        return new SuiteSourceReconcilingStrategy(editor);
    }

    @Override
    public IContentFormatter getContentFormatter(final ISourceViewer sourceViewer) {
        final MultiPassContentFormatter formatter = new MultiPassContentFormatter(
                getConfiguredDocumentPartitioning(sourceViewer), IDocument.DEFAULT_CONTENT_TYPE);
        formatter.setMasterStrategy(new SuiteSourceFormattingStrategy());
        return formatter;
    }

    private static class SingleTokenScanner extends BufferedRuleBasedScanner {

        public SingleTokenScanner(final IRule[] rules) {
            setRules(rules);
        }
    };
}
