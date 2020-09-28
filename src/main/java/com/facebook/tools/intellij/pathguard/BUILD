java_library(
    name = "pathguard",
    srcs = glob(["*.java"]),
    deps = [
        "@maven//:org_ow2_asm_asm",
        "@maven//:org_ow2_asm_asm_tree"
    ]
)

java_binary(
    name ="pathguard_agent",
    runtime_deps = [ ":pathguard" ],
    main_class = "com.facebook.tools.intellij.pathguard.PathGuardAgent",
    deploy_manifest_lines = [
        "Premain-Class: com.facebook.tools.intellij.pathguard.PathGuardAgent",
        "Agent-Class: com.facebook.tools.intellij.pathguard.PathGuardAgent",
        "Can-Redefine-Classes: true",
        "Can-Retransform-Classes: true",
    ]
)

