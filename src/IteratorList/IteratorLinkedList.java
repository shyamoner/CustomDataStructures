package IteratorList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import Node.Node;

/**
 * This class is used as both a LinkedList and an Iterator. The reason for this is to allow
 * a user to go back and forth between previous and next elements without the need to recreate 
 * an Iterator or ListIterator each time the list has been modified. In addition, it allows for
 * concurrent modification, such as removing elements while in an enhanced for-loop.
 * 
 * <p>This class can almost acts as a ListIterator, with the ability to move between previous and
 * next elements and modifying the list during the moves. Unlike an Iterator, the list can be 
 * modified whenever during an enhanced for-loop and the for-loop will still work.
 * 
 * <p>For example, {@code for(Integer i: list){if(i==2){list.remove();}}} will work
 * @author Ziwei Wu
 * @param <E> - The type of element the list will contain.
 */
public class IteratorLinkedList<E> implements IteratorList<E>{
	private int size = 0;
	private Node<E> current, head, tail, 
		headStart = new Node<E>(null), tailStart = new Node<E>(null);
	
	public IteratorLinkedList() {
		current = headStart;
	}
	
	@Override
	public void headStart() {
		current = headStart;
	}
	
	@Override
	public void tailStart() {
		current = tailStart;
	}
	
	@Override
	public void moveToIndex(int index) {
		if(index < 0 || index >= size) {
			String sizeS = Integer.toString(size);
			String indexS = Integer.toString(index);
			throw new IndexOutOfBoundsException("Size: " + sizeS + ", Index: " + indexS);
		}
		current = getNode(index);
	}
	
	@Override
	public boolean add(E e) {
		Node<E> temp = new Node<>(e);
		if(size == 0) {
			head = temp;
			headStart.setNext(head);
			tail = head;
			tailStart.setPrevious(tail);
		}
		else {
			tail.setNext(temp);
			temp.setPrevious(tail);
			tail = temp;
			tailStart.setPrevious(tail);
		}
		size++;
		return true;
	}

	@Override
	public void add(int index, E element) {
		if(index < 0 || index > size) {
			String sizeS = Integer.toString(size);
			String indexS = Integer.toString(index);
			throw new IndexOutOfBoundsException("Size: " + sizeS + ", Index: " + indexS);
		}
		Node<E> temp = new Node<>(element);
		
		if(size == 0) {
			add(element);
		}
		else if(index == size) {
			temp.setPrevious(tail);
			tail.setNext(temp);
			tail = temp;
			tailStart.setPrevious(tail);
		}
		else if(index == 0) {
			temp.setNext(head);
			head.setPrevious(temp);
			head = temp;
			headStart.setNext(head);
		}
		else {
			Node<E> tempNext = getNode(index);
			Node<E> tempPrev = tempNext.previous();
			tempPrev.setNext(temp);
			tempNext.setPrevious(temp);
			temp.setNext(tempNext);
			temp.setPrevious(tempPrev);
		}
		size++;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		if(c == null) throw new NullPointerException();
		for(E e: c) {
			add(e);
		}
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		if(c == null) throw new NullPointerException();
		if (index < 0 || index > size) {
			String sizeS = Integer.toString(size);
			String indexS = Integer.toString(index);
			throw new IndexOutOfBoundsException("Size: " + sizeS + ", Index: " + indexS);
		}
		for(E e: c) {
			add(index++, e);
		}
		return true;
	}

	@Override
	public void clear() {
		Node<E> tempCur = head;
		while(tempCur.hasNext()) {
			Node<E> next = tempCur.next();
			tempCur.clear();
			tempCur = next;
		}
		head = null;
		tail = null;
		headStart.setNext(head);
		tailStart.setPrevious(tail);
		current = headStart;
		size = 0;	
	}

	@Override
	public boolean contains(Object o) {
		return (indexOf(o) != -1) ? true:false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if(c==null) throw new NullPointerException();
		for(Object o: c) {
			if(!contains(o)) return false;
		}
		return true;
	}

