///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2024 the original author or authors.
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

import java.util.NoSuchElementException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Simple {@link XMLEventReader} with no elements.
 *
 * @author Richard Veach
 */
public final class EmptyXmlEventReader implements XMLEventReader {
    @Override
    public Object next() {
        throw new NoSuchElementException();
    }

    @Override
    public XMLEvent nextEvent() throws XMLStreamException {
        throw new NoSuchElementException();
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
        return null;
    }

    @Override
    public String getElementText() throws XMLStreamException {
        return null;
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        return null;
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return null;
    }

    @Override
    public void close() throws XMLStreamException {
    }
}
