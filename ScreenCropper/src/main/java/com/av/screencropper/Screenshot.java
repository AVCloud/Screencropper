package com.av.screencropper;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

public class Screenshot extends JLabel implements HotkeyListener {
	Rectangle currentRect = null;
	Rectangle rectToDraw = null;
	Rectangle previousRectDrawn = new Rectangle();
	static JFrame sw;
	static Screenshot sc;

	// private static String path =
	// WindowsUtils.getCurrentUserDesktopPath().replace("\\",
	// "\\\\")+"\\\\Screenshot.jpg";
	private static String path = DesktopHelper.OSTypeDesktopPath().replace(
			"\\", "\\\\");

	public Screenshot(String filePath) {

		ImageIcon icon = new ImageIcon(filePath);
		icon.getImage().flush();
		setIcon(icon);
		setOpaque(true);
		setAlignmentX(Component.LEFT_ALIGNMENT);
		MyListener myListener = new MyListener(filePath);
		addMouseListener(myListener);
		addMouseMotionListener(myListener);

	}

	private class MyListener extends MouseInputAdapter {
		String filePath;

		MyListener(String filePath) {
			this.filePath = filePath;

		}

		public void mousePressed(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			currentRect = new Rectangle(x, y, 0, 0);
			updateDrawableRect(getWidth(), getHeight());
			repaint();
		}

		public void mouseDragged(MouseEvent e) {
			updateSize(e);
		}

		public void mouseReleased(MouseEvent e) {
			updateSize(e);
			CropImage(filePath);
		}

		/*
		 * Update the size of the current rectangle and call repaint. Because
		 * currentRect always has the same origin, translate it if the width or
		 * height is negative.
		 * 
		 * For efficiency (though that isn't an issue for this program), specify
		 * the painting region using arguments to the repaint() call.
		 */
		void updateSize(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			currentRect.setSize(x - currentRect.x, y - currentRect.y);
			updateDrawableRect(getWidth(), getHeight());
			Rectangle totalRepaint = rectToDraw.union(previousRectDrawn);
			repaint(totalRepaint.x, totalRepaint.y, totalRepaint.width,
					totalRepaint.height);
		}
	}

