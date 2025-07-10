import knime.extension as knext
import knime.extension.ports as kp
from typing import Sequence
from dataclasses import dataclass


@dataclass()
class Point3D:
    """3D point for bounding box corners."""

    x: float
    y: float
    z: float

    def __repr__(self):
        return f"[{self.x}, {self.y}, {self.z}]"

    def to_list(self) -> list:
        return [self.x, self.y, self.z]

    @staticmethod
    def from_list(lst: Sequence[float]) -> "Point3D":
        return Point3D(lst[0], lst[1], lst[2])


class BoundingBoxSpec(knext.PortObjectSpec):
    def serialize(self) -> dict:
        # No additional spec data to serialize
        return {}

    @staticmethod
    def deserialize(data: dict) -> "BoundingBoxSpec":
        # No additional spec data to deserialize
        return BoundingBoxSpec()


class BoundingBoxPortObject(knext.PortObject):
    def __init__(
        self,
        spec: BoundingBoxSpec,
        min_point: Sequence[float],
        max_point: Sequence[float],
    ):
        super().__init__(spec)
        self._min = Point3D.from_list(min_point)
        self._max = Point3D.from_list(max_point)

    @property
    def min(self) -> Point3D:
        return self._min

    @property
    def max(self) -> Point3D:
        return self._max

    def __repr__(self):
        return f"BoundingBox{{ {self._min} -> {self._max} }}"

    def serialize(self) -> bytes:
        import json

        return json.dumps(self.min.to_list() + self.max.to_list()).encode()

    @classmethod
    def deserialize(
        cls, spec: BoundingBoxSpec, storage: bytes
    ) -> "BoundingBoxPortObject":
        import json

        arr = json.loads(storage.decode())
        if not isinstance(arr, list) or len(arr) != 6:
            raise ValueError("Expected JSON array of 6 elements for bounding box")
        min_pt = [arr[0], arr[1], arr[2]]
        max_pt = [arr[3], arr[4], arr[5]]

        return cls(spec, min_pt, max_pt)


class BoundingBoxPortConverter(
    kp.PortObjectDecoder[
        BoundingBoxPortObject,
        kp.StringIntermediateRepresentation,
        BoundingBoxSpec,
        kp.EmptyIntermediateRepresentation,
    ],
    kp.PortObjectEncoder[
        BoundingBoxPortObject,
        kp.StringIntermediateRepresentation,
        BoundingBoxSpec,
        kp.EmptyIntermediateRepresentation,
    ],
):
    """
    Converter between KNIME Java BoundingBoxPortObject and Python BoundingBoxPortObject.
    """

    def __init__(self):
        # call super constructors so that the proper object and spec types get registered
        kp.PortObjectDecoder.__init__(self, BoundingBoxPortObject, BoundingBoxSpec)
        kp.PortObjectEncoder.__init__(self, BoundingBoxPortObject, BoundingBoxSpec)

    def decode_spec(self, intermediate_representation):
        return BoundingBoxSpec()

    def decode_object(
        self, intermediate_representation: kp.StringIntermediateRepresentation, spec
    ):
        import json

        arr = json.loads(intermediate_representation.getStringRepresentation())
        if not isinstance(arr, list) or len(arr) != 6:
            raise ValueError("Expected JSON array of 6 elements for bounding box")
        min_pt = [arr[0], arr[1], arr[2]]
        max_pt = [arr[3], arr[4], arr[5]]
        return BoundingBoxPortObject(spec, min_pt, max_pt)

    def encode_object(
        self, port_object: BoundingBoxPortObject
    ) -> kp.StringIntermediateRepresentation:
        import json

        return kp.StringIntermediateRepresentation(
            json.dumps(port_object.min.to_list() + port_object.max.to_list())
        )

    def encode_spec(self, spec):
        # Spec doesn't need to be serialized
        return None
