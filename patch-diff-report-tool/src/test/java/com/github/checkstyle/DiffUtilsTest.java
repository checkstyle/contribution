///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
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

package com.github.checkstyle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.checkstyle.data.DiffUtils;
import com.github.checkstyle.internal.AbstractTest;

public class DiffUtilsTest extends AbstractTest {

    @Test
    public void testConstructor() throws Exception {
        assertUtilsClassHasPrivateConstructor(DiffUtils.class);
    }

    @Test
    public void testEmptyLists() {
        final List<Integer> list1 = Collections.emptyList();
        final List<Integer> list2 = Collections.emptyList();
        final List<Integer> actual = DiffUtils.produceDiff(list1, list2);
        Assert.assertEquals("Expected empty list", Collections.emptyList(), actual);
    }

    @Test
    public void testMatch() {
        final List<Integer> list1 = Arrays.asList(1, 2, 3);
        final List<Integer> list2 = Arrays.asList(1, 2, 3);
        final List<Integer> actual = DiffUtils.produceDiff(list1, list2);
        Assert.assertEquals("Expected empty list", Collections.emptyList(), actual);
    }

    @Test
    public void testOddEven() {
        final List<Integer> list1 = Arrays.asList(1, 3, 5);
        final List<Integer> list2 = Arrays.asList(2, 4, 6);
        final List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5, 6);
        final List<Integer> actual = DiffUtils.produceDiff(list1, list2);
        Assert.assertEquals("Expected [1, 2, 3, 4, 5, 6]", expected, actual);
    }

    @Test
    public void testEmptyLeft() {
        final List<Integer> list1 = Collections.emptyList();
        final List<Integer> list2 = Arrays.asList(1, 2, 3);
        final List<Integer> actual = DiffUtils.produceDiff(list1, list2);
        Assert.assertEquals("Expected list2", list2, actual);
    }

    @Test
    public void testEmptyRight() {
        final List<Integer> list1 = Arrays.asList(1, 2, 3);
        final List<Integer> list2 = Collections.emptyList();
        final List<Integer> actual = DiffUtils.produceDiff(list1, list2);
        Assert.assertEquals("Expected list1", list1, actual);
    }

    @Test
    public void testLeftTail() {
        final List<Integer> list1 = Arrays.asList(1, 2, 3, 4);
        final List<Integer> list2 = Arrays.asList(1, 2);
        final List<Integer> expected = Arrays.asList(3, 4);
        final List<Integer> actual = DiffUtils.produceDiff(list1, list2);
        Assert.assertEquals("Expected [3, 4]", expected, actual);
    }

    @Test
    public void testRightTail() {
        final List<Integer> list1 = Arrays.asList(1, 2);
        final List<Integer> list2 = Arrays.asList(1, 2, 4, 5);
        final List<Integer> expected = Arrays.asList(4, 5);
        final List<Integer> actual = DiffUtils.produceDiff(list1, list2);
        Assert.assertEquals("Expected [4, 5]", expected, actual);
    }

    @Test
    public void testLeftLower() {
        final List<Integer> list1 = Arrays.asList(1, 2);
        final List<Integer> list2 = Arrays.asList(4, 5);
        final List<Integer> expected = Arrays.asList(1, 2, 4, 5);
        final List<Integer> actual = DiffUtils.produceDiff(list1, list2);
        Assert.assertEquals("Expected [1, 2, 4, 5]", expected, actual);
    }

    @Test
    public void testRightLower() {
        final List<Integer> list1 = Arrays.asList(4, 5);
        final List<Integer> list2 = Arrays.asList(1, 2);
        final List<Integer> expected = Arrays.asList(1, 2, 4, 5);
        final List<Integer> actual = DiffUtils.produceDiff(list1, list2);
        Assert.assertEquals("Expected [1, 2, 4, 5]", expected, actual);
    }

    @Test
    public void testLeftHeadTail() {
        final List<Integer> list1 = Arrays.asList(1, 2, 3, 5, 6, 7);
        final List<Integer> list2 = Arrays.asList(3, 4, 5);
        final List<Integer> expected = Arrays.asList(1, 2, 4, 6, 7);
        final List<Integer> actual = DiffUtils.produceDiff(list1, list2);
        Assert.assertEquals("Expected [1, 2, 4, 6, 7]", expected, actual);
    }

    @Test
    public void testRightHeadTail() {
        final List<Integer> list1 = Arrays.asList(3, 4, 5);
        final List<Integer> list2 = Arrays.asList(1, 2, 3, 5, 6, 7);
        final List<Integer> expected = Arrays.asList(1, 2, 4, 6, 7);
        final List<Integer> actual = DiffUtils.produceDiff(list1, list2);
        Assert.assertEquals("Expected [1, 2, 4, 6, 7]", expected, actual);
    }

}
