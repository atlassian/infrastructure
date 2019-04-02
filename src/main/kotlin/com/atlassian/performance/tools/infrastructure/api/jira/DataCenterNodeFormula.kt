package com.atlassian.performance.tools.infrastructure.api.jira

//internal class DataCenterNodeFormula(
//    private val nodeIndex: Int,
//    private val sharedHome: Future<SharedHome>,
//    private val base: NodeFormula
//) : NodeFormula by base {
//
//    override fun provision(): StoppedNode {
//
//        val provisionedNode = base.provision()
//        val localSharedHome = sharedHome.get().localSharedHome
//
//        provisionedNode.ssh.newConnection().use {
//            sharedHome.get().mount(it)
//            val jiraHome = provisionedNode.jiraHome
//
//            it.execute("echo ehcache.object.port = 40011 >> $jiraHome/cluster.properties")
//            it.execute("echo jira.node.id = node$nodeIndex >> $jiraHome/cluster.properties")
//            it.execute("echo jira.shared.home = `realpath $localSharedHome` >> $jiraHome/cluster.properties")
//        }
//
//        return object : StoppedNode by provisionedNode {
//            override fun start(): StartedNode {
//                return provisionedNode.start().copy(
//                    name = name,
//                    analyticLogs = localSharedHome
//                )
//            }
//
//            override fun toString() = "node #$nodeIndex"
//        }
//    }
//
//    override fun toString() = "node formula #$nodeIndex"
//}