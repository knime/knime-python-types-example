import knime.extension as knext
import knime.ports.boundingbox as bb

import pyarrow as pa

# get the port type registered for our Java BoundingBoxPortObject
bounding_box_port_type = knext.nodes.get_port_type_for_id(
    "org.knime.pythonportexample.core.BoundingBoxPortObject"
)


@knext.node(
    name="BoundingBox Creator",
    node_type=knext.NodeType.SOURCE,
    icon_path="icon.png",
    category="/community",
)
@knext.output_port(
    "BoundingBox Output", "A bounding box port object", bounding_box_port_type
)
class BoundingBoxCreator(knext.PythonNode):
    """
    Create a bounding box from the given parameters
    """

    width = knext.DoubleParameter("Width", "Box width", 1.0)
    height = knext.DoubleParameter("Height", "Box height", 1.0)
    depth = knext.DoubleParameter("Depth", "Box depth", 1.0)
    offset_x = knext.DoubleParameter("X-Offset", "Box start in X", 0.0)
    offset_y = knext.DoubleParameter("Y-Offset", "Box start in Y", 0.0)
    offset_z = knext.DoubleParameter("Z-Offset", "Box start in Z", 0.0)

    def configure(self, config_context):
        return bb.BoundingBoxSpec()

    def execute(self, exec_context):
        return bb.BoundingBoxPortObject(
            bb.BoundingBoxSpec(),
            [self.offset_x, self.offset_y, self.offset_z],
            [self.width, self.height, self.depth],
        )


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
