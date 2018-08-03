package com.atlassian.performance.tools.infrastructure.metric

enum class Dimension(
    val description: String,
    val reduction: ReductionType
) {
    CPU_LOAD(
        description = "CPU load",
        reduction = ReductionType.MEAN
    ),
    JSTAT_SURVI_0(
        description = "Survivor space 0 utilization %",
        reduction = ReductionType.MEAN
    ),
    JSTAT_SURVI_1(
        description = "Survivor space 1 utilization %",
        reduction = ReductionType.MEAN
    ),
    JSTAT_EDEN(
        description = "Eden space utilisation %",
        reduction = ReductionType.MEAN
    ),
    JSTAT_OLD(
        description = "Old generation space utilisation %",
        reduction = ReductionType.MEAN
    ),
    JSTAT_META(
        description = "Metaspace utilisation %",
        reduction = ReductionType.MEAN
    ),
    JSTAT_COMPRESSED_CLASS(
        description = "Compressed class space utilisation %",
        reduction = ReductionType.MEAN
    ),
    JSTAT_YOUNG_GEN_GC(
        description = "Number of young generation GC events",
        reduction = ReductionType.SUM
    ),
    JSTAT_YOUNG_GEN_GC_TIME(
        description = "Young generation GC time (s)",
        reduction = ReductionType.SUM
    ),
    JSTAT_FULL_GC(
        description = "Number of full GC events",
        reduction = ReductionType.SUM
    ),
    JSTAT_FULL_GC_TIME(
        description = "Full GC time (s)",
        reduction = ReductionType.SUM
    ),
    JSTAT_TOTAL_GC_TIME(
        description = "Accumulated GC time (s)",
        reduction = ReductionType.MEAN
    )
}