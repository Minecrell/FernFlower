package org.jetbrains.java.decompiler.struct;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public interface StructContext extends Closeable {

  Map<String, StructClass> getClasses();

  default StructClass getClass(String name) {
    return getClasses().get(name);
  }

  byte[] readClass(String className) throws IOException;

  default byte[] readClass(StructClass structClass) throws IOException {
    return readClass(structClass.qualifiedName);
  }

  Set<String> getResources();

  InputStream openResource(String resource) throws IOException;

  byte[] readResource(String resource) throws IOException;

  void reloadContext() throws IOException;

}
