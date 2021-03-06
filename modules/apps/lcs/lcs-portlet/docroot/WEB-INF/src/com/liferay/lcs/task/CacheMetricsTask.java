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

package com.liferay.lcs.task;

import com.liferay.lcs.management.MBeanServerService;
import com.liferay.lcs.management.ObjectNameKeyPropertyMapKeyStrategy;
import com.liferay.lcs.messaging.MetricsMessage;
import com.liferay.lcs.util.KeyGenerator;
import com.liferay.lcs.util.LCSConnectionManager;
import com.liferay.lcs.util.PortletPropsValues;
import com.liferay.portal.kernel.bean.PortalBeanLocatorUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.ObjectName;

/**
 * @author Ivica Cardic
 */
public class CacheMetricsTask implements ScheduledTask {

	@Override
	public Type getType() {
		return Type.LOCAL;
	}

	@Override
	public void run() {
		try {
			doRun();
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	public void setKeyGenerator(KeyGenerator keyGenerator) {
		_keyGenerator = keyGenerator;
	}

	public void setLCSConnectionManager(
		LCSConnectionManager lcsConnectionManager) {

		_lcsConnectionManager = lcsConnectionManager;
	}

	public void setmBeanServerService(MBeanServerService mBeanServerService) {
		_mBeanServerService = mBeanServerService;
	}

	protected Map<String, Object> getHibernateMetrics() throws Exception {
		Map<String, Object> map = new HashMap<>();

		Object sessionFactory = PortalBeanLocatorUtil.locate(
			"liferayHibernateSessionFactory");

		Class<?> sessionFactoryClass = sessionFactory.getClass();

		Method getStatisticsMethod = sessionFactoryClass.getDeclaredMethod(
			"getStatistics");

		Object statistics = getStatisticsMethod.invoke(sessionFactory);

		Class<?> statisticsClass = statistics.getClass();

		Field queryCacheHitCountField = statisticsClass.getDeclaredField(
			"queryCacheHitCount");

		queryCacheHitCountField.setAccessible(true);

		AtomicLong queryCacheHitCountAtomicLong =
			(AtomicLong)queryCacheHitCountField.get(statistics);

		map.put("QueryCacheHitCount", queryCacheHitCountAtomicLong.get());

		Field queryCacheMissCountField = statisticsClass.getDeclaredField(
			"queryCacheMissCount");

		queryCacheMissCountField.setAccessible(true);

		AtomicLong queryCacheMissCountAtomicLong =
			(AtomicLong)queryCacheMissCountField.get(statistics);

		map.put("QueryCacheMissCount", queryCacheMissCountAtomicLong.get());

		Field queryExecutionCountField = statisticsClass.getDeclaredField(
			"queryExecutionCount");

		queryExecutionCountField.setAccessible(true);

		AtomicLong queryExecutionCountAtomicLong =
			(AtomicLong)queryExecutionCountField.get(statistics);

		map.put("QueryExecutionCount", queryExecutionCountAtomicLong.get());

		Field queryExecutionMaxTimeField = statisticsClass.getDeclaredField(
			"queryExecutionMaxTime");

		queryExecutionMaxTimeField.setAccessible(true);

		AtomicLong queryExecutionMaxTimeAtomicLong =
			(AtomicLong)queryExecutionMaxTimeField.get(statistics);

		map.put("QueryExecutionMaxTime", queryExecutionMaxTimeAtomicLong.get());

		return map;
	}

	protected Map<String, Map<String, Object>> getLiferayMultiVMMetrics()
		throws Exception {

		Set<ObjectName> objectNames = _mBeanServerService.getObjectNames(
			new ObjectName(
				PortletPropsValues.CACHE_METRICS_MULTI_VM_OBJECT_NAME),
			null);

		return _mBeanServerService.getObjectNamesAttributes(
			objectNames,
			new String[] {"CacheHits", "CacheMisses", "ObjectCount"},
			new ObjectNameKeyPropertyMapKeyStrategy("name"));
	}

	protected Map<String, Map<String, Object>> getLiferaySingleVMMetrics()
		throws Exception {

		Set<ObjectName> objectNames = _mBeanServerService.getObjectNames(
			new ObjectName(
				PortletPropsValues.CACHE_METRICS_SINGLE_VM_OBJECT_NAME),
			null);

		return _mBeanServerService.getObjectNamesAttributes(
			objectNames,
			new String[] {"CacheHits", "CacheMisses", "ObjectCount"},
			new ObjectNameKeyPropertyMapKeyStrategy("name"));
	}

	protected Object getPayload() throws Exception {
		Map<String, Object> payload = new HashMap<>();

		payload.put("hibernateMetrics", getHibernateMetrics());
		payload.put("liferayMultiVMMetrics", getLiferayMultiVMMetrics());
		payload.put("liferaySingleVMMetrics", getLiferaySingleVMMetrics());

		return payload;
	}

	private void doRun() throws Exception {
		if (!_lcsConnectionManager.isReady()) {
			if (_log.isDebugEnabled()) {
				_log.debug("Waiting for LCS connection manager");
			}

			return;
		}

		MetricsMessage metricsMessage = new MetricsMessage();

		metricsMessage.setCreateTime(System.currentTimeMillis());
		metricsMessage.setKey(_keyGenerator.getKey());
		metricsMessage.setMetricsType(MetricsMessage.METRICS_TYPE_CACHE);
		metricsMessage.setPayload(getPayload());

		_lcsConnectionManager.sendMessage(metricsMessage);
	}

	private static Log _log = LogFactoryUtil.getLog(CacheMetricsTask.class);

	private KeyGenerator _keyGenerator;
	private LCSConnectionManager _lcsConnectionManager;
	private MBeanServerService _mBeanServerService;

}