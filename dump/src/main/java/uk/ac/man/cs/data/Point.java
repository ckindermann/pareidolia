package uk.ac.man.cs.data;

public class Point {

    private double X;
    private double Y;

    public Point(double x, double y){
        this.X = x;
        this.Y = y; 
    }

    public double getX(){ 
        return this.X;
    }

    public double getY(){ 
        return this.Y;
    } 
}
