package com.kludwisz.featurefinder;

import com.kludwisz.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class UserInterface {
    private static final String TEXT_ACTIVE = "Find Cool Stuff";
    private static final String TEXT_WORKING = "Working...";

    private static final JTextField seedInput = new JTextField(20);

    private static final MultiFeatureFinder mff = new MultiFeatureFinder(
            MultiFeatureFinder.getAllFinders(),
            (FeatureFinder f) -> {}
    );

    public static void main(String[] args) {
        JFrame frame = new JFrame("Feature Finder by Kris");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

//        JPanel panel = new JPanel();
//        panel.setLayout(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.ipadx = 10;
//        gbc.ipady = 5;
//        gbc.insets = new Insets(5, 5, 5, 5);
//
//        JLabel wslabel = new JLabel("World Seed");
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.anchor = GridBagConstraints.WEST;
//        panel.add(wslabel, gbc);
//
//        gbc.gridx = 1;
//        gbc.gridy = 0;
//        panel.add(seedInput, gbc);
//
//        JButton button = getCoolStuffButton(frame);
//        gbc.gridx = 0;
//        gbc.gridy = 2;
//        gbc.gridwidth = 2;
//        panel.add(button, gbc);
//
//        gbc.gridx = 0;
//        gbc.gridy = 3;
//        panel.add(outputInfoLabel, gbc);
//
//        gbc.gridx = 0;
//        gbc.gridy = 4;
//        panel.add(tpCommandLabel, gbc);
//
//        gbc.gridx = 0;
//        gbc.gridy = 5;
//        copyTpButton.addActionListener(e -> {
//            StringSelection stringSelection = new StringSelection(tpCommandLabel.getText());
//            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//            clipboard.setContents(stringSelection, null);
//        });
//        panel.add(copyTpButton, gbc);
//        frame.add(panel);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // seed input field & label
        // TODO

        // button to start searching
        // TODO

        JPanel finderArray = createFinderArray();
        frame.add(finderArray);

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
                    mff.setWorldSeed(seed);

                    SwingUtilities.invokeLater(() -> {
                        mff.run();
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

    private static JPanel createFinderArray() {
        JPanel result = new JPanel();
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));

        int index = 0;
        for (FeatureFinder finder : mff.getFinders()) {
            Logger.log("Adding finder: " + finder.getClass().getSimpleName());

            JPanel arrayRow = new JPanel();
            arrayRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            arrayRow.setLayout(new GridLayout(1, 4, 50, 0));

            // enable/disable checkbox
            JCheckBox checkBox = new JCheckBox(finder.name());
            checkBox.setSelected(true);
            final int finalIndex = index;
            checkBox.addActionListener(e -> {
                if (checkBox.isSelected()) {
                    mff.enableFinder(finalIndex);
                } else {
                    mff.disableFinder(finalIndex);
                }
            });
            arrayRow.add(checkBox);

            // label displaying feedback message
            JLabel feedbackLabel = new JLabel("-");
            arrayRow.add(feedbackLabel);

            // label displaying TP command
            JLabel tpLabel = new JLabel("-");
            arrayRow.add(tpLabel);

            // button for tp-ing tp command
            JButton tpButton = new JButton("Copy TP Command");
            // make the button constant-size
            tpButton.setPreferredSize(new Dimension(50, 30));

            tpButton.addActionListener(e -> {
                StringSelection stringSelection = new StringSelection(tpLabel.getText());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            });
            arrayRow.add(tpButton);
            index++;

            result.add(arrayRow);
            result.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // squish everything to the top
        result.add(Box.createRigidArea(new Dimension(0, 4096)));

        return result;
    }
}

