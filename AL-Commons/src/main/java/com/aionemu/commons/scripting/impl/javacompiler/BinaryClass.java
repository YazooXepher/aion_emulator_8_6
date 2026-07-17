package com.aionemu.commons.scripting.impl.javacompiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * This class is just a hack to make javac compiler work with classes loaded by
 * previous classloader. Also it's used as container for loaded class
 *
 * @author SoulKeeper
 */
public class BinaryClass extends SimpleJavaFileObject {

	private final String name;
	private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private Class<?> definedClass;

	protected BinaryClass(String name) {
		super(URI.create("class:///" + name.replace('.', '/') + ".class"), Kind.CLASS);
		this.name = name;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return new ByteArrayInputStream(baos.toByteArray());
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return baos;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Writer openWriter() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLastModified() {
		return 0;
	}

	@Override
	public boolean delete() {
		return false;
	}

	public byte[] getBytes() {
		return baos.toByteArray();
	}

	public Class<?> getDefinedClass() {
		return definedClass;
	}

	public void setDefinedClass(Class<?> definedClass) {
		this.definedClass = definedClass;
	}

	@Override
	public Kind getKind() {
		return Kind.CLASS;
	}

	@Override
	public String getName() {
		return name + ".class";
	}

	public String getShortName() {
		return this.name;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof BinaryClass) {
			return ((BinaryClass) arg0).name.equals(this.name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		return Kind.CLASS.equals(kind) || kind == Kind.OTHER;
	}

	public String inferBinaryName() {
		return name;
	}
}