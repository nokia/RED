/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation;

import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.DocumentationViewInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordDefinitionInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.KeywordProposalInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.LibraryImportSettingInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SettingOfCaseWithDocumentationInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SettingOfKeywordWithDocumentationInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SettingWithDocumentationInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.SuiteFileInput;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs.TestCaseInput;

public class Documentations {

    public static void showDocForEditorSourceSelection(final IWorkbenchPartSite site, final RobotSuiteFile suiteModel,
            final IDocument document, final int offset) {

        final Optional<? extends RobotElement> element = suiteModel.findElement(offset);
        if (element.isPresent()) {
            try {
                final Optional<IRegion> activeCellRegion = DocumentUtilities.findCellRegion(document,
                        suiteModel.isTsvFile(), offset);
                if (activeCellRegion.isPresent()) {
                    final String cellContent = document.get(activeCellRegion.get().getOffset(),
                            activeCellRegion.get().getLength());
                    showDoc(site, (RobotFileInternalElement) element.get(), cellContent);
                }
            } catch (final BadLocationException e) {
                RedPlugin.logError("Could not find selected position in document", e);
            }
        }
    }

    public static void showDocForEditorTablesSelection(final IWorkbenchPartSite site,
            final SelectionLayerAccessor selectionLayerAccessor) {
        final PositionCoordinate[] coordinates = selectionLayerAccessor.getSelectedPositions();
        Stream.of(coordinates)
                .findFirst()
                .map(selectionLayerAccessor::getLabelFromCell)
                .ifPresent(label -> selectionLayerAccessor.getSelectedElements()
                        .filter(RobotFileInternalElement.class::isInstance)
                        .map(RobotFileInternalElement.class::cast)
                        .findFirst()
                        .ifPresent(elem -> showDoc(site, elem, label)));
    }

    private static void showDoc(final IWorkbenchPartSite site, final RobotFileInternalElement element,
            final String cellContent) {

        final Optional<DocumentationViewInput> input = requiresPerLabelSearching(element)
                ? Optional.of(new KeywordProposalInput(element, cellContent))
                : findInput(element);
        if (input.isPresent()) {
            display(site, input.get());
        }
    }

    private static boolean requiresPerLabelSearching(final RobotFileInternalElement element) {
        if (element instanceof RobotSetting) {
            final RobotSetting setting = (RobotSetting) element;
            return !(setting.isDocumentation() || setting.isLibraryImport() || setting.isResourceImport());
        } else if (element instanceof RobotDefinitionSetting) {
            return !((RobotDefinitionSetting) element).isDocumentation();
        }
        return element instanceof RobotKeywordCall;
    }

    private static Optional<DocumentationViewInput> findInput(final RobotFileInternalElement element) {
        if (element instanceof RobotSuiteFile) {
            return Optional.of(new SuiteFileInput((RobotSuiteFile) element));

        } else if (element instanceof RobotKeywordDefinition) {
            return Optional.of(new KeywordDefinitionInput((RobotKeywordDefinition) element));

        } else if (element instanceof RobotCase) {
            return Optional.of(new TestCaseInput((RobotCase) element));

        } else if (element instanceof RobotDefinitionSetting) {
            final RobotDefinitionSetting setting = (RobotDefinitionSetting) element;
            if (setting.isDocumentation() && setting.getParent() instanceof RobotCase) {
                return Optional.of(new SettingOfCaseWithDocumentationInput(setting));

            } else if (setting.isDocumentation() && setting.getParent() instanceof RobotKeywordDefinition) {
                return Optional.of(new SettingOfKeywordWithDocumentationInput(setting));
            }

        } else if (element instanceof RobotSetting) {
            final RobotSetting setting = (RobotSetting) element;
            if (setting.isDocumentation()) {
                return Optional.of(new SettingWithDocumentationInput(setting));

            } else if (setting.isResourceImport()) {
                final Optional<IResource> resource = setting.getImportedResource();
                if (resource.isPresent() && resource.get().exists() && resource.get().getType() == IResource.FILE) {
                    final RobotSuiteFile resourceSuite = RedPlugin.getModelManager()
                            .createSuiteFile((IFile) resource.get());

                    return Optional.of(new SuiteFileInput(resourceSuite));
                }

            } else if (setting.isLibraryImport()) {
                return Optional.of(new LibraryImportSettingInput(setting));
            }
        }
        return Optional.empty();
    }

    public static void markViewSyncBroken(final IWorkbenchPartSite partSite) {
        final IWorkbenchPage page = partSite.getPage();
        final IViewPart docViewPart = page.findView(DocumentationView.ID);
        if (docViewPart == null) {
            return;
        }
        @SuppressWarnings("restriction")
        final DocumentationView docView = ((DocumentationViewWrapper) docViewPart).getComponent();
        docView.markSyncBroken();
    }

    private static void display(final IWorkbenchPartSite partSite, final DocumentationViewInput input) {
        final DocumentationView docView = openDocumentationViewIfNeeded(partSite);
        if (docView == null) {
            return;
        }
        docView.displayDocumentation(input);
    }

    @SuppressWarnings("restriction")
    private static DocumentationView openDocumentationViewIfNeeded(final IWorkbenchPartSite partSite) {
        final IWorkbenchPage page = partSite.getPage();
        final IViewPart docViewPart = page.findView(DocumentationView.ID);
        if (docViewPart == null) {
            try {
                return ((DocumentationViewWrapper) page.showView(DocumentationView.ID)).getComponent();
            } catch (final PartInitException e) {
                RedPlugin.logError("Unable to show Documentation View.", e);
                return null;
            }
        } else {
            return ((DocumentationViewWrapper) docViewPart).getComponent();
        }
    }
}
