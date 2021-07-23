package com.atlassian.performance.tools.infrastructure.api.network

interface Networked {

    /**
     * @return CIDR
     */
    fun subnet(): String
}