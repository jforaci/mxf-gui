# mxf-gui
A GUI to inspect and extract data from [MXF](https://en.wikipedia.org/wiki/Material_Exchange_Format) files. Grab a [release](https://github.com/jforaci/mxf-gui/releases) and start it (see "Quick start" below for an example).

![image](https://user-images.githubusercontent.com/38170229/144078414-6612b754-da76-4592-8a9f-acd4d6a4d315.png)

The panel on the top-right shows the structural metadata. The tabs on the bottom show information about the file like the entire file's metadata, partitions, track layout, [ancillary data](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.1364-3-201510-I!!PDF-E.pdf) (SMPTE 436m), parsed [708 captions](https://en.wikipedia.org/wiki/CTA-708), and [DolbyE](https://en.wikipedia.org/wiki/Dolby_E) track ac3 metadata. The latter two require the corresponding option to be selected before opening the file.

An example of browsing metadata:

![image](https://user-images.githubusercontent.com/38170229/144508123-8a7d6281-ad80-478f-98ba-c8f643b3450d.png)

mxf-gui uses a library called [mxf-reader](https://github.com/jforaci/mxf-reader). Note: playback isn't supported at this point.

## Quick start
To run MXF GUI:
```Bash
java -jar mxf-gui-v1.0-snapshot.jar
```
You only need [Java](https://www.java.com/en/download/manual.jsp) installed.

Once it starts, you can use the menu or drag an MXF file onto the main window to inspect it.

## Building
Build with [Maven](https://maven.apache.org/):
```Bash
mvn package
```

## Extracting essence
Use the `Export > Essence to files...` menu to extract any of the essence data from the source tracks. You can optionally extract a caption file containing the carryover 608 bytes from the s436m ancillary data track, if it's present.
