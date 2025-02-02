package jico.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

class PNGIconReader implements IconReader {
    @Override
    public BufferedImage readBufferedImage(int imageSize, final InputStream is) throws IOException {
        return ImageIO.read(new BufferedInputStream(is, imageSize));
    }
}
