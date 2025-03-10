package com.kludwisz;

import com.kludwisz.featurefinder.FeatureFinder;
import com.kludwisz.featurefinder.MultiFeatureFinder;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

public class UserInterface {
    private static final String TEXT_ACTIVE = "Find Cool Stuff";
    private static final String TEXT_WORKING = "Working...";

    private static final JTextField seedInput = new JTextField(20);

    private static final ArrayList<JLabel> tpRefs = new ArrayList<>();
    private static final ArrayList<JLabel> feedbackRefs = new ArrayList<>();
    private static final MultiFeatureFinder mff = new MultiFeatureFinder(
            MultiFeatureFinder.getAllFinders(),
            ff -> SwingUtilities.invokeLater(() -> UserInterface.updateFuntion(ff))
    );

    private static void updateFuntion(FeatureFinder f) {
        int index = mff.getFinders().indexOf(f);
        if (index == -1)
            return; // finder is not on the visiblee list, ignore

        Logger.log("Updating UI for finder " + f.name() + " " + f.getFeedbackMessage() + " " + f.getFeatureTPCommand());
        JLabel feedbackLabel = feedbackRefs.get(index);
        JLabel tpLabel = tpRefs.get(index);

        feedbackLabel.setText(f.getFeedbackMessage());
        tpLabel.setText(f.getFeatureTPCommand());
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Feature Finder by Kris");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.X_AXIS));

        // seed input field & label
        JLabel seedLabel = new JLabel("   World Seed:   ");
        inputPanel.add(seedLabel);
        inputPanel.add(seedInput);

        // button to start searching
        JButton searchButton = getCoolStuffButton(frame);
        inputPanel.add(searchButton);

        JPanel finderArray = createFinderArray();

        mainPanel.add(inputPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(finderArray);
        frame.add(mainPanel);
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
                    mff.run();

                    SwingUtilities.invokeLater(() -> {
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
            //Logger.log("Adding finder: " + finder.getClass().getSimpleName());

            JPanel arrayRow = new JPanel();
            arrayRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            arrayRow.setLayout(new GridLayout(1, 4, 0, 0));

            // enable/disable checkbox
            JCheckBox checkBox = new JCheckBox(finder.name());
            checkBox.setSelected(true);
            final int finalIndex = index;
            checkBox.addActionListener(e -> {
                if (checkBox.isSelected()) {
                    Logger.log("Enabling finder " + finalIndex + " -> " + mff.getFinders().get(finalIndex).name());
                    mff.enableFinder(finalIndex);
                    checkBox.setFont(new Font(checkBox.getFont().getName(), Font.BOLD, checkBox.getFont().getSize()));
                } else {
                    Logger.log("Disabling finder " + finalIndex + " -> " + mff.getFinders().get(finalIndex).name());
                    mff.disableFinder(finalIndex);
                    checkBox.setFont(new Font(checkBox.getFont().getName(), Font.ITALIC, checkBox.getFont().getSize()));
                }
            });
            arrayRow.add(checkBox);

            // label displaying feedback message
            JLabel feedbackLabel = new JLabel("-");
            arrayRow.add(feedbackLabel);
            feedbackRefs.add(feedbackLabel);

            // label displaying TP command
            JLabel tpLabel = new JLabel("-");
            arrayRow.add(tpLabel);
            tpRefs.add(tpLabel);

            // button for tp-ing tp command
            JButton tpButton = new JButton("Copy TP Command");

            tpButton.addActionListener(e -> {
                StringSelection stringSelection = new StringSelection(tpLabel.getText());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            });
            arrayRow.add(tpButton);
            index++;

            result.add(arrayRow);
            result.add(Box.createRigidArea(new Dimension(10, 10)));
        }

        // squish everything to the top
        result.add(Box.createRigidArea(new Dimension(0, 2048)));

        return result;
    }
}

