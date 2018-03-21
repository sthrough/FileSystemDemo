import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class UICommander {
    public static void main(String[] args) {
        FileFrame frame = new FileFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.show();
    }
}

class FileFrame extends JFrame {
    public FileManager fileManager = new FileManager();
    public FileFrame() {
        setSize(Utility.WIDTH, Utility.HEIGHT);
        setTitle("FileManager(李雪阳)");

        FilePanel filePanel = new FilePanel(fileManager);
        Container contentPane = getContentPane();
        contentPane.add(filePanel);

        ////////////////////////////配置环境///////////////////////////
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                try {
                    DataOutputStream out =
                            new DataOutputStream(
                                    new BufferedOutputStream(
                                            new FileOutputStream("deploy.ini")));
                    for (int i = 0; i < Utility.NUM_OF_ROOTFILE; i++) {
                        for (int j = 0; j < Utility.SIZE_OF_FILEINFO; j++) {
                            out.writeChar(fileManager.rootTable[i][j]);
                        }

                    }

                    for (int i = 0; i < Utility.NUM_OF_DATASECTOR; i++) {
                        out.writeChar(fileManager.fatTable[i]);
                    }

                    for (int i = 0; i < Utility.NUM_OF_DATASECTOR; i++) {
                        for (int j = 0; j < Utility.SIZE_OF_SECTOR; j++) {
                            out.writeChar(fileManager.dataArea[i][j]);
                        }
                    }
                    out.close();

                } catch (Exception e) {
                    System.out.println(e);
                }
                System.out.println("windowClosing");
            }

            public void windowOpened(WindowEvent arg0) {
                try {
                    DataInputStream in =
                            new DataInputStream(
                                    new BufferedInputStream(
                                            new FileInputStream("deploy.ini")));

                    for (int i = 0; i < Utility.NUM_OF_ROOTFILE; i++) {
                        for (int j = 0; j < Utility.SIZE_OF_FILEINFO; j++) {
                            fileManager.rootTable[i][j] = in.readChar();
                        }

                    }

                    for (int i = 0; i < Utility.NUM_OF_DATASECTOR; i++) {
                        fileManager.fatTable[i] = in.readChar();
                    }

                    for (int j = 0; j < Utility.NUM_OF_DATASECTOR; j++) {
                        for (int i = 0; i < Utility.SIZE_OF_SECTOR; i++) {
                            fileManager.dataArea[j][i] = in.readChar();
                        }
                    }
                    in.close();

                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
        ///////////////////////////////////////////////////////////////////
    }
}

class FilePanel extends JPanel {
    private JFrame frame;
    private FileEditor fileEditor;
    private JTextArea textOutput;
    private JTextField textInput;
    private String currentPath = "MyRoot:\\>";
    private FileManager fileManager;

    public FilePanel(FileManager fileManager) {
        //初始化文件管理器
        this.fileManager = fileManager;

        //界面设置
        setLayout(new BorderLayout());
        Border brd = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK);

        //输入命令行
        textInput = new JTextField();
        textInput.setBorder(brd);
        textInput.setBackground(Color.YELLOW);
        //textInput.setForeground(Color.WHITE);

        KeyHandler KeyListener = new KeyHandler();
        textInput.addKeyListener(KeyListener);
        textInput.setFont(new Font("Verdana", Font.BOLD, 18));
        textInput.setFocusable(true);

        JLabel label = new JLabel("[INPUT]");
        label.setFont(new Font("Times New Roman", Font.BOLD, 15));
        label.setBorder(brd);
        label.setForeground(Color.black);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(label, BorderLayout.WEST);
        panel.add(textInput);

        this.add(panel, BorderLayout.SOUTH);

        //输出界面
        textOutput = new JTextArea();
        textOutput.setBorder(brd);
        textOutput.setLineWrap(true);
        textOutput.setWrapStyleWord(true);
        textOutput.setFocusable(false);
        textOutput.setBackground(Color.DARK_GRAY);
        textOutput.setForeground(Color.GREEN);
        textOutput.setFont(new Font("Verdana", Font.BOLD, 15));
        textOutput.append(currentPath);

        JScrollPane spOutput =
                new JScrollPane(
                        textOutput,
                        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.add(spOutput);
    }

