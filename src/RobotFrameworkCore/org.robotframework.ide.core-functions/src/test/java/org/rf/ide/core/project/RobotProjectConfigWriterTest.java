package org.rf.ide.core.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.rf.ide.core.project.RobotProjectConfigReader.RobotProjectConfigWithLines;

public class RobotProjectConfigWriterTest {

    private static final VariableMapping MAPPING_WITH_SPECIAL_CHARS = VariableMapping.create("${var}", "ęóąśłżń");

    private final RobotProjectConfigWriter writer = new RobotProjectConfigWriter();

    private final RobotProjectConfigReader reader = new RobotProjectConfigReader();

    @BeforeClass
    public static void setup() throws Exception {
        setDefaultCharset(StandardCharsets.ISO_8859_1);
    }

    @AfterClass
    public static void teardown() throws Exception {
        setDefaultCharset(StandardCharsets.UTF_8);
    }

    @Test
    public void testIfConfigurationIsRead_whenDefaultCharsetIsDifferentThanUTF8() throws Exception {
        final RobotProjectConfig writeConfig = new RobotProjectConfig();
        writeConfig.setVariableMappings(Arrays.asList(MAPPING_WITH_SPECIAL_CHARS));
        final InputStream stream = writer.writeConfiguration(writeConfig);

        final RobotProjectConfig readConfig = reader.readConfiguration(stream);
        assertThat(readConfig.getVariableMappings()).containsOnly(MAPPING_WITH_SPECIAL_CHARS);
    }

    @Test
    public void testIfConfigurationWithLinesIsRead_whenDefaultCharsetIsDifferentThanUTF8() throws Exception {
        final RobotProjectConfig writeConfig = new RobotProjectConfig();
        writeConfig.setVariableMappings(Arrays.asList(MAPPING_WITH_SPECIAL_CHARS));
        final InputStream stream = writer.writeConfiguration(writeConfig);

        final RobotProjectConfigWithLines readConfig = reader.readConfigurationWithLines(stream);
        assertThat(readConfig.getConfigurationModel().getVariableMappings()).containsOnly(MAPPING_WITH_SPECIAL_CHARS);
    }

    private static void setDefaultCharset(final Charset charset) throws Exception {
        // reflection used for the purpose of testing only
        final Field defaultCharset = Charset.class.getDeclaredField("defaultCharset");
        defaultCharset.setAccessible(true);
        defaultCharset.set(null, charset);
        defaultCharset.setAccessible(false);
    }

}
