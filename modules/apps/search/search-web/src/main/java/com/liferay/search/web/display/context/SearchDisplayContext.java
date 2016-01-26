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

package com.liferay.search.web.display.context;

import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.PredicateFilter;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Group;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PropsValues;
import com.liferay.search.facet.SearchFacet;
import com.liferay.search.facet.util.SearchFacetTracker;
import com.liferay.search.web.constants.SearchPortletKeys;
import com.liferay.search.web.constants.SearchPortletParams;

import java.util.List;

import javax.portlet.PortletPreferences;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Eudaldo Alonso
 */
public class SearchDisplayContext {

	public SearchDisplayContext(
		HttpServletRequest request, PortletPreferences portletPreferences) {

		_request = request;
		_portletPreferences = portletPreferences;
	}

	public String checkViewURL(String viewURL, String currentURL) {
		ThemeDisplay themeDisplay = getThemeDisplay();

		if (Validator.isNotNull(viewURL) &&
			viewURL.startsWith(themeDisplay.getURLPortal())) {

			viewURL = HttpUtil.setParameter(
				viewURL, "inheritRedirect", isViewInContext());

			if (!isViewInContext()) {
				viewURL = HttpUtil.setParameter(
					viewURL, "redirect", currentURL);
			}
		}

		return viewURL;
	}

	public int getCollatedSpellCheckResultDisplayThreshold() {
		if (_collatedSpellCheckResultDisplayThreshold != null) {
			return _collatedSpellCheckResultDisplayThreshold;
		}

		_collatedSpellCheckResultDisplayThreshold = GetterUtil.getInteger(
			_portletPreferences.getValue(
				"collatedSpellCheckResultDisplayThreshold", null),
			PropsValues.
				INDEX_SEARCH_COLLATED_SPELL_CHECK_RESULT_SCORES_THRESHOLD);

		if (_collatedSpellCheckResultDisplayThreshold < 0) {
			_collatedSpellCheckResultDisplayThreshold =
				PropsValues.
					INDEX_SEARCH_COLLATED_SPELL_CHECK_RESULT_SCORES_THRESHOLD;
		}

		return _collatedSpellCheckResultDisplayThreshold;
	}

	public Preferences getPreferences() {
		return _preferences;
	}

	public List<SearchFacet> getEnabledSearchFacets() {
		if (_enabledSearchFacets != null) {
			return _enabledSearchFacets;
		}

		_enabledSearchFacets = ListUtil.filter(
			SearchFacetTracker.getSearchFacets(),
			new PredicateFilter<SearchFacet>() {

				@Override
				public boolean filter(SearchFacet searchFacet) {
					return isDisplayFacet(searchFacet.getClassName());
				}

			});

		return _enabledSearchFacets;
	}

	public int getQueryIndexingThreshold() {
		if (_queryIndexingThreshold != null) {
			return _queryIndexingThreshold;
		}

		_queryIndexingThreshold = GetterUtil.getInteger(
			_portletPreferences.getValue("queryIndexingThreshold", null),
			PropsValues.INDEX_SEARCH_QUERY_INDEXING_THRESHOLD);

		if (_queryIndexingThreshold < 0) {
			_queryIndexingThreshold =
				PropsValues.INDEX_SEARCH_QUERY_INDEXING_THRESHOLD;
		}

		return _queryIndexingThreshold;
	}

	public int getQuerySuggestionsDisplayThreshold() {
		if (_querySuggestionsDisplayThreshold != null) {
			return _querySuggestionsDisplayThreshold;
		}

		_querySuggestionsDisplayThreshold = GetterUtil.getInteger(
			_portletPreferences.getValue(
				"querySuggestionsDisplayThreshold", null),
			PropsValues.INDEX_SEARCH_QUERY_SUGGESTION_SCORES_THRESHOLD);

		if (_querySuggestionsDisplayThreshold < 0) {
			_querySuggestionsDisplayThreshold =
				PropsValues.INDEX_SEARCH_QUERY_SUGGESTION_SCORES_THRESHOLD;
		}

		return _querySuggestionsDisplayThreshold;
	}

	public int getQuerySuggestionsMax() {
		if (_querySuggestionsMax != null) {
			return _querySuggestionsMax;
		}

		_querySuggestionsMax = GetterUtil.getInteger(
			_portletPreferences.getValue("querySuggestionsMax", null),
			PropsValues.INDEX_SEARCH_QUERY_SUGGESTION_MAX);

		if (_querySuggestionsMax <= 0) {
			_querySuggestionsMax =
				PropsValues.INDEX_SEARCH_QUERY_SUGGESTION_MAX;
		}

		return _querySuggestionsMax;
	}

	public Scope getScope(String scopeString) {
		if (Validator.isBlank(scopeString)) {
			return new Scope(getSearchScope());
		}

		return new Scope(scopeString);
	}

	public String getSearchConfiguration() {
		if (_searchConfiguration != null) {
			return _searchConfiguration;
		}

		_searchConfiguration = _portletPreferences.getValue(
			"searchConfiguration", StringPool.BLANK);

		return _searchConfiguration;
	}

	public String getSearchScope() {
		if (_searchScope != null) {
			return _searchScope;
		}

		_searchScope = _portletPreferences.getValue(
			"searchScope", SearchPortletParams.THIS_SITE);

		return _searchScope;
	}

