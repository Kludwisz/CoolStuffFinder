package com.kludwisz.featurefinder;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class UserInterface {
    private static final String TEXT_ACTIVE = "Find Cool Stuff";
    private static final String TEXT_WORKING = "Working...";

    private static final JTextField seedInput = new JTextField(20);
    private static final JLabel outputInfoLabel = new JLabel("-");
    private static final JLabel tpCommandLabel = new JLabel("-");
    private static final JButton copyTpButton = new JButton("Copy TP Command");

    public static void main(String[] args) {
        JFrame frame = new JFrame("Feature Finder by Kris");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.ipadx = 10;
        gbc.ipady = 5;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel wslabel = new JLabel("World Seed");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(wslabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(seedInput, gbc);

        JButton button = getCoolStuffButton(frame);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(button, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(outputInfoLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(tpCommandLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        copyTpButton.addActionListener(e -> {
            StringSelection stringSelection = new StringSelection(tpCommandLabel.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        });
        panel.add(copyTpButton, gbc);

        frame.add(panel);
        frame.setVisible(true);
    }

    private static JButton getCoolStuffButton(JFrame frame) {
        JButton button = new JButton(TEXT_ACTIVE);

        button.addActionListener(e -> {
            button.setEnabled(false);
            button.setText(TEXT_WORKING);

            try {
                long seed = Long.parseLong(seedInput.getText());

                // launch finder on separate thread to avoid freezing the UI
                Thread thread = new Thread(() -> {
                    ObbyFinder finder = new ObbyFinder(seed);
                    String cmd = finder.getFeatureTPCommand();
                    String feedback = finder.getFeedbackMessage();

                    SwingUtilities.invokeLater(() -> {
                        tpCommandLabel.setText(cmd);
                        outputInfoLabel.setText(feedback);
                        button.setEnabled(true);
                        button.setText(TEXT_ACTIVE);
                    });
                });
                thread.start();
            }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid seed", "Error", JOptionPane.ERROR_MESSAGE);
                button.setEnabled(true);
                button.setText(TEXT_ACTIVE);
            }
        });

        return button;
    }
}

