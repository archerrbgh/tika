/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tika.parser.chm;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.tika.detect.TestContainerAwareDetector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.parser.chm.accessor.ChmDirectoryListingSet;
import org.apache.tika.parser.chm.accessor.ChmItsfHeader;
import org.apache.tika.parser.chm.accessor.ChmItspHeader;
import org.apache.tika.parser.chm.accessor.ChmLzxcControlData;
import org.apache.tika.parser.chm.core.ChmCommons;
import org.apache.tika.parser.chm.core.ChmConstants;
import org.apache.tika.parser.chm.lzx.ChmLzxState;

public class TestChmLzxState extends TestCase {
	private ChmLzxState chmLzxState;
	private int windowSize;

	public void setUp() throws Exception {
		try {
			TikaInputStream stream = TikaInputStream
					.get(TestContainerAwareDetector.class
							.getResource(TestParameters.chmFile));

			byte[] data = TestUtils.toByteArray(stream);

			/* Creates and parses itsf header */
			ChmItsfHeader chmItsHeader = new ChmItsfHeader();
			chmItsHeader.parse(Arrays.copyOfRange(data, 0,
					ChmConstants.CHM_ITSF_V3_LEN - 1), chmItsHeader);
			/* Creates and parses itsp block */
			ChmItspHeader chmItspHeader = new ChmItspHeader();
			chmItspHeader.parse(Arrays.copyOfRange(data,
					(int) chmItsHeader.getDirOffset(),
					(int) chmItsHeader.getDirOffset()
							+ ChmConstants.CHM_ITSP_V1_LEN), chmItspHeader);

			/* Creating instance of ChmDirListingContainer */
			ChmDirectoryListingSet chmDirListCont = new ChmDirectoryListingSet(
					data, chmItsHeader, chmItspHeader);
			int indexOfControlData = ChmCommons.indexOf(
					chmDirListCont.getDirectoryListingEntryList(),
					ChmConstants.CONTROL_DATA);

			int indexOfResetTable = ChmCommons.indexOfResetTableBlock(data,
					ChmConstants.LZXC.getBytes());
			byte[] dir_chunk = null;
			if (indexOfResetTable > 0) {
				dir_chunk = Arrays.copyOfRange(data, indexOfResetTable,
						indexOfResetTable
								+ chmDirListCont.getDirectoryListingEntryList()
										.get(indexOfControlData).getLength());
			}

			ChmLzxcControlData clcd = new ChmLzxcControlData();
			clcd.parse(dir_chunk, clcd);
			windowSize = (int) clcd.getWindowSize();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void testChmLzxStateConstructor() {
		chmLzxState = new ChmLzxState(windowSize);
		Assert.assertNotNull(chmLzxState);
	}

	public void testToString() {
		if (chmLzxState == null)
			testChmLzxStateConstructor();
		Assert.assertTrue(chmLzxState.toString().length() > 20);
	}

	public void tearDown() throws Exception {
	}

}