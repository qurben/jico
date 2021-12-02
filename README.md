# JICO

Read .ico files in java.

```
import jico.Ico;

...

JFrame frame = new JFrame();
frame.setIconImages(Ico.getAllIcoImages(getClass().getClassLoader().getResourceAsStream("favicon.ico")));
```

Ico parsing is based on [`org.apache.commons:commons-imaging`](https://github.com/apache/commons-imaging).