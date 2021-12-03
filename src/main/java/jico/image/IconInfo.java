package jico.image;

public class IconInfo {
    /*public final byte width;
    public final byte height;
    public final byte colorCount;
    public final byte reserved;
    public final int planes;
    public final int bitCount;*/
    public final int imageSize;
    public final int imageOffset;

    public IconInfo(/*final byte width, final byte height,
                    final byte colorCount, final byte reserved, final int planes,
                    final int bitCount,*/ final int imageSize, final int imageOffset) {
        /*this.width = width;
        this.height = height;
        this.colorCount = colorCount;
        this.reserved = reserved;
        this.planes = planes;
        this.bitCount = bitCount;*/
        this.imageSize = imageSize;
        this.imageOffset = imageOffset;
    }
}