	private void CropImage(String filePath) {
		File f = new File(filePath);
		try {
			BufferedImage img = ImageIO.read(f);
			final BufferedImage imgOriginal = img.getSubimage(rectToDraw.x,
					rectToDraw.y, rectToDraw.width - 1, rectToDraw.height - 1);
			ImageIO.write(imgOriginal, "jpg", new File(filePath));
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					CopyImagetoClipBoard Cic = new CopyImagetoClipBoard();
					Cic.toClipBoard(imgOriginal);
				}
			});
			t1.run();
			sw.dispose();
			// System.exit(1);
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	protected void paintComponent(Graphics g) {
		// this //paints the background and image
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		// If currentRect exists, paint a box on top.
		if (currentRect != null) {

			// Draw a rectangle on top of the image.
			// g2.setStroke(new BasicStroke(5));
			// g2.setXORMode(Color.white ); //Color of line varies
			// depending on image colors
			g2.setColor(Color.red);

			// AlphaComposite ac =
			// AlphaComposite.getInstance(AlphaComposite.SRC_IN,1f);
			// g2.setComposite(ac);

			g2.drawRect(rectToDraw.x, rectToDraw.y, rectToDraw.width - 1,
					rectToDraw.height - 1);

			// controller.updateLabel(rectToDraw);
		}
	}

	private void updateDrawableRect(int compWidth, int compHeight) {
		int x = currentRect.x;
		int y = currentRect.y;
		int width = currentRect.width;
		int height = currentRect.height;

		// Make the width and height positive, if necessary.
		if (width < 0) {
			width = 0 - width;
			x = x - width + 1;
			if (x < 0) {
				width += x;
				x = 0;
			}
		}
		if (height < 0) {
			height = 0 - height;
			y = y - height + 1;
			if (y < 0) {
				height += y;
				y = 0;
			}
		}

		// The rectangle shouldn't extend past the drawing area.
		if ((x + width) > compWidth) {
			width = compWidth - x;
		}
		if ((y + height) > compHeight) {
			height = compHeight - y;
		}

		// Update rectToDraw after saving old value.
		if (rectToDraw != null) {
			previousRectDrawn.setBounds(rectToDraw.x, rectToDraw.y,
					rectToDraw.width, rectToDraw.height);
			rectToDraw.setBounds(x, y, width, height);
		} else {
			rectToDraw = new Rectangle(x, y, width, height);
		}
	}

	public static void main(String[] args) {
		// Determine what the GraphicsDevice can support.
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		final int width = gd.getDisplayMode().getWidth();
		final int height = gd.getDisplayMode().getHeight();
		// final boolean isTranslucencySupported =
		// gd.isWindowTranslucencySupported(TRANSLUCENT);

		// If shaped windows aren't supported, exit.
		/*
		 * if (!gd.isWindowTranslucencySupported(PERPIXEL_TRANSPARENT)) {
		 * System.err.println("Shaped windows are not supported");
		 * System.exit(0); }
		 */

		// If translucent windows aren't supported,
		// create an opaque window.

		/*
		 * if (!isTranslucencySupported) { System.out.println(
		 * "Translucency is not supported, creating an opaque window"); }
		 */
		final Rectangle rectTwo = new Rectangle(width, height);
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported");
			return;
		}

		// OPTIONAL: check to see if an instance of this application is already
		// running, use the name of the window title of this JFrame for checking
		if (JIntellitype.checkInstanceAlreadyRunning("Screenshot")) {
			System.out
					.println("An instance of this application is already running");
			System.exit(1);
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		final PopupMenu popup = new PopupMenu();
		final TrayIcon trayIcon = new TrayIcon(createImage(
				"images/Screenscropper.jpg", "tray icon"));
		final SystemTray tray = SystemTray.getSystemTray();

		MenuItem screenCrop = new MenuItem("Crop Screen");
		MenuItem exitItem = new MenuItem("Exit");

		popup.add(screenCrop);
		popup.addSeparator();
		popup.add(exitItem);

		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);
		trayIcon.setToolTip("ScreenCropper");

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
			return;
		}

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tray.remove(trayIcon);
				System.exit(0);
			}
		});

		screenCrop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sc.createAndShowGUI(rectTwo, width, height);

			}
		});

		JIntellitype.getInstance().registerHotKey(1,
				JIntellitype.MOD_ALT + JIntellitype.MOD_SHIFT, (int) 'S');

		// assign this class to be a HotKeyListener
		JIntellitype.getInstance().addHotKeyListener(sc);
		// Create the GUI on the event-dispatching thread

	}

	public void createAndShowGUI(final Rectangle rectTwo, final int width,
			final int height) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String fullPath = Filepath(path);
				Robot r;
				try {
					BufferedImage img = null;
					r = new Robot();
					img = r.createScreenCapture(rectTwo);
					try {
						ImageIO.write(img, "jpg", new File(fullPath));
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (AWTException e) {
					e.printStackTrace();
				}

				sw = new JFrame("Screenshot");
				sw.setUndecorated(true);
				sw.setSize(width, height);
				sw.setLocation(0, 0);
				sw.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				Container container = sw.getContentPane();
				container.setLayout(new BoxLayout(container,
						BoxLayout.PAGE_AXIS));
				sc = new Screenshot(fullPath);
				container.add(sc);
				sw.setContentPane(container);
				// Display the window.
				sw.setVisible(true);

			}
		});
	}

	public void onHotKey(int aIdentifier) {
		if (aIdentifier == 1) {
			System.out.println("ALT + SHFT+ S hotkey pressed");
		}
	}

	protected static Image createImage(String path, String description) {
		URL imageURL = Screenshot.class.getResource(path);

		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}

	private static String Filepath(String path) {
		SimpleDateFormat sd = new SimpleDateFormat("MMddyyyyHHmmss");
		String DateString = sd.format(new Date());
		String fullPath = path + "\\\\Screenshot_" + DateString + ".jpg";
		// System.out.println(fullPath);
		return fullPath;
	}
}
