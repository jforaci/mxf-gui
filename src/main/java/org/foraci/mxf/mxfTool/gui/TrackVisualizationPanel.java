package org.foraci.mxf.mxfTool.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: jforaci
 * Date: Feb 28, 2012
 * Time: 3:17:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrackVisualizationPanel extends JPanel implements MxfViewListener {
    private MxfView view;
    private TrackVisPanel drawPanel;

    public TrackVisualizationPanel(MxfView view) {
        super(new BorderLayout());
        this.view = view;
        createUI();
    }

    private void createUI() {
        drawPanel = new TrackVisPanel(view);
        JScrollPane sp = new JScrollPane(drawPanel);
        sp.getViewport().setBackground(Color.WHITE);
        add(sp, BorderLayout.CENTER);
    }

    public void assetStartLoad() {
        drawPanel.reset();
    }

    public void assetLoaded() {
        drawPanel.repaint();
    }
}
