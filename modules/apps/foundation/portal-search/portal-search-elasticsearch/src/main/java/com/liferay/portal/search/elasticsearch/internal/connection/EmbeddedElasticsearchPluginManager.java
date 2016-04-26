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

import com.liferay.portal.search.elasticsearch.connection.ElasticsearchPluginManager;

import java.io.File;
import java.io.IOException;

import org.elasticsearch.common.cli.Terminal;
import org.elasticsearch.common.cli.Terminal.Verbosity;
import org.elasticsearch.common.settings.Settings;

/**
 * @author Artur Aquino
 * @author André de Oliveira
 */
public class EmbeddedElasticsearchPluginManager {

	public static void installPlugin(
			String pluginName, Settings settings,
			ElasticsearchPluginManager elasticsearchPluginManager)
		throws IOException {

		File file = new File(settings.get("path.plugins"));

		file.mkdirs();

		Terminal terminal = Terminal.DEFAULT;

		terminal.verbosity(Verbosity.SILENT);

		elasticsearchPluginManager.removeAndInstallPlugin(terminal, pluginName);
	}

}