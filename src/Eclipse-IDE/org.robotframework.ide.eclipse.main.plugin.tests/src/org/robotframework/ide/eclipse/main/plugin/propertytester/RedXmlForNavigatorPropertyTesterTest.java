/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.mock;
import static org.robotframework.red.junit.jupiter.ProjectExtension.configure;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getDir;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class RedXmlForNavigatorPropertyTesterTest {

    @Project(dirs = { "excluded_dir", "included_dir", ".hidden_dir" },
            files = { "excluded_dir/file.robot", "included_dir/file.robot" })
    static IProject configuredProject;

    @Project(nameSuffix = "NoConfig", files = { "file.robot" })
    static IProject notConfiguredProject;

    private final RedXmlForNavigatorPropertyTester tester = new RedXmlForNavigatorPropertyTester();

    @BeforeAll
    public static void beforeSuite() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath("excluded_dir");
        configure(configuredProject, config);

        createFile(notConfiguredProject, "file.robot");
    }

    @Test
    public void exceptionIsThrown_whenReceiverIsNotResource() {
        assertThatIllegalArgumentException().isThrownBy(() -> tester.test(new Object(), "property", null, true))
                .withMessage("Property tester is unable to test properties of java.lang.Object. It should be used with "
                        + IResource.class.getName())
                .withNoCause();
    }

    @Test
    public void falseIsReturned_whenExpectedValueIsAString() {
        final boolean testResult = tester.test(mock(IResource.class), RedXmlForNavigatorPropertyTester.IS_EXCLUDED,
                null, "value");

        assertThat(testResult).isFalse();
    }

    @Test
    public void falseIsReturnedForUnknownProperty() {
        assertThat(tester.test(mock(IResource.class), "unknown_property", null, true)).isFalse();
        assertThat(tester.test(mock(IResource.class), "unknown_property", null, false)).isFalse();
    }

    @Test
    public void testIsApplicableProperty() {
        assertThat(isApplicable(getFile(configuredProject, "excluded_dir/file.robot"), true)).isTrue();
        assertThat(isApplicable(getFile(configuredProject, "excluded_dir/file.robot"), false)).isFalse();

        assertThat(isApplicable(getFile(notConfiguredProject, "file.robot"), true)).isFalse();
        assertThat(isApplicable(getFile(notConfiguredProject, "file.robot"), false)).isTrue();
    }

    @Test
    public void testIsExcludedProperty() {
        assertThat(isExcluded(getDir(configuredProject, "excluded_dir"), true)).isTrue();
        assertThat(isExcluded(getDir(configuredProject, "excluded_dir"), false)).isFalse();

        assertThat(isExcluded(getDir(configuredProject, "included_dir"), true)).isFalse();
        assertThat(isExcluded(getDir(configuredProject, "included_dir"), false)).isTrue();

        assertThat(isExcluded(getFile(configuredProject, "excluded_dir/file.robot"), true)).isFalse();
        assertThat(isExcluded(getFile(configuredProject, "excluded_dir/file.robot"), false)).isTrue();

        assertThat(isExcluded(configuredProject, true)).isFalse();
        assertThat(isExcluded(configuredProject, false)).isTrue();
    }

    @Test
    public void testIsInternalFolderProperty() {
        assertThat(isInternalFolder(getDir(configuredProject, "excluded_dir"), true)).isTrue();
        assertThat(isInternalFolder(getDir(configuredProject, "excluded_dir"), false)).isFalse();

        assertThat(isInternalFolder(getDir(configuredProject, "included_dir"), true)).isTrue();
        assertThat(isInternalFolder(getDir(configuredProject, "included_dir"), false)).isFalse();

        assertThat(isInternalFolder(getFile(configuredProject, "excluded_dir/file.robot"), true)).isFalse();
        assertThat(isInternalFolder(getFile(configuredProject, "excluded_dir/file.robot"), false)).isTrue();

        assertThat(isInternalFolder(configuredProject, true)).isFalse();
        assertThat(isInternalFolder(configuredProject, false)).isTrue();
    }

    @Test
    public void testIsFileProperty() {
        assertThat(isFile(getDir(configuredProject, "excluded_dir"), false)).isTrue();
        assertThat(isFile(getDir(configuredProject, "excluded_dir"), true)).isFalse();

        assertThat(isFile(getDir(configuredProject, "included_dir"), false)).isTrue();
        assertThat(isFile(getDir(configuredProject, "included_dir"), true)).isFalse();

        assertThat(isFile(getFile(configuredProject, "excluded_dir/file.robot"), true)).isTrue();
        assertThat(isFile(getFile(configuredProject, "excluded_dir/file.robot"), false)).isFalse();

        assertThat(isFile(configuredProject, true)).isFalse();
        assertThat(isFile(configuredProject, false)).isTrue();
    }

    @Test
    public void testIsExcludedViaInheritanceProperty() {
        assertThat(isExcludedViaInheritance(getDir(configuredProject, "excluded_dir"), false)).isTrue();
        assertThat(isExcludedViaInheritance(getDir(configuredProject, "excluded_dir"), true)).isFalse();

        assertThat(isExcludedViaInheritance(getDir(configuredProject, "included_dir"), false)).isTrue();
        assertThat(isExcludedViaInheritance(getDir(configuredProject, "included_dir"), true)).isFalse();

        assertThat(isExcludedViaInheritance(getFile(configuredProject, "excluded_dir/file.robot"), true)).isTrue();
        assertThat(isExcludedViaInheritance(getFile(configuredProject, "excluded_dir/file.robot"), false)).isFalse();

        assertThat(isExcludedViaInheritance(getFile(configuredProject, "included_dir/file.robot"), true)).isFalse();
        assertThat(isExcludedViaInheritance(getFile(configuredProject, "included_dir/file.robot"), false)).isTrue();

        assertThat(isExcludedViaInheritance(configuredProject, true)).isFalse();
        assertThat(isExcludedViaInheritance(configuredProject, false)).isTrue();
    }

    @Test
    public void testIsProjectProperty() {
        assertThat(isProject(configuredProject, true)).isTrue();
        assertThat(isProject(configuredProject, false)).isFalse();

        assertThat(isProject(getDir(configuredProject, "included_dir"), false)).isTrue();
        assertThat(isProject(getDir(configuredProject, "included_dir"), true)).isFalse();

        assertThat(isProject(getFile(configuredProject, "excluded_dir/file.robot"), true)).isFalse();
        assertThat(isProject(getFile(configuredProject, "excluded_dir/file.robot"), false)).isTrue();
    }

    @Test
    public void testIsHiddenProperty() {
        assertThat(isHidden(getDir(configuredProject, ".hidden_dir"), true)).isTrue();
        assertThat(isHidden(getDir(configuredProject, ".hidden_dir"), false)).isFalse();

        assertThat(isHidden(configuredProject, true)).isFalse();
        assertThat(isHidden(configuredProject, false)).isTrue();

        assertThat(isHidden(getDir(configuredProject, "included_dir"), true)).isFalse();
        assertThat(isHidden(getDir(configuredProject, "included_dir"), false)).isTrue();

        assertThat(isHidden(getFile(configuredProject, "excluded_dir/file.robot"), true)).isFalse();
        assertThat(isHidden(getFile(configuredProject, "excluded_dir/file.robot"), false)).isTrue();
    }

    private boolean isApplicable(final IResource element, final boolean expected) {
        return tester.test(element, RedXmlForNavigatorPropertyTester.IS_APPLICABLE, null, expected);
    }

    private boolean isExcluded(final IResource element, final boolean expected) {
        return tester.test(element, RedXmlForNavigatorPropertyTester.IS_EXCLUDED, null, expected);
    }

    private boolean isInternalFolder(final IResource element, final boolean expected) {
        return tester.test(element, RedXmlForNavigatorPropertyTester.IS_INTERNAL_FOLDER, null, expected);
    }

    private boolean isFile(final IResource element, final boolean expected) {
        return tester.test(element, RedXmlForNavigatorPropertyTester.IS_FILE, null, expected);
    }

    private boolean isExcludedViaInheritance(final IResource element, final boolean expected) {
        return tester.test(element, RedXmlForNavigatorPropertyTester.PARENT_EXCLUDED, null, expected);
    }

    private boolean isProject(final IResource element, final boolean expected) {
        return tester.test(element, RedXmlForNavigatorPropertyTester.IS_PROJECT, null, expected);
    }

    private boolean isHidden(final IResource element, final boolean expected) {
        return tester.test(element, RedXmlForNavigatorPropertyTester.IS_HIDDEN, null, expected);
    }
}
