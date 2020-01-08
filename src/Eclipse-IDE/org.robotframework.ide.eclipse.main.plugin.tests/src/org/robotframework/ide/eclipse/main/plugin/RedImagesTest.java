/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class RedImagesTest {

    @Test
    public void templateImageIsDefined() throws Exception {
        assertThat(RedImages.getTemplateImage()).isNotNull();
    }

    @Test
    public void templatedKeywordImageIsDefined() throws Exception {
        assertThat(RedImages.getTemplatedKeywordImage()).isNotNull();
    }

}
