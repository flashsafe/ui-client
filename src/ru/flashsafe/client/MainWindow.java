package ru.flashsafe.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.trolltech.qt.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trolltech.qt.QUiForm;
import com.trolltech.qt.core.Qt.MatchFlag;
import com.trolltech.qt.core.Qt.MatchFlags;
import com.trolltech.qt.core.Qt.SortOrder;
import com.trolltech.qt.core.Qt.WindowType;
import com.trolltech.qt.gui.QAbstractItemView.DragDropMode;
import com.trolltech.qt.gui.QAbstractItemView.SelectionBehavior;
import com.trolltech.qt.gui.QAbstractItemView.SelectionMode;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCommandLinkButton;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFileIconProvider;
import com.trolltech.qt.gui.QFileIconProvider.IconType;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QFrame;
import com.trolltech.qt.gui.QGraphicsAnchorLayout;
import com.trolltech.qt.gui.QGraphicsItem;
import com.trolltech.qt.gui.QGraphicsLayout;
import com.trolltech.qt.gui.QGraphicsLayoutItemInterface;
import com.trolltech.qt.gui.QGraphicsWidget;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QHeaderView;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLayout;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QListView.LayoutMode;
import com.trolltech.qt.gui.QListWidgetItem;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QProgressBar;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QSizePolicy.Policy;
import com.trolltech.qt.gui.QSystemTrayIcon;
import com.trolltech.qt.gui.QTreeWidget;
import com.trolltech.qt.gui.QTreeWidgetItem;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import ru.flashsafe.client.api.DownloadProgressListener;
import ru.flashsafe.client.api.FSCallback;
import ru.flashsafe.client.api.FlashObject;
import ru.flashsafe.client.api.FlashSafe;
import ru.flashsafe.client.api.FlashSafe.DownloadListener;
import ru.flashsafe.client.api.UploadProgressListener;
import ru.flashsafe.client.util.HistoryObject;
import ru.flashsafe.client.util.TokenUtil;

/**
 * @author Alexander Krysin
 *
 */
public class MainWindow implements QUiForm<FramelessWindow>, FileController {
	private static final Logger LOGGER = LoggerFactory.getLogger(MainWindow.class);
    
    private QWidget centralwidget;
    public QFrame leftFrame;
    public QWidget verticalLayoutWidget;
    public QVBoxLayout verticalLayout;
    private QHBoxLayout horizontalLayout;
    private QLabel logo;
    private QPushButton settingsButton;
    public QTreeWidget tree;
    public QCommandLinkButton addFolderButton/*, trashButton*/;
    public QPushButton closeButton, minimizeButton, maximizeButton;
    private QPushButton backwardButton, forwardButton;
    //private QPushButton tableButton, gridButton;
    //private QPushButton filterButton;
    public QLineEdit searchField;
    public QLabel searchIcon;
    public QDragAndDropTableWidget table;
    public QProgressBar contentProgress;
    
    private static final List<FlashObject> currentFolderEntries = new ArrayList();
    private static final HistoryObject<String> historyObject = new HistoryObject<>();
    public String currentFolder = "";
    
    private FramelessWindow window;
    private static final QMainWindow loadsWindow = new QMainWindow();
    private static final QMainWindow createPathWindow = new QMainWindow();
    private static final QMainWindow settingsWindow = new QMainWindow();
    
    private static final LoadsWindow loadsUI = new LoadsWindow();
    private final CreatePathWindow createPathUI = new CreatePathWindow(this);
    private static final SettingsWindow settingsUI = new SettingsWindow();
    
    private static final HashMap<String, QTreeWidgetItem> FOLDERS_TREE = new HashMap<>();
    
    private static final HashMap<QTreeWidgetItem, String> HASHES_TREE = new HashMap<>();
    
    private static final QSystemTrayIcon TRAY_ICON = new QSystemTrayIcon();

    public MainWindow() {
        super();
        loadsUI.setupUi(loadsWindow);
        createPathUI.setupUi(createPathWindow);
        settingsUI.setupUi(settingsWindow);
    }
    
