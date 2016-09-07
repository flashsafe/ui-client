package ru.flashsafe.client;

import com.trolltech.qt.core.QDir;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QFileInfo;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.DropAction;
import com.trolltech.qt.core.Qt.FocusPolicy;
import static com.trolltech.qt.core.Qt.ItemDataRole.DisplayRole;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QContextMenuEvent;
import com.trolltech.qt.gui.QDragEnterEvent;
import com.trolltech.qt.gui.QDragMoveEvent;
import com.trolltech.qt.gui.QDropEvent;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QFileDialog.Option;
import com.trolltech.qt.gui.QHeaderView;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QTableWidget;
import com.trolltech.qt.gui.QTableWidgetItem;
import com.trolltech.qt.gui.QWidget;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javafx.collections.ObservableList;
import javax.tools.FileObject;
import ru.flashsafe.client.api.FSCallback;
import ru.flashsafe.client.api.FlashObject;
import ru.flashsafe.client.api.FlashSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander Krysin
 *
 */
public class QDragAndDropTableWidget extends QTableWidget {
    private static final Logger LOGGER = LoggerFactory.getLogger(QDragAndDropTableWidget.class);
    
    private List<FlashObject> currentFolderEntries;
    private final FileController fileController;
    private String upload = "";
    private FlashObject download = null;
    private String downloadHash = "";
    private String downloadName = "";
    private String delete = "";
	
    public QDragAndDropTableWidget(QWidget widget, FileController fileController) {
        super(widget);
        this.fileController = fileController;
        setDefaultDropAction(DropAction.CopyAction);
    }
    
    public void setEntries(List<FlashObject> currentFolderEntries) {
        this.currentFolderEntries = currentFolderEntries;
        setRowCount(currentFolderEntries.size());
        for(int i=0;i<currentFolderEntries.size();i++) {
            FlashObject flashObject = currentFolderEntries.get(i);
            setRow(i, flashObject);
        }
        setSelectionBehavior(SelectionBehavior.SelectRows);
        setSelectionMode(SelectionMode.ExtendedSelection);

        setFocusPolicy(Qt.FocusPolicy.NoFocus);
    }

    synchronized  void setRow(int i, FlashObject o) {
        String iconUri = o.objectType.equals("FILE") ?
                IconUtil.getFileIconUri(o.objectName) : IconUtil.getFolderIconUri(o);
        QTableWidgetItem item = new QTableWidgetItem();
        item.setIcon(new QIcon(iconUri));
        item.setText(o.objectName);
        setItem(i, 0, item);
        QTableWidgetItem item1 = new QTableWidgetItem();
        item1.setText(o.objectType.equals("FILE") ? "File" : "Folder");
        setItem(i, 1, item1);
        QTableWidgetItem item2 = new QTableWidgetItem();
        String size = "";
        size = Long.toString(o.size / 1024) + "KB";
        item2.setText(size);
        setItem(i, 2, item2);
    }
    
    @Override
    protected void contextMenuEvent(QContextMenuEvent event) {
        QMenu menu = new QMenu(this);
        QModelIndex index = indexAt(event.pos());
        if(index != null) {
        FlashObject flashObject = currentFolderEntries.get(index.row());
        download = flashObject;
        downloadHash = flashObject.objectHash;
        downloadName = flashObject.objectName;
        delete = flashObject.objectHash;
            QAction download_action = menu.addAction("Download", this, "download()");
            QAction delete_action = menu.addAction("Delete", this, "delete()");
        }
        QAction refresh_action = menu.addAction("Refresh", this, "refresh()");
        menu.popup(event.globalPos());
    }
    
    @Override
    protected void dragEnterEvent(QDragEnterEvent event) {
        event.setDropAction(DropAction.CopyAction);
        event.acceptProposedAction();
    }

    @Override
    protected void dragMoveEvent(QDragMoveEvent event) {
        event.acceptProposedAction();
    }
    
    @Override
    protected void dropEvent(QDropEvent event) {
        if(event.mimeData().hasUrls()) {
            for(int i=0;i<event.mimeData().urls().size();i++) {
                QFile object = new QFile(event.mimeData().urls().get(i).toLocalFile());
                QFileInfo finfo = new QFileInfo(object);
                if(finfo.isFile()) {
                    fileController.upload(object, fileController.getCurrentLocation());
                    ((MainWindow) fileController).refresh();
                } else { // dir
                    QDir dir = new QDir(event.mimeData().urls().get(i).toLocalFile());
                    String hash = fileController.createDirectory(fileController.getCurrentLocation(), dir.dirName());
                    createDirs(hash, dir.entryInfoList());
                    ((MainWindow) fileController).refresh();
                }
            }
        }
        event.accept();
    }
    
    private void createDirs(String parentHash, List<QFileInfo> childs) {
        for(QFileInfo child : childs) {
            if(child.isDir()) {
                QDir dir = child.absoluteDir();
                String hash = fileController.createDirectory(parentHash, dir.dirName());
                createDirs(hash, dir.entryInfoList());
            } else {
                fileController.upload(new QFile(child.absoluteFilePath()), parentHash);
            }
        }
    }
    
    @Override
    protected void mousePressEvent(QMouseEvent event) {}
    
    @Override
    protected void mouseDoubleClickEvent(QMouseEvent event) {
        QModelIndex index = indexAt(event.pos());
        if(index != null) {
            FlashObject flashObject = currentFolderEntries.get(index.row());
            if(flashObject.objectType.equals("FOLDER")) {
                ((MainWindow) fileController).browseFolder(flashObject.objectHash);
            }
        }
    }
    
    private void download() {
        String dir = QFileDialog.getExistingDirectory(this, "Choose directory for download", "", Option.ShowDirsOnly);
        if(null != dir && !dir.equals("") && download != null) {
            if(download.objectType.equals("FILE")) {
                ((MainWindow) fileController).download(download.isEncrypted == 1, downloadHash, new QFile(dir + "/" + downloadName));
            } else { // dir
                FlashSafe.listObjects(download.objectHash, new FSCallback<ArrayList<FlashObject>>() {

			@Override
			public void onResult(ArrayList<FlashObject> folderEntries) {
				createLocalDirs(dir, download.objectHash, folderEntries);
			}
		});
            }
        }
    }
    
    private void createLocalDirs(String dir, String parentHash, List<FlashObject> childs) {
        for(FlashObject child : childs) {
            if(child.objectType.equals("FOLDER")) {
                QDir cdir = new QDir(dir);
                cdir.mkdir(child.objectName);
                FlashSafe.listObjects(child.objectHash, new FSCallback<ArrayList<FlashObject>>() {

					@Override
					public void onResult(ArrayList<FlashObject> clist) {
						createLocalDirs(cdir.absolutePath(), child.objectHash, clist);
					}
				});
            } else {
                ((MainWindow) fileController).download(download.isEncrypted == 1, child.objectHash, new QFile(dir + "/" + child.objectName));
                //((MainWindow) fileController).refresh();
            }
        }
    }
    
    private void delete() {
        ((MainWindow) fileController).delete(delete);
    }
    
    private void refresh() {
        ((MainWindow) fileController).refresh();
    }
    
}
