package com.facebook.tools.intellij.pathguard;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class PathGuardAgent {
  public static void premain(String agentArgs, Instrumentation inst) {
    System.out.println("Is redefinition supported?" + inst.isRedefineClassesSupported());

    inst.addTransformer(new ClassFileTransformer() {
      @Override
      public byte[] transform(ClassLoader loader, String className, Class<?> oldClazz, ProtectionDomain domain,
          byte[] classfileBuffer) {

        if ("com/intellij/openapi/vfs/impl/local/DirectoryAccessChecker".equals(className)) {
          System.out.println("Redefining DirectoryAccessChecker!");
          ClassReader reader = new ClassReader(classfileBuffer);
          ClassNode classNode = new ClassNode();
          reader.accept(classNode, Opcodes.ASM8);
          for (MethodNode method : classNode.methods) {
            if ("getFileFilter".equals(method.name)) {
              InsnList instructions = new InsnList();
              instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/facebook/tools/intellij/pathguard/GuardMediator", "getFilter", "()Ljava/io/FilenameFilter;"));
              instructions.add(new InsnNode(Opcodes.ARETURN));
              method.instructions = instructions;
            }
          }

          ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
          classNode.accept(writer);

          return writer.toByteArray();
        }

        return null;       
      }
    });
  }


}