package com.atlassian.performance.tools.infrastructure.metric

enum class ReductionType(val lambda: (Iterable<Double>) -> Double) {
    SUM({ it.sum() }),
    MEAN({ it.average() })
}