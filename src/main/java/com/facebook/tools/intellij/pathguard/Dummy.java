package com.facebook.tools.intellij.pathguard;

import java.io.File;
import java.io.FilenameFilter;

public class Dummy {
  public static FilenameFilter getFileFilter(File directory) {
    return GuardMediator.getFilter();
  }
}
