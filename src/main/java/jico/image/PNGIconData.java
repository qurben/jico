package jico.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class PNGIconData implements IconData {
    private final InputStream is;

    public PNGIconData(final InputStream is) {
        this.is = is;
    }

    @Override
    public BufferedImage readBufferedImage() throws IOException {
        return ImageIO.read(is);
    }
}
