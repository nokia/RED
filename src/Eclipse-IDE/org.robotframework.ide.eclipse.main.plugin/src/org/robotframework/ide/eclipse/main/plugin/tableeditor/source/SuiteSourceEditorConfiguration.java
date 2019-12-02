/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
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
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Shell;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.SourceHyperlinksToFilesDetector;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.SourceHyperlinksToKeywordsDetector;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.SourceHyperlinksToVariablesDetector;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotSuiteAutoEditStrategy.EditStrategyPreferences;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.AssistantContext;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CodeReservedWordsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CodeReservedWordsInSettingsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CombinedAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CycledContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CycledContentAssistProcessor.AssistantCallbacks;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.GeneralSettingsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.ImportsInCodeAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.ImportsInSettingsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.KeywordCallTemplateAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.KeywordCallsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.KeywordCallsInSettingsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.LibrariesImportAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.NewKeywordTemplateAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.NewSectionTemplateAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.NewTaskTemplateAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.NewTestTemplateAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.LocalAssignQuickAssistProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.ResourcesImportAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SectionsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SettingsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.TemplateArgumentsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.VariablesAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.VariablesDefinitionsAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.VariablesImportAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.CaseNameRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.CommentRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.CommentRule.ITodoTaskToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ExecutableCallInSettingsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ExecutableCallRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.KeywordCallOverridingRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.KeywordNameRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.KeywordSettingsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.MatchEverythingRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.RedCachingScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.RedTokenScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.RedTokensStore;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SectionHeaderRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SettingRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SettingsTemplateRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SpecialTokensInNestedExecsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.SpecialTokensRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TaskNameRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TaskSettingsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TestCaseSettingsRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.VariableDefinitionRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.VariableUsageRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SourceDocumentFormatter;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter.SuiteSourceFormattingStrategy;

import com.google.common.annotations.VisibleForTesting;

public class SuiteSourceEditorConfiguration extends SourceViewerConfiguration {

    private final SuiteSourceEditor editor;

    private final KeySequence contentAssistActivationTrigger;

    private IReconciler reconciler;

    private ColoringTokens coloringTokens;

    private EditStrategyPreferences editStrategyPreferences;

    private ContentAssistant contentAssistant;

    private final RedTokensStore store;


    public SuiteSourceEditorConfiguration(final SuiteSourceEditor editor,
            final KeySequence contentAssistActivationTrigger) {
        this.editor = editor;
        this.contentAssistActivationTrigger = contentAssistActivationTrigger;
        this.store = new RedTokensStore();
    }

    ColoringTokens getColoringTokens() {
        return coloringTokens;
    }

    EditStrategyPreferences getEditStrategyPreferences() {
        return editStrategyPreferences;
    }

    ContentAssistant getContentAssistant() {
        return contentAssistant;
    }

