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

import jico.image.BMPIconData;
import jico.image.IconData;
import jico.image.IconInfo;
import jico.image.PNGIconData;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static jico.ImageFormatGuesser.*;
import static jico.common.BinaryFunctions.*;

class IcoImageParser {

    /**
     * Size of the ICONDIR header.
     */
    private static final int ICONDIR_SIZE = 6;
    /**
     * Size of the ICONDIRENTRY header.
     */
    private static final int ICONDIRENTRY_SIZE = 16;

    public IcoImageParser() {
    }

    private FileHeader readFileHeader(final InputStream is) throws ImageReadException, IOException {
        final int reserved = readShort(is, "Not a Valid ICO File");
        final int iconType = readShort(is, "Not a Valid ICO File");
        final int iconCount = readShort(is, "Not a Valid ICO File");

        if (reserved != 0) {
            throw new ImageReadException("Not a Valid ICO File: reserved is " + reserved);
        }
        if (iconType != 1 && iconType != 2) {
            throw new ImageReadException("Not a Valid ICO File: icon type is " + iconType);
        }

        return new FileHeader(reserved, iconType, iconCount);
    }

    private IconInfo readIconInfo(final InputStream is) throws IOException {
        // Width (1 byte), Width of Icon (1 to 255)
        /*final byte width = */readByte(is, "Not a Valid ICO File");
        // Height (1 byte), Height of Icon (1 to 255)
        /*final byte height = */readByte(is, "Not a Valid ICO File");
        // ColorCount (1 byte), Number of colors, either
        // 0 for 24 bit or higher,
        // 2 for monochrome or 16 for 16 color images.
        /*final byte colorCount = */readByte(is, "Not a Valid ICO File");
        // Reserved (1 byte), Not used (always 0)
        /*final byte reserved = */readByte(is, "Not a Valid ICO File");
        // Planes (2 bytes), always 1
        /*final int planes = */readShort(is, "Not a Valid ICO File");
        // BitCount (2 bytes), number of bits per pixel (1 for monochrome,
        // 4 for 16 colors, 8 for 256 colors, 24 for true colors,
        // 32 for true colors + alpha channel)
        /*final int bitCount = */readShort(is, "Not a Valid ICO File");
        // ImageSize (4 bytes), Length of resource in bytes
        final int imageSize = readInt(is, "Not a Valid ICO File");
        // ImageOffset (4 bytes), start of the image in the file
        final int imageOffset = readInt(is, "Not a Valid ICO File");

        return new IconInfo(/*width, height, colorCount, reserved, planes, bitCount,*/ imageSize, imageOffset);
    }

    private IconData readIconData(final byte[] iconData)
            throws ImageReadException, IOException {

        if (guessFormat(iconData) == ImageFormat.PNG) {
            return new PNGIconData(new ByteArrayInputStream(iconData));
        }
        return new BMPIconData(new ByteArrayInputStream(iconData));
    }

    private ImageContents readImage(final InputStream byteSource)
            throws ImageReadException, IOException {
        try (InputStream is = byteSource) {
            final FileHeader fileHeader = readFileHeader(is);

            final List<IconInfo> fIconInfos = new ArrayList<>(fileHeader.iconCount);
            for (int i = 0; i < fileHeader.iconCount; i++) {
                fIconInfos.add(readIconInfo(is));
            }

            int offset = ICONDIR_SIZE + ICONDIRENTRY_SIZE * fileHeader.iconCount;

            final List<IconData> fIconDatas = new ArrayList<>(fileHeader.iconCount);

            for (IconInfo iconInfo : fIconInfos) {
                long skipped = byteSource.skip(iconInfo.imageOffset - offset);
                if (skipped != iconInfo.imageOffset - offset) {
                    throw new ImageReadException("Could not skip bytes");
                }

                final byte[] iconData = byteSource.readNBytes(iconInfo.imageSize);
                if (iconData.length != iconInfo.imageSize) {
                    throw new ImageReadException("Could not read bytes");
                }

                offset = iconInfo.imageOffset + iconInfo.imageSize;

                fIconDatas.add(readIconData(iconData));
            }

            return new ImageContents(fileHeader, fIconDatas);
        }
    }

    public List<BufferedImage> getAllBufferedImages(final InputStream byteSource) throws ImageReadException, IOException {
        final ImageContents contents = readImage(byteSource);

        final List<BufferedImage> result = new ArrayList<>(contents.fileHeader.iconCount);
        for (final IconData iconData : contents.iconDatas) {
            result.add(iconData.readBufferedImage());
        }

        return result;
    }

    private static class FileHeader {
        public final int reserved; // Reserved (2 bytes), always 0
        public final int iconType; // IconType (2 bytes), if the image is an icon it?s 1, for cursors the value is 2.
        public final int iconCount; // IconCount (2 bytes), number of icons in this file.

        FileHeader(final int reserved, final int iconType, final int iconCount) {
            this.reserved = reserved;
            this.iconType = iconType;
            this.iconCount = iconCount;
        }
    }

    private static class ImageContents {
        public final FileHeader fileHeader;
        public final List<IconData> iconDatas;

        ImageContents(final FileHeader fileHeader, final List<IconData> iconDatas) {
            this.fileHeader = fileHeader;
            this.iconDatas = iconDatas;
        }
    }
}
