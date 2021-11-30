# mxf-gui
A GUI to inspect and extract data from [MXF](https://en.wikipedia.org/wiki/Material_Exchange_Format) files.

The panel on the top-right shows the structural metadata. The tabs on the bottom show information about the file like the entire file's metadata, partitions, track layout, ancillary data (SMPTE 436m), parsed 708 captions, and DolbyE track ac3 metadata. The latter two require the corresponding option to be selected before opening the file.

It uses a library called [mxf-reader](https://github.com/jforaci/mxf-reader). Note: playback isn't supported at this point.

## Quick start
Build with [Maven](https://maven.apache.org/) and run MXF GUI:
```Bash
mvn package
java -jar target/mxf-gui-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Once it starts, you can use the menu or drag an MXF file onto the main window to inspect it.

## Extracting essence
Use the menu to extract any of the essence data from the source tracks. You can optionally extract a caption file containing the carryover 608 bytes from the s436m ancillary data track, if it's present.
