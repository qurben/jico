package jico.image;

import jico.ImageReadException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public interface IconReader {
    BufferedImage readBufferedImage(int size, InputStream inputStream)
            throws ImageReadException, IOException;
}
