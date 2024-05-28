package org.example.persistence.util.model

import com.fasterxml.uuid.Generators
import java.util.UUID

@JvmInline
value class UUIDv7(private val value: UUID) {
    init {
        @Suppress("detekt:style:MagicNumber")
        require(value.version() == 7) {
            "Expected version == 7, but was actually ${value.version()}"
        }

        @Suppress("detekt:style:MagicNumber")
        require(value.variant() == 2) {
            "Expected variant == 2, but was actually ${value.variant()}"
        }
    }

    override fun toString() = value.toString()

    companion object {
        fun randomUUIDv7() = UUIDv7(Generators.timeBasedEpochGenerator().generate())
    }
}
