/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.ScrolledFormText;
import org.rf.ide.core.libraries.KeywordSpecification;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.jface.dialogs.InputLoadingFormComposite;

class KeywordDocumentationComposite extends InputLoadingFormComposite {

    private InputLoadingFormComposite.InputJob collectingJob;

    private FormText argumentsText;
    private ScrolledFormText documentationFormText;

    KeywordDocumentationComposite(final Composite parent, final KeywordSpecification specification) {
        super(parent, SWT.NONE, specification.getName());
        this.collectingJob = new InputLoadingFormComposite.InputJob("Loading keyword documentation") {
            @Override
            protected Object createInput(final IProgressMonitor monitor) {
                setStatus(Status.OK_STATUS);
                return new Documentation(specification);
            }
        };
        createComposite();
    }

    @Override
    protected Composite createControl(final Composite parent) {
        setFormImage(RedImages.getKeywordImage());

        final Composite actualComposite = getToolkit().createComposite(parent);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(3, 3).applyTo(actualComposite);

        argumentsText = getToolkit().createFormText(actualComposite, false);
        argumentsText.setFont("monospace", JFaceResources.getTextFont());
        argumentsText.setColor("header", getToolkit().getColors().getColor(IFormColors.TITLE));
        argumentsText.setFont("header", JFaceResources.getBannerFont());
        GridDataFactory.fillDefaults().span(2, 1).hint(400, SWT.DEFAULT).grab(true, false).applyTo(argumentsText);

        documentationFormText = new ScrolledFormText(actualComposite, SWT.V_SCROLL | SWT.H_SCROLL, true);
        getToolkit().adapt(documentationFormText);
        GridDataFactory.fillDefaults().span(2, 1).hint(400, 500).grab(true, true).applyTo(documentationFormText);

        final FormText docFormText = documentationFormText.getFormText();
        docFormText.setWhitespaceNormalized(false);
        docFormText.setFont("monospace", JFaceResources.getTextFont());
        docFormText.setColor("header", getToolkit().getColors().getColor(IFormColors.TITLE));
        docFormText.setFont("header", JFaceResources.getBannerFont());

        final IHyperlinkListener hyperlinkListener = LibraryDocumentationComposite.createHyperlinkListener();
        docFormText.addHyperlinkListener(hyperlinkListener);
        argumentsText.addHyperlinkListener(hyperlinkListener);
        addDisposeListener(e -> {
            docFormText.removeHyperlinkListener(hyperlinkListener);
            argumentsText.removeHyperlinkListener(hyperlinkListener);
        });
        return actualComposite;
    }

    @Override
    protected void createActions() {
        addAction(new WrapFormAction(documentationFormText));
        super.createActions();
    }

    @Override
    protected Composite getControl() {
        return (Composite) super.getControl();
    }

    @Override
    protected InputLoadingFormComposite.InputJob provideInputCollectingJob() {
        return collectingJob;
    }

    @Override
    protected void fillControl(final Object jobResult) {
        final Documentation kwSpec = (Documentation) jobResult;

        if (getControl() == null || getControl().isDisposed()) {
            return;
        }

        if (kwSpec.isHtml) {
            documentationFormText.getFormText().setText(kwSpec.text, true, true);
        } else {
            argumentsText.setText(kwSpec.arguments, true, false);
            documentationFormText.getFormText().setText(kwSpec.text, false, true);
        }
        argumentsText.layout();
        documentationFormText.reflow(true);
        getControl().layout();
    }

    private class Documentation {
        private String arguments;
        private final String text;
        private final boolean isHtml;

        public Documentation(final KeywordSpecification spec) {
            isHtml = spec.canBeConvertedToHtml();
            if (isHtml) {
                arguments = "";
                text = "<form>" + createArgumentsDoc(spec.getArguments()) + spec.getDocumentationAsHtml() + "</form>";
            } else {
                arguments = "<form>" + createArgumentsDoc(spec.getArguments()) + "</form>";
                text = spec.getDocumentation();
            }
        }

        private String createArgumentsDoc(final List<String> arguments) {
            final StringBuilder builder = new StringBuilder("<p><b>Arguments</b></p>");
            for (final String arg : arguments) {
                builder.append("<li>");
                builder.append(arg);
                builder.append("</li>");
            }
            return builder.toString();
        }
    }
}
