package map;
import java.awt.BorderLayout;
import java.awt.*;
import java.awt.GraphicsConfiguration;

import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import map.TournamentWindowPanel.*;
import es.csic.iiia.fabregues.dip.board.Game;
import es.csic.iiia.fabregues.dip.board.Power;
import es.csic.iiia.fabregues.dip.board.Province;
import es.csic.iiia.fabregues.dip.board.Region;

public class TournamentObserverMapWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public TournamentObserverMapWindow() throws HeadlessException {
		sharedConstructor();
	}

	public TournamentObserverMapWindow(GraphicsConfiguration arg0) {
		super(arg0);
		sharedConstructor();
	}

	public TournamentObserverMapWindow(String arg0) throws HeadlessException {
		super(arg0);
		sharedConstructor();
	}

	public TournamentObserverMapWindow(String arg0, GraphicsConfiguration arg1) {
		super(arg0, arg1);
		sharedConstructor();
	}
	
	public TournamentObserverMapWindow(boolean arg0){
		// Load the class in debug mode. If arg0 is true, we listen for mouse presses and dump the locations (relative to the draw panel) in the console.
		sharedConstructor();
		if (arg0) panel.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				System.out.println("x: " + e.getX() + " - y:" + e.getY());
			}
		});
		
		panel.showAllUnits();
		
	}

	private static TournamentWindowPanel panel;
	private Game currentGameState;
	
	private void sharedConstructor() {
		
		// Frame-specific properties
		this.setTitle("Tournament Observer Map");
		
		// Content panel
		panel = new TournamentWindowPanel();
		
		// Add to components
		this.add(panel, BorderLayout.CENTER);
		
		// Adapt size to contents
		this.pack();
		
		// Show and refresh contents.
		this.setVisible(true);
		panel.initUnits();
		panel.repaint();
	}
	
	
	
	public void updateMap(Game game){
		// Is called whenever the game statistics are updated.
		
		currentGameState = game;
		
		panel.updateUnits(currentGameState);
	}
	
	public void endGameMapCall(){
		// Is called at the end of every game.

	}
	
	public static enum XMLResource {
		POWERS ("power", "resources/powers.xml"),
		PROVINCES ("province", "resources/provinces.xml"),
		REGIONS ("region", "resources/regions.xml"),
		SUPPLYCENTERS ("supplycenter", "resources/supplycenters.xml");
		
		private Document xmlfile;
		private NodeList listofitems;
		private String listtype;
		private String path;
		
		XMLResource(String tagname, String path){
			
			this.listtype = tagname;
			this.path = path;
			
			// Lets find out if we can read an XML-file.
			try {
				this.xmlfile = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getClass().getClassLoader().getResourceAsStream(this.path));
				this.xmlfile.getDocumentElement().normalize();
				this.listofitems = this.xmlfile.getElementsByTagName(listtype);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
				
		public ArrayList<Unit> getUnitList(){
			
			// Decodes the actual XML into the objects we want to populate the list with.
			ArrayList<Unit> output = new ArrayList<Unit>();
			Element elementatindex;
			String[] elementcontents;
			
			// These variables are instantiated on a per-object basis.
			String label;
			int X;
			int Y;
			String[] color;
			
			for (int i = 0; i < this.listofitems.getLength(); i++){
				
				elementatindex = (Element)listofitems.item(i);
				
				if (this.listtype == "power"){
					label = elementatindex.getTextContent().trim().substring(0,3);
					color = elementatindex.getTextContent().trim().substring(3).split(":");
					output.add(panel.new Power(new Color(
							Integer.parseInt(color[0].trim()),
							Integer.parseInt(color[1].trim()),
							Integer.parseInt(color[2].trim())),
							label));
				}
				if (this.listtype == "supplycenter"){
					elementcontents = elementatindex.getTextContent().split("\\s+");
					label =  elementcontents[1].trim();
					X = Integer.parseInt(elementcontents[2]);
					Y = Integer.parseInt(elementcontents[3]);
					output.add(panel.new SupplyCenter(X,Y,label));
				}
				if (this.listtype == "region"){
					// Regions are either army-specific or fleet-specific.
					elementcontents = elementatindex.getTextContent().split("\\s+");
					label = elementcontents[1];
					X = Integer.parseInt(elementcontents[2]);
					Y = Integer.parseInt(elementcontents[3]);
					
					if (label.endsWith("FLT") || label.endsWith("ECS") || label.endsWith("SCS") || label.endsWith("NCS"))
						output.add(panel.new Fleet(X,Y,label));
					else
						output.add(panel.new Army(X,Y,label));
				}
			}
			
			return output;
		}
	}
	
	public void writeOutGameState(){
		
		// Messy crappy spaghetti used to create XML mappings for location markers. Keeping this around as code example to in case we need to get our grubby hands on XML for other uses.
		// Current implementation is to be called at the end of a game and drops the xml files in your home folder.
		
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			
			String path = System.getProperty("user.home") + File.separator;
			
			Element root = doc.createElement("regions");
			doc.appendChild(root);
			
			Element item;
			Element location;
			Element x;
			Element y;
			
			for (Region r : currentGameState.getRegions()){
				// Add this region on newline
				item = doc.createElement("region");
				root.appendChild(item);
				// Set ID in region-tag.
				item.setAttribute("id", Integer.toString(r.getId()));
				// Create children:
				item.appendChild(doc.createElement("name").appendChild(doc.createTextNode(r.getName())));
				
				location = doc.createElement("location");
				item.appendChild(location);
				x = doc.createElement("X");
				y = doc.createElement("Y");
				location.appendChild(x);
				location.appendChild(y);
				x.appendChild(doc.createTextNode("0"));
				y.appendChild(doc.createTextNode("0"));
			}
			
	        TransformerFactory transformerFactory =
            TransformerFactory.newInstance();
            Transformer transformer =
            transformerFactory.newTransformer();
            
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path + "regions.xml"));
            transformer.transform(source, result);
			
			// Repeat for provinces.
			doc = dBuilder.newDocument();
			
			root = doc.createElement("provinces");
			doc.appendChild(root);
			
			for (Province p : currentGameState.getProvinces()){
				// Add this region on newline
				item = doc.createElement("province");
				root.appendChild(item);
				// Set ID in region-tag.
				item.setAttribute("id", Integer.toString(p.getId()));
				// Create children:
				item.appendChild(doc.createElement("name").appendChild(doc.createTextNode(p.getName())));
				
				location = doc.createElement("location");
				item.appendChild(location);
				x = doc.createElement("X");
				y = doc.createElement("Y");
				location.appendChild(x);
				location.appendChild(y);
				x.appendChild(doc.createTextNode("0"));
				y.appendChild(doc.createTextNode("0"));
				
				item.appendChild(location);
			}
			
            source = new DOMSource(doc);
            result = new StreamResult(new File(path + "provinces.xml"));
            transformer.transform(source, result);
            
            // Repeat for powers.
			doc = dBuilder.newDocument();
			
			root = doc.createElement("powers");
			doc.appendChild(root);
			
			for (Power p : currentGameState.getPowers()){
				// Add this region on newline
				item = doc.createElement("power");
				root.appendChild(item);
				// Set ID in region-tag.
				item.setAttribute("id", Integer.toString(p.getId()));
				// Create children:
				item.appendChild(doc.createElement("name").appendChild(doc.createTextNode(p.getName())));
				location = doc.createElement("color");
				item.appendChild(location);
				location.appendChild(doc.createTextNode("color"));
			}
			
            source = new DOMSource(doc);
            result = new StreamResult(new File(path + "powers.xml"));
            transformer.transform(source, result);
			
			// Repeat for supply centers
			doc = dBuilder.newDocument();
			
			root = doc.createElement("supplycenters");
			doc.appendChild(root);
			
			for (Province p : currentGameState.getProvinces()){
				if (p.isSC()) {
					// Add this region on newline
					item = doc.createElement("supplycenters");
					root.appendChild(item);
					// Set ID in region-tag.
					item.setAttribute("id", Integer.toString(p.getId()));
					// Create children:
					item.appendChild(doc.createElement("name").appendChild(doc.createTextNode(p.getName())));
					
					location = doc.createElement("location");
					item.appendChild(location);
					x = doc.createElement("X");
					y = doc.createElement("Y");
					location.appendChild(x);
					location.appendChild(y);
					x.appendChild(doc.createTextNode("0"));
					y.appendChild(doc.createTextNode("0"));
					
					item.appendChild(location);
				}
			}
			
            source = new DOMSource(doc);
            result = new StreamResult(new File(path + "supplycenters.xml"));
            transformer.transform(source, result);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		
		// Used for testing purposes. Runs the map only, displaying all possible locations.
		
		SwingUtilities.invokeLater(new Runnable(){
			
			@Override
			public void run(){
				TournamentObserverMapWindow t = new TournamentObserverMapWindow(true);
				t.setVisible(true);
				
			}
		});
	}
	
}