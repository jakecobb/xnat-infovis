package edu.gatech.cs7450;

/**
 * Simple pair of objects.
 *
 * @param <T1> the type of the first item
 * @param <T2> the type of the second item
 */
public final class Pair<T1, T2> {
	/**
	 * Makes a pair parameterized with the types of the arguments.
	 * 
	 * @param first  the first item
	 * @param second the second item
	 * @return the pair
	 */
	public static <T1, T2> Pair<T1, T2> make(T1 first, T2 second) {
		return new Pair<T1,T2>(first, second);
	}
	
	/** The first item. */
	private final T1 first;
	/** The second item. */
	private final T2 second;
	
	/**
	 * Copy constructor.
	 * 
	 * @param pair the pair to copy
	 * @throws NullPointerException if <code>pair</code> is <code>null</code>
	 */
	public Pair(Pair<? extends T1, ? extends T2> pair) {
		if( pair == null ) throw new NullPointerException("pair is null");
		this.first = pair.first;
		this.second = pair.second;
	}

	/**
	 * Creates a new pair.
	 * 
	 * @param first  the first item
	 * @param second the second item
	 */
	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Returns the first item.
	 * @return the first item
	 */
	public T1 getFirst() {
		return first;
	}

	/**
	 * Returns the second item.
	 * @return the second item
	 */
	public T2 getSecond() {
		return second;
	}

// Object overrides (generated)
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		Pair<?,?> other = (Pair<?,?>)obj;
		if( first == null ) {
			if( other.first != null )
				return false;
		} else if( !first.equals(other.first) )
			return false;
		if( second == null ) {
			if( other.second != null )
				return false;
		} else if( !second.equals(other.second) )
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Pair [first=" + first + ", second=" + second + "]";
	}
}
