package com.ibm.research.msr.expandcluster;

public class ClassPair {
	
		String thisClass;
		
		public String getThisClass() {
			return thisClass;
		}



		public void setThisClass(String thisClass) {
			this.thisClass = thisClass;
		}



		public String getUsedClass() {
			return usedClass;
		}



		public void setUsedClass(String usedClass) {
			this.usedClass = usedClass;
		}



		String usedClass;
		
		

	public ClassPair(String thisClass, String usedClass) {
			super();
			this.thisClass = thisClass;
			this.usedClass = usedClass;
		}



	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((thisClass == null) ? 0 : thisClass.hashCode());
		result = prime * result + ((usedClass == null) ? 0 : usedClass.hashCode());
		return result;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassPair other = (ClassPair) obj;
		if (thisClass == null) {
			if (other.thisClass != null)
				return false;
		} else if (!thisClass.equals(other.thisClass))
			return false;
		if (usedClass == null) {
			if (other.usedClass != null)
				return false;
		} else if (!usedClass.equals(other.usedClass))
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ClassPair [thisClass=" + thisClass + ", usedClass=" + usedClass + "]";
	}



	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
