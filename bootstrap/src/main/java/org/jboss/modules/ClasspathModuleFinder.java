package org.jboss.modules;

import org.wildfly.swarm.bootstrap.util.Layout;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Bob McWhirter
 */
public class ClasspathModuleFinder implements ModuleFinder {

    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        final String path = "modules/" + identifier.getName().replace('.', '/') + "/" + identifier.getSlot() + "/module.xml";

        try {
            ClassLoader cl = Layout.getBootstrapClassLoader();
            InputStream in = cl.getResourceAsStream(path);

            if (in == null && cl != ClasspathModuleFinder.class.getClassLoader() ) {
                in = ClasspathModuleFinder.class.getClassLoader().getResourceAsStream(path);
            }

            if ( in == null ) {
                return null;
            }

            ModuleSpec moduleSpec = null;
            try {
                moduleSpec = ModuleXmlParser.parseModuleXml(new ModuleXmlParser.ResourceRootFactory() {
                    @Override
                    public ResourceLoader createResourceLoader(final String rootPath, final String loaderPath, final String loaderName) throws IOException {
                        return Environment.getModuleResourceLoader(rootPath, loaderPath, loaderName);
                    }
                }, "/", in, path.toString(), delegateLoader, identifier);

            } catch (IOException e) {
                throw new ModuleLoadException(e);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new ModuleLoadException(e);
                }
            }
            return moduleSpec;
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(99);
            return null;

        } finally {
            System.err.println( "DONE LOAD" );
            System.out.println("DONE LOAD");
        }

    }
}
