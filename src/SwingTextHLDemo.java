import com.sun.corba.se.impl.ior.iiop.JavaSerializationComponent;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by weigl on 30.06.16.
 */
public class SwingTextHLDemo extends JFrame {

    JPanel root = new JPanel(new BorderLayout());
    Box keywords = new Box(BoxLayout.Y_AXIS);
    JEditorPane editorPane = new JEditorPane();
    JScrollPane pane = new JScrollPane(editorPane);

    String[] labelStrings = {"public", "int", "class", "try", "if", "catch"};
    JLabel[] lbl = new JLabel[labelStrings.length];

    SwingTextHLDemo() throws IOException {
        // Read an Java File just as a demo:
        String content = FileUtils.readFileToString(new File("HelloWorld.java"), "utf-8");

        // create label in the right panel
        for (int i = 0; i < labelStrings.length; i++) {
            lbl[i] = new JLabel(labelStrings[i]);
            lbl[i].setFont(new Font(Font.MONOSPACED, 0, 72));
            keywords.add(lbl[i]);
        }

        // build up ui, center: editor, right: keyword labels
        editorPane.setText(content);
        root.add(pane, BorderLayout.CENTER);
        root.add(keywords, BorderLayout.EAST);
        setContentPane(root);

        SpecialGlassPane glass = new SpecialGlassPane();
        setGlassPane(glass);

        // Bug in Swing, set after GlassPane was added!
        glass.setOpaque(false);
        glass.setVisible(true);
    }


    class SpecialGlassPane extends JComponent {

        SpecialGlassPane() {
        }

        @Override
        protected void paintChildren(Graphics g) {
            // the fun part... I hope this scales

            Graphics2D g2 = (Graphics2D) g;

            // be beautiful ...
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


            // position of the keyword panels
            Point panelLoc = keywords.getLocation();

            // for each keyword do ...
            for (int i = 0; i < labelStrings.length; i++) {
                String keyword = labelStrings[i];

                //label position
                Point loc = lbl[i].getLocation();

                //Better use swing utillieties
                //SwingUtilities.convertRectangle();
                //SwingUtilities.convertPoint()

                // absolute position in frame
                loc.translate(panelLoc.x, panelLoc.y);

                Dimension dim = lbl[i].getSize();
                Rectangle2D.Float rect = new Rectangle2D.Float(loc.x, loc.y, dim.width, dim.height);

                // some colors
                Color line = new Color(("sfklsdföklsdjfsdajfsaklfj" + keyword).hashCode());
                Color fill = new Color(("sfklsdföklsdjfsdajfsaklfj" + keyword).hashCode(), true);

                g2.setColor(line);
                g2.setStroke(new BasicStroke(5.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER));

                g2.draw(rect); //(rect.x,rect.y,rect.width,rect.height, 10,10);

                g2.setStroke(new BasicStroke(1.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER));

                // magic part
                float cX = rect.x; // center x of the label
                float cY = rect.y + rect.height / 2; // center y of the label
                final float BEZIER_X = getWidth()>>3; // bezier margin for control point

                for (Rectangle r : scanText(keyword)) {
                    g2.setColor(line);
                    g2.drawRoundRect(r.x, r.y, r.width, r.height, 5, 5);


                    Path2D.Float path = new Path2D.Float();
                    path.moveTo(r.x + r.width, r.y + (r.height / 2));
                    path.curveTo(
                            r.x + r.width + BEZIER_X, r.y,
                            cX - BEZIER_X, cY,
                            cX, cY
                    );

                    g2.draw(path);

                    g2.setColor(fill);
                    g2.fillRoundRect(r.x, r.y, r.width, r.height, 5, 5);
                }
            }
        }
    }

    public List<Rectangle> scanText(String text) {
        String content = editorPane.getText();
        int found = 0;
        List<Rectangle> list = new ArrayList<>();
        String needle = " " + text + " "; // only match word
        JViewport view = pane.getViewport();

        for (int off = 0; (found = content.indexOf(needle, off)) >= 0; off++) {
            try {
                // translation due to words matching
                Rectangle start = editorPane.modelToView(found + 1);
                Rectangle end = editorPane.modelToView(found + text.length() + 1);
                start.width = end.x - start.x;

                // translate within viewport
                start.translate(
                        -view.getViewPosition().x,
                        -view.getViewPosition().y);

                // if place is not visible
                if(view.getVisibleRect().contains(start))
                    list.add(start);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            off = found + text.length();
        }
        System.out.println(list);
        return list;
    }


    public static void main(String[] args) throws IOException {
        SwingTextHLDemo a = new SwingTextHLDemo();
        a.setSize(500, 500);
        a.setVisible(true);
    }
}
