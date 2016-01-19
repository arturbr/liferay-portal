package com.liferay.portal.search.elasticsearch.internal.index;

import com.liferay.portal.kernel.test.IdempotentRetryAssert;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse.FieldMappingMetaData;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.IndicesAdminClient;

import org.junit.Assert;
public class AnalyzerUtil {

	public AnalyzerUtil(AdminClient adminClient, long index) {
		_init(adminClient, String.valueOf(index));
	}

	public AnalyzerUtil(AdminClient adminClient, String index) {
		_init(adminClient, index);
	}

	protected static String getAnalyzer(
		FieldMappingMetaData fieldMappingMetaData, final String field) {

		Map<String, Object> mappings = fieldMappingMetaData.sourceAsMap();

		Map<String, Object> mapping = (Map<String, Object>)mappings.get(field);

		return (String)mapping.get("analyzer");
	}

	protected static FieldMappingMetaData getFieldMapping(
		AdminClient adminClient, String index, String type, String field) {

		IndicesAdminClient indicesAdminClient = adminClient.indices();

		GetFieldMappingsRequestBuilder getFieldMappingsRequestBuilder =
			indicesAdminClient.prepareGetFieldMappings(index);

		getFieldMappingsRequestBuilder.setFields(field);
		getFieldMappingsRequestBuilder.setTypes(type);

		GetFieldMappingsResponse getFieldMappingsResponse =
			getFieldMappingsRequestBuilder.get();

		return getFieldMappingsResponse.fieldMappings(index, type, field);
	}

	protected void assertAnalyzer(final String field, final String analyzer)
		throws Exception {

		IdempotentRetryAssert.retryAssert(
			10, TimeUnit.SECONDS,
			new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					Assert.assertEquals(
						analyzer, getAnalyzer(getFieldMapping(field), field));

					return null;
				}

			});
	}

	protected FieldMappingMetaData getFieldMapping(final String field) {
		return getFieldMapping(
			_adminClient, _index, LiferayTypeMappingsConstants.TYPE, field);
	}

	private void _init(AdminClient adminClient, String index) {
		_adminClient = adminClient;
		_index = index;
	}

	private AdminClient _adminClient;
	private String _index;

}