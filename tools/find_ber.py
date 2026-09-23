import zipfile

u = r"C:\Users\guan\.gradle\caches\modules-2\files-2.1\net.neoforged\neoforge\21.1.248\9a97bebbd7641685052d59368bb32c56ecb9c913\neoforge-21.1.248-universal.jar"
z = zipfile.ZipFile(u)
targets = ["FMLClientSetupEvent", "FMLEnvironment"]
for n in z.namelist():
    if n.endswith('.class'):
        for t in targets:
            if ('/' + t + '.class') in n:
                print("FOUND", n)
