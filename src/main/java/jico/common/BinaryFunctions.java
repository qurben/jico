/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jico.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/**
 * Convenience methods for various binary and I/O operations.
 */
public final class BinaryFunctions {
    private BinaryFunctions() {
    }

    public static byte readByte(final InputStream is, final String exception)
            throws IOException {
        final int result = is.read();
        if ((result < 0)) {
            throw new IOException(exception);
        }
        return (byte) (0xff & result);
    }

    public static byte[] readBytes(final InputStream is, final int length, final String exception) throws IOException {
        if (length < 0) {
            throw new IOException(String.format("%s, invalid length: %d", exception, length));
        }
        final byte[] result = new byte[length];
        int read = 0;
        while (read < length) {
            final int count = is.read(result, read, length - read);
            if (count < 0) {
                throw new IOException(exception + " count: " + count
                        + " read: " + read + " length: " + length);
            }

            read += count;
        }

        return result;
    }

    public static byte[] readBytes(final InputStream is, final int count) throws IOException {
        return readBytes(is, count, "Unexpected EOF");
    }

    public static void skipBytes(final InputStream is, final long length, final String exception)
            throws IOException {
        long total = 0;
        while (length != total) {
            final long skipped = is.skip(length - total);
            if (skipped < 1) {
                throw new IOException(exception + " (" + skipped + ")");
            }
            total += skipped;
        }
    }

    public static int read4Bytes(final InputStream is, final String exception, final ByteOrder byteOrder) throws IOException {
        final int byte0 = is.read();
        final int byte1 = is.read();
        final int byte2 = is.read();
        final int byte3 = is.read();
        if ((byte0 | byte1 | byte2 | byte3) < 0) {
            throw new IOException(exception);
        }

        final int result;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            result = (byte0 << 24) | (byte1 << 16)
                    | (byte2 << 8) | (byte3 << 0);
        } else {
            result = (byte3 << 24) | (byte2 << 16)
                    | (byte1 << 8) | (byte0 << 0);
        }

        return result;
    }

    public static int read2Bytes(final InputStream is,
                                 final String exception, final ByteOrder byteOrder) throws IOException {
        final int byte0 = is.read();
        final int byte1 = is.read();
        if ((byte0 | byte1) < 0) {
            throw new IOException(exception);
        }

        final int result;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            result = (byte0 << 8) | byte1;
        } else {
            result = (byte1 << 8) | byte0;
        }

        return result;
    }

    public static void skipBytes(final InputStream is, final long length) throws IOException {
        skipBytes(is, length, "Couldn't skip bytes");
    }
}
