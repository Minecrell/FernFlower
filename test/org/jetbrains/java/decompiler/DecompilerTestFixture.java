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
package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.struct.FileStructContext;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class DecompilerTestFixture {
  private File testDataDir;
  private Fernflower decompiler;
  private FileStructContext context;
  public boolean cleanup = true;

  public void setUp() throws IOException {
    setUp(Collections.<String, Object>emptyMap());
  }

  public void setUp(final Map<String, Object> options) throws IOException {
    testDataDir = new File("testData");
    if (!isTestDataDir(testDataDir)) testDataDir = new File("community/plugins/java-decompiler/engine/testData");
    if (!isTestDataDir(testDataDir)) testDataDir = new File("plugins/java-decompiler/engine/testData");
    if (!isTestDataDir(testDataDir)) testDataDir = new File("../community/plugins/java-decompiler/engine/testData");
    if (!isTestDataDir(testDataDir)) testDataDir = new File("../plugins/java-decompiler/engine/testData");
    assertTrue("current dir: " + new File("").getAbsolutePath(), isTestDataDir(testDataDir));

    this.context = new FileStructContext();
    decompiler = new Fernflower(this.context, new HashMap<String, Object>() {{
      put(IFernflowerPreferences.LOG_LEVEL, "warn");
      put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1");
      put(IFernflowerPreferences.REMOVE_SYNTHETIC, "1");
      put(IFernflowerPreferences.REMOVE_BRIDGE, "1");
      put(IFernflowerPreferences.LITERALS_AS_IS, "1");
      put(IFernflowerPreferences.UNIT_TEST_MODE, "1");
      putAll(options);
    }}, new PrintStreamLogger(System.err));
  }

  public void tearDown() throws IOException {
    this.decompiler.close();
  }

  public File getTestDataDir() {
    return testDataDir;
  }

  public Fernflower getDecompiler() {
    return decompiler;
  }

  public FileStructContext getContext() {
    return this.context;
  }

  private static boolean isTestDataDir(File dir) {
    return dir.isDirectory() && new File(dir, "classes").isDirectory() && new File(dir, "results").isDirectory();
  }

  private static void delete(File file) {
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File f : files) delete(f);
      }
    }
    assertTrue(file.delete());
  }
}
