package jico;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageFormatGuesser {
    private static final int[] MAGIC_NUMBERS_PNG = { 0x89, 0x50, };
    private static final int[] MAGIC_NUMBERS_BMP = { 0x42, 0x4d, };

    /**
     * Attempts to determine the image format of a file based on its
     * "magic numbers," the first bytes of the data.
     * <p>Many graphics format specify identifying byte
     * values that appear at the beginning of the data file.  This method
     * checks for such identifying elements and returns a ImageFormat
     * enumeration indicating what it detects. Note that this
     * method can return "false positives" in cases where non-image files
     * begin with the specified byte values.
     *
     * @param bytes  Byte array containing an image file.
     * @return An ImageFormat, such as ImageFormat.IMAGE_FORMAT_JPEG. Returns
     *         ImageFormat.IMAGE_FORMAT_UNKNOWN if the image type cannot be
     *         determined.
     * @throws ImageReadException in the event of an unsuccessful
     *         attempt to read the image data
     * @throws IOException in the event of an unrecoverable I/O condition.
     */
    public ImageFormat guessFormat(final byte[] bytes)
            throws ImageReadException, IOException {
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            final int i1 = is.read();
            final int i2 = is.read();
            if ((i1 < 0) || (i2 < 0)) {
                throw new ImageReadException(
                        "Couldn't read magic numbers to guess format.");
            }

            final int b1 = i1 & 0xff;
            final int b2 = i2 & 0xff;
            final int[] bytePair = { b1, b2, };

            if (compareBytePair(MAGIC_NUMBERS_PNG, bytePair)) {
                return ImageFormats.PNG;
            }
            if (compareBytePair(MAGIC_NUMBERS_BMP, bytePair)) {
                return ImageFormats.BMP;
            }

            return ImageFormats.UNKNOWN;
        }
    }

    private boolean compareBytePair(final int[] a, final int[] b) {
        if (a.length != 2 && b.length != 2) {
            throw new RuntimeException("Invalid Byte Pair.");
        }
        return (a[0] == b[0]) && (a[1] == b[1]);
    }
}
