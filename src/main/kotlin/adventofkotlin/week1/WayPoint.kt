package adventofkotlin.week1

enum class WayType {
    START,
    GOAL,
    WALL,
    FREE,
    PATH
}

data class WayPoint(val x: Int, val y: Int, var type: WayType) : Iterable<WayPoint> {

    var distance = Double.MAX_VALUE

    var predecessor = BOTTOM

    fun neighbors(): List<Pair<Int, Int>> = ((x-1)..(x+1)).flatMap { x -> ((y-1)..(y+1)).map { y -> Pair(x, y) } }

    override fun iterator(): Iterator<WayPoint> = WayPointIterator(this)

    inner class WayPointIterator(var current: WayPoint) : Iterator<WayPoint> {
        override fun next(): WayPoint {
            val next = current
            current = current.predecessor
            return next
        }

        override fun hasNext(): Boolean = current != BOTTOM
    }

    companion object {
        val BOTTOM = WayPoint(-1, -1, WayType.WALL)
    }

}
