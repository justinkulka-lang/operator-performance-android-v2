package com.jk.operatorperformance.program

/**
 * Stored V74 position, display phase, and the handoff digest.
 * [ProgramV74] stays the allocation source and is not edited for these rules.
 */
object V74Pointer {
    const val AUTHORITY_SHA256 = "f083d0a9fa694034f996c774e02d706a4d65e6b8ecd9d7f0705fef26e3d9e988"
    const val CONTINUOUS_LINE = "Continuous program: four 8-week blocks, repeated."

    private val romans = listOf("I", "II", "III", "IV")
    private val phaseNames = listOf("Draft", "Build", "Backstop", "Live")
    private val purposes = listOf(
        "Base volume.",
        "Peak volume.",
        "Heavier main lifts.",
        "Rotated lifts.",
    )

    fun loopFor(absoluteWeek: Int): Int {
        require(absoluteWeek >= 1) { "Absolute week starts at 1." }
        return (absoluteWeek - 1) / 32 + 1
    }

    /** C-5 position. Used for a fresh start, migration activation, and entering V74 from a retained week. */
    fun position(absoluteWeek: Int): Triple<Int, Int, Int> {
        val (block, blockWeek) = ProgramV74.position(absoluteWeek)
        return Triple(block, blockWeek, loopFor(absoluteWeek))
    }

    /** C-7 absolute week. Loop stays 1; this is not the C-5 loop. */
    fun absoluteWeek(block: Int, blockWeek: Int): Int {
        require(block in 1..4) { "Block must be 1–4." }
        require(blockWeek in 1..8) { "Block week must be 1–8." }
        return (block - 1) * 8 + blockWeek
    }

    fun roman(block: Int): String {
        require(block in 1..4)
        return romans[block - 1]
    }

    fun phaseName(block: Int): String {
        require(block in 1..4)
        return phaseNames[block - 1]
    }

    fun phase(block: Int): ProgramBlock {
        require(block in 1..4)
        return ProgramBlock(
            name = phaseName(block),
            godName = roman(block),
            startWeek = (block - 1) * 8 + 1,
            endWeek = block * 8,
            status = BlockStatus.POPULATED,
            purpose = purposes[block - 1],
        )
    }

    fun header(absoluteWeek: Int, block: Int, blockWeek: Int): String =
        "Week $absoluteWeek · ${roman(block)} · ${phaseName(block)} · Week $blockWeek of 8"

    fun reference(absoluteWeek: Int, block: Int, blockWeek: Int, day: Int): SessionReference {
        val session = SessionResolverV74.resolve(block, blockWeek, day)
        return SessionReference(
            week = absoluteWeek,
            day = day,
            title = session.title,
            rows = session.rows,
            phase = phase(block),
            authority = ProgramAuthority.V74,
        )
    }
}
