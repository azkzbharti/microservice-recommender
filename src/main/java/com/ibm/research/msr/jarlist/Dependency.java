package com.ibm.research.msr.jarlist;

class Dependency {
	

	String groupId;

	String artifactId;

	String version;

	public Dependency(String groupId, String artifactId, String version) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @return the artifactId
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Dependency [groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + "]";
	}
}