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
    public void test_read_toCharArray_twice() throws IOException {
        // prepare
        String[] lines = { "firstLine", "secondLine", "thirdLine" };
        String separator = LINUX_SEPARATOR;
        boolean lastLineWithSeparator = false;

        ByteBuffer bb = buildSingleLineToRead(lines, separator,
                lastLineWithSeparator);
        bbr = new ByteBufferReader(bb, "UTF-8");

        // // FIRST READ
        assertReadCharArray(lines, 0, separator);

        // // SECOND READ
        assertReadCharArray(lines, 1, separator);
    }


    private void assertReadCharArray(String[] lines, int position,
            String separator) throws IOException {
        int numberOfCharsToRead = lines[position].length() + separator.length();
        char text[] = new char[numberOfCharsToRead];

        // execute
        int readChars = bbr.read(text, 0, numberOfCharsToRead);

        // verify
        assertThat(readChars).isEqualTo(numberOfCharsToRead);
        assertThat(text).isEqualTo((lines[position] + separator).toCharArray());
    }


    @Test
    public void test_readLine_markWithoutBounder_firstLineMark_and_afterSecondLineMark()
            throws IOException {
        // prepare
        String[] lines = { "firstLine", "secondLine", "thirdLine" };
        String separator = LINUX_SEPARATOR;
        boolean lastLineWithSeparator = false;

        ByteBuffer bb = buildSingleLineToRead(lines, separator,
                lastLineWithSeparator);
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
                lines[0].length() + separator.length());
    }


    @Test
    public void test_readLine_markWithoutBounder_firstLineMark()
            throws IOException {
        // prepare
        String[] lines = { "firstLine", "secondLine", "thirdLine" };
        String separator = LINUX_SEPARATOR;
        boolean lastLineWithSeparator = false;

        ByteBuffer bb = buildSingleLineToRead(lines, separator,
                lastLineWithSeparator);
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
        String separator = LINUX_SEPARATOR;
        boolean lastLineWithSeparator = false;

        ByteBuffer bb = buildSingleLineToRead(lines, separator,
                lastLineWithSeparator);
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
        String separator = LINUX_SEPARATOR;
        boolean lastLineWithSeparator = true;

        ByteBuffer bb = buildSingleLineToRead(lines, separator,
                lastLineWithSeparator);
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
        String separator = LINUX_SEPARATOR;
        boolean lastLineWithSeparator = true;

        ByteBuffer bb = buildSingleLineToRead(lines, separator,
                lastLineWithSeparator);
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
        String separator = WINDOWS_SEPARATOR;
        boolean lastLineWithSeparator = false;

        ByteBuffer bb = buildSingleLineToRead(lines, separator,
                lastLineWithSeparator);
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
        String separator = WINDOWS_SEPARATOR;
        boolean lastLineWithSeparator = true;

        ByteBuffer bb = buildSingleLineToRead(lines, separator,
                lastLineWithSeparator);
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
        String separator = WINDOWS_SEPARATOR;
        boolean lastLineWithSeparator = true;

        ByteBuffer bb = buildSingleLineToRead(lines, separator,
                lastLineWithSeparator);
        bbr = new ByteBufferReader(bb, "UTF-8");

        // execute & verify
        for (String line : lines) {
            String text = bbr.readLine();
            assertThat(text).isEqualTo(line);
        }
    }


    private ByteBuffer buildSingleLineToRead(String[] lines, String separator,
            boolean lastLineWithSeparator) {
        StringBuilder strBuilder = new StringBuilder();
        int length = lines.length;
        for (int i = 0; i < length; i++) {
            if (i == length - 1 && !lastLineWithSeparator) {
                strBuilder.append(lines[i]);
            } else {
                strBuilder.append(lines[i]).append(separator);
            }
        }

        return ByteBuffer.wrap(strBuilder.toString().getBytes());
    }


    @After
    public void tearDown() throws IOException {
        bbr.close();
        bbr = null;
    }
}