    void resetTokensStore() {
        store.reset();
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
    public ITextDoubleClickStrategy getDoubleClickStrategy(final ISourceViewer sourceViewer, final String contentType) {
        return new RedSourceDoubleClickStrategy(editor.getFileModel().isTsvFile());
    }

    @Override
    public IHyperlinkPresenter getHyperlinkPresenter(final ISourceViewer sourceViewer) {
        return new MultipleHyperlinkPresenter(new RGB(0, 0, 255));
    }

    @Override
    public IHyperlinkDetector[] getHyperlinkDetectors(final ISourceViewer sourceViewer) {
        return getHyperlinkDetectors(RedPlugin.getDefault().getPreferences()::isLibraryKeywordHyperlinkingEnabled);
    }

    public IHyperlinkDetector[] getHyperlinkDetectors(final Supplier<Boolean> shouldLinkLibraryKeywords) {
        final RobotSuiteFile model = editor.getFileModel();
        if (model.isFromLocalStorage()) {
            return new IHyperlinkDetector[0];
        }
        return new IHyperlinkDetector[] { new SourceHyperlinksToVariablesDetector(model),
                new SourceHyperlinksToKeywordsDetector(shouldLinkLibraryKeywords, model),
                new SourceHyperlinksToFilesDetector(model) };
    }

    @Override
    public IAutoEditStrategy[] getAutoEditStrategies(final ISourceViewer sourceViewer, final String contentType) {
        if (editStrategyPreferences == null) {
            editStrategyPreferences = new EditStrategyPreferences(RedPlugin.getDefault().getPreferences());
            editStrategyPreferences.refresh();
        }
        return new IAutoEditStrategy[] {
                new RobotSuiteAutoEditStrategy(editStrategyPreferences, editor.getFileModel().isTsvFile()) };
    }

    @Override
    public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer) {
        contentAssistant = new ContentAssistant();
        contentAssistant.enableColoredLabels(true);
        contentAssistant.enableAutoInsert(RedPlugin.getDefault().getPreferences().isAssistantAutoInsertEnabled());
        contentAssistant
                .enableAutoActivation(RedPlugin.getDefault().getPreferences().isAssistantAutoActivationEnabled());
        contentAssistant
                .setAutoActivationDelay(RedPlugin.getDefault().getPreferences().getAssistantAutoActivationDelay());
        contentAssistant.setEmptyMessage("No proposals");
        contentAssistant.setShowEmptyList(true);
        contentAssistant.setStatusLineVisible(true);
        contentAssistant.setRepeatedInvocationMode(true);
        contentAssistant.setRepeatedInvocationTrigger(contentAssistActivationTrigger);

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
        final InformationControlSupport infoControlSupport = new InformationControlSupport(
                "Press 'Tab' from proposal table or click for focus");
        final AssistantCallbacks assistantAccessor = new AssistantCallbacks() {

            @Override
            public void setStatus(final String title) {
                contentAssistant.setStatusMessage(title);
            }

            @Override
            public void openCompletionProposals() {
                contentAssistant.showPossibleCompletions();
            }
        };
        final Supplier<RobotSuiteFile> modelSupplier = () -> {
            final RobotSuiteFile suiteModel = editor.getFileModel();
            suiteModel.dispose();

            try {
                final RobotDocument document = (RobotDocument) editor.getDocument();
                final RobotFileOutput fileOutput = document.getNewestFileOutput();
                suiteModel.link(fileOutput);
            } catch (final InterruptedException e) {
                // ok we'll return not-yet-parsed version
            }
            return suiteModel;
        };
        createSettingsAssist(infoControlSupport, contentAssistant, modelSupplier, assistantAccessor);
        createVariablesAssist(infoControlSupport, contentAssistant, modelSupplier, assistantAccessor);
        createKeywordsAssist(infoControlSupport, contentAssistant, modelSupplier, assistantAccessor);
        createTestCasesAssist(infoControlSupport, contentAssistant, modelSupplier, assistantAccessor);
        createTasksAssist(infoControlSupport, contentAssistant, modelSupplier, assistantAccessor);
        createDefaultAssist(infoControlSupport, contentAssistant, modelSupplier, assistantAccessor);
    }

