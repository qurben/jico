package jico.image;

import jico.ImageReadException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static jico.common.BinaryFunctions.readBytes;
import static jico.common.BinaryFunctions.readInt;
import static jico.common.BinaryFunctions.readShort;
import static jico.common.BinaryFunctions.writeShort;
import static jico.common.BinaryFunctions.writeInt;

public class BMPIconData implements IconData {

    public static final int BITMAPV3INFOHEADER_SIZE = 56;
    public static final int BITMAPHEADER_SIZE = 14;
    private final InputStream is;

    public BMPIconData(final InputStream is) {
        this.is = is;
    }

    private BufferedImage readBitmapIconData(final InputStream is) throws ImageReadException, IOException {
        final int size = readInt(is, "Not a Valid ICO File"); // Size (4 bytes), size of this structure (always 40)
        final int width = readInt(is, "Not a Valid ICO File"); // Width (4 bytes), width of the image (same as iconinfo.width)
        final int height = readInt(is, "Not a Valid ICO File"); // Height (4 bytes), scanlines in the color map + transparent map (iconinfo.height * 2)
        final int planes = readShort(is, "Not a Valid ICO File"); // Planes (2 bytes), always 1
        final int bitCount = readShort(is, "Not a Valid ICO File"); // BitCount (2 bytes), 1,4,8,16,24,32 (see iconinfo for details)
        int compression = readInt(is, "Not a Valid ICO File"); // Compression (4 bytes), we don?t use this (0)
        final int sizeImage = readInt(is, "Not a Valid ICO File"); // SizeImage

        final int xPelsPerMeter = readInt(is, "Not a Valid ICO File"); // XPelsPerMeter (4 bytes), we don?t use this (0)
        final int yPelsPerMeter = readInt(is, "Not a Valid ICO File"); // YPelsPerMeter (4 bytes), we don?t use this (0)
        final int colorsUsed = readInt(is, "Not a Valid ICO File"); // ColorsUsed (4 bytes), we don?t use this (0)
        final int colorsImportant = readInt(is, "Not a Valid ICO File"); // ColorsImportant (4 bytes), we don?t
        // use this (0)
        int redMask = 0;
        int greenMask = 0;
        int blueMask = 0;
        int alphaMask = 0;
        if (compression == 3) {
            redMask = readInt(is, "Not a Valid ICO File");
            greenMask = readInt(is, "Not a Valid ICO File");
            blueMask = readInt(is, "Not a Valid ICO File");
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

        final int bitmapPixelsOffset = BITMAPHEADER_SIZE + BITMAPV3INFOHEADER_SIZE + 4 * ((colorsUsed == 0 && bitCount <= 8) ? (1 << bitCount)
                : colorsUsed);
        final int bitmapSize = BITMAPHEADER_SIZE + BITMAPV3INFOHEADER_SIZE + restOfFile.length;

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(bitmapSize);
        baos.write('B');
        baos.write('M');
        writeInt(baos, bitmapSize);
        writeInt(baos, 0);
        writeInt(baos, bitmapPixelsOffset);

        writeInt(baos, BITMAPV3INFOHEADER_SIZE);
        writeInt(baos, width);
        writeInt(baos, height / 2);
        writeShort(baos, planes);
        writeShort(baos, bitCount);
        writeInt(baos, compression);
        writeInt(baos, sizeImage);
        writeInt(baos, xPelsPerMeter);
        writeInt(baos, yPelsPerMeter);
        writeInt(baos, colorsUsed);
        writeInt(baos, colorsImportant);
        writeInt(baos, redMask);
        writeInt(baos, greenMask);
        writeInt(baos, blueMask);
        writeInt(baos, alphaMask);
        baos.write(restOfFile);
        baos.flush();

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
            transparencyMap = readBytes(bmpInputStream, colorMapSizeBytes, "Not a Valid ICO File");
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

    @Override
    public BufferedImage readBufferedImage() throws IOException, ImageReadException {
        return readBitmapIconData(is);
    }
}
