////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2016 the original author or authors.
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
////////////////////////////////////////////////////////////////////////////////

package com.github.checkstyle.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.github.checkstyle.data.CheckstyleRecord;
import com.github.checkstyle.data.ParsedContent;
import com.github.checkstyle.data.Severity;
import com.github.checkstyle.data.StatisticsHolder;

/**
 * Contains logics of the StaX parser for the checkstyle xml reports.
 * If its scheme changed, this class should be the first one to fix.
 *
 * @author atta_troll
 */
public final class StaxParserProcessor {

    /**
     * String value for "file" tag.
     */
    private static final String FILE_TAG = "file";

    /**
     * String value for "error" tag.
     */
    private static final String ERROR_TAG = "error";

    /**
     * String value for "name" attribute.
     */
    private static final String FILENAME_ATTR = "name";

    /**
     * String value for "line" attribute.
     */
    private static final String LINE_ATTR = "line";

    /**
     * String value for "column" attribute.
     */
    private static final String COLUMN_ATTR = "column";

    /**
     * String value for "severity" attribute.
     */
    private static final String SEVERITY_ATTR = "severity";

    /**
     * String value for "message" attribute.
     */
    private static final String MESSAGE_ATTR = "message";

    /**
     * String value for "source" attribute.
     */
    private static final String SOURCE_ATTR = "source";

    /**
     * Severity attribute value "warning".
     */
    private static final String SEVERITY_WARNING = "warning";

    /**
     * Severity attribute value "error".
     */
    private static final String SEVERITY_ERROR = "error";

    /**
     * Utility ctor.
     */
    private StaxParserProcessor() {

    }

    /**
     * Parses input XML files: creates 2 parsers
     * which process their XML files in rotation and try
     * to write their results to the ParsedContent class
     * inner map, where they are eagerly compared.
     * @param content
     *        container for parsed data.
     * @param xml1
     *        path to first XML file.
     * @param xml2
     *        path to second XML file.
     * @param portionSize
     *        single portion of XML file processed at once by any parser.
     * @param holder
     *        StatisticsHolder instance.
     * @throws FileNotFoundException
     *         thrown if files not found.
     * @throws XMLStreamException
     *         thrown on internal parser error.
     */
    public static void parse(ParsedContent content, Path xml1,
            Path xml2, int portionSize, StatisticsHolder holder)
                    throws FileNotFoundException, XMLStreamException {
        final XMLEventReader reader1 = prepareParsing(xml1);
        final XMLEventReader reader2 = prepareParsing(xml2);
        while (reader1.hasNext() || reader2.hasNext()) {
            parseXmlPortion(content, reader1, portionSize, true, holder);
            parseXmlPortion(content, reader2, portionSize, false, holder);
        }
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
    private static XMLEventReader prepareParsing(Path xmlFilename)
            throws FileNotFoundException, XMLStreamException {
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        // Setup a new eventReader
        final InputStream inputStream =
            new FileInputStream(xmlFilename.toFile());
        return inputFactory.createXMLEventReader(inputStream);
    }

    /**
     * Parses portion of the XML report.
     *
     * @param content
     *        container for parsed data.
     * @param reader
     *        pStAX parser interface.
     * @param numOfFilenames
     *        number of "file" tags to parse.
     * @param first
     *        flag of parsing the first report.
     * @param holder
     *        StatisticsHolder instance.
     * @throws XMLStreamException
     *         thrown on internal parser error.
     */
    private static void parseXmlPortion(ParsedContent content,
            XMLEventReader reader, int numOfFilenames, boolean first,
            StatisticsHolder holder)
                    throws XMLStreamException {
        int counter = numOfFilenames;
        String filename = null;
        List<CheckstyleRecord> records = null;
        while (reader.hasNext()) {
            final XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                final StartElement startElement = event.asStartElement();
                final String startElementName = startElement.getName()
                        .getLocalPart();
                //file tag encounter
                if (startElementName.equals(FILE_TAG)) {
                    counter--;
                    registerFile(holder, first);
                    final Iterator<Attribute> attributes = startElement
                            .getAttributes();
                    while (attributes.hasNext()) {
                        final Attribute attribute = attributes.next();
                        if (attribute.getName().toString()
                                .equals(FILENAME_ATTR)) {
                            filename = attribute.getValue();
                        }
                    }
                    records = new ArrayList<>();
                }
                //error tag encounter
                else if (startElementName.equals(ERROR_TAG)) {
                    records.add(parseErrorTag(startElement, holder, first));
                }
            }
            if (event.isEndElement()) {
                final EndElement endElement = event.asEndElement();
                if (endElement.getName().getLocalPart().equals(FILE_TAG)) {
                    content.addRecords(filename, records);
                    if (counter == 0) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Registers new file in statistics.
     *
     * @param holder
     *        a StatisticsHolder instance.
     * @param first
     *        flag of parsing the first report.
     */
    private static void registerFile(StatisticsHolder holder, boolean first) {
        if (first) {
            holder.registerSingleFile1();
        }
        else {
            holder.registerSingleFile2();
        }
    }

    /**
     * Parses "error" XML tag.
     *
     * @param startElement
     *        cursor of StAX parser pointed on the tag.
     * @param holder
     *        StatisticsHolder instance.
     * @param first
     *        flag of parsing the first report.
     * @return parsed data as CheckstyleRecord instance.
     */
    private static CheckstyleRecord parseErrorTag(StartElement startElement,
            StatisticsHolder holder, boolean first) {
        int line = -1;
        int column = -1;
        Severity severity = Severity.INFORMATIONAL;
        String source = null;
        String message = null;

        final Iterator<Attribute> attributes = startElement
                .getAttributes();
        while (attributes.hasNext()) {
            final Attribute attribute = attributes.next();
            final String attrName = attribute.getName().toString();
            if (attrName.equals(LINE_ATTR)) {
                line = Integer.parseInt(attribute.getValue());
            }
            else if (attrName.equals(COLUMN_ATTR)) {
                column = Integer.parseInt(attribute.getValue());
            }
            else if (attrName.equals(SEVERITY_ATTR)) {
                final String attrValue = attribute.getValue();
                if (attrValue.equals(SEVERITY_ERROR)) {
                    severity = Severity.ERROR;
                }
                else if (attrValue.equals(SEVERITY_WARNING)) {
                    severity = Severity.WARNING;
                }
                incrementStatistics(severity, holder, first);
            }
            else if (attrName.equals(MESSAGE_ATTR)) {
                message = attribute.getValue();
            }
            else if (attrName.equals(SOURCE_ATTR)) {
                source = attribute.getValue();
            }
        }
        return new CheckstyleRecord(first,
                line, column, severity, source, message);

    }

    /**
     * Adds statistics from single "error" tag to the StatisticsHolder POJO.
     *
     * @param severity
     *        severity level of "error" tag.
     * @param holder
     *        StatisticsHolder instance.
     * @param first
     *        flag of parsing the first report.
     */
    private static void incrementStatistics(Severity severity,
            StatisticsHolder holder, boolean first) {
        switch (severity) {
            case ERROR:
                if (first) {
                    holder.registerSingleError1();
                }
                else {
                    holder.registerSingleError2();
                }
                break;
            case WARNING:
                if (first) {
                    holder.registerSingleWarning1();
                }
                else {
                    holder.registerSingleWarning2();
                }
                break;
            default:
                if (first) {
                    holder.registerSingleInfo1();
                }
                else {
                    holder.registerSingleInfo2();
                }
                break;
        }
    }
}
