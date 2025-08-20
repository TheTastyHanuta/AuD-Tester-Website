import tester.annotations.*;

import org.junit.Test;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SecretClass
public class BinarySearchTreeSecretTest {
	// =============== SYSTEM ===============
//	@Rule
//	public final PointsLogger pointsLogger = new PointsLogger();
//	@ClassRule
//	public final static PointsSummary pointsSummary = new PointsSummary();

	// ========= TEST DATA =========
	private static final Random RND = new Random(4711_0815_666L);

	private int[] createRandomArray(int size) {
		int[] vals = new int[size];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = (int) (RND.nextDouble() * 1000);

			// Prevent duplicate values
			for (int j = 0; j < i; j++) {
				if (vals[i] == vals[j]) {
					i--;
					break;
				}
			}
		}
		return vals;
	}

	private Object getRoot(BinarySearchTree tree) {
		try {
			return TestcaseHelper.getValueOfPrivateField(tree, "root");
		} catch (Exception e) {
			TestcaseHelper.fail("There was an unexpected exception during grabbing the root of the tree.");
			return null;
		}
	}

	private int value(Object item) {
		try {
			return (int) TestcaseHelper.getValueOfPrivateField(item, "value");
		} catch (Exception e) {
			TestcaseHelper.fail("There was an unexpected exception during grabbing the value of an element."
					+ "Is the inner class implemented right (int) and has right attribute names?");
			return Integer.MAX_VALUE;
		}
	}

	// ============ TESTS ==========
	@Test(timeout = 500)
	@Points(exID = "0. Coderichtlinien", bonus = 1, comment = "Wird noch manuell korrigiert (2 Punkte)")
	public void secTest__checkCodeStyle() {
		fail();
	}

	@Test(timeout = 500)
	@Points(exID = "ElementExistsException", bonus = 0.5, comment = "ElementExistsException extends Exception")
	public void pubTest__exception() {
		TestcaseHelper.checkSuperclass(ElementExistsException.class, Exception.class);
	}

	@Test(timeout = 500)
	@Points(exID = "BinarySearchTreeTests", bonus = 0.5, comment = "clear")
	public void pubTest__clear() throws ElementExistsException {
		BinarySearchTree bst = new BinarySearchTree();
		bst.insert(42);
		bst.clear();
		Object root = getRoot(bst);
		assertEquals("The root of the tree should be null after clear().", null, root);
	}

	@Test(timeout = 500)
	@Points(exID = "BinarySearchTreeTests", bonus = 1, comment = "TreeNode")
	public void A03TestInnerclass() {
		Class<?> innerClazz = TestcaseHelper.getInnerClass("BinarySearchTreeTests.BinarySearchTree$TreeNode");
		String[] names = new String[] { "left", "right", "parent", "value" };
		Class<?>[] types = new Class<?>[] { innerClazz, innerClazz, innerClazz, int.class };
		int[] modifiers = new int[] { Modifier.PRIVATE, Modifier.PRIVATE, Modifier.PRIVATE, Modifier.PRIVATE };
		TestcaseHelper.checkAttributes(innerClazz, names, types, modifiers);
	}

	@Test(timeout = 500)
	@Points(exID = "BinarySearchTreeTests", bonus = 4, comment = "insert and exists")
	public void B01TestInsertExists() {
		BinarySearchTree bst = new BinarySearchTree();

		TestcaseHelper.assertEquals(false, bst.exists(1), "Element exists in empty tree!");

		try {
			bst.insert(1);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}
		TestcaseHelper.assertNotNull(getRoot(bst), "The root Element was not set by insertion!");
		TestcaseHelper.assertEquals(1, value(getRoot(bst)), "The inserted Element did not contain the value!");
		TestcaseHelper.assertEquals(true, bst.exists(1), "Element just inserted does not exist in tree!");

		try {
			bst.insert(2);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}
		TestcaseHelper.assertEquals(true, bst.exists(2), "Element just inserted does not exist in tree!");

		try {
			bst.insert(2);
			TestcaseHelper.fail("Insert threw no Exception for already inserted value!");
		} catch (Exception e) {
			if (!ElementExistsException.class.isInstance(e)) {
				TestcaseHelper.fail("Insert threw wrong Exception for already inserted value!");
			}
		}

		try {
			bst.insert(4);
			bst.insert(8);
			bst.insert(5);
			bst.insert(6);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		// check if all elements can still be found
		TestcaseHelper
				.assertArrayEquals(new boolean[] { true, true, false, true, true, true, false, true, false },
						new boolean[] { bst.exists(1), bst.exists(2), bst.exists(3), bst.exists(4), bst.exists(5),
								bst.exists(6), bst.exists(7), bst.exists(8), bst.exists(9) },
						"Check for exists failed!");

		// Try to insert some elements again
		try {
			bst.insert(4);
			TestcaseHelper.fail("Insert threw no Exception for already inserted value!");
		} catch (Exception e) {
			if (!ElementExistsException.class.isInstance(e)) {
				TestcaseHelper.assertEquals(true, bst.exists(4), "Element inserted does not exist in tree!");
				TestcaseHelper.fail("Insert threw wrong Exception for already inserted value!");
			}
		}

		try {
			bst.insert(8);
			TestcaseHelper.fail("Insert threw no Exception for already inserted value!");
		} catch (Exception e) {
			if (!ElementExistsException.class.isInstance(e)) {
				TestcaseHelper.assertEquals(true, bst.exists(8), "Element inserted does not exist in tree!");
				TestcaseHelper.fail("Insert threw wrong Exception for already inserted value!");
			}
		}

		try {
			bst.insert(6);
			TestcaseHelper.fail("Insert threw no Exception for already inserted value!");
		} catch (Exception e) {
			if (!ElementExistsException.class.isInstance(e)) {
				TestcaseHelper.assertEquals(true, bst.exists(6), "Element inserted does not exist in tree!");
				TestcaseHelper.fail("Insert threw wrong Exception for already inserted value!");
			}
		}
		// Try to insert one elements a third time
		try {
			bst.insert(4);
			TestcaseHelper.fail("Insert threw no Exception for already inserted value!");
		} catch (Exception e) {
			if (!ElementExistsException.class.isInstance(e)) {
				TestcaseHelper.assertEquals(true, bst.exists(4), "Element inserted does not exist in tree!");
				TestcaseHelper.fail("Insert threw wrong Exception for already inserted value!");
			}
		}
	}

	@Test(timeout = 500)
	@Points(exID = "BinarySearchTreeTests", bonus = 7, comment = "remove")
	public void pubTest__bst_remove() {
		BinarySearchTree bst = new BinarySearchTree();
		try {
			bst.remove(3);
			TestcaseHelper.fail("Remove threw no Exception for empty tree!");
		} catch (NoSuchElementException e) {
			// success
		} catch (Exception e) {
			e.printStackTrace();
			TestcaseHelper.fail("Remove threw wrong Exception for empty tree!");
		}

		try {
			bst.insert(5);
			bst.insert(9);
			bst.insert(3);
			bst.insert(1);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(7);
			TestcaseHelper.fail("Remove threw no Exception for non-existing element!");
		} catch (NoSuchElementException e) {
			// success
		} catch (Exception e) {
			TestcaseHelper.fail("Remove threw wrong Exception for non-existing element!");
		}

		// no successor
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(5);
		} catch (Exception e) {
			e.printStackTrace();
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(5), "Element exists after remove");

		// one successor
		// successor left
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
			bst.insert(1);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(5);
		} catch (Exception e) {
			e.printStackTrace();
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(5), "Element exists after remove!");
		TestcaseHelper.assertEquals(true, bst.exists(1), "Element no longer exists after unrelated remove!");

		// successor right
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
			bst.insert(8);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(5);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(5), "Element exists after remove!");
		TestcaseHelper.assertEquals(true, bst.exists(8), "Element no longer exists after unrelated remove!");

		// two successors
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
			bst.insert(1);
			bst.insert(8);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(5);
		} catch (Exception e) {
			e.printStackTrace();
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(5), "Element exists after remove!");
		TestcaseHelper.assertEquals(true, bst.exists(8), "Element no longer exists after unrelated remove!");
		TestcaseHelper.assertEquals(true, bst.exists(1), "Element no longer exists after unrelated remove!");

		// left node of parent, no right node
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
			bst.insert(1);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(1);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(1), "Element exists after remove");
		TestcaseHelper.assertEquals(true, bst.exists(5), "Element no longer exists after unrelated remove!");

		// left node of parent, right node exists
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
			bst.insert(1);
			bst.insert(8);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(1);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(1), "Element exists after remove");
		TestcaseHelper.assertEquals(true, bst.exists(5), "Element no longer exists after unrelated remove!");
		TestcaseHelper.assertEquals(true, bst.exists(8), "Element no longer exists after unrelated remove!");

		// right node of parent, no left node
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
			bst.insert(8);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(8);
		} catch (Exception e) {
			e.printStackTrace();
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(8), "Element exists after remove");
		TestcaseHelper.assertEquals(true, bst.exists(5), "Element no longer exists after unrelated remove!");

		// right node of parent, left node exists
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
			bst.insert(1);
			bst.insert(8);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(8);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(8), "Element exists after remove");
		TestcaseHelper.assertEquals(true, bst.exists(5), "Element no longer exists after unrelated remove!");
		TestcaseHelper.assertEquals(true, bst.exists(1), "Element no longer exists after unrelated remove!");

		// left node of parent, successor left
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
			bst.insert(3);
			bst.insert(1);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(3);
		} catch (Exception e) {
			e.printStackTrace();
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(3), "Element exists after remove!");
		TestcaseHelper.assertEquals(true, bst.exists(5), "Element no longer exists after unrelated remove!");
		TestcaseHelper.assertEquals(true, bst.exists(1), "Element no longer exists after unrelated remove!");

		// left node of parent, successor right
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
			bst.insert(3);
			bst.insert(4);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(3);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(3), "Element exists after remove!");
		TestcaseHelper.assertEquals(true, bst.exists(5), "Element no longer exists after unrelated remove!");
		TestcaseHelper.assertEquals(true, bst.exists(4), "Element no longer exists after unrelated remove!");

		// right node of parent, successor left
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
			bst.insert(8);
			bst.insert(7);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(8);
		} catch (Exception e) {
			e.printStackTrace();
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(8), "Element exists after remove!");
		TestcaseHelper.assertEquals(true, bst.exists(5), "Element no longer exists after unrelated remove!");
		TestcaseHelper.assertEquals(true, bst.exists(7), "Element no longer exists after unrelated remove!");

		// right node of parent, successor right
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
			bst.insert(8);
			bst.insert(10);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(8);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(8), "Element exists after remove!");
		TestcaseHelper.assertEquals(true, bst.exists(5), "Element no longer exists after unrelated remove!");
		TestcaseHelper.assertEquals(true, bst.exists(10), "Element no longer exists after unrelated remove!");

		// left node of parent
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
			bst.insert(3);
			bst.insert(1);
			bst.insert(4);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(3);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(3), "Element exists after remove!");
		TestcaseHelper.assertEquals(true, bst.exists(5), "Element no longer exists after unrelated remove!");
		TestcaseHelper.assertEquals(true, bst.exists(1), "Element no longer exists after unrelated remove!");
		TestcaseHelper.assertEquals(true, bst.exists(4), "Element no longer exists after unrelated remove!");

		// right node of parent
		bst = new BinarySearchTree();
		try {
			bst.insert(5);
			bst.insert(8);
			bst.insert(6);
			bst.insert(10);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(8);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(8), "Element exists after remove!");
		TestcaseHelper.assertEquals(true, bst.exists(5), "Element no longer exists after unrelated remove!");
		TestcaseHelper.assertEquals(true, bst.exists(6), "Element no longer exists after unrelated remove!");
		TestcaseHelper.assertEquals(true, bst.exists(10), "Element no longer exists after unrelated remove!");

		// left node of parent
		bst = new BinarySearchTree();
		try {
			bst.insert(15);
			bst.insert(5); // will be removed
			// right child tree from 5, create path to the left
			bst.insert(10);
			bst.insert(9);
			bst.insert(8);
			bst.insert(7);
			bst.insert(6);
			// left child tree from 5, create path to the right
			bst.insert(1);
			bst.insert(2);
			bst.insert(3);
			bst.insert(4);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception during insertion!");
		}

		try {
			bst.remove(5);
		} catch (Exception e) {
			TestcaseHelper.fail("There has been an unexpected Exception when removing root!");
		}

		TestcaseHelper.assertEquals(false, bst.exists(5), "Element exists after remove!");
		TestcaseHelper.assertArrayEquals(new boolean[] { true, true, true, true, true, true, true, true, true, true },
				new boolean[] { bst.exists(1), bst.exists(2), bst.exists(3), bst.exists(4), bst.exists(6),
						bst.exists(7), bst.exists(8), bst.exists(9), bst.exists(10), bst.exists(15) },
				"Element no longer exists after unrelated remove");
	}

	@Test(timeout = 500)
	@Points(exID = "BinarySearchTreeTests", bonus = 1, comment = "inOrderList")
	public void pubTest__bst_inOrderList() {
		int[] values = createRandomArray(20);
		BinarySearchTreeRef treeRef = new BinarySearchTreeRef();
		BinarySearchTree tree = new BinarySearchTree();

		for (int value : values) {
			try {
				treeRef.insert(value);
				tree.insert(value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		TestcaseHelper.assertEquals(treeRef.inOrderList(), tree.inOrderList(), "Inorder traversal wrong!");
	}

	@Test(timeout = 500)
	@Points(exID = "BinarySearchTreeTests", bonus = 1, comment = "preOrderList")
	public void pubTest__bst_preOrderList() {
		int[] values = createRandomArray(20);
		BinarySearchTreeRef treeRef = new BinarySearchTreeRef();
		BinarySearchTree tree = new BinarySearchTree();

		for (int value : values) {
			try {
				treeRef.insert(value);
				tree.insert(value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		TestcaseHelper.assertEquals(treeRef.preOrderList(), tree.preOrderList(), "Preorder traversal wrong!");
	}

	@Test(timeout = 500)
	@Points(exID = "BinarySearchTreeTests", bonus = 1, comment = "postOrderList")
	public void pubTest__bst_postOrderList() {
		int[] values = createRandomArray(20);
		BinarySearchTreeRef treeRef = new BinarySearchTreeRef();
		BinarySearchTree tree = new BinarySearchTree();

		for (int value : values) {
			try {
				treeRef.insert(value);
				tree.insert(value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		TestcaseHelper.assertEquals(treeRef.postOrderList(), tree.postOrderList(), "Postorder traversal wrong!");
	}

	@Test(timeout = 500)
	@Points(exID = "BinarySearchTreeTests", bonus = 2, comment = "main-Methode")
	public void pubTest__bst() {
		fail("Wird manuell geprueft (2 Punkte)");
	}

	public class BinarySearchTreeRef implements BSTInterface {

		private class TreeNode {
			private int value;
			private TreeNode left;
			private TreeNode right;
			private TreeNode parent;

			public TreeNode(int value) {
				this.value = value;
			}
		}

		private TreeNode root;

		@Override
		public void clear() {
			root = null;
		}

		@Override
		public boolean exists(int value) {
			return find(root, value) != null;
		}

		private TreeNode find(TreeNode node, int value) {

			if (node == null) {
				return null;
			}

			if (node.value == value) {
				return node;
			}

			TreeNode nextChild = (value > node.value) ? node.right : node.left;

			return find(nextChild, value);
		}

		@Override
		public void insert(int value) throws ElementExistsException {

			if (root == null) {
				root = new TreeNode(value);
				return;
			}

			insert(root, value);
		}

		private void insert(TreeNode node, int value) throws ElementExistsException {

			if (node.value == value) {
				//throw new ElementExistsException(value);
				return;
			}

			TreeNode child = (value > node.value) ? node.right : node.left;

			if (child != null) {
				insert(child, value);
				return;
			}

			TreeNode newNode = new TreeNode(value);
			newNode.parent = node;

			if (value > node.value) {
				node.right = newNode;
			} else {
				node.left = newNode;
			}
		}

		@Override
		public void remove(int value) throws NoSuchElementException {

			TreeNode toRem = find(root, value);

			if (toRem == null) {
				throw new NoSuchElementException("Value " + value + " could not be be removed (wasn't found).");
			}

			remove(toRem);
		}

		private void remove(TreeNode node) {
			switch (numChildren(node)) {
			case 0:
				replaceInParent(node, null);
				break;
			case 1:
				TreeNode child = (node.left != null) ? node.left : node.right;
				replaceInParent(node, child);
				child.parent = node.parent;
				break;
			case 2:
				TreeNode closest = node.left;
				while (closest.right != null) {
					closest = closest.right;
				}

				node.value = closest.value;
				remove(closest);
				break;
			}
		}

		private int numChildren(TreeNode node) {
			boolean hasLeft = node.left != null;
			boolean hasRight = node.right != null;

			if (hasLeft && hasRight) {
				return 2;
			} else if (hasLeft || hasRight) {
				return 1;
			}
			return 0;
		}

		/**
		 * Replaces the reference to node in its parent by newNode. If node is the root,
		 * root is set to newNode instead.
		 * 
		 * @param node
		 * @param newNode
		 */
		private void replaceInParent(TreeNode node, TreeNode newNode) {
			if (node == root) {
				root = newNode;
				return;
			}

			if (node.parent.left == node) {
				node.parent.left = newNode;
			} else {
				node.parent.right = newNode;
			}
		}

		@Override
		public List<Integer> inOrderList() {
			List<Integer> list = new ArrayList<>();
			inOrderList(root, list);
			return list;
		}

		private void inOrderList(TreeNode node, List<Integer> list) {
			if (node == null) {
				return;
			}

			inOrderList(node.left, list);
			list.add(node.value);
			inOrderList(node.right, list);
		}

		@Override
		public List<Integer> preOrderList() {
			List<Integer> list = new ArrayList<>();
			preOrderList(root, list);
			return list;
		}

		private void preOrderList(TreeNode node, List<Integer> list) {
			if (node == null) {
				return;
			}

			list.add(node.value);
			preOrderList(node.left, list);
			preOrderList(node.right, list);
		}

		@Override
		public List<Integer> postOrderList() {
			List<Integer> list = new ArrayList<>();
			postOrderList(root, list);
			return list;
		}

		private void postOrderList(TreeNode node, List<Integer> list) {
			if (node == null) {
				return;
			}

			postOrderList(node.left, list);
			postOrderList(node.right, list);
			list.add(node.value);
		}

	}
}
