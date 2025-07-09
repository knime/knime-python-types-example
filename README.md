# KNIME® - Python Extension Data Type Example

This is an example of how to add a new data type to the columnar backend (KNIME's Java side) and Python,
so that it can be used in Python scripts in the KNIME AP and in pure-Python KNIME nodes.

## KNIME Extension Data Types in Java and Python

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

## KNIME port types in Python Pure-Python Custom Port Type Example

This repository also contains an example of how to define and use a custom port type entirely in Python for KNIME nodes, located in the `pure-python-ports/` directory.

### What is a Pure-Python Port Type?
A pure-Python port type allows you to define new data exchange formats between nodes without any Java implementation. This is useful for rapid prototyping or for Python-only extensions.

### Example: Box3D Port Type
The example implements a simple `Box3D` port type, representing a 3D box with width, height, and depth. It includes:

- `Box3DSpec`: The port object spec, which could hold metadata (here, just a label)
- `Box3DPortObject`: The port object, which holds the box dimensions
- Nodes to create and consume the `Box3D` port object

#### Usage
- The `Box3D Creator` node creates a `Box3D` port object from user parameters.
- The `Box3D Volume` node takes a `Box3D` port object as input and outputs a table with the computed volume.

See the `pure-python-ports/README.md` and `pure-python-ports/EXAMPLE.md` for details and code listings.

For more details on custom port types, see the examples in the `knime-python-nodes-testing` repository.

## Java/Python Hybrid Port Type Example

A cross-language port type example is provided in the `ports/` directory. This demonstrates how to implement a custom port type (here, a 3D bounding box) with both a Java ValueFactory and a Python ValueFactory, allowing seamless data exchange between Java and Python nodes in KNIME.

- The Java implementation is in `ports/java/BoundingBoxPortObject.java` and `BoundingBoxValueFactory.java`.
- The Python implementation is in `ports/python/bounding_box_port.py` and `bounding_box_nodes.py`.
- Both implementations use the same serialization format (three doubles for width, height, depth).
- The port type is registered in both the Java and Python plugin.xml files, referencing the same ValueFactory.

This is similar to the Databricks port object, but for a minimal bounding box example.

## Repository Content

The contents of this repository contain a project with an example data type that behaves like a 3D box,
it has width, height and depth and provides methods to compute its volume.

The code is organized as follows:

* `org.knime.features.pythontypeexample`: The feature that bundles the Java and Python type plugins and which should be included as dependency into your code if you are working with the pythontypeexample data types.
* `org.knime.pythontypeexample.core`: The plugin that implements the Java type implementation.
* `org.knime.pythontypeexample.python`: The plugin that contains the Python type implementation.
* `org.knime.update.pythontypeexample`: The plugin that builds an update site so that the feature can be installed from within KNIME


## Development Notes

* For more examples of Java `ValueFactory`s, please see e.g. https://bitbucket.org/KNIME/knime-base/src/master/org.knime.time/src/org/knime/core/data/v2/time/
* For more examples of the Python implementations, please see https://bitbucket.org/KNIME/knime-python/src/master/org.knime.python3.arrow.types/plugin.xml

## Join the Community

* [KNIME Forum](https://forum.knime.com/c/community-extensions/)