    public void setupUi(FramelessWindow window) {
        this.window = window;
        window.setObjectName("window");
        
        String stylesheet = ResourcesUtil.loadQSS("flashsafe");
        window.setStyleSheet(stylesheet);
        
        TRAY_ICON.setIcon(new QIcon("classpath:img/logo.png"));
        TRAY_ICON.show();
        
        QTimer timer = new QTimer(window);
        timer.setInterval(5000);
        timer.timeout.connect(this, "timer()");
        timer.start();
        
        window.setWindowIcon(new QIcon("classpath:img/logo.png"));
        window.setIconSize(new QSize(32, 32));
        
        window.setWindowFlags(WindowType.FramelessWindowHint);
        
        centralwidget = new QWidget(window);
        centralwidget.setObjectName("centralwidget");
        //centralwidget.setMaximumSize(new QSize(65536, 65536));
        //centralwidget.setSizePolicy(Policy.Maximum, Policy.Maximum);
        
        leftFrame = new QFrame(centralwidget);
        leftFrame.setObjectName("leftFrame");
        QSizePolicy sp = new QSizePolicy(Policy.Expanding, Policy.Expanding);
        leftFrame.setSizePolicy(sp);
        leftFrame.setGeometry(new QRect(0, 0, 200, 750));
        leftFrame.setMinimumSize(new QSize(200, 750));
        leftFrame.setBaseSize(new QSize(200, 750));
        leftFrame.setMaximumSize(new QSize(200, 65536));
        //leftFrame.setSizePolicy(Policy.Maximum, Policy.Maximum);
        
        verticalLayoutWidget = new QWidget(leftFrame);
        verticalLayoutWidget.setObjectName("verticalLayoutWidget");
        verticalLayoutWidget.setGeometry(new QRect(0, 0, 200, 750));
        verticalLayoutWidget.setMaximumSize(new QSize(200, 65536));
        verticalLayoutWidget.setSizePolicy(Policy.Maximum, Policy.Maximum);
        
        verticalLayout = new QVBoxLayout(verticalLayoutWidget);
        verticalLayout.setObjectName("verticalLayout");
        
        horizontalLayout = new QHBoxLayout();
        horizontalLayout.setSpacing(6);
        horizontalLayout.setMargin(10);
        horizontalLayout.setObjectName("horizontalLayout");
        
        logo = new QLabel(verticalLayoutWidget);
        logo.setObjectName("logo");
        logo.setMinimumSize(new QSize(140, 40));
        logo.setMaximumSize(new QSize(16777215, 40));
        logo.setBaseSize(new QSize(140, 40));
        logo.setSizePolicy(Policy.Maximum, Policy.Maximum);
        
        QFont font = new QFont();
        font.setPointSize(11);
        
        logo.setFont(font);

        horizontalLayout.addWidget(logo);

        settingsButton = new QPushButton(verticalLayoutWidget);
        settingsButton.setObjectName("settingsButton");
        settingsButton.setGeometry(100, 0, 32, 32);
        settingsButton.setMinimumSize(new QSize(32, 32));
        settingsButton.setMaximumSize(new QSize(32, 32));
        settingsButton.setBaseSize(new QSize(32, 32));
        settingsButton.setIcon(new QIcon("classpath:img/sttngs.png"));
        settingsButton.setIconSize(new QSize(32, 32));
        settingsButton.clicked.connect(this, "showSettingsWindow()");

        horizontalLayout.addWidget(settingsButton);

        verticalLayout.addLayout(horizontalLayout);

        tree = new QTreeWidget(verticalLayoutWidget);
        tree.setObjectName("tree");
        tree.setMinimumWidth(200);
        tree.setMaximumWidth(200);
        tree.setMaximumHeight(65536);
        tree.setSizePolicy(Policy.Maximum, Policy.Maximum);
        tree.header().hide();
        tree.setColumnCount(1);
        tree.itemDoubleClicked.connect(this, "pathClick()");
        
        verticalLayout.addWidget(tree);

        addFolderButton = new QCommandLinkButton(verticalLayoutWidget);
        addFolderButton.setObjectName("addFolderButton");
        addFolderButton.setMinimumSize(new QSize(0, 40));
        addFolderButton.setMaximumSize(new QSize(180, 40));
        addFolderButton.setBaseSize(new QSize(180, 40));
        addFolderButton.setSizePolicy(Policy.Maximum, Policy.Maximum);
        
        QFont font1 = new QFont();
        font1.setFamily("Segoe UI");
        font1.setPointSize(14);
        
        addFolderButton.setFont(font1);
        addFolderButton.setIcon(new QIcon("classpath:img/add_folder.png"));
        addFolderButton.setIconSize(new QSize(24, 24));
        addFolderButton.clicked.connect(this, "showCreatePathWindow()");
        

        verticalLayout.addWidget(addFolderButton);

        /*trashButton = new QCommandLinkButton(verticalLayoutWidget);
        trashButton.setObjectName("trashButton");
        trashButton.setEnabled(true);
        trashButton.setMinimumSize(new QSize(180, 50));
        trashButton.setMaximumSize(new QSize(180, 50));
        trashButton.setBaseSize(new QSize(180, 50));
        trashButton.setSizePolicy(Policy.Maximum, Policy.Maximum);*/
        
       /* QFont font2 = new QFont();
        font2.setFamily("Segoe UI");
        font2.setPointSize(14);*/
        
        /*trashButton.setFont(font2);
        trashButton.setIcon(new QIcon("classpath:img/trash.png"));
        trashButton.setIconSize(new QSize(24, 24));*/
        
        //verticalLayout.addWidget(trashButton);
        
        closeButton = new QPushButton(centralwidget);
        closeButton.setObjectName("closeButton");
        closeButton.setGeometry(new QRect(976, 10, 14, 14));
        closeButton.setMinimumSize(new QSize(14, 14));
        closeButton.setMaximumSize(new QSize(14, 14));
        closeButton.setBaseSize(new QSize(14, 14));
        closeButton.setIcon(new QIcon("classpath:img/close_no_shape.png"));
        closeButton.setIconSize(new QSize(14, 14));
        closeButton.clicked.connect(this, "exit()");
        
        minimizeButton = new QPushButton(centralwidget);
        minimizeButton.setObjectName("minimizeButton");
        minimizeButton.setGeometry(new QRect(952, 10, 14, 14));
        minimizeButton.setMinimumSize(new QSize(14, 14));
        minimizeButton.setMaximumSize(new QSize(14, 14));
        minimizeButton.setBaseSize(new QSize(14, 14));
        minimizeButton.setIcon(new QIcon("classpath:img/minimize_no_shape.png"));
        minimizeButton.setIconSize(new QSize(14, 14));
        minimizeButton.clicked.connect(this, "minimize()");
        
        maximizeButton = new QPushButton(centralwidget);
        maximizeButton.setObjectName("maximizeButton");
        maximizeButton.setGeometry(new QRect(928, 10, 14, 14));
        maximizeButton.setMinimumSize(new QSize(14, 14));
        maximizeButton.setMaximumSize(new QSize(14, 14));
        maximizeButton.setBaseSize(new QSize(14, 14));
        maximizeButton.setIcon(new QIcon("classpath:img/maximize_no_shape.png"));
        maximizeButton.setIconSize(new QSize(14, 14));
        maximizeButton.clicked.connect(this, "maximize()");
        
        backwardButton = new QPushButton(centralwidget);
        backwardButton.setObjectName("backwardButton");
        backwardButton.setGeometry(new QRect(210, 20, 25, 25));
        backwardButton.setMinimumSize(new QSize(25, 25));
        backwardButton.setMaximumSize(new QSize(25, 25));
        backwardButton.setBaseSize(new QSize(25, 25));
        backwardButton.setIcon(new QIcon("classpath:img/backward_disabled.png"));
        backwardButton.setIconSize(new QSize(25, 25));
        backwardButton.clicked.connect(this, "navigateBackward()");
        
        forwardButton = new QPushButton(centralwidget);
        forwardButton.setObjectName("forwardButton");
        forwardButton.setGeometry(new QRect(245, 20, 25, 25));
        forwardButton.setMinimumSize(new QSize(25, 25));
        forwardButton.setMaximumSize(new QSize(25, 25));
        forwardButton.setBaseSize(new QSize(25, 25));
        forwardButton.setIcon(new QIcon("classpath:img/forward_disabled.png"));
        forwardButton.setIconSize(new QSize(25, 25));
        forwardButton.clicked.connect(this, "navigateForward()");
        
        /*tableButton = new QPushButton(centralwidget);
        tableButton.setObjectName("tableButton");
        tableButton.setGeometry(new QRect(290, 20, 25, 25));
        tableButton.setMinimumSize(new QSize(25, 25));
        tableButton.setMaximumSize(new QSize(25, 25));
        tableButton.setBaseSize(new QSize(25, 25));
        tableButton.setIcon(new QIcon("classpath:img/list_active.png"));
        tableButton.setIconSize(new QSize(25, 25));
        
        gridButton = new QPushButton(centralwidget);
        gridButton.setObjectName("gridButton");
        gridButton.setGeometry(new QRect(325, 20, 25, 25));
        gridButton.setMinimumSize(new QSize(25, 25));
        gridButton.setMaximumSize(new QSize(25, 25));
        gridButton.setBaseSize(new QSize(25, 25));
        gridButton.setIcon(new QIcon("classpath:img/table_inactive.png"));
        gridButton.setIconSize(new QSize(25, 25));
        
        filterButton = new QPushButton(centralwidget);
        filterButton.setObjectName("filterButton");
        filterButton.setGeometry(new QRect(370, 20, 25, 25));
        filterButton.setMinimumSize(new QSize(25, 25));
        filterButton.setMaximumSize(new QSize(25, 25));
        filterButton.setBaseSize(new QSize(25, 25));
        filterButton.setIcon(new QIcon("classpath:img/filter_inactive.png"));
        filterButton.setIconSize(new QSize(25, 25));*/
        
        searchField = new QLineEdit(centralwidget);
        searchField.setObjectName("searchField");
        searchField.setGeometry(new QRect(889, 88, 100, 22));
        
        QFont font3 = new QFont();
        font3.setPointSize(11);
        
        searchField.setFont(font3);
        searchField.setMaxLength(255);
        searchField.setFrame(true);
        
        searchIcon = new QLabel(centralwidget);
        searchIcon.setObjectName("searchIcon");
        searchIcon.setGeometry(new QRect(869, 90, 20, 20));
        searchIcon.setPixmap(new QPixmap("classpath:img/search.png").scaled(14, 14));
        
        table = new QDragAndDropTableWidget(centralwidget, this);
        table.setObjectName("table");
        table.setGeometry(new QRect(210, 120, 780, 620));
        table.setMinimumSize(new QSize(780, 620));
        table.setBaseSize(new QSize(780, 620));
        table.setMaximumSize(new QSize(65536, 65536));

        table.verticalHeader().hide();

        table.setColumnCount(3);

        table.setHorizontalHeaderLabels(Arrays.asList("Name", "Type", "Size"));
        //table.horizontalHeader().setMinimumWidth(780);

        table.setSelectionBehavior(SelectionBehavior.SelectRows);
        table.setSelectionMode(SelectionMode.ExtendedSelection);

        table.setFocusPolicy(Qt.FocusPolicy.NoFocus);

        table.horizontalHeader().setResizeMode(QHeaderView.ResizeMode.Stretch);

        table.setSortingEnabled(true);

        table.horizontalHeader().setMovable(true);
        table.horizontalHeader().setSortIndicatorShown(true);
        

        
        //table.sortItems(0, SortOrder.AscendingOrder);

        
        table.setDragDropOverwriteMode(false);
        table.setDropIndicatorShown(true);
        table.setAcceptDrops(true);
        //table.setDragEnabled(false);
        table.setDragDropMode(DragDropMode.DropOnly);
        

        //table.setSelectionBehavior(SelectionBehavior.SelectRows);

        
        contentProgress = new QProgressBar(centralwidget);
        contentProgress.setObjectName("contentProgress");
        contentProgress.setGeometry(new QRect(210, 115, 780, 2));
        contentProgress.setMinimum(0);
        contentProgress.setMaximum(0);
        contentProgress.setValue(0);
        contentProgress.setTextVisible(false);
        contentProgress.setMaximumSize(new QSize(65536, 2));
        
        window.setCentralWidget(centralwidget);
        
        retranslateUi(window);
        
        window.connectSlotsByName();
        
        loadTree();
        browseFolder(currentFolder);
    }

