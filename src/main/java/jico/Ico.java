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

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.List;

/**
 * The primary application programming interface (API) to the JICO library.
 */
public final class Ico {
    private Ico() {
        // Instances can not be created
    }

    /**
     * Gets all images specified by the InputStream.
     * @param is A valid InputStream
     * @return A valid (potentially empty) list of BufferedImage objects.
     * @throws ImageReadException In the event that the specified
     * content does not conform to the format of the specific parser
     * implementation.
     * @throws IOException In the event of unsuccessful read or
     * access operation.
     */
    public static List<BufferedImage> read(final InputStream is) throws ImageReadException, IOException {
        if (is == null) {
            throw new IllegalArgumentException("is == null!");
        }

        return new IcoImageParser().getAllBufferedImages(is);
    }

    /**
     * Gets all images specified by the File.
     * @param file A file
     * @return A valid (potentially empty) list of BufferedImage objects.
     * @throws ImageReadException In the event that the specified
     * content does not conform to the format of the specific parser
     * implementation.
     * @throws IOException In the event of unsuccessful read or
     * access operation.
     */
    public static List<BufferedImage> read(final File file) throws  ImageReadException, IOException {
        if (file == null) {
            throw new IllegalArgumentException("file == null!");
        }
        if (!file.canRead()) {
            throw new IOException("Can't read input file!");
        }

        try (InputStream is = new FileInputStream(file)) {
            return read(is);
        }
    }

    /**
     * Gets all images specified by the URL to an ico file.
     * @param url A url
     * @return A valid (potentially empty) list of BufferedImage objects.
     * @throws ImageReadException In the event that the specified
     * content does not conform to the format of the specific parser
     * implementation.
     * @throws IOException In the event of unsuccessful read or
     * access operation.
     */
    public static List<BufferedImage> read(final URL url) throws IOException, ImageReadException {
        if (url == null) {
            throw new IllegalArgumentException("input == null!");
        }

        try (InputStream is = url.openStream()) {
            return read(is);
        }
    }
}
