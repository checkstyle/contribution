///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2026 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
///////////////////////////////////////////////////////////////////////////////////////////////

package com.github.checkstyle.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

/**
 * Utility class for StAX parser routines.
 *
 * @author attatrol
 *
 */
final class StaxUtils {

    /**
     * Private ctor, use static methods instead.
     */
    private StaxUtils() {

    }

    /**
     * Creates parser linked to the existing XML file.
     *
     * @param xmlFilename
     *        name of an XML report file.
     * @return StAX parser interface.
     * @throws FileNotFoundException
     *         on wrong filename.
     * @throws XMLStreamException
     *         on internal factory failure.
     */
    public static XMLEventReader createReader(Path xmlFilename)
            throws FileNotFoundException, XMLStreamException {
        final XMLEventReader result;

        if (xmlFilename == null) {
            result = new EmptyXmlEventReader();
        }
        else {
            final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Setup a new eventReader
            final InputStream inputStream =
                new FileInputStream(xmlFilename.toFile());
            result = inputFactory.createXMLEventReader(inputStream);
        }

        return result;
    }

}
