package ru.flashsafe.client;

import com.trolltech.qt.QUiForm;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.QTimer;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QTabWidget;
import com.trolltech.qt.gui.QTextBrowser;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.pkcs11.jacknji11.CKA;
import org.pkcs11.jacknji11.CKM;
import org.pkcs11.jacknji11.CKO;
import org.pkcs11.jacknji11.CK_TOKEN_INFO;
import ru.flashsafe.client.api.Settings;
import ru.flashsafe.client.util.TokenUtil;

public class SettingsWindow implements QUiForm<QMainWindow> {
    private QWidget centralWidget;
    public QTabWidget tabWidget;
    public QWidget encrypt;
    public QCheckBox encrypt_checkbox;
    public QPushButton saveRestoreFile;
    public QLineEdit pin_edit;
    public QWidget about;
    public QTextBrowser about_info;
    
    private QMainWindow window;
    
    public SettingsWindow() {}

    @Override
    public void setupUi(QMainWindow window) {
        this.window = window;
        window.setObjectName("MainWindow");
        window.resize(new QSize(300, 200).expandedTo(window.minimumSizeHint()));
        window.setWindowIcon(new QIcon("classpath:img/logo.png"));
        window.setIconSize(new QSize(32, 32));
        window.setWindowTitle("Settings");
        QSizePolicy sizePolicy = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Fixed, com.trolltech.qt.gui.QSizePolicy.Policy.Fixed);
        sizePolicy.setHorizontalStretch((byte)0);
        sizePolicy.setVerticalStretch((byte)0);
        sizePolicy.setHeightForWidth(window.sizePolicy().hasHeightForWidth());
        window.setSizePolicy(sizePolicy);
        window.setStyleSheet("background: #252B2D");
        centralWidget = new QWidget(window);
        centralWidget.setObjectName("centralwidget");
        tabWidget = new QTabWidget(centralWidget);
        tabWidget.setObjectName("tabWidget");
        tabWidget.setGeometry(new QRect(0, 0, 401, 301));
        encrypt = new QWidget();
        encrypt.setObjectName("encrypt");
        encrypt_checkbox = new QCheckBox(encrypt);
        encrypt_checkbox.setObjectName("encrypt_checkbox");
        encrypt_checkbox.setGeometry(new QRect(10, 10, 280, 20));
        encrypt_checkbox.setStyleSheet("color: #EEEEEE;font-family: Tahoma;font-size: 14px;QCheckBox::indicator {width: 20px;height: 20px;}");
        encrypt_checkbox.setIconSize(new QSize(20, 20));
        encrypt_checkbox.setTristate(false);
        encrypt_checkbox.setCheckState(isEncryptEnabled() ? Qt.CheckState.Checked : Qt.CheckState.Unchecked);
        encrypt_checkbox.stateChanged.connect(this, "changeEncryptState()");
        saveRestoreFile = new QPushButton(encrypt);
        saveRestoreFile.setObjectName("saveRestoreFile");
        saveRestoreFile.setGeometry(new QRect(10, 140, 280, 30));
        saveRestoreFile.setStyleSheet("color: #EEEEEE;\n"+
"font-family: Tahoma;\n"+
"font-size: 14px;");
        saveRestoreFile.clicked.connect(this, "saveRestoreFile()");
        pin_edit = new QLineEdit(encrypt);
        pin_edit.setObjectName("pin_edit");
        pin_edit.setGeometry(new QRect(10, 100, 280, 30));
        QSizePolicy sizePolicy1 = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Fixed, com.trolltech.qt.gui.QSizePolicy.Policy.Fixed);
        sizePolicy1.setHorizontalStretch((byte)0);
        sizePolicy1.setVerticalStretch((byte)0);
        sizePolicy1.setHeightForWidth(pin_edit.sizePolicy().hasHeightForWidth());
        pin_edit.setSizePolicy(sizePolicy1);
        pin_edit.setStyleSheet("color: #EEEEEE;\n"+
"font-family: Tahoma;\n"+
"font-size: 14px;");
        tabWidget.addTab(encrypt, com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Encrypting", null));
        about = new QWidget();
        about.setObjectName("about");
        about_info = new QTextBrowser(about);
        about_info.setObjectName("about_info");
        about_info.setGeometry(new QRect(0, 0, 298, 178));
        QSizePolicy sizePolicy2 = new QSizePolicy(com.trolltech.qt.gui.QSizePolicy.Policy.Fixed, com.trolltech.qt.gui.QSizePolicy.Policy.Fixed);
        sizePolicy2.setHorizontalStretch((byte)0);
        sizePolicy2.setVerticalStretch((byte)0);
        sizePolicy2.setHeightForWidth(about_info.sizePolicy().hasHeightForWidth());
        about_info.setSizePolicy(sizePolicy2);
        about_info.setStyleSheet("font-family: Tahoma;font-size: 12px;color: #EEEEEE;");
        tabWidget.addTab(about, com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "About", null));
        window.setCentralWidget(centralWidget);
        retranslateUi(window);

        tabWidget.setCurrentIndex(1);


        window.connectSlotsByName();
    }
    
    protected void saveRestoreFile() {
        String dir = QFileDialog.getExistingDirectory(centralWidget, "Choose directory for save", "", QFileDialog.Option.ShowDirsOnly);
        if(null != dir && !dir.equals("")) {
            try {
                File f = new File(dir + "/" + "restore.fs");
                f.createNewFile();
                FileWriter fwriter = new FileWriter(f);
                String restoredata = MD5(MD5(MD5((getSN() + getSecret()).getBytes()).getBytes()).getBytes());
                fwriter.write(restoredata);
                fwriter.flush();
                fwriter.close();
            } catch(IOException e) {
                System.err.println("Error on save restore file" + e);
            }
        }
    }
    
    private static String getSN() {
        /*CK_TOKEN_INFO tokenInfo = TokenUtil.getTokenInfo(TokenUtil.slot);*/
        return /*new String(tokenInfo.serialNumber)*/ Settings.getDSN();
    }

    private static String getSecret() {
        /*TokenUtil.findObjectsInit(TokenUtil.session, new CKA(CKA.CLASS, CKO.PUBLIC_KEY));
        long[] objects = TokenUtil.findObjects(TokenUtil.session, 1);
        TokenUtil.findObjectsFinal(TokenUtil.session);
        CKA publicValue = TokenUtil.getAttributeValue(TokenUtil.session, objects[0], CKA.MODULUS);*/
        return /*MD5(publicValue.getValue())*/Settings.getToken();
    }

    private static String MD5(byte[] md5) {
        try {
             java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
             byte[] array = md.digest(md5);
             StringBuffer sb = new StringBuffer();
             for (int i = 0; i < array.length; ++i) {
               sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
             return sb.toString();
         } catch (java.security.NoSuchAlgorithmException e) {
             System.err.println("Error on generate MD5" + e);
         }
        return null;
    }
    
    private boolean isEncryptEnabled() {
        //if(TokenUtil.session <= 0) TokenUtil.init();
        /*TokenUtil.findObjectsInit(TokenUtil.session, new CKA(CKA.LABEL, "Encrypt"));
        long[] encrypt = TokenUtil.findObjects(TokenUtil.session, 1);
        TokenUtil.findObjectsFinal(TokenUtil.session);*/
        return /*encrypt.length > 0*/Settings.needEncrypt() == 1;
    }

    void changeEncryptState() {
        if(encrypt_checkbox.checkState() == Qt.CheckState.Checked) {
            enableEncrypting();
        } else {
            disableEncrypting();
        }
    }

    private void enableEncrypting() {
        /*try {
            File e = new File("./.e");
            e.createNewFile();
        } catch(IOException e) {
            System.err.println("Error on enable encryption");
        }*/
        if(!TokenUtil.isInitialized()) TokenUtil.init();
        TokenUtil.createEncryptFlag();
    }
    
    private void disableEncrypting() {
        /*File e = new File("./.e");
        if(e.exists()) e.delete();*/
        if(!TokenUtil.isInitialized()) TokenUtil.init();
        if(TokenUtil.findObjects(new CKA(CKA.LABEL, "Encrypt")) > 0) {
            TokenUtil.destroyEncryptFlag(TokenUtil.findObject(new CKA(CKA.LABEL, "Encrypt")));
        }
    }
    
    void retranslateUi(QMainWindow MainWindow) {
        MainWindow.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "MainWindow", null));
        encrypt_checkbox.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Encrypt  files", null));
        saveRestoreFile.setText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Save restore file", null));
        pin_edit.setText("");
        pin_edit.setPlaceholderText(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Enter PIN-code to encrypt restore file", null));
        tabWidget.setTabText(tabWidget.indexOf(encrypt), com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "Encrypting", null));
        about_info.setHtml(com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\" \"http://www.w3.org/TR/REC-html40/strict.dtd\">\n"+
"<html><head><meta name=\"qrichtext\" content=\"1\" /><style type=\"text/css\">\n"+
"p, li { white-space: pre-wrap; }\n"+
"</style></head><body style=\" font-family:'Tahoma'; font-size:12px; font-weight:400; font-style:normal;\">\n"+
"<p align=\"center\" style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8pt;\"><br /></p>\n"+
"<p align=\"center\" style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8pt;\"><br /></p>\n"+
"<p align=\"center\" style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8pt;\"><br /></p>\n"+
"<p align=\"center\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:16pt; font-weight:600; color:#efefef;\">Flashsafe</span></p>\n"+
"<p align=\"center\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:12pt; color:#efefef;\">Infinity secure storage</span></p>\n"+
"<p align=\"center\" style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:12pt; color:#efefef;\"><br /></p>\n"+
"<p align=\"center\" style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:12pt; color:#efefef;\"><br /></p>\n"+
"<p align=\"center\" style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:12pt; color:#efefef;\"><br /></p>\n"+
"<p align=\"center\" style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:10pt; color:#efefef;\">Version: </span><span style=\" font-size:10pt; font-weight:600; color:#efefef;\">0.0.0.3b</span></p></body></html>", null));
        tabWidget.setTabText(tabWidget.indexOf(about), com.trolltech.qt.core.QCoreApplication.translate("MainWindow", "About", null));
    }
    
    protected void timer() {
        window.setStyleSheet(ResourcesUtil.loadQSS("flashsafe"));
    }
    
}
