package models

data class Genre(
    val id : Int,
    val name : String,
    val slug : String,
) {
    override fun equals(other: Any?): Boolean {
        if (other is String) {
            return other == this.name
        }
        if (other is Int) {
            return other == this.id
        }
        if (other is Genre) {
            return (other.id == this.id && other.name == this.name)
        }
        return super.equals(other)
    }

    override fun toString(): String {
        return this.name
    }
}