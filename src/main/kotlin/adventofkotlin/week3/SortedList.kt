package adventofkotlin.week3

import java.util.*


interface SortedMutableList<T> : Iterable<T> {
    val size: Int
    fun add(element: T)
    fun remove(element: T)
    operator fun get(index: Int): T
    operator fun contains(element: T): Boolean
}

class TreeList<T>(private val comparator: Comparator<T>) : SortedMutableList<T> {
    override val size: Int
        get() = this.count()

    private var root: Tree<T> = Empty()

    override fun add(element: T) {
        root = treeAdd(root, element)
    }

    override fun remove(element: T) {
        TODO("not implemented")
    }

    override fun get(index: Int): T = this.drop(index).first()

    override fun contains(element: T): Boolean = treeContains(root, element)

    override fun iterator(): Iterator<T> = TreeIterator(root)

    private fun treeContains(tree: Tree<T>, element: T): Boolean = when(tree) {
        is Node -> {
            when (comparator.compare(element, tree.value)) {
                0 -> {
                    if (tree.value == element) {
                        true
                    } else {
                        treeContains(tree.left, element)
                    }
                }
                in Int.MIN_VALUE..-1 -> treeContains(tree.left, element)
                else -> treeContains(tree.right, element)
            }
        }
        is Empty -> false
    }

    private fun treeAdd(tree: Tree<T>, element: T): Tree<T> = when(tree) {
        is Node -> {
            when (comparator.compare(element, tree.value)) {
                0 -> {
                    if (tree.value == element) {
                        tree
                    } else {
                        Node(tree.value, treeAdd(tree.left, element), tree.right)
                    }
                }
                in Int.MIN_VALUE..-1 -> Node(tree.value, treeAdd(tree.left, element), tree.right)
                else -> Node(tree.value, tree.left, treeAdd(tree.right, element))
            }
        }
        is Empty -> Node(element, Empty(), Empty())
    }

    companion object {

        fun <T> fromList(comparator: Comparator<T>, vararg elements: T): TreeList<T> {
            val list = TreeList(comparator)
            for (element in elements) {
                list.add(element)
            }
            return list
        }

        fun <T: Comparable<T>> fromList(vararg elements: T): TreeList<T>
                = fromList(compareBy { it }, *elements)
    }

    inner class TreeIterator<T>(tree: Tree<T>): Iterator<T> {

        private val nodeStack = Stack<Node<T>>()

        init {
            var tree = tree
            while (tree is Node) {
                nodeStack.push(tree)
                tree = tree.left
            }
        }

        override fun hasNext(): Boolean = !nodeStack.empty()

        override fun next(): T {
            val next = nodeStack.pop()
            var tree: Tree<T> = next.right
            while (tree is Node) {
                nodeStack.push(tree)
                tree = tree.left
            }
            return next.value
        }

    }

}

sealed class Tree<T>

class Node<T>(val value: T,
              val left: Tree<T>,
              val right: Tree<T>
): Tree<T>()

class Empty<T>: Tree<T>()
