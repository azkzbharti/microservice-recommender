package com.ibm.research.msr.db.dto;

import org.bson.Document;

public class GitProject extends Project{
	private String gitURL;
	private String branchName;
	private String sshKeyFileLocation;
	private String passPhrase;
	private String token;
	


	public GitProject(String name, String desc, String gitUrl, String branchName, String projLocation, String srcLang, String type, String codeType, String rootAnalyzePath) {
		super(name,  desc, projLocation,  srcLang,  type, codeType, rootAnalyzePath);
		this.gitURL = gitUrl;
		this.branchName  = branchName;
//		this.sshKeyFileLocation = sshKeyFileLocation;
//		this.passPhrase = passPhrase;
	}
	
	public GitProject(String name, String desc, String gitUrl, String token, String branchName, String projLocation, String srcLang, String type, String codeType, String rootAnalyzePath) {
		super(name,  desc, projLocation,  srcLang,  type, codeType, rootAnalyzePath);
		this.gitURL = gitUrl;
		this.branchName  = branchName;
		this.token = token;
	}

	public GitProject(Document d) {
		super(d);
		this.gitURL = d.getString("git_url");
		this.branchName  = d.getString("branch_name");
		this.token = d.getString("token");
//		this.sshKeyFileLocation = d.getString("sshkey_file_location");
//		this.passPhrase = d.getString("passPhrase");
	}

	public Document toDocument() {
		Document d = new Document()
				.append("_id", this.get_id())
				.append("project_name", this.getProjectName())
				.append("project_desc", this.getProjectDesc())
				.append("git_url", this.getGitURL())
				.append("token", this.getToken())
				.append("branch_name", this.getBranchName())
				.append("project_zip_location", this.getProjectLocation())
				.append("src_language", this.getSourceLanguage())
				.append("type", this.getType())
				.append("code_type", this.getCodeType())
				.append("status", this.getStatus())
				.append("root_analyze_path", this.getRootAnalyzePath());

		return d;
	}

	public String getGitURL() {
		return gitURL;
	}

	public void setGitURL(String gitURL) {
		this.gitURL = gitURL;
	}
	
	public String getSshKeyFileLocation() {
		return sshKeyFileLocation;
	}

	public void setSshKeyFileLocation(String sshKeyFileLocation) {
		this.sshKeyFileLocation = sshKeyFileLocation;
	}
	
	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getPassPhrase() {
		return passPhrase;
	}

	public void setPassPhrase(String passPhrase) {
		this.passPhrase = passPhrase;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
