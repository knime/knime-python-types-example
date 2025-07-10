import knime.extension as knext
import knime.ports.boundingbox as bb

import pyarrow as pa

# get the port type registered for our Java BoundingBoxPortObject
bounding_box_port_type = knext.nodes.get_port_type_for_id(
    "org.knime.pythonportexample.core.BoundingBoxPortObject"
)

def is_numeric(c: knext.Column):
    return c.ktype in [knext.int32(), knext.int64(), knext.double()]

@knext.node(
    name="BoundingBox Extender",
    node_type=knext.NodeType.MANIPULATOR,
    icon_path="icon.png",
    category="/community",
)
@knext.input_port(
    "BoundingBox Input",
    "The bounding box before extending it by the data in the table",
    bounding_box_port_type,
)
@knext.input_table(
    "Input Table", "The table with data that contains x, y, and z coordinates"
)
@knext.output_port(
    "BoundingBox Output",
    "The bounding box surrounding the data in the table and the input bounding box",
    bounding_box_port_type,
)
class BoundingBoxExtender(knext.PythonNode):
    """
    Extend the input bounding box so that it also contains all points in the table
    """

    x_column = knext.ColumnParameter(
        "X Coordinates",
        "The column in the input table that provides the X coordinate",
        column_filter=is_numeric,
        port_index=1,
    )

    y_column = knext.ColumnParameter(
        "X Coordinates",
        "The column in the input table that provides the X coordinate",
        column_filter=is_numeric,
        port_index=1,
    )

    z_column = knext.ColumnParameter(
        "X Coordinates",
        "The column in the input table that provides the X coordinate",
        column_filter=is_numeric,
        port_index=1,
    )

    def configure(self, config_context, bounding_box_spec, table_spec):
        return bb.BoundingBoxSpec()

    def execute(self, exec_context, bounding_box: bb.BoundingBoxPortObject, table):
        df = table.to_pandas([self.x_column, self.y_column, self.z_column])

        for _, row in df.iterrows():
            if row[self.x_column] < bounding_box.min.x:
                bounding_box.min.x = row[self.x_column]
            if row[self.x_column] > bounding_box.max.x:
                bounding_box.max.x = row[self.x_column]

            if row[self.y_column] < bounding_box.min.y:
                bounding_box.min.y = row[self.y_column]
            if row[self.y_column] > bounding_box.max.y:
                bounding_box.max.y = row[self.y_column]

            if row[self.z_column] < bounding_box.min.z:
                bounding_box.min.z = row[self.z_column]
            if row[self.z_column] > bounding_box.max.z:
                bounding_box.max.z = row[self.z_column]

        return bounding_box


@knext.node(
    name="BoundingBox Volume",
    node_type=knext.NodeType.MANIPULATOR,
    icon_path="icon.png",
    category="Examples/Java-Python Ports",
)
@knext.input_port(
    "BoundingBox Input", "A bounding box port object", bounding_box_port_type
)
@knext.output_table("Volume Table", "Table with box volume")
class BoundingBoxVolume(knext.PythonNode):
    def configure(self, config_context, spec: bb.BoundingBoxSpec):
        return knext.Schema.from_columns([
            knext.Column(knext.double(), "Volume")
        ])

    def execute(self, exec_context, box: bb.BoundingBoxPortObject):
        volume = (
            (box.max.x - box.min.x) * (box.max.y - box.min.y) * (box.max.z - box.min.z)
        )
        arr = pa.table({
            "Volume": [volume]
        })
        return knext.Table.from_pyarrow(arr)
