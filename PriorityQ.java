
public class PriorityQ<E extends Comparable<? super E>>
{
	private QNode<E> first;
	private int size;
	
	/**
	 * enq short for enqueue, this method
	 * inserts a node at its correct spot in 
	 * the queue based on its priority
	 */
	public void enq(E item, int pri) // pri short for priority
	{
		QNode<E> node = new QNode<E>(item, pri);
		if(this.first == null)
		{
			this.first = node;
		}
		else if(node.getPriority() < this.first.getPriority())
		{
			node.setNext(this.first);
			this.first = node;
		}
		else 
		{
			QNode<E> temp = this.first;
			while( (temp.getNext() != null) && (node.getPriority() >= temp.getNext().getPriority()) )
			{
				temp = temp.getNext();
			}
			node.setNext(temp.getNext());
			temp.setNext(node);
		}
		this.size++;
	}
	
	/**
	 * deq short for dequeue, this method
	 * removes the first node in the queue
	 */
	public E deq()
	{
		if(this.first != null)
		{
			E item = this.first.getData();
			this.first = this.first.getNext();
			this.size--;
			return item;
		}
		return null;
	}
	
	/**
	 * returns the first element in the queue	
	 */
	public E first()
	{
		if(this.first != null)
		{
			return this.first.getData();
		}
		return null;
	}
	
	/**
	 * returns the number of elements
	 * in the queue	
	 */
	public int size()
	{
		return this.size;
	}
	
	/**
	 * empties the priority queue	
	 */
	public void clear()
	{
		this.first = null;
		this.size = 0;
	}
	
	/**
	 * shows all the elements inthe queue along
	 * with their corresponding priority	
	 */
	public String toString()
	{
		QNode<E> temp = this.first;
		String s = "";
		while(temp.getNext() != null)
		{
			s += temp.getData() + ": " + temp.getPriority() + ",  ";
		}
		s += temp.getData() + ": " + temp.getPriority();
		return s;
	}
	
	/**
	 * a class that hold generirc nodes that each
	 * have an associated prority assigned to them	
	 */
	private class QNode<E extends Comparable<? super E>>
	{
		private E data;
		//pri short for priority
		private int pri;
		private QNode<E> next;
		
		/**
		 * my code will always provide a prioirty and
		 * data when creatibg QNode objects	
		 */
		public QNode(E data, int pri)
		{
			this.data = data;
			this.pri = pri;
			this.next = null;
		}
		
		/**
		 * returns the data of this QNode	
		 */
		public E getData()
		{
			return this.data;
		}
		
		/**
		 * returns the priority of this QNode	
		 */
		public int getPriority()
		{
			return this.pri;
		}
		
		/**
		 * returns the node after this QNode
		 * in the queue
		 */
		public QNode<E> getNext()
		{
			return this.next;
		}
		
		/**
		 * sets the QNode after this QNode 
		 * to the given QNode
		 */
		public void setNext(QNode<E> node)
		{
			this.next = node;
		}
	}
}