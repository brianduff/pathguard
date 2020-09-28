# PathGuard for IntelliJ

This tool regulates file system access from IntelliJ / Android Studio.

The regulator has one or more controlled root paths. It will not interfere with file accesses outside these roots. 

For each directory under a root, including the root itself, there are three permission bits:

  - AllowListFiles: readdir() is permitted to return file children of this directory.
  - AllowListDirs: readdir() is permitted to return directory children of this directory.
  - AllowAll: this behaves like AllowListFiles+AllowListDirs for this directory and all of its child directories recursively. This implies AllowListFiles and AllowListDirs.

  


