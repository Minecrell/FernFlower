package org.jetbrains.java.decompiler.struct.file;

import com.google.common.base.MoreObjects;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipStructEntry implements StructEntry {

  private final ZipFile zip;
  private final ZipEntry entry;

  public ZipStructEntry(ZipFile zip, ZipEntry entry) {
    this.zip = zip;
    this.entry = entry;
  }

  @Override
  public InputStream open() throws IOException {
    return this.zip.getInputStream(this.entry);
  }

  @Override
  public byte[] read() throws IOException {
    try (InputStream in = open()) {
      return ByteStreams.toByteArray(in);
    }
  }

  @Override
  public void close() throws IOException {
    this.zip.close();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("zip", this.zip)
        .add("entry", this.entry)
        .toString();
  }

}