    private class KeyHandler implements KeyListener {
        public void keyPressed(KeyEvent event) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.VK_ENTER
                    && textInput.getText().compareTo("") != 0) {
                handerInput(textInput.getText());
                textInput.setText("");
                textOutput.append(currentPath);
            }
        }
        public void keyReleased(KeyEvent event) {
        }

        public void keyTyped(KeyEvent event) {
        }
    }

    private void handerInput(String cmd) {
        textOutput.append(cmd + "\n");
        String cmdHead, cmdBody;

        //取指令
        int pos = cmd.indexOf(" ");
        if (pos == -1) {
            cmdHead = cmd;
            cmdBody = "";
        } else {
            cmdHead = cmd.substring(0, pos);
            cmdBody = cmd.substring(pos).trim();
        }
        cmdHead = cmdHead.toLowerCase();
        cmdBody = cmdBody.toLowerCase();
        //  textOutput.append(" cmdHead: " + cmdHead + " cmdBody: " + cmdBody);

        //////////////////////////////////////////////////////////////////////////
        //处理指令
        //CD       ( .. || \ || dirname)
        //DIR      Displays a list of files and subdirectories in a directory.
        //HELP     Provides Help information for my filemanager.
        //MD       Creates a directory.
        //RD       Removes a directory.
        //MF       Creates a file.
        //RF       Removes a file.
        //Edit     Edit a file.]
        //Exit
        /////////////////////////////////////////////////////////////////////////

        if (cmdHead.compareTo("cd") == 0) {
            handleCD(cmdBody);
        }
        else if (cmdHead.substring(0, 2).compareTo("cd") == 0 && cmdBody.compareTo("") == 0) {
            if (cmdHead.substring(2).trim().compareTo("\\") == 0
                    || cmdHead.substring(2).trim().compareTo("..") == 0
                    || cmdHead.substring(2).trim().compareTo(".") == 0) {
                String temp = cmdHead.substring(2).trim();
                System.out.println("here!!!!!!!!!!!!! " + temp);
                handleCD(temp);
            }
            else {
                textOutput.append("'" + cmd + "' is not a valid command !\nPlease input \'help\' to gain valid command ^_^ ");
            }
        }
        else if (cmdHead.compareTo("ls") == 0 && cmdBody.compareTo("") == 0) {
            handleDir();
        }
        else if (cmdHead.compareTo("help") == 0) {
            handleHelp();
        }
        else if (cmdHead.compareTo("md") == 0) {
            handleMd(cmdBody);
        }
        else if (cmdHead.compareTo("rd") == 0) {
            handleRd(cmdBody);
        }
        else if (cmdHead.compareTo("mf") == 0) {
            handleMf(cmdBody);
        }
        else if (cmdHead.compareTo("rf") == 0) {
            handleRf(cmdBody);
        }
        else if (cmdHead.compareTo("edit") == 0) {
            handleEdit(cmdBody);
        } else if (cmdHead.compareTo("format") == 0) {
            handleFormat();
        } else {
            textOutput.append(
                    "'"
                            + cmd
                            + "' is not a valid command !\nPlease input \'help\' to gain valid command ^_^ ");
        }
        textOutput.append("\n\n");
        textOutput.setCaretPosition(textOutput.getText().length());
    }

    void handleCD(String para) {

        if (fileManager.changeDirectory(para) == true) {
            this.currentPath = fileManager.getCurrentPath();
        } else {
            textOutput.append("The subDirectory doesn't exist!");
        }

    }
    void handleDir() {
        ArrayList fileList = fileManager.getCurrentDirInfo();
        String name;
        char type;
        textOutput.append("-------LIST OF FILES & DIRECTORIES-------\n");
        textOutput.append("     <DIR>       .\n");
        textOutput.append("     <DIR>       ..\n");
        int i;
        for (i = 0; i < fileList.size(); i++) {
            if (i != 0)
                textOutput.append("\n");

            String fileInfo = (String) fileList.get(i);
            name =
                    fileInfo
                            .substring(
                                    Utility.POS_NAME,
                                    Utility.POS_NAME + Utility.LEN_OF_NAME)
                            .trim();
            type = fileInfo.charAt(Utility.POS_STATE);

            if (type == Utility.DIRECTORY) {
                textOutput.append("     <DIR>       ");
            } else {
                textOutput.append("                        ");
            }
            textOutput.append(name);
        }
    }
    void handleHelp() {
        //textOutput.append("handleHelp()");
        textOutput.append(
                "[FORMAT] Formats the disk for use.\n"
                        + "[HELP]   Provides Help information for my filemanager.\n"
                        + "[LS]    Displays the files and subdirectories in a directory.\n"
                        + "[CD]     changes the current directory.\n"
                        + "             ( '.' or '..' or '\\' or name of the directory)\n"
                        + "[MD]     Creates a directory.\n"
                        + "[RD]     Removes a directory.\n"
                        + "[MF]     Creates a file.\n"
                        + "[RF]     Removes a file.\n"
                        + "[Edit]   Edit a file.\n");
    }
    void handleMd(String para) {
        if (para.length() == 0) {
            textOutput.append("Please input the name of the directory!");
            return;
        }
        if (para.length() >= 12) {
            textOutput.append(
                    "Create Fail:\nThe length of the name should between 1 and 12!");
            return;
        }
        if (fileManager.createInfo(Utility.DIRECTORY, para) == false) {
            textOutput.append(
                    "Create Fail:\nNames collide!Please input other name!");
            return;
        }
        textOutput.append(
                "Create the SubDirectory '" + para + "' successfully!");
        //textOutput.append("handleMd " + para);
    }
    void handleRd(String para) {
        if (para.length() == 0) {
            textOutput.append("Please input the name of the directory!");
            return;
        }
        if (fileManager.deleteInfo(Utility.DIRECTORY, para) == true) {
            textOutput.append(
                    "Delete the SubDirectory '" + para + "' successfully!");
        } else {
            textOutput.append("The SubDirectory '" + para + "' doesn't exist!");
        }
        //textOutput.append("handleRd " + para);
    }
    void handleMf(String para) {
        if (para.length() == 0) {
            textOutput.append("Please input the name of the file!");
            return;
        }
        if (para.length() >= 12) {
            textOutput.append(
                    "Create Fail:\nThe length of the name should between 1 and 12!");
            return;
        }
        if (fileManager.createInfo(Utility.FILE, para) == false) {
            textOutput.append(
                    "Create Fail:\nNames collide!Please input other name!");
            return;
        }
        textOutput.append("Create the file '" + para + "' successfully!");
    }
    void handleRf(String para) {
        if (para.length() == 0) {
            textOutput.append("Please input the name of the file!");
            return;
        }
        if (fileManager.deleteInfo(Utility.FILE, para) == true) {
            textOutput.append("Delete the file '" + para + "' successfully!");
        } else {
            textOutput.append("The file '" + para + "' doesn't exist!");
        }
        //textOutput.append("handleRf " + para);
    }

    void handleEdit(String para) {
        if (para.length() == 0) {
            textOutput.append("Please input the name of the file!");
            return;
        }
        StringBuffer content = new StringBuffer();
        if (fileManager.loadFile(para, content) == true) {
            fileEditor = new FileEditor(null, para);
            fileEditor.textArea.setText(content.toString());
            fileEditor.show();
        } else {
            textOutput.append("'" + para + "' doesn't exist!");
        }

    }

    void handleFormat() {
        fileManager.formatAll();
        textOutput.append("Format the File System sussfully!");
    }

    class FileEditor extends JDialog {
        JTextArea textArea = new JTextArea();
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Canel");
        String filename;

        public FileEditor(JFrame frame, String name) {
            super(frame, name, true);
            setSize(430, 430);
            setLocation(400, 150);
            setResizable(false);
            this.filename = name;

            Border brd =
                    BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK);
            textArea.setBorder(brd);
            textArea.setBackground(Color.WHITE);
            textArea.setFont(new Font("Arial", Font.TRUETYPE_FONT, 25));
            textArea.setLineWrap(true);
            ButtonListener listener = new ButtonListener();
            save.addActionListener(listener);
            cancel.addActionListener(listener);

            JScrollPane spEdit =
                    new JScrollPane(
                            textArea,
                            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            JPanel btPanel = new JPanel();
            btPanel.add(save);
            btPanel.add(cancel);

            Container container = getContentPane();
            container.setLayout(new BorderLayout());
            container.add(btPanel, BorderLayout.SOUTH);
            container.add(spEdit);
        }

        class ButtonListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                if ((JButton) e.getSource() == save) {
                    String content = textArea.getText();
                    if (fileManager.writeFile(filename, content) == true) {
                        dispose();
                        return;
                    }
                    //处理失败情况
                } else {
                    dispose();
                    return;
                }
            }
        }
    }
}
