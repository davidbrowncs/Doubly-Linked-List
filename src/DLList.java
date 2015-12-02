import java.io.Serializable;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;

/**
 * Copyright (C) 2015 David Brown. Permission is granted to copy, distribute
 * and/or modify this document under the terms of the GNU Free Documentation
 * License, Version 1.3 or any later version published by the Free Software
 * Foundation; with no Invariant Sections, no Front-Cover Texts, and no
 * Back-Cover Texts. A copy of the license is included in the section entitled
 * "GNU Free Documentation License".
 *
 * Implementation of a doubly linked list. Time complexities for operations are
 * as expected by a linked list, except when adding an element, this is constant
 * time, and getting the size of the list is constant time. Returning an element
 * from a given index is also slightly better than a traditional linked list,
 * where it is proportional to n/2, with n being the number of elements in the
 * list.
 *
 * Implemented using the notion of a "dummy" element, to point to the first and
 * last elements in the list, and also to remove the need for checking for null
 * nodes or elements.
 *
 * @author David Brown
 *
 * @param <T>
 *            Type of object to be stored.
 */
public class DLList<T> implements List<T>, Queue<T>, Cloneable, Serializable {

	/**
	 * Generated {@code serialVersionUID}
	 */
	private static final long serialVersionUID = -4402595667489128874L;

	/**
	 * Number of modifications made to the list. Only incremented when the
	 * elements in the list are changed, and not when simply reading from the
	 * list.
	 */
	private int modCount = 0;

	/**
	 * Current size of the list. (Number of elements)
	 */
	private int size = 0;

	/**
	 * The special "dummy" element, where its contents is always null, and its
	 * next and previous fields point to the start and end of the list
	 * respectively.
	 */
	private Elem dummy;

	/**
	 * Default initialisation of the Linked list.
	 */
	public DLList() {
		dummy = new Elem(null);
		dummy.prev = dummy;
		dummy.next = dummy;
	}

	/**
	 * Create a linked list with the elements from the given collection.
	 * Collection must not be null. Throws a {@code NullPointerException} if the
	 * given collection is null.
	 *
	 * @param c
	 *            The collection from which to copy elements from initially.
	 */
	public DLList(Collection<? extends T> c) {
		this();
		addAll(c);
	}

	/**
	 * Class which implements the structure of a linked list. Has pointers to
	 * the next and previous element in the list, and also holds an object which
	 * is of the type of object being stored by this list.
	 *
	 * @author David
	 *
	 */
	private class Elem {

		/**
		 * Actual data being stored by the list.
		 */
		private T content;

		/**
		 * Pointer to the next element in the list, or to the dummy element if
		 * there are no further elements.
		 */
		private Elem next;

		/**
		 * Pointer to the previous element in the list, or the dummy element if
		 * there are no previous elements.
		 */
		private Elem prev;

		/**
		 * Initialise the element with the given pointers.
		 *
		 * @param c
		 *            The object which is to be stored in the list.
		 * @param n
		 *            The successor of this element.
		 * @param p
		 *            The element preceding this element.
		 */
		private Elem(T c, Elem n, Elem p) {
			this.content = c;
			next = n;
			prev = p;
		}

		/**
		 * Initialise an element without specifying its pointers. The next and
		 * previous fields are initialised to the dummy element.
		 *
		 * @param c
		 *            The object to be stored in this element.
		 */
		private Elem(T c) {
			this.content = c;
			next = dummy;
			prev = dummy;
		}

