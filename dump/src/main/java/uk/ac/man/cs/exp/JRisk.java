package uk.ac.man.cs.exp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class JRisk {

    private JFrame mainMap;
    private Polygon poly;

    public JRisk(double[] percentages) {

        //initComponents();
        getXCoordinates(percentages);

    }

    private void initComponents() {

        mainMap = new JFrame();
        mainMap.setResizable(false);

        mainMap.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 

        int xPoly[] = {150, 250, 325, 375, 450, 275, 100};
        int yPoly[] = {150, 100, 125, 225, 250, 375, 300};

        poly = new Polygon(xPoly, yPoly, xPoly.length);
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLUE);
                g.drawPolygon(poly);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(800, 600);
            }
        };
        mainMap.add(p);
        mainMap.pack();
        mainMap.setVisible(true);

    }

    private void getXCoordinates(double[] percentages){

        mainMap = new JFrame();
        mainMap.setResizable(false);

        mainMap.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 

        double x = 1;
        double y = 0;

        double[] xPoly = new double[percentages.length];
        double[] yPoly = new double[percentages.length]; 

        int[] xPoly10 = new int[percentages.length];
        int[] yPoly10 = new int[percentages.length];

        double degree = 0;

        for(int i = 0; i<percentages.length; i++){

            xPoly[i] = percentages[i] * x * Math.cos(degree) - percentages[i] * y * Math.sin(degree) ;
            yPoly[i] = percentages[i] * y * Math.cos(degree) + percentages[i] * x * Math.sin(degree) ; 

            System.out.println("X: " + xPoly[i] + "Y : " + yPoly[i]);

            xPoly10[i] = (int) Math.ceil(xPoly[i] * 100);
            yPoly10[i] = (int) Math.ceil(yPoly[i] * 100);

            System.out.println(Integer.toString(xPoly10[i]) + " " + Integer.toString(yPoly10[i])
                    + "Degree: " + Double.toString(degree) 
                    );
            degree += 2*Math.PI/percentages.length;
        }

        poly = new Polygon(xPoly10, yPoly10, xPoly10.length);



        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLUE);
                g.drawPolygon(poly);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1000, 1000);
            }
        };
        mainMap.add(p);
        mainMap.pack();
        mainMap.setVisible(true); 


        //try
        //{
        //    BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
        //    Graphics2D graphics2D = image.createGraphics();
        //    mainMap.paint(graphics2D);
        //    ImageIO.write(image,"jpeg", new File("/home/chris/Desktop/jmemPractice.jpeg"));
        //}
        //catch(Exception exception)
        //{
        //    ;
        //}

    }

    /**
     * @param args
     */
    public static void main(String[] args) {


        double percentages[] = {0.5, 0.5, 0.8, 0.5, 0.5};

        double perc[] = {0.8,0.7,0.5,0.5,0.2,0.5,0.0,0.5,0.5};



        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new JRisk(perc);
            }
        });
    }
}
