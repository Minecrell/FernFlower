package org.jetbrains.java.decompiler.struct.file;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface StructEntry extends Closeable {

  InputStream open() throws IOException;

  byte[] read() throws IOException;

}
