import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

public class TestCase {

	LinkedList<Integer> test;
	DLList<Integer> list;

	@Before
	public void setup() {
		test = new LinkedList<>();
		list = new DLList<>();
	}

	@Test
	public void testAdd() {
		test.add(1);
		list.add(1);
		assertEquals(test.size(), list.size());

		test.add(2);
		list.add(2);
		assertEquals(test.size(), list.size());
	}

	@Test
	public void testClear() {
		test.add(1);
		list.add(1);
		assertEquals(test.size(), list.size());

		test.clear();
		list.clear();
		assertTrue(list.isEmpty());
		assertEquals(test.size(), list.size());

		setup();
		for (int i = 0; i < 10000; i++) {
			test.add(i);
			list.add(i);
		}

		test.clear();
		list.clear();
		assertTrue(list.isEmpty());
		assertEquals(test.size(), list.size());
	}

	@Test
	public void testIterator() {
		for (int i = 0; i < 10; i++) {
			list.add(i);
		}

		Iterator<Integer> it = list.iterator();
		for (int i = 0; i < 10; i++) {
			assertTrue(it.hasNext());
			assertEquals(i, (int) it.next());
		}
		assertFalse(it.hasNext());
	}

	@Test
	public void testRemove() {
		list.add(1);
		assertTrue(list.contains(1));
		list.remove(0);
		assertFalse(list.contains(1));
		for (int i = 0; i < 5; i++) {
			list.add(i);
			test.add(i);
		}
		list.remove(3);
		test.remove(3);
		assertEquals(4, list.size());
		Object[] ar = test.toArray();
		Object[] arr = list.toArray();
		assertTrue(Arrays.equals(ar, arr));
	}

	@Test
	public void testItRemove() {
		list.add(1);
		Iterator<Integer> it = list.iterator();
		it.next();
		it.remove();
		assertTrue(list.isEmpty());
		assertEquals(0, list.size());
	}

	@Test
	public void testContains() {
		list.add(1);
		assertTrue(list.contains(1));

		for (int i = 1; i < 50; i++) {
			list.add(i);
		}

		assertTrue(list.contains(30));
		assertFalse(list.contains(50));

		setup();
		for (int i = 0; i < 10000; i++) {
			list.add(i);
			assertTrue(list.contains(i));
		}
	}

	@Test
	public void testIndexOf() {
		list.add(0);
		assertEquals(0, list.indexOf(0));

		list.add(1);
		assertEquals(0, list.indexOf(0));
		assertEquals(1, list.indexOf(1));

		for (int i = 2; i < 30; i++) {
			list.add(i);
		}

		assertEquals(30, list.size());
		for (int i = 0; i < 30; i++) {
			assertEquals(i, list.indexOf(i));
		}

		setup();

		for (int i = 0; i < 1000; i++) {
			test.add(i);
			list.add(i);
		}

		assertEquals(test.indexOf(30), list.indexOf(30));
		assertEquals(-1, list.indexOf(1000));
	}

	@Test
	public void testToArray() {
		for (int i = 0; i < 5; i++) {
			test.add(i);
			list.add(i);
		}

		Object[] arr = list.toArray();
		Object[] arr2 = test.toArray();
		assertTrue(Arrays.equals(arr2, arr));
	}

	@Test
	public void testSet() {
		list.add(0);
		list.add(1);
		list.add(2);

		list.set(1, 4);
		assertEquals(1, list.indexOf(4));

		Integer[] a = new Integer[] { 0, 4, 2 };
		assertTrue(Arrays.equals(a, list.toArray()));
	}

	@Test
	public void testIteratorAdd() {
		ListIterator<Integer> it = list.listIterator();
		it.add(1);
		assertEquals(1, list.size());
		it.add(2);
		assertEquals(2, list.size());
		it.add(3);
		assertEquals(3, list.size());
		it.add(4);
		assertEquals(4, list.size());

		Integer[] a = new Integer[] { 1, 2, 3, 4 };
		Object[] arr = list.toArray();
		assertTrue(Arrays.equals(a, arr));
	}

