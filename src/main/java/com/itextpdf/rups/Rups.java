/*
    This file is part of the iText (R) project.
    Copyright (c) 2007-2018 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.rups;

import com.itextpdf.kernel.Version;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.rups.controller.RupsController;
import com.itextpdf.rups.model.LoggerHelper;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.Observer;

public class Rups {

    private RupsController controller;

    private volatile CompareTool.CompareResult lastCompareResult = null;

    protected Rups() {
        this.controller = null;
    }

    protected RupsController getController() {
        return controller;
    }

    protected void setController(RupsController controller) {
        this.controller = controller;
    }

    /**
     * Initializes the main components of the Rups application.
     *
     * @param f                a file that should be opened on launch
     * @param onCloseOperation the close operation
     */
    public static void startNewApplication(final File f, final int onCloseOperation) {
        final Rups rups = new Rups();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame();
                // defines the size and location
                initFrameDim(frame);
                RupsController controller = new RupsController(frame.getSize(), frame, false);
                initApplication(frame, controller, onCloseOperation);
                rups.setController(controller);
                if (null != f && f.canRead()) {
                    rups.loadDocumentFromFile(f, false);
                }
            }
        });
    }

    public static Rups startNewPlugin(final JComponent comp, final Dimension size, final Frame frame) {
        final Rups rups = new Rups();
        RupsController controller = new RupsController(size, frame, true);
        comp.add(controller.getMasterComponent());
        rups.setController(controller);
        return rups;
    }

    public void loadDocumentFromFile(final File f, final boolean readOnly) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                controller.loadFile(f, readOnly);
            }
        });
    }

    public void loadDocumentFromStream(final InputStream inputStream, final String name, final File directory, final boolean readOnly) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                controller.loadFileFromStream(inputStream, name, directory, readOnly);
            }
        });
    }

    public void loadDocumentFromRawContent(final byte[] bytes, final String name, final File directory, final boolean readOnly) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                controller.loadRawContent(bytes, name, directory, readOnly);
            }
        });
    }

    public void closeDocument() {
        controller.closeRoutine();
    }

    public void saveDocumentAs(final File f) {
        controller.saveFile(f);
    }

    public boolean compareWithDocument(final PdfDocument document) {
        return compareWithDocument(document, false);
    }

    public boolean compareWithDocument(final PdfDocument document, final boolean showResults) {
        lastCompareResult = controller.compareWithDocument(document);
        if (!showResults) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    controller.highlightChanges(lastCompareResult);
                }
            });
        }
        return isEqual();
    }

    public boolean compareWithFile(final File file) {
        return compareWithFile(file, false);
    }

    public boolean compareWithFile(final File file, final boolean showResults) {
        lastCompareResult = controller.compareWithFile(file);
        if (!showResults) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    controller.highlightChanges(lastCompareResult);
                }
            });
        }
        return isEqual();
    }

    public boolean compareWithStream(final InputStream is) {
        return compareWithStream(is, false);
    }

    public boolean compareWithStream(final InputStream is, final boolean showResults) {
        lastCompareResult = controller.compareWithStream(is);
        if (!showResults) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    controller.highlightChanges(lastCompareResult);
                }
            });
        }
        return isEqual();
    }

    public void highlightLastSavedChanges() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                controller.highlightChanges(lastCompareResult);
            }
        });
    }

    public void clearHighlights() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                controller.highlightChanges(null);
            }
        });
    }

    public void requestLogToConsole(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LoggerHelper.info(message, getClass());
            }
        });
    }

    static void initApplication(JFrame frame, RupsController controller, final int onCloseOperation) {
        // title bar
        frame.setTitle("iText RUPS " + Version.getInstance().getVersion());
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Rups.class.getResource("logo.png")));
        frame.setDefaultCloseOperation(onCloseOperation);
        // the content
        frame.setJMenuBar(controller.getMenuBar());
        frame.getContentPane().add(controller.getMasterComponent(), java.awt.BorderLayout.CENTER);
        frame.setVisible(true);
    }

    static void initFrameDim(JFrame frame) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize((int) (screen.getWidth() * .90), (int) (screen.getHeight() * .90));
        frame.setLocation((int) (screen.getWidth() * .05), (int) (screen.getHeight() * .05));
        frame.setResizable(true);
    }

    private boolean isEqual() {
        return lastCompareResult != null && lastCompareResult.isOk();
    }

    public void registerEventObserver(Observer observer) {
        getController().addObserver(observer);
    }
    public void unregisterEventObserver(Observer observer) {
        getController().deleteObserver(observer);
    }

}