package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.DISectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SectionEditorPart;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsEditorPart.SettingsEditor;

import com.google.common.base.Optional;

public class SettingsEditorPart extends DISectionEditorPart<SettingsEditor> {

    public SettingsEditorPart() {
        super(SettingsEditor.class);
        setTitleImage(RobotImages.getRobotSettingImage().createImage());
    }

    public static class SettingsEditor extends SectionEditorPart {

        private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.tableeditor.settings.context";

        private GeneralSettingsFormFragment generalFragment;

        private MetadataSettingsFormFragment metadataFragment;

        private ImportSettingsFormFragment importFragment;

        @Override
        protected String getContextId() {
            return CONTEXT_ID;
        }

        @Override
        protected String getTitle() {
            return "Settings";
        }

        @Override
        protected String getSectionName() {
            return RobotSuiteSettingsSection.SECTION_NAME;
        }

        @Override
        public boolean isPartFor(final RobotSuiteFileSection section) {
            return section instanceof RobotSuiteSettingsSection;
        }

        @Override
        public void revealElement(final RobotElement robotElement) {
            final RobotSetting setting = (RobotSetting) robotElement;
            if (setting.getGroup() == SettingsGroup.NO_GROUP) {
                generalFragment.revealSetting(setting);
                metadataFragment.clearSettingsSelection();
                importFragment.clearSettingsSelection();
            } else if (setting.getGroup() == SettingsGroup.METADATA) {
                generalFragment.clearSettingsSelection();
                metadataFragment.revealSetting(setting);
                importFragment.clearSettingsSelection();
            } else if (SettingsGroup.getImportsGroupsSet().contains(setting.getGroup())) {
                generalFragment.clearSettingsSelection();
                metadataFragment.clearSettingsSelection();
                importFragment.revealSetting(setting);
            }
        }

        @Override
        public Optional<RobotElement> provideSection(final RobotSuiteFile suiteModel) {
            return suiteModel.findSection(RobotSuiteSettingsSection.class);
        }

        @Override
        protected List<? extends ISectionFormFragment> createFormFragments() {
            generalFragment = new GeneralSettingsFormFragment();
            metadataFragment = new MetadataSettingsFormFragment();
            importFragment = new ImportSettingsFormFragment();
            return newArrayList(generalFragment, metadataFragment, importFragment);
        }

        @Override
        protected ISelectionProvider getSelectionProvider() {
            return new SettingsEditorPageSelectionProvider(generalFragment.getViewer(), metadataFragment.getViewer(),
                    importFragment.getViewer());
        }
    }
}
