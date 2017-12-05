package org.robotframework.ide.eclipse.main.plugin.navigator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.EnumSet;

import org.eclipse.jface.viewers.Viewer;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.NamedElement;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.ParentElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;

public class SettingsSectionSorterTest {

    private final SettingsSectionSorter sorter = new SettingsSectionSorter();

    @Test
    public void categoryIsAssignedOnlyForArtificialGroupingElements() throws Exception {
        assertThat(sorter.category(mock(RobotProject.class))).isZero();
        assertThat(sorter.category(mock(RobotFolder.class))).isZero();
        assertThat(sorter.category(mock(RobotSuiteFile.class))).isZero();
        assertThat(sorter.category(mock(RobotSuiteFileSection.class))).isZero();
        assertThat(sorter.category(mock(RobotCasesSection.class))).isZero();
        assertThat(sorter.category(mock(RobotKeywordsSection.class))).isZero();
        assertThat(sorter.category(mock(RobotSettingsSection.class))).isZero();
        assertThat(sorter.category(mock(RobotVariablesSection.class))).isZero();
        assertThat(sorter.category(mock(RobotKeywordCall.class))).isZero();
        assertThat(sorter.category(mock(RobotDefinitionSetting.class))).isZero();
        assertThat(sorter.category(mock(RobotSetting.class))).isZero();
        assertThat(sorter.category(mock(RobotCase.class))).isZero();
        assertThat(sorter.category(mock(RobotKeywordDefinition.class))).isZero();
        assertThat(sorter.category(mock(RobotVariable.class))).isZero();

        assertThat(sorter.category(newGroup(SettingsGroup.METADATA))).isEqualTo(1);
        assertThat(sorter.category(newGroup(SettingsGroup.NO_GROUP))).isEqualTo(1);
        assertThat(sorter.category(newGroup(SettingsGroup.LIBRARIES))).isEqualTo(2);
        assertThat(sorter.category(newGroup(SettingsGroup.RESOURCES))).isEqualTo(2);
        assertThat(sorter.category(newGroup(SettingsGroup.VARIABLES))).isEqualTo(2);
    }

    @Test
    public void ordinarySettingIsAlwaysBeforeGroup() {
        for (final SettingsGroup group : EnumSet.allOf(SettingsGroup.class)) {
            final ParentElement parent = new ParentElement();

            final RobotElement e1 = new NamedElement(parent, "e1");
            final RobotElement e2 = newGroup(group);

            parent.getChildren().add(e1);
            parent.getChildren().add(e2);

            assertThat(sorter.compare(mock(Viewer.class), e1, e2)).isNegative();
        }
    }

    private static ArtificialGroupingRobotElement newGroup(final SettingsGroup group) {
        return new ArtificialGroupingRobotElement(group, new ArrayList<RobotFileInternalElement>());
    }
}