    void retranslateUi(FramelessWindow window) {
        window.setWindowTitle(com.trolltech.qt.core.QCoreApplication.translate("window", "Flashsafe", null));
        logo.setText(com.trolltech.qt.core.QCoreApplication.translate("window", "MY FLASHSAFE", null));
        settingsButton.setText("");
        addFolderButton.setText(com.trolltech.qt.core.QCoreApplication.translate("window", "Add new folder", null));
        //trashButton.setText(com.trolltech.qt.core.QCoreApplication.translate("window", "Trash", null));
        closeButton.setText("");
        minimizeButton.setText("");
        maximizeButton.setText("");
        backwardButton.setText("");
        forwardButton.setText("");
        //tableButton.setText("");
        //gridButton.setText("");
        //filterButton.setText("");
        searchField.setPlaceholderText(com.trolltech.qt.core.QCoreApplication.translate("window", "Search...", null));
        searchIcon.setText("");
    }
    
    protected void pathClick() {
    	browseFolder(HASHES_TREE.get(tree.selectedItems().get(0)));
    }
    
    protected void exit() {
        window.close();
        TRAY_ICON.dispose();
        TokenUtil.destruct();
        //Main.app.dispose();
        //QApplication.quit();
        System.exit(0);
    }
    
    protected void minimize() {
        if(!window.isMinimized()) {
            window.showMinimized();
        } else {
            window.showNormal();
        }
    }
    
