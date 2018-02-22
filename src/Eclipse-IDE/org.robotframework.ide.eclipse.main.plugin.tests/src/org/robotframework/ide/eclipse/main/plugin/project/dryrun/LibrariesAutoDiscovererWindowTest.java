/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscovererWindow.DiscoveredLibrariesViewerContentProvider;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscovererWindow.DiscoveredLibrariesViewerLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscovererWindow.DryRunLibraryImportChildElement;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscovererWindow.DryRunLibraryImportListChildElement;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;

public class LibrariesAutoDiscovererWindowTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(LibrariesAutoDiscovererWindowTest.class);

    private static IFile suite1;

    private static IFile suite2;

    private static IFile lib;

    private final DiscoveredLibrariesViewerContentProvider contentProvider = new LibrariesAutoDiscovererWindow.DiscoveredLibrariesViewerContentProvider();

    private final DiscoveredLibrariesViewerLabelProvider labelProvider = new LibrariesAutoDiscovererWindow.DiscoveredLibrariesViewerLabelProvider();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        suite1 = projectProvider.createFile("suite1.robot");
        suite2 = projectProvider.createFile("suite2.robot");
        lib = projectProvider.createFile("PyLib.py");
    }

    @Test
    public void testLibImportComparator() throws Exception {
        final RobotDryRunLibraryImport added = new RobotDryRunLibraryImport("addedLib");
        added.setStatus(DryRunLibraryImportStatus.ADDED);

        final RobotDryRunLibraryImport otherAdded = new RobotDryRunLibraryImport("otherAddedLib");
        otherAdded.setStatus(DryRunLibraryImportStatus.ADDED);

        final RobotDryRunLibraryImport notAdded = new RobotDryRunLibraryImport("notAddedLib");
        notAdded.setStatus(DryRunLibraryImportStatus.NOT_ADDED);

        final RobotDryRunLibraryImport existing = new RobotDryRunLibraryImport("existingLib");
        existing.setStatus(DryRunLibraryImportStatus.ALREADY_EXISTING);

        assertThat(LibrariesAutoDiscovererWindow.LIBRARY_IMPORT_COMPARATOR.compare(added, added)).isZero();
        assertThat(LibrariesAutoDiscovererWindow.LIBRARY_IMPORT_COMPARATOR.compare(notAdded, notAdded)).isZero();
        assertThat(LibrariesAutoDiscovererWindow.LIBRARY_IMPORT_COMPARATOR.compare(existing, existing)).isZero();
        assertThat(LibrariesAutoDiscovererWindow.LIBRARY_IMPORT_COMPARATOR.compare(added, otherAdded)).isNegative();
        assertThat(LibrariesAutoDiscovererWindow.LIBRARY_IMPORT_COMPARATOR.compare(otherAdded, added)).isPositive();
        assertThat(LibrariesAutoDiscovererWindow.LIBRARY_IMPORT_COMPARATOR.compare(added, notAdded)).isNegative();
        assertThat(LibrariesAutoDiscovererWindow.LIBRARY_IMPORT_COMPARATOR.compare(notAdded, added)).isPositive();
        assertThat(LibrariesAutoDiscovererWindow.LIBRARY_IMPORT_COMPARATOR.compare(added, existing)).isNegative();
        assertThat(LibrariesAutoDiscovererWindow.LIBRARY_IMPORT_COMPARATOR.compare(existing, added)).isPositive();
        assertThat(LibrariesAutoDiscovererWindow.LIBRARY_IMPORT_COMPARATOR.compare(notAdded, existing)).isPositive();
        assertThat(LibrariesAutoDiscovererWindow.LIBRARY_IMPORT_COMPARATOR.compare(existing, notAdded)).isNegative();
    }

    @Test
    public void testConvertingToText_forUnknownElement() throws Exception {
        assertThat(LibrariesAutoDiscovererWindow.convertToText(new Object())).isEmpty();
    }

    @Test
    public void testConvertingToText_forChildElementWithLabel() throws Exception {
        assertThat(LibrariesAutoDiscovererWindow.convertToText(new DryRunLibraryImportChildElement("name", "value")))
                .isEqualTo("name value");
    }

    @Test
    public void testConvertingToText_forChildElementWithoutLabel() throws Exception {
        assertThat(LibrariesAutoDiscovererWindow.convertToText(new DryRunLibraryImportChildElement(null, "value")))
                .isEqualTo("value");
    }

    @Test
    public void testConvertingToText_forListChildElement() throws Exception {
        final List<DryRunLibraryImportChildElement> childElements = Arrays.asList(
                new DryRunLibraryImportChildElement(null, "value1"),
                new DryRunLibraryImportChildElement(null, "value2"),
                new DryRunLibraryImportChildElement(null, "value3"),
                new DryRunLibraryImportChildElement(null, "value4"));
        final DryRunLibraryImportListChildElement listChildElement = new DryRunLibraryImportListChildElement("name",
                childElements);

        assertThat(LibrariesAutoDiscovererWindow.convertToText(listChildElement))
                .isEqualTo("name\n" + "value1\n" + "value2\n" + "value3\n" + "value4");
    }

    @Test
    public void testConvertingToText_forLibraryImportWithUnknownStatus() throws Exception {
        final RobotDryRunLibraryImport libImportElement = new RobotDryRunLibraryImport("name");
        libImportElement.setStatus(null);

        assertThat(LibrariesAutoDiscovererWindow.convertToText(libImportElement))
                .isEqualTo("Source: Unknown\n" + "Importers: Unknown");
    }

    @Test
    public void testConvertingToText_forLibraryImportWithKnownStatus() throws Exception {
        final RobotDryRunLibraryImport libImportElement = new RobotDryRunLibraryImport("name");
        libImportElement.setStatus(DryRunLibraryImportStatus.NOT_ADDED);

        assertThat(LibrariesAutoDiscovererWindow.convertToText(libImportElement))
                .isEqualTo("Status: Not added to project configuration\n" + "Source: Unknown\n" + "Importers: Unknown");
    }

    @Test
    public void testConvertingToText_forLibraryImportWithKnownStatusAndSource() throws Exception {
        final RobotDryRunLibraryImport libImportElement = new RobotDryRunLibraryImport("name", lib.getLocationURI());
        libImportElement.setStatus(DryRunLibraryImportStatus.NOT_ADDED);

        assertThat(LibrariesAutoDiscovererWindow.convertToText(libImportElement))
                .isEqualTo("Status: Not added to project configuration\n" + "Source: "
                        + lib.getLocation().toFile().getAbsolutePath() + "\n" + "Importers: Unknown");
    }

    @Test
    public void testConvertingToText_forRemoteLibraryImportWithKnownStatusAndSource() throws Exception {
        final URI remote_uri = URI.create("http://127.0.0.1:9000");
        final RobotDryRunLibraryImport libImportElement = new RobotDryRunLibraryImport("Remote", remote_uri);
        libImportElement.setStatus(DryRunLibraryImportStatus.NOT_ADDED);

        assertThat(LibrariesAutoDiscovererWindow.convertToText(libImportElement))
                .isEqualTo("Status: Not added to project configuration\n" + "Source: "
                        + libImportElement.getSourcePath().toString() + "\n" + "Importers: Unknown");
    }

    @Test
    public void testConvertingToText_forLibraryImportWithSingleImporter() throws Exception {
        final RobotDryRunLibraryImport libImportElement = new RobotDryRunLibraryImport("name");
        libImportElement.setStatus(DryRunLibraryImportStatus.ALREADY_EXISTING);
        libImportElement.addImporterPath(suite1.getLocationURI());

        assertThat(LibrariesAutoDiscovererWindow.convertToText(libImportElement))
                .isEqualTo("Status: Already existing in project configuration\n" + "Source: Unknown\n" + "Importers: "
                        + suite1.getLocation().toFile().getAbsolutePath());
    }

    @Test
    public void testConvertingToText_forLibraryImportWithMultipleImporters() throws Exception {
        final RobotDryRunLibraryImport libImportElement = new RobotDryRunLibraryImport("name");
        libImportElement.setStatus(DryRunLibraryImportStatus.ADDED);
        libImportElement.addImporterPath(suite1.getLocationURI());
        libImportElement.addImporterPath(suite2.getLocationURI());

        assertThat(LibrariesAutoDiscovererWindow.convertToText(libImportElement))
                .isEqualTo("Status: Added to project configuration\n" + "Source: Unknown\n" + "Importers:\n"
                        + suite1.getLocation().toFile().getAbsolutePath() + "\n"
                        + suite2.getLocation().toFile().getAbsolutePath());
    }

    @Test
    public void testConvertingToText_forLibraryImportWithAdditionalInfo() throws Exception {
        final RobotDryRunLibraryImport libImportElement = new RobotDryRunLibraryImport("name");
        libImportElement.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        libImportElement.setAdditionalInfo("some additional info text");

        assertThat(LibrariesAutoDiscovererWindow.convertToText(libImportElement))
                .isEqualTo("Status: Not added to project configuration\n" + "Source: Unknown\n" + "Importers: Unknown\n"
                        + "Additional info: some additional info text");
    }

    @Test
    public void onlyForElementsWithChildren_providerSaysTheyHaveChildren() throws Exception {
        final RobotDryRunLibraryImport libImportElement = new RobotDryRunLibraryImport("name");
        final DryRunLibraryImportChildElement childElement = new DryRunLibraryImportChildElement("name", "value");
        final DryRunLibraryImportListChildElement listChildElement = new DryRunLibraryImportListChildElement("name",
                Arrays.asList(childElement));

        assertThat(contentProvider.hasChildren(libImportElement)).isTrue();
        assertThat(contentProvider.hasChildren(childElement)).isFalse();
        assertThat(contentProvider.hasChildren(listChildElement)).isTrue();
    }

    @Test
    public void nullIsReturned_whenProviderIsAskedForParent() throws Exception {
        final RobotDryRunLibraryImport libImportElement = new RobotDryRunLibraryImport("name");
        final DryRunLibraryImportChildElement childElement = new DryRunLibraryImportChildElement("name", "value");
        final DryRunLibraryImportListChildElement listChildElement = new DryRunLibraryImportListChildElement("name",
                Arrays.asList(childElement));

        assertThat(contentProvider.getParent(libImportElement)).isNull();
        assertThat(contentProvider.getParent(childElement)).isNull();
        assertThat(contentProvider.getParent(listChildElement)).isNull();
    }

    @Test
    public void arrayOfElementsIsReturned_whenProviderIsAskedForInputWithArray() throws Exception {
        final RobotDryRunLibraryImport libImportElement1 = new RobotDryRunLibraryImport("n1");
        final RobotDryRunLibraryImport libImportElement2 = new RobotDryRunLibraryImport("n2");

        assertThat(contentProvider.getElements(new RobotDryRunLibraryImport[] {})).isEmpty();
        assertThat(contentProvider.getElements(new RobotDryRunLibraryImport[] { libImportElement1 }))
                .containsExactly(libImportElement1);
        assertThat(contentProvider.getElements(new RobotDryRunLibraryImport[] { libImportElement1, libImportElement2 }))
                .containsExactly(libImportElement1, libImportElement2);
    }

    @Test
    public void elementChildrenAreProvided_whenProviderIsAskedForChildren() throws Exception {
        final DryRunLibraryImportChildElement childElement1 = new DryRunLibraryImportChildElement("n1", "v1");
        final DryRunLibraryImportChildElement childElement2 = new DryRunLibraryImportChildElement("n2", "v2");
        final DryRunLibraryImportListChildElement listChildElement = new DryRunLibraryImportListChildElement("name",
                Arrays.asList(childElement1, childElement2));
        final RobotDryRunLibraryImport libImportElement = new RobotDryRunLibraryImport("name", lib.getLocationURI());
        libImportElement.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        libImportElement.addImporterPath(suite1.getLocationURI());
        libImportElement.setAdditionalInfo("additional info error");

        assertThat(contentProvider.getChildren(childElement1)).isNull();
        assertThat(contentProvider.getChildren(listChildElement)).containsExactly(childElement1, childElement2);

        final Object[] libImportChildren = contentProvider.getChildren(libImportElement);
        assertThat(libImportChildren).hasSize(4).allMatch(p -> p instanceof DryRunLibraryImportChildElement);
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[0]).getName()).isEqualTo("Status:");
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[0]).getValue())
                .isEqualTo("Not added to project configuration");
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[1]).getName()).isEqualTo("Source:");
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[1]).getValue())
                .isEqualTo(lib.getLocation().toFile().getAbsolutePath());
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[2]).getName()).isEqualTo("Importers:");
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[2]).getValue())
                .isEqualTo(suite1.getLocation().toFile().getAbsolutePath());
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[3]).getName()).isEqualTo("Additional info:");
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[3]).getValue())
                .isEqualTo("additional info error");
    }

    @Test
    public void elementChildrenAreProvided_whenProviderIsAskedForChildrenDuringForRemoteImport() throws Exception {
        final URI remote_uri = URI.create("http://127.0.0.1:9000");
        final RobotDryRunLibraryImport libImportElement = new RobotDryRunLibraryImport("Remote", remote_uri);
        libImportElement.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        libImportElement.addImporterPath(suite1.getLocationURI());
        libImportElement.setAdditionalInfo("additional info error");

        final Object[] libImportChildren = contentProvider.getChildren(libImportElement);
        assertThat(libImportChildren).hasSize(4).allMatch(p -> p instanceof DryRunLibraryImportChildElement);
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[0]).getName()).isEqualTo("Status:");
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[0]).getValue())
                .isEqualTo("Not added to project configuration");
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[1]).getName()).isEqualTo("Source:");
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[1]).getValue())
                .isEqualTo(libImportElement.getSourcePath().toString());
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[2]).getName()).isEqualTo("Importers:");
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[2]).getValue())
                .isEqualTo(suite1.getLocation().toFile().getAbsolutePath());
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[3]).getName()).isEqualTo("Additional info:");
        assertThat(((DryRunLibraryImportChildElement) libImportChildren[3]).getValue())
                .isEqualTo("additional info error");
    }

    @Test
    public void unknownElement_hasEmptyLabelWithoutStyles() throws Exception {
        final StyledString label = labelProvider.getStyledText(new Object());

        assertThat(label.getString()).isEmpty();
        assertThat(label.getStyleRanges()).isEmpty();
    }

    @Test
    public void libImportElement_hasOnlyNameInLabelWithoutStyles() throws Exception {
        final RobotDryRunLibraryImport libImportElement = new RobotDryRunLibraryImport("name");

        final StyledString label = labelProvider.getStyledText(libImportElement);

        assertThat(label.getString()).isEqualTo("name");
        assertThat(label.getStyleRanges()).isEmpty();
    }

    @Test
    public void childElement_hasOnlyValueInLabelWithoutStyles() throws Exception {
        final DryRunLibraryImportChildElement childElement = new DryRunLibraryImportChildElement(null, "value");

        final StyledString label = labelProvider.getStyledText(childElement);

        assertThat(label.getString()).isEqualTo("value");
        assertThat(label.getStyleRanges()).isEmpty();
    }

    @Test
    public void childElement_hasNameAndValueInLabelWithBoldStyleForName() throws Exception {
        final DryRunLibraryImportChildElement childElement = new DryRunLibraryImportChildElement("name", "value");

        final StyledString label = labelProvider.getStyledText(childElement);

        assertThat(label.getString()).isEqualTo("name value");

        final StyleRange[] ranges = label.getStyleRanges();
        assertThat(ranges).hasSize(1);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(4);
        assertThat(ranges[0].font).isEqualTo(FontsManager.transformFontWithStyle(SWT.BOLD));
    }

    @Test
    public void childElement_hasNameAndValueInLabelWithBoldStyleForNameAndHyperlinkStyleForValue() throws Exception {
        final String workspacePath = lib.getLocation().toFile().getAbsolutePath();
        final DryRunLibraryImportChildElement childElement = new DryRunLibraryImportChildElement("name", workspacePath);

        final StyledString label = labelProvider.getStyledText(childElement);

        assertThat(label.getString()).isEqualTo("name " + workspacePath);

        final StyleRange[] ranges = label.getStyleRanges();
        assertThat(ranges).hasSize(2);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(4);
        assertThat(ranges[0].font).isEqualTo(FontsManager.transformFontWithStyle(SWT.BOLD));
        assertThat(ranges[1].start).isEqualTo(5);
        assertThat(ranges[1].length).isEqualTo(workspacePath.length());
        assertThat(ranges[1].foreground).isEqualTo(JFaceColors.getHyperlinkText(Display.getCurrent()));
        assertThat(ranges[1].underline).isTrue();
        assertThat(ranges[1].underlineColor).isEqualTo(JFaceColors.getHyperlinkText(Display.getCurrent()));
    }

    @Test
    public void listChildElement_hasOnlyNameInLabelWithBoldStyle() throws Exception {
        final DryRunLibraryImportListChildElement listChildElement = new DryRunLibraryImportListChildElement("name",
                Arrays.asList());

        final StyledString label = labelProvider.getStyledText(listChildElement);

        assertThat(label.getString()).isEqualTo("name");

        final StyleRange[] ranges = label.getStyleRanges();
        assertThat(ranges).hasSize(1);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(4);
        assertThat(ranges[0].font).isEqualTo(FontsManager.transformFontWithStyle(SWT.BOLD));
    }

    @Test
    public void libImportElementWithoutStatus_hasNoImage() throws Exception {
        final RobotDryRunLibraryImport unknownLibImportElement = new RobotDryRunLibraryImport("name");
        unknownLibImportElement.setStatus(null);

        assertThat(labelProvider.getImage(unknownLibImportElement)).isNull();
    }

    @Test
    public void libImportElementWithAddedStatus_hasBigSuccessImage() {
        final RobotDryRunLibraryImport addedLibImportElement = new RobotDryRunLibraryImport("name");
        addedLibImportElement.setStatus(DryRunLibraryImportStatus.ADDED);

        assertThat(labelProvider.getImage(addedLibImportElement))
                .isSameAs(ImagesManager.getImage(RedImages.getBigSuccessImage()));
    }

    @Test
    public void libImportElementWithNotAddedStatus_hasFatalErrorImage() {
        final RobotDryRunLibraryImport notAddedLibImportElement = new RobotDryRunLibraryImport("name");
        notAddedLibImportElement.setStatus(DryRunLibraryImportStatus.NOT_ADDED);

        assertThat(labelProvider.getImage(notAddedLibImportElement))
                .isSameAs(ImagesManager.getImage(RedImages.getFatalErrorImage()));
    }

    @Test
    public void libImportElementWithAlreadyExistingStatus_hasBigWarningImage() {
        final RobotDryRunLibraryImport existingLibImportElement = new RobotDryRunLibraryImport("name");
        existingLibImportElement.setStatus(DryRunLibraryImportStatus.ALREADY_EXISTING);

        assertThat(labelProvider.getImage(existingLibImportElement))
                .isSameAs(ImagesManager.getImage(RedImages.getBigWarningImage()));
    }

    @Test
    public void childElement_hasElementImage() {
        final DryRunLibraryImportChildElement childElement = new DryRunLibraryImportChildElement("name", "value");

        assertThat(labelProvider.getImage(childElement)).isSameAs(ImagesManager.getImage(RedImages.getElementImage()));
    }

    @Test
    public void listChildElement_hasElementImage() {
        final DryRunLibraryImportListChildElement listChildElement = new DryRunLibraryImportListChildElement("name",
                Arrays.asList());

        assertThat(labelProvider.getImage(listChildElement))
                .isSameAs(ImagesManager.getImage(RedImages.getElementImage()));
    }

}
