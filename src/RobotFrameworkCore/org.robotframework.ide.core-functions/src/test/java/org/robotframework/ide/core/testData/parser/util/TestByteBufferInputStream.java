package org.robotframework.ide.core.testData.parser.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Test;


public class TestByteBufferInputStream {

    private ByteBufferInputStream bbis;


    @Test
    public void test_markWith_oneByte_read_twoBytesAnd_thenReset_positionShouldnotMove()
            throws IOException {
        // prepare
        char a = 'A';
        char b = 'B';
        char c = 'C';
        byte[] bytes = new byte[] { (byte) a, (byte) b, (byte) c };
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bbis = new ByteBufferInputStream(bb);

        // execute
        bbis.mark(1);
        bbis.read();
        bbis.read();
        bbis.reset();

        // verify
        assertThat(bb.position()).isEqualTo(2);
    }


    @Test
    public void test_markWithNegativeParameter_shouldThrowsException() {
        // prepare
        char a = 'A';
        char b = 'B';
        char c = 'C';
        byte[] bytes = new byte[] { (byte) a, (byte) b, (byte) c };
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bbis = new ByteBufferInputStream(bb);

        // execute
        Exception ex = null;
        try {
            bbis.mark(-1);
            fail("Exception was expected!!!");
        } catch (IllegalArgumentException e) {
            ex = e;
        }

        // verify
        assertThat(ex.getMessage()).isEqualTo(
                "Got read limit for buffer less than zero: " + -1);
    }


    @Test
    public void test_markWithoutParameter_twoReads() throws IOException {
        // prepare
        char a = 'A';
        char b = 'B';
        char c = 'C';
        byte[] bytes = new byte[] { (byte) a, (byte) b, (byte) c };
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bbis = new ByteBufferInputStream(bb);

        // execute
        bbis.mark();
        bbis.read();

        bbis.mark(); // now we marking position 1
        bbis.read();

        int posAfterTwoReads = bb.position();
        bbis.reset();
        int posAfterReset = bb.position();

        // verify
        assertThat(posAfterTwoReads).isEqualTo(2);
        assertThat(posAfterReset).isEqualTo(1);
    }


    @Test
    public void test_markWithoutParameter_oneRead() throws IOException {
        // prepare
        char a = 'A';
        char b = 'B';
        char c = 'C';
        byte[] bytes = new byte[] { (byte) a, (byte) b, (byte) c };
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bbis = new ByteBufferInputStream(bb);

        // execute
        bbis.mark();
        bbis.read();
        int posAfterRead = bb.position();
        bbis.reset();
        int posAfterReset = bb.position();

        // verify
        assertThat(posAfterRead).isEqualTo(1);
        assertThat(posAfterReset).isEqualTo(0);
    }


    @Test
    public void test_readArrayOfBytes_threeByteArray_oneRead()
            throws IOException {
        // prepare
        char a = 'A';
        char b = 'B';
        char c = 'C';
        byte[] bytes = new byte[] { (byte) a, (byte) b, (byte) c };
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bbis = new ByteBufferInputStream(bb);

        // execute
        byte[] readData = new byte[bytes.length];
        int read = bbis.read(readData, 0, 10);

        // verify
        assertThat(read).isEqualTo(3);
        assertThat(readData).isEqualTo(bytes);
        assertThat(bb.position()).isEqualTo(3);
    }


    @Test
    public void test_availableByteInBuffer_oneByteArray_oneRead()
            throws IOException {
        // prepare
        char c = 'A';
        ByteBuffer bb = ByteBuffer.wrap(new byte[] { (byte) c });
        bbis = new ByteBufferInputStream(bb);

        // execute
        bbis.read();

        // verify
        assertThat(bbis.available()).isEqualTo(0);
    }


    @Test
    public void test_availableByteInBuffer_oneByteArray_noReading()
            throws IOException {
        // prepare
        char c = 'A';
        ByteBuffer bb = ByteBuffer.wrap(new byte[] { (byte) c });
        bbis = new ByteBufferInputStream(bb);

        // execute & verify
        assertThat(bbis.available()).isEqualTo(1);
    }


    @Test
    public void test_currentByteInBuffer_oneByteArray_noReading()
            throws IOException {
        // prepare
        char c = 'A';
        ByteBuffer bb = ByteBuffer.wrap(new byte[] { (byte) c });
        bbis = new ByteBufferInputStream(bb);

        // execute & verify
        assertThat(bbis.currentByteInBuffer()).isEqualTo(c);
    }


    @Test
    public void test_currentByteInBuffer_oneByteArray_readFirstByte()
            throws IOException {
        // prepare
        char c = 'A';
        ByteBuffer bb = ByteBuffer.wrap(new byte[] { (byte) c });
        bbis = new ByteBufferInputStream(bb);

        // execute
        bbis.read();

        // verify
        assertThat(bbis.currentByteInBuffer()).isEqualTo(-1);
    }


    @Test
    public void test_tryToReadTwoBytes_fromOneByte_buffer() throws IOException {
        // prepare
        char c = 'A';
        ByteBuffer bb = ByteBuffer.wrap(new byte[] { (byte) c });
        bbis = new ByteBufferInputStream(bb);

        // execute
        bbis.read();
        int pos = bbis.read();

        // verify
        assertThat(bb.position()).isEqualTo(1);
        assertThat(pos).isEqualTo(-1);
    }


    @Test
    public void test_readOneByte_fromZeroLengthByte_buffer() throws IOException {
        // prepare
        ByteBuffer bb = ByteBuffer.wrap(new byte[] {});
        bbis = new ByteBufferInputStream(bb);

        // execute
        int got = bbis.read();

        // verify
        assertThat(bb.position()).isEqualTo(0);
        assertThat(got).isEqualTo(-1);
    }


    @Test
    public void test_readOneByte_fromOneByte_buffer() throws IOException {
        // prepare
        char c = 'A';
        ByteBuffer bb = ByteBuffer.wrap(new byte[] { (byte) c });
        bbis = new ByteBufferInputStream(bb);

        // execute
        char got = (char) bbis.read();

        // verify
        assertThat(bb.position()).isEqualTo(1);
        assertThat(got).isEqualTo(c);
    }


    @After
    public void tearDown() throws IOException {
        bbis.close();
        bbis = null;
    }
}
