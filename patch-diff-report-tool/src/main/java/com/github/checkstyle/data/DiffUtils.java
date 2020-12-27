////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2021 the original author or authors.
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

package com.github.checkstyle.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to calculate difference between 2 sorted lists.
 */
public final class DiffUtils {

    /** Private ctor. */
    private DiffUtils() {
    }

    /**
     * Creates difference between 2 sorted lists.
     *
     * @param firstList
     *        the first list.
     * @param secondList
     *        the second list.
     * @param <T> the type of elements.
     * @return the difference list.
     */
    public static <T extends Comparable<T>> List<T> produceDiff(
            List<T> firstList, List<T> secondList) {
        final List<T> result;
        if (firstList.isEmpty()) {
            result = secondList;
        }
        else if (secondList.isEmpty()) {
            result = firstList;
        }
        else {
            result = produceDiff(firstList.iterator(), secondList.iterator());
        }
        return result;
    }

    /**
     * Creates difference between 2 non-empty iterators.
     *
     * @param firstIterator
     *        the first iterator.
     * @param secondIterator
     *        the second iterator.
     * @param <T> the type of elements.
     * @return the difference list (always sorted).
     */
    private static <T extends Comparable<T>> List<T> produceDiff(
            Iterator<T> firstIterator, Iterator<T> secondIterator) {
        T firstVal = firstIterator.next();
        T secondVal = secondIterator.next();
        final List<T> result = new ArrayList<>();
        while (true) {
            final int diff = firstVal.compareTo(secondVal);
            if (diff < 0) {
                result.add(firstVal);
                if (!firstIterator.hasNext()) {
                    result.add(secondVal);
                    break;
                }
                firstVal = firstIterator.next();
            }
            else if (diff > 0) {
                result.add(secondVal);
                if (!secondIterator.hasNext()) {
                    result.add(firstVal);
                    break;
                }
                secondVal = secondIterator.next();
            }
            else {
                if (!firstIterator.hasNext() || !secondIterator.hasNext()) {
                    break;
                }
                firstVal = firstIterator.next();
                secondVal = secondIterator.next();
            }
        }
        // add tails
        while (firstIterator.hasNext()) {
            result.add(firstIterator.next());
        }
        while (secondIterator.hasNext()) {
            result.add(secondIterator.next());
        }
        return Collections.unmodifiableList(result);
    }

}
