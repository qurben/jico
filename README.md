# JICO

Read .ico files in java. Uses `javax.imageio.ImageIO` to parse the contained png and bmp files.

The .ico parser is based on [`org.apache.commons:commons-imaging`](https://github.com/apache/commons-imaging).

## Usage

```
import jico.Ico;

...

JFrame frame = new JFrame();
frame.setIconImages(Ico.getAllIcoImages(getClass().getClassLoader().getResourceAsStream("favicon.ico")));
```
