package models

data class Platforms (
    val platform : Platform,
    val released_at : String?,
    val requirements_en : Requirement?

) {
    inner class Platform (
        val id : Int,
        val name : String,
        val slug : String,
        val image_background : String

    ) {
        override fun toString(): String {
            return this.name
        }
    }

    inner class Requirement(
        val minimum: String,
        val recommended: String,

    ) {}

    override fun toString(): String {
        return this.platform.toString()
    }

}