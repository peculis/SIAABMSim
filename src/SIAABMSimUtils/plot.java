package SIAABMSimUtils;

//Class plot uses the package plot to implement graphics.
//Coded by Ricardo Peculis.
//Initial 25 Sep 2009.

import net.quies.math.plot.*;

import java.awt.*;
import java.util.*;
import java.util.Timer;
import java.math.*;
import javax.swing.*;

public class plot extends JFrame {
	
	JFrame frame;
	JPanel panel;
	Graph graph;
	Insets padding;
	JLabel title, dummy;
	ArrayList <String> functionNames;
	ArrayList <Function> functionList;
	ArrayList <ChartStyle> styleList;
	ArrayList <JLabel> labelList;
	String s;
	
	public plot(String name, int x, int y){
		frame = new JFrame(name);
		frame.setLocation(x, y);
		graph = new InteractiveGraph();
		padding = new Insets(50, 130, 50, 50);
		functionNames = new ArrayList<String>();
		functionList = new ArrayList<Function>();
		styleList = new ArrayList<ChartStyle>();
		labelList = new ArrayList<JLabel>();
		title = new JLabel();
		title.setForeground(Color.BLACK);
		title.setText(name);
		title.setVerticalAlignment(SwingConstants.TOP);
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setFont(new Font("Arial",Font.PLAIN, 22));
		dummy = new JLabel();
		dummy.setForeground(Color.WHITE);
		dummy.setText("dummy");
		dummy.setVerticalAlignment(SwingConstants.CENTER);
		dummy.setHorizontalAlignment(SwingConstants.LEFT);
		graph.setPadding(padding);
		graph.setBackground(Color.WHITE);
		graph.add(title);
		graph.validate();
		frame.getContentPane().add(graph);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setSize(680, 400);
	    frame.setBackground(Color.WHITE);
	    frame.validate();
	    frame.setVisible(true);
	}
	
	public void addFunction(String function, Color color){
		Function f;
		ChartStyle s;
		JLabel l, l0;
		f = new Function(function);
		s = new ChartStyle();
		l = new JLabel();
		s.setPaint(color);
		functionNames.add(function);
		functionList.add(f);
		styleList.add(s);
		graph.addFunction(f,s);
		
		l = new JLabel();
		l.setForeground(color);
		l.setText("   " + function);
		l.setFont(new Font("Arial", Font.PLAIN, 15));
		l.setVerticalAlignment(SwingConstants.CENTER);
		l.setHorizontalAlignment(SwingConstants.LEFT);
		graph.add(l);
		graph.validate();
		labelList.add(l);
		l.setLocation(l.getX(), l.getY() + 25 * labelList.size());
	}
	
	public int getNumberOfFunctions(){
		return functionList.size();
	}
	
	public void updateFunction(String function, double x, double y){
		BigDecimal X, Y;
		
		X = new BigDecimal(x);
		Y = new BigDecimal(y);
		for(int f=0; f< functionNames.size(); f++){
			if(functionNames.get(f).equals(function)){
				functionList.get(f).addPoint(X, Y);
			}
		}
		
	}
	
	public void refresh(){
		graph.render();
		graph.repaint();
	}

}
