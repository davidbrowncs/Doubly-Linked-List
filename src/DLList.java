import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class DLList<T> implements List<T> {

	private int modCount = 0;
	private int size = 0;
	private Elem dummy;

	public DLList() {
		dummy = new Elem(null, null, null);
		dummy.prev = dummy;
		dummy.next = dummy;
	}

	public DLList(Collection<? extends T> c) {
		this();
		addAll(c);
	}

	private class Elem {

		private T content;

		private Elem next;

		private Elem prev;

		private Elem(T c, Elem n, Elem p) {
			this.content = c;
			next = n;
			prev = p;
		}

		private Elem(T c) {
			this.content = c;
			next = dummy;
			prev = dummy;
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

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

	private class It implements Iterator<T>, ListIterator<T> {

		private int xp = modCount;

		private Elem cur = dummy.next;

		private Elem lr = null;

		private int p = 0;

		@Override
		public boolean hasPrevious() {
			return p - 1 != -1;
		}

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

		@Override
		public int nextIndex() {
			return p;
		}

		@Override
		public int previousIndex() {
			return p - 1;
		}

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

		@Override
		public void set(T e) {
			checkModification();
			if (lr == null) {
				throw new IllegalStateException();
			}
			lr.content = e;
			lr = null;
		}

		private void checkModification() {
			if (xp != modCount) {
				throw new ConcurrentModificationException();
			}
		}

		@Override
		public void add(T e) {
			checkModification();
			Elem a = new Elem(e);
			cur.prev.next = a;
			a.prev = cur.prev;
			cur.prev = a;
			a.next = cur;
			size++;
		}

		@Override
		public boolean hasNext() {
			return p != size;
		}

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

	@Override
	public Iterator<T> iterator() {
		return new It();
	}

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

	@SuppressWarnings("unchecked")
	@Override
	public <E> E[] toArray(E[] a) {
		if (a == null) {
			throw new NullPointerException();
		}
		if (size > a.length) {
			a = (E[]) new Object[size];
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

	@Override
	public boolean addAll(Collection<? extends T> c) {
		Objects.requireNonNull(c);
		int s = size;
		for (Iterator<? extends T> it = c.iterator(); it.hasNext();) {
			add(it.next());
		}
		return s != size;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		Objects.requireNonNull(c);
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		int sz = size;
		Elem e = dummy.next;
		for (int cn = 0; cn < index; cn++, e = e.next) {}
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

	@Override
	public boolean removeAll(Collection<?> c) {
		return compareRemove(c, true);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return compareRemove(c, false);
	}

	private boolean compareRemove(Collection<?> c, boolean mod) {
		Objects.requireNonNull(c);
		int sz = size;
		Elem e = dummy.next;
		for (int i = 0; i < sz; i++) {
			if (c.contains(e.content) == mod) {
				e.prev.next = e.next;
				e.next.prev = e.prev;
				size--;
				modCount++;
			}
			e = e.next;
		}
		return size != sz;
	}

	@Override
	public void clear() {
		dummy.next = dummy;
		dummy.prev = dummy;
		size = 0;
		modCount++;
	}

	@Override
	public T get(int index) {
		assertIndex(index);
		int c = 0;
		for (Iterator<T> it = iterator(); it.hasNext(); c++) {
			T o = it.next();
			if (c == index) {
				return o;
			}
		}
		return null; // Either index out of bounds, or we return the element
	}

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
		return elem;
	}

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

	private void assertIndex(int i) {
		if (i < 0 || i >= size) {
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public int indexOf(Object o) {
		int c = 0;
		if (o == null) {
			for (Iterator<T> i = iterator(); i.hasNext(); c++) {
				T next = i.next();
				if (next == null) {
					return c;
				}
			}
		} else {
			for (Iterator<T> i = iterator(); i.hasNext(); c++) {
				T next = i.next();
				if (o.equals(next)) {
					return c;
				}
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		int c = 0, ret = -1;
		if (o == null) {
			for (Iterator<T> t = iterator(); t.hasNext(); c++) {
				if (t.next() == o) {
					ret = c;
				}
			}
		} else {
			for (Iterator<T> t = iterator(); t.hasNext(); c++) {
				if (o.equals(t.next())) {
					ret = c;
				}
			}
		}
		return ret;
	}

	@Override
	public ListIterator<T> listIterator() {
		return new It();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		}
		ListIterator<T> it = (ListIterator<T>) iterator();
		for (int i = 0; i < index; i++) {
			it.next();
		}
		return it;
	}

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
}
