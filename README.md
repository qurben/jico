# JICO

[![Java CI with Maven](https://github.com/qurben/jico/actions/workflows/maven.yml/badge.svg)](https://github.com/qurben/jico/actions/workflows/maven.yml)

A tiny library to read .ico files in java. Uses `ImageIO` to parse the contained png and bmp files. This package is only about **16KB** and focuses only on loading .ico images.

The .ico parser is based on code from [`org.apache.commons:commons-imaging`](https://github.com/apache/commons-imaging).

## Download

You can download binaries from the releases page.

Alternatively you can pull it from the central Maven repositories:

```xml
<dependency>
  <groupId>io.github.qurben</groupId>
  <artifactId>jico</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Usage

This library comes with one method that can be used to read a file, input stream or url containing a .ico file to a list of images.

The main usecase is reading a .ico file and setting it as the icon on a Swing JFrame, but it can also be

Use the `jico.Ico.read(...)` to read a list of `java.awt.image.BufferedImage` from a `File`, `URL` or `InputStream`.

```
JFrame frame = new JFrame();
frame.setIconImages(Ico.read(getClass().getClassLoader().getResourceAsStream("favicon.ico")));
```

## License

This code is under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0).

See the `NOTICE.md` file for required notices and attributions.