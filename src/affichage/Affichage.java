package affichage;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import businesslogic.ImageWing;
import businesslogic.Landmark;
import drawing.IDrawable;
import facade.Facade;
import ij.ImagePlus;
import ij.io.Opener;



public class Affichage extends JPanel implements MouseListener, ActionListener, MouseMotionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPopupMenu jpm = new JPopupMenu();

	private JMenuItem trueLandmark = new JMenuItem("True Landmark");      
	private JMenuItem falseLandmark = new JMenuItem("False Landmark");
	public JMenuItem suppLandmark = new JMenuItem("Delete Landmark");


	public static ArrayList<Landmark> ListLandmark = new ArrayList<Landmark>();
	public static ArrayList<Landmark> SelectionLandmark = new ArrayList<Landmark>();
	public static ArrayList<Landmark> UndoListLandmark = new ArrayList<Landmark>();
	public static List<IDrawable> drawables = new LinkedList<IDrawable>();
	public static List<Graphics> graphic = new LinkedList<Graphics>();
	public static HashSet<drawCircle> set = new HashSet<drawCircle>() ; // Utile pour �liminer les doublons
	public static ArrayList<drawCircle> ListCircle = new ArrayList<drawCircle>();
	public static ArrayList<drawCircle> ListTempCircle = new ArrayList<drawCircle>();


	private double X = 0;
	private double Y = 0;

	public int WIDTH = 0;
	public int HEIGHT = 0;
	public int indexOfSelectedLandmark = 0;
	public int indexOfSelectedCircle = 0;
	public static int SelectionLandmarkSize = 0;
	public int size = ListLandmark.size();
	public static int displayLandmark = 0;
	public static int nbTempUndoList = 0;
	public static int UndoListSize = UndoListLandmark.size();
	public static int compteur = SelectionLandmarkSize;


	public float WIDTH2;
	public float HEIGHT2;
	public float temp = 0;
	public static float sliderValue = 100;
	public static float getSliderValue() {
		return sliderValue;
	}

	public String test;
	public String test2;
	public Landmark selectedLandmark;
	public Landmark selectedLandmark2;
	public boolean isCtrlDown = false;


	public drawCircle selectedCircle;

	private ImageWing im;

	BufferedImage monImage;
	MouseEvent e;




	public Affichage(ImageWing im) {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		this.im = im;

		ListLandmark = im.getLandmarks();
		sliderValue = JSlidePanel.sliderValue;

		this.addMouseListener(this);
	
		this.addMouseMotionListener(this);
		this.setFocusable(true);
		this.addKeyListener( new KeyListener() {

			
			public void keyTyped(KeyEvent e) {
				System.out.println("TEST CONTROL");

			}

			public void keyReleased(KeyEvent e) {

				if(e.getKeyCode() == KeyEvent.VK_CONTROL){
					isCtrlDown=false;
					System.out.println("TEST CONTROL Release");
				}
			}

			public void keyPressed(KeyEvent e) {

				if(e.getKeyCode() == KeyEvent.VK_CONTROL){
					isCtrlDown=true;
					System.out.println("TEST CONTROL");
				}

			}
		});


		trueLandmark.addActionListener((ActionListener)this);
		falseLandmark.addActionListener((ActionListener)this);
		suppLandmark.addActionListener((ActionListener)this);
		setLayout(null);


		this.addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent event){

				if(event.isPopupTrigger()){       


					// jpm.add(new Landmark(event.getX(), event.getY(), true));
					jpm.add(trueLandmark);
					jpm.add(falseLandmark);
					jpm.add(suppLandmark);
					// jpm.add(new LandMark(e.getX(), e.getY(), true));  

					X = event.getX();
					Y = event.getY(); 

					jpm.show(Cadre2.panneau, event.getX(), event.getY());
				}
			}
		});



	}



	public static void UndoLandmark(){


		if(nbTempUndoList < UndoListSize ){

			int tmp = UndoListSize - nbTempUndoList;

			for(int i =0 ; i< tmp; i++){

				UndoListLandmark.add(ListLandmark.get(ListLandmark.size() -1));
				ListLandmark.remove(ListLandmark.get(ListLandmark.size() -1));

				nbTempUndoList--;
			}
		}

		else if(SelectionLandmark.size() != 0){

			ListLandmark.add(SelectionLandmark.get(SelectionLandmark.size() -1));
			UndoListLandmark.add(SelectionLandmark.get(SelectionLandmark.size() -1));

			SelectionLandmark.remove(SelectionLandmark.get(SelectionLandmark.size() -1));
			System.out.println("SelectionLandmarkSize = "+SelectionLandmarkSize);
		}
		else {

			if(ListLandmark.size() ==0 && SelectionLandmark.size() == 0){
				JOptionPane.showMessageDialog(null, "You have nothing to Undo", "Warning", JOptionPane.WARNING_MESSAGE);

			} else

				UndoListLandmark.add(ListLandmark.get(ListLandmark.size() -1));
			ListLandmark.remove(ListLandmark.get(ListLandmark.size() -1));
			System.out.println("SelectionLandmarkSize = "+SelectionLandmarkSize);
		}


	}



	public static void RedoLandmark() {

		if(UndoListLandmark.size() == 0){
			JOptionPane.showMessageDialog(null, "You have nothing to Redo", "Warning", JOptionPane.WARNING_MESSAGE);

		}
		else{
			if(SelectionLandmarkSize != 0 && SelectionLandmarkSize > UndoListLandmark.size() ){

				while (compteur != 0) {
					// Tant que la liste de s�l�ction n'est pas vide on la d�cr�mente 
					//Sinon on sortirai trop t�t de ce cas 
					SelectionLandmark.add(UndoListLandmark.get(SelectionLandmarkSize));
					UndoListLandmark.remove(UndoListLandmark.get(UndoListLandmark.size() -1));
					compteur  = compteur-1;
					System.out.println("SelectionLandmarkSize = "+SelectionLandmarkSize);
				} 
				SelectionLandmarkSize = 0;
			}
			else if ( UndoListLandmark.size() - SelectionLandmarkSize > 0){

				ListLandmark.add(UndoListLandmark.get(UndoListLandmark.size() -1));
				UndoListLandmark.remove(UndoListLandmark.get(UndoListLandmark.size() -1));
				System.out.println("SelectionLandmarkSize = "+SelectionLandmarkSize);
			}
			else {

				ListLandmark.add(UndoListLandmark.get(UndoListLandmark.size() -1));
				UndoListLandmark.remove(UndoListLandmark.get(UndoListLandmark.size() -1));
				System.out.println("SelectionLandmarkSize = "+SelectionLandmarkSize);
			}
		}
	}



	public void addDrawable(IDrawable d) {
		drawables.add(d);
		repaint();
	}




	public static void addLandMark(double x, double y , boolean B){

		//	String texte = PanelData.jText.getText();
		PanelData.jText.setText(Cadre2.ListLandmarkTemp.toString()+ "\n");
		//PanelData.jText.setText(texte+"\n Landmark n�"+i+" X = "+ListLandmark.get(i).getPosX()+ " Y = "+ListLandmark.get(i).getPosY()+ " Type = " +ListLandmark.get(i).getIsLandmark());


	}


	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
	/*	for(int i = 0 ; i< Cadre2.ListLandmarkTemp.size(); i++){
			System.out.println("L_TEMP = " +Cadre2.ListLandmarkTemp.get(i).toString());
		}
	*/

		test = im.getProperties().get("WIDTH");
		test2 = im.getProperties().get("HEIGHT");
		// Les donn�es sont des String donc on les convertis
		WIDTH2 = Float.parseFloat(test);
		HEIGHT2 = Float.parseFloat(test2);


		float value = 0;
		value = JSlidePanel.getSliderValue();

		if( value != temp){
			if(value < temp){
				reduireImage(value);
				temp = value;

			}else if(value > temp)
			{
				agrandirImage(value);
				temp= value;

			}


		}

	

		addLandMark(0,0,true);
	
		g.drawImage(monImage, 0 ,0 , null);
		
		WIDTH = monImage.getWidth();
		HEIGHT = monImage.getHeight();
		//System.out.println("WIDTH = "+WIDTH+" HAIGHT =  "+HEIGHT);
		// On r�cup�re la Taille de l'image Via ses m�tadata



		// On r�cup�re la taille de l'image affich�e � l'�cran, redimensionn�e ou non

		if( ListLandmark.size() != size ){

			for(int i = 0; i<ListLandmark.size() ; i++){

				float XX = ListLandmark.get(i).getPosX();
				float YY = ListLandmark.get(i).getPosY();

				// R�cup�rer le poucentage de diff�rence entre l'image redimensionn� et l'image originale

				float width = WIDTH / WIDTH2;
				float height = HEIGHT / HEIGHT2;

				if ( width != 1 && height !=1) {

					XX = XX * width;      // Utile pour conserver les position malgr� un redimensionnement
					YY = YY * height;


					boolean isLandmark = ListLandmark.get(i).getIsLandmark();

    
					ListTempCircle.add(new drawCircle(g,(int) XX,(int) YY, 5, isLandmark,0,displayLandmark));
				
					set.addAll(ListTempCircle) ;
					set.clone();

					ListCircle = new ArrayList<drawCircle>(set);

					repaint();
				}
				else {

					XX = (XX / WIDTH2)*100; 
					YY = (YY / HEIGHT2)*100;

					float X2 = (XX * WIDTH)/100;
					float Y2 = (YY * HEIGHT)/100;

					boolean isLandmark = ListLandmark.get(i).getIsLandmark();


					ListTempCircle.add(new drawCircle(g,(int) X2,(int) Y2, 5, isLandmark,0,displayLandmark));
					set.addAll(ListTempCircle) ;
					
					set.clone();
					ListCircle = new ArrayList<drawCircle>(set);
					repaint();
				}
			}

			size = ListLandmark.size();

		}

		for(int i = 0; i<ListLandmark.size() ; i++){

			//System.out.println(" Boucle for pour dessiner des p*** de cercle, : taille = "+ListLandmark.size());
			float XX = ListLandmark.get(i).getPosX();
			float YY = ListLandmark.get(i).getPosY();
			XX = (XX / WIDTH2)*100; 
			YY = (YY / HEIGHT2)*100;
			float X2 = (XX * WIDTH)/100;
			float Y2 = (YY * HEIGHT)/100;
			boolean isLandmark = ListLandmark.get(i).getIsLandmark();
			new drawCircle(g,(int) X2,(int) Y2, 5, isLandmark,0,displayLandmark);

		}

		for(int i = 0 ; i<SelectionLandmark.size(); i++) {

			float X = SelectionLandmark.get(i).getPosX();
			float Y = SelectionLandmark.get(i).getPosY();

			float width = WIDTH / WIDTH2;
			float height = HEIGHT / HEIGHT2;

			if ( width != 1 && height !=1) {
				X = X * width;
				Y = Y * height;


				boolean isLandmark = SelectionLandmark.get(i).getIsLandmark();


				ListTempCircle.add(new drawCircle(g,(int) X,(int) Y, 5, isLandmark, 1 , displayLandmark));
				set.addAll(ListTempCircle) ;
				set.clone();
				ListCircle = new ArrayList<drawCircle>(set);
				repaint();
			}
			else {

				X = (X / WIDTH2)*100; 
				Y = (Y / HEIGHT2)*100;

				float X2 = (X * WIDTH)/100;
				float Y2 = (Y * HEIGHT)/100;

				boolean isLandmark = SelectionLandmark.get(i).getIsLandmark();

				ListTempCircle.add(new drawCircle(g,(int) X2,(int) Y2, 5, isLandmark,1,displayLandmark));
				set.addAll(ListTempCircle) ;
				set.clone();
				ListCircle = new ArrayList<drawCircle>(set);
				repaint();
			}

		}
		repaint();

	}




	public int getValue(JSlider slide) {
		int val = slide.getValue();


		return val;

	}

	public static Mat bufferedImageToMat(BufferedImage in) {
		/*	System.out.println("Etape 2.1");
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		System.out.println("Etape 2.2");
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		System.out.println("Etape 2.3");
		mat.put(0, 0, data);
		System.out.println("Etape 2.4");
		 */
		Mat out;
		byte[] data;
		int r, g, b;

		if(in.getType() == BufferedImage.TYPE_INT_RGB)
		{
			out = new Mat(1728, 1296, CvType.CV_8UC3);
			data = new byte[1728 * 1296* (int)out.elemSize()];
			int[] dataBuff = in.getRGB(0, 0,320,240, null, 0, 320);
			for(int i = 0; i < dataBuff.length; i++)
			{
				data[i*3] = (byte) ((dataBuff[i] >> 16) & 0xFF);
				data[i*3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
				data[i*3 + 2] = (byte) ((dataBuff[i] >> 0) & 0xFF);
			}
		}
		else
		{
			out = new Mat(1728, 1296, CvType.CV_8UC1);
			data = new byte[1728*1296* (int)out.elemSize()];
			int[] dataBuff = in.getRGB(0, 0, 1728, 1296, null, 0, 320);
			for(int i = 0; i < dataBuff.length; i++)
			{
				r = (byte) ((dataBuff[i] >> 16) & 0xFF);
				g = (byte) ((dataBuff[i] >> 8) & 0xFF);
				b = (byte) ((dataBuff[i] >> 0) & 0xFF);
				data[i] = (byte)((0.21 * r) + (0.71 * g) + (0.07 * b)); //luminosity
			}
		}
		out.put(0, 0, data);

		return out;



		//return mat;
	}

	public static BufferedImage mat2Img(Mat in)
	{
		BufferedImage out;
		byte[] data = new byte[1728 * 1296 * (int)in.elemSize()];
		int type;
		in.get(0, 0, data);

		if(in.channels() == 1)
			type = BufferedImage.TYPE_BYTE_GRAY;
		else
			type = BufferedImage.TYPE_3BYTE_BGR;

		out = new BufferedImage(1728, 1296, type);

		out.getRaster().setDataElements(0, 0, 1728, 1296, data);
		return out;
	} 



	static boolean resizeIfNeeded(Mat img, int desiredWidth, int desiredHeight) {
		Size size = img.size();
		Size desiredSize = new Size(desiredWidth, desiredHeight);
		if (size.width != desiredWidth || size.height != desiredHeight) {
			Imgproc.resize(img, img, desiredSize);
			return true;
		}
		return false;
	}

	protected void reduireImage(float sliderValue)
	{


		/*	System.out.println("R2DUIRE");
		System.out.println("WIDTH2 "+WIDTH2+ "  HEINGHT2  "+HEIGHT2);

		sliderValue = sliderValue/100;
		System.out.println("slidervalue "+sliderValue);
		 */

		if(sliderValue > 0){

			/*		System.out.println("Etape 1");
			Mat imgMat;
			System.out.println("Etape 2");		

			imgMat =bufferedImageToMat(monImage);
			System.out.println("Etape 3");

		//	resizeIfNeeded(imgMat,(int) (monImage.getWidth() * 0.9), (int)(monImage.getHeight()*0.9));
			 System.out.println("Etape 4");
			monImage=  mat2Img(imgMat);
			System.out.println("Etape 5");
			 */	



			BufferedImage imageReduite = new BufferedImage((int) (monImage.getWidth()*0.9) ,(int) (monImage.getHeight()*0.9), monImage.getType());
			AffineTransform reduire = AffineTransform.getScaleInstance(0.9, 0.9);
			int interpolation = AffineTransformOp.TYPE_BICUBIC;
			AffineTransformOp retaillerImage = new AffineTransformOp(reduire, interpolation);
			retaillerImage.filter(monImage, imageReduite );


			monImage = imageReduite ;

			System.out.println("Etape 6");
			repaint();
			System.out.println("Etape 7");
		}


	}


	protected void agrandirImage(float sliderValue)
	{

		sliderValue = 0;


		BufferedImage imageZoomer = new BufferedImage((int) (monImage.getWidth()*1.1)  ,(int)(monImage.getHeight()*1.1), monImage.getType());
		AffineTransform agrandir = AffineTransform.getScaleInstance(1.1,1.1);
		int interpolation = AffineTransformOp.TYPE_BICUBIC;
		AffineTransformOp retaillerImage = new AffineTransformOp(agrandir, interpolation);
		retaillerImage.filter(monImage, imageZoomer );
		monImage = imageZoomer ;
		repaint();
	}




	protected void ajouterImage(File fichierImage)
	{  

		String result = fichierImage.getAbsolutePath().toString();
		ImagePlus imagePlus = new Opener().openTiff(result, "");
		BufferedImage bufferedImage = imagePlus.getBufferedImage();


		System.out.println("Chargement image dans la fonction Ajouter Image");
		System.out.println("File ajouterImage : "+fichierImage);
		monImage = bufferedImage;

		System.out.println("Image 2 : "+monImage);
	}




	protected BufferedImage getImagePanneau()
	{      // r�cup�rer une image du panneau
		int width  = this.getWidth();
		int height = this.getHeight();
		BufferedImage image = new BufferedImage(width, height,  BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();

		this.paintAll(g);
		g.dispose();
		return image;
	}

	protected void enregistrerImage(File fichierImage)
	{
		String format ="JPG";
		BufferedImage image = getImagePanneau();
		try {
			ImageIO.write(image, format, fichierImage);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected void enregistrerImage2(BufferedImage image2, File fichierImage)
	{
		String format ="JPG";

		BufferedImage image = image2;
		try {
			ImageIO.write(image, format, fichierImage);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}





	@Override
	public void mouseClicked(MouseEvent e) {


		addLandMark(e.getX(), e.getY(), true);
		System.out.println("X  : "+e.getX()+ " Y = "+e.getY());

		
	}

	public int TESTX;
	public int TESTY;

	@Override
	public void mouseReleased(MouseEvent e) {

		TESTX = e.getX();
		TESTY = e.getY();
		System.out.println(" mouseRelease X = "+e.getX()+ " Y = "+e.getY());
		selectedCircle = null;
		selectedLandmark = null;
		selectedLandmark2 = null;


	}



	@Override
	public void mouseMoved(MouseEvent e) {



	}




	@Override
	public void mouseEntered(MouseEvent e) {
		

	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		System.out.println(" mouseDragged X = "+e.getX()+ " Y = "+e.getY());
		if(selectedCircle != null || selectedLandmark != null || selectedLandmark2 != null) {

			float width = WIDTH / WIDTH2;
			float height = HEIGHT / HEIGHT2;

			selectedCircle.setxCenter((int) (e.getX()/ width));
			selectedCircle.setyCenter((int) (e.getY()/ height));

			selectedLandmark.setPosX((int) (e.getX()/ width));
			selectedLandmark.setPosY((int) (e.getY()/ height));
			
		//	selectedLandmark2.setPosX((int) (e.getX()/ width));
		//	selectedLandmark2.setPosY((int) (e.getY()/ height));
		}
		repaint();

	}





	@Override
	public void mousePressed(MouseEvent e) {

		if(isCtrlDown == true) {

			System.out.println("CTRL PRESSED !!!!!");
			for(int j = 0; j<ListLandmark.size() ; j++){

				float width = WIDTH / WIDTH2;
				float height = HEIGHT / HEIGHT2;

				float moinsX = e.getX()-9;
				float plusX = e.getX()+9;

				float moinsY = e.getY()-9;
				float plusY = e.getY()+9;

				float LAND = ListLandmark.get(j).getPosX();
				float LAND2 = ListLandmark.get(j).getPosY();


				if( (moinsX/width) < LAND && LAND < (plusX/width)  && (moinsY / height) < LAND2 && LAND2 < (plusY/height) ){
					nbTempUndoList = ListLandmark.size();

					SelectionLandmark.add(ListLandmark.get(j));
					SelectionLandmarkSize = SelectionLandmarkSize+1;
					System.out.println("SelectionLandmarkSize = "+SelectionLandmarkSize);
					ListLandmark.remove(j);

					repaint();
				}

			}

		}
		else {
			float width = WIDTH / WIDTH2;
			float height = HEIGHT / HEIGHT2;

		

					
			int i = 0;
			for(int j = 0; j<ListCircle.size() ; j++){

				for( i = 0; i<ListLandmark.size(); i++){



					float moinsX = e.getX()-9;
					float plusX = e.getX()+9;

					//System.out.println("Size ListLandmark : " +ListLandmark.size());

					float moinsY = e.getY()-9;
					float plusY = e.getY()+9;

					float LAND = ListLandmark.get(i).getPosX();
					float LAND2 = ListLandmark.get(i).getPosY();


					if( (moinsX/ width) < LAND && LAND < (plusX/height)  &&( moinsY/ width) < LAND2 && LAND2 < (plusY/height) /*&& CIRCL == LAND && CIRCL2 == LAND2*/){
						


						//System.out.println(" mousePressed2  X = "+e.getX()+ " Y = "+e.getY()+ " I = "+i+ " J = " +j+ " LISTCIRCLE = "+ListCircle.size());
						selectedCircle = ListCircle.get(j);
						selectedLandmark = ListLandmark.get(i);
				//		selectedLandmark2 = Cadre2.ListLandmark.get(i);

						indexOfSelectedLandmark = i;
						indexOfSelectedCircle = j;


						ListLandmark.get(i).setPosX(TESTX);
						ListLandmark.get(i).setPosY(TESTY);
						
				//		Cadre2.ListLandmark.get(i).setPosX(TESTX);
				//		Cadre2.ListLandmark.get(i).setPosY(TESTY);
						
						ListCircle.get(j).setxCenter(TESTX);
						ListCircle.get(j).setyCenter(TESTY);
						repaint();

					
					}
				}
			}
		}
	}

	public boolean ChangeTypeLandmark(boolean b ){

		
		for(int j = 0; j<ListLandmark.size() ; j++){

			float width = WIDTH / WIDTH2;
			float height = HEIGHT / HEIGHT2;

			float moinsX = (float) (X-9);
			float plusX = (float) (X+9);
			System.out.println("X = "+X/width);
			float moinsY = (float) (Y-9);
			System.out.println("Y = "+Y/height);
			float plusY = (float) (Y+9);

			float LAND = ListLandmark.get(j).getPosX();
			float LAND2 = ListLandmark.get(j).getPosY();
			System.out.println("Land = "+LAND);
			System.out.println("Land2 = "+LAND2);
		

			if( (moinsX/width) < LAND && LAND < (plusX/width)  && (moinsY / height) < LAND2 && LAND2 < (plusY/height) ){
				nbTempUndoList = ListLandmark.size();

				ListLandmark.get(j).setIsLandmark(b);
				
				repaint();
				System.out.println(" Return B = "+b);
				return b;
			}

		} 
		if ( b == true){
			// Utile si on ne rentre pas dans la boucle if, on retourne l'inverse de b pour que l'ajout de landmark se fasse normalement
			return false;

		}else if( b == false) {

			return true;
		}
		System.out.println("End Function");
		return false;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		WIDTH = monImage.getWidth();
		HEIGHT = monImage.getHeight();


		test = im.getProperties().get("WIDTH");
		test2 = im.getProperties().get("HEIGHT");
		// Les donn�es sont des String donc on les convertis
		float WIDTHREAL = Float.parseFloat(test);
		float HEIGHTREAL = Float.parseFloat(test2);


		if (e.getSource().equals(trueLandmark))
		{

			boolean type = ChangeTypeLandmark(true);
			System.out.println(" Type = "+type);
			if (type == true){
				System.out.println("State changed");

			}else {

				//	X = (X/WIDTH);
				//	Y = (Y/HEIGHT);

				X = (X/WIDTH);
				Y = (Y/HEIGHT);
				if(X> 1 || Y > 1) {

					JOptionPane.showMessageDialog(null, "You are not on the image", "Attention", JOptionPane.WARNING_MESSAGE);
				}

				else if( X <=1 || Y <= 1){

					if ( WIDTH != WIDTHREAL && HEIGHT != HEIGHTREAL){

						float tempX = WIDTH / WIDTHREAL;
						float tempY = HEIGHT / HEIGHTREAL;

						X = (X * WIDTH);     //Passage en pixels
						Y = (Y * HEIGHT);	 //Passage en pixels

						X = X /tempX;		 
						Y = Y /tempY;
						System.out.println(" ADD LANDMARK ");
						ListLandmark.add(new Landmark((float)X,(float) Y, true));
						
						Facade.addLandmark(im, ListLandmark);

					}else{


						X = (X * WIDTH); 
						Y = (Y * HEIGHT);
						addLandMark(X, Y, true);
						System.out.println("2eme : " +X );
						ListLandmark.add(new Landmark((float)X,(float) Y, true));
				


						Facade.addLandmark(im, ListLandmark);

						System.out.println("True = "+X+ "  "+Y);
					}
				}
			}
		}


		if (e.getSource().equals(falseLandmark))
		{
			System.out.println("FALSE ");
			boolean type = ChangeTypeLandmark(false);
			System.out.println(" Type = "+type);
			if (type == false){
				System.out.println("State changed");

			}else {

				X = (X/WIDTH);
				Y = (Y/HEIGHT); 

				if(X> 1 || Y > 1) {


					JOptionPane.showMessageDialog(null, "You are not in the image", "Attention", JOptionPane.WARNING_MESSAGE);

				}
				else if( X <=1 || Y <= 1){

					if ( WIDTH != WIDTHREAL && HEIGHT != HEIGHTREAL){

						float tempX = WIDTH / WIDTHREAL;
						float tempY = HEIGHT / HEIGHTREAL;

						X = (X * WIDTH); 
						Y = (Y * HEIGHT);

						X = X /tempX;
						Y = Y /tempY;
						ListLandmark.add(new Landmark((float)X,(float) Y, false));
						
						Facade.addLandmark(im, ListLandmark);

					}else{


						X = (X * WIDTH); 
						Y = (Y * HEIGHT);

						ListLandmark.add(new Landmark((float) X, (float) Y, false));	
						
						Facade.addLandmark(im, ListLandmark);

						System.out.println("False = "+X+ "  "+Y);

					}
				}
			} 

			if (e.getSource().equals(suppLandmark)){

				System.out.println("WOAW");
				if(SelectionLandmark.size() != 0){

					for(int i=0; i<SelectionLandmark.size(); i++){
						SelectionLandmark.remove(i);
						SelectionLandmark.removeAll(SelectionLandmark);
						SelectionLandmarkSize =0;
						System.out.println("SelectionLandmarkSize = "+SelectionLandmarkSize);
						repaint();

					}

				}else{

					ListLandmark.remove(indexOfSelectedLandmark);
					
					SelectionLandmarkSize = SelectionLandmarkSize -1;
					System.out.println("SelectionLandmarkSize = "+SelectionLandmarkSize);
					repaint();
				}
			}

		}
	}
}

