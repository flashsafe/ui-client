package ru.flashsafe.client;

import com.trolltech.qt.QUiForm;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.QTimer;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QListWidgetItem;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QSizePolicy.Policy;
import com.trolltech.qt.gui.QWidget;

/**
 * @author Alexander Krysin
 *
 */
public class LoadsWindow implements QUiForm<QMainWindow> {
    public QWidget centralwidget;
    public QListWidget loadsList;

    private QMainWindow window;

    public LoadsWindow() {
            super();
    }

    @Override
    public void setupUi(QMainWindow window) {
        this.window = window;
        window.setObjectName("window");
        window.setSizePolicy(Policy.Fixed, Policy.Fixed);
        window.setFixedSize(new QSize(350, 250));
        window.setWindowIconText("Loads");
        window.setWindowIcon(new QIcon("classpath:img/logo.png"));
        window.setIconSize(new QSize(32, 32));
        
        String stylesheet = ResourcesUtil.loadQSS("flashsafe");
        window.setStyleSheet(stylesheet);
        
        QTimer timer = new QTimer(window);
        timer.setInterval(5000);
        timer.timeout.connect(this, "timer()");
        timer.start();
        
        window.setWindowIcon(new QIcon(new QPixmap("classpath:img/logo.png")));
        window.setIconSize(new QSize(32, 32));

        centralwidget = new QWidget(window);
        centralwidget.setObjectName("centralwidget");
        centralwidget.setMinimumSize(window.minimumSize());
        
        loadsList = new QListWidget(centralwidget);
        loadsList.setMinimumSize(centralwidget.minimumSize());
        loadsList.setMaximumSize(centralwidget.maximumSize());
        
        window.setCentralWidget(centralwidget);
        
        retranslateUi(window);
        
        window.connectSlotsByName();
    }

    void retranslateUi(QMainWindow window) {
        window.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("window", "File transfers", null));
    }
    
    protected void timer() {
        window.setStyleSheet(ResourcesUtil.loadQSS("flashsafe"));
    }
    
    public QListWidgetItem addLoad(QWidget load) {
        QListWidgetItem item = new QListWidgetItem();
        item.setSizeHint(new QSize(348, 50));
        loadsList.addItem(item);
        loadsList.setItemWidget(item, load);
        return item;
    }

}