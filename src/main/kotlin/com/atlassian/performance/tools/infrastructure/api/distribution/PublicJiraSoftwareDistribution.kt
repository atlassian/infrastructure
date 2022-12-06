package com.atlassian.performance.tools.infrastructure.api.distribution

import com.atlassian.performance.tools.infrastructure.PublicAtlassianProduct

/**
 * Downloads Jira Software installers from the official Jira downloads site.
 * @since 4.8.0
 */
class PublicJiraSoftwareDistribution(
    version: String
) : ProductDistribution by PublicAtlassianProduct("atlassian-jira-software-$version.tar.gz")
