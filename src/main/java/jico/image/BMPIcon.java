package jico.image;

import jico.ImageReadException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class BMPIcon implements Icon {
    public static final int BITMAPV3INFOHEADER_SIZE = 56;
    public static final int BITMAPHEADER_SIZE = 14;
    public static final byte[] MAGIC_NUMBERS_BMP = {0x42, 0x4d,};

    @Override
    public BufferedImage readBufferedImage(int imageSize, final InputStream is) throws IOException, ImageReadException {
        ByteBuffer byteBuffer = ByteBuffer.wrap(is.readNBytes(imageSize))
                .order(ByteOrder.LITTLE_ENDIAN);

        final int size = byteBuffer.getInt();
        final int width = byteBuffer.getInt();
        final int height = byteBuffer.getInt();
        final short planes = byteBuffer.getShort();
        final short bitCount = byteBuffer.getShort();
        int compression = byteBuffer.getInt();
        final int sizeImage = byteBuffer.getInt();
        final int xPelsPerMeter = byteBuffer.getInt();
        final int yPelsPerMeter = byteBuffer.getInt();
        final int colorsUsed = byteBuffer.getInt();
        final int colorsImportant = byteBuffer.getInt();

        // use this (0)
        int redMask = 0;
        int greenMask = 0;
        int blueMask = 0;
        int alphaMask = 0;
        if (compression == 3) {
            redMask = byteBuffer.getInt();
            greenMask = byteBuffer.getInt();
            blueMask = byteBuffer.getInt();
        }
        final byte[] restOfFile = new byte[byteBuffer.remaining()];
        byteBuffer.get(restOfFile);

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

        final int bitmapPixelsOffset = BITMAPHEADER_SIZE + BITMAPV3INFOHEADER_SIZE + 4 * ((colorsUsed == 0 && bitCount <= 8) ? (1 << bitCount)
                : colorsUsed);
        final int bitmapSize = BITMAPHEADER_SIZE + BITMAPV3INFOHEADER_SIZE + restOfFile.length;

        final ByteBuffer buffer = ByteBuffer.allocate(bitmapSize)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(MAGIC_NUMBERS_BMP)
                .putInt(bitmapSize)
                .putInt(0)
                .putInt(bitmapPixelsOffset)

                .putInt(BITMAPV3INFOHEADER_SIZE)
                .putInt(width)
                .putInt(height / 2)
                .putShort(planes)
                .putShort(bitCount)
                .putInt(compression)
                .putInt(sizeImage)
                .putInt(xPelsPerMeter)
                .putInt(yPelsPerMeter)
                .putInt(colorsUsed)
                .putInt(colorsImportant)
                .putInt(redMask)
                .putInt(greenMask)
                .putInt(blueMask)
                .putInt(alphaMask)
                .put(restOfFile);

        final ByteArrayInputStream bmpInputStream = new ByteArrayInputStream(buffer.array());
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
            ByteBuffer byteBuffer1 = ByteBuffer.wrap(bmpInputStream.readNBytes(colorMapSizeBytes)).order(ByteOrder.LITTLE_ENDIAN);
            transparencyMap = new byte[byteBuffer1.remaining()];
            byteBuffer1.get(transparencyMap);
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
        return resultImage;
    }
}