    protected void maximize() {
        if(!window.isMaximized()) {
            window.showMaximized();
        } else {
            window.showNormal();
        }
    }
    
    protected void timer() {
        window.setStyleSheet(ResourcesUtil.loadQSS("flashsafe"));
    }
    
    private void showCreatePathWindow() {
        if(createPathWindow.isHidden()) createPathWindow.show();
    }
    
    private void showSettingsWindow() {
        if(settingsWindow.isHidden()) settingsWindow.show();
    }

    @Override
    public void loadContent(String path) {
        browseFolder(path);
    }

    @Override
    public void move(String fromPath, String toPath) {}

    @Override
    public void copy(String fromPath, String toPath) {}

    @Override
    public void rename(String fileObjectHash, String name) {}

    public void refresh() {
    	browseFolder(currentFolder);
    	loadTree();
    }
    
    @Override
    public void delete(String path) {
            FlashSafe.delete(path, new FSCallback<ArrayList<FlashObject>>() {

				@Override
				public void onResult(ArrayList<FlashObject> folderEntries) {
					currentFolderEntries.clear();
		            currentFolderEntries.addAll(folderEntries);
		            if(currentFolderEntries.size() == 0) {
		                QApplication.invokeLater(() -> table.setRowCount(table.height() / table.horizontalHeader().height()));
		            }
		            QApplication.invokeLater(() -> table.setEntries(currentFolderEntries));
		            QApplication.invokeLater(() -> contentProgress.setMaximum(1));
		            QApplication.invokeLater(() -> contentProgress.setValue(1));
				}});
            refresh();
    }
    
