package com.atlassian.performance.tools.infrastructure.api.virtualusers

import com.atlassian.performance.tools.virtualusers.api.VirtualUserLoad
import com.atlassian.performance.tools.virtualusers.api.VirtualUserOptions
import net.jcip.annotations.NotThreadSafe
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.iterable.Extractor
import org.junit.Test
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
        virtualUserLoad = load
    )

    @Test
    fun shouldSpreadLoadOverFourNodes() {
        val nodes = (1..4).map { SingleUseVirtualUsers() }
        val virtualUsers = MulticastVirtualUsers(nodes)

        virtualUsers.applyLoad(options)
        val virtualUserLoad = nodes.map { it.lastOptions!!.virtualUserLoad }
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

        assertThat(node.lastOptions!!.virtualUserLoad)
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

        val virtualUserLoad = nodes
            .map { it.lastOptions!!.virtualUserLoad }
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

    override fun gatherResults() {
        throw Exception("unexpected call")
    }
}
