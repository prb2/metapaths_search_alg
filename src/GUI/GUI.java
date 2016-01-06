package GUI;

import Metagraph.MetaRun;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSourceDOT;

import javax.swing.*;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class GUI extends JFrame {

    private final JLabel title = new JLabel("Metagraph Creation and Search");
    private final JTextField fileChooser = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField startField = new JTextField();
    private final JTextField targetField = new JTextField();
    private final JTextField flowField = new JTextField();
    private final JCheckBox pruningCheck = new JCheckBox("Enable pruning");
    private final JCheckBox targetStopCheck = new JCheckBox("Stop searching when target is found");
    private final JButton searchBtn = new JButton("Search");
    private final JButton quitBtn = new JButton("Quit");

    public GUI() {
        initGUI();
    }
    private void initGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 500);

        Container display = getContentPane();

        display.setLayout(new GridLayout(0,1,0,10));

        nameField.setText("Graph Name");
        title.setFont(new Font("Lucida Grande", Font.PLAIN, 20));
        display.add(title);

        fileChooser.setText("graphs/Medium1/medium1.dot");
        display.add(fileChooser);

        nameField.setText("Medium1");
        display.add(nameField);

        startField.setText("0");
        display.add(startField);

        targetField.setText("44");
        display.add(targetField);

        flowField.setText("4");
        display.add(flowField);

        pruningCheck.setSelected(true); // enable pruning by default
        display.add(pruningCheck);

        targetStopCheck.setSelected(false); // enable pruning by default
        display.add(targetStopCheck);

        display.add(searchBtn);
        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MetaRun runner = new MetaRun();

                // Load the input graph
                Graph g = new SingleGraph(nameField.getText());
                FileSourceDOT fs = new FileSourceDOT();
                fs.addSink(g);

                try {
                    fs.readAll(fileChooser.getText());
                }  catch (IOException x) {
                    System.out.println(x);
                }

                // Create the metagraph and write to file
                runner.run(g, g.getId(), startField.getText(), targetField.getText(),
                        Integer.parseInt(flowField.getText()), targetStopCheck.isSelected(),
                        pruningCheck.isSelected());
            }
        });

        display.add(quitBtn);
        quitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    public void start(){
        setVisible(true);
    }

}
