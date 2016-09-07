package ru.flashsafe.client;

import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt.CursorShape;
import com.trolltech.qt.core.Qt.MouseButton;
import com.trolltech.qt.gui.QCursor;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QResizeEvent;
import com.trolltech.qt.gui.QWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static ru.flashsafe.client.FramelessWindow.ResizeType.E;
import static ru.flashsafe.client.FramelessWindow.ResizeType.N;
import static ru.flashsafe.client.FramelessWindow.ResizeType.NE;
import static ru.flashsafe.client.FramelessWindow.ResizeType.NW;
import static ru.flashsafe.client.FramelessWindow.ResizeType.S;
import static ru.flashsafe.client.FramelessWindow.ResizeType.SE;
import static ru.flashsafe.client.FramelessWindow.ResizeType.SW;
import static ru.flashsafe.client.FramelessWindow.ResizeType.W;

/**
 * Frameless QMainWindow, whitch support moving and resizing
 * @author Alexander Krysin
 */
public class FramelessWindow extends QMainWindow {
    public Logger LOGGER = LoggerFactory.getLogger(FramelessWindow.class);
    
    private boolean drag, resize;
    private QPoint start, sPos;
    private CursorShape cursorEvent;
    private final int BORDER = 4;
    private QSize sSize;
    private ResizeType resType;
    private final int minW, minH;
    
    private MainWindow mw;
    
    protected enum ResizeType {
        N, NE, E, SE, S, SW, W, NW;
    }
    
    public FramelessWindow(QSize minimumSize, MainWindow mw) {
        super();
        setMinimumSize(minimumSize);
        minW = minimumWidth();
        minH = minimumHeight();
        this.mw = mw;
    }
    
    @Override
    protected void resizeEvent(QResizeEvent event) {
    	if(isVisible()) {
	    	mw.leftFrame.resize(mw.leftFrame.width(), mw.leftFrame.height() + event.size().height() - event.oldSize().height());
	    	mw.verticalLayoutWidget.resize(mw.verticalLayoutWidget.width(), mw.verticalLayoutWidget.height() + event.size().height() - event.oldSize().height());
	    	mw.tree.resize(mw.tree.width(), mw.tree.height() + event.size().height() - event.oldSize().height());
	    	mw.addFolderButton.move(mw.addFolderButton.x(), mw.addFolderButton.y() + (event.size().height() - event.oldSize().height()));
	    	mw.maximizeButton.move(mw.maximizeButton.x() + event.size().width() - event.oldSize().width(), mw.maximizeButton.y());
	    	mw.minimizeButton.move(mw.minimizeButton.x() + event.size().width() - event.oldSize().width(), mw.minimizeButton.y());
	    	mw.closeButton.move(mw.closeButton.x() + event.size().width() - event.oldSize().width(), mw.closeButton.y());
	    	mw.searchField.move(mw.searchField.x() + event.size().width() - event.oldSize().width(), mw.searchField.y());
	    	mw.searchIcon.move(mw.searchIcon.x() + event.size().width() - event.oldSize().width(), mw.searchIcon.y());
	    	mw.contentProgress.resize(mw.contentProgress.width() + event.size().width() - event.oldSize().width(), mw.contentProgress.height());
	    	mw.table.resize(mw.table.width() + event.size().width() - event.oldSize().width(), mw.table.height() + event.size().height() - event.oldSize().height());
    	}
    	event.accept();
    }
    
    @Override
    protected void mousePressEvent(QMouseEvent e) {
        if(e.button() == MouseButton.LeftButton) {
            int x = e.globalX() - x(), y = e.globalY() - y();
            int maxX = width() - BORDER, maxY = height() - BORDER;
            if(x > BORDER && x <= maxX && y > BORDER && y <= /*maxY*/50) {
                drag = true;
                start = e.pos();
            } else if(x <= BORDER || x >= maxX || y <= BORDER || y >= maxY) {
                resize = true;
                start = e.globalPos();
                sSize = size();
                sPos = pos(); 
                resType = x <= BORDER ? y <= BORDER ? NW : y >= maxY ? SW : W
                        : x >= maxX ? y <= BORDER ? NE : y >= maxY ? SE : E
                        : y <= BORDER ? N : S;
            }
        }
    }
    
    @Override
    protected void mouseMoveEvent(QMouseEvent e) {
        if(drag) move(e.globalPos().subtract(start));
        if(resize) {
            int dX = start.x() - e.globalX(), dY = start.y() - e.globalY();
            int w = sSize.width(), h = sSize.height(),
                    mX = sPos.x(), mY = sPos.y();
            w = (resType == NE || resType == E || resType == SE) ? w - dX
                    : (resType == N || resType == S) ? w : w + dX;
            h = (resType == SE || resType == S || resType == SW) ? h - dY
                    : (resType == E || resType == W) ? h : h + dY;
            mX = (resType != SW && resType != W && resType != NW) ? mX
                    : (width() <= minW ? x() : mX - dX);
            mY = (resType != N && resType != NE && resType != NW) ? mY
                    : (height() <= minH ? y() : mY - dY);
            if(resType == N || resType == NE) {
                if(h > minH) move(mX, mY);
            } else if(resType == SW || resType == W) {
                if(w > minW) move(mX, mY);
            } else if(resType == NW) {
                if(w > minW && h > minH) move(mX, mY);
            }
            resize(w, h);
        }
    }
    
    @Override
    protected void mouseReleaseEvent(QMouseEvent e) {
        if(drag) drag = false;
        if(resize) resize = false;
    }
    
    @Override
    public boolean eventFilter(QObject object, QEvent e) {
        switch (e.type()) {
            case MouseMove:
                if(((QMouseEvent) e).button() == MouseButton.NoButton) {
                    QWidget child = (QWidget) object;
                    QMouseEvent ee = (QMouseEvent) e;
                    int x = ee.globalX() - x(), y = ee.globalY() - y();
                    if ((x < BORDER && y < BORDER)
                            || (x > width() - BORDER && y > height() - BORDER)){
                        cursorEvent = CursorShape.SizeFDiagCursor;
                    } else if ((x < BORDER && y > height() - BORDER)
                            || (x > width() - BORDER && y < BORDER)) {
                        cursorEvent = CursorShape.SizeBDiagCursor;
                    } else if (x < BORDER || x > width() - BORDER) {
                        cursorEvent = CursorShape.SizeHorCursor;
                    } else if (y < BORDER || y > height() - BORDER) {
                        cursorEvent = CursorShape.SizeVerCursor;
                    } else {
                        cursorEvent = CursorShape.ArrowCursor;
                    }
                    child.setCursor(new QCursor(cursorEvent));
                } else {
                    mouseMoveEvent((QMouseEvent) e);
                }
                return false;
            case MouseButtonPress:
                mousePressEvent((QMouseEvent) e);
                return false;
            case MouseButtonRelease:
                mouseReleaseEvent((QMouseEvent) e);
                return false;
            default:
                return false;
        }
    }
    
}
