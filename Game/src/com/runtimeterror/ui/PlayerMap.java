package com.runtimeterror.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class PlayerMap extends JFrame {

    private final JLabel mapLocationLbl;

    public PlayerMap() {
        // setting the JFrame up
        setSize(550, 350);
        setResizable(false);
        setTitle("Player Map");
        setLocation(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // creating the map on the frame
        Image roomMap = null;
        try {
            roomMap = ImageIO.read(new File("Game/maps/masterbathroom.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mapLocationLbl = new JLabel(new ImageIcon(roomMap), SwingConstants.CENTER);
        mapLocationLbl.setBounds(0, 450, 500, 260);
        add(mapLocationLbl);
    }

    // getter to post new map based on player movement
    public JLabel getMapLocationLbl() {
        return mapLocationLbl;
    }
}