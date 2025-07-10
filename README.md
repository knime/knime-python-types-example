# KNIME - Examples for extending KNIME's data and port types in Python

## KNIME Extension Data Types in Java and Python

This is an example of how to add a new data type to the columnar backend (KNIME's Java side) and Python,
so that it can be used in Python scripts in the KNIME AP and in pure-Python KNIME nodes.

Tabular data is passed between the KNIME AP and Python using files on disk in the Apache Arrow IPC format.
This is the exact format, that KNIME's columnar table backend uses, which is the reason why tables are always
converted to the columnar backend in KNIME, no matter whether the workflow operates using the "Row-based" or "Columnar"
backend. That also explains why using Python nodes should generally be faster when using the columnar backend:
no conversion is needed.

To make sure that data types can be understood by KNIME as well as Python we need to define two things:

* a Java `ValueFactory` for the data type to make it a "known type" in the columnar backend
* a Python `PythonValueFactory` that provides a Python interface for the type

Both, the Java and Python implementations must serialize and deserialize to the same underlying
data structure. This data can be made up of primitive types known to Apache Arrow as well as lists 
and structs of these.

The Java and Python `ValueFactory`s must be registered at their respective extension points:

* for the Java type, the DataType is registered at extension point org.knime.core.DataType in 
  the plugin's `plugin.xml`. There we need to add `factoryValue=<fully-classified-ValueFactory-name>`
  like this:

  ```
  <extension point="org.knime.core.DataType">
      <DataType
            cellClass="org.knime.core.data.time.localtime.LocalTimeCell"
            factoryClass="org.knime.core.data.time.localtime.LocalTimeCellFactory"
            factoryValue="org.knime.core.data.v2.time.LocalTimeValueFactory">
         <serializer
               cellClass="org.knime.core.data.time.localtime.LocalTimeCell"
               serializerClass="org.knime.core.data.time.localtime.LocalTimeCellSerializer">
         </serializer>
      </DataType>
  </extension>
  ```

    * the `cellClass` defines the class that represents the content of a value in a table cell. The value type is 
    defined by an interface deriving from `DataValue`.
    * the `factoryClass` is optional and can be used to provide factory methods to create instances of the `cellClass`.
    * the `factoryValue` is equivalent to a dedicated `ValueFactory` element and references the class that contains the
      Columnar Backend `ValueFactory` for the data type.
    * the `serializer` is needed for classic KNIME I/O -- when no Columnar Backend is involved. It is necessary even if
      only the Columnar Backend is used.

* for the Python type, the value factory is part of a module that we need to load when Python is 
  initialized. This file is usually part of a dedicated plugin. All types in that file are then 
  registered at the extension point `org.knime.python3.types.PythonValueFactory` in the `plugin.xml` of 
  this plugin:

  ```
  <extension point="org.knime.python3.types.PythonValueFactory">
      <Module modulePath="src/main/python" moduleName="knime.types.builtin">

         <PythonValueFactory
               PythonClassName="LocalTimeValueFactory"
               ValueFactory="org.knime.core.data.v2.time.LocalTimeValueFactory"
               ValueTypeName="datetime.time">
         </PythonValueFactory>
      </Module>
  </extension>
  ```

    * The `Module` element references a Python file that can be imported using the `moduleName` and resides at the 
      given `modulePath`. It can contain many `PythonValueFactory`s.
    * Each `PythonValueFactory` makes one data type available to use in Python.

## KNIME PortObject conversion between Java and Python (experimental)

`PortObject`s are the containers that transport data between KNIME nodes, and `PortObjectSpecs` ship the specifications that are already available after running `configure()`. The most prominent implementation of the PortObject is a table, which in KNIME terms is called a `BufferedDataTable` with the corresponding `DataTableSpec`. Other examples are trained models, DB connections or images, which you can visually distinguish by the square port at a KNIME node with a dedicated color per port.

It was possible for a long time already to build custom `PortObject`s in Java. These also get registered at an extension point (similar to the type example above). They need to consist of a `PortObject` and a `PortObjectSpec`, have a serializer, and need to have a color and a name for display. 

For pure-Python KNIME nodes, dedicated `PortObjects` can be created that can be read and written by Python-nodes only. This is useful for e.g. all nodes of a Python-based extension that work with the same topic. See https://docs.knime.com/latest/pure_python_node_extensions_guide/index.html#_node_port_configuration for more details.

Since KNIME AP 5.5 there is experimental support for using `PortObject`s  from Java also in Python, and in 5.6 we fixed a few issues so that we can also create those `PortObject`s in Python and send them to Java. To accomplish that cross-language communication, there must be an `Encoder` on the sender and a `Decoder` on the other side with an intermediate representation that can currently be a `StringIntermediateRepresentation` or an `EmptyIntermediateRepresentation` if no data needs to be sent over (e.g. for an empty spec, or an authentication `PortObject` without data). These `Encoder`s and `Decoders` get registered as `Converter`s at the `org.knime.python3.types.PythonPortObjectConverter` extension point like so

```xml
    <extension point="org.knime.python3.types.PythonPortObjectConverter">
        <Module modulePath="src/main/python" moduleName="knime.ports.boundingbox">
            <KnimeToPythonPortObjectConverter
                JavaConverterClass="org.knime.pythonportexample.core.BoundingBoxPortObjectConverter"
                PythonConverterClass="BoundingBoxPortConverter">
            </KnimeToPythonPortObjectConverter>
            <PythonToKnimePortObjectConverter
                JavaConverterClass="org.knime.pythonportexample.core.BoundingBoxPortObjectConverter"
                PythonConverterClass="BoundingBoxPortConverter">
            </PythonToKnimePortObjectConverter>
        </Module>
    </extension>
```

As you can see, there is a `modulePath` which will be put on the `PYTHONPATH`, and from there the import `knime.ports.boundingbox` is loaded, which means on disk the `modulePath` must contain `<modulePath>/knime/ports/boundingbox.py` which in turn contains a class named `BoundingBoxPortConverter`. On the Java side, we specify the path to the converter with its fully qualified name `org.knime.pythonportexample.core.BoundingBoxPortObjectConverter`.

> **Note:** This is functionality is still experimental and might change in the future.

The example in this repo here is a _Bounding Box_ `PortObject` that contains min and max `x`, `y`, and `z` coordinates. There is one Java-based node that can create a bounding box, and two Python based nodes that can work with and modify this `PortObject`.

The `BoundingBoxPortObjectConverter` converter uses a `StringIntermediateRepresentation` for the `PortObject` content where all coordinates get serialized to JSON. As we want to be able to send these `PortObject` from Java to Python and back, we need to implement an `Encoder` and a `Decoder` on both sides.

> **Note:** `PortObject`s still have an old-school Java view that is used here to show the min and max of the bounding box, implemented in the `getViews` method on the Java side.

# Repository Content

The contents of this repository contain a project with an example data type that behaves like a 3D box,
it has width, height and depth and provides methods to compute its volume.

The code is organized as follows:

* `org.knime.features.pythontypeexample`: The feature that bundles the Java and Python type plugins and which should be included as dependency into your code if you are working with the pythontypeexample data types.
* `org.knime.pythontypeexample.core`: The plugin that implements the Java type implementation.
* `org.knime.pythontypeexample.python`: The plugin that contains the Python type implementation.
* `org.knime.pythonportexample.core`: implements the Java side of the PortObject and a Java node that uses it
* `org.knime.pythonportexample.python`: contains the Python PortObject implementation and Python nodes that use it
* `org.knime.update.pythontypeexample`: The plugin that builds an update site so that the feature can be installed from within KNIME


# Development Notes

* For more examples of Java `ValueFactory`s, please see e.g. https://bitbucket.org/KNIME/knime-base/src/master/org.knime.time/src/org/knime/core/data/v2/time/
* For more examples of the Python implementations, please see https://bitbucket.org/KNIME/knime-python/src/master/org.knime.python3.arrow.types/plugin.xml

# Join the Community

* [KNIME Forum](https://forum.knime.com/c/community-extensions/)