	public boolean isCollatedSpellCheckResultEnabled() {
		if (_collatedSpellCheckResultEnabled != null) {
			return _collatedSpellCheckResultEnabled;
		}

		_collatedSpellCheckResultEnabled = GetterUtil.getBoolean(
			_portletPreferences.getValue(
				"collatedSpellCheckResultEnabled", null),
			PropsValues.INDEX_SEARCH_COLLATED_SPELL_CHECK_RESULT_ENABLED);

		return _collatedSpellCheckResultEnabled;
	}

	public boolean isDisplayFacet(String className) {
		return GetterUtil.getBoolean(
			_portletPreferences.getValue(className, null), true);
	}

	public boolean isDisplayMainQuery() {
		if (_displayMainQuery != null) {
			return _displayMainQuery;
		}

		_displayMainQuery = GetterUtil.getBoolean(
			_portletPreferences.getValue("displayMainQuery", null));

		return _displayMainQuery;
	}

	public boolean isDisplayOpenSearchResults() {
		if (_displayOpenSearchResults != null) {
			return _displayOpenSearchResults;
		}

		_displayOpenSearchResults = GetterUtil.getBoolean(
			_portletPreferences.getValue("displayOpenSearchResults", null));

		return _displayOpenSearchResults;
	}

	public boolean isDisplayResultsInDocumentForm() {
		if (_displayResultsInDocumentForm != null) {
			return _displayResultsInDocumentForm;
		}

		ThemeDisplay themeDisplay = getThemeDisplay();

		_displayResultsInDocumentForm = GetterUtil.getBoolean(
			_portletPreferences.getValue("displayResultsInDocumentForm", null));

		PermissionChecker permissionChecker =
			themeDisplay.getPermissionChecker();

		if (!permissionChecker.isCompanyAdmin()) {
			_displayResultsInDocumentForm = false;
		}

		return _displayResultsInDocumentForm;
	}

	public boolean isDLLinkToViewURL() {
		if (_dlLinkToViewURL != null) {
			return _dlLinkToViewURL;
		}

		_dlLinkToViewURL = false;

		return _dlLinkToViewURL;
	}

	public boolean isIncludeSystemPortlets() {
		if (_includeSystemPortlets != null) {
			return _includeSystemPortlets;
		}

		_includeSystemPortlets = false;

		return _includeSystemPortlets;
	}

	public boolean isQueryIndexingEnabled() {
		if (_queryIndexingEnabled != null) {
			return _queryIndexingEnabled;
		}

		_queryIndexingEnabled = GetterUtil.getBoolean(
			_portletPreferences.getValue("queryIndexingEnabled", null),
			PropsValues.INDEX_SEARCH_QUERY_INDEXING_ENABLED);

		return _queryIndexingEnabled;
	}

	public boolean isQuerySuggestionsEnabled() {
		if (_querySuggestionsEnabled != null) {
			return _querySuggestionsEnabled;
		}

		_querySuggestionsEnabled = GetterUtil.getBoolean(
			_portletPreferences.getValue("querySuggestionsEnabled", null),
			PropsValues.INDEX_SEARCH_QUERY_SUGGESTION_ENABLED);

		return _querySuggestionsEnabled;
	}

	public boolean isShowMenu() {
		for (SearchFacet searchFacet : SearchFacetTracker.getSearchFacets()) {
			if (isDisplayFacet(searchFacet.getClassName())) {
				return true;
			}
		}

		return false;
	}

	public boolean isViewInContext() {
		if (_viewInContext != null) {
			return _viewInContext;
		}

		_viewInContext = GetterUtil.getBoolean(
			_portletPreferences.getValue("viewInContext", null), true);

		return _viewInContext;
	}

	protected ThemeDisplay getThemeDisplay() {
		return (ThemeDisplay)_request.getAttribute(WebKeys.THEME_DISPLAY);
	}

	private Integer _collatedSpellCheckResultDisplayThreshold;
	private Boolean _collatedSpellCheckResultEnabled;
	private final Preferences _preferences = new Preferences();
	private Boolean _displayMainQuery;
	private Boolean _displayOpenSearchResults;
	private Boolean _displayResultsInDocumentForm;
	private Boolean _dlLinkToViewURL;
	private List<SearchFacet> _enabledSearchFacets;
	private Boolean _includeSystemPortlets;
	private final PortletPreferences _portletPreferences;
	private Boolean _queryIndexingEnabled;
	private Integer _queryIndexingThreshold;
	private Integer _querySuggestionsDisplayThreshold;
	private Boolean _querySuggestionsEnabled;
	private Integer _querySuggestionsMax;
	private final HttpServletRequest _request;
	private String _searchConfiguration;
	private String _searchScope;
	private Boolean _viewInContext;


	public class Preferences {

		public boolean isSearchScope(String scope) {
			if (Validator.equals(getSearchScope(), scope)) {
				return true;
			}

			return false;
		}

		public boolean isSearchScopeEverythingAvailable() {
			ThemeDisplay themeDisplay = getThemeDisplay();

			Group group = themeDisplay.getScopeGroup();

			if (group.isStagingGroup()) {
				return false;
			}

			return true;
		}

		public boolean isSearchScopeLetTheUserChoose() {
			return isSearchScope("let-the-user-choose");
		}

	}

	public class Scope {

		public String getScopeString() {
			return _scopeString;
		}

		public boolean isEverything() {
			return isSearchScope(SearchPortletParams.EVERYTHING);
		}

		public boolean isSearchScope(String scope) {
			if (Validator.equals(_scopeString, scope)) {
				return true;
			}

			return false;
		}

		protected Scope(String scopeString) {
			_scopeString = scopeString;
		}

		private final String _scopeString;

	}

}