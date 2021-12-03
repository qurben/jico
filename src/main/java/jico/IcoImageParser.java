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

import static jico.common.BinaryFunctions.read2Bytes;
import static jico.common.BinaryFunctions.read4Bytes;
import static jico.common.BinaryFunctions.readByte;
import static jico.common.BinaryFunctions.readBytes;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import jico.common.BinaryOutputStream;
import jico.common.bytesource.ByteSource;

import javax.imageio.ImageIO;

class IcoImageParser {

    public static final int BITMAPV3INFOHEADER_SIZE = 56;
    public static final int BITMAPHEADER_SIZE = 14;

    public IcoImageParser() {
    }

    private static class FileHeader {
        public final int reserved; // Reserved (2 bytes), always 0
        public final int iconType; // IconType (2 bytes), if the image is an
                                   // icon it?s 1, for cursors the value is 2.
        public final int iconCount; // IconCount (2 bytes), number of icons in
                                    // this file.

        FileHeader(final int reserved, final int iconType, final int iconCount) {
            this.reserved = reserved;
            this.iconType = iconType;
            this.iconCount = iconCount;
        }
    }

    private FileHeader readFileHeader(final InputStream is) throws ImageReadException, IOException {
        final int reserved = read2Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN);
        final int iconType = read2Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN);
        final int iconCount = read2Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN);

        if (reserved != 0) {
            throw new ImageReadException("Not a Valid ICO File: reserved is " + reserved);
        }
        if (iconType != 1 && iconType != 2) {
            throw new ImageReadException("Not a Valid ICO File: icon type is " + iconType);
        }

        return new FileHeader(reserved, iconType, iconCount);

    }

    private static class IconInfo {
        public final byte width;
        public final byte height;
        public final byte colorCount;
        public final byte reserved;
        public final int planes;
        public final int bitCount;
        public final int imageSize;
        public final int imageOffset;

        IconInfo(final byte width, final byte height,
                final byte colorCount, final byte reserved, final int planes,
                final int bitCount, final int imageSize, final int imageOffset) {
            this.width = width;
            this.height = height;
            this.colorCount = colorCount;
            this.reserved = reserved;
            this.planes = planes;
            this.bitCount = bitCount;
            this.imageSize = imageSize;
            this.imageOffset = imageOffset;
        }
    }

    private IconInfo readIconInfo(final InputStream is) throws IOException {
        // Width (1 byte), Width of Icon (1 to 255)
        final byte width = readByte(is, "Not a Valid ICO File");
        // Height (1 byte), Height of Icon (1 to 255)
        final byte height = readByte(is, "Not a Valid ICO File");
        // ColorCount (1 byte), Number of colors, either
        // 0 for 24 bit or higher,
        // 2 for monochrome or 16 for 16 color images.
        final byte colorCount = readByte(is, "Not a Valid ICO File");
        // Reserved (1 byte), Not used (always 0)
        final byte reserved = readByte(is, "Not a Valid ICO File");
        // Planes (2 bytes), always 1
        final int planes = read2Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN);
        // BitCount (2 bytes), number of bits per pixel (1 for monochrome,
        // 4 for 16 colors, 8 for 256 colors, 24 for true colors,
        // 32 for true colors + alpha channel)
        final int bitCount = read2Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN);
        // ImageSize (4 bytes), Length of resource in bytes
        final int imageSize = read4Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN);
        // ImageOffset (4 bytes), start of the image in the file
        final int imageOffset = read4Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN);

        return new IconInfo(width, height, colorCount, reserved, planes, bitCount, imageSize, imageOffset);
    }

    private static class BitmapHeader {
        public final int size;
        public final int width;
        public final int height;
        public final int planes;
        public final int bitCount;
        public final int compression;
        public final int sizeImage;
        public final int xPelsPerMeter;
        public final int yPelsPerMeter;
        public final int colorsUsed;
        public final int colorsImportant;

        BitmapHeader(final int size, final int width, final int height,
                final int planes, final int bitCount, final int compression,
                final int sizeImage, final int pelsPerMeter,
                final int pelsPerMeter2, final int colorsUsed,
                final int colorsImportant) {
            this.size = size;
            this.width = width;
            this.height = height;
            this.planes = planes;
            this.bitCount = bitCount;
            this.compression = compression;
            this.sizeImage = sizeImage;
            xPelsPerMeter = pelsPerMeter;
            yPelsPerMeter = pelsPerMeter2;
            this.colorsUsed = colorsUsed;
            this.colorsImportant = colorsImportant;
        }
    }

    private abstract static class IconData {
        public final IconInfo iconInfo;

        IconData(final IconInfo iconInfo) {
            this.iconInfo = iconInfo;
        }

        public abstract BufferedImage readBufferedImage()
                throws ImageReadException;
    }

    private static class BitmapIconData extends IconData {
        public final BitmapHeader header;
        public final BufferedImage bufferedImage;

        BitmapIconData(final IconInfo iconInfo,
                final BitmapHeader header, final BufferedImage bufferedImage) {
            super(iconInfo);
            this.header = header;
            this.bufferedImage = bufferedImage;
        }

        @Override
        public BufferedImage readBufferedImage() {
            return bufferedImage;
        }
    }

    private static class PNGIconData extends IconData {
        public final BufferedImage bufferedImage;

        PNGIconData(final IconInfo iconInfo,
                final BufferedImage bufferedImage) {
            super(iconInfo);
            this.bufferedImage = bufferedImage;
        }

        @Override
        public BufferedImage readBufferedImage() {
            return bufferedImage;
        }
    }

    private IconData readBitmapIconData(final byte[] iconData, final IconInfo fIconInfo)
            throws ImageReadException, IOException {
        final ByteArrayInputStream is = new ByteArrayInputStream(iconData);
        final int size = read4Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN); // Size (4
                                                                   // bytes),
                                                                   // size of
                                                                   // this
                                                                   // structure
                                                                   // (always
                                                                   // 40)
        final int width = read4Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN); // Width (4
                                                                     // bytes),
                                                                     // width of
                                                                     // the
                                                                     // image
                                                                     // (same as
                                                                     // iconinfo.width)
        final int height = read4Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN); // Height
                                                                       // (4
                                                                       // bytes),
                                                                       // scanlines
                                                                       // in the
                                                                       // color
                                                                       // map +
                                                                       // transparent
                                                                       // map
                                                                       // (iconinfo.height
                                                                       // * 2)
        final int planes = read2Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN); // Planes
                                                                       // (2
                                                                       // bytes),
                                                                       // always
                                                                       // 1
        final int bitCount = read2Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN); // BitCount
                                                                           // (2
                                                                           // bytes),
                                                                           // 1,4,8,16,24,32
                                                                           // (see
                                                                           // iconinfo
                                                                           // for
                                                                           // details)
        int compression = read4Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN); // Compression
                                                                                 // (4
                                                                                 // bytes),
                                                                                 // we
                                                                                 // don?t
                                                                                 // use
                                                                                 // this
                                                                                 // (0)
        final int sizeImage = read4Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN); // SizeImage
                                                                             // (4
                                                                             // bytes),
                                                                             // we
                                                                             // don?t
                                                                             // use
                                                                             // this
                                                                             // (0)
        final int xPelsPerMeter = read4Bytes(is,
                "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN); // XPelsPerMeter (4 bytes), we don?t
                                         // use this (0)
        final int yPelsPerMeter = read4Bytes(is,
                "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN); // YPelsPerMeter (4 bytes), we don?t
                                         // use this (0)
        final int colorsUsed = read4Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN); // ColorsUsed
                                                                               // (4
                                                                               // bytes),
                                                                               // we
                                                                               // don?t
                                                                               // use
                                                                               // this
                                                                               // (0)
        final int colorsImportant = read4Bytes(is,
                "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN); // ColorsImportant (4 bytes), we don?t
                                         // use this (0)
        int redMask = 0;
        int greenMask = 0;
        int blueMask = 0;
        int alphaMask = 0;
        if (compression == 3) {
            redMask = read4Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN);
            greenMask = read4Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN);
            blueMask = read4Bytes(is, "Not a Valid ICO File", ByteOrder.LITTLE_ENDIAN);
        }
        final byte[] restOfFile = readBytes(is, is.available());

        if (size != 40) {
            throw new ImageReadException("Not a Valid ICO File: Wrong bitmap header size " + size);
        }
        if (planes != 1) {
            throw new ImageReadException("Not a Valid ICO File: Planes can't be " + planes);
        }

        if (compression == 0 && bitCount == 32) {
            // 32 BPP RGB icons need an alpha channel, but BMP files don't have
            // one unless BI_BITFIELDS is used...
            compression = 3;
            redMask = 0x00ff0000;
            greenMask = 0x0000ff00;
            blueMask = 0x000000ff;
            alphaMask = 0xff000000;
        }

        final BitmapHeader header = new BitmapHeader(size, width, height, planes,
                bitCount, compression, sizeImage, xPelsPerMeter, yPelsPerMeter,
                colorsUsed, colorsImportant);

        final int bitmapPixelsOffset = BITMAPHEADER_SIZE + BITMAPV3INFOHEADER_SIZE + 4 * ((colorsUsed == 0 && bitCount <= 8) ? (1 << bitCount)
                : colorsUsed);
        final int bitmapSize = BITMAPHEADER_SIZE + BITMAPV3INFOHEADER_SIZE + restOfFile.length;

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(bitmapSize);
        try (BinaryOutputStream bos = new BinaryOutputStream(baos, ByteOrder.LITTLE_ENDIAN)) {
            bos.write('B');
            bos.write('M');
            bos.write4Bytes(bitmapSize);
            bos.write4Bytes(0);
            bos.write4Bytes(bitmapPixelsOffset);

            bos.write4Bytes(BITMAPV3INFOHEADER_SIZE);
            bos.write4Bytes(width);
            bos.write4Bytes(height / 2);
            bos.write2Bytes(planes);
            bos.write2Bytes(bitCount);
            bos.write4Bytes(compression);
            bos.write4Bytes(sizeImage);
            bos.write4Bytes(xPelsPerMeter);
            bos.write4Bytes(yPelsPerMeter);
            bos.write4Bytes(colorsUsed);
            bos.write4Bytes(colorsImportant);
            bos.write4Bytes(redMask);
            bos.write4Bytes(greenMask);
            bos.write4Bytes(blueMask);
            bos.write4Bytes(alphaMask);
            bos.write(restOfFile);
            bos.flush();
        }

        final ByteArrayInputStream bmpInputStream = new ByteArrayInputStream(baos.toByteArray());
        final BufferedImage bmpImage = ImageIO.read(bmpInputStream);

        // Transparency map is optional with 32 BPP icons, because they already
        // have
        // an alpha channel, and Windows only uses the transparency map when it
        // has to
        // display the icon on a < 32 BPP screen. But it's still used instead of
        // alpha
        // if the image would be completely transparent with alpha...
        int t_scanline_size = (width + 7) / 8;
        if ((t_scanline_size % 4) != 0) {
            t_scanline_size += 4 - (t_scanline_size % 4); // pad scanline to 4
                                                          // byte size.
        }
        final int colorMapSizeBytes = t_scanline_size * (height / 2);
        byte[] transparencyMap = null;
        try {
            transparencyMap = readBytes(
                    bmpInputStream, colorMapSizeBytes,
                    "Not a Valid ICO File");
        } catch (final IOException ioEx) {
            if (bitCount != 32) {
                throw ioEx;
            }
        }

        boolean allAlphasZero = true;
        if (bitCount == 32) {
            for (int y = 0; allAlphasZero && y < bmpImage.getHeight(); y++) {
                for (int x = 0; x < bmpImage.getWidth(); x++) {
                    if ((bmpImage.getRGB(x, y) & 0xff000000) != 0) {
                        allAlphasZero = false;
                        break;
                    }
                }
            }
        }
        BufferedImage resultImage;
        if (allAlphasZero) {
            resultImage = new BufferedImage(bmpImage.getWidth(),
                    bmpImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < resultImage.getHeight(); y++) {
                for (int x = 0; x < resultImage.getWidth(); x++) {
                    int alpha = 0xff;
                    if (transparencyMap != null) {
                        final int alphaByte = 0xff & transparencyMap[t_scanline_size
                                * (bmpImage.getHeight() - y - 1) + (x / 8)];
                        alpha = 0x01 & (alphaByte >> (7 - (x % 8)));
                        alpha = (alpha == 0) ? 0xff : 0x00;
                    }
                    resultImage.setRGB(x, y, (alpha << 24)
                            | (0xffffff & bmpImage.getRGB(x, y)));
                }
            }
        } else {
            resultImage = bmpImage;
        }
        return new BitmapIconData(fIconInfo, header, resultImage);
    }

    private IconData readIconData(final byte[] iconData, final IconInfo fIconInfo)
            throws ImageReadException, IOException {

        final ImageFormat imageFormat = new ImageFormatGuesser().guessFormat(iconData);
        if (imageFormat.equals(ImageFormat.PNG)) {
            return new PNGIconData(fIconInfo, ImageIO.read(new ByteArrayInputStream(iconData)));
        }
        return readBitmapIconData(iconData, fIconInfo);
    }

    private static class ImageContents {
        public final FileHeader fileHeader;
        public final IconData[] iconDatas;

        ImageContents(final FileHeader fileHeader, final IconData[] iconDatas) {
            this.fileHeader = fileHeader;
            this.iconDatas = iconDatas;
        }
    }

    private ImageContents readImage(final ByteSource byteSource)
            throws ImageReadException, IOException {
        try (InputStream is = byteSource.getInputStream()) {
            final FileHeader fileHeader = readFileHeader(is);

            final IconInfo[] fIconInfos = new IconInfo[fileHeader.iconCount];
            for (int i = 0; i < fileHeader.iconCount; i++) {
                fIconInfos[i] = readIconInfo(is);
            }

            final IconData[] fIconDatas = new IconData[fileHeader.iconCount];
            for (int i = 0; i < fileHeader.iconCount; i++) {
                final byte[] iconData = byteSource.getBlock(
                        fIconInfos[i].imageOffset, fIconInfos[i].imageSize);
                fIconDatas[i] = readIconData(iconData, fIconInfos[i]);
            }

            return new ImageContents(fileHeader, fIconDatas);
        }
    }

    public List<BufferedImage> getAllBufferedImages(final ByteSource byteSource)
            throws ImageReadException, IOException {
        final ImageContents contents = readImage(byteSource);

        final FileHeader fileHeader = contents.fileHeader;
        final List<BufferedImage> result = new ArrayList<>(fileHeader.iconCount);
        for (int i = 0; i < fileHeader.iconCount; i++) {
            final IconData iconData = contents.iconDatas[i];

            final BufferedImage image = iconData.readBufferedImage();

            result.add(image);
        }

        return result;
    }
}