    public synchronized void navigateBackward() {
        if (historyObject.hasPrevious()) {
            String previousLocation = historyObject.previous();
            if (listFolder(previousLocation)) {
                currentFolder = previousLocation;
                QApplication.invokeLater(() -> {
                    if(!historyObject.hasPrevious()) backwardButton.setIcon(new QIcon("classpath:img/backward_disabled.png"));
                    forwardButton.setIcon(new QIcon("classpath:img/forward_enabled.png"));
                });
            }
        }
    }

    public synchronized void navigateForward() {
        if (historyObject.hasNext()) {
            String previousLocation = historyObject.next();
            if (listFolder(previousLocation)) {
                currentFolder = previousLocation;
                QApplication.invokeLater(() -> {
                	if(!historyObject.hasNext()) forwardButton.setIcon(new QIcon("classpath:img/forward_disabled.png"));
                    backwardButton.setIcon(new QIcon("classpath:img/backward_enabled.png"));
                });
            }
        }
    }

    @Override
    public String createDirectory(String parentHash, String name) {
        if (name.isEmpty()) return null;
        final String[] hash = new String[1];
        FlashSafe.makeFolder(parentHash, name, new FSCallback<String>() {

			@Override
			public void onResult(String t) {
				hash[0] = t;
			}
		});
        return hash[0];
    }
    
