package com.kludwisz.featurefinder;

import javax.swing.*;
import java.awt.*;

public class UserInterface {
    private static final String TEXT_ACTIVE = "Find Cool Stuff";
    private static final String TEXT_WORKING = "Working...";

    public static void main(String[] args) {
        // Create a new JFrame
        JFrame frame = new JFrame("Feature Finder by Kris");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);

        // Create a panel to hold the components
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.ipadx = 10;
        gbc.ipady = 5;

        // Create a label and a text field
        JLabel label = new JLabel("World Seed");
        JTextField textField = new JTextField(20);

        // Add label and text field side by side
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(textField, gbc);

        JButton button = getCoolStuffButton(textField, frame);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(button, gbc);

        // Add the panel to the frame
        frame.add(panel);

        // Make the frame visible
        frame.setVisible(true);
    }

    private static JButton getCoolStuffButton(JTextField textField, JFrame frame) {
        JButton button = new JButton(TEXT_ACTIVE);

        button.addActionListener(e -> {
            button.setEnabled(false);
            button.setText(TEXT_WORKING);

            try {
                long seed = Long.parseLong(textField.getText());

                // launch finder on separate thread to avoid freezing the UI
                Thread thread = new Thread(() -> {
                    ObbyFinder finder = new ObbyFinder(seed);
                    String cmd = finder.getFeatureTPCommand();
                });
                thread.start();
            }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid seed", "Error", JOptionPane.ERROR_MESSAGE);
            }

            button.setEnabled(true);
            button.setText(TEXT_ACTIVE);
        });

        return button;
    }
}

