package org.jboss.as.arquillian.container.managed.archive;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.jboss.as.protocol.StreamUtils;
import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerMessages;

/**
 * @author bloemgracht
 */
public class SimpleTest {

    private static final String TEMP_CONTAINER_DIRECTORY = "arquillian-temp-container";

    @Test
    public void test() throws Exception {
        final File tempContainerDirectory = createTempContainerDirectory();
       // Properties props = new Properties();
        //final File jbossHomeDir = new File(props.getProperty(ServerEnvironment.HOME_DIR));
       // setupCleanDirectories(tempContainerDirectory, jbossHomeDir, props);
    }

    private File createTempContainerDirectory() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File tempContainer = new File(tempDir, TEMP_CONTAINER_DIRECTORY);
        if (tempContainer.exists()) {
            deleteRecursively(tempContainer);
        }
        if (!tempContainer.mkdir()) {
            throw new IOException("Could not create temp directory: " + tempContainer.getAbsolutePath());
        }
        return tempContainer;
    }

    /**
     * Delete a file if exists.
     *
     * @param file the file to delete
     */
    private void deleteRecursively(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                for (String name : file.list()) {
                    deleteRecursively(new File(file, name));
                }
            }
            file.delete();
        }
    }


    private void createCleanContainer() throws IOException {
        System.out.println(System.getProperty("java.io.tmpdir"));
        File arquillianCleanContainer = File.createTempFile("arquillian-clean-container", null);


        System.out.println(arquillianCleanContainer);
        File.createTempFile("run", Long.toString(System.nanoTime()), arquillianCleanContainer);
    }

    static void setupCleanDirectories(File tempRoot, File jbossHomeDir, Properties props) {
        if (tempRoot == null) {
            return;
        }

        File originalConfigDir = getFileUnderAsRoot(jbossHomeDir, props, ServerEnvironment.SERVER_CONFIG_DIR, "configuration", true);
        File originalDataDir = getFileUnderAsRoot(jbossHomeDir, props, ServerEnvironment.SERVER_DATA_DIR, "data", false);

        File configDir = new File(tempRoot, "config");
        configDir.mkdir();
        File dataDir = new File(tempRoot, "data");
        dataDir.mkdir();
        // For jboss.server.deployment.scanner.default
        File deploymentsDir = new File(tempRoot, "deployments");
        deploymentsDir.mkdir();

        copyDirectory(originalConfigDir, configDir);
        if (originalDataDir.exists()) {
            copyDirectory(originalDataDir, dataDir);
        }

        props.put(ServerEnvironment.SERVER_BASE_DIR, tempRoot.getAbsolutePath());
        props.put(ServerEnvironment.SERVER_CONFIG_DIR, configDir.getAbsolutePath());
        props.put(ServerEnvironment.SERVER_DATA_DIR, dataDir.getAbsolutePath());

    }

    private static File getFileUnderAsRoot(File jbossHomeDir, Properties props, String propName, String relativeLocation, boolean mustExist) {
        String prop = props.getProperty(propName, null);
        if (prop == null) {
            prop = props.getProperty(ServerEnvironment.SERVER_BASE_DIR, null);
            if (prop == null) {
                File dir = new File(jbossHomeDir, "standalone" + File.separator + relativeLocation);
                if (mustExist && (!dir.exists() || !dir.isDirectory())) {
                    throw ServerMessages.MESSAGES.embeddedServerDirectoryNotFound("standalone" + File.separator + relativeLocation, jbossHomeDir.getAbsolutePath());
                }
                return dir;
            } else {
                File server = new File(prop);
                validateDirectory(ServerEnvironment.SERVER_BASE_DIR, server);
                return new File(server, relativeLocation);
            }
        } else {
            File dir = new File(prop);
            validateDirectory(ServerEnvironment.SERVER_CONFIG_DIR, dir);
            return dir;
        }

    }


    private static void validateDirectory(String property, File file) {
        if (!file.exists()) {
            throw ServerMessages.MESSAGES.propertySpecifiedFileDoesNotExist(property, file.getAbsolutePath());
        }
        if (!file.isDirectory()) {
            throw ServerMessages.MESSAGES.propertySpecifiedFileIsNotADirectory(property, file.getAbsolutePath());
        }
    }

    private static void copyDirectory(File src, File dest) {
        for (String current : src.list()) {
            final File srcFile = new File(src, current);
            final File destFile = new File(dest, current);

            if (srcFile.isDirectory()) {
                destFile.mkdir();
                copyDirectory(srcFile, destFile);
            } else {
                try {
                    final InputStream in = new BufferedInputStream(new FileInputStream(srcFile));
                    final OutputStream out = new BufferedOutputStream(new FileOutputStream(destFile));

                    try {
                        int i;
                        while ((i = in.read()) != -1) {
                            out.write(i);
                        }
                    } catch (IOException e) {
                        throw ServerMessages.MESSAGES.errorCopyingFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath(), e);
                    } finally {
                        StreamUtils.safeClose(in);
                        StreamUtils.safeClose(out);
                    }

                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