	@Override
	public E get(int index) {
		if (index < 0 || index >= size) {
			String sizeS = Integer.toString(size);
			String indexS = Integer.toString(index);
			throw new IndexOutOfBoundsException("Size: " + sizeS + ", Index: " + indexS);
		}
		return getNode(index).get();
	}

	@Override
	public int indexOf(Object o) {
		int i = 0;
		for(E e: this) {
			boolean equals = (o == null)? e == null : e != null && e.equals(o);
			if(equals) return i;
			i++;
		}
		return -1;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Iterator<E> iterator() {
		current = headStart;
		return this;
	}

	@Override
	public int lastIndexOf(Object o) {
		int i = size -1;
		Node<E> tempCur = tail;
		while(i >= 0) {
			E e = tempCur.get();
			boolean equals = (o == null)? e == null : e != null && e.equals(o);
			if(equals) return i;
			i--;
			tempCur = tempCur.previous();
		}
		return -1;
	}

	@Override
	public ListIterator<E> listIterator() {
		return new ListIt<E>(0);
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		if(index < 0 || index > size()) {
			String sizeS = Integer.toString(size);
			String indexS = Integer.toString(index);
			throw new IndexOutOfBoundsException("Size: " + sizeS + ", Index: " + indexS);
		}
		return new ListIt<E>(index);
	}

	@Override
	public boolean remove(Object o) {
		for(E e: this) {
			boolean equals = (o == null)? e==null : e!=null && e.equals(o);
			if(equals) {
				remove();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void remove() {
		if(current.next() != head) {
			Node<E> tempCurrent = current;
			current = new Node<E>(null);
			current.setNext(tempCurrent.next());
			current.setPrevious(tempCurrent.previous());
			removeNode(tempCurrent);
		}
	}
	
	/**
	 * Used to cleanly remove a node. 
	 * @param node - the node to be removed
	 */
	private void removeNode(Node<E> node) {
		if(node == head && node == tail) {
			clear();
		}
		else if(node == head) {
			head = head.next();
			head.setPrevious(null);
			headStart.setNext(head);
			node.clear();
			size--;
		}
		else if(node == tail) {
			tail = tail.previous();
			tail.setNext(null);
			tailStart.setPrevious(tail);
			node.clear();
			size--;
		}
		else {
			Node<E> tmpNext = node.next();
			Node<E> tmpPrev = node.previous();
			tmpNext.setPrevious(tmpPrev);
			tmpPrev.setNext(tmpNext);
			node.clear();
			size--;
		}
	}

	@Override
	public E remove(int index) {
		if (index < 0 || index >= size) {
			String sizeS = Integer.toString(size);
			String indexS = Integer.toString(index);
			throw new IndexOutOfBoundsException("Size: " + sizeS + ", Index: " + indexS);
		}
		Node<E> tempCur = getNode(index);
		E e = tempCur.get();
		removeNode(tempCur);
		return e;
	}
	
	@Override
	public boolean removeAllInstances(Object o) {
		boolean contains = false;
		for(E e: this) {
			boolean equals = (o == null)? e==null : e!=null && e.equals(o);
			if(equals) {
				remove();
				contains = true;
			}
		}
		return contains;
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		if(c == null) throw new NullPointerException();
		boolean changed = false;
		for(Object o: c) {
			if(removeAllInstances(o) && !changed) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if(c == null) throw new NullPointerException();
		boolean changed = false;
		for(E e: this) {
			if(!c.contains(e)) {
				remove();
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public E set(int index, E element) {
		if(index < 0 || index >= size()) {
			String sizeS = Integer.toString(size);
			String indexS = Integer.toString(index);
			throw new IndexOutOfBoundsException("Size: " + sizeS + ", Index: " + indexS);
		}
		Node<E> tempCur = getNode(index);
		E tempE = tempCur.get();
		tempCur.set(element);
		return tempE;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		if(fromIndex < 0 || toIndex > size || fromIndex > toIndex) {
			throw new IndexOutOfBoundsException();
		}
		int i = 0;
		List<E> list = new IteratorLinkedList<>();
		for(E e: this) {
			if(i >= toIndex) {
				break;
			}
			if(i >= fromIndex) {
				list.add(e);
			}
			i++;
		}
		return list;
	}

	@Override
	public Object[] toArray() {
		Object[] arr = new Object[size];
		int i =0;
		for(E e: this) {
			arr[i++] = e;
		}
		return arr;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		if(a==null) throw new NullPointerException();
		if(a.length < size) {
			a = (T[])java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
		}
		if(a.length > size) {
			a[size] = null;
		}
		int i =0;
		for(E e: this) {
			a[i++] = (T) e;
		}
		return a;
	}

	@Override
	public boolean hasNext() {
		return current.hasNext();
	}

	@Override
	public E next() {
		current = current.next();
		return current.get();
	}
	
	@Override
	public boolean hasPrevious() {
		return current.hasPrevious();
	}
	
	@Override
	public E previous() {
		current = current.previous();
		return current.get();
	}
	
	/**
	 * Retrieves the node at the index.
	 * @param index - the index of where the node is
	 * @return A Node<E> at the index
	 */
	private Node<E> getNode(int index){
		int count = (index - 0 < size -1 - index)? 0:size-1;
		Node<E> tempCur = (count == 0) ? head:tail;
		int minus = (count == 0) ? 1:-1;
		while(count!=index) {
			tempCur = (minus == 1) ? tempCur.next() : tempCur.previous();
			count += minus;
		}
		return tempCur;
	}
	
	/**
	 * This class is completely implemented because it is needed for the Collections
	 * framework (such as {@code Collections.sort(List<T> list)}). The implementation
	 * was copied from the ListIteration implementation from OpenJDK's LinkedList.
	 * @author Ziwei Wu
	 * @param <T> - any object
	 */
	private class ListIt<T> implements ListIterator<E>{

		private int nextIndex;
		private Node<E> lastReturned;
		private Node<E> next;
		
		public ListIt(int index) {
			nextIndex = index;
			next = (index == size) ? null : getNode(index);
		}
		
		@Override
		public void add(E arg0) {
			lastReturned = null;
			if(next == null) IteratorLinkedList.this.add(arg0);
			else {
				IteratorLinkedList.this.add(nextIndex, arg0);
			}
			nextIndex++;
		}

		@Override
		public boolean hasNext() {
			return nextIndex < size;
		}

		@Override
		public boolean hasPrevious() {
			return nextIndex > 0;
		}

		@Override
		public E next() {
			if(!hasNext()) {
				throw new NoSuchElementException();
			}
			lastReturned = next;
			nextIndex++;
			next = next.next();
			return lastReturned.get();
		}

		@Override
		public int nextIndex() {
			return nextIndex;
		}

		@Override
		public E previous() {
			if(!hasPrevious()) {
				throw new NoSuchElementException();
			}
			lastReturned = next =(next == null) ? tail : next.previous();
			nextIndex--;
			return lastReturned.get();
		}

		@Override
		public int previousIndex() {
			return nextIndex - 1;
		}

		@Override
		public void remove() {
			if(lastReturned == null) {
				throw new IllegalStateException("next() or previous() was not called beforehand!");
			}
			Node<E> lastNext = lastReturned.next();
			IteratorLinkedList.this.removeNode(lastReturned);
			if(next == lastReturned) {
				next = lastNext;
			}
			else {
				nextIndex--;
			}
			lastReturned = null;
		}

		@Override
		public void set(E arg0) {
			if(lastReturned == null) {
				throw new IllegalStateException("next() or previous() was not called beforehand!");
			}
			lastReturned.set(arg0);
		}
		
	}
}
