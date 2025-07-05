package PDF.Pad;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class Note1 {
    private JFrame mainFrame;
    private JMenuBar menuBar;
    private JMenu fileMenu, editMenu, formatMenu, viewMenu, helpMenu;
    private JMenuItem newFile, newWindow, openFile, saveFile, saveFileAs, pageSetup, print, exit;
    private JMenuItem undo, cut, copy, paste, delete, find, findNext, findPrevious, replace, goTo, selectAll, timeOrDate;
    private JMenuItem font, fontColor, backgroundColor, wordWrap;
    private JMenuItem zoomIn, zoomOut, resetZoom, restoreDefaultZoom, statusBar;
    private JMenuItem viewHelp, about;
    private JTextArea textArea;
    private JLabel statusLabel;
    private JCheckBoxMenuItem wordWrapItem;
    private JCheckBoxMenuItem statusBarItem;
    private UndoManager undoManager;
    private File currentFile;
    private int lastSearchIndex = -1;
    private String lastSearchTerm = "";
    private boolean matchCase = false;
    private Font currentFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private float zoomFactor = 1.0f;

    public Note1() {
        initializeUI();
        setupUndoManager();
        setupStatusBar();
        setupListeners();
    }

    private void initializeUI() {
        mainFrame = new JFrame("Note1Pad");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setFont(currentFont);
        textArea.setMargin(new Insets(5, 5, 5, 5));
        textArea.setTabSize(4);

        JScrollPane scrollPane = new JScrollPane(textArea);
        mainFrame.add(scrollPane, BorderLayout.CENTER);

        createMenus();
        mainFrame.setJMenuBar(menuBar);
        mainFrame.setVisible(true);
        mainFrame.setLocationRelativeTo(null);
    }

    private void createMenus() {
        menuBar = new JMenuBar();

        // File Menu
        fileMenu = new JMenu("File");
        newFile = createMenuItem("New", KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK);
        newWindow = createMenuItem("New Window", KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        openFile = createMenuItem("Open...", KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
        saveFile = createMenuItem("Save", KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
        saveFileAs = createMenuItem("Save As...", 0, 0);
        pageSetup = createMenuItem("Page Setup...", 0, 0);
        print = createMenuItem("Print...", KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK);
        exit = createMenuItem("Exit", 0, 0);

        fileMenu.add(newFile);
        fileMenu.add(newWindow);
        fileMenu.addSeparator();
        fileMenu.add(openFile);
        fileMenu.add(saveFile);
        fileMenu.add(saveFileAs);
        fileMenu.addSeparator();
        fileMenu.add(pageSetup);
        fileMenu.add(print);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        // Edit Menu
        editMenu = new JMenu("Edit");
        undo = createMenuItem("Undo", KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
        cut = createMenuItem("Cut", KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK);
        copy = createMenuItem("Copy", KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
        paste = createMenuItem("Paste", KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
        delete = createMenuItem("Delete", KeyEvent.VK_DELETE, 0);
        find = createMenuItem("Find...", KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK);
        findNext = createMenuItem("Find Next", KeyEvent.VK_F3, 0);
        findPrevious = createMenuItem("Find Previous", KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK);
        replace = createMenuItem("Replace...", KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK);
        goTo = createMenuItem("Go To...", KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK);
        selectAll = createMenuItem("Select All", KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK);
        timeOrDate = createMenuItem("Time/Date", KeyEvent.VK_F5, 0);

        editMenu.add(undo);
        editMenu.addSeparator();
        editMenu.add(cut);
        editMenu.add(copy);
        editMenu.add(paste);
        editMenu.add(delete);
        editMenu.addSeparator();
        editMenu.add(find);
        editMenu.add(findNext);
        editMenu.add(findPrevious);
        editMenu.add(replace);
        editMenu.add(goTo);
        editMenu.addSeparator();
        editMenu.add(selectAll);
        editMenu.add(timeOrDate);

        // Format Menu
        formatMenu = new JMenu("Format");
        wordWrapItem = new JCheckBoxMenuItem("Word Wrap");
        font = createMenuItem("Font...", 0, 0);
        fontColor = createMenuItem("Font Color...", 0, 0);
        backgroundColor = createMenuItem("Background Color...", 0, 0);

        formatMenu.add(wordWrapItem);
        formatMenu.addSeparator();
        formatMenu.add(font);
        formatMenu.add(fontColor);
        formatMenu.add(backgroundColor);

        // View Menu
        viewMenu = new JMenu("View");
        zoomIn = createMenuItem("Zoom In", KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK);
        zoomOut = createMenuItem("Zoom Out", KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK);
        resetZoom = createMenuItem("Reset Zoom", KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK);
        restoreDefaultZoom = createMenuItem("Restore Default Zoom", 0, 0);
        statusBarItem = new JCheckBoxMenuItem("Status Bar", true);

        viewMenu.add(zoomIn);
        viewMenu.add(zoomOut);
        viewMenu.add(resetZoom);
        viewMenu.add(restoreDefaultZoom);
        viewMenu.addSeparator();
        viewMenu.add(statusBarItem);

        // Help Menu
        helpMenu = new JMenu("Help");
        viewHelp = createMenuItem("View Help", KeyEvent.VK_F1, 0);
        about = createMenuItem("About Note1Pad", 0, 0);

        helpMenu.add(viewHelp);
        helpMenu.add(about);

        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(formatMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        // Set up action listeners
        setupActionListeners();
    }

    private JMenuItem createMenuItem(String text, int keyCode, int modifiers) {
        JMenuItem item = new JMenuItem(text);
        if (keyCode != 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
        }
        return item;
    }

    private void setupActionListeners() {
        // File Menu Actions
        newFile.addActionListener(e -> newFile());
        newWindow.addActionListener(e -> new Note1());
        openFile.addActionListener(e -> openFile());
        saveFile.addActionListener(e -> saveFile());
        saveFileAs.addActionListener(e -> saveFileAs());
        print.addActionListener(e -> printFile());
        exit.addActionListener(e -> System.exit(0));

        // Edit Menu Actions
        undo.addActionListener(e -> undo());
        cut.addActionListener(e -> textArea.cut());
        copy.addActionListener(e -> textArea.copy());
        paste.addActionListener(e -> textArea.paste());
        delete.addActionListener(e -> textArea.replaceSelection(""));
        find.addActionListener(e -> showFindDialog());
        findNext.addActionListener(e -> findNext());
        findPrevious.addActionListener(e -> findPrevious());
        replace.addActionListener(e -> showReplaceDialog());
        goTo.addActionListener(e -> showGoToDialog());
        selectAll.addActionListener(e -> textArea.selectAll());
        timeOrDate.addActionListener(e -> insertTimeDate());

        // Format Menu Actions
        wordWrapItem.addActionListener(e -> toggleWordWrap());
        font.addActionListener(e -> changeFont());
        fontColor.addActionListener(e -> changeFontColor());
        backgroundColor.addActionListener(e -> changeBackgroundColor());

        // View Menu Actions
        zoomIn.addActionListener(e -> zoom(1.1f));
        zoomOut.addActionListener(e -> zoom(0.9f));
        resetZoom.addActionListener(e -> resetZoom());
        restoreDefaultZoom.addActionListener(e -> restoreDefaultZoom());
        statusBarItem.addActionListener(e -> toggleStatusBar());

        // Help Menu Actions
        about.addActionListener(e -> showAboutDialog());
        viewHelp.addActionListener(e -> showHelp());
    }

    private void setupUndoManager() {
        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
    }

    private void setupStatusBar() {
        statusLabel = new JLabel("Line: 1, Column: 1");
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        mainFrame.add(statusPanel, BorderLayout.SOUTH);
    }

    private void setupListeners() {
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateStatusBar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateStatusBar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateStatusBar();
            }
        });

        textArea.addCaretListener(e -> updateStatusBar());
    }

    private void updateStatusBar() {
        if (statusBarItem.isSelected()) {
            int caretPos = textArea.getCaretPosition();
            int line = 1;
            int column = 1;
            
            try {
                int caretLine = textArea.getLineOfOffset(caretPos);
                int lineStart = textArea.getLineStartOffset(caretLine);
                column = caretPos - lineStart + 1;
                line = caretLine + 1;
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            
            statusLabel.setText("Line: " + line + ", Column: " + column);
        }
    }

    // File Operations
    private void newFile() {
        if (textArea.getText().isEmpty() || confirmSave()) {
            textArea.setText("");
            currentFile = null;
            mainFrame.setTitle("Note1Pad - New File");
        }
    }

    private void openFile() {
        if (!confirmSave()) return;
        
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                textArea.setText("");
                String line;
                while ((line = reader.readLine()) != null) {
                    textArea.append(line + "\n");
                }
                currentFile = file;
                mainFrame.setTitle("Note1Pad - " + file.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(mainFrame, "Error opening file: " + e.getMessage());
            }
        }
    }

    private void saveFile() {
        if (currentFile == null) {
            saveFileAs();
        } else {
            saveToFile(currentFile);
        }
    }

    private void saveFileAs() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            saveToFile(file);
            currentFile = file;
            mainFrame.setTitle("Note1Pad - " + file.getName());
        }
    }

    private void saveToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(textArea.getText());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(mainFrame, "Error saving file: " + e.getMessage());
        }
    }

    private boolean confirmSave() {
        if (textArea.getText().isEmpty()) return true;
        
        int result = JOptionPane.showConfirmDialog(mainFrame,
                "Do you want to save changes?",
                "Confirm Save",
                JOptionPane.YES_NO_CANCEL_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            saveFile();
            return true;
        }
        return result != JOptionPane.CANCEL_OPTION;
    }

    private void printFile() {
        try {
            textArea.print();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Print error: " + e.getMessage());
        }
    }

    // Edit Operations
    private void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    private void showFindDialog() {
        JTextField findField = new JTextField(20);
        JCheckBox caseCheck = new JCheckBox("Match case");
        
        Object[] message = {
            "Find what:", findField,
            caseCheck
        };

        int option = JOptionPane.showConfirmDialog(mainFrame, message, "Find", 
                JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            lastSearchTerm = findField.getText();
            matchCase = caseCheck.isSelected();
            lastSearchIndex = -1;
            findNext();
        }
    }

    private void findNext() {
        if (lastSearchTerm.isEmpty()) {
            showFindDialog();
            return;
        }
        
        String content = matchCase ? textArea.getText() : textArea.getText().toLowerCase();
        String searchTerm = matchCase ? lastSearchTerm : lastSearchTerm.toLowerCase();
        
        int startIndex = lastSearchIndex + 1;
        int foundIndex = content.indexOf(searchTerm, startIndex);
        
        if (foundIndex == -1 && startIndex > 0) {
            // Wrap around search
            foundIndex = content.indexOf(searchTerm);
        }
        
        if (foundIndex != -1) {
            textArea.select(foundIndex, foundIndex + searchTerm.length());
            textArea.grabFocus();
            lastSearchIndex = foundIndex;
        } else {
            JOptionPane.showMessageDialog(mainFrame, "Cannot find \"" + lastSearchTerm + "\"");
        }
    }

    private void findPrevious() {
        if (lastSearchTerm.isEmpty()) return;
        
        String content = matchCase ? textArea.getText() : textArea.getText().toLowerCase();
        String searchTerm = matchCase ? lastSearchTerm : lastSearchTerm.toLowerCase();
        
        int startIndex = lastSearchIndex - 1;
        if (startIndex < 0) startIndex = content.length() - 1;
        
        int foundIndex = content.lastIndexOf(searchTerm, startIndex);
        
        if (foundIndex == -1) {
            // Wrap around search
            foundIndex = content.lastIndexOf(searchTerm);
        }
        
        if (foundIndex != -1) {
            textArea.select(foundIndex, foundIndex + searchTerm.length());
            textArea.grabFocus();
            lastSearchIndex = foundIndex;
        } else {
            JOptionPane.showMessageDialog(mainFrame, "Cannot find \"" + lastSearchTerm + "\"");
        }
    }

    private void showReplaceDialog() {
        JTextField findField = new JTextField(20);
        JTextField replaceField = new JTextField(20);
        JCheckBox caseCheck = new JCheckBox("Match case");
        
        Object[] message = {
            "Find what:", findField,
            "Replace with:", replaceField,
            caseCheck
        };

        int option = JOptionPane.showConfirmDialog(mainFrame, message, "Replace", 
                JOptionPane.OK_CANCEL_OPTION);
        
        if (option == JOptionPane.OK_OPTION) {
            String findText = findField.getText();
            String replaceText = replaceField.getText();
            boolean matchCase = caseCheck.isSelected();
            
            String content = textArea.getText();
            if (!matchCase) {
                content = content.toLowerCase();
                findText = findText.toLowerCase();
            }
            
            textArea.setText(content.replaceAll(Pattern.quote(findText), replaceText));
        }
    }

    private void showGoToDialog() {
        String lineNum = JOptionPane.showInputDialog(mainFrame, "Line number:", "Go To", JOptionPane.PLAIN_MESSAGE);
        if (lineNum != null) {
            try {
                int line = Integer.parseInt(lineNum) - 1;
                int totalLines = textArea.getLineCount();
                
                if (line >= 0 && line < totalLines) {
                    try {
                        int start = textArea.getLineStartOffset(line);
                        int end = textArea.getLineEndOffset(line);
                        textArea.setCaretPosition(start);
                        textArea.select(start, end);
                        textArea.grabFocus();
                    } catch (BadLocationException e) {
                        JOptionPane.showMessageDialog(mainFrame, "Invalid line number");
                    }
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Line number out of range");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(mainFrame, "Please enter a valid number");
            }
        }
    }

    private void insertTimeDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa dd/MM/yyyy");
        textArea.insert(sdf.format(new Date()), textArea.getCaretPosition());
    }

    // Format Operations
    private void toggleWordWrap() {
        textArea.setLineWrap(wordWrapItem.isSelected());
        textArea.setWrapStyleWord(wordWrapItem.isSelected());
    }

    private void changeFont() {
        FontDialog fontDialog = new FontDialog(mainFrame, "Choose Font", textArea.getFont());
        fontDialog.setVisible(true);
        if (fontDialog.isOkSelected()) {
            currentFont = fontDialog.getSelectedFont();
            textArea.setFont(currentFont.deriveFont(currentFont.getSize2D() * zoomFactor));
        }
    }

    private void changeFontColor() {
        Color color = JColorChooser.showDialog(mainFrame, "Select Font Color", textArea.getForeground());
        if (color != null) {
            textArea.setForeground(color);
        }
    }

    private void changeBackgroundColor() {
        Color color = JColorChooser.showDialog(mainFrame, "Select Background Color", textArea.getBackground());
        if (color != null) {
            textArea.setBackground(color);
        }
    }

    // View Operations
    private void zoom(float factor) {
        zoomFactor *= factor;
        textArea.setFont(currentFont.deriveFont(currentFont.getSize2D() * zoomFactor));
    }

    private void resetZoom() {
        zoomFactor = 1.0f;
        textArea.setFont(currentFont);
    }

    private void restoreDefaultZoom() {
        zoomFactor = 1.0f;
        currentFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        textArea.setFont(currentFont);
    }

    private void toggleStatusBar() {
        statusLabel.setVisible(statusBarItem.isSelected());
        mainFrame.revalidate();
    }

    // Help Operations
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(mainFrame,
                "NotePad v1.0\n This is a Notepad \n© 2025 \n Created by Daniel Rein",
                "About  NotePad",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHelp() {
        JOptionPane.showMessageDialog(mainFrame,
                "Unfortunately I cant help \n seek help from Chatgpt.com ",
                "Note1Pad Help",
                JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Note1::new);
    }

    // Custom Font Dialog
    static class FontDialog extends JDialog {
        private boolean okSelected = false;
        private Font selectedFont;
        private final JList<String> fontList;
        private final JComboBox<Integer> sizeCombo;
        private final JCheckBox boldCheck, italicCheck;

        public FontDialog(Frame owner, String title, Font initialFont) {
            super(owner, title, true);
            setSize(400, 300);
            setLayout(new BorderLayout());

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] fonts = ge.getAvailableFontFamilyNames();
            fontList = new JList<>(fonts);
            fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fontList.setSelectedValue(initialFont.getFamily(), true);

            Integer[] sizes = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72};
            sizeCombo = new JComboBox<>(sizes);
            sizeCombo.setSelectedItem(initialFont.getSize());

            boldCheck = new JCheckBox("Bold", initialFont.isBold());
            italicCheck = new JCheckBox("Italic", initialFont.isItalic());

            JPanel controlPanel = new JPanel(new GridLayout(3, 1));
            controlPanel.add(sizeCombo);
            controlPanel.add(boldCheck);
            controlPanel.add(italicCheck);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(new JScrollPane(fontList), BorderLayout.CENTER);
            mainPanel.add(controlPanel, BorderLayout.EAST);

            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> {
                okSelected = true;
                setVisible(false);
            });

            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> setVisible(false));

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);

            add(mainPanel, BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        public boolean isOkSelected() {
            return okSelected;
        }

        public Font getSelectedFont() {
            int style = Font.PLAIN;
            if (boldCheck.isSelected()) style |= Font.BOLD;
            if (italicCheck.isSelected()) style |= Font.ITALIC;
            
            return new Font(
                fontList.getSelectedValue(),
                style,
                (Integer) sizeCombo.getSelectedItem()
            );
        }
    }
}