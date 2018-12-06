package adventofkotlin.week1

import java.lang.Exception
import java.util.*
import kotlin.math.abs
import kotlin.math.hypot

fun addPath(map: String) : String = Map.createMap(map).run {
    solve()
    toString()
}

fun <E> PriorityQueue<E>.insertOrUpdate(element: E): Boolean {
    if (this.contains(element)) {
        this.remove(element)
    }
    return this.add(element)
}

class Map(private val map: Array<Array<WayPoint>>) {

    private val start =
        map.map{ it.find { it.type == WayType.START }}.find { it != null } ?: throw Exception("Missing start node")

    private val goal =
        map.map{ it.find { it.type == WayType.GOAL }}.find { it != null } ?: throw Exception("Missing goal node")

    override fun toString(): String {
        var str = String()
        map.map { line ->
            line.map {
                str = str.plus(when (it.type) {
                    WayType.START -> '*'
                    WayType.GOAL -> '*'
                    WayType.FREE -> '.'
                    WayType.PATH -> '*'
                    WayType.WALL -> 'B'
                })
            }
            str = str.plus('\n')
        }
        return str.removeSuffix("\n")
    }

    private fun distanceBetween(a: WayPoint, b: WayPoint): Double = when(abs(a.x - b.x) + abs(a.y - b.y)) {
        1 -> 1.0
        2 -> 1.5
        else -> 0.0
    }

    private fun distanceToGoal(point: WayPoint): Double =
        hypot((goal.x - point.x).toDouble(), (goal.y - point.y).toDouble())

    fun solve() {
        val queue = PriorityQueue<WayPoint>(compareBy{it.distance + distanceToGoal(it)})
        queue.add(start)

        while (!queue.isEmpty()) {
            val nextPoint = queue.poll()
            if (nextPoint == goal) break

            nextPoint.neighbors().forEach { (x, y) ->
                try {
                    val point = map[y][x]
                    val distanceToPoint = distanceBetween(nextPoint, point)
                    if (point.distance > (nextPoint.distance + distanceToPoint) && point.type != WayType.WALL) {
                        point.distance = nextPoint.distance + distanceToPoint
                        point.predecessor = nextPoint
                        queue.insertOrUpdate(point)
                    }
                } catch (e: ArrayIndexOutOfBoundsException) {
                }
            }
        }
        for (predecessor in goal) {
            predecessor.type = WayType.PATH
        }
    }

    companion object {
        fun createMap(map: String) : Map {
            val result = emptyList<Array<WayPoint>>().toMutableList()
            for ((y, line) in map.lines().withIndex()) {
                val points = emptyArray<WayPoint>().toMutableList()
                for ((x, c) in line.toCharArray().withIndex()) {
                    when(c) {
                        '.' -> points.add(WayPoint(x, y, WayType.FREE))
                        'B' -> points.add(WayPoint(x, y, WayType.WALL))
                        'S' -> points.add(WayPoint(x, y, WayType.START).apply{ distance = 0.0 })
                        'X' -> points.add(WayPoint(x, y, WayType.GOAL))
                        else -> throw Exception("Unknown point type \"${c}\"")
                    }
                }
                result.add(points.toTypedArray())
            }
            return Map(result.toTypedArray())
        }
    }
}
