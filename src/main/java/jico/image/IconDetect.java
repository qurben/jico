package jico.image;

import jico.ImageReadException;

import java.io.IOException;
import java.io.InputStream;

public final class IconDetect {
    public IconDetect() {}

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
    public IconReader detect(InputStream inputStream) throws IOException, ImageReadException {
        inputStream.mark(2);
        final int i1 = inputStream.read();
        final int i2 = inputStream.read();
        inputStream.reset();
        if ((i1 < 0) || (i2 < 0)) {
            throw new ImageReadException(
                    "Couldn't guess format.");
        }

        if (0x89 == (i1 & 0xff) && 0x50 == (i2 & 0xff)) {
            return new PNGIconReader();
        }

        return new BMPIconReader();
    }
}
