package org.foraci.mxf.mxfTool.gui;

import org.foraci.mxf.mxfReader.entities.GroupNode;
import org.foraci.mxf.mxfReader.entities.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Captures MXF dump
 */
public class ConsolePanel extends JPanel implements MxfViewListener, ActionListener
{
    private MxfView view;
    private JTextArea textArea;
    private JButton unthreadButton;

    public ConsolePanel(MxfView view) {
        super(new BorderLayout());
        this.view = view;
        createUI();
    }

    private void createUI() {
        unthreadButton = new JButton("unthread");
        unthreadButton.addActionListener(this);
        if (view.isDebug()) {
            add(unthreadButton, BorderLayout.NORTH);
        }
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setTabSize(4);
        add(new JScrollPane(textArea), BorderLayout.CENTER);
    }

    public void dump(String line) {
        // remove null terminators from text so that copy/paste from JTextArea works
        if (line.indexOf("\u0000") != -1) {
            line = line.replaceAll("\u0000", "");
        }
        textArea.append(line + "\n");
    }

    private void printTree(GroupNode parent) {
        for (Node child : parent.getChildren()) {
            textArea.append(child.toString() + "\n");
            if (child instanceof GroupNode) {
                GroupNode group = (GroupNode) child;
                printTree(group);
            }
        }
    }

    public void assetStartLoad() {
        textArea.setText("");
    }

    public void assetLoaded() {

    }

    public void actionPerformed(ActionEvent e) {
        assetStartLoad();
        printTree(view.getRootGroupNode());
    }
}
