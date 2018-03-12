/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.core.resources.IResource;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.red.junit.ProjectProvider;

public class RedXmlForNavigatorPropertyTesterTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedXmlForNavigatorPropertyTesterTest.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final RedXmlForNavigatorPropertyTester tester = new RedXmlForNavigatorPropertyTester();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("excluded_dir");
        projectProvider.createDir("included_dir");
        projectProvider.createDir(".hidden_dir");
        projectProvider.createFile("excluded_dir/file.robot");
        projectProvider.createFile("included_dir/file.robot");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath("excluded_dir");
        projectProvider.configure(config);
    }

    @Test
    public void exceptionIsThrown_whenReceiverIsNotResource() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Property tester is unable to test properties of java.lang.Object. It should be used with "
                + IResource.class.getName());

        tester.test(new Object(), "property", null, true);
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
    public void testIsExcludedProperty() {
        assertThat(isExcluded(projectProvider.getDir("excluded_dir"), true)).isTrue();
        assertThat(isExcluded(projectProvider.getDir("excluded_dir"), false)).isFalse();

        assertThat(isExcluded(projectProvider.getDir("included_dir"), true)).isFalse();
        assertThat(isExcluded(projectProvider.getDir("included_dir"), false)).isTrue();

        assertThat(isExcluded(projectProvider.getFile("excluded_dir/file.robot"), true)).isFalse();
        assertThat(isExcluded(projectProvider.getFile("excluded_dir/file.robot"), false)).isTrue();

        assertThat(isExcluded(projectProvider.getProject(), true)).isFalse();
        assertThat(isExcluded(projectProvider.getProject(), false)).isTrue();
    }

    @Test
    public void testIsInternalFolderProperty() {
        assertThat(isInternalFolder(projectProvider.getDir("excluded_dir"), true)).isTrue();
        assertThat(isInternalFolder(projectProvider.getDir("excluded_dir"), false)).isFalse();

        assertThat(isInternalFolder(projectProvider.getDir("included_dir"), true)).isTrue();
        assertThat(isInternalFolder(projectProvider.getDir("included_dir"), false)).isFalse();

        assertThat(isInternalFolder(projectProvider.getFile("excluded_dir/file.robot"), true)).isFalse();
        assertThat(isInternalFolder(projectProvider.getFile("excluded_dir/file.robot"), false)).isTrue();

        assertThat(isInternalFolder(projectProvider.getProject(), true)).isFalse();
        assertThat(isInternalFolder(projectProvider.getProject(), false)).isTrue();
    }

    @Test
    public void testIsFileProperty() {
        assertThat(isFile(projectProvider.getDir("excluded_dir"), false)).isTrue();
        assertThat(isFile(projectProvider.getDir("excluded_dir"), true)).isFalse();

        assertThat(isFile(projectProvider.getDir("included_dir"), false)).isTrue();
        assertThat(isFile(projectProvider.getDir("included_dir"), true)).isFalse();

        assertThat(isFile(projectProvider.getFile("excluded_dir/file.robot"), true)).isTrue();
        assertThat(isFile(projectProvider.getFile("excluded_dir/file.robot"), false)).isFalse();

        assertThat(isFile(projectProvider.getProject(), true)).isFalse();
        assertThat(isFile(projectProvider.getProject(), false)).isTrue();
    }

    @Test
    public void testIsExcludedViaInheritanceProperty() {
        assertThat(isExcludedViaInheritance(projectProvider.getDir("excluded_dir"), false)).isTrue();
        assertThat(isExcludedViaInheritance(projectProvider.getDir("excluded_dir"), true)).isFalse();

        assertThat(isExcludedViaInheritance(projectProvider.getDir("included_dir"), false)).isTrue();
        assertThat(isExcludedViaInheritance(projectProvider.getDir("included_dir"), true)).isFalse();

        assertThat(isExcludedViaInheritance(projectProvider.getFile("excluded_dir/file.robot"), true)).isTrue();
        assertThat(isExcludedViaInheritance(projectProvider.getFile("excluded_dir/file.robot"), false)).isFalse();

        assertThat(isExcludedViaInheritance(projectProvider.getFile("included_dir/file.robot"), true)).isFalse();
        assertThat(isExcludedViaInheritance(projectProvider.getFile("included_dir/file.robot"), false)).isTrue();

        assertThat(isExcludedViaInheritance(projectProvider.getProject(), true)).isFalse();
        assertThat(isExcludedViaInheritance(projectProvider.getProject(), false)).isTrue();
    }

    @Test
    public void testIsProjectProperty() {
        assertThat(isProject(projectProvider.getProject(), true)).isTrue();
        assertThat(isProject(projectProvider.getProject(), false)).isFalse();

        assertThat(isProject(projectProvider.getDir("included_dir"), false)).isTrue();
        assertThat(isProject(projectProvider.getDir("included_dir"), true)).isFalse();

        assertThat(isProject(projectProvider.getFile("excluded_dir/file.robot"), true)).isFalse();
        assertThat(isProject(projectProvider.getFile("excluded_dir/file.robot"), false)).isTrue();
    }

    @Test
    public void testIsHiddenProperty() {
        assertThat(isHidden(projectProvider.getDir(".hidden_dir"), true)).isTrue();
        assertThat(isHidden(projectProvider.getDir(".hidden_dir"), false)).isFalse();

        assertThat(isHidden(projectProvider.getProject(), true)).isFalse();
        assertThat(isHidden(projectProvider.getProject(), false)).isTrue();

        assertThat(isHidden(projectProvider.getDir("included_dir"), true)).isFalse();
        assertThat(isHidden(projectProvider.getDir("included_dir"), false)).isTrue();

        assertThat(isHidden(projectProvider.getFile("excluded_dir/file.robot"), true)).isFalse();
        assertThat(isHidden(projectProvider.getFile("excluded_dir/file.robot"), false)).isTrue();
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
