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

/**
 * Contains logics of the StaX parser for the checkstyle xml reports.
 * If its scheme changed, this class should be the first one to fix.
 *
 * @author atta_troll
 */
public final class StaxParserProcessor {
    /**
     * XML tags values.
     */
    private static final String FILE_TAG = "file";
    private static final String ERROR_TAG = "error";
    private static final String FILENAME_ATTR = "name";
    private static final String LINE_ATTR = "line";
    private static final String COLUMN_ATTR = "column";
    private static final String SEVERITY_ATTR = "severity";
    private static final String MESSAGE_ATTR = "message";
    private static final String SOURCE_ATTR = "source";

    /**
     * Severity attribute values.
     */
    private static final String SEVERITY_WARNING = "warning";
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
        XMLEventReader reader1 = prepareParsing(xml1);
        XMLEventReader reader2 = prepareParsing(xml2);
        while (reader1.hasNext() || reader2.hasNext()) {
            parseXMLPortion(content, reader1, portionSize, true, holder);
            parseXMLPortion(content, reader2, portionSize, false, holder);
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
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        // Setup a new eventReader
        InputStream in = new FileInputStream(xmlFilename.toFile());
        return inputFactory.createXMLEventReader(in);
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
    private static void parseXMLPortion(ParsedContent content,
            XMLEventReader reader, int numOfFilenames, boolean first,
            StatisticsHolder holder)
                    throws XMLStreamException {
        int counter = numOfFilenames;
        String filename = null;
        List<CheckstyleRecord> records = null;
        while (reader.hasNext() && counter > 0) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                final StartElement startElement = event.asStartElement();
                final String startElementName = startElement.getName()
                        .getLocalPart();
                //file tag encounter
                if (startElementName.equals(FILE_TAG)) {
                    counter--;
                    if (first) {
                        holder.incrementFileNum1();
                    }
                    else {
                        holder.incrementFileNum2();
                    }
                    Iterator<Attribute> attributes = startElement
                            .getAttributes();
                    while (attributes.hasNext()) {
                        Attribute attribute = attributes.next();
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
                }
            }
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

        Iterator<Attribute> attributes = startElement
                .getAttributes();
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
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
                holder.incrementErrorNum1();
            }
            else {
                holder.incrementErrorNum2();
            }
            break;
        case WARNING:
            if (first) {
                holder.incrementWarningNum1();
            }
            else {
                holder.incrementWarningNum2();
            }
            break;
        default:
            if (first) {
                holder.incrementInfoNum1();
            }
            else {
                holder.incrementInfoNum2();
            }
            break;
        }
        if (first) {
            holder.incrementTotalNum1();
        }
        else {
            holder.incrementTotalNum2();
        }
    }
}
