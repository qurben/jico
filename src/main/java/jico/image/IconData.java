package jico.image;

import jico.ImageReadException;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface IconData {
    BufferedImage readBufferedImage()
            throws ImageReadException, IOException;
}