    @Override
    public void createDirectory(String folderName) {
    	if (folderName.isEmpty()) return;
    	String hash;
    	FlashSafe.makeFolder(currentFolder, folderName, new FSCallback<String>() {

			@Override
			public void onResult(String t) {
				QApplication.invokeLater(() -> refresh());
			}
		});
    }
    
    public synchronized void browseTrash() {}
    
    @Override
    public String getCurrentLocation() {
        return currentFolder;
    }

    public final synchronized void browseFolder(String folderPath) {
        contentProgress.setMaximum(0);
        if (listFolder(folderPath)) {
            currentFolder = folderPath;
            historyObject.addObject(currentFolder);
        }
    }
    
    private boolean listFolder(String path) {
        FlashSafe.listObjects(path, new FSCallback<ArrayList<FlashObject>>() {

			@Override
			public void onResult(ArrayList<FlashObject> folderEntries) {
				QApplication.invokeLater(() -> {
					currentFolderEntries.clear();
					currentFolderEntries.addAll(folderEntries);
		            if(currentFolderEntries.size() == 0) {
		                table.setRowCount(table.height() / table.horizontalHeader().height());
		            }
		            table.clearContents();
		            table.setEntries(currentFolderEntries);
		            contentProgress.setMaximum(1);
		            contentProgress.setValue(1);
				});
			}
		});
        return true;
    }
    
    public void loadTree() {
    	tree.clear();
    	FlashSafe.getTree(new FSCallback<ArrayList<FlashObject>>() {
			@Override
			public void onResult(ArrayList<FlashObject> dirs) {
				QApplication.invokeLater(() -> {
					dirs.forEach(dir -> {
						if(dir.parentHash == null || dir.parentHash.equals("")) {
		                    QTreeWidgetItem item = new QTreeWidgetItem();
		                    item.setIcon(0, new QIcon(IconUtil.getFolderIconUri(dir)));
		                    item.setText(0, dir.objectName);
		                    tree.addTopLevelItem(item);
		                    FOLDERS_TREE.put(dir.objectHash, item);
		                    HASHES_TREE.put(item, dir.objectHash);
		                }
		            });
		            dirs.forEach(dir -> {
		                if(dir.parentHash != null && !dir.parentHash.equals("")) {
		                    QTreeWidgetItem item = new QTreeWidgetItem();
		                    item.setIcon(0, new QIcon(IconUtil.getFolderIconUri(dir)));
		                    item.setText(0, dir.objectName);
		                    FOLDERS_TREE.get(dir.parentHash).addChild(item);
		                    FOLDERS_TREE.put(dir.objectHash, item);
		                    HASHES_TREE.put(item, dir.objectHash);
		                }
		            });
				});
			}
		});
    }
    
