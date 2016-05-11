/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.java.decompiler.struct;

import org.jetbrains.java.decompiler.struct.file.FileStructEntry;
import org.jetbrains.java.decompiler.struct.file.StructEntry;
import org.jetbrains.java.decompiler.struct.file.ZipStructEntry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileStructContext implements StructContext {

  protected final Map<String, StructEntry> entries = new HashMap<>();
  private final Set<String> entryKeyView = Collections.unmodifiableSet(this.entries.keySet());

  protected Map<String, StructClass> classes = new HashMap<>();
  private Map<String, StructClass> classesView = Collections.unmodifiableMap(this.classes);

  @Override
  public Map<String, StructClass> getClasses() {
    return this.classesView;
  }

  @Override
  public StructClass getClass(String name) {
    return this.classes.get(name);
  }

  @Override
  public byte[] readClass(String className) throws IOException {
    StructClass structClass = this.classes.get(className);
    return structClass != null ? this.entries.get(structClass.source).read() : null;
  }

  @Override
  public byte[] readClass(StructClass structClass) throws IOException {
    StructEntry entry = this.entries.get(structClass.source);
    return entry != null ? entry.read() : null;
  }

  @Override
  public Set<String> getResources() {
    return this.entryKeyView;
  }

  @Override
  public InputStream openResource(String resource) throws IOException {
    StructEntry entry = this.entries.get(resource);
    return entry != null ? entry.open() : null;
  }

  @Override
  public byte[] readResource(String resource) throws IOException {
    StructEntry entry = this.entries.get(resource);
    return entry != null ? entry.read() : null;
  }

  @Override
  public void reloadContext() throws IOException {
    Collection<StructClass> oldClasses = this.classes.values();
    Map<String, StructClass> newClasses = new HashMap<>();

    for (StructClass cl : oldClasses) {
      byte[] bytes = readClass(cl);
      StructClass newCl = new StructClass(bytes, cl.isOwn(), cl.context, cl.source);
      newClasses.put(newCl.qualifiedName, newCl);
    }

    this.classes = newClasses;
    this.classesView = Collections.unmodifiableMap(newClasses);
  }

  @Override
  public void close() throws IOException {
    IOException e = null;
    for (StructEntry entry : this.entries.values()) {
      try {
        entry.close();
      } catch (IOException ex) {
        if (e == null) {
          e = new IOException("Failed to close struct entries");
        }

        e.addSuppressed(ex);
      }
    }

    if (e != null) {
      throw e;
    }
  }

  public void scan(Path path, boolean own) throws IOException {
    if (Files.isDirectory(path)) {
      scanDirectory(path, own);
      return;
    }

    String s = path.toString();
    if (s.endsWith(".class")) {
      addClassFile(path, own);
    } else if (s.endsWith(".jar") || s.endsWith(".zip")) {
      scanZipFile(path, own);
    } else if (own) {
      addResourceFile(path);
    }
  }

  protected StructEntry addClassFile(Path path, boolean own) throws IOException {
    return addClassFile(path.getFileName().toString(), path, own);
  }

  protected StructEntry addClassFile(String source, Path path, boolean own) throws IOException {
    StructEntry entry = createFileEntry(source, path);
    addResource(source, entry);
    readClass(source, entry, own);
    return entry;
  }

  protected StructEntry addResourceFile(Path path) throws IOException {
    return addResourceFile(path.getFileName().toString(), path);
  }

  protected StructEntry addResourceFile(String source, Path path) throws IOException {
    StructEntry entry = createFileEntry(source, path);
    addResource(source, entry);
    return entry;
  }

  protected StructEntry createFileEntry(String source, Path path) {
    return new FileStructEntry(path);
  }

  protected void scanDirectory(Path path, boolean own) throws IOException {
    throw new UnsupportedOperationException();
  }

  protected void scanZipFile(Path path, boolean own) throws IOException {
    // TODO: Ensure it gets always properly closed

    boolean success = false;

    ZipFile zip = new ZipFile(path.toFile());
    try {
      Enumeration<? extends ZipEntry> entries = zip.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.isDirectory()) {
          continue; // TODO
        }

        final String name = entry.getName();
        if (name.endsWith(".class")) {
          addZipClass(name, zip, entry, own);
        } else if (own) {
          addZipResource(name, zip, entry);
        }

        success = true;
      }
    } finally {
      if (!success) {
        zip.close();
      }
    }
  }

  protected StructEntry addZipClass(String source, ZipFile zip, ZipEntry entry, boolean own) throws IOException {
    StructEntry structEntry = createZipEntry(zip, entry);
    addResource(source, structEntry);
    readClass(source, structEntry, own);
    return structEntry;
  }

  protected StructEntry addZipResource(String source, ZipFile zip, ZipEntry entry) throws IOException {
    StructEntry structEntry = createZipEntry(zip, entry);
    addResource(source, structEntry);
    return structEntry;
  }

  protected StructEntry createZipEntry(ZipFile zip, ZipEntry entry) {
    return new ZipStructEntry(zip, entry);
  }

  protected void readClass(String source, StructEntry entry, boolean own) throws IOException {
    StructClass structClass = new StructClass(entry.read(), own, this, source);

    StructClass current = this.classes.get(structClass.qualifiedName);
    if (current != null) {
      throw new IllegalArgumentException("Duplicate class: " + current.qualifiedName);
    }

    this.classes.put(structClass.qualifiedName, structClass);
  }

  protected void addResource(String source, StructEntry entry) throws IOException {
    StructEntry current = this.entries.get(source);
    if (current != null) {
      throw new IllegalArgumentException("Cannot register " + entry + " as " + source + ", already registered to " + current);
    }

    this.entries.put(source, entry);
  }

}
