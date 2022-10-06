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

import jico.image.BMPIcon;
import jico.image.Icon;
import jico.image.PNGIcon;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static jico.ImageFormatGuesser.guessFormat;

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

    private Icon getIconData(final byte[] iconData)
            throws ImageReadException, IOException {

        if (guessFormat(iconData) == ImageFormat.PNG) {
            return new PNGIcon(new ByteArrayInputStream(iconData));
        }
        return new BMPIcon(new ByteArrayInputStream(iconData));
    }

    private ImageContents readImage(final InputStream byteSource) throws ImageReadException, IOException {
        try (InputStream is = byteSource) {
            final FileHeader fileHeader = new FileHeader(is);

            final List<IconInfo> fIconInfos = new ArrayList<>(fileHeader.iconCount);
            for (int i = 0; i < fileHeader.iconCount; i++) {
                fIconInfos.add(new IconInfo(is));
            }

            int offset = ICONDIR_SIZE + ICONDIRENTRY_SIZE * fileHeader.iconCount;

            final List<Icon> icons = new ArrayList<>(fileHeader.iconCount);

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

                icons.add(getIconData(iconData));
            }

            return new ImageContents(fileHeader, icons);
        }
    }

    public List<BufferedImage> getAllBufferedImages(final InputStream byteSource) throws ImageReadException, IOException {
        final ImageContents contents = readImage(byteSource);

        final List<BufferedImage> result = new ArrayList<>(contents.fileHeader.iconCount);
        for (final Icon icon : contents.icons) {
            result.add(icon.readBufferedImage());
        }

        return result;
    }

    private static class FileHeader {
        public static final int FILE_HEADER_SIZE = 6;
        /**
         * Reserved (2 bytes), always 0
         */
        public final short reserved;
        /**
         * IconType (2 bytes), if the image is an icon it?s 1, for cursors the value is 2.
         */
        public final short iconType;
        /**
         * IconCount (2 bytes), number of icons in this file.
         */
        public final short iconCount;

        FileHeader(InputStream is) throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.wrap(is.readNBytes(FILE_HEADER_SIZE)).order(ByteOrder.LITTLE_ENDIAN);

            reserved = byteBuffer.getShort();
            iconType = byteBuffer.getShort();
            iconCount = byteBuffer.getShort();

            if (reserved != 0) {
                throw new IOException("Not a Valid ICO File: reserved is " + reserved);
            }
            if (iconType != 1 && iconType != 2) {
                throw new IOException("Not a Valid ICO File: icon type is " + iconType);
            }
        }
    }

    private static class ImageContents {
        public final FileHeader fileHeader;
        public final List<Icon> icons;

        ImageContents(final FileHeader fileHeader, final List<Icon> icons) {
            this.fileHeader = fileHeader;
            this.icons = icons;
        }
    }

    private static class IconInfo {
        public static final int ICON_INFO_SIZE = 16;
        /*public final byte width;
        public final byte height;
        public final byte colorCount;
        public final byte reserved;
        public final short planes;
        public final short bitCount;*/
        public final int imageSize;
        public final int imageOffset;

        public IconInfo(InputStream is) throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.wrap(is.readNBytes(ICON_INFO_SIZE)).order(ByteOrder.LITTLE_ENDIAN);

            // Width (1 byte), Width of Icon (1 to 255)
            /*width = */
            byteBuffer.get();
            // Height (1 byte), Height of Icon (1 to 255)
            /*height = */
            byteBuffer.get();
            // ColorCount (1 byte), Number of colors, either
            // 0 for 24 bit or higher,
            // 2 for monochrome or 16 for 16 color images.
            /*colorCount = */
            byteBuffer.get();
            // Reserved (1 byte), Not used (always 0)
            /*reserved = */
            byteBuffer.get();
            // Planes (2 bytes), always 1
            /*planes = */byteBuffer.getShort();
            // BitCount (2 bytes), number of bits per pixel (1 for monochrome,
            // 4 for 16 colors, 8 for 256 colors, 24 for true colors,
            // 32 for true colors + alpha channel)
            /*bitCount = */
            byteBuffer.getShort();
            // ImageSize (4 bytes), Length of resource in bytes
            imageSize = byteBuffer.getInt();
            // ImageOffset (4 bytes), start of the image in the file
            imageOffset = byteBuffer.getInt();
        }
    }
}
