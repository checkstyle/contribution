///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2025 the original author or authors.
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

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.github.checkstyle.data.CheckstyleRecord;
import com.github.checkstyle.data.DiffReport;
import com.github.checkstyle.data.Statistics;

/**
 * Contains logics of the StaX parser for the checkstyle xml reports.
 * If its scheme is changed, this class should be the first one to fix.
 *
 * @author attatrol
 */
public final class CheckstyleReportsParser {

    /**
     * Internal index of the base report file.
     */
    public static final int BASE_REPORT_INDEX = 1;

    /**
     * Internal index of the patch report file.
     */
    public static final int PATCH_REPORT_INDEX = 2;

    /**
     * Internal index of the generated difference.
     */
    public static final int DIFF_REPORT_INDEX = 0;

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
     * Private ctor, see parse method.
     */
    private CheckstyleReportsParser() {

    }

    /**
     * Parses input XML files: creates 2 parsers
     * which process their XML files in rotation and try
     * to write their results to the ParsedContent class
     * inner map, where they are eagerly compared.
     *
     * @param baseXml
     *        path to base XML file.
     * @param patchXml
     *        path to patch XML file.
     * @param portionSize
     *        single portion of XML file processed at once by any parser.
     * @return parsed content.
     * @throws FileNotFoundException
     *         if files not found.
     * @throws XMLStreamException
     *         on internal parser error.
     */
    public static DiffReport parse(Path baseXml, Path patchXml, int portionSize)
                    throws FileNotFoundException, XMLStreamException {
        final DiffReport content = new DiffReport();
        final XMLEventReader baseReader = StaxUtils.createReader(baseXml);
        final XMLEventReader patchReader = StaxUtils.createReader(patchXml);
        while (baseReader.hasNext() || patchReader.hasNext()) {
            parseXmlPortion(content, baseReader, portionSize, BASE_REPORT_INDEX);
            parseXmlPortion(content, patchReader, portionSize, PATCH_REPORT_INDEX);
        }
        content.getDiffStatistics();
        return content;
    }

    /**
     * Parses portion of the XML report.
     *
     * @param diffReport
     *        container for parsed data.
     * @param reader
     *        StAX parser interface.
     * @param numOfFilenames
     *        number of "file" tags to parse.
     * @param index
     *        internal index of the parsed file.
     * @throws XMLStreamException
     *         on internal parser error.
     */
    private static void parseXmlPortion(DiffReport diffReport,
            XMLEventReader reader, int numOfFilenames, int index)
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
                // file tag encounter
                if (startElementName.equals(FILE_TAG)) {
                    counter--;
                    diffReport.getStatistics().incrementFileCount(index);
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
                // error tag encounter
                else if (startElementName.equals(ERROR_TAG)) {
                    records.add(parseErrorTag(startElement, diffReport.getStatistics(), index,
                            filename));
                }
            }
            if (event.isEndElement()) {
                final EndElement endElement = event.asEndElement();
                if (endElement.getName().getLocalPart().equals(FILE_TAG)) {
                    diffReport.addRecords(records, filename);
                    if (counter == 0) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Parses "error" XML tag.
     *
     * @param startElement
     *        cursor of StAX parser pointed on the tag.
     * @param statistics
     *        container accumulating statistics.
     * @param index
     *        internal index of the parsed file.
     * @param filename
     *        file name.
     * @return parsed data as CheckstyleRecord instance.
     */
    private static CheckstyleRecord parseErrorTag(StartElement startElement,
            Statistics statistics, int index, String filename) {
        int line = -1;
        int column = -1;
        String source = null;
        String message = null;
        String severity = null;
        final Iterator<Attribute> attributes = startElement
                .getAttributes();
        while (attributes.hasNext()) {
            final Attribute attribute = attributes.next();
            final String attrName = attribute.getName().toString();
            switch (attrName) {
                case LINE_ATTR:
                    line = Integer.parseInt(attribute.getValue());
                    break;
                case COLUMN_ATTR:
                    column = Integer.parseInt(attribute.getValue());
                    break;
                case SEVERITY_ATTR:
                    severity = attribute.getValue();
                    statistics.addSeverityRecord(severity, index);
                    break;
                case MESSAGE_ATTR:
                    message = attribute.getValue();
                    break;
                case SOURCE_ATTR:
                    source = attribute.getValue();
                    statistics.addModuleRecord(source, index);
                    break;
                default:
                    break;
            }
        }
        return new CheckstyleRecord(index,
                line, column, severity, source, message, filename);

    }

}
