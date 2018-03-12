/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.propertytester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.robotframework.ide.eclipse.main.plugin.project.editor.validation.ProjectTreeElement;
import org.robotframework.red.junit.ProjectProvider;

public class RedXmlValidationPropertyTesterTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RedXmlValidationPropertyTesterTest.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final RedXmlValidationPropertyTester tester = new RedXmlValidationPropertyTester();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("dir");
        projectProvider.createFile("file");
    }

    @Test
    public void exceptionIsThrown_whenReceiverIsNotProjectTreeElement() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Property tester is unable to test properties of java.lang.Object. It should be used with "
                + ProjectTreeElement.class.getName());

        tester.test(new Object(), "property", null, true);
    }

    @Test
    public void falseIsReturned_whenExpectedValueIsAString() {
        final boolean testResult = tester.test(mock(ProjectTreeElement.class),
                RedXmlValidationPropertyTester.IS_EXCLUDED, null, "value");

        assertThat(testResult).isFalse();
    }

    @Test
    public void falseIsReturnedForUnknownProperty() {
        assertThat(tester.test(mock(ProjectTreeElement.class), "unknown_property", null, true)).isFalse();
        assertThat(tester.test(mock(ProjectTreeElement.class), "unknown_property", null, false)).isFalse();
    }

    @Test
    public void testIsExcludedProperty() {
        final ProjectTreeElement excludedElement = new ProjectTreeElement(null, true);
        final ProjectTreeElement notExcludedElement = new ProjectTreeElement(null, false);

        assertThat(isExcluded(excludedElement, true)).isTrue();
        assertThat(isExcluded(excludedElement, false)).isFalse();

        assertThat(isExcluded(notExcludedElement, true)).isFalse();
        assertThat(isExcluded(notExcludedElement, false)).isTrue();
    }

    @Test
    public void testIsInternalFolderProperty() {
        final ProjectTreeElement folderElement = new ProjectTreeElement(projectProvider.getDir("dir"), false);
        final ProjectTreeElement nonFolderElement = new ProjectTreeElement(projectProvider.getFile("file"), false);

        assertThat(isInternalFolder(folderElement, true)).isTrue();
        assertThat(isInternalFolder(folderElement, false)).isFalse();

        assertThat(isInternalFolder(nonFolderElement, true)).isFalse();
        assertThat(isInternalFolder(nonFolderElement, false)).isTrue();
    }

    @Test
    public void testIsFileProperty() {
        final ProjectTreeElement fileElement = new ProjectTreeElement(projectProvider.getFile("file"), false);
        final ProjectTreeElement notFileElement = new ProjectTreeElement(projectProvider.getDir("dir"), false);

        assertThat(isFile(fileElement, true)).isTrue();
        assertThat(isFile(fileElement, false)).isFalse();

        assertThat(isFile(notFileElement, true)).isFalse();
        assertThat(isFile(notFileElement, false)).isTrue();
    }

    @Test
    public void testIsExcludedViaInheritanceProperty() {
        final ProjectTreeElement rootElement = new ProjectTreeElement(null, false);
        final ProjectTreeElement excludedElement = new ProjectTreeElement(null, true);
        final ProjectTreeElement notExcludedElement = new ProjectTreeElement(null, false);
        final ProjectTreeElement excludedViaInheritanceElement = new ProjectTreeElement(null, false);
        final ProjectTreeElement notExcludedViaInheritanceElement = new ProjectTreeElement(null, false);
        rootElement.addChild(excludedElement);
        rootElement.addChild(notExcludedElement);
        excludedElement.addChild(excludedViaInheritanceElement);
        notExcludedElement.addChild(notExcludedViaInheritanceElement);

        assertThat(isExcludedViaInheritance(excludedViaInheritanceElement, true)).isTrue();
        assertThat(isExcludedViaInheritance(excludedViaInheritanceElement, false)).isFalse();

        assertThat(isExcludedViaInheritance(notExcludedViaInheritanceElement, true)).isFalse();
        assertThat(isExcludedViaInheritance(notExcludedViaInheritanceElement, false)).isTrue();

        assertThat(isExcludedViaInheritance(excludedElement, true)).isTrue();
        assertThat(isExcludedViaInheritance(excludedElement, false)).isFalse();

        assertThat(isExcludedViaInheritance(notExcludedElement, true)).isFalse();
        assertThat(isExcludedViaInheritance(notExcludedElement, false)).isTrue();
    }

    @Test
    public void testIsProjectProperty() {
        final ProjectTreeElement projectElement = new ProjectTreeElement(projectProvider.getProject(), false);
        final ProjectTreeElement notProjectElement = new ProjectTreeElement(projectProvider.getFile("file"), false);

        assertThat(isProject(projectElement, true)).isTrue();
        assertThat(isProject(projectElement, false)).isFalse();

        assertThat(isProject(notProjectElement, true)).isFalse();
        assertThat(isProject(notProjectElement, false)).isTrue();
    }

    private boolean isExcluded(final ProjectTreeElement element, final boolean expected) {
        return tester.test(element, RedXmlValidationPropertyTester.IS_EXCLUDED, null, expected);
    }

    private boolean isInternalFolder(final ProjectTreeElement element, final boolean expected) {
        return tester.test(element, RedXmlValidationPropertyTester.IS_INTERNAL_FOLDER, null, expected);
    }

    private boolean isFile(final ProjectTreeElement element, final boolean expected) {
        return tester.test(element, RedXmlValidationPropertyTester.IS_FILE, null, expected);
    }

    private boolean isExcludedViaInheritance(final ProjectTreeElement element, final boolean expected) {
        return tester.test(element, RedXmlValidationPropertyTester.PARENT_EXCLUDED, null, expected);
    }

    private boolean isProject(final ProjectTreeElement element, final boolean expected) {
        return tester.test(element, RedXmlValidationPropertyTester.IS_PROJECT, null, expected);
    }
}
