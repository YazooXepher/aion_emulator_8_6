package com.aionemu.commons.scripting.impl.javacompiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.DiagnosticListener;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

import com.aionemu.commons.scripting.ScriptClassLoader;

/**
 * This class manages loaded classes. It is also responsible for tricking compiler.
 *
 * @author SoulKeeper
 */
public class ClassFileManager extends ForwardingJavaFileManager<JavaFileManager> {

	private final Map<String, BinaryClass> compiledClasses = new HashMap<String, BinaryClass>();
	protected ScriptClassLoaderImpl loader;
	protected ScriptClassLoader parentClassLoader;

	public ClassFileManager(JavaCompiler compiler, DiagnosticListener<? super JavaFileObject> listener) {
		super(compiler.getStandardFileManager(listener, null, null));
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling)
			throws IOException {
		BinaryClass co = new BinaryClass(className);
		compiledClasses.put(className, co);
		return co;
	}

	@Override
	public synchronized ScriptClassLoaderImpl getClassLoader(Location location) {
		if (loader == null) {
			if (parentClassLoader != null) {
				loader = new ScriptClassLoaderImpl(this, parentClassLoader);
			} else {
				loader = new ScriptClassLoaderImpl(this);
			}
		}
		return loader;
	}

	public void setParentClassLoader(ScriptClassLoader classLoader) {
		this.parentClassLoader = classLoader;
	}

	public void addLibrary(File file) throws IOException {
		ScriptClassLoaderImpl classLoader = getClassLoader(null);
		classLoader.addJarFile(file);
	}

	public void addLibraries(Iterable<File> files) throws IOException {
		for (File f : files) {
			addLibrary(f);
		}
	}

	public Map<String, BinaryClass> getCompiledClasses() {
		return compiledClasses;
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
			throws IOException {
		Iterable<JavaFileObject> objects = super.list(location, packageName, kinds, recurse);

		if (StandardLocation.CLASS_PATH.equals(location) && kinds.contains(Kind.CLASS)) {
			List<JavaFileObject> temp = new ArrayList<JavaFileObject>();
			for (JavaFileObject object : objects) {
				temp.add(object);
			}
			temp.addAll(loader.getClassesForPackage(packageName));
			objects = temp;
		}

		return objects;
	}

	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		if (file instanceof BinaryClass) {
			return ((BinaryClass) file).inferBinaryName();
		}
		return super.inferBinaryName(location, file);
	}
}