package com.pmikee.svnexplorer;

public class POMDependency {

	private String artifact;
	private String groupId;
	private String version;
	private String originalVersion;

	public POMDependency(String artifact, String groupId, String version) {
		super();
		this.artifact = artifact;
		this.groupId = groupId;
		this.version = version;
	}

	public String getArtifact() {
		return artifact;
	}

	public void setArtifact(String artifact) {
		this.artifact = artifact;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getOriginalVersion() {
		return originalVersion;
	}

	public void setOriginalVersion(String originalVersion) {
		this.originalVersion = originalVersion;
	}

}
