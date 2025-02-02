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

import jico.image.IconDetect;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class IcoImageParser {

    public static final int FILE_HEADER_SIZE = 6;
    /**
     * Size of the ICONDIR header.
     */
    private static final int ICONDIR_SIZE = 6;
    /**
     * Size of the ICONDIRENTRY header.
     */
    private static final int ICONDIRENTRY_SIZE = 16;
    private static final int ICON_INFO_SIZE = 16;

    private final IconDetect iconDetect;


    public IcoImageParser(IconDetect iconDetect) {
        this.iconDetect = iconDetect;
    }

    public BufferedImage[] getAllBufferedImages(final InputStream byteSource) throws ImageReadException, IOException {
        try (InputStream is = new BufferedInputStream(byteSource, 1024)) {
            short iconCount = getIconCount(is);

            final IconInfo[] iconInfos = new IconInfo[iconCount];
            for (int i = 0; i < iconCount; i++) {
                iconInfos[i] = createIconInfo(is);
            }

            int offset = ICONDIR_SIZE + ICONDIRENTRY_SIZE * iconCount;

            final BufferedImage[] icons = new BufferedImage[iconCount];

            for (int i = 0; i < iconInfos.length; i++) {
                IconInfo iconInfo = iconInfos[i];
                long skipped = is.skip(iconInfo.getImageOffset() - offset);
                if (skipped != iconInfo.getImageOffset() - offset) {
                    throw new ImageReadException("Invalid offset");
                }

                offset = iconInfo.getImageOffset() + iconInfo.getImageSize();

                icons[i] = iconDetect.detect(is).readBufferedImage(iconInfo.getImageSize(), is);
            }

            return icons;
        }
    }

    private short getIconCount(InputStream is) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(is.readNBytes(FILE_HEADER_SIZE)).order(ByteOrder.LITTLE_ENDIAN);

        final short reserved = byteBuffer.getShort();
        final short iconType = byteBuffer.getShort();
        final short iconCount = byteBuffer.getShort();

        if (reserved != 0) {
            throw new IOException("Not valid, reserved is " + reserved);
        }
        if (iconType != 1 && iconType != 2) {
            throw new IOException("Not valid, icon type is " + iconType);
        }

        return iconCount;
    }

    private IconInfo createIconInfo(InputStream is) throws IOException {
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
        /*planes = */
        byteBuffer.getShort();
        // BitCount (2 bytes), number of bits per pixel (1 for monochrome,
        // 4 for 16 colors, 8 for 256 colors, 24 for true colors,
        // 32 for true colors + alpha channel)
        /*bitCount = */
        byteBuffer.getShort();
        // ImageSize (4 bytes), Length of resource in bytes
        int imageSize = byteBuffer.getInt();
        // ImageOffset (4 bytes), start of the image in the file
        int imageOffset = byteBuffer.getInt();

        return new IconInfo(imageSize, imageOffset);
    }

    public static class IconInfo {
        private final int imageSize;
        private final int imageOffset;

        public IconInfo(final int imageSize, final int imageOffset) throws IOException {
            this.imageSize = imageSize;
            this.imageOffset = imageOffset;
        }

        public int getImageSize() {
            return imageSize;
        }

        public int getImageOffset() {
            return imageOffset;
        }
    }
}