	@Test
	public void testSubList() {
		for (int i = 0; i < 100; i++) {
			test.add(i);
			list.add(i);
		}
		Object[] a = test.subList(3, 40).toArray();
		Object[] b = list.subList(3, 40).toArray();

		assertTrue(Arrays.equals(a, b));
	}

	@Test
	public void testReverseIterator() {
		for (int i = 0; i < 40; i++) {
			test.add(i);
			list.add(i);
		}
		ListIterator<Integer> i = test.listIterator(40);
		ListIterator<Integer> it = list.listIterator(40);

		assertEquals(i.hasNext(), it.hasNext());

		while (i.hasPrevious()) {
			assertEquals(i.previous(), it.previous());
			assertEquals(i.nextIndex(), it.nextIndex());
			assertEquals(i.previousIndex(), it.previousIndex());
		}
		assertEquals(i.hasPrevious(), it.hasPrevious());
	}

	@Test(expected = NullPointerException.class)
	public void testNullAddAll() {
		list.addAll(null);
	}

	@Test(expected = NullPointerException.class)
	public void testNullRetainAll() {
		list.retainAll(null);
	}

	@Test(expected = NullPointerException.class)
	public void testNullRemoveAll() {
		list.removeAll(null);
	}

	@Test
	public void testAddAll() {
		List<Integer> toAdd = new ArrayList<>();
		for (int i = 0; i < 40; i++) {
			test.add(i);
			list.add(i);
		}
		test.addAll(toAdd);
		list.addAll(toAdd);
		assertTrue(Arrays.equals(test.toArray(), list.toArray()));
	}

