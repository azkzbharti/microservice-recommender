package com.ibm.research.msr.db.dto;

import org.bson.Document;

public class SourceProject extends Project{
	public SourceProject(String name, String desc, String projLocation, String srcLang, String type, String codeType, String rootAnalyzePath) {
		super(name,  desc, projLocation,  srcLang,  type, codeType, rootAnalyzePath);
	}

	public SourceProject(Document d) {
		super(d);
	}

	public Document toDocument() {
		Document d = new Document()
				.append("_id", this.get_id())
				.append("project_name", this.getProjectName())
				.append("project_desc", this.getProjectDesc())
				.append("project_zip_location", this.getProjectLocation())
				.append("src_language", this.getSourceLanguage())
				.append("type", this.getType())
				.append("code_type", this.getCodeType())
				.append("status", this.getStatus())
				.append("root_analyze_path", this.getRootAnalyzePath());

		return d;
	}

}
