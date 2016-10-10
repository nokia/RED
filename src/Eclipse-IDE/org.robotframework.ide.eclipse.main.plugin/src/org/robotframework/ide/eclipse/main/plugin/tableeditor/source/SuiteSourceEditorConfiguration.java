/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;

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
import org.eclipse.jface.text.hyperlink.MultipleHyperlinkPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.quickassist.QuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences.ColoringPreference;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.SourceHyperlinksToFilesDetector;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.SourceHyperlinksToKeywordsDetector;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.SourceHyperlinksToVariablesDetector;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CombinedAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CycledContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CycledContentAssistProcessor.AssitantCallbacks;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.ForLoopAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.GeneralSettingsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.KeywordCallsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.KeywordsInSettingsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.LibrariesImportAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.ResourcesImportAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SectionsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SettingsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SuiteSourceAssistantContext;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.VariablesAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.VariablesDefinitionsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.VariablesImportAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.CaseNameRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.CommentRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ExecutableRowCallRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.KeywordNameRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.KeywordSettingsCallRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.KeywordSettingsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.MatchEverythingRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.RedCachingScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.RedTokenScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.RedTokensStore;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SectionHeaderRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SettingRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SettingsCallRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TestCaseSettingsCallRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TestCaseSettingsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.VariableDefinitionRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.VariableUsageRule;
import org.robotframework.red.graphics.ColorsManager;

class SuiteSourceEditorConfiguration extends SourceViewerConfiguration {

    private final SuiteSourceEditor editor;

