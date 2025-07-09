package org.knime.pythonportexample.core;

import org.knime.python3.types.port.converter.PortObjectConversionContext;
import org.knime.python3.types.port.converter.PortObjectDecoder;
import org.knime.python3.types.port.converter.PortObjectEncoder;
import org.knime.python3.types.port.converter.PortObjectSpecConversionContext;
import org.knime.python3.types.port.ir.EmptyIntermediateRepresentation;
import org.knime.python3.types.port.ir.JavaEmptyIntermediateRepresentation;
import org.knime.python3.types.port.ir.JavaStringIntermediateRepresentation;
import org.knime.python3.types.port.ir.PortObjectIntermediateRepresentation;
import org.knime.python3.types.port.ir.PortObjectSpecIntermediateRepresentation;
import org.knime.python3.types.port.ir.StringIntermediateRepresentation;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Converter class for encoding and decoding {@link BoundingBoxPortObject} instances to and from intermediate
 * representations for KNIME port object serialization.
 *
 * The intermediate representation is analogously used on the Python side to encode and decode the PortObject.
 *
 * This class implements both {@link PortObjectEncoder} and {@link PortObjectDecoder} interfaces, allowing back and
 * forth conversion between {@link BoundingBoxPortObject} and a StringIntermediateRepresentation which is a serialized
 * JSON array representation.
 *
 * The {@link BoundingBoxPortObjectSpec} is converted via an {@link EmptyIntermediateRepresentation} because it doesn't
 * contain any information that needs to be communicated.
 */
@SuppressWarnings("restriction")
public class BoundingBoxPortObjectConverter
    implements PortObjectEncoder<BoundingBoxPortObject, BoundingBoxPortObjectSpec>,
    PortObjectDecoder<BoundingBoxPortObject, StringIntermediateRepresentation, BoundingBoxPortObjectSpec, EmptyIntermediateRepresentation> {

    @Override
    public Class<BoundingBoxPortObject> getPortObjectClass() {
        return BoundingBoxPortObject.class;
    }

    @Override
    public Class<BoundingBoxPortObjectSpec> getPortObjectSpecClass() {
        return BoundingBoxPortObjectSpec.class;
    }

    @Override
    public BoundingBoxPortObject decodePortObject(final StringIntermediateRepresentation intermediateRepresentation,
        final BoundingBoxPortObjectSpec spec, final PortObjectConversionContext context) {
        try {
            var objectMapper = new ObjectMapper();
            var arr = objectMapper.readTree(intermediateRepresentation.getStringRepresentation());
            if (!arr.isArray() || arr.size() != 6) {
                throw new IllegalArgumentException("Expected JSON array of 6 elements");
            }
            double minX = arr.get(0).asDouble();
            double minY = arr.get(1).asDouble();
            double minZ = arr.get(2).asDouble();
            double maxX = arr.get(3).asDouble();
            double maxY = arr.get(4).asDouble();
            double maxZ = arr.get(5).asDouble();
            BoundingBoxPortObject.Point3D min = new BoundingBoxPortObject.Point3D(minX, minY, minZ);
            BoundingBoxPortObject.Point3D max = new BoundingBoxPortObject.Point3D(maxX, maxY, maxZ);
            return new BoundingBoxPortObject(min, max);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode BoundingBoxPortObject", e);
        }
    }

    @Override
    public PortObjectIntermediateRepresentation encodePortObject(final BoundingBoxPortObject portObject,
        final PortObjectConversionContext context) {
        // Represent as JSON Array of doubles
        var objectMapper = new ObjectMapper();
        var node = objectMapper.createArrayNode();
        node.add(portObject.getMin().x());
        node.add(portObject.getMin().y());
        node.add(portObject.getMin().z());

        node.add(portObject.getMax().x());
        node.add(portObject.getMax().y());
        node.add(portObject.getMax().z());
        return new JavaStringIntermediateRepresentation(node.toString());
    }

    @Override
    public BoundingBoxPortObjectSpec decodePortObjectSpec(
        final EmptyIntermediateRepresentation intermediateRepresentation,
        final PortObjectSpecConversionContext context) {
        return new BoundingBoxPortObjectSpec();
    }

    @Override
    public PortObjectSpecIntermediateRepresentation encodePortObjectSpec(final BoundingBoxPortObjectSpec spec,
        final PortObjectSpecConversionContext context) {
        return JavaEmptyIntermediateRepresentation.INSTANCE;
    }
}
