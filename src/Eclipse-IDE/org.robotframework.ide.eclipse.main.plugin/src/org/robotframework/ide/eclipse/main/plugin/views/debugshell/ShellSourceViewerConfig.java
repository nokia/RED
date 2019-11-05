/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.ColoringTokens;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.InformationControlSupport;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CombinedAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CycledContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.CycledContentAssistProcessor.AssistantCallbacks;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedContentAssistProcessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SuiteSourceAssistantContext;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.ISyntaxColouringRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.TokenTypeBasedRule;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring.VariableUsageRule;
import org.robotframework.red.graphics.ColorsManager;

import com.google.common.annotations.VisibleForTesting;

public class ShellSourceViewerConfig extends SourceViewerConfiguration {

    private final KeySequence contentAssistActivationTrigger;

    private ColoringTokens coloringTokens;

    public ShellSourceViewerConfig() {
        this.contentAssistActivationTrigger = getContentAssistActivationTrigger();
    }

    private KeySequence getContentAssistActivationTrigger() {
        final IBindingService service = PlatformUI.getWorkbench().getAdapter(IBindingService.class);
        return (KeySequence) service.getBestActiveBindingFor(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
    }

    ColoringTokens getColoringTokens() {
        return coloringTokens;
    }

    @Override
    public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer) {
        final ContentAssistant contentAssistant = new ContentAssistant();
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

        final SuiteSourceAssistantContext assistContext = new ShellAssistantContext(infoControlSupport,
                contentAssistActivationTrigger);

        final KeywordCallsInShellAssistProcessor keywordCallsProcessor = new KeywordCallsInShellAssistProcessor(
                assistContext);
        final VariablesInShellAssistProcessor variablesProcessor = new VariablesInShellAssistProcessor(assistContext);
        final RedContentAssistProcessor combinedProcessor = new CombinedAssistProcessor(keywordCallsProcessor,
                variablesProcessor);

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
        final CycledContentAssistProcessor cycledProcessor = new CycledContentAssistProcessor(assistContext,
                assistantAccessor);
        cycledProcessor.addProcessor(combinedProcessor);
        cycledProcessor.addProcessor(keywordCallsProcessor);
        cycledProcessor.addProcessor(variablesProcessor);

        contentAssistant.setContentAssistProcessor(cycledProcessor, IDocument.DEFAULT_CONTENT_TYPE);
        contentAssistant.addCompletionListener(cycledProcessor);
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(final ISourceViewer sourceViewer) {
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

        final IToken variable = coloringTokens.get(SyntaxHighlightingCategory.VARIABLE);
        final IToken call = coloringTokens.get(SyntaxHighlightingCategory.KEYWORD_CALL);
        final IToken quote = coloringTokens.get(SyntaxHighlightingCategory.KEYWORD_CALL_QUOTE);
        final IToken library = coloringTokens.get(SyntaxHighlightingCategory.KEYWORD_CALL_LIBRARY);
        final IToken modeToken = new Token(new TextAttribute(ColorsManager.getColor(100, 130, 185), null, 0));
        final IToken passToken = new Token(new TextAttribute(ColorsManager.getColor(50, 255, 50), null, 0));
        final IToken failToken = new Token(new TextAttribute(ColorsManager.getColor(255, 50, 50), null, 0));

        final ISyntaxColouringRule[] colouringRules = new ISyntaxColouringRule[] {
                new TokenTypeBasedRule(modeToken, ShellTokenType.MODE_FLAG, ShellTokenType.MODE_CONTINUATION),
                new TokenTypeBasedRule(passToken, ShellTokenType.PASS),
                new TokenTypeBasedRule(failToken, ShellTokenType.FAIL),
                new ExecutableCallInShellRule(call, library, quote, variable), new VariableUsageRule(variable) };

        final Map<String, ISyntaxColouringRule[]> rules = new HashMap<>();
        rules.put(IDocument.DEFAULT_CONTENT_TYPE, colouringRules);
        return rules;
    }

    private DefaultDamagerRepairer createDamageRepairer(final ISyntaxColouringRule... rules) {
        return new DefaultDamagerRepairer(new ShellTokensScanner(rules));
    }
}
