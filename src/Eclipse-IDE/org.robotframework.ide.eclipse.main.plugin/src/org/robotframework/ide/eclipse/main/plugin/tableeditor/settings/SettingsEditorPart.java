/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors.ITableHyperlinksDetector;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TreeLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsEditorPart.SettingsEditor;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;

@SuppressWarnings("restriction")
public class SettingsEditorPart extends DISectionEditorPart<SettingsEditor> {

    public SettingsEditorPart() {
        super(SettingsEditor.class);
        setTitleImage(ImagesManager.getImage(RedImages.getRobotSettingImage()));
    }

    public static class SettingsEditor extends SectionEditorPart {

        private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.settings.context";

        private GeneralSettingsFormFragment generalFragment;

        private Optional<MetadataSettingsFormFragment> metadataFragment;

        private ImportSettingsFormFragment importFragment;

        private SettingsEditorPageSelectionProvider settingsEditorPageSelectionProvider;

        @Override
        protected String getContextId() {
            return CONTEXT_ID;
        }

        @Override
        public String getId() {
            return "red.settings";
        }

        @Override
        protected String getTitle() {
            return "Settings";
        }

        @Override
        protected String getSectionName() {
            return RobotSettingsSection.SECTION_NAME;
        }

        @Override
        public boolean isPartFor(final RobotSuiteFileSection section) {
            return section instanceof RobotSettingsSection;
        }

        @Override
        public void revealElement(final RobotElement robotElement) {
            revealElement(robotElement, false);
        }

        @Override
        public void revealElementAndFocus(final RobotElement robotElement) {
            revealElement(robotElement, true);
        }

        private void revealElement(final RobotElement robotElement, final boolean focus) {
            if (robotElement instanceof RobotSetting) {
                final RobotSetting setting = (RobotSetting) robotElement;
                if (setting.getGroup() == SettingsGroup.NO_GROUP) {
                    generalFragment.revealSetting(setting, focus);
                    if (metadataFragment.isPresent()) {
                        metadataFragment.get().clearSettingsSelection();
                    }
                    importFragment.clearSettingsSelection();
                } else if (setting.getGroup() == SettingsGroup.METADATA) {
                    generalFragment.clearSettingsSelection();
                    if (metadataFragment.isPresent()) {
                        metadataFragment.get().revealSetting(setting, focus);
                    }
                    importFragment.clearSettingsSelection();
                } else if (setting.isImportSetting()) {
                    generalFragment.clearSettingsSelection();
                    if (metadataFragment.isPresent()) {
                        metadataFragment.get().clearSettingsSelection();
                    }
                    importFragment.revealSetting(setting, focus);
                }
            }
        }

        @Override
        public Optional<? extends RobotSuiteFileSection> provideSection(final RobotSuiteFile suiteModel) {
            return suiteModel.findSection(RobotSettingsSection.class);
        }

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            generalFragment = new GeneralSettingsFormFragment();
            metadataFragment = shouldShowMetadata() ? Optional.of(new MetadataSettingsFormFragment())
                    : Optional.<MetadataSettingsFormFragment> absent();
            importFragment = new ImportSettingsFormFragment();

            if (metadataFragment.isPresent()) {
                return newArrayList(generalFragment, metadataFragment.get(), importFragment);
            } else {
                return newArrayList(generalFragment, importFragment);
            }
        }

        private boolean shouldShowMetadata() {
            final RobotSuiteFile model = (RobotSuiteFile) getContext().get(RobotEditorSources.SUITE_FILE_MODEL);
            return model != null && !model.isResourceFile();
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            if (settingsEditorPageSelectionProvider == null) {
                settingsEditorPageSelectionProvider = createSettingsEditorPageSelectionProvider();
            }
            return settingsEditorPageSelectionProvider;
        }

        @Override
        public SelectionLayerAccessor getSelectionLayerAccessor() {
            if (settingsEditorPageSelectionProvider == null) {
                settingsEditorPageSelectionProvider = createSettingsEditorPageSelectionProvider();
            }
            return settingsEditorPageSelectionProvider.getSelectionLayerAccessor();
        }
        

        @Override
        public Optional<TreeLayerAccessor> getTreeLayerAccessor() {
            return Optional.absent();
        }

        @Override
        public void waitForPendingJobs() {
            if (generalFragment != null) {
                generalFragment.waitForDocumentationChangeJob();
            }
        }

        private SettingsEditorPageSelectionProvider createSettingsEditorPageSelectionProvider() {
            if (metadataFragment.isPresent()) {
                return new SettingsEditorPageSelectionProvider(generalFragment, metadataFragment.get(), importFragment);
            } else {
                return new SettingsEditorPageSelectionProvider(generalFragment, importFragment);
            }
        }

        @Override
        public void aboutToChangeToOtherPage() {
            generalFragment.aboutToChangeToOtherPage();
            importFragment.aboutToChangeToOtherPage();
            if (metadataFragment.isPresent()) {
                metadataFragment.get().aboutToChangeToOtherPage();
            }
        }

        @Override
        @Persist
        public void onSave() {
            final ISelection selection = settingsEditorPageSelectionProvider.getSelection();

            // when documentation is not selected, invoke save action on table
            if (selection != null && !selection.isEmpty()) {
                final ISettingsFormFragment activeFormFragment = settingsEditorPageSelectionProvider.getActiveFormFragment();
                activeFormFragment.invokeSaveAction();
            }

            final IDirtyProviderService dirtyProviderService = getContext().getActive(IDirtyProviderService.class);
            dirtyProviderService.setDirtyState(false);
        }

        @Override
        public List<ITableHyperlinksDetector> getDetectors() {
            final ISelection selection = settingsEditorPageSelectionProvider.getSelection();

            if (selection != null && !selection.isEmpty()) {
                final ISettingsFormFragment activeFormFragment = settingsEditorPageSelectionProvider
                        .getActiveFormFragment();
                return activeFormFragment.getDetectors();
            }
            return newArrayList();
        }
    }
}