    private void createSettingsAssist(final InformationControlSupport infoControlSupport,
            final ContentAssistant contentAssistant, final Supplier<RobotSuiteFile> modelSupplier,
            final AssistantCallbacks assistantAccessor) {
        final AssistantContext assistContext = new AssistantContext(infoControlSupport,
                modelSupplier, contentAssistActivationTrigger);

        final GeneralSettingsAssistProcessor settingNamesProcessor = new GeneralSettingsAssistProcessor(assistContext);
        final VariablesAssistProcessor variablesAssistProcessor = new VariablesAssistProcessor(assistContext);
        final CombinedAssistProcessor templatesAssistProcessor = new CombinedAssistProcessor("Templates",
                new KeywordCallTemplateAssistProcessor(assistContext),
                new NewSectionTemplateAssistProcessor(assistContext));

        final CombinedAssistProcessor combinedProcessor = new CombinedAssistProcessor(
                new CodeReservedWordsInSettingsAssistProcessor(assistContext),
                new LibrariesImportAssistProcessor(assistContext),
                new VariablesImportAssistProcessor(assistContext),
                new ResourcesImportAssistProcessor(assistContext),
                settingNamesProcessor,
                new SectionsAssistProcessor(assistContext),
                new KeywordCallsInSettingsAssistProcessor(assistContext),
                new ImportsInSettingsAssistProcessor(assistContext),
                variablesAssistProcessor,
                templatesAssistProcessor);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext,
                assistantAccessor);
        cycledProcessor.addProcessor(combinedProcessor);
        cycledProcessor.addProcessor(settingNamesProcessor);
        cycledProcessor.addProcessor(variablesAssistProcessor);
        cycledProcessor.addProcessor(templatesAssistProcessor);

