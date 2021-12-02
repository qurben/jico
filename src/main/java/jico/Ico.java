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
package jico;

import jico.common.bytesource.ByteSource;
import jico.common.bytesource.ByteSourceInputStream;
import jico.formats.ico.IcoImageParser;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * The primary application programming interface (API) to the JICO library.
 */
public final class Ico {
    private Ico() {
        // Instances can not be created
    }

    /**
     * Gets all images specified by the InputStream  (some
     * formats may include multiple images within a single data source).
     * @param is A valid InputStream
     * @return A valid (potentially empty) list of BufferedImage objects.
     * @throws ImageReadException In the event that the specified
     * content does not conform to the format of the specific parser
     * implementation.
     * @throws IOException In the event of unsuccessful read or
     * access operation.
     */
    public static List<BufferedImage> getAllIcoImages(final InputStream is) throws ImageReadException, IOException {
        return getAllIcoImages(new ByteSourceInputStream(is));
    }

    private static List<BufferedImage> getAllIcoImages(final ByteSource byteSource) throws ImageReadException, IOException {
        return new IcoImageParser().getAllBufferedImages(byteSource);
    }
}
