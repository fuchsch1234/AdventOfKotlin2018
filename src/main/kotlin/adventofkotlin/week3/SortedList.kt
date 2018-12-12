package adventofkotlin.week3

import java.util.*


interface SortedMutableList<T> : Iterable<T> {
    val size: Int
    fun add(element: T)
    fun remove(element: T)
    operator fun get(index: Int): T
    operator fun contains(element: T): Boolean
}

/**
 * Sorted list using a binary tree to store all elements.
 *
 * @param comparator Comparator used to sort all elements in the list.
 */
class TreeList<T>(private val comparator: Comparator<T>) : SortedMutableList<T> {
    override val size: Int
        get() = this.count()

    /**
     * The binary tree holding all elements in the list in sorted order.
     *
     * Variable because tree nodes are immutable, so adding or removing an element must construct a new tree.
     */
    private var root: Tree<T> = Empty()

    override fun add(element: T) {
        root = treeAdd(root, element)
    }

    override fun remove(element: T) {
        root = treeRemove(root, element)
    }

    override fun get(index: Int): T = this.drop(index).first()

    override fun contains(element: T): Boolean = treeContains(root, element)

    override fun iterator(): Iterator<T> = TreeIterator(root)

    /**
     * Searches for a value equal to element in a binary tree.
     *
     * @param tree The binary tree to search.
     * @param element The element to search in the tree.
     * @return true if element is contained in tree, false otherwise
     */
    private tailrec fun treeContains(tree: Tree<T>, element: T): Boolean = when(tree) {
        is Node -> {
            when (comparator.compare(element, tree.value)) {
                0 -> {
                    // Comparator matches. Check is element equals node value, or continue search in left subtree.
                    if (tree.value == element) {
                        true
                    } else {
                        treeContains(tree.left, element)
                    }
                }
                // Element is smaller than value in current node, search in left subtree
                in Int.MIN_VALUE..-1 -> treeContains(tree.left, element)
                // Element is bigger than value in current node, search in right subtree
                else -> treeContains(tree.right, element)
            }
        }
        // Reached leaf node without finding element
        is Empty -> false
    }

    /**
     * Add element to a tree.
     *
     * Constructs a new tree containing all elements of original tree in the same order and the new element.
     * Element is always added to a tree leaf.
     *
     * @param tree The original tree to add the element to.
     * @param element The element which is added to the tree.
     * @return A new tree containing all elements of the original tree and the new element in sorted order.
     */
    private fun treeAdd(tree: Tree<T>, element: T): Tree<T> = when(tree) {
        is Node -> {
            when (comparator.compare(element, tree.value)) {
                // Element compares smaller or equal to value in current node, add to left subtree.
                in Int.MIN_VALUE..0 -> Node(tree.value, treeAdd(tree.left, element), tree.right)
                // Element compares bigger to value in current node, add to right subtree.
                else -> Node(tree.value, tree.left, treeAdd(tree.right, element))
            }
        }
        // Reached leaf node, construct new node containing the element.
        is Empty -> Node(element, Empty(), Empty())
    }

    /**
     * Remove an element from a tree.
     *
     * Constructs a new tree containing all elements of original tree without the element that should be removed.
     *
     * @param tree The original tree from which to remove the element.
     * @param element The element which should be removed.
     * @return A new tree containing all elements of the original tree except for the removed element in sorted order.
     */
    private fun treeRemove(tree: Tree<T>, element: T): Tree<T> = when(tree) {
        is Node -> {
            when (comparator.compare(element, tree.value)) {
                0 -> {
                    if (tree.value == element) {
                        // Found the element, remove it, but retain subtrees of the node.
                        when  {
                            // If one of the subtrees is Empty, just use the other subtree in place of the removed node.
                            tree.left is Empty -> tree.right
                            tree.right is Empty -> tree.left
                            else -> {
                                // Special case: Both left and right node contain more elements.
                                // Extract the leftmost element from the right subtree and use it as the new parent
                                // for both subtrees instead of the removed node.
                                val (head, rest) = extractLeftMostNode(tree.right as Node)
                                Node(head.value, tree.left, rest)
                            }
                        }
                    } else {
                        // Element compares same to value in current node, but is not equal, remove from left subtree.
                        Node(tree.value, treeRemove(tree.left, element), tree.right)
                    }
                }
                // Element compares smaller to value in current node, remove from left subtree.
                in Int.MIN_VALUE..-1 -> Node(tree.value, treeRemove(tree.left, element), tree.right)
                // Element compares bigger to value in current node, remove from right subtree.
                else -> Node(tree.value, tree.left, treeRemove(tree.right, element))
            }
        }
        // Reached leaf without finding the element. Does not modify the tree.
        is Empty -> Empty()
    }