        contentAssistant.setContentAssistProcessor(cycledProcessor, SuiteSourcePartitionScanner.SETTINGS_SECTION);
        contentAssistant.addCompletionListener(cycledProcessor);
    }

    private void createVariablesAssist(final InformationControlSupport infoControlSupport,
            final ContentAssistant contentAssistant, final Supplier<RobotSuiteFile> modelSupplier,
            final AssistantCallbacks assistantAccessor) {
        final AssistantContext assistContext = new AssistantContext(infoControlSupport,
                modelSupplier, contentAssistActivationTrigger);

        final VariablesAssistProcessor variablesAssistProcessor = new VariablesAssistProcessor(assistContext);
        final CombinedAssistProcessor templatesAssistProcessor = new CombinedAssistProcessor("Templates",
                new KeywordCallTemplateAssistProcessor(assistContext),
                new NewSectionTemplateAssistProcessor(assistContext));

        final CombinedAssistProcessor combinedProcessor = new CombinedAssistProcessor(
                new VariablesDefinitionsAssistProcessor(assistContext),
                new SectionsAssistProcessor(assistContext),
                variablesAssistProcessor,
                templatesAssistProcessor);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext,
                assistantAccessor);
        cycledProcessor.addProcessor(combinedProcessor);
        cycledProcessor.addProcessor(variablesAssistProcessor);
        cycledProcessor.addProcessor(templatesAssistProcessor);

        contentAssistant.setContentAssistProcessor(cycledProcessor, SuiteSourcePartitionScanner.VARIABLES_SECTION);
        contentAssistant.addCompletionListener(cycledProcessor);
    }

    private void createTestCasesAssist(final InformationControlSupport infoControlSupport,
            final ContentAssistant contentAssistant, final Supplier<RobotSuiteFile> modelSupplier,
            final AssistantCallbacks assistantAccessor) {
        final AssistantContext assistContext = new AssistantContext(infoControlSupport,
                modelSupplier, contentAssistActivationTrigger);

        final KeywordCallsAssistProcessor keywordCallsAssistProcessor = new KeywordCallsAssistProcessor(assistContext);
        final VariablesAssistProcessor variablesAssistProcessor = new VariablesAssistProcessor(assistContext);
        final CombinedAssistProcessor templatesAssistProcessor = new CombinedAssistProcessor("Templates",
                new KeywordCallTemplateAssistProcessor(assistContext),
                new NewSectionTemplateAssistProcessor(assistContext),
                new NewTestTemplateAssistProcessor(assistContext));

        final CombinedAssistProcessor combinedProcessor = new CombinedAssistProcessor(
                new SectionsAssistProcessor(assistContext),
                new TemplateArgumentsAssistProcessor(assistContext),
                new SettingsAssistProcessor(assistContext),
                new CodeReservedWordsAssistProcessor(assistContext),
                keywordCallsAssistProcessor,
                new ImportsInCodeAssistProcessor(assistContext),
                variablesAssistProcessor,
                templatesAssistProcessor);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext,
                assistantAccessor);
        cycledProcessor.addProcessor(combinedProcessor);
        cycledProcessor.addProcessor(keywordCallsAssistProcessor);
        cycledProcessor.addProcessor(variablesAssistProcessor);
        cycledProcessor.addProcessor(templatesAssistProcessor);

        contentAssistant.setContentAssistProcessor(cycledProcessor, SuiteSourcePartitionScanner.TEST_CASES_SECTION);
        contentAssistant.addCompletionListener(cycledProcessor);
    }

    private void createTasksAssist(final InformationControlSupport infoControlSupport,
            final ContentAssistant contentAssistant, final Supplier<RobotSuiteFile> modelSupplier,
            final AssistantCallbacks assistantAccessor) {
        final AssistantContext assistContext = new AssistantContext(infoControlSupport,
                modelSupplier, contentAssistActivationTrigger);

        final KeywordCallsAssistProcessor keywordCallsAssistProcessor = new KeywordCallsAssistProcessor(assistContext);
        final VariablesAssistProcessor variablesAssistProcessor = new VariablesAssistProcessor(assistContext);
        final CombinedAssistProcessor templatesAssistProcessor = new CombinedAssistProcessor("Templates",
                new KeywordCallTemplateAssistProcessor(assistContext),
                new NewSectionTemplateAssistProcessor(assistContext),
                new NewTaskTemplateAssistProcessor(assistContext));

        final CombinedAssistProcessor combinedProcessor = new CombinedAssistProcessor(
                new SectionsAssistProcessor(assistContext),
                new TemplateArgumentsAssistProcessor(assistContext),
                new SettingsAssistProcessor(assistContext),
                new CodeReservedWordsAssistProcessor(assistContext),
                keywordCallsAssistProcessor,
                new ImportsInCodeAssistProcessor(assistContext),
                variablesAssistProcessor,
                templatesAssistProcessor);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext,
                assistantAccessor);
        cycledProcessor.addProcessor(combinedProcessor);
        cycledProcessor.addProcessor(keywordCallsAssistProcessor);
        cycledProcessor.addProcessor(variablesAssistProcessor);
        cycledProcessor.addProcessor(templatesAssistProcessor);

        contentAssistant.setContentAssistProcessor(cycledProcessor, SuiteSourcePartitionScanner.TASKS_SECTION);
        contentAssistant.addCompletionListener(cycledProcessor);
    }

    private void createKeywordsAssist(final InformationControlSupport infoControlSupport,
            final ContentAssistant contentAssistant, final Supplier<RobotSuiteFile> modelSupplier,
            final AssistantCallbacks assistantAccessor) {
        final AssistantContext assistContext = new AssistantContext(infoControlSupport,
                modelSupplier, contentAssistActivationTrigger);
        final KeywordCallsAssistProcessor keywordCallsAssistProcessor = new KeywordCallsAssistProcessor(assistContext);
        final VariablesAssistProcessor variablesAssistProcessor = new VariablesAssistProcessor(assistContext);
        final CombinedAssistProcessor templatesAssistProcessor = new CombinedAssistProcessor("Templates",
                new KeywordCallTemplateAssistProcessor(assistContext),
                new NewSectionTemplateAssistProcessor(assistContext),
                new NewKeywordTemplateAssistProcessor(assistContext));

        final CombinedAssistProcessor combinedProcessor = new CombinedAssistProcessor(
                new SectionsAssistProcessor(assistContext),
                new SettingsAssistProcessor(assistContext),
                new CodeReservedWordsAssistProcessor(assistContext),
                keywordCallsAssistProcessor,
                new ImportsInCodeAssistProcessor(assistContext),
                variablesAssistProcessor,
                templatesAssistProcessor);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext,
                assistantAccessor);
        cycledProcessor.addProcessor(combinedProcessor);
        cycledProcessor.addProcessor(keywordCallsAssistProcessor);
        cycledProcessor.addProcessor(variablesAssistProcessor);
        cycledProcessor.addProcessor(templatesAssistProcessor);

        contentAssistant.setContentAssistProcessor(cycledProcessor, SuiteSourcePartitionScanner.KEYWORDS_SECTION);
        contentAssistant.addCompletionListener(cycledProcessor);
    }

    private void createDefaultAssist(final InformationControlSupport infoControlSupport,
            final ContentAssistant contentAssistant, final Supplier<RobotSuiteFile> modelSupplier,
            final AssistantCallbacks assistantAccessor) {
        // We are adding all the assistants for default content type. Most of them (excluding
        // section headers assistant) are working in default content type only at the very last
        // position in file (this position always has default content type, but it can be actually
        // prepended with some valid meaningful content type)

        final AssistantContext assistContext = new AssistantContext(infoControlSupport,
                modelSupplier, contentAssistActivationTrigger);

        final GeneralSettingsAssistProcessor generalSettingProcessor = new GeneralSettingsAssistProcessor(
                assistContext);
        final KeywordCallsAssistProcessor keywordCallsProcessor = new KeywordCallsAssistProcessor(assistContext);
        final VariablesAssistProcessor variablesProcessor = new VariablesAssistProcessor(assistContext);
        final CombinedAssistProcessor templatesAssistProcessor = new CombinedAssistProcessor("Templates",
                new KeywordCallTemplateAssistProcessor(assistContext),
                new NewSectionTemplateAssistProcessor(assistContext),
                new NewKeywordTemplateAssistProcessor(assistContext),
                new NewTestTemplateAssistProcessor(assistContext),
                new NewTaskTemplateAssistProcessor(assistContext));

        final CombinedAssistProcessor combinedProcessor = new CombinedAssistProcessor(
                new VariablesDefinitionsAssistProcessor(assistContext),
                new LibrariesImportAssistProcessor(assistContext),
                new VariablesImportAssistProcessor(assistContext),
                new ResourcesImportAssistProcessor(assistContext),
                generalSettingProcessor,
                new SectionsAssistProcessor(assistContext),
                new TemplateArgumentsAssistProcessor(assistContext),
                new SettingsAssistProcessor(assistContext),
                new CodeReservedWordsAssistProcessor(assistContext),
                new CodeReservedWordsInSettingsAssistProcessor(assistContext),
                new KeywordCallsInSettingsAssistProcessor(assistContext),
                keywordCallsProcessor,
                new ImportsInSettingsAssistProcessor(assistContext),
                new ImportsInCodeAssistProcessor(assistContext),
                variablesProcessor,
                templatesAssistProcessor);

        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext,
                assistantAccessor);
        cycledProcessor.addProcessor(combinedProcessor);
        cycledProcessor.addProcessor(generalSettingProcessor);
        cycledProcessor.addProcessor(keywordCallsProcessor);
        cycledProcessor.addProcessor(variablesProcessor);
        cycledProcessor.addProcessor(templatesAssistProcessor);

        contentAssistant.setContentAssistProcessor(cycledProcessor, IDocument.DEFAULT_CONTENT_TYPE);
        contentAssistant.addCompletionListener(cycledProcessor);
    }

    @Override
    public IQuickAssistAssistant getQuickAssistAssistant(final ISourceViewer sourceViewer) {
        final IQuickAssistAssistant assistant = new QuickAssistAssistant();
        final SuiteSourceQuickAssistProcessor processor = new SuiteSourceQuickAssistProcessor(editor.getFileModel(),
                sourceViewer);
        processor.addAssistProviders(newArrayList(new LocalAssignQuickAssistProvider()));
        assistant.setQuickAssistProcessor(processor);
        assistant.addCompletionListener(processor);
        sourceViewer.getTextWidget().addDisposeListener(e -> assistant.removeCompletionListener(processor));
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
        final List<String> legal = new ArrayList<>(SuiteSourcePartitionScanner.LEGAL_CONTENT_TYPES);
        legal.add(0, IDocument.DEFAULT_CONTENT_TYPE);
        return legal.toArray(new String[0]);
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(final ISourceViewer sourceViewer) {
        sourceViewer.addTextInputListener(store);

        final PresentationReconciler reconciler = new PresentationReconciler();
        createColoringRules().forEach((contentType, rules) -> {
            final DefaultDamagerRepairer damagerRepairer = createDamageRepairer(rules);
            reconciler.setDamager(damagerRepairer, contentType);
            reconciler.setRepairer(damagerRepairer, contentType);
        });
        return reconciler;
    }

    @VisibleForTesting
    Map<String, ISyntaxColouringRule[]> createColoringRules() {
        if (coloringTokens == null) {
            coloringTokens = new ColoringTokens(RedPlugin.getDefault().getPreferences());
            coloringTokens.initialize();
        }

        final IToken section = coloringTokens.get(SyntaxHighlightingCategory.SECTION_HEADER);
        final IToken comment = coloringTokens.get(SyntaxHighlightingCategory.COMMENT);
        final ITodoTaskToken tasks = (ITodoTaskToken) coloringTokens.get(SyntaxHighlightingCategory.TASKS);
        final IToken definition = coloringTokens.get(SyntaxHighlightingCategory.DEFINITION);
        final IToken variable = coloringTokens.get(SyntaxHighlightingCategory.VARIABLE);
        final IToken call = coloringTokens.get(SyntaxHighlightingCategory.KEYWORD_CALL);
        final IToken libraryKwCall = coloringTokens.get(SyntaxHighlightingCategory.KEYWORD_CALL_FROM_LIB);
        final IToken quote = coloringTokens.get(SyntaxHighlightingCategory.KEYWORD_CALL_QUOTE);
        final IToken library = coloringTokens.get(SyntaxHighlightingCategory.KEYWORD_CALL_LIBRARY);
        final IToken setting = coloringTokens.get(SyntaxHighlightingCategory.SETTING);
        final IToken gherkin = coloringTokens.get(SyntaxHighlightingCategory.GHERKIN);
        final IToken special = coloringTokens.get(SyntaxHighlightingCategory.SPECIAL);
        final IToken defaultSection = coloringTokens.get(SyntaxHighlightingCategory.DEFAULT_SECTION);

        final Function<ISyntaxColouringRule, ISyntaxColouringRule> callOverridder = r -> new KeywordCallOverridingRule(
                r, call, libraryKwCall, editor.getKeywordUsagesFinder());

        final ISyntaxColouringRule[] defaultRules = new ISyntaxColouringRule[] { new SectionHeaderRule(section),
                new CommentRule(comment, tasks), new MatchEverythingRule(defaultSection) };

        final ISyntaxColouringRule[] testCasesRules = new ISyntaxColouringRule[] { new SectionHeaderRule(section),
                new CaseNameRule(definition), new TestCaseSettingsRule(setting),
                callOverridder.apply(new SettingsTemplateRule(call, variable)),
                callOverridder.apply(ExecutableCallInSettingsRule.forExecutableInTestSetupOrTeardown(call, gherkin,
                        library, quote, variable)),
                callOverridder
                        .apply(ExecutableCallRule.forExecutableInTestCase(call, gherkin, library, quote, variable)),
                new SpecialTokensInNestedExecsRule(special), new CommentRule(comment, tasks),
                new VariableUsageRule(variable), new SpecialTokensRule(special) };

        final ISyntaxColouringRule[] tasksRules = new ISyntaxColouringRule[] { new SectionHeaderRule(section),
                new TaskNameRule(definition), new TaskSettingsRule(setting),
                callOverridder.apply(new SettingsTemplateRule(call, variable)),
                callOverridder.apply(ExecutableCallInSettingsRule.forExecutableInTaskSetupOrTeardown(call, gherkin,
                        library, quote, variable)),
                callOverridder.apply(ExecutableCallRule.forExecutableInTask(call, gherkin, library, quote, variable)),
                new SpecialTokensInNestedExecsRule(special), new CommentRule(comment, tasks),
                new VariableUsageRule(variable), new SpecialTokensRule(special) };

        final ISyntaxColouringRule[] keywordsRules = new ISyntaxColouringRule[] { new SectionHeaderRule(section),
                new KeywordNameRule(definition, variable), new KeywordSettingsRule(setting),
                callOverridder.apply(ExecutableCallInSettingsRule.forExecutableInKeywordTeardown(call, gherkin, library,
                        quote, variable)),
                callOverridder
                        .apply(ExecutableCallRule.forExecutableInKeyword(call, gherkin, library, quote, variable)),
                new SpecialTokensInNestedExecsRule(special), new CommentRule(comment, tasks),
                new VariableUsageRule(variable), new SpecialTokensRule(special) };

        final ISyntaxColouringRule[] settingsRules = new ISyntaxColouringRule[] { new SectionHeaderRule(section),
                new SettingRule(setting), callOverridder.apply(new SettingsTemplateRule(call, variable)),
                callOverridder.apply(ExecutableCallInSettingsRule.forExecutableInGeneralSettingsSetupsOrTeardowns(call,
                        gherkin, library, quote, variable)),
                new SpecialTokensInNestedExecsRule(special), new CommentRule(comment, tasks),
                new VariableUsageRule(variable), new SpecialTokensRule(special) };

        final ISyntaxColouringRule[] variablesRules = new ISyntaxColouringRule[] { new SectionHeaderRule(section),
                new VariableDefinitionRule(variable), new CommentRule(comment, tasks),
                new VariableUsageRule(variable) };

        final Map<String, ISyntaxColouringRule[]> rules = new HashMap<>();
        rules.put(IDocument.DEFAULT_CONTENT_TYPE, defaultRules);
        rules.put(SuiteSourcePartitionScanner.TEST_CASES_SECTION, testCasesRules);
        rules.put(SuiteSourcePartitionScanner.TASKS_SECTION, tasksRules);
        rules.put(SuiteSourcePartitionScanner.KEYWORDS_SECTION, keywordsRules);
        rules.put(SuiteSourcePartitionScanner.SETTINGS_SECTION, settingsRules);
        rules.put(SuiteSourcePartitionScanner.VARIABLES_SECTION, variablesRules);
        return rules;
    }

    private DefaultDamagerRepairer createDamageRepairer(final ISyntaxColouringRule... rules) {
        final RedTokenScanner tokenScanner = new RedTokenScanner(rules);
        final ITokenScanner scanner = new RedCachingScanner(tokenScanner, store);
        return new RedDamagerRepairer(scanner, editor.getViewer());
    }

    @Override
    public IReconciler getReconciler(final ISourceViewer sourceViewer) {
        if (reconciler == null) {
            final IReconcilingStrategy strategy = new SuiteSourceReconcilingStrategy(editor);
            reconciler = new MonoReconciler(strategy, true);
        }
        return reconciler;
    }

    @Override
    public IContentFormatter getContentFormatter(final ISourceViewer sourceViewer) {
        final IRuntimeEnvironment env = editor.getFileModel().getRuntimeEnvironment();

        final MultiPassContentFormatter formatter = new MultiPassContentFormatter(
                getConfiguredDocumentPartitioning(sourceViewer), IDocument.DEFAULT_CONTENT_TYPE);
        formatter.setMasterStrategy(new SuiteSourceFormattingStrategy(
                () -> SourceDocumentFormatter.create(RedPlugin.getDefault().getPreferences(), env)));
        return formatter;
    }
}
