"""
Runtime dependency audit for rope_winch_plus (fixed).
Extract class references from javap -c disassembly ('// pkg/Path.member' comments),
then check each resolves against the real runtime classpath.
"""
import zipfile, os, re, glob, subprocess
from collections import Counter

PROJECT = r"C:\Users\guan\Desktop\我的世界MOD开发\RopeWinchPlus"
CLASSES = os.path.join(PROJECT, "build", "classes", "java", "main")
JAVAP = "D:/jdk-21/bin/javap.exe"

def refs_of(classfile):
    out = subprocess.run([JAVAP, "-p", "-c", classfile],
                         capture_output=True, text=True, errors="replace").stdout
    refs = set()
    # disassembly shows:  // Method a/b/C.m(...)V  |  // Field a/b/C.f:L...;  |  // class a/b/C
    for m in re.finditer(r"// (?:Method|Field|class|InterfaceMethod)\s+([\w/$.]+)", out):
        tok = m.group(1)
        # class part: strip method args / descriptor, then take up to first dot
        tok = tok.split("(")[0]
        if tok.count("/") >= 2:
            refs.add(tok.split(".")[0])
    return refs

my_refs = set()
for root, _, files in os.walk(CLASSES):
    for f in files:
        if f.endswith(".class"):
            my_refs |= refs_of(os.path.join(root, f))

external = {c for c in my_refs if not c.startswith("com/rope_winch_plus")}
external_dots = {c.replace("/", ".") for c in external}

runtime_classes = set()
def add_jar(path):
    try:
        z = zipfile.ZipFile(path)
        for n in z.namelist():
            if n.endswith(".class"):
                runtime_classes.add(n[:-6].replace("/", "."))
    except Exception:
        pass

mods = r"D:\NeoForge_1.21.1-航空学\mods"
libs = os.path.join(PROJECT, "libs")
for d in (libs, mods):
    if os.path.isdir(d):
        for f in os.listdir(d):
            if f.lower().endswith(".jar"):
                add_jar(os.path.join(d, f))

add_jar(r"C:\Users\guan\.gradle\caches\modules-2\files-2.1\net.neoforged\neoforge\21.1.248\9a97bebbd7641685052d59368bb32c56ecb9c913\neoforge-21.1.248-universal.jar")
for j in glob.glob(r"C:\Users\guan\.gradle\caches\ng_execute\**\outputs.jar", recursive=True):
    add_jar(j)
# joml/fastutil bundled in MC — also scan the NeoForm joined outputs already covered.

print("runtime_classes size:", len(runtime_classes))
print("Total external class refs:", len(external_dots))
missing = sorted(c for c in external_dots if c not in runtime_classes)
print("MISSING (NoClassDefFoundError risk):", len(missing))
for m in missing:
    print("   MISSING:", m)

def bucket(c):
    if c.startswith("dev.simulated_team.simulated"): return "simulated(航空学)"
    if c.startswith("com.simibubi.create"): return "create(机械动力)"
    if c.startswith("dev.ryanhcode.sable"): return "sable"
    if c.startswith("net.neoforged"): return "neoforge/fml"
    if c.startswith("net.minecraft"): return "minecraft"
    if c.startswith("org.joml"): return "joml(MC内置)"
    if c.startswith("it.unimi.dsi.fastutil"): return "fastutil(MC内置)"
    return "other"
cnt = Counter(bucket(c) for c in external_dots)
print("\nRef distribution:")
for k, v in cnt.most_common():
    print(f"   {v:4d}  {k}")
print("\nFull external ref list:")
for c in sorted(external_dots):
    print("   ", c)
