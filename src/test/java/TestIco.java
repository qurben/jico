import jico.Ico;
import jico.ImageReadException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

public class TestIco {
    /**
     * Calling getAllIcoImages does not throw an exception.
     */
    @Test
    public void testNoExceptionThrown() throws IOException, ImageReadException {
        Ico.read(getClass().getClassLoader().getResourceAsStream("github.ico"));
    }

    @Test
    public void testIcoFile() throws URISyntaxException, IOException, ImageReadException {
        List<BufferedImage> images = Ico.read(new File(Objects.requireNonNull(getClass().getClassLoader().getResource("github.ico")).toURI()));

        Assertions.assertNotEquals(0, images.size());
    }

    /**
     * At least one image in the favicon at github.com
     */
    @Test
    public void testIcoUrl() throws IOException, ImageReadException {
        List<BufferedImage> images = Ico.read(new URL("https://github.com/favicon.ico"));

        Assertions.assertNotEquals(0, images.size());
    }

    /**
     * github.ico contains two images.
     */
    @Test
    public void testNumberOfImages() throws IOException, ImageReadException {
        Assertions.assertEquals(2, Ico.read(getClass().getClassLoader().getResourceAsStream("github.ico")).size());
    }

    /**
     * github.ico contains two different images.
     */
    @Test
    public void testImageSizes() throws IOException, ImageReadException {
        List<BufferedImage> images = Ico.read(getClass().getClassLoader().getResourceAsStream("github.ico"));

        BufferedImage img1 = images.get(0);
        BufferedImage img2 = images.get(1);

        Assertions.assertEquals(16, img1.getWidth());
        Assertions.assertEquals(16, img1.getHeight());

        Assertions.assertEquals(32, img2.getWidth());
        Assertions.assertEquals(32, img2.getHeight());
    }

    /**
     * multi.ico contains 10 different icons. Encoded in 10 different ways.
     */
    @Test
    public void testImageTypes() throws IOException, ImageReadException {
        List<BufferedImage> images = Ico.read(getClass().getClassLoader().getResourceAsStream("multi.ico"));

        Assertions.assertEquals(10, images.size());

        for (BufferedImage img : images) {
            Assertions.assertEquals(32, img.getWidth());
            Assertions.assertEquals(32, img.getHeight());
        }

        for (BufferedImage img : images.subList(0, 5)) {
            Assertions.assertEquals(BufferedImage.TYPE_INT_ARGB, img.getType());
        }

        for (BufferedImage img : images.subList(5, 10)) {
            Assertions.assertEquals(BufferedImage.TYPE_4BYTE_ABGR, img.getType());
        }
    }

    /**
     * bmp.ico contains 1 (one) image.
     */
    @Test
    public void testBmpIco() throws IOException, ImageReadException {
        List<BufferedImage> images = Ico.read(getClass().getClassLoader().getResourceAsStream("bmp.ico"));

        Assertions.assertEquals(1, images.size());
    }

    /**
     * bmp.bmp and bmp.ico are based on the exact same image. Make sure that this is actually reflected in the loaded images.
     */
    @Test
    public void testBmpEqual() throws IOException, ImageReadException {
        BufferedImage icoImage = Ico.read(getClass().getClassLoader().getResourceAsStream("bmp.ico")).get(0);

        BufferedImage bmpImage = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("bmp.bmp")));

        DataBuffer bmpBuffer = bmpImage.getData().getDataBuffer();
        DataBuffer icoBuffer = icoImage.getData().getDataBuffer();

        Assertions.assertEquals(bmpBuffer.getSize(), icoBuffer.getSize());

        // Bitwise comparison of the images.
        for (int i = 0; i < bmpBuffer.getSize(); i++) {
            Assertions.assertEquals(bmpBuffer.getElem(i), icoBuffer.getElem(i));
        }
    }

    @Test
    public void testReadInvalid() throws IOException {
        try {
            Ico.read(getClass().getClassLoader().getResourceAsStream("jpg.jpg"));
            Assertions.fail("Expected an exception to be thrown.");
        } catch (IOException | ImageReadException ex) {
            Assertions.assertEquals("Not a Valid ICO File: reserved is -9985", ex.getMessage());
        }
    }
}
