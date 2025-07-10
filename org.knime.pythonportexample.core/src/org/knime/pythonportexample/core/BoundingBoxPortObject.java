package org.knime.pythonportexample.core;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

/**
 * A KNIME PortObject representing a 3D bounding box defined by two points (min and max).
 * The bounding box is serialized as two Point3D records (min and max).
 *
 * @author Carsten Haubold, KNIME GmbH, Konstanz, Germany
 */
public class BoundingBoxPortObject extends AbstractSimplePortObject {

    /** Accessor for the type */
    @SuppressWarnings("hiding")
    public static PortType TYPE = PortTypeRegistry.getInstance().getPortType(BoundingBoxPortObject.class);

    /**
     * Immutable 3D point record used for bounding box corners.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public static record Point3D(double x, double y, double z) {
        @Override
        public String toString() {
            return "[" + this.x + ", " + this.y + ", " + this.z + "]";
        }
    }

    /** Serializer as required by ext point definition. */
    public static final class Serializer extends AbstractSimplePortObjectSerializer<BoundingBoxPortObject> {
    }

    private Point3D m_min;
    private Point3D m_max;

    /**
     * Constructs a bounding box from two 3D points.
     * @param min the minimum (lower) corner of the bounding box
     * @param max the maximum (upper) corner of the bounding box
     */
    public BoundingBoxPortObject(final Point3D min, final Point3D max) {
        this.m_min = min;
        this.m_max = max;
    }

    /**
     * Empty Constructor needed for deserialization
     */
    public BoundingBoxPortObject() {
    }

    /**
     * @return the minimum (lower) corner of the bounding box
     */
    public Point3D getMin() { return m_min; }
    /**
     * @return the maximum (upper) corner of the bounding box
     */
    public Point3D getMax() { return m_max; }

    @Override
    public String getSummary() {
        return "BoundingBox{" + this.m_min + " -> " + this.m_max + "}";
    }

    @Override
    public PortObjectSpec getSpec() {
        return new BoundingBoxPortObjectSpec();
    }

    /**
     * Show a string representation of the Bounding Box as "view"
     *
     * NB: We still need to provide a swing GUI for legacy reasons. This will be
     * replaced by a web UI soon.
     */
    @Override
    public JComponent[] getViews() {
        javax.swing.JLabel label = new javax.swing.JLabel(getSummary());
        label.setName("BoundingBoxPortObject");
        return new JComponent[] { label };
    }

    @Override
    protected void save(final ModelContentWO model, final ExecutionMonitor exec) throws CanceledExecutionException {
        model.addDouble("minx", m_min.x);
        model.addDouble("miny", m_min.y);
        model.addDouble("minz", m_min.z);
        model.addDouble("maxx", m_max.x);
        model.addDouble("maxy", m_max.y);
        model.addDouble("maxz", m_max.z);
    }

    @Override
    protected void load(final ModelContentRO model, final PortObjectSpec spec, final ExecutionMonitor exec)
        throws InvalidSettingsException, CanceledExecutionException {
        this.m_min = new Point3D(model.getDouble("minx"), model.getDouble("miny"), model.getDouble("minz"));
        this.m_max = new Point3D(model.getDouble("maxx"), model.getDouble("maxy"), model.getDouble("maxz"));
    }
}
