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
import java.io.OutputStream;

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

    public static int readInt(final InputStream is, final String exception) throws IOException {
        final int byte0 = is.read();
        final int byte1 = is.read();
        final int byte2 = is.read();
        final int byte3 = is.read();
        if ((byte0 | byte1 | byte2 | byte3) < 0) {
            throw new IOException(exception);
        }

        return (byte3 << 24) | (byte2 << 16) | (byte1 << 8) | (byte0);
    }

    public static int readShort(final InputStream is, final String exception) throws IOException {
        final int byte0 = is.read();
        final int byte1 = is.read();
        if ((byte0 | byte1) < 0) {
            throw new IOException(exception);
        }

        return (byte1 << 8) | byte0;
    }

    public static void writeInt(OutputStream os, final int value) throws IOException {
        os.write(0xff & value);
        os.write(0xff & (value >> 8));
        os.write(0xff & (value >> 16));
        os.write(0xff & (value >> 24));
    }

    public static void writeShort(OutputStream os, final int value) throws IOException {
        os.write(0xff & value);
        os.write(0xff & (value >> 8));
    }
}
