package infomgag.visualization.map;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;

import map.TournamentObserverMapWindow.XMLResource;
import es.csic.iiia.fabregues.dip.board.Game;
import es.csic.iiia.fabregues.dip.board.Province;
import es.csic.iiia.fabregues.dip.board.Region;

public class TournamentWindowPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5871997476424077347L;
	
	private static ArrayList<SupplyCenter> scList = new ArrayList<SupplyCenter>();
	private static ArrayList<Power> powerList = new ArrayList<Power>();
	private static ArrayList<Unit> regionList = new ArrayList<Unit>();
	private static Image background;
	
	public TournamentWindowPanel() {
		
		try {
			background = ImageIO.read(getClass().getClassLoader().getResource("resources/v1-map-SHORT.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.setPreferredSize(new Dimension(800,671));

	}
	
	public void initUnits(){
		if (scList.size() == 0){
			for (Unit u : XMLResource.SUPPLYCENTERS.getUnitList()){
				scList.add((SupplyCenter)u);
			}
		}
		if (powerList.size() == 0){
			for (Unit u : XMLResource.POWERS.getUnitList()){
				powerList.add((Power)u);
			}
		}
		if (regionList.size() == 0){
			for (Unit u : XMLResource.REGIONS.getUnitList()){
				regionList.add(u);
			}
		}
		
		for (SupplyCenter sc : scList){
			sc.visible = true;
		}
	}
	
	public void updateUnits(Game gamestate){
		
		// Updaate visibility and coloring based upon the current gamestate.
		
		// Hide regions;
		for (Unit u : regionList) u.setVisibility(false);
		// Gray out supply centers
		for (SupplyCenter sc : scList) sc.color = Color.GRAY;
		
		// Updates are processed on a per-power basis.
		for (Power p : powerList){
			// Get controlled regions for a power.
			for (Region r : gamestate.getPower(p.label).getControlledRegions()){
				// Slow linear comparison search; finds corresponding region in regionList.
				for (Unit u : regionList){
					// If a region is controlled by this power; make it visible and color it appropriately.
					if (u.label.equals(r.getName())){
						u.setVisibility(true);
						u.setColor(p.getColor());
					}
				}
			}
			
			for (Province pr : gamestate.getPower(p.label).getOwnedSCs()){
				for (SupplyCenter sc : scList){
					if (sc.label.equals(pr.getName())){
						sc.setColor(p.getColor());
						break;
					}
				}
			}
		}
		
		this.repaint();
	}

	public void showAllUnits(){
		// Used to display all regions, so we can debug their locations.
		
		for (int i=0; i< regionList.size(); i++){
			regionList.get(i).setVisibility(true);
			regionList.get(i).setColor(Color.GRAY);
		}
		
	}
	
	public void paintComponent(Graphics g){
		
		// Basic Setup
		super.paintComponent(g);
		
		// Draw background image
		g.drawImage(background, 0, 0, this);
		
		// Draw units
		for (int i=0; i< scList.size(); i++) scList.get(i).drawUnit(g);
		for (int i=0; i< regionList.size(); i++) if (regionList.get(i).getVisibility()) regionList.get(i).drawUnit(g);
	}
	
	public abstract class Unit {
		
		String label = "";
		int x = 0;
		int y = 0;
		int shapesize = 7;
		int fontsize = 12;
		boolean visible = true;
		Color color;
		String icontext = "";
		
		public Unit (int x, int y, Color color){
			
			this.x = x;
			this.y = y;
			this.color = color;
		}
		
		public Unit (int x, int y){
			
			this.x = x;
			this.y = y;
			this.color = Color.GRAY;
			this.visible = false;
		}
		
		public Unit(){
			this.label = "TEST";
		}
		
		public void drawUnit(Graphics g){
			if (icontext == ""){
				g.setColor(this.color);
				g.fillOval(this.x-this.shapesize, this.y-this.shapesize, this.shapesize*2, this.shapesize*2);
				g.setPaintMode();
				g.setColor(Color.black);
				g.drawOval(this.x-this.shapesize, this.y-this.shapesize, this.shapesize*2, this.shapesize*2);
			}else{
				g.setColor(this.color);
				g.fillRect(this.x-this.shapesize, this.y-this.shapesize, this.shapesize*2, this.shapesize*2);
				g.setPaintMode();
				g.setColor(Color.black);
				g.drawRect(this.x-this.shapesize, this.y-this.shapesize, this.shapesize*2, this.shapesize*2);
				g.setFont(new Font("Arial", Font.BOLD, fontsize));
				g.drawString(icontext, this.x - (icontext == "F" ? 2 : 3), this.y + 5);
			}
		}
		
		public void setVisibility(boolean value){
			this.visible = value;
		}
		public boolean getVisibility(){
			return this.visible;
		}
		
		public void setColor(Color c){
			this.color = c;
		}
		public Color getColor(){
			return this.color;
		}
		
		public String toString(){
			return this.getClass().getName() + ", " + this.label + ", x: " + this.x + ", y: " + this.y + ", c: " + this.color.toString();
		}
	}
	
	public class Fleet extends Unit {
		
		public Fleet (int x, int y, String label){
			super(x, y);
			icontext = "F";
			this.label = label;
		}
	}
	
	public class Army extends Unit {
		
		public Army (int x, int y, String label){
			super(x, y);
			icontext = "A";
			this.label = label;
		}
		
	}
	
	public class SupplyCenter extends Unit {
		
		public SupplyCenter (int x, int y, String label){
			super(x, y, Color.GRAY);
			this.label = label;
			this.shapesize = 5;
		}
		
	}
	
	public class Power extends Unit {
		
		public Power (Color red, String label){
			super(0, 0, red);
			this.label = label;
		}
		
	}
}
