package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.jirasoftwareactions.api.JiraSoftwareScenario
import com.atlassian.performance.tools.virtualusers.api.VirtualUserLoad
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions
import com.atlassian.performance.tools.virtualusers.api.browsers.GoogleChrome
import com.atlassian.performance.tools.virtualusers.api.config.VirtualUserBehavior
import com.atlassian.performance.tools.virtualusers.api.config.VirtualUserTarget
import net.jcip.annotations.NotThreadSafe
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.iterable.Extractor
import org.junit.Test
import java.net.URI
import java.time.Duration
import java.time.Duration.ofSeconds

class MulticastVirtualUsersTest {
    private val load = VirtualUserLoad(
        virtualUsers = 12,
        hold = ofSeconds(10),
        ramp = ofSeconds(16),
        flat = ofSeconds(55)
    )
    private val options = VirtualUserOptions(
        behavior = VirtualUserBehavior(
            load = load,
            browser = GoogleChrome::class.java,
            scenario = JiraSoftwareScenario::class.java,
            diagnosticsLimit = 1,
            seed = 123
        ),
        target = VirtualUserTarget(
            webApplication = URI("http://localhost:8080/"),
            userName = "abc",
            password = "xyz"
        )
    )

    @Test
    fun shouldSpreadLoadOverFourNodes() {
        val nodes = (1..4).map { SingleUseVirtualUsers() }
        val virtualUsers = MulticastVirtualUsers(nodes)

        virtualUsers.applyLoad(options)
        val virtualUserLoad = nodes.map { it.getLastAppliedLoad() }
        assertThat(virtualUserLoad)
            .extracting(
                Extractor<VirtualUserLoad, ComparableLoad> {
                    ComparableLoad(it)
                }
            )
            .contains(
                ComparableLoad(3, ofSeconds(10), ofSeconds(4), ofSeconds(67)),
                ComparableLoad(3, ofSeconds(14), ofSeconds(4), ofSeconds(63)),
                ComparableLoad(3, ofSeconds(18), ofSeconds(4), ofSeconds(59)),
                ComparableLoad(3, ofSeconds(22), ofSeconds(4), ofSeconds(55))
            )
    }

    @Test
    fun shouldSpreadTheSameLoad() {
        val node = SingleUseVirtualUsers()
        val virtualUsers = MulticastVirtualUsers(listOf(node))

        virtualUsers.applyLoad(options)

        assertThat(node.getLastAppliedLoad())
            .extracting {
                ComparableLoad(it.virtualUsers, it.hold, it.ramp, it.flat)
            }
            .isEqualTo(ComparableLoad(load))
    }

    @Test
    fun shouldSpreadWithEqualTotals() {
        val nodes = (1..10).map { SingleUseVirtualUsers() }
        val virtualUsers = MulticastVirtualUsers(nodes)

        virtualUsers.applyLoad(options)

        val virtualUserLoad = nodes.map { it.getLastAppliedLoad() }
        assertThat(virtualUserLoad)
            .extracting(Extractor<VirtualUserLoad, Duration> {
                it.total
            })
            .contains(load.total)
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

        assertThat(exception).isNotNull()
        assertThat(exception!!.message)
            .isEqualTo("12 virtual users are not enough to spread into $tooManyNodes nodes")
    }

    @Test
    fun shouldSetUpJiraOnlyOnTheFirstNode() {
        val nodes = (1..5).map { SingleUseVirtualUsers() }
        val virtualUsers = MulticastVirtualUsers(nodes)

        virtualUsers.applyLoad(options)

        val firstNode = nodes.first()
        val theRest = nodes.subList(1, 5)
        assertThat(firstNode.hasSetUpJira())
            .`as`("the first node should set up Jira")
            .isTrue()
        assertThat(theRest.map { it.hasSetUpJira() })
            .`as`("the rest of the nodes should NOT set up Jira")
            .noneMatch { it == true }
    }
}

private data class ComparableLoad(
    val virtualUsers: Int,
    val hold: Duration,
    val ramp: Duration,
    val flat: Duration
) {
    constructor(load: VirtualUserLoad) : this(
        virtualUsers = load.virtualUsers,
        hold = load.hold,
        ramp = load.ramp,
        flat = load.flat
    )
}

@NotThreadSafe
private class SingleUseVirtualUsers : VirtualUsers {

    private var lastOptions: VirtualUserOptions? = null

    override fun applyLoad(
        options: VirtualUserOptions
    ) {
        if (lastOptions == null) {
            lastOptions = options
        } else {
            throw Exception("Don't apply load multiple times")
        }
    }

    override fun gatherResults() {
        throw Exception("unexpected call")
    }

    internal fun getLastAppliedLoad() = getLastAppliedOptions().behavior.load

    internal fun hasSetUpJira(): Boolean = getLastAppliedOptions().toCliArgs().none { it == "--skip-setup" }

    private fun getLastAppliedOptions(): VirtualUserOptions = lastOptions ?: throw Exception("Load was not applied yet")
}