    private IReconciler reconciler;

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
        return new MultipleHyperlinkPresenter(new RGB(0, 0, 255));
    }

    @Override
    public IHyperlinkDetector[] getHyperlinkDetectors(final ISourceViewer sourceViewer) {
        final RobotSuiteFile model = editor.getFileModel();
        return new IHyperlinkDetector[] { new SourceHyperlinksToVariablesDetector(model),
                new SourceHyperlinksToKeywordsDetector(model), new SourceHyperlinksToFilesDetector(model) };
    }

    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(final ISourceViewer sourceViewer, final String contentType) {
        final boolean isTsv = "tsv".equals(editor.fileModel.getFileExtension());
        final List<IAutoEditStrategy> strategies = newArrayList();
        strategies.add(new SuiteSourceIndentLineEditStrategy(isTsv));
        if (contentType.equals(SuiteSourcePartitionScanner.KEYWORDS_SECTION)
                || contentType.equals(SuiteSourcePartitionScanner.TEST_CASES_SECTION)
                || contentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
            strategies.add(new SuiteSourceIndentLineAfterDefinitionStrategy(isTsv));
        }
        return strategies.toArray(new IAutoEditStrategy[0]);
    }

    @Override
    public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer) {
        final ContentAssistant contentAssistant = new ContentAssistant();
        contentAssistant.enableColoredLabels(true);
        contentAssistant.enableAutoInsert(false);
        contentAssistant
                .enableAutoActivation(RedPlugin.getDefault().getPreferences().isAssistantAutoActivationEnabled());
        contentAssistant
                .setAutoActivationDelay(RedPlugin.getDefault().getPreferences().getAssistantAutoActivationDelay());
        contentAssistant.setEmptyMessage("No proposals");
        contentAssistant.setShowEmptyList(true);
        contentAssistant.setStatusLineVisible(true);
        contentAssistant.setRepeatedInvocationMode(true);
        contentAssistant
                .setRepeatedInvocationTrigger(KeySequence.getInstance(KeyStroke.getInstance(SWT.CTRL, SWT.SPACE)));

        setupAssistantProcessors(contentAssistant);

        contentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
        contentAssistant.setInformationControlCreator(new AbstractReusableInformationControlCreator() {

            @Override
            protected IInformationControl doCreateInformationControl(final Shell parent) {
                return new DefaultInformationControl(parent, true);
            }
        });
        return contentAssistant;
    }

    private void setupAssistantProcessors(final ContentAssistant contentAssistant) {
        final AssitantCallbacks assistantAccessor = new AssitantCallbacks() {

            @Override
            public void setStatus(final String title) {
                contentAssistant.setStatusMessage(title);
            }

            @Override
            public void openCompletionProposals() {
                contentAssistant.showPossibleCompletions();
            }
        };
        createSettingsAssist(contentAssistant, assistantAccessor);
        createVariablesAssist(contentAssistant, assistantAccessor);
        createKeywordsAssist(contentAssistant, assistantAccessor);
        createTestCasesAssist(contentAssistant, assistantAccessor);
        createDefaultAssist(contentAssistant, assistantAccessor);
    }

    private void createSettingsAssist(final ContentAssistant contentAssistant,
            final AssitantCallbacks assistantAccessor) {
        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(editor.getFileModel());

        final SectionsAssistProcessor sectionsAssistProcessor = new SectionsAssistProcessor(assistContext);
        final GeneralSettingsAssistProcessor settingNamesProcessor = new GeneralSettingsAssistProcessor(assistContext);
        final LibrariesImportAssistProcessor libraryImportsProcessor = new LibrariesImportAssistProcessor(assistContext);
        final VariablesImportAssistProcessor variableImportsProcessor = new VariablesImportAssistProcessor(
                assistContext);
        final ResourcesImportAssistProcessor resourceImportsProcessor = new ResourcesImportAssistProcessor(
                assistContext);
        final KeywordsInSettingsAssistProcessor keywordsAssistProcessor = new KeywordsInSettingsAssistProcessor(
                assistContext);
        final VariablesAssistProcessor variablesAssistProcessor = new VariablesAssistProcessor(assistContext);

        final CombinedAssistProcessor combinedProcessor = new CombinedAssistProcessor(libraryImportsProcessor,
                variableImportsProcessor, resourceImportsProcessor, sectionsAssistProcessor, settingNamesProcessor,
                keywordsAssistProcessor, variablesAssistProcessor);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext,
                assistantAccessor);
        cycledProcessor.addProcessor(combinedProcessor);
        cycledProcessor.addProcessor(settingNamesProcessor);
        cycledProcessor.addProcessor(variablesAssistProcessor);

        contentAssistant.setContentAssistProcessor(cycledProcessor, SuiteSourcePartitionScanner.SETTINGS_SECTION);
        contentAssistant.addCompletionListener(cycledProcessor);
    }

    private void createVariablesAssist(final ContentAssistant contentAssistant,
            final AssitantCallbacks assistantAccessor) {
        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(editor.getFileModel());

        final SectionsAssistProcessor sectionsAssistProcessor = new SectionsAssistProcessor(assistContext);
        final VariablesDefinitionsAssistProcessor variableDefsAssistProcessor = new VariablesDefinitionsAssistProcessor(assistContext);
        final VariablesAssistProcessor variablesAssistProcessor = new VariablesAssistProcessor(assistContext);

        final CombinedAssistProcessor combinedProcessor = new CombinedAssistProcessor(variableDefsAssistProcessor,
                sectionsAssistProcessor, variablesAssistProcessor);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext,
                assistantAccessor);
        cycledProcessor.addProcessor(combinedProcessor);
        cycledProcessor.addProcessor(variablesAssistProcessor);

        contentAssistant.setContentAssistProcessor(cycledProcessor, SuiteSourcePartitionScanner.VARIABLES_SECTION);
        contentAssistant.addCompletionListener(cycledProcessor);
    }

    private void createTestCasesAssist(final ContentAssistant contentAssistant,
            final AssitantCallbacks assistantAccessor) {
        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(editor.getFileModel());

        final SectionsAssistProcessor sectionsAssistProcessor = new SectionsAssistProcessor(assistContext);
        final ForLoopAssistProcessor forLoopAssistProcessor = new ForLoopAssistProcessor(assistContext);
        final KeywordCallsAssistProcessor keywordCallsAssistProcessor = new KeywordCallsAssistProcessor(assistContext);
        final SettingsAssistProcessor tcSettingsAssistProcessor = new SettingsAssistProcessor(assistContext);
        final VariablesAssistProcessor variablesAssistProcessor = new VariablesAssistProcessor(assistContext);

        final CombinedAssistProcessor combinedProcessor = new CombinedAssistProcessor(sectionsAssistProcessor,
                tcSettingsAssistProcessor, forLoopAssistProcessor, keywordCallsAssistProcessor,
                variablesAssistProcessor);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext,
                assistantAccessor);
        cycledProcessor.addProcessor(combinedProcessor);
        cycledProcessor.addProcessor(keywordCallsAssistProcessor);
        cycledProcessor.addProcessor(variablesAssistProcessor);

        contentAssistant.setContentAssistProcessor(cycledProcessor, SuiteSourcePartitionScanner.TEST_CASES_SECTION);
        contentAssistant.addCompletionListener(cycledProcessor);
    }

    private void createKeywordsAssist(final ContentAssistant contentAssistant,
            final AssitantCallbacks assistantAccessor) {
        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(editor.getFileModel());

        final SectionsAssistProcessor sectionsAssistProcessor = new SectionsAssistProcessor(assistContext);
        final ForLoopAssistProcessor forLoopAssistProcessor = new ForLoopAssistProcessor(assistContext);
        final KeywordCallsAssistProcessor keywordCallsAssistProcessor = new KeywordCallsAssistProcessor(assistContext);
        final SettingsAssistProcessor kwSettingsAssistProcessor = new SettingsAssistProcessor(assistContext);
        final VariablesAssistProcessor variablesAssistProcessor = new VariablesAssistProcessor(assistContext);

        final CombinedAssistProcessor combinedProcessor = new CombinedAssistProcessor(sectionsAssistProcessor,
                kwSettingsAssistProcessor, forLoopAssistProcessor, keywordCallsAssistProcessor,
                variablesAssistProcessor);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext,
                assistantAccessor);
        cycledProcessor.addProcessor(combinedProcessor);
        cycledProcessor.addProcessor(keywordCallsAssistProcessor);
        cycledProcessor.addProcessor(variablesAssistProcessor);

        contentAssistant.setContentAssistProcessor(cycledProcessor, SuiteSourcePartitionScanner.KEYWORDS_SECTION);
        contentAssistant.addCompletionListener(cycledProcessor);
    }

    private void createDefaultAssist(final ContentAssistant contentAssistant,
            final AssitantCallbacks assistantAccessor) {
        // we are adding all the assistants for default content type. Most of them (excluding
        // section headers assistant) are working in default content type only at the very last
        // position in file (this position always has default content type, but it can be actually
        // prepended with some valid meaningful content type

        final SuiteSourceAssistantContext assistContext = new SuiteSourceAssistantContext(editor.getFileModel());

        final SectionsAssistProcessor sectionsAssistProcessor = new SectionsAssistProcessor(assistContext);
        final GeneralSettingsAssistProcessor generalSettingNamesProcessor = new GeneralSettingsAssistProcessor(
                assistContext);
        final VariablesDefinitionsAssistProcessor variableDefsAssistProcessor = new VariablesDefinitionsAssistProcessor(
                assistContext);
        final LibrariesImportAssistProcessor libraryImportsProcessor = new LibrariesImportAssistProcessor(
                assistContext);
        final VariablesImportAssistProcessor variableImportsProcessor = new VariablesImportAssistProcessor(
                assistContext);
        final ResourcesImportAssistProcessor resourceImportsProcessor = new ResourcesImportAssistProcessor(
                assistContext);
        final KeywordsInSettingsAssistProcessor keywordsAssistProcessor = new KeywordsInSettingsAssistProcessor(
                assistContext);
        final SettingsAssistProcessor settingsAssistProcessor = new SettingsAssistProcessor(assistContext);
        final KeywordCallsAssistProcessor keywordCallsAssistProcessor = new KeywordCallsAssistProcessor(assistContext);
        final VariablesAssistProcessor variablesAssistProcessor = new VariablesAssistProcessor(assistContext);

        final CombinedAssistProcessor combinedProcessor = new CombinedAssistProcessor(variableDefsAssistProcessor,
                sectionsAssistProcessor, generalSettingNamesProcessor, libraryImportsProcessor,
                variableImportsProcessor, resourceImportsProcessor, keywordsAssistProcessor, settingsAssistProcessor,
                keywordCallsAssistProcessor, variablesAssistProcessor);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext,
                assistantAccessor);
        cycledProcessor.addProcessor(combinedProcessor);
        cycledProcessor.addProcessor(generalSettingNamesProcessor);
        cycledProcessor.addProcessor(keywordCallsAssistProcessor);
        cycledProcessor.addProcessor(variablesAssistProcessor);

        contentAssistant.setContentAssistProcessor(cycledProcessor, IDocument.DEFAULT_CONTENT_TYPE);
        contentAssistant.addCompletionListener(cycledProcessor);
    }

    @Override
    public IQuickAssistAssistant getQuickAssistAssistant(final ISourceViewer sourceViewer) {
        final IQuickAssistAssistant assistant = new QuickAssistAssistant();
        final SuiteSourceQuickAssistProcessor processor = new SuiteSourceQuickAssistProcessor(editor.getFileModel(),
                sourceViewer);
        assistant.setQuickAssistProcessor(processor);
        assistant.addCompletionListener(processor);
        sourceViewer.getTextWidget().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(final DisposeEvent e) {
                assistant.removeCompletionListener(processor);
            }
        });
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

        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();

        final ColoringPreference sectionPref = preferences.getSyntaxColoring(SyntaxHighlightingCategory.SECTION_HEADER);
        final IToken section = new Token(createAttribute(sectionPref));

        final ColoringPreference commentPref = preferences.getSyntaxColoring(SyntaxHighlightingCategory.COMMENT);
        final IToken comment = new Token(createAttribute(commentPref));

        final ColoringPreference definitionPref = preferences.getSyntaxColoring(SyntaxHighlightingCategory.DEFINITION);
        final IToken definition = new Token(createAttribute(definitionPref));

        final ColoringPreference variablePref = preferences.getSyntaxColoring(SyntaxHighlightingCategory.VARIABLE);
        final IToken variable = new Token(createAttribute(variablePref));

        final ColoringPreference callPref = preferences.getSyntaxColoring(SyntaxHighlightingCategory.KEYWORD_CALL);
        final IToken call = new Token(createAttribute(callPref));

        final ColoringPreference settingPref = preferences.getSyntaxColoring(SyntaxHighlightingCategory.SETTING);
        final IToken setting = new Token(createAttribute(settingPref));

        final ColoringPreference garbagePref = preferences
                .getSyntaxColoring(SyntaxHighlightingCategory.DEFAULT_SECTION);
        final IToken defaultSection = new Token(createAttribute(garbagePref));

        final ISyntaxColouringRule[] defaultRules = new ISyntaxColouringRule[] { new SectionHeaderRule(section),
                new CommentRule(comment), new MatchEverythingRule(defaultSection) };
        final RedTokensStore store = new RedTokensStore();
        sourceViewer.addTextInputListener(store);

        createDamageRepairer(reconciler, IDocument.DEFAULT_CONTENT_TYPE, store, defaultRules);

        final ISyntaxColouringRule[] testCasesRules = new ISyntaxColouringRule[] { new SectionHeaderRule(section),
                new CaseNameRule(definition), new TestCaseSettingsRule(setting), new TestCaseSettingsCallRule(call),
                new ExecutableRowCallRule(call), new CommentRule(comment), new VariableUsageRule(variable) };
        createDamageRepairer(reconciler, SuiteSourcePartitionScanner.TEST_CASES_SECTION, store,
                testCasesRules);

        final ISyntaxColouringRule[] keywordsRules = new ISyntaxColouringRule[] { new SectionHeaderRule(section),
                new KeywordNameRule(definition, variable), new KeywordSettingsRule(setting),
                new KeywordSettingsCallRule(call), new ExecutableRowCallRule(call), new CommentRule(comment),
                new VariableUsageRule(variable) };
        createDamageRepairer(reconciler, SuiteSourcePartitionScanner.KEYWORDS_SECTION, store, keywordsRules);

        final ISyntaxColouringRule[] settingsRules = new ISyntaxColouringRule[] { new SectionHeaderRule(section),
                new SettingRule(setting), new SettingsCallRule(call), new CommentRule(comment),
                new VariableUsageRule(variable) };
        createDamageRepairer(reconciler, SuiteSourcePartitionScanner.SETTINGS_SECTION, store, settingsRules);

        final ISyntaxColouringRule[] variablesRules = new ISyntaxColouringRule[] { new SectionHeaderRule(section),
                new VariableDefinitionRule(variable), new CommentRule(comment), new VariableUsageRule(variable) };
        createDamageRepairer(reconciler, SuiteSourcePartitionScanner.VARIABLES_SECTION, store,
                variablesRules);

        return reconciler;
    }

    private TextAttribute createAttribute(final ColoringPreference sectionPref) {
        return new TextAttribute(ColorsManager.getColor(sectionPref.getRgb()), null, sectionPref.getFontStyle());
    }

    private static void createDamageRepairer(final PresentationReconciler reconciler, final String contentType,
            final RedTokensStore store, final ISyntaxColouringRule[] rules) {
        final boolean useDirectScanner = Boolean.valueOf(System.getProperty("red.tmp.useDirectScanner")).booleanValue();
        final RedTokenScanner tokenScanner = new RedTokenScanner(rules);
        final ITokenScanner scanner = useDirectScanner ? tokenScanner : new RedCachingScanner(tokenScanner, store);
        final DefaultDamagerRepairer damagerRepairer = useDirectScanner ? new DefaultDamagerRepairer(scanner)
                : new RedDamagerRepairer(scanner);
        reconciler.setDamager(damagerRepairer, contentType);
        reconciler.setRepairer(damagerRepairer, contentType);
    }

    @Override
    public IReconciler getReconciler(final ISourceViewer sourceViewer) {
        if (reconciler == null) {
            reconciler = new MonoReconciler(getReconcilingStrategy(), true);
        }
        return reconciler;
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
}