		/**
		 * Specialised hashcode function to ensure that if the dummy element is
		 * the caller (gone over the end of the list, or gone before the start
		 * of the list), then we return immediately. This is since the hashcode
		 * is recursively calculated from the dummy element.
		 *
		 * @param caller
		 *            The parent of this element, this element is referenced in
		 *            the parent's by either the {@code next} or {@code prev}
		 *            field.
		 * @return The resulting hashcode of this element.
		 */
		private int innerHashCode(Elem caller) {
			if (caller == dummy) {
				return 1;
			} else {
				return hashCode(); // Need to check if the caller is the dummy,
									// since dummy is always the first element
									// processed by the list's hashcode function
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + next.innerHashCode(this);
			result = prime * result + prev.innerHashCode(this);
			result = prime * result + (content == null ? 1 : content.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof DLList.Elem)) {
				return false;
			}
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Elem other = (DLList.Elem) obj;
			if (content == null) {
				if (other.content != null) {
					return false;
				}
			} else if (!content.equals(other.content)) {
				return false;
			}
			if (next == null) {
				if (other.next != null) {
					return false;
				}
			} else if (!next.equals(other.next)) {
				return false;
			}
			if (prev == null) {
				if (other.prev != null) {
					return false;
				}
			} else if (!prev.equals(other.prev)) {
				return false;
			}
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#size()
	 */
	@Override
	public int size() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		if (o == null) {
			for (Elem e = dummy.next; e != dummy; e = e.next) {
				if (e.content == null) {
					return true;
				}
			}
		} else {
			for (Elem e = dummy.next; e != dummy; e = e.next) {
				if (o.equals(e.content)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Class for implementing both the {code Iterator} and {code ListIterator}
	 * interfaces. Behaves as specified by both interfaces.
	 */
	private class It implements Iterator<T>, ListIterator<T> {

		/**
		 * The expected modification count. Used to check if the underlying list
		 * has been modified by anything except this iterator's methods after
		 * creating this iterator.
		 */
		private int xp = modCount;

		/**
		 * Initialise the current element of this iterator to the head of the
		 * list. The current element always references the next element to be
		 * processed by the {@code next()} method.
		 */
		private Elem cur = dummy.next;

		/**
		 * The element that was last returned either by {@code next()} or
		 * {@code previous()}. If it is null, this indicates that either next or
		 * previous have not been called yet, or {@code add} or {@code remove}
		 * have been called since the last call to {@code next} or
		 * {@code previous}
		 */
		private Elem lr = null;

		/**
		 * Pointer to the current index in the list this iterator is pointing
		 * to. Initialised to the head of the list.
		 */
		private int p = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ListIterator#hasPrevious()
		 */
		@Override
		public boolean hasPrevious() {
			return p - 1 != -1;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.ListIterator#previous()
		 */
		@Override
		public T previous() {
			checkModification();
			if (!hasPrevious()) {
				throw new NoSuchElementException();
			}
			T o = cur.prev.content;
			lr = cur.prev;
			cur = lr;
			p--;
			return o;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ListIterator#nextIndex()
		 */
		@Override
		public int nextIndex() {
			return p;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ListIterator#previousIndex()
		 */
		@Override
		public int previousIndex() {
			return p - 1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			checkModification();
			if (lr == null) {
				throw new IllegalStateException();
			}
			lr.next.prev = lr.prev;
			lr.prev.next = lr.next;
			lr = null;
			size--;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.ListIterator#set(java.lang.Object)
		 */
		@Override
		public void set(T e) {
			checkModification();
			if (lr == null) {
				throw new IllegalStateException();
			}
			lr.content = e;
			lr = null;
		}

		/**
		 * Checks if the underlying linked list has been modified by anything
		 * other than this iterator's methods. Throws a
		 * {@code ConcurrentModificationException} if the underlying list has
		 * been modified by anything other than this iterator's methods since
		 * this iterator was created.
		 */
		private void checkModification() {
			if (xp != modCount) {
				throw new ConcurrentModificationException();
			}
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.ListIterator#add(java.lang.Object)
		 */
		@Override
		public void add(T e) {
			checkModification();
			Elem a = new Elem(e);
			cur.prev.next = a;
			a.prev = cur.prev;
			cur.prev = a;
			a.next = cur;
			lr = null;
			size++;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return p != size;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Iterator#next()
		 */
		@Override
		public T next() {
			checkModification();
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			T o = cur.content;
			lr = cur;
			cur = cur.next;
			p++;
			return o;
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new It();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#listIterator()
	 */
	@Override
	public ListIterator<T> listIterator() {
		return new It();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#toArray()
	 */
	@Override
	public T[] toArray() {
		@SuppressWarnings("unchecked")
		T[] r = (T[]) new Object[size];
		Elem e = dummy.next;
		for (int i = 0; i < size; i++, e = e.next) {
			r[i] = e.content;
		}
		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#toArray(java.lang.Object[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <E> E[] toArray(E[] a) {
		Objects.requireNonNull(a);
		if (size > a.length) {
			a = (E[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
		}
		int elem = 0;
		for (Iterator<T> it = iterator(); it.hasNext(); elem++) {
			a[elem] = (E) it.next();
		}
		if (a.length > size) {
			a[size] = null;
		}
		return a;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#add(java.lang.Object)
	 */
	@Override
	public boolean add(T o) {
		Elem e = new Elem(o);
		dummy.prev.next = e;
		e.prev = dummy.prev;
		dummy.prev = e;
		size++;
		modCount++;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		if (o == null) {
			for (Elem e = dummy.next; e != dummy; e = e.next) {
				if (e.content == null) {
					e.prev.next = e.next;
					e.next.prev = e.prev;
					e = null;
					size--;
					modCount++;
					return true;
				}
			}
		} else {
			for (Elem e = dummy.next; e != dummy; e = e.next) {
				if (o.equals(e.content)) {
					e.prev.next = e.next;
					e.next.prev = e.prev;
					e = null;
					size--;
					modCount++;
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		Objects.requireNonNull(c);
		if (c.size() == 0) {
			return true;
		}
		for (Elem e = dummy.next; e != dummy; e = e.next) {
			if (!c.contains(e.content)) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends T> c) {
		Objects.requireNonNull(c);
		int s = size;
		for (Iterator<? extends T> it = c.iterator(); it.hasNext();) {
			add(it.next());
		}
		return s != size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		Objects.requireNonNull(c);
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		int sz = size;
		Elem e = dummy.next;

		// Get the element referencer (e) to the correct location first
		for (int cn = 0; cn < index; cn++, e = e.next) {}

		// Now iterate through all the given collection's elements and add them
		for (Iterator<? extends T> it = c.iterator(); it.hasNext();) {
			Elem a = new Elem(it.next());
			e.prev.next = a;
			a.prev = e.prev;
			e.prev = a;
			a.next = e;
			e = a.next;
			size++;
			modCount++;
		}
		return sz != size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		return compareRemove(c, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.List#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		return compareRemove(c, false);
	}

	/**
	 * Used by {@code retainAll} and {@code removeAll} to compare all the
	 * elements in this list against the given collection, and remove them where
	 * necessary
	 *
	 * @param c
	 *            The collection from which to compare this list's elements
	 *            against
	 * @param mod
	 *            The flag to indicate whether to remove elements that are in
	 *            the specified collection, or to keep elements that are in the
	 *            specified collection
	 * @return Returns true if this list was modified as a result of calling
	 *         this method.
	 */
	private boolean compareRemove(Collection<?> c, boolean mod) {
		Objects.requireNonNull(c);
		int sz = size;
		for (Elem e = dummy.next; e != dummy; e = e.next) {
			if (c.contains(e.content) == mod) {
				e.prev.next = e.next;
				e.next.prev = e.prev;
				size--;
				modCount++;
			}
		}
		return size != sz;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#clear()
	 */
	@Override
	public void clear() {
		dummy.next = dummy;
		dummy.prev = dummy;
		size = 0;
		modCount++;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#get(int)
	 */
	@Override
	public T get(int index) {
		assertIndex(index);
		if (index < size / 2) {
			Elem e = dummy.next;
			for (int i = 0; i < index; i++, e = e.next) {}
			return e.content;
		} else {
			Elem e = dummy.prev;
			for (int i = size - 1; i > index; e = e.prev, i--) {}
			return e.content;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	@Override
	public T set(int index, T element) {
		assertIndex(index);
		int c = 0;
		Elem t = dummy.next;
		while (c < index) {
			t = t.next;
			c++;
		}
		T elem = t.content;
		t.content = element;
		modCount++;
		return elem;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, T element) {
		assertIndex(index);
		Elem n = new Elem(element, null, null);
		Elem a = dummy.next;
		for (int i = 0; i < index; i++) {
			a = a.next;
		}
		n.prev = a.prev;
		a.prev.next = n;
		a.prev = n;
		n.next = a;
		size++;
		modCount++;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#remove(int)
	 */
	@Override
	public T remove(int index) {
		assertIndex(index);
		Elem a = dummy.next;
		for (int i = 0; i < index; i++) {
			a = a.next;
		}
		a.prev.next = a.next;
		a.next.prev = a.prev;
		T o = a.content;
		a = null;
		size--;
		modCount++;
		return o;
	}

	/**
	 * Checks if the given index is a valid index for an element currently in
	 * this list. I.e, valid index range is 0 or greater, up to but not
	 * including the size of the list. Throws an
	 * {@code IndexOutOfBoundsException} if the given index is not a valid index
	 * for an element currently in the list.
	 *
	 * @param i
	 *            Index to check
	 */
	private void assertIndex(int i) {
		if (i < 0 || i >= size) {
			throw new IndexOutOfBoundsException();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object o) {
		int c = 0;
		if (o == null) {
			for (Elem a = dummy.next; a != dummy; a = a.next, c++) {
				if (a.content == null) {
					return c;
				}
			}
		} else {
			for (Elem a = dummy.next; a != dummy; a = a.next, c++) {
				if (o.equals(a.content)) {
					return c;
				}
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object o) {
		int c = 0, ret = -1;
		if (o == null) {
			for (Elem a = dummy.next; a != dummy; a = a.next, c++) {
				if (a.content == null) {
					ret = c;
				}
			}
		} else {
			for (Elem a = dummy.next; a != dummy; a = a.next, c++) {
				if (o.equals(a.content)) {
					ret = c;
				}
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<T> listIterator(int index) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		ListIterator<T> it = listIterator();
		for (int i = 0; i < index; i++, it.next()) {}
		return it;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.List#subList(int, int)
	 */
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		assertIndex(fromIndex);
		assertIndex(toIndex);
		if (toIndex < fromIndex) {
			throw new IllegalArgumentException();
		}
		List<T> l = new DLList<>();
		Elem e = dummy.next;
		for (int i = 0; i < fromIndex; i++) {
			e = e.next;
		}
		for (int i = fromIndex; i < toIndex; i++) {
			l.add(e.content);
			e = e.next;
		}
		return l;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Queue#offer(java.lang.Object)
	 */
	@Override
	public boolean offer(T e) {
		return add(e);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Queue#remove()
	 */
	@Override
	public T remove() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		return remove(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Queue#poll()
	 */
	@Override
	public T poll() {
		return size == 0 ? null : remove(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Queue#element()
	 */
	@Override
	public T element() {
		if (size == 0) {
			throw new NoSuchElementException();
		}
		return dummy.next.content;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.Queue#peek()
	 */
	@Override
	public T peek() {
		return dummy.next.content; // If the list is empty, this will return the
									// dummy's content which is null
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DLList<T> clone() {
		DLList<T> t;
		try {
			t = (DLList<T>) super.clone();
			return t;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dummy.hashCode();
		result = prime * result + modCount;
		result = prime * result + size;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DLList)) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		DLList other = (DLList) obj;
		if (dummy == null) {
			if (other.dummy != null) {
				return false;
			}
		} else if (!dummy.equals(other.dummy)) {
			return false;
		}
		if (modCount != other.modCount) {
			return false;
		}
		if (size != other.size) {
			return false;
		}
		return true;
	}
}
