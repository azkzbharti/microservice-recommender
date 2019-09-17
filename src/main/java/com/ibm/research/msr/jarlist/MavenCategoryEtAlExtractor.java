package com.ibm.research.msr.jarlist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * extracts category, tags et al from a jar by going to maven repo website
 * (using its group id, artifact id which is extracted from the jar)
 * 
 * @author GiriprasadSridhara
 *
 */
public class MavenCategoryEtAlExtractor {

	/**
	 * class for GSON transformation
	 * 
	 * @author GiriprasadSridhara
	 *
	 */
	class MavenCategoryEtAl {
		StringBuffer description = new StringBuffer();

		StringBuffer license = new StringBuffer();

		StringBuffer categories = new StringBuffer();

		StringBuffer tags = new StringBuffer();

		StringBuffer usedBy = new StringBuffer();

		String name = new String();

		public MavenCategoryEtAl(String description, String license, String categories, String tags, String usedBy) {
			super();
			this.description.append(description + " ");
			this.license.append(license + " ");
			this.categories.append(categories + " ");
			this.tags.append(tags + " ");
			this.usedBy.append(usedBy + " ");
		}

		public MavenCategoryEtAl(String name) {
			this.name = name;
		}

		public void setDescription(String d) {
			description.append(d.toString() + " ");
		}

		public void setNameValuePairs(String n, String v) {
			if (n.compareTo("License") == 0) {
				license.append(v + " ");
			} else if (n.compareTo("Categories") == 0) {
				categories.append(v + " ");
			} else if (n.compareTo("Tags") == 0) {
				tags.append(v + " ");
			} else if (n.compareTo("Used By") == 0) {
				usedBy.append(v + " ");
			}
		}
	}

	public void find(String jarRootPath, String outputJSONFile) {

		File fRoot = new File(jarRootPath);
		String[] extensions = new String[] { "jar" };
		System.out.println("Getting all .jar  in " + fRoot.getPath() + " including those in subdirectories");
		List<File> files = (List<File>) FileUtils.listFiles(fRoot, extensions, true);
		Set<String> processedJars = new HashSet<String>();

		JSONArray mArray = new JSONArray();

		for (File f : files) {
			String fn = f.getName();
			if (!fn.endsWith("jar")) {
				continue;
			}
			if (processedJars.contains(fn)) {
				continue;
			}
			processedJars.add(fn);

			String jarName = f.getAbsolutePath();

			MavenCategoryEtAl m = processOneJar(jarName);
			if (m != null) {

				JSONObject mObject = new JSONObject();
				mObject.put("description", m.description.toString());
				mObject.put("license", m.license.toString());
				mObject.put("categories", m.categories.toString());
				mObject.put("tags", m.tags.toString());
				mObject.put("usedBy", m.usedBy.toString());
				mObject.put("name", m.name.toString());

				mArray.add(mObject);

			}

		}

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(outputJSONFile);
			pw.write(mArray.toString());

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.flush();
				pw.close();
			}
		}

	}

	public MavenCategoryEtAl processOneJar(String jarName) {
		JarFile jarFile = null;
		MavenCategoryEtAl m = null;

		try {

			int li = jarName.lastIndexOf(File.separator);
			String jarWoPath = null;
			if (li != -1) {
				jarWoPath = jarName.substring(li + 1);
			} else {
				jarWoPath = jarName;
			}

			jarFile = new JarFile(jarName);

			Enumeration<JarEntry> entries = jarFile.entries();
			String groupId = null;
			String artifactId = null;

			m = new MavenCategoryEtAl(jarWoPath);

			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				// System.out.println(entry.getName());
				// looking for META-INF/maven/com.google.guava/guava/
				String name = entry.getName();
				if (name.contains("META-INF/maven/")) {
					String[] parts = name.split("/");
					if (parts.length == 4) {
						groupId = parts[2];
						artifactId = parts[3];
					}
					if (groupId == null || artifactId == null) {
						continue;
					}

				} else {
					continue;
				}
				if (groupId == null || artifactId == null) {
					System.err.println("group/artifact id null " + groupId + " , " + artifactId);
					return m;
				}

				// for url like:
				// https://mvnrepository.com/artifact/com.google.guava/guava
				String sUrl = "https://mvnrepository.com/artifact/" + groupId + "/" + artifactId;
				Document d = Jsoup.connect(sUrl).get();

				// System.out.println(d.html());

				String descText = null;
				Elements desc = d.select("div.im-description");
				if (desc != null) {
					Element descElem = desc.get(0);
					if (descElem != null) {
						// System.out.println("description=" + descElem.text());
						descText = descElem.text();
						m.setDescription(descText);
					}
				}

				Elements tables = d.select(".grid");
				if (tables != null) {
					Element gridTable = tables.get(0);
					if (gridTable != null) {
						Elements rows = gridTable.select("tr");

						for (int i = 0; i < rows.size(); i++) {
							Element row = rows.get(i);
							Elements th = row.select("th");
							Elements td = row.select("td");

							String rowName = th.get(0).text();
							// System.out.println("th=" + rowName);

							for (int j = 0; j < td.size(); j++) {
								Element el = td.get(j);
								// System.out.println("td " + j + " " + el.text());
								String rowVal = el.text();
								m.setNameValuePairs(rowName, rowVal);
							}

							// System.out.println("td="+td.get(0).text());
							// if (cols.get(3).text().equals("Titan")) {

							// }

						}
					}
				}

				return m;

			}

			if (groupId == null || artifactId == null) {
				System.err.println("group/artifact id null " + groupId + " , " + artifactId + " for JAR " + jarName);
				return null;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return m;

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MavenCategoryEtAlExtractor m = new MavenCategoryEtAlExtractor();
		int choice = 2;

		if (choice == 2) {
			if (args.length < 2) {
				System.err.println(
						"USAGE: <root folder under which jars are located (recursively searched)> <output root where jsons are written>");
				System.exit(-1);
			}
			// root="C:\\Users\\GiriprasadSridhara\\Downloads\\digdeep-master\\";
			String root = args[0];
			String opRoot = args[1];
			m.find(root, opRoot);
		} else {
			// String
			// jarName="C:\\Users\\GiriprasadSridhara\\Documents\\demo-july\\july-30\\guava-27.1-jre.jar";
			String jarName = "C:\\Users\\GiriprasadSridhara\\.m2\\repository\\commons-cli\\commons-cli\\1.4\\commons-cli-1.4.jar";
			// String opJsonFileName="C:\\temp\\maven-category-etal.json";
			String opRoot = "C:\\temp";
			MavenCategoryEtAl meta = m.processOneJar(jarName);
		}
	}

}
