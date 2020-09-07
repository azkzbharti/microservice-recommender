package com.ibm.research.msr.binaryextractor;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javassist.ClassPool;
import javassist.CtClass;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ReferencedClassesExtractor.class, ClassParser.class, ClassPool.class})
public class ReferencedClassesExtractorTest {
	
	@Test
	public void getFullyQualifiedClassNameTest() throws Exception {
		ClassParser classParser = mock(ClassParser.class);
		whenNew(ClassParser.class).withAnyArguments().thenReturn(classParser);
		
		JavaClass javaClass = mock(JavaClass.class);
		when(classParser.parse()).thenReturn(javaClass);
		when(javaClass.getClassName()).thenReturn("className");
		
		ReferencedClassesExtractor extractor = new ReferencedClassesExtractor();
		
		Assert.assertEquals("className", extractor.getFullyQualifiedClassName("localtion"));
	}
	
	@Test
	public void getClassPathToInsertTest() {
		String classPathToInsert = ReferencedClassesExtractor.getClassPathToInsert("\\temp\\com\\test", "com.test");
		Assert.assertEquals("\\temp\\", classPathToInsert);
	}
	
	@Test
	public void extractFromClassTest() throws Exception {
		ClassParser classParser = mock(ClassParser.class);
		whenNew(ClassParser.class).withAnyArguments().thenReturn(classParser);
		
		JavaClass javaClass = mock(JavaClass.class);
		when(classParser.parse()).thenReturn(javaClass);
		when(javaClass.getClassName()).thenReturn("com.test");
		
		ClassPool classPool = mock(ClassPool.class);
		when(classPool.insertClassPath(Mockito.anyString())).thenReturn(null);
		
		CtClass ctClass = mock(CtClass.class);
		when(classPool.get(Mockito.anyString())).thenReturn(ctClass);
		
		Collection<String> collection = new ArrayList<>();
		collection.add("class1");
		collection.add("class2");
		when(ctClass.getRefClasses()).thenReturn(collection);
		
		ReferencedClassesExtractor extractor = new ReferencedClassesExtractor();
		HashSet<String> hashSet = extractor.extractFromClass("\\temp\\com\\test");
		
		Assert.assertTrue(hashSet.isEmpty());
	}
	
	
	@Test
	public void extractFromJARTest() throws Exception {
		JarFile jarFile = mock(JarFile.class);
		whenNew(JarFile.class).withAnyArguments().thenReturn(jarFile);
		
		List<JarEntry> list = new ArrayList<>();
		
		JarEntry jarEntry1 = new JarEntry("class1.class");
		JarEntry jarEntry2 = new JarEntry("class2.class");
		
		list.add(jarEntry1);
		list.add(jarEntry2);
		
		Enumeration<JarEntry> enumeration = Collections.enumeration(list);
		
		when(jarFile.entries()).thenReturn(enumeration);
		
		ClassParser classParser = mock(ClassParser.class);
		whenNew(ClassParser.class).withAnyArguments().thenReturn(classParser);
		
		JavaClass javaClass = mock(JavaClass.class);
		when(classParser.parse()).thenReturn(javaClass);
		when(javaClass.getClassName()).thenReturn("com.test");
		
		ClassPool classPool = mock(ClassPool.class);
		when(classPool.insertClassPath(Mockito.anyString())).thenReturn(null);
		
		CtClass ctClass = mock(CtClass.class);
		when(classPool.get(Mockito.anyString())).thenReturn(ctClass);
		
		ReferencedClassesExtractor extractor = new ReferencedClassesExtractor();
		Map<String, HashSet<String>> extractFromJAR = extractor.extractFromJAR("jarName");
		
		Assert.assertTrue(extractFromJAR.isEmpty());
	}

}
