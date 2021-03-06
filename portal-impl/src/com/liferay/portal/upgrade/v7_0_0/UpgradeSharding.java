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

package com.liferay.portal.upgrade.v7_0_0;

import com.liferay.portal.dao.jdbc.spring.DataSourceFactoryBean;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.upgrade.util.UpgradeTable;
import com.liferay.portal.kernel.upgrade.util.UpgradeTableFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LoggingTimer;
import com.liferay.portal.upgrade.v7_0_0.util.ClassNameTable;
import com.liferay.portal.upgrade.v7_0_0.util.ClusterGroupTable;
import com.liferay.portal.upgrade.v7_0_0.util.CounterTable;
import com.liferay.portal.upgrade.v7_0_0.util.CountryTable;
import com.liferay.portal.upgrade.v7_0_0.util.PortalPreferencesTable;
import com.liferay.portal.upgrade.v7_0_0.util.RegionTable;
import com.liferay.portal.upgrade.v7_0_0.util.ReleaseTable;
import com.liferay.portal.upgrade.v7_0_0.util.ResourceActionTable;
import com.liferay.portal.upgrade.v7_0_0.util.ServiceComponentTable;
import com.liferay.portal.upgrade.v7_0_0.util.VirtualHostTable;
import com.liferay.portal.util.PropsUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

/**
 * @author Manuel de la Peña
 */
public class UpgradeSharding extends UpgradeProcess {

	protected void copyControlTable(
			Connection sourceConnection, Connection targetConnection,
			String tableName, Object[][] columns, String createSQL)
		throws Exception {

		UpgradeTable upgradeTable = UpgradeTableFactoryUtil.getUpgradeTable(
			tableName, columns);

		upgradeTable.setCreateSQL(createSQL);

		upgradeTable.copyTable(sourceConnection, targetConnection);
	}

	protected void copyControlTables(List<String> shardNames) throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer()) {
			List<String> uniqueShardNames = ListUtil.unique(shardNames);

			if (uniqueShardNames.size() == 1) {
				if (_log.isInfoEnabled()) {
					_log.info(
						"Skip copying of control tables because all " +
							"companies are located in the same shard");
				}

				return;
			}

			String defaultShardName = GetterUtil.getString(
				PropsUtil.get("shard.default.name"), "default");

			for (String uniqueShardName : uniqueShardNames) {
				if (!uniqueShardName.equals(defaultShardName)) {
					copyControlTables(uniqueShardName);
				}
			}
		}
	}

	protected void copyControlTables(String shardName) throws Exception {
		DataSourceFactoryBean dataSourceFactoryBean =
			new DataSourceFactoryBean();

		dataSourceFactoryBean.setPropertyPrefix("jdbc." + shardName + ".");

		DataSource dataSource = dataSourceFactoryBean.createInstance();

		try (Connection targetConnection = dataSource.getConnection()) {
			copyControlTable(
				connection, targetConnection, ClassNameTable.TABLE_NAME,
				ClassNameTable.TABLE_COLUMNS, ClassNameTable.TABLE_SQL_CREATE);
			copyControlTable(
				connection, targetConnection, ClusterGroupTable.TABLE_NAME,
				ClusterGroupTable.TABLE_COLUMNS,
				ClusterGroupTable.TABLE_SQL_CREATE);
			copyControlTable(
				connection, targetConnection, CounterTable.TABLE_NAME,
				CounterTable.TABLE_COLUMNS, CounterTable.TABLE_SQL_CREATE);
			copyControlTable(
				connection, targetConnection, CountryTable.TABLE_NAME,
				CountryTable.TABLE_COLUMNS, CountryTable.TABLE_SQL_CREATE);
			copyControlTable(
				connection, targetConnection, PortalPreferencesTable.TABLE_NAME,
				PortalPreferencesTable.TABLE_COLUMNS,
				PortalPreferencesTable.TABLE_SQL_CREATE);
			copyControlTable(
				connection, targetConnection, RegionTable.TABLE_NAME,
				RegionTable.TABLE_COLUMNS, RegionTable.TABLE_SQL_CREATE);
			copyControlTable(
				connection, targetConnection, ReleaseTable.TABLE_NAME,
				ReleaseTable.TABLE_COLUMNS, ReleaseTable.TABLE_SQL_CREATE);
			copyControlTable(
				connection, targetConnection, ResourceActionTable.TABLE_NAME,
				ResourceActionTable.TABLE_COLUMNS,
				ResourceActionTable.TABLE_SQL_CREATE);
			copyControlTable(
				connection, targetConnection, ServiceComponentTable.TABLE_NAME,
				ServiceComponentTable.TABLE_COLUMNS,
				ServiceComponentTable.TABLE_SQL_CREATE);
			copyControlTable(
				connection, targetConnection, VirtualHostTable.TABLE_NAME,
				VirtualHostTable.TABLE_COLUMNS,
				VirtualHostTable.TABLE_SQL_CREATE);
		}
		catch (Exception e) {
			_log.error("Unable to copy control tables", e);
		}
	}

	@Override
	protected void doUpgrade() throws Exception {
		List<String> shardNames = getShardNames();

		if (shardNames.size() <= 1) {
			return;
		}

		copyControlTables(shardNames);
	}

	protected List<String> getShardNames() throws Exception {
		try (LoggingTimer loggingTimer = new LoggingTimer();
			PreparedStatement ps = connection.prepareStatement(
				"select name from Shard");
			ResultSet rs = ps.executeQuery()) {

			List<String> shardNames = new ArrayList<>();

			while (rs.next()) {
				shardNames.add(rs.getString("name"));
			}

			return shardNames;
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		UpgradeSharding.class);

}