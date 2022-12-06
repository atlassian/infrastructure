package com.atlassian.performance.tools.infrastructure.api.distribution

import com.atlassian.performance.tools.infrastructure.PublicAtlassianProduct

/**
 * Downloads Jira Service Desk installers from the official Jira downloads site.
 * @since 4.8.0
 */
class PublicJiraServiceDeskDistribution(
    version: String
) : ProductDistribution by PublicAtlassianProduct("atlassian-servicedesk-$version.tar.gz")