    /**
     * Extracts the leftmost node from a tree.
     *
     * @param tree The tree the leftmost node is extracted from.
     * @return Pair of the extracted node and a new tree, that is equal to the original tree,
     * but without the extracted node.
     */
    private fun extractLeftMostNode(tree: Node<T>): Pair<Node<T>, Tree<T>> = when(tree.left) {
        is Node -> {
            // There exists still a node to the left. Continue extraction in left subtree.
            val (head, rest) = extractLeftMostNode(tree.left)
            // Return extracted node and construct a new tree without this node. Only the left subtree is modified,
            // so it is replaced with the result of extracting the leftmost node from it.
            Pair(head, Node(tree.value, rest, tree.right))
        }
        // Reached leftmost node. The leftmost node is this node and the tree without it, is just its right subtree.
        is Empty -> Pair(tree, tree.right)
    }

    companion object {

        /**
         * Convenience function to create a sorted list from a regular list.
         *
         * @param comparator Comparator used to sort the elements in the list.
         * @param elements Initial elements for this list.
         */
        fun <T> fromList(comparator: Comparator<T>, vararg elements: T): TreeList<T> {
            val list = TreeList(comparator)
            for (element in elements) {
                list.add(element)
            }
            return list
        }

        /**
         * Convenience function to create a sorted list from a regular list.
         *
         * Takes only comparable types. Returned list is sorted using elements natural ordering.
         *
         * @param elements Initial elements for this list.
         */
        fun <T: Comparable<T>> fromList(vararg elements: T): TreeList<T>
                = fromList(compareBy { it }, *elements)
    }

    /**
     * Iterator over binary tree structures.
     *
     * Visits all elements in depth first infix order.
     *
     * @param tree The tree to iterate over
     */
    inner class TreeIterator<T>(tree: Tree<T>): Iterator<T> {

        /**
         * Holds nodes that have not been visited yet.
         */
        private val nodeStack = Stack<Node<T>>()

        init {
            // Descend down the tree, always using the left subtree, memorizing all nodes on the way in the stack.
            // When the loop finished nodeStacks top element is the leftmost non empty node in the tree.
            var node = tree
            while (node is Node) {
                nodeStack.push(node)
                node = node.left
            }
        }

        override fun hasNext(): Boolean = !nodeStack.empty()

        override fun next(): T {
            // Take latest not yet handled node from the stack.
            // If a node is the stacks top element its complete left subtree is already iterated over.
            val next = nodeStack.pop()
            // Tree is iterated depth first in infix order. So we continue with this nodes right subtree.
            var tree: Tree<T> = next.right
            // Descend down right subtree, always using the left subtree.
            while (tree is Node) {
                nodeStack.push(tree)
                tree = tree.left
            }
            return next.value
        }

    }

}

/**
 * Base class for binary tree. May be either a Node containing a value, left and right children or an Empty node.
 */
sealed class Tree<T>

/**
 * Binary Tree Node
 *
 * @param value The element held by this node.
 * @param left Left subtree, holds all elements that compare lesser or equal to value.
 * @param right Right subtree, holds all elements that compare greater to value.
 */
class Node<T>(val value: T,
              val left: Tree<T>,
              val right: Tree<T>
): Tree<T>()

/**
 * Empty node.
 *
 * This node type is used as a trees lead nodes.
 */
class Empty<T>: Tree<T>()