    @Override
    public void upload(QFile fileObject, String toPath) {
    	QWidget[] loadWidget = new QWidget[1];
        QProgressBar[] bar = new QProgressBar[1];
        QListWidgetItem[] items = new QListWidgetItem[1];
        loadWidget[0] = createLoadWidget(fileObject.fileName(), "Uploading...");
        bar[0] = (QProgressBar) loadWidget[0].children().get(4);
        items[0] = loadsUI.addLoad(loadWidget[0]);
        if(loadsWindow.isHidden()) loadsWindow.show();
    	try {
			FlashSafe.upload(toPath, new QFileInfo(fileObject).absoluteFilePath(),
				new FSCallback<ArrayList<FlashObject>>() {

					@Override
					public void onResult(ArrayList<FlashObject> t) {
						QApplication.invokeLater(() -> {
							((QLabel) loadWidget[0].children().get(3)).setText("Upload finished.");
							//items[0].setSizeHint(new QSize(348, 36));
				            TRAY_ICON.showMessage("Upload finished", "File " + fileObject.fileName() + " was upload to Your Flashsafe.", QSystemTrayIcon.MessageIcon.Information, 5000);
				            refresh();
						});
					}
				},
				new UploadProgressListener() {

					@Override
					public void transferred(long num) {
						QApplication.invokeLater(() -> bar[0].setValue((int) num));
					}
				}
			);
		} catch (IOException e) {
			LOGGER.error("Error on uploading file " + fileObject.fileName(), e);
		}
    }
    
    @Override
    public void download(boolean enc, String fromPath, QFile toFile) {
    	QWidget[] loadWidget = new QWidget[1];
        QProgressBar[] bar = new QProgressBar[1];
        QListWidgetItem[] items = new QListWidgetItem[1];
        loadWidget[0] = createLoadWidget(toFile.fileName(), "Downloading...");
        bar[0] = (QProgressBar) loadWidget[0].children().get(4);
        items[0] = loadsUI.addLoad(loadWidget[0]);
        if(loadsWindow.isHidden()) loadsWindow.show();
    	try {
			FlashSafe.download(enc, fromPath, toFile.fileName(), new DownloadListener() {

				@Override
				public void onSuccess() {
					QApplication.invokeLater(() -> {
						((QLabel) loadWidget[0].children().get(3)).setText("Download finished.");
			            //items[0].setSizeHint(new QSize(348, 36));
			            TRAY_ICON.showMessage("Download finished", "File " + toFile.fileName() + " was download to Your PC.", QSystemTrayIcon.MessageIcon.Information, 5000);
					});
				}
			}, new DownloadProgressListener() {

				@Override
				public void transferred(long num) {
					QApplication.invokeLater(() -> bar[0].setValue((int) num));
				}
			});
		} catch (IOException e) {
			LOGGER.error("Error on download file " + toFile.fileName(), e);
		}
    }
    
    private QWidget createLoadWidget(String fileName, String process) {
        QVBoxLayout layout = new QVBoxLayout();
        layout.setContentsMargins(0, 0, 0, 0);
        QHBoxLayout hlayout = new QHBoxLayout();
        hlayout.setContentsMargins(0, 0, 0, 0);
        QLabel icon = new QLabel();
        icon.setObjectName("loadIcon");
        QIcon licon = new QIcon(IconUtil.getFileIconUri(fileName.substring(fileName.lastIndexOf("/") + 1)));
        icon.setPixmap(licon.pixmap(32, 32));
        icon.setMaximumSize(new QSize(32, 32));
        hlayout.addWidget(icon);
        QVBoxLayout vlayout = new QVBoxLayout();
        vlayout.setContentsMargins(0, 0, 0, 0);
        QFont font = new QFont("Tahoma", 12);
        font.setBold(true);
        QLabel name = new QLabel(fileName.substring(fileName.lastIndexOf("/") + 1));
        name.setObjectName("loadName");
        name.setFont(font);
        vlayout.addWidget(name);
        QLabel info = new QLabel(process);
        info.setObjectName("loadInfo");
        font.setBold(false);
        info.setFont(font);
        vlayout.addWidget(info);
        hlayout.addLayout(vlayout);
        layout.addLayout(hlayout);
        QProgressBar progress = new QProgressBar();
        progress.setObjectName("loadProgress");
        progress.setTextVisible(false);
        progress.setMaximum(100);
        layout.addWidget(progress);
        QWidget loadWidget = new QWidget();
        loadWidget.setLayout(layout);
        return loadWidget;
    }

}
