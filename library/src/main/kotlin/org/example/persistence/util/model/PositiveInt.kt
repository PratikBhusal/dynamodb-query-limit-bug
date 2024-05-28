package org.example.persistence.util.model

@JvmInline
value class PositiveInt(private val value: UInt) {
    init {
        require(value > 0u) {
            "Value must be a positive integer. Was actually: $value"
        }
    }

    fun toInt() = value.toInt()

    override fun toString() = value.toString()
}
