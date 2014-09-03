package viewer;

import java.util.Collections;
import java.util.Comparator;

import com.android.hierarchyviewer.scene.ViewNode;

public class ViewerUtility {

    public static int countFrontWhitespace(String line) {
        int count = 0;
        while (line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }

    public static void loadProperties(ViewNode node, String data) {
        int start = 0;
        boolean stop;

        do {
            int index = data.indexOf('=', start);
            ViewNode.Property property = new ViewNode.Property();
            property.name = data.substring(start, index);

            int index2 = data.indexOf(',', index + 1);
            int length = Integer.parseInt(data.substring(index + 1, index2));
            start = index2 + 1 + length;
            property.value = data.substring(index2 + 1, index2 + 1 + length);
            
            node.properties.add(property);
            node.namedProperties.put(property.name, property);

            stop = start >= data.length();
            if (!stop) {
                start += 1;
            }
        } while (!stop);

        Collections.sort(node.properties, new Comparator<ViewNode.Property>() {
            public int compare(ViewNode.Property source, ViewNode.Property destination) {
                return source.name.compareTo(destination.name);
            }
        });

        node.decode();
    }

}
