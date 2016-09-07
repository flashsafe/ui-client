package ru.flashsafe.client;

import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QDialog;

/**
 * @author Alexander Krysin
 *
 */
public class Main {
	public static QApplication app;

	public static void main(String[] args) {
		launch();
	}
	
	public static void launch() {
		QApplication.initialize("Flashsafe", new String[]{});
	    
	    QDialog splash = new QDialog();
	    SplashUI splashUI = new SplashUI();
	    splashUI.setupUi(splash);
	    splash.show();
	    splashUI.start();
	    
	    QApplication.execStatic();
		QApplication.shutdown();
	}

}
