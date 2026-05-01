package com.spring_midterm.midterm;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spring_midterm.midterm.dto.GridRequest;
import org.junit.jupiter.api.Test;

class GridRequestTest {

	@Test
	void omittedPageSizeMeansUnpagedGridRequest() {
		GridRequest request = new GridRequest(0, null, null, null);

		assertNull(request.pageSize());
		assertTrue(request.pageIndex() == 0);
	}
}
