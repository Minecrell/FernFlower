package org.jetbrains.java.decompiler.struct.file;

import com.google.common.base.MoreObjects;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStructEntry implements StructEntry {

  private final Path path;

  public FileStructEntry(Path path) {
    this.path = path;
  }

  @Override
  public InputStream open() throws IOException {
    return Files.newInputStream(this.path);
  }

  @Override
  public byte[] read() throws IOException {
    return Files.readAllBytes(this.path);
  }

  @Override
  public void close() throws IOException {

  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .addValue(this.path)
        .toString();
  }

}
