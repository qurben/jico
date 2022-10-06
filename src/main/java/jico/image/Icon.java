package jico.image;

import jico.ImageReadException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public interface Icon {
    BufferedImage readBufferedImage(int size, InputStream inputStream)
            throws ImageReadException, IOException;

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
     * @param inputStream Input stream containing an image file.
     * @return An ImageFormat, such as ImageFormat.IMAGE_FORMAT_JPEG. Returns
     * ImageFormat.IMAGE_FORMAT_UNKNOWN if the image type cannot be
     * determined.
     * @throws ImageReadException in the event of an unsuccessful
     *                            attempt to read the image data
     * @throws IOException        in the event of an unrecoverable I/O condition.
     */
    static Icon detect(InputStream inputStream) throws IOException, ImageReadException {
        inputStream.mark(2);
        final int i1 = inputStream.read();
        final int i2 = inputStream.read();
        inputStream.reset();
        if ((i1 < 0) || (i2 < 0)) {
            throw new ImageReadException(
                    "Couldn't read magic numbers to guess format.");
        }

        final int b1 = i1 & 0xff;
        final int b2 = i2 & 0xff;
        final int[] bytePair = {b1, b2,};

        if ((0x89 == bytePair[0]) && (0x50 == bytePair[1])) {
            return new PNGIcon();
        }

        return new BMPIcon();
    }
}
