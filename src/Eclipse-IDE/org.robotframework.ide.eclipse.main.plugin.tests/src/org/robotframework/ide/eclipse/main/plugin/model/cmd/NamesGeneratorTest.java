package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.NamedElement;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.ParentElement;


public class NamesGeneratorTest {

    @Test
    public void givenNameIsReturnedAsGenerated_whenThereAreNoOtherChildren() {
        final ParentElement parent = new ParentElement();

        assertThat(NamesGenerator.generateUniqueName(parent, "name")).isEqualTo("name");
    }

    @Test
    public void givenNameIsReturnedAsGenerated_whenChildrenHaveDifferentName() {
        final ParentElement parent = new ParentElement(new NamedElement("suchCoolName"));

        assertThat(NamesGenerator.generateUniqueName(parent, "name")).isEqualTo("name");
    }

    @Test
    public void generatedNameHave1AsSuffix_whenThereIsAChildWithTheSameName() {
        final ParentElement parent = new ParentElement(new NamedElement("name"));

        assertThat(NamesGenerator.generateUniqueName(parent, "name")).isEqualTo("name 1");
    }

    @Test
    public void generatedNameHaveConsecutiveNumber_whenThereAreAlreadyNumberedChildren_1() {
        final ParentElement parent = new ParentElement(
                new NamedElement("name"), 
                new NamedElement("name 1"), 
                new NamedElement("name 2"));

        assertThat(NamesGenerator.generateUniqueName(parent, "name")).isEqualTo("name 3");
    }

    @Test
    public void generatedNameHaveConsecutiveNumber_whenThereAreAlreadyNumberedChildren_2() {
        final ParentElement parent = new ParentElement(
                new NamedElement("name"), 
                new NamedElement("name 100"), 
                new NamedElement("name 200"));

        assertThat(NamesGenerator.generateUniqueName(parent, "name")).isEqualTo("name 201");
    }

}
