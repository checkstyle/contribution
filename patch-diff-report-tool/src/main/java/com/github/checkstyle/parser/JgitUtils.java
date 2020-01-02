////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2020 the original author or authors.
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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;

/**
 * Utility class for JGit routines.
 *
 * @author Richard Veach
 */
public final class JgitUtils {
    /** The cached difference algorithm to use for comparisons. */
    private static DiffAlgorithm diffAlgorithm;

    /** Private ctor. */
    private JgitUtils() {
    }

    /**
     * Generates the differences between the contents of the 2 files.
     *
     * @param baseFile
     *            The base file to examine.
     * @param patchFile
     *            The patch file to examine.
     * @return The iterator containing the differences.
     * @throws IOException
     *             if Exceptions occur while reading the file.
     */
    public static Iterator<JgitDifference> getDifferences(File baseFile, File patchFile)
            throws IOException {
        if (diffAlgorithm == null) {
            diffAlgorithm = DiffAlgorithm.getAlgorithm(SupportedAlgorithm.HISTOGRAM);
        }

        final RawText baseFileRaw = new RawText(baseFile);
        final RawText patchFileRaw = new RawText(patchFile);

        return new JgitDifferenceIterator(diffAlgorithm.diff(RawTextComparator.DEFAULT,
                baseFileRaw, patchFileRaw), baseFileRaw, patchFileRaw);
    }

    /**
     * Custom iterator to loop through all the differences found by JGit.
     *
     * @author Richard Veach
     */
    private static final class JgitDifferenceIterator implements Iterator<JgitDifference> {
        /** Initial position used to denote to do no more processing. */
        private static final int INITIAL_END_LINE = -2;

        /** The list of base and patch line numbers with differences. */
        private final EditList edits;
        /** The raw base file. */
        private final RawText baseFileRaw;
        /** The raw patch file. */
        private final RawText patchFileRaw;

        /** The current difference from {@link #edits}. */
        private Edit currentEdit;
        /** The next item in the iterator. */
        private JgitDifference next;

        /** The current index of {@link #edits}. */
        private int index = -1;
        /** The current line number for the base file of the {@link #currentEdit}. */
        private int baseLineNo = index;
        /** The max line number for the base file of the {@link #currentEdit}. */
        private int baseEndLineNo = INITIAL_END_LINE;
        /** The current line number for the patch file of the {@link #currentEdit}. */
        private int patchLineNo = baseLineNo;
        /** The max line number for the patch file of the {@link #currentEdit}. */
        private int patchEndLineNo = baseEndLineNo;

        /**
         * Default constructor.
         *
         * @param edits
         *            The list of base and patch line numbers with differences.
         * @param baseFileRaw
         *            The raw base file.
         * @param patchFileRaw
         *            The raw patch file.
         */
        private JgitDifferenceIterator(EditList edits, RawText baseFileRaw, RawText patchFileRaw) {
            this.edits = edits;
            this.baseFileRaw = baseFileRaw;
            this.patchFileRaw = patchFileRaw;
        }

        @Override
        public boolean hasNext() {
            populateNext();

            return next != null;
        }

        @Override
        public JgitDifference next() {
            populateNext();

            final JgitDifference result = next;

            next = null;

            return result;
        }

        /** Populates the value for {@link #next} to be returned. */
        private void populateNext() {
            if (next == null) {
                nextEdit();

                if (currentEdit != null) {
                    if (baseLineNo < currentEdit.getEndA()) {
                        next = new JgitDifference(CheckstyleTextParser.BASE_REPORT_INDEX,
                                baseLineNo, baseFileRaw.getString(baseLineNo));

                        baseLineNo++;
                    }
                    else if (patchLineNo < currentEdit.getEndB()) {
                        next = new JgitDifference(CheckstyleTextParser.PATCH_REPORT_INDEX,
                                patchLineNo, patchFileRaw.getString(patchLineNo));

                        patchLineNo++;
                    }
                }
            }
        }

        /** Sets the next edit with differences to be used. */
        private void nextEdit() {
            if (baseLineNo >= baseEndLineNo && patchLineNo >= patchEndLineNo) {
                index++;

                if (index >= edits.size()) {
                    currentEdit = null;
                }
                else {
                    currentEdit = edits.get(index);

                    baseLineNo = Math.max(0, currentEdit.getBeginA());
                    patchLineNo = Math.max(0, currentEdit.getBeginB());
                    baseEndLineNo = Math.min(baseFileRaw.size(), currentEdit.getEndA());
                    patchEndLineNo = Math.min(patchFileRaw.size(), currentEdit.getEndB());
                }
            }
        }
    }

    /**
     * Class that holds the information of JGit's differences.
     *
     * @author Richard Veach
     */
    public static final class JgitDifference {
        /**
         * Index of the source.
         */
        private final int index;
        /**
         * Line number of the difference.
         */
        private final int lineNo;
        /**
         * Contents of the difference.
         */
        private final String line;

        /**
         * Default constructor.
         *
         * @param index
         *            internal index of the source.
         * @param lineNo
         *            line number.
         * @param line
         *            contents of the line.
         */
        private JgitDifference(int index, int lineNo, String line) {
            this.index = index;
            this.lineNo = lineNo;

            // truncate line for windows
            String temp = line;
            if (temp.endsWith("\r")) {
                temp = temp.substring(0, temp.length() - 1);
            }

            this.line = temp;
        }

        public int getIndex() {
            return index;
        }

        public int getLineNo() {
            return lineNo;
        }

        public String getLine() {
            return line;
        }
    }
}