	@Test
	public void testRetainAll() {
		List<Integer> toR = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			test.add(i);
			list.add(i);
			if (i >= 30) {
				toR.add(i);
			}
		}
		test.retainAll(toR);
		list.retainAll(toR);
		Object[] a = test.toArray();
		Object[] b = list.toArray();
		assertTrue(Arrays.equals(a, b));
	}

	@Test
	public void testRemoveAll() {
		List<Integer> to = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			test.add(i);
			list.add(i);
			if (i >= 30 && i < 70) {
				to.add(i);
			}
		}
		test.removeAll(to);
		list.removeAll(to);
		assertArrayEquals(test.toArray(), list.toArray());
	}

	@Test
	public void testAddAllAtIndex() {
		List<Integer> t = new ArrayList<>();
		for (int i = 10; i < 20; i++) {
			t.add(i);
		}

		for (int i = 0; i < 10; i++) {
			test.add(i);
			list.add(i);
		}

		test.addAll(10, t);
		list.addAll(10, t);

		Object[] a = test.toArray();
		Object[] b = list.toArray();

		assertArrayEquals(a, b);
	}

	@Test
	public void testListIterator() {
		for (int i = 0; i < 100; i++) {
			test.add(i);
			list.add(i);
		}
		ListIterator<Integer> it = test.listIterator();
		ListIterator<Integer> ti = list.listIterator();

		while (it.hasNext()) {
			assertEquals(it.hasNext(), ti.hasNext());
			assertEquals(it.next(), ti.next());
			assertEquals(it.hasPrevious(), ti.hasPrevious());
			assertEquals(it.nextIndex(), ti.nextIndex());
			assertEquals(it.previousIndex(), ti.previousIndex());
		}

		while (it.hasPrevious()) {
			assertEquals(it.hasPrevious(), ti.hasPrevious());
			assertEquals(it.hasNext(), ti.hasNext());
			assertEquals(it.nextIndex(), ti.nextIndex());
			assertEquals(it.previousIndex(), ti.previousIndex());
			assertEquals(it.previous(), ti.previous());
		}

		it = test.listIterator();
		ti = list.listIterator();

		for (int i = 100; i < 200; i++) {
			it.next();
			ti.next();
			it.add(i);
			ti.add(i);
			assertEquals(test.size(), list.size());
			assertTrue(list.contains(i));
		}

		Object[] a = test.toArray();
		Object[] b = list.toArray();

		assertArrayEquals(a, b);

		setup();
	}

	@Test(expected = NoSuchElementException.class)
	public void testNoMoreElementsInIterator() {
		for (int i = 0; i < 100; i++) {
			list.add(i);
		}

		Iterator<Integer> ti = list.listIterator();

		while (ti.hasNext()) {
			ti.next();
		}

		ti.next();
	}

	@Test(expected = ConcurrentModificationException.class)
	public void testConcurrentModification() {
		list.add(1);
		Iterator<Integer> it = list.iterator();
		list.remove(0);
		it.next();
	}

	@Test
	public void testClone() {
		for (int i = 0; i < 100; i++) {
			list.add(i * 1000);
		}
		DLList<Integer> c = list.clone();
		assertFalse(c == list);
		assertTrue(c.equals(list));
	}

	@Test
	public void testIteratorSet() {
		for (int i = 0; i < 10; i++) {
			test.add(i);
			list.add(i);
		}

		ListIterator<Integer> it = test.listIterator();
		ListIterator<Integer> ti = list.listIterator();

		it.next();
		ti.next();
		it.set(4);
		ti.set(4);
		assertArrayEquals(test.toArray(), list.toArray());

		while (it.hasNext()) {
			it.next();
			ti.next();
		}

		it.previous();
		ti.previous();

		it.set(1000);
		ti.set(1000);

		assertArrayEquals(test.toArray(), list.toArray());
		assertEquals(test.size(), list.size());
	}

	@Test(expected = ArrayStoreException.class)
	public void testWrongArrayType() {
		list.add(1);
		list.toArray(new String[0]);
	}

	@Test(expected = NullPointerException.class)
	public void testNullConstructor() {
		list = new DLList<Integer>(null);
	}

	@Test
	public void testAddInConstructor() {
		List<Integer> ll = new LinkedList<>();
		for (int i = 0; i < 10; i++) {
			ll.add(i);
		}

		list = new DLList<>(ll);
		assertEquals(ll.size(), list.size());
		assertTrue(ll.containsAll(list) && list.containsAll(ll));
		assertFalse(list.isEmpty());
	}

	@Test
	public void testGetAtIndex() {
		Integer[] a = new Integer[500];
		for (int i = 0; i < 500; i++) {
			a[i] = 500 - i;
			list.add(i);
			test.add(i);
		}

		for (int i = 0; i < 500; i++) {
			assertEquals(test.get(i), list.get(i));
		}

		for (int i = 0; i < 500; i++) {
			int x = a[i];
			assertEquals(test.indexOf(x), list.indexOf(x));
		}
	}

	@Test
	public void testSetAtIndex() {
		for (int i = 0; i < 1000; i++) {
			test.add(i % 30);
			list.add(i % 30);
		}

		test.set(10, 5000);
		list.set(10, 5000);

		assertArrayEquals(test.toArray(), list.toArray());
	}

	@Test
	public void testAddAtIndex() {
		for (int i = 0; i < 10; i++) {
			test.add(i);
			list.add(i);
		}

		test.add(4, 420);
		list.add(4, 420);
		assertEquals(test.size(), list.size());
		assertArrayEquals(test.toArray(), list.toArray());
	}

	@Test
	public void testLastIndexOf() {
		for (int i = 0; i < 1000; i++) {
			test.add(i % 30);
			list.add(i % 30);
		}

		assertEquals(test.lastIndexOf(29), list.lastIndexOf(29));
	}

	@Test
	public void testEquals() {
		for (int i = 0; i < 50; i++) {
			list.add(i);
		}
		System.out.println(list.hashCode());
	}
}
