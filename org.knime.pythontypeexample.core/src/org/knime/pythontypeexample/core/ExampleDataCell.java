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

import java.io.IOException;
import java.util.Objects;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;

/**
 * Example DataCell that should also be accessible from Python.
 *
 * @author Carsten Haubold, KNIME GmbH, Konstanz, Germany
 */
public final class ExampleDataCell extends DataCell implements ExampleDataValue {

    private static final long serialVersionUID = 1L;

    private final double m_width;

    private final double m_height;

    private final double m_depth;

    /**
     * Create an ExampleDataCell
     * @param width
     * @param height
     * @param depth
     */
    public ExampleDataCell(final double width, final double height, final double depth) {
        m_width = width;
        m_height = height;
        m_depth = depth;
    }

    @Override
    public double getHeight() {
        return m_height;
    }

    @Override
    public double getWidth() {
        return m_width;
    }

    @Override
    public double getDepth() {
        return m_depth;
    }

    @Override
    public double getVolume() {
        return m_width * m_height * m_depth;
    }

    @Override
    public String toString() {
        return String.format("%fm x %fm x %fm", m_width, m_height, m_depth);
    }

    @Override
    protected boolean equalsDataCell(final DataCell dc) {
        var edc = (ExampleDataCell)dc;
        return m_width == edc.m_width && m_height == edc.m_height && m_depth == edc.m_depth;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_width, m_height, m_depth);
    }

    /**
     * Serializer for {@link ExampleDataCells}s.
     *
     * @noreference This class is not intended to be referenced by clients.
     */
    public static final class CellSerializer implements DataCellSerializer<ExampleDataCell> {
        @Override
        public void serialize(final ExampleDataCell cell, final DataCellDataOutput output) throws IOException {
            output.writeDouble(cell.getWidth());
            output.writeDouble(cell.getHeight());
            output.writeDouble(cell.getDepth());

        }

        @Override
        public ExampleDataCell deserialize(final DataCellDataInput input) throws IOException {
            return new ExampleDataCell(input.readDouble(), input.readDouble(), input.readDouble());
        }
    }

}