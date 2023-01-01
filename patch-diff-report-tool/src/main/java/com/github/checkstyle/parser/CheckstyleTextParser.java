///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2023 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.github.checkstyle.data.CheckstyleRecord;
import com.github.checkstyle.data.DiffReport;
import com.github.checkstyle.data.Statistics;
import com.github.checkstyle.parser.JgitUtils.JgitDifference;

/**
 * Contains logics of the parser for the raw files.
 *
 * @author Richard Veach
 */
public final class CheckstyleTextParser {
    /**
     * Internal index of the base report file.
     */
    public static final int BASE_REPORT_INDEX = 1;

    /**
     * Internal index of the patch report file.
     */
    public static final int PATCH_REPORT_INDEX = 2;

    /**
     * Default severity of any differences.
     */
    public static final String DEFAULT_SEVERITY = "difference";

    /**
     * Default source of any differences.
     */
    public static final String DEFAULT_SOURCE = "patch-diff-report-tool";

    /**
     * Private ctor, see parse method.
     */
    private CheckstyleTextParser() {
    }

    /**
     * Parses input files: creates 2 parsers which process their files in rotation and try to
     * compare and write their results to the {@link DiffReport} class.
     *
     * @param baseReport
     *            path to base directory.
     * @param patchReport
     *            path to patch directory.
     * @return parsed content.
     * @throws IOException
     *             if there is a problem accessing a file.
     */
    public static DiffReport parse(Path baseReport, Path patchReport) throws IOException {
        final DiffReport content = new DiffReport();
        final StringListIterator baseReader = getFiles(baseReport);
        final StringListIterator patchReader = getFiles(patchReport);
        while (true) {
            final boolean baseNext = baseReader.hasNext();
            final boolean patchNext = patchReader.hasNext();

            if (baseNext && patchNext) {
                parseDifference(content, baseReader, baseReport, patchReader, patchReport);
            }
            else if (baseNext != patchNext) {
                if (baseNext) {
                    parseDifferenceSingle(content, baseReader, baseReport, BASE_REPORT_INDEX);
                }
                else {
                    parseDifferenceSingle(content, patchReader, patchReport, PATCH_REPORT_INDEX);
                }
            }
            else {
                break;
            }
        }
        content.getDiffStatistics();
        return content;
    }

    /**
     * Compares the next file in {@code baseReader} and {@code patchReader} and the contents of
     * those files. File contents are only compared if the files have the same name.
     *
     * @param diffReport
     *            container for parsed data.
     * @param baseReader
     *            reader for base file list.
     * @param baseReport
     *            path for base files.
     * @param patchReader
     *            reader for patch file list.
     * @param patchReport
     *            path for patch files.
     * @throws IOException
     *             if there is a problem accessing a file.
     */
    private static void parseDifference(DiffReport diffReport, StringListIterator baseReader,
            Path baseReport, StringListIterator patchReader, Path patchReport) throws IOException {
        final int order = baseReader.peek().compareTo(patchReader.peek());

        if (order == 0) {
            parseDifferenceFile(diffReport, baseReader.next(), baseReport, patchReport);

            patchReader.next();
        }
        else if (order < 0) {
            parseDifferenceSingle(diffReport, baseReader, baseReport, BASE_REPORT_INDEX);
        }
        else {
            parseDifferenceSingle(diffReport, patchReader, patchReport, PATCH_REPORT_INDEX);
        }
    }

    /**
     * Compares the contents of the files located at {@code fielPath} at {@code baseReport} and
     * {@code patchReport}.
     *
     * @param diffReport
     *            container for parsed data.
     * @param filePath
     *            path for files.
     * @param baseReport
     *            path for base files.
     * @param patchReport
     *            path for patch files.
     * @throws IOException
     *             if there is a problem accessing a file.
     */
    private static void parseDifferenceFile(DiffReport diffReport, String filePath,
            Path baseReport, Path patchReport) throws IOException {
        final File baseFile = new File(baseReport.toFile(), filePath);
        final File patchFile = new File(patchReport.toFile(), filePath);

        final Statistics statistics = diffReport.getStatistics();
        final Iterator<JgitDifference> iterator = JgitUtils.getDifferences(baseFile, patchFile);
        final List<CheckstyleRecord> records = new ArrayList<>();

        while (iterator.hasNext()) {
            final JgitDifference diff = iterator.next();

            final String xref;

            if (diff.getIndex() == BASE_REPORT_INDEX) {
                xref = baseFile.getPath();
            }
            else {
                xref = patchFile.getPath();
            }

            final CheckstyleRecord checkstyleRecord = new CheckstyleRecord(diff.getIndex(),
                    diff.getLineNo() + 1, 1, DEFAULT_SEVERITY, DEFAULT_SOURCE, diff.getLine(),
                    xref);

            statistics.addSeverityRecord(DEFAULT_SEVERITY, diff.getIndex());
            statistics.addModuleRecord(DEFAULT_SOURCE, diff.getIndex());

            records.add(checkstyleRecord);
        }

        diffReport.addRecords(records, filePath);

        statistics.incrementFileCount(BASE_REPORT_INDEX);
        statistics.incrementFileCount(PATCH_REPORT_INDEX);
    }

    /**
     * Adds the next file in the {@code reader} as a difference as there is no matching file in the
     * other side.
     *
     * @param diffReport
     *            container for parsed data.
     * @param reader
     *            reader for file list.
     * @param path
     *            path for files.
     * @param index
     *            internal index of the parsed file.
     */
    private static void parseDifferenceSingle(DiffReport diffReport, StringListIterator reader,
            Path path, int index) {
        final int otherIndex;

        if (index == BASE_REPORT_INDEX) {
            otherIndex = PATCH_REPORT_INDEX;
        }
        else {
            otherIndex = BASE_REPORT_INDEX;
        }

        final String filePath = reader.next();
        final String xref = new File(path.toFile(), filePath).getPath();

        final Statistics statistics = diffReport.getStatistics();
        final List<CheckstyleRecord> records = new ArrayList<>();

        final CheckstyleRecord checkstyleRecord =
            new CheckstyleRecord(otherIndex, 1, 1, DEFAULT_SEVERITY,
                DEFAULT_SOURCE, "File not found.", xref);

        statistics.addSeverityRecord(DEFAULT_SEVERITY, otherIndex);
        statistics.addModuleRecord(DEFAULT_SOURCE, otherIndex);

        records.add(checkstyleRecord);

        diffReport.addRecords(records, filePath);

        statistics.incrementFileCount(index);
    }

    /**
     * Retrieves the list of files in the {@code base}.
     *
     * @param base
     *            The base directory to scan.
     * @return The iterator with the list of files found.
     * @throws IOException
     *             if a canonical path can't be retrieved.
     */
    private static StringListIterator getFiles(Path base) throws IOException {
        final List<String> result = new ArrayList<>();
        final File baseFile = base.toFile();

        getFiles(baseFile, baseFile.getCanonicalPath().length() + 1, result);

        return new StringListIterator(result.iterator());
    }

    /**
     * Retrieves the list of files in the {@code base}.
     *
     * @param directory
     *            The directory to scan.
     * @param baseLength
     *            The amount to cutoff from the file names.
     * @param result
     *            The list of files found.
     * @throws IOException
     *             if a canonical path can't be retrieved.
     */
    private static void getFiles(File directory, int baseLength, List<String> result)
            throws IOException {
        final File[] files = directory.listFiles();

        Arrays.sort(files);

        for (File file : files) {
            if (file.isDirectory()) {
                getFiles(file, baseLength, result);
            }
            else {
                result.add(file.getCanonicalPath().substring(baseLength));
            }
        }
    }

    /**
     * A custom iterator for a list of strings with peek functionality.
     *
     * @author Richard Veach
     */
    private static final class StringListIterator implements Iterator<String> {
        /** The wrapped Iterator. */
        private final Iterator<String> iterator;
        /** The next item in the iterator, or {@code null} if to use {@link #iterator}. */
        private String next;

        /**
         * Default constructor.
         *
         * @param iterator
         *            The iterator to get the results from.
         */
        private StringListIterator(Iterator<String> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            final boolean result;

            if (next != null) {
                result = true;
            }
            else {
                result = iterator.hasNext();
            }

            return result;
        }

        @Override
        public String next() {
            final String result;

            if (next != null) {
                result = next;
                next = null;
            }
            else {
                result = iterator.next();
            }

            return result;
        }

        /**
         * Check the next element without reading it from the iterator. Returns {@code null} if the
         * iterator is at the end or has no more elements. A call to this method will be equal to
         * the next return of {@link #next()}.
         *
         * @return The next item.
         */
        public String peek() {
            final String result;

            if (next != null) {
                result = next;
            }
            else if (iterator.hasNext()) {
                next = iterator.next();
                result = next;
            }
            else {
                result = null;
            }

            return result;
        }
    }
}
