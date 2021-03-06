/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.lcs.rest;

import com.liferay.lcs.util.LCSUtil;

import java.util.List;

/**
 * @author Ivica Cardic
 * @author Igor Beslic
 */
public class LCSClusterEntryServiceUtil {

	public static LCSClusterEntry addLCSClusterEntry(
		long lcsProjectId, String name, String description, String location,
		int type) {

		return _lcsClusterEntryService.addLCSClusterEntry(
			lcsProjectId, name, description, location, null, type);
	}

	public static LCSClusterEntry getLCSClusterEntry(long lcsClusterEntryId) {
		return _lcsClusterEntryService.getLCSClusterEntry(lcsClusterEntryId);
	}

	public static List<LCSClusterEntry>
		getLCSProjectManageableLCSClusterEntries(long lcsProjectId) {

		return _lcsClusterEntryService.getLCSProjectManageableLCSClusterEntries(
			lcsProjectId, LCSUtil.getLocalLCSClusterEntryType());
	}

	public void setLCSClusterEntryService(
		LCSClusterEntryService lcsClusterEntryService) {

		_lcsClusterEntryService = lcsClusterEntryService;
	}

	private static LCSClusterEntryService _lcsClusterEntryService;

}