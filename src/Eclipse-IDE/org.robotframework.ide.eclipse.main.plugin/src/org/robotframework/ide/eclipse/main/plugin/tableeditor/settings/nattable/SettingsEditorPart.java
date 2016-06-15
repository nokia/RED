/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.nattable;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.FocusedViewerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.nattable.SettingsEditorPart.SettingsEditor;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;

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
            final RobotSetting setting = (RobotSetting) robotElement;
            if (setting.getGroup() == SettingsGroup.NO_GROUP) {
                generalFragment.revealSetting(setting);
                if (metadataFragment.isPresent()) {
                    metadataFragment.get().clearSettingsSelection();
                }
                importFragment.clearSettingsSelection();
            } else if (setting.getGroup() == SettingsGroup.METADATA) {
                generalFragment.clearSettingsSelection();
                if (metadataFragment.isPresent()) {
                    metadataFragment.get().revealSetting(setting);
                }
                importFragment.clearSettingsSelection();
            } else if (SettingsGroup.getImportsGroupsSet().contains(setting.getGroup())) {
                generalFragment.clearSettingsSelection();
                if (metadataFragment.isPresent()) {
                    metadataFragment.get().clearSettingsSelection();
                }
                importFragment.revealSetting(setting);
            }
        }

        @Override
        public Optional<? extends RobotSuiteFileSection> provideSection(final RobotSuiteFile suiteModel) {
            return suiteModel.findSection(RobotSettingsSection.class);
        }

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            generalFragment = new GeneralSettingsFormFragment();
            final MetadataSettingsFormFragment fragment = shouldShowMetadata() ? new MetadataSettingsFormFragment()
                    : null;
            metadataFragment = Optional.fromNullable(fragment);
            importFragment = new ImportSettingsFormFragment();

            if (metadataFragment.isPresent()) {
                return newArrayList(generalFragment, metadataFragment.get(), importFragment);
            } else {
                return newArrayList(generalFragment, importFragment);
            }

        }

        private boolean shouldShowMetadata() {
            final Object model = getContext().get(RobotEditorSources.SUITE_FILE_MODEL);
            if (model instanceof RobotSuiteFile) {
                return !((RobotSuiteFile) model).isResourceFile();
            }
            return true;
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            if(settingsEditorPageSelectionProvider == null) {
                settingsEditorPageSelectionProvider = createSettingsEditorPageSelectionProvider();
            }
            return settingsEditorPageSelectionProvider;
        }

        @Override
        public FocusedViewerAccessor getFocusedViewerAccessor() {
            return null;
        }

        @Override
        public SelectionLayerAccessor getSelectionLayerAccessor() {
            if(settingsEditorPageSelectionProvider == null) {
                settingsEditorPageSelectionProvider = createSettingsEditorPageSelectionProvider();
            }
            return settingsEditorPageSelectionProvider.getSelectionLayerAccessor();
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
    }
}
