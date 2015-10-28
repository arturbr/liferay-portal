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

package com.liferay.portal.search.elasticsearch.internal.cluster;

import com.liferay.portal.model.Company;
import com.liferay.portal.service.CompanyLocalService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Artur Aquino
 */
public class ElasticsearchClusterTest {

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		_replicasClusterContext = createReplicasClusterContext();
	}

	@Test
	public void testGetTargetIndexNames() {
		long[] companyIds = new long[] {42, 142857};

		setUpCompanyLocalService(getCompanies(companyIds));

		String[] targetIndexNames =
			_replicasClusterContext.getTargetIndexNames();

		Arrays.sort(targetIndexNames);

		Assert.assertEquals(
			"[0, 142857, 42]", Arrays.toString(targetIndexNames));
	}

	private ReplicasClusterContext createReplicasClusterContext() {
		ElasticsearchCluster elasticsearchCluster = new ElasticsearchCluster();

		elasticsearchCluster.setCompanyLocalService(_companyLocalService);

		return elasticsearchCluster.new ReplicasClusterContextImpl();
	}

	private List<Company> getCompanies(long[] companyIds) {
		int length = companyIds.length;

		List<Company> companies = new ArrayList<>(length);

		for (int i = 0; i < length; i++) {
			companies.add(getCompany(companyIds[i]));
		}

		return companies;
	}

	private Company getCompany(long companyId) {
		Company company = Mockito.mock(Company.class);

		Mockito.when(
			company.getCompanyId()
		).thenReturn(
			companyId
		);

		return company;
	}

	private void setUpCompanyLocalService(List<Company> companies) {
		Mockito.when(
			_companyLocalService.getCompanies()
		).thenReturn(
			companies
		);
	}

	@Mock
	private CompanyLocalService _companyLocalService;

	private ReplicasClusterContext _replicasClusterContext;

}