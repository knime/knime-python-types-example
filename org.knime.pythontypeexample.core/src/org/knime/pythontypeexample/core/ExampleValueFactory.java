
/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 */

package org.knime.pythontypeexample.core;

import org.knime.core.data.DataCell;
import org.knime.core.data.v2.ReadValue;
import org.knime.core.data.v2.ValueFactory;
import org.knime.core.data.v2.WriteValue;
import org.knime.core.table.access.DoubleAccess.DoubleReadAccess;
import org.knime.core.table.access.DoubleAccess.DoubleWriteAccess;
import org.knime.core.table.access.StructAccess.StructReadAccess;
import org.knime.core.table.access.StructAccess.StructWriteAccess;
import org.knime.core.table.schema.DataSpec;
import org.knime.core.table.schema.StructDataSpec;

/**
 * {@link ValueFactory} implementation for ExampleDataCell
 *
 * @author Carsten Haubold, KNIME GmbH, Konstanz, Germany
 */
public final class ExampleValueFactory implements ValueFactory<StructReadAccess, StructWriteAccess> {

    /** A stateless instance of {@link ExampleValueFactory} */
    public static final ExampleValueFactory INSTANCE = new ExampleValueFactory();

    @Override
    public ReadValue createReadValue(final StructReadAccess access) {
        return new DefaultExampleReadValue(access);
    }

    @Override
    public WriteValue<ExampleDataValue> createWriteValue(final StructWriteAccess access) {
        return new DefaultExampleWriteValue(access);
    }

    @Override
    public StructDataSpec getSpec() {
        return new StructDataSpec(DataSpec.doubleSpec(), DataSpec.doubleSpec(), DataSpec.doubleSpec());
    }

    private static final class DefaultExampleReadValue implements ReadValue, ExampleDataValue {
        private final DoubleReadAccess m_width;
        private final DoubleReadAccess m_height;
        private final DoubleReadAccess m_depth;

        private DefaultExampleReadValue(final StructReadAccess access) {
            m_width = access.getAccess(0);
            m_height = access.getAccess(1);
            m_depth = access.getAccess(2);
        }

        @Override
        public DataCell getDataCell() {
            return new ExampleDataCell(m_width.getDoubleValue(), m_height.getDoubleValue(), m_depth.getDoubleValue());
        }

        @Override
        public double getHeight() {
            return m_height.getDoubleValue();
        }

        @Override
        public double getWidth() {
            return m_width.getDoubleValue();
        }

        @Override
        public double getDepth() {
            return m_depth.getDoubleValue();
        }

        @Override
        public double getVolume() {
            return m_width.getDoubleValue() * m_height.getDoubleValue() * m_depth.getDoubleValue();
        }
    }

    private static final class DefaultExampleWriteValue implements WriteValue<ExampleDataValue> {

        private final DoubleWriteAccess m_width;
        private final DoubleWriteAccess m_height;
        private final DoubleWriteAccess m_depth;

        private DefaultExampleWriteValue(final StructWriteAccess access) {
            m_width = access.getWriteAccess(0);
            m_height = access.getWriteAccess(1);
            m_depth = access.getWriteAccess(2);
        }

        @Override
        public void setValue(final ExampleDataValue value) {
            m_width.setDoubleValue(value.getWidth());
            m_height.setDoubleValue(value.getHeight());
            m_depth.setDoubleValue(value.getDepth());
        }

    }
}