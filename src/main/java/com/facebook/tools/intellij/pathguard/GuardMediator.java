package com.facebook.tools.intellij.pathguard;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GuardMediator implements FilenameFilter {
  private static final String ROOT = "/Users/bduff/fbsource";

  private AllowedFile root = new AllowedFile();


  
  enum WildcardType {
    NONE,
    ALLOW_CHILDREN,
    ALLOW_RECURSIVE
  }

  class AllowedFile {
    WildcardType wildcardType = WildcardType.NONE;
    final Map<String, AllowedFile> allowedChildren = new HashMap<String, AllowedFile>();

    AllowedFile allowChild(String name) {
      name = name.intern();
      return allowedChildren.computeIfAbsent(name, x -> new AllowedFile());
    }

    AllowedFile getIfAllowed(String name) {
      return allowedChildren.get(name);
    }
  }

  private void addAllowedPath(AllowedFile root, String path) {
    String[] bits = path.split("/");
    AllowedFile parent = root;
    String lastBit = null;
    for (String bit : bits) {
      if (!"*".equals(bit) && !"**".equals(bit)) {
        AllowedFile child = parent.allowChild(bit);
        parent = child;
      }
      lastBit = bit;
    }
    // Set the allow all bit for the last path component.
    if ("*".equals(lastBit)) {
      parent.wildcardType = WildcardType.ALLOW_CHILDREN;
    } else if ("**".equals(lastBit)) {
      parent.wildcardType = WildcardType.ALLOW_RECURSIVE;
    }
  }

  GuardMediator() {
    try {
      File patterns = new File(ROOT + "/.guardpatterns");
      Files.readAllLines(patterns.toPath()).forEach(line -> {
        line = line.trim();
        if (line.length() == 0 || line.charAt(0) == '#') return;
        addAllowedPath(root, line);
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    printAllowed();
  }

  private void printAllowed() {
    printAllowed(0, root);
  }

  private void printAllowed(int spaceCount, AllowedFile parent) {
    for (String key : parent.allowedChildren.keySet()) {
      AllowedFile child = parent.getIfAllowed(key);
      for (int i = 0; i < spaceCount; i++) {
        System.out.print(" ");
      }
      System.out.print(key);
      switch (child.wildcardType) {
        case ALLOW_CHILDREN: 
          System.out.println(" *");
          break;
        case ALLOW_RECURSIVE:
          System.out.println(" **");
          break;
        case NONE:
          System.out.println();
      }
      printAllowed(spaceCount + 2, child);
    }
  }

  private String getPathFromRoot(String path) {
    return path.substring(ROOT.length() + 1);
  }


  private final Set<String> roots = new HashSet<>();
  private final Set<String> allowedPaths = new HashSet<>();

  private final static GuardMediator mediator = new GuardMediator();

  public static FilenameFilter getFilter() {    
    return mediator;
  }

  @Override
  public boolean accept(File dir, String name) {
    String path = dir.getAbsolutePath();
    if (!isUnderRoot(path)) {
      return true;
    }

    AllowedFile parent;
    if (dir.getAbsolutePath().equals(ROOT)) {
      parent = root;
    } else {
      String pathFromRoot = getPathFromRoot(dir.getAbsolutePath());
      String[] parts = pathFromRoot.split("/");
      parent = root;
      for (String part : parts) {
        parent = parent.getIfAllowed(part);
        // If some parent in the path allows recursive, then we're done, and just allow.
        if (parent != null && parent.wildcardType == WildcardType.ALLOW_RECURSIVE) {
          // System.out.println("✅ " + dir + "/" + name);
          return true;
        }

        // If parent is null at some point, then this directory is not allowed
        if (parent == null) {
          // System.out.println("❌ " + dir + "/" + name);
          return false;
        }
      }
    }

    // If we get here, parent is not null, and it may or may not allow the child in
    // "name".
    boolean allowed = parent.wildcardType != WildcardType.NONE || parent.getIfAllowed(name) != null;

    // System.out.println((allowed ? "✅ " : "❌ ") + dir + "/" + name);
    
    return allowed;
  }
  
  /**
   * Adds a root. Roots are top level directories in the filesystem that we control
   * access to. The mediator will permit any access outside of a controlled root. 
   * 
   * By default, we only allow 
   * 
   * @param allowedPath
   */
  public void addRoot(String rootPath) {
    roots.add(rootPath);
  }

  /**
   * Adds an allowed path. 
   * @param allowedPath
   */
  public void addAllowedPath(String allowedPath) {
    this.allowedPaths.add(allowedPath);
  }

  /**
   * Returns true if the given path is under the root.
   */
  public boolean isUnderRoot(String path) {
    return path.startsWith(ROOT);
  }
}
