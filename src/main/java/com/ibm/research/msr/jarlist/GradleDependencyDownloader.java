package com.ibm.research.msr.jarlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.control.SourceUnit;
import org.w3c.dom.Text;

//import com.ibm.research.appmod.dma.download.Dependency;

//import com.ibm.research.appmod.dma.download.Dependency;

public class GradleDependencyDownloader {

	/**
	 * Method used by MSRLauncher
	 * 
	 * @param pomFiles
	 * @param tempFolder
	 */
	public ArrayList<String> createPOMFiles(ArrayList<String> gradleFiles, String jarFolder) {
		Iterator<String> itr = gradleFiles.iterator();
		int i = 1;
		ArrayList<String> pomFiles = new ArrayList<>();
		while (itr.hasNext()) {
			createPOMFile(itr.next(), jarFolder + File.separator + "pom" + i + ".xml");
			pomFiles.add(jarFolder + File.separator + "pom" + i + ".xml");
			i++;
		}
		
		return pomFiles;
	}

	public void createPOMFile(String gradleBuildFile, String pomFile) {
		BufferedReader br = null;
		try {
			// String gradleFileToString = IOUtils.toString( new FileInputStream(
			// gradleBuildFile ), "UTF-8" );

			br = new BufferedReader(new FileReader(gradleBuildFile));
			// StringBuffer sb=new StringBuffer();
			String s = null;

			String DEPENDENCIES_BEGIN = ".*dependencies.*{";

			String[] depPatterns = new String[] { "\\s*api\\s+(.+)", "\\s*implementation\\s+(.+)",
					"\\s*providedCompile\\s+(.+)", "\\s*compile\\s+(.+)", "\\s*compileOnly\\s+(.+)" };

			Pattern[] patArr = new Pattern[depPatterns.length];
			for (int i = 0; i < depPatterns.length; i++) {
				patArr[i] = Pattern.compile(depPatterns[i]);
			}

			// group:'javax.json', name:'javax.json-api', version:'1.0'
			String sGroupNameVersion = "group:'(.+)',\\s*name:'(.+)',\\s*version:'(.+)'";
			// 'org.hibernate.javax.persistence:hibernate-jpa-2.1-api:1+'
			String sGroupNameVersion2 = "'(.+):(.+):(.+)'";

			Pattern pGroupNameVersion = Pattern.compile(sGroupNameVersion);
			Pattern pGroupNameVersion2 = Pattern.compile(sGroupNameVersion2);

			// Pattern p=Pattern.compile(DEPENDENCIES_BEGIN);

			List<Dependency> depList = new ArrayList<Dependency>();
			while ((s = br.readLine()) != null) {
				for (int i = 0; i < patArr.length; i++) {
					Matcher m = patArr[i].matcher(s);
					if (m.matches()) {

						//System.out.println("match with " + depPatterns[i]);
						//System.out.println("m.group(1)=" + m.group(1));

						String groupNameVersion = m.group(1);

						Matcher mGNV = pGroupNameVersion.matcher(groupNameVersion);
						if (mGNV.matches()) {
							//System.out.println("GNV" + mGNV.group(1) + "---" + mGNV.group(2) + "---" + mGNV.group(3));
							Dependency dep = null;
							if (mGNV.group(3).contains("+")) {
								//System.out.println("+ in version");
								String lv = POMDependencyDownloader.extractLatestVersionNumberFromMaven(mGNV.group(1),
										mGNV.group(2));
								dep = new Dependency(mGNV.group(1), mGNV.group(2), lv);
							} else {
								dep = new Dependency(mGNV.group(1), mGNV.group(2), mGNV.group(3));
							}
							depList.add(dep);
						} else {
							//System.out.println("no match for=" + groupNameVersion);
//						String[] gnvArr=groupNameVersion.split(":");
							Matcher mGNV2 = pGroupNameVersion2.matcher(groupNameVersion);
							if (mGNV2.matches()) {
								//System.out.println("\t mGNV2 match = " + mGNV2.group(1) + "---" + mGNV2.group(2) + "---"
								//		+ mGNV2.group(3));

								Dependency dep = null;

								if (mGNV2.group(3).contains("+")) {
									//System.out.println("+ in version");
									String lv = POMDependencyDownloader
											.extractLatestVersionNumberFromMaven(mGNV2.group(1), mGNV2.group(2));
									dep = new Dependency(mGNV2.group(1), mGNV2.group(2), lv);
								} else {
									dep = new Dependency(mGNV2.group(1), mGNV2.group(2), mGNV2.group(3));
								}

								depList.add(dep);

							}
						}

						// Dependency dep=new Dependency(groupId, artifactId, latestVersionNumber);
						break;
					}
				}
				// sb.append(s+"\r\n");
			}

			generatePOMXMLWithLatestVersions(depList, pomFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(br!=null)
				try {
					br.close();
				} catch (IOException e) {
					System.err.println("Exception while closing reader." + e.getMessage());
				}
		}
	}

	public void findDependenciesByPlainTextParsing(String projectRoot) {
		File f = new File(projectRoot);
		String gradleBuildFile = null;
		String pomFile = null;

		if (f.isDirectory()) {
			gradleBuildFile = projectRoot + File.separator + "build.gradle";
			pomFile = projectRoot + File.separator + "pom-minimal.xml";

		} else {
			gradleBuildFile = projectRoot;
		}

		createPOMFile(gradleBuildFile, pomFile);

	}

	public void download2(String projectRoot) {
		File f = new File(projectRoot);
		String gradleBuildFile = null;
		if (f.isDirectory()) {
			gradleBuildFile = projectRoot + File.separator + "build.gradle";
		} else {
			gradleBuildFile = projectRoot;
		}

		String gradleFileToString = null;
		try {
			gradleFileToString = IOUtils.toString(new FileInputStream(gradleBuildFile), "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SourceUnit unit = SourceUnit.create("gradle", gradleFileToString);
		unit.parse();
		unit.completePhase();
		unit.convert();
		GradleDependenciesVisitor gv = new GradleDependenciesVisitor();
		visitScriptCode(unit, gv);

	}

	private void visitScriptCode(SourceUnit source, GroovyCodeVisitor transformer) {
		//System.out.println("enter visitScriptCode");
		source.getAST().getStatementBlock().visit(transformer);

		//System.out.println("source.getAST().getMethods() =" + source.getAST().getMethods().size());

		for (Object method : source.getAST().getMethods()) {
			MethodNode methodNode = (MethodNode) method;
			//System.out.println("methodNode to string=" + methodNode.toString());
			methodNode.getCode().visit(transformer);
		}
	}

	public boolean download(String projectRoot) {

		File f = new File(projectRoot);
		String gradleBuildFile = null;
		if (f.isDirectory()) {
			gradleBuildFile = projectRoot + File.separator + "build.gradle";
		} else {
			gradleBuildFile = projectRoot;
		}

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(gradleBuildFile));
			StringBuffer sb = new StringBuffer();
			String s = null;
			while ((s = br.readLine()) != null) {
				sb.append(s + "\r\n");
			}

//			System.out.println(sb);

			AstBuilder astb = new AstBuilder();
			String source = IOUtils.toString(new FileInputStream(gradleBuildFile), "UTF-8");
			// List<ASTNode> nodes = astb.buildFromString(sb.toString());
			List<ASTNode> nodes = astb.buildFromString(source);
			// astb.
			GradleDependenciesVisitor gv = new GradleDependenciesVisitor();

			for (ASTNode n : nodes) {
				//System.out.println("ASTNode=" + n.getText());
//				n.visit(new CodeVisitorSupport() {
//			
//
//				});
				n.visit(gv);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(br!= null)
				try {
					br.close();
				} catch (IOException e) {
					System.err.println("Exception while closing reader. " + e.getMessage());
				}
		}

		return true;
	}

	private void generatePOMXMLWithLatestVersions(List<Dependency> dependencies, String opPOMXMLName)
			throws TransformerFactoryConfigurationError {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
			docFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			org.w3c.dom.Document doc = docBuilder.newDocument();
			org.w3c.dom.Element rootElement = doc.createElement("project");
			doc.appendChild(rootElement);

			org.w3c.dom.Element eDependencies = doc.createElement("dependencies");
			rootElement.appendChild(eDependencies);

			for (Dependency d : dependencies) {
				org.w3c.dom.Element eDependency = doc.createElement("dependency");
				eDependencies.appendChild(eDependency);

				org.w3c.dom.Element gid = doc.createElement("groupId");
				Text tnGID = doc.createTextNode(d.getGroupId());
				gid.appendChild(tnGID);
				eDependency.appendChild(gid);

				org.w3c.dom.Element aid = doc.createElement("artifactId");
				Text tnAID = doc.createTextNode(d.getArtifactId());
				aid.appendChild(tnAID);
				eDependency.appendChild(aid);

				org.w3c.dom.Element version = doc.createElement("version");
				Text tnVersion = doc.createTextNode(d.getVersion());
				version.appendChild(tnVersion);
				eDependency.appendChild(version);

			}

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
			transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, ""); // Compliant
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			// StreamResult result = new StreamResult(new
			// File("C:\\Users\\GiriprasadSridhara\\Documents\\rs-minimal-pom-for-download-2.xml"));
			StreamResult result = new StreamResult(new File(opPOMXMLName));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);
			//System.out.println("wrote latest pom xml to " + opPOMXMLName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GradleDependencyDownloader g = new GradleDependencyDownloader();
		//String projectRoot = "C:\\Users\\GiriprasadSridhara\\sample.daytrader7\\daytrader-ee7-ejb";
		String projectRoot="C:\\Users\\GiriprasadSridhara\\sample.plantsbywebsphere-18.0.0.4\\sample.plantsbywebsphere";
		// String
		// projectRoot="C:\\Users\\GiriprasadSridhara\\sample.plantsbywebsphere\\";

		// String projectRoot="C:\\temp\\temp.build.gradle";
		// g.download2(projectRoot);
		g.findDependenciesByPlainTextParsing(projectRoot);
	}

}
