package org.robotframework.ide.core.testData.parser.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Test;


/**
 * 
 * @author wypych
 * @see ByteBufferReader
 */
public class TestByteBufferReader {

    private ByteBufferReader bbr;
    private final String WINDOWS_SEPARATOR = "\r\n";
    private final String LINUX_SEPARATOR = "\n";


    @Test
    public void test_readLine_markWithoutBounder_firstLineMark_and_afterSecondLineMark()
            throws IOException {
        // prepare
        String[] lines = { "firstLine", "secondLine", "thirdLine" };

        StringBuilder strBuilder = new StringBuilder();
        for (String line : lines) {
            if (line.equals(lines[lines.length - 1])) {
                strBuilder.append(line);
            } else {
                strBuilder.append(line).append(LINUX_SEPARATOR);
            }
        }

        ByteBuffer bb = ByteBuffer.wrap(strBuilder.toString().getBytes());
        bbr = new ByteBufferReader(bb, "UTF-8");

        // execute & verify
        bbr.mark();

        String text = bbr.readLine();
        assertThat(text).isEqualTo(lines[0]);
        bbr.mark(); // mark 2
        text = bbr.readLine();
        assertThat(text).isEqualTo(lines[1]);
        bbr.reset();

        // after mark 2, the position should be equal to length of first line
        // plus OS separator
        assertThat(bb.position()).isEqualTo(
                lines[0].length() + LINUX_SEPARATOR.length());
    }


    @Test
    public void test_readLine_markWithoutBounder_firstLineMark()
            throws IOException {
        // prepare
        String[] lines = { "firstLine", "secondLine", "thirdLine" };

        StringBuilder strBuilder = new StringBuilder();
        for (String line : lines) {
            if (line.equals(lines[lines.length - 1])) {
                strBuilder.append(line);
            } else {
                strBuilder.append(line).append(LINUX_SEPARATOR);
            }
        }

        ByteBuffer bb = ByteBuffer.wrap(strBuilder.toString().getBytes());
        bbr = new ByteBufferReader(bb, "UTF-8");

        bbr.mark();

        // execute & verify
        for (String line : lines) {
            String text = bbr.readLine();
            assertThat(text).isEqualTo(line);
        }

        assertThat(bb.position()).isEqualTo(bb.capacity());
        bbr.reset();
        assertThat(bb.position()).isEqualTo(0);
    }


    @Test
    public void test_readLine_by_line_inLinux_lastLine_noEnterAfterLast()
            throws IOException {
        // prepare
        String[] lines = { "firstLine", "secondLine", "thirdLine" };

        StringBuilder strBuilder = new StringBuilder();
        for (String line : lines) {
            if (line.equals(lines[lines.length - 1])) {
                strBuilder.append(line);
            } else {
                strBuilder.append(line).append(LINUX_SEPARATOR);
            }
        }

        ByteBuffer bb = ByteBuffer.wrap(strBuilder.toString().getBytes());
        bbr = new ByteBufferReader(bb, "UTF-8");

        // execute & verify
        for (String line : lines) {
            String text = bbr.readLine();
            assertThat(text).isEqualTo(line);
        }
    }


    @Test
    public void test_readLine_by_line_inLinux_lastLineIsEmpty()
            throws IOException {
        // prepare
        String[] lines = { "firstLine", "secondLine", "thirdLine", "" };

        StringBuilder strBuilder = new StringBuilder();
        for (String line : lines) {
            strBuilder.append(line).append(LINUX_SEPARATOR);
        }

        ByteBuffer bb = ByteBuffer.wrap(strBuilder.toString().getBytes());
        bbr = new ByteBufferReader(bb, "UTF-8");

        // execute & verify
        for (String line : lines) {
            String text = bbr.readLine();
            assertThat(text).isEqualTo(line);
        }
    }


    @Test
    public void test_readLine_by_line_inLinux() throws IOException {
        // prepare
        String[] lines = { "firstLine", "secondLine", "thirdLine" };

        StringBuilder strBuilder = new StringBuilder();
        for (String line : lines) {
            strBuilder.append(line).append(LINUX_SEPARATOR);
        }

        ByteBuffer bb = ByteBuffer.wrap(strBuilder.toString().getBytes());
        bbr = new ByteBufferReader(bb, "UTF-8");

        // execute & verify
        for (String line : lines) {
            String text = bbr.readLine();
            assertThat(text).isEqualTo(line);
        }
    }


    @Test
    public void test_readLine_by_line_inWindows_lastLine_noEnterAfterLast()
            throws IOException {
        // prepare
        String[] lines = { "firstLine", "secondLine", "thirdLine" };

        StringBuilder strBuilder = new StringBuilder();
        for (String line : lines) {
            if (line.equals(lines[lines.length - 1])) {
                strBuilder.append(line);
            } else {
                strBuilder.append(line).append(WINDOWS_SEPARATOR);
            }
        }

        ByteBuffer bb = ByteBuffer.wrap(strBuilder.toString().getBytes());
        bbr = new ByteBufferReader(bb, "UTF-8");

        // execute & verify
        for (String line : lines) {
            String text = bbr.readLine();
            assertThat(text).isEqualTo(line);
        }
    }


    @Test
    public void test_readLine_by_line_inWindows_lastLineIsEmpty()
            throws IOException {
        // prepare
        String[] lines = { "firstLine", "secondLine", "thirdLine", "" };

        StringBuilder strBuilder = new StringBuilder();
        for (String line : lines) {
            strBuilder.append(line).append(WINDOWS_SEPARATOR);
        }

        ByteBuffer bb = ByteBuffer.wrap(strBuilder.toString().getBytes());
        bbr = new ByteBufferReader(bb, "UTF-8");

        // execute & verify
        for (String line : lines) {
            String text = bbr.readLine();
            assertThat(text).isEqualTo(line);
        }
    }


    @Test
    public void test_readLine_by_line_inWindows() throws IOException {
        // prepare
        String[] lines = { "firstLine", "secondLine", "thirdLine" };

        StringBuilder strBuilder = new StringBuilder();
        for (String line : lines) {
            strBuilder.append(line).append(WINDOWS_SEPARATOR);
        }

        ByteBuffer bb = ByteBuffer.wrap(strBuilder.toString().getBytes());
        bbr = new ByteBufferReader(bb, "UTF-8");

        // execute & verify
        for (String line : lines) {
            String text = bbr.readLine();
            assertThat(text).isEqualTo(line);
        }
    }


    @After
    public void tearDown() throws IOException {
        bbr.close();
        bbr = null;
    }
}
