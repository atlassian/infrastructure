package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.jiraactions.scenario.Scenario
import com.atlassian.performance.tools.virtualusers.api.VirtualUserLoad
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions
import net.jcip.annotations.NotThreadSafe
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.URI
import java.time.Duration.ofSeconds

class MulticastVirtualUsersTest {
    private val load = VirtualUserLoad(
        virtualUsers = 12,
        hold = ofSeconds(10),
        ramp = ofSeconds(16),
        flat = ofSeconds(55)
    )
    private val options = VirtualUserOptions(
        virtualUserLoad = load
    )

    @Test
    fun shouldSpreadLoadOverFourNodes() {
        val nodes = (1..4).map { SingleUseVirtualUsers() }
        val virtualUsers = MulticastVirtualUsers(nodes)

        virtualUsers.applyLoad(options)

        assertThat(
            nodes.map { it.lastOptions!!.virtualUserLoad },
            hasItems(
                VirtualUserLoad(
                    virtualUsers = 3,
                    hold = ofSeconds(10),
                    ramp = ofSeconds(4),
                    flat = ofSeconds(67)
                ),
                VirtualUserLoad(
                    virtualUsers = 3,
                    hold = ofSeconds(14),
                    ramp = ofSeconds(4),
                    flat = ofSeconds(63)
                ),
                VirtualUserLoad(
                    virtualUsers = 3,
                    hold = ofSeconds(18),
                    ramp = ofSeconds(4),
                    flat = ofSeconds(59)
                ),
                VirtualUserLoad(
                    virtualUsers = 3,
                    hold = ofSeconds(22),
                    ramp = ofSeconds(4),
                    flat = ofSeconds(55)
                )
            )
        )
    }

    @Test
    fun shouldSpreadTheSameLoad() {
        val node = SingleUseVirtualUsers()
        val virtualUsers = MulticastVirtualUsers(listOf(node))

        virtualUsers.applyLoad(options)

        assertThat(node.lastOptions!!.virtualUserLoad, equalTo(load))
    }

    @Test
    fun shouldSpreadWithEqualTotals() {
        val nodes = (1..10).map { SingleUseVirtualUsers() }
        val virtualUsers = MulticastVirtualUsers(nodes)

        virtualUsers.applyLoad(options)

        nodes
            .map { it.lastOptions!!.virtualUserLoad }
            .forEach { assertThat(it.total, equalTo(load.total)) }
    }

    @Test
    fun shouldAvoidEmptyNodes() {
        val tooManyNodes = 300
        val nodes = (1..tooManyNodes).map { SingleUseVirtualUsers() }
        val virtualUsers = MulticastVirtualUsers(nodes)

        val exception: Exception? = try {
            virtualUsers.applyLoad(options)
            null
        } catch (e: Exception) {
            e
        }

        assertThat(exception, notNullValue())
        assertThat(
            exception!!.message,
            equalTo("12 virtual users are not enough to spread into $tooManyNodes nodes")
        )
    }

}

@NotThreadSafe
private class SingleUseVirtualUsers : VirtualUsers {

    var lastOptions: VirtualUserOptions? = null

    override fun applyLoad(
        options: VirtualUserOptions
    ) {
        if (lastOptions == null) {
            lastOptions = options
        } else {
            throw Exception("Don't apply load multiple times")
        }
    }

    override fun applyLoad(
        jira: URI,
        loadProfile: LoadProfile,
        scenarioClass: Class<out Scenario>?,
        diagnosticsLimit: Int?
    ) {
        throw Exception("unexpected call")
    }

    override fun gatherResults() {
        throw Exception("unexpected call")
    }
}
