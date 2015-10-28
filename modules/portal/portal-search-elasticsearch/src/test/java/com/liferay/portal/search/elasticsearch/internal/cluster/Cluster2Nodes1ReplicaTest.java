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

import com.liferay.portal.kernel.security.RandomUtil;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.CompanyConstants;
import com.liferay.portal.search.elasticsearch.internal.connection.ElasticsearchFixture;
import com.liferay.portal.service.CompanyLocalService;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * @author Artur Aquino
 */
public class Cluster2Nodes1ReplicaTest {

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		_replicasClusterContext = createReplicasClusterContext();

		_testCluster.setUp();
	}

	@After
	public void tearDown() throws Exception {
		_testCluster.tearDown();
	}

	@Test
	public void test2Nodes2PrimaryShards1Replica() throws Exception {
		ElasticsearchFixture elasticsearchFixture = _testCluster.getNode(0);

		elasticsearchFixture.createIndex(CompanyConstants.SYSTEM_STRING);

		ClusterAssert.assert1PrimaryShardAnd2Nodes(elasticsearchFixture);

		int id = RandomUtil.nextInt(10000);
		elasticsearchFixture.createIndex(Integer.toString(id));

		ClusterAssert.assert2PrimaryShardsAnd2Nodes(elasticsearchFixture);

		setUpCompany(id);

		ReplicasManager replicasManager = new ReplicasManagerImpl(
			elasticsearchFixture.getIndicesAdminClient());
		replicasManager.updateNumberOfReplicas(
			1, _replicasClusterContext.getTargetIndexNames());

		ClusterAssert.assert2PrimaryShards1ReplicaAnd2Nodes(
			elasticsearchFixture);
	}

	private ReplicasClusterContext createReplicasClusterContext() {
		ElasticsearchCluster elasticsearchCluster = new ElasticsearchCluster();

		elasticsearchCluster.setCompanyLocalService(_companyLocalService);

		return elasticsearchCluster.new ReplicasClusterContextImpl();
	}

	private void setUpCompany(long id) {
		Mockito.when(
			_company.getCompanyId()
		).thenReturn(
			id
		);

		Mockito.when(
			_companyLocalService.getCompanies()
		).thenReturn(
			new ArrayList<>(Arrays.asList(_company))
		);
	}

	@Mock
	private Company _company;

	@Mock
	private CompanyLocalService _companyLocalService;

	private ReplicasClusterContext _replicasClusterContext;
	private final TestCluster _testCluster = new TestCluster(2, this);

}