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

package com.liferay.portal.search.elasticsearch.internal.connection;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.search.elasticsearch.connection.ElasticsearchPluginManager;

import java.io.IOException;

import java.net.URL;

import org.elasticsearch.common.cli.Terminal;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.env.Environment;
import org.elasticsearch.plugins.PluginManager;

/**
 * @author Artur Aquino
 */
public class ElasticsearchPluginManagerImpl
	implements ElasticsearchPluginManager {

	public ElasticsearchPluginManagerImpl(Settings settings, URL url) {
		_pluginManager = new PluginManager(
			new Environment(settings), url, PluginManager.OutputMode.SILENT,
			TimeValue.timeValueMinutes(1));
	}

	@Override
	public void removeAndInstallPlugin(Terminal terminal, String pluginName) {
		try {
			_pluginManager.removePlugin(pluginName, terminal);

			_pluginManager.downloadAndExtract(pluginName, terminal, true);
		}
		catch (IOException ioe) {
			if (_log.isInfoEnabled()) {
				_log.info(
					"Unable to remove and install plugin " + pluginName, ioe);
			}
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		ElasticsearchPluginManagerImpl.class);

	private final PluginManager _pluginManager;

}