package org.knime.pythonportexample.core;

import javax.swing.JComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;

/**
 * The BoundingBoxPortObjectSpec does not contain any specific info, as the
 * min and max coordinates of the bounding box are part of the PortObject.
 */
public class BoundingBoxPortObjectSpec extends AbstractSimplePortObjectSpec {

    /** Serializer as required by extension point. */
    public static final class Serializer
        extends AbstractSimplePortObjectSpecSerializer<BoundingBoxPortObjectSpec> {
    }

    /**
     * NB: We could provide a swing GUI for legacy reasons. This will be
     * replaced by a web UI soon. But we don't want to show a view for the spec
     */
    @Override
    public JComponent[] getViews() {
        return new JComponent[] {};
    }

    @Override
    protected void save(final ModelContentWO model) {
    }

    @Override
    protected void load(final ModelContentRO model) throws InvalidSettingsException {
    }
}
