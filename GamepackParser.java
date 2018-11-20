import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class GamepackParser {

    public HashMap<String, String> parseParameters() {
        HashMap<String, String> parameters = new HashMap<>();
        try {
            System.out.println("Parsing website");
            final URLConnection urlConnection = new URL("http://oldschool93.runescape.com/l=0/jav_config.ws").openConnection();
            urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36");
            final BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\">'", "\"").replaceAll("'", "").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\"", "").replaceAll(" ", "").replaceAll("param=", "")
                        .replaceAll(";", "").replaceAll("value", "");
                final String[] splitted = line.split("=");
                if (splitted.length == 1) {
                    parameters.put(splitted[0], "");
                } else if (splitted.length == 2) {
                    parameters.put(splitted[0], splitted[1]);
                } else if (splitted.length == 3) {
                    parameters.put(splitted[0], splitted[1] + "=" + splitted[2]);
                } else if (splitted.length == 4) {
                    parameters.put(splitted[0], splitted[1] + "=" + splitted[2] + "=" + splitted[3]);
                }
            }
            return parameters;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public JarFile loadJar() {
        try {
            String gamepackUrl = "http://oldschool93.runescape.com/" + parseParameters().get("initial_jar");

            System.out.println("Downloading gamepack");
            long start = System.currentTimeMillis();

            ReadableByteChannel rbc = Channels.newChannel(new URL(gamepackUrl).openStream());
            FileOutputStream fos = new FileOutputStream("gamepack.jar");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            System.out.println("Gamepack download took " + (System.currentTimeMillis() - start) + "ms");

            return new JarFile(new File("gamepack.jar"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HashMap<String, ClassNode> parseJar(JarFile jar) {
        HashMap<String, ClassNode> classes = new HashMap<>();
        try {
            Enumeration<?> enumeration = jar.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry entry = (JarEntry) enumeration.nextElement();
                if (entry.getName().endsWith(".class")) {
                    ClassReader classReader = new ClassReader(jar.getInputStream(entry));
                    ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    classes.put(classNode.name, classNode);
                }
            }
            return classes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String... args) {
        GamepackParser gamepackParser = new GamepackParser();
        HashMap<String, ClassNode> classes = gamepackParser.parseJar(gamepackParser.loadJar());

        System.out.println("\nClasses found:\n");
        classes.keySet().forEach(System.out::println);
    }
}
