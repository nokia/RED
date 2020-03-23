/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.libraries;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringReader;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class KeywordSpecificationTest {

    @DisplayName("when deprecation attribute is specified it is intialized without looking into documentation")
    @Test
    public void deprecationIsReadFromAttributeTest() {
        final KeywordSpecification spec1 = read(xmlSpec("keyword", Optional.of(Boolean.TRUE), ""));
        final KeywordSpecification spec2 = read(xmlSpec("keyword", Optional.of(Boolean.FALSE), ""));
        final KeywordSpecification spec3 = read(
                xmlSpec("keyword", Optional.of(Boolean.TRUE), "*DEPRECATED* because is too old"));
        final KeywordSpecification spec4 = read(
                xmlSpec("keyword", Optional.of(Boolean.FALSE), "*DEPRECATED* because is too old"));

        assertThat(spec1.getDeprecatedState()).isNotNull().isEqualTo(Boolean.TRUE);
        assertThat(spec1.isDeprecated()).isTrue();

        assertThat(spec2.getDeprecatedState()).isNotNull().isEqualTo(Boolean.FALSE);
        assertThat(spec2.isDeprecated()).isFalse();

        assertThat(spec3.getDeprecatedState()).isNotNull().isEqualTo(Boolean.TRUE);
        assertThat(spec3.isDeprecated()).isTrue();

        assertThat(spec4.getDeprecatedState()).isNotNull().isEqualTo(Boolean.FALSE);
        assertThat(spec4.isDeprecated()).isFalse();
    }

    @DisplayName("when deprecation attribute is not specified the flag is read from documentation")
    @Test
    public void deprecationIsReadFromDocumentationTest() {
        final KeywordSpecification spec1 = read(xmlSpec("keyword", Optional.empty(), ""));
        final KeywordSpecification spec2 = read(
                xmlSpec("keyword", Optional.empty(), "*DEPRECATED* because is too old"));

        assertThat(spec1.getDeprecatedState()).isNull();
        assertThat(spec1.isDeprecated()).isFalse();
        assertThat(spec1.getDeprecatedState()).isNotNull().isEqualTo(Boolean.FALSE);

        assertThat(spec2.getDeprecatedState()).isNull();
        assertThat(spec2.isDeprecated()).isTrue();
        assertThat(spec2.getDeprecatedState()).isNotNull().isEqualTo(Boolean.TRUE);
    }

    private static String xmlSpec(final String name, final Optional<Boolean> deprecated, final String documentation) {
        final StringBuilder content = new StringBuilder();
        content.append("<kw");
        content.append(" name=\"" + name + "\"");
        if (deprecated.isPresent()) {
            content.append(" deprecated=\"" + deprecated.get().toString().toLowerCase() + "\"");
        }
        content.append(">");
        content.append("  <doc>" + documentation + "</doc>");
        content.append("</kw>");
        return content.toString();
    }

    private static KeywordSpecification read(final String xmlContent) {
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(KeywordSpecification.class);
            final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (KeywordSpecification) jaxbUnmarshaller.unmarshal(new StringReader(xmlContent));
        } catch (final JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
