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
import java.io.OutputStream;
import java.nio.ByteOrder;

public class BinaryOutputStream extends OutputStream {
    private final OutputStream os;
    // default byte order for Java, many file formats.
    private final ByteOrder byteOrder;

    public BinaryOutputStream(final OutputStream os, final ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
        this.os = os;
    }

    @Override
    public void write(final int i) throws IOException {
        os.write(i);
    }

    @Override
    public final void write(final byte[] bytes) throws IOException {
        os.write(bytes, 0, bytes.length);
    }

    @Override
    public final void write(final byte[] bytes, final int offset, final int length) throws IOException {
        os.write(bytes, offset, length);
    }

    @Override
    public void flush() throws IOException {
        os.flush();
    }

    @Override
    public void close() throws IOException {
        os.close();
    }

    public final void write4Bytes(final int value) throws IOException {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            write(0xff & (value >> 24));
            write(0xff & (value >> 16));
            write(0xff & (value >> 8));
            write(0xff & value);
        } else {
            write(0xff & value);
            write(0xff & (value >> 8));
            write(0xff & (value >> 16));
            write(0xff & (value >> 24));
        }
    }

    public final void write2Bytes(final int value) throws IOException {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            write(0xff & (value >> 8));
            write(0xff & value);
        } else {
            write(0xff & value);
            write(0xff & (value >> 8));
        }
    }
}
