import java.util.ArrayList;

public class FileManager {
    public static void main(String[] args) {
        //-----for test-----//

        //FileManager fileManager = new FileManager();
        //fileManager.testCreate();
        //fileManager.testChangeDir();
        //fileManager.testModify();
        //fileManager.testDelete();
    }

    //////////////////////////////////////
    //根目录区二维空间（默认大小占1个扇区）
    char[][] rootTable;

    //fat表,用于索引数据区
    char[] fatTable;

    //数据区，用于存放文本文件或者是目录信息
    char[][] dataArea;

    //记录当前目录信息的栈
    ArrayList directoryStack;

    //记录当前的路径
    ArrayList path;

    //////////////////////////////////////
    private void test() {
    }

    //构造函数，初始化一些信息
    public FileManager() {
        System.out.println("Create filemanager!");
        rootTable = new char[Utility.NUM_OF_ROOTFILE][Utility.SIZE_OF_FILEINFO];
        fatTable = new char[Utility.NUM_OF_DATASECTOR];
        dataArea = new char[Utility.NUM_OF_DATASECTOR][Utility.SIZE_OF_SECTOR];

        this.formatAll();

    }

    /**
     * 配置文件存储环境
     */
    private boolean deploy() {

        return true;
    }

    /**
     * 格式化和清除信息
     */
    //格式化所有信息（主要针对根目录表和Fat表）
    void formatAll() {
        //将文件信息的状态项表示为可用（其他信息在再次使用的时候清除）
        for (int i = 0; i < Utility.NUM_OF_ROOTFILE; i++) {
            rootTable[i][Utility.POS_STATE] = Utility.FREE;
            rootTable[i][Utility.POS_FAT] = Utility.FREE_FOR_FAT;
        }

        //清空Fat表（没有必要此时清除数据区的内容，Fat表的信息可控制数据区的信息）
        for (int i = 0; i < Utility.NUM_OF_DATASECTOR; i++) {
            fatTable[i] = Utility.FREE_FOR_FAT;
        }

        //新建目录栈
        directoryStack = new ArrayList();
        directoryStack.add(new Integer(-1)); //初始为根目录
        path = new ArrayList();
        path.add("MyRoot:");
        //System.out.println("stack = " + directoryStack.size());
    }

    //格式化相应的数据区的扇区内容(用于存放数据)
    void formatSectorForData(int freeIndex) {
        for (int i = 0; i < Utility.SIZE_OF_SECTOR; i++) {
            dataArea[freeIndex][i] = 0;
        }
    }

    //格式化相应的数据区的扇区内容(用于存放目录信息)
    void formatSectorForDir(int freeIndex) {
        char[] sector = dataArea[freeIndex];
        for (int i = 0; i < Utility.NUM_OF_SUBFILE; i++) {
            sector[i * Utility.SIZE_OF_FILEINFO + Utility.POS_STATE] =
                    Utility.FREE;
            sector[i * Utility.SIZE_OF_FILEINFO + Utility.POS_FAT] =
                    Utility.FREE_FOR_FAT;
        }
    }

    /**
     * 新建目录或文件信息项
     */
    boolean createInfo(char type, String name) {
        if (name.length() > 12) {
            return false;
        }
        int currentDirectory =
                ((Integer) directoryStack.get(directoryStack.size() - 1))
                        .intValue();
        int fatIndex;

        if (this.inDirectory(type, name, false) != -1) {
            System.out.println(
                    "collide fatIndex:" + inDirectory(type, name, false));
            return false;
        }

        //在根目录建
        if (currentDirectory == -1) {
            for (int i = 0; i < Utility.NUM_OF_ROOTFILE; i++) {
                if (rootTable[i][Utility.POS_STATE] == Utility.FREE) {
                    if ((fatIndex = getFreeSector(type)) != -1) {
                        rootTable[i][Utility.POS_FAT] = (char) fatIndex;
                        //format
                        rootTable[i][Utility.POS_STATE] = type;
                        this.changeNameOfFileInfo(
                                rootTable[i],
                                Utility.POS_NAME,
                                name);
                        return true;
                    }
                    return false;
                }
            }
            return false;
        }
        //在子目录建
        char[] subDirectory = dataArea[currentDirectory];

        for (int i = 0; i < Utility.NUM_OF_SUBFILE; i++) {
            if (subDirectory[i * Utility.SIZE_OF_FILEINFO + Utility.POS_STATE]
                    == Utility.FREE) {
                if ((fatIndex = getFreeSector(type)) != -1) {
                    subDirectory[i * Utility.SIZE_OF_FILEINFO
                            + Utility.POS_FAT] =
                            (char) fatIndex;
                    subDirectory[i * Utility.SIZE_OF_FILEINFO
                            + Utility.POS_STATE] =
                            type;
                    this.changeNameOfFileInfo(
                            subDirectory,
                            i * Utility.SIZE_OF_FILEINFO,
                            name);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private void testCreate() {
        System.out.println("------testCreate--------");
        this.createInfo(Utility.FILE, "file1");
        //this.createInfo(Utility.FILE, "file2");
        //this.createInfo(Utility.FILE, "file3");
        //this.showCurrentDirInfo();
        this.createInfo(Utility.DIRECTORY, "dir1");
        //this.createInfo(Utility.DIRECTORY, "dir2");
        //this.showCurrentDirInfo();
    }

    //在Fat表找空闲扇区,找不到返回-1
    int getFreeSector(char type) {
        for (int i = 0; i < Utility.NUM_OF_DATASECTOR; i++) {
            if (fatTable[i] == Utility.FREE_FOR_FAT) {
                if (type == Utility.FILE) {
                    this.formatSectorForData(i);
                } else if (type == Utility.DIRECTORY) {
                    this.formatSectorForDir(i);
                }
                fatTable[i] = Utility.END_OF_FILE;
                return i;
            }
        }
        return -1;
    }

    //修改根目录文件信息中的名字信息
    void changeNameOfFileInfo(char[] fileInfo, int index, String name) {
        for (int i = 0; i < Utility.LEN_OF_NAME; i++) {
            fileInfo[index + i] = 0;
        }
        for (int i = 0; i < name.length(); i++) {
            fileInfo[index + i] = name.charAt(i);
        }
    }

    /**
     * 修改文件所需的扇区
     */
    public boolean loadFile(String name, StringBuffer content) {
        int fatIndex = inDirectory(Utility.FILE, name, false);
        if (fatIndex == -1) {
            return false;
        }

        int nextIndex = fatTable[fatIndex];
        while (true) {
            content.append(
                    String
                            .valueOf(dataArea[fatIndex], 0, Utility.SIZE_OF_SECTOR)
                            .trim());
            System.out.println("file content : " + content.toString());
            if (nextIndex == Utility.END_OF_FILE) {
                return true;
            }
            fatIndex = nextIndex;
            nextIndex = fatTable[fatIndex];
        }
    }

    public boolean writeFile(String name, String content) {
        int fatIndex = inDirectory(Utility.FILE, name, false);
        if (fatIndex == -1) {
            return false;
        }
        if (content.length() == 0) {
            return true;
        }

        int objectTotal =
                content.length() % Utility.SIZE_OF_SECTOR == 0
                        ? content.length() / Utility.SIZE_OF_SECTOR
                        : content.length() / Utility.SIZE_OF_SECTOR + 1;

        if (this.modifySector(name, objectTotal) == false) {
            return false;
        }

        int bufferIndex = 0;
        int bufferLeft = content.length();

        for (int i = 0; i < objectTotal; i++) {
            if (i == objectTotal - 1) {
                for (int j = 0; j < bufferLeft; j++) {
                    char a = content.charAt(bufferIndex++);
                    this.dataArea[fatIndex][j] = a;
                }
            } else {
                for (int k = 0; k < Utility.SIZE_OF_SECTOR; k++) {
                    this.dataArea[fatIndex][k] = content.charAt(bufferIndex++);
                }
            }
            bufferLeft = bufferLeft - Utility.SIZE_OF_SECTOR;
            fatIndex = fatTable[fatIndex];
        }
        return true;
    }

    //根据目标大小修改文件的扇区
    boolean modifySector(String name, int objectTotal) {
        int fatIndex = this.inDirectory(Utility.FILE, name, false);
        if (fatIndex == -1) {
            return false;
        }
        showFatList(fatIndex);

        int initIndex = fatIndex;
        int[] fatArray = new int[Utility.MAX_SECTOR];
        int arrayIndex = -1;

        fatArray[++arrayIndex] = fatIndex;
        while (true) {
            if (fatTable[fatIndex] != Utility.END_OF_FILE) {
                fatArray[++arrayIndex] = fatTable[fatIndex];
                fatIndex = fatArray[arrayIndex];
            } else
                break;
        }
        int orientTotal = arrayIndex + 1;
        /*System.out.println(
            "orient total : "
                + orientTotal
                + "   object total : "
                + objectTotal);*/
        int diff = Math.abs(objectTotal - orientTotal);
        if (objectTotal > orientTotal) {
            int freeIndex;
            for (int i = 0; i < diff; i++) {
                freeIndex = getFreeSector(Utility.FILE);
                if (freeIndex == -1) {
                    return false;
                }
                fatArray[++arrayIndex] = freeIndex;
            }
            //保证了空间足够
            arrayIndex = orientTotal - 1;
            for (int i = 0; i < diff; i++) {
                fatTable[fatArray[arrayIndex]] =
                        (char) fatArray[arrayIndex + 1];
                arrayIndex++;
            }
            fatTable[fatArray[arrayIndex]] = Utility.END_OF_FILE;
            showFatList(initIndex);
        }
        if (objectTotal < orientTotal) {
            arrayIndex = objectTotal - 1;
            fatTable[arrayIndex] = Utility.END_OF_FILE;
            for (int i = 1; i <= diff; i++) {
                fatTable[fatArray[arrayIndex + i]] = Utility.FREE_FOR_FAT;
            }
            showFatList(initIndex);
        }
        return true;
    }

    private void showFatList(int fatIndex) {
        System.out.print("Fat List : " + fatIndex);
        while (fatTable[fatIndex] != Utility.END_OF_FILE) {
            fatIndex = fatTable[fatIndex];
            System.out.print(" " + fatIndex);
        }
        System.out.println(" " + fatTable[fatIndex]);
    }

    private void testModify() {
        System.out.println("------testModify--------");
        this.showCurrentDirInfo();
        this.createInfo(Utility.FILE, "file1");
        this.createInfo(Utility.DIRECTORY, "dir1");
        this.showCurrentDirInfo();
        this.changeDirectory("dir1");
        this.createInfo(Utility.FILE, "dir1");
        this.showCurrentDirInfo();
        modifySector("dir1", 3);
        this.showCurrentDirInfo();
    }

    /**
     * 删除目录或文件信息项
     */
    //删除当前目录下的文件或子目录
    boolean deleteInfo(char type, String name) {
        if (type == Utility.FILE) {
            int fatIndex = this.inDirectory(Utility.FILE, name, true);
            if (fatIndex != -1) {
                deleteSectorList(fatIndex);
                return true;
            }
        } else if (type == Utility.DIRECTORY) {
            int fatIndex = this.inDirectory(Utility.DIRECTORY, name, true);
            if (fatIndex != -1) {
                deleteSubDir(fatIndex);
                return true;
            }
        }
        return false;
    }

    private void deleteSubDir(int fatIndex) {
        deleteSectorList(fatIndex);
        char[] subDirectory = dataArea[fatIndex];
        for (int i = 0; i < Utility.NUM_OF_SUBFILE; i++) {
            int initPos = i * Utility.SIZE_OF_FILEINFO;
            if (subDirectory[initPos + Utility.POS_STATE] != Utility.FREE) {
                if (subDirectory[initPos + Utility.POS_STATE]
                        == Utility.FILE) {
                    deleteSectorList(subDirectory[initPos + Utility.POS_FAT]);
                } else if (
                        subDirectory[initPos + Utility.POS_STATE]
                                == Utility.DIRECTORY) {
                    deleteSubDir(subDirectory[initPos + Utility.POS_FAT]);
                }
                subDirectory[initPos + Utility.POS_STATE] = Utility.FREE;
            }
        }
    }

    //在Fat表中清除某个文件信息的扇区链
    void deleteSectorList(int firstIndex) {
        if (firstIndex < 0 || firstIndex > Utility.NUM_OF_DATASECTOR) {
            return;
        }

        int nextIndex = fatTable[firstIndex];
        while (true) {
            //  System.out.println(
            //      "allocFreeSector " + firstIndex + "  " + (int) nextIndex);
            fatTable[firstIndex] = Utility.FREE_FOR_FAT;
            if (nextIndex == Utility.END_OF_FILE) {
                return;
            }
            firstIndex = nextIndex;
            nextIndex = fatTable[firstIndex];
        }
    }

    private void testDelete() {
        System.out.println("------testDelete--------");
        this.showCurrentDirInfo();

        changeDirectory("dir1");
        this.showCurrentDirInfo();

        changeDirectory("dir2");
        this.showCurrentDirInfo();

        changeDirectory("dir3");
        this.showCurrentDirInfo();

        //////delete subdir
        changeDirectory("\\");
        this.showCurrentDirInfo();

        deleteInfo(Utility.FILE, "file1");
        this.showCurrentDirInfo();

        deleteInfo(Utility.DIRECTORY, "dir1");
        this.showCurrentDirInfo();

        //////recreate
        this.createInfo(Utility.FILE, "file6");
        this.createInfo(Utility.DIRECTORY, "dir6");
        this.showCurrentDirInfo();

        changeDirectory("dir6");
        this.createInfo(Utility.FILE, "file7");
        this.showCurrentDirInfo();
        //this.modifySector("file7",3);
        this.createInfo(Utility.DIRECTORY, "dir7");
        this.showCurrentDirInfo();

        this.changeDirectory("\\");
        this.deleteInfo(Utility.FILE, "file6");
        changeDirectory("dir6");
        changeDirectory("dir7");
        this.showCurrentDirInfo();

        this.createInfo(Utility.FILE, "file8");
        this.createInfo(Utility.DIRECTORY, "dir8");
        this.showCurrentDirInfo();

    }

    /**
     * 改变当前目录
     */
    boolean changeDirectory(String name) {
        if (name.length() > 12) {
            return false;
        }

        //本目录
        if (name.compareTo(".") == 0) {
            return true;
        }

        //返回上一级目录
        if (name.compareTo("..") == 0) {
            if (directoryStack.size() <= 1) {
                return true;
            }
            path.remove(path.size() - 1);
            directoryStack.remove(directoryStack.size() - 1);
            return true;
        }

        //返回根目录
        if (name.compareTo("\\") == 0) {
            int deleteSize = directoryStack.size() - 1;
            for (int i = 1; i <= deleteSize; i++) {
                path.remove(1);
                directoryStack.remove(1);
            }
            return true;
        }

        //进入一个新的目录
        int fatIndex = inDirectory(Utility.DIRECTORY, name, false);
        if (fatIndex != -1) {
            path.add(name);
            directoryStack.add(new Integer(fatIndex));
            return true;
        }
        return false;
    }

    private void testChangeDir() {
        System.out.println("------testChangeDir--------");
        //this.showCurrentDirInfo();

        if (!changeDirectory("dir1"))
            System.out.println("can't change");
        //this.showCurrentDirInfo();

        this.createInfo(Utility.FILE, "file2");
        this.createInfo(Utility.DIRECTORY, "dir2");
        //this.showCurrentDirInfo();
        if (!changeDirectory("dir2"))
            System.out.println("can't change");
        //this.showCurrentDirInfo();

        this.createInfo(Utility.FILE, "file3");
        this.createInfo(Utility.DIRECTORY, "dir3");
        //this.showCurrentDirInfo();
        if (!changeDirectory("dir3"))
            System.out.println("can't change");
        //this.showCurrentDirInfo();

        if (!changeDirectory("\\"))
            System.out.println("can't change");
        //this.showCurrentDirInfo();
    }

    /**
     * 目录信息读取和判断
     */
    //判断文件和子目录的名字是否在当前目录中
    int inDirectory(char type, String name, boolean delete) {
        String tempName;
        //根目录下
        if (((Integer) directoryStack.get(directoryStack.size() - 1))
                .intValue()
                == -1) {
            if (type == Utility.FILE) {
                for (int i = 0; i < Utility.NUM_OF_ROOTFILE; i++) {
                    if (rootTable[i][Utility.POS_STATE] == Utility.FILE) {
                        tempName =
                                new String(
                                        rootTable[i],
                                        Utility.POS_NAME,
                                        Utility.LEN_OF_NAME)
                                        .trim();
                        if (tempName.compareTo(name) == 0) {
                            if (delete == true) {
                                rootTable[i][Utility.POS_STATE] = Utility.FREE;
                            }
                            return rootTable[i][Utility.POS_FAT];
                        }
                    }
                }
            } else if (type == Utility.DIRECTORY) {
                for (int i = 0; i < Utility.NUM_OF_ROOTFILE; i++) {
                    if (rootTable[i][Utility.POS_STATE] == Utility.DIRECTORY) {
                        tempName =
                                new String(
                                        rootTable[i],
                                        Utility.POS_NAME,
                                        Utility.LEN_OF_NAME)
                                        .trim();
                        if (tempName.compareTo(name) == 0) {
                            if (delete == true) {
                                rootTable[i][Utility.POS_STATE] = Utility.FREE;
                            }
                            return rootTable[i][Utility.POS_FAT];
                        }
                    }
                }
            }
            return -1;
        }

        //普通子目录下
        int fatIndex =
                ((Integer) directoryStack.get(directoryStack.size() - 1))
                        .intValue();
        char[] subDirectory = dataArea[fatIndex];

        if (type == Utility.FILE) {
            for (int i = 0; i < Utility.NUM_OF_SUBFILE; i++) {
                if (subDirectory[i * Utility.SIZE_OF_FILEINFO
                        + Utility.POS_STATE]
                        == Utility.FILE) {
                    tempName =
                            new String(
                                    subDirectory,
                                    i * Utility.SIZE_OF_FILEINFO + Utility.POS_NAME,
                                    Utility.LEN_OF_NAME)
                                    .trim();
                    if (tempName.compareTo(name) == 0) {
                        if (delete == true) {
                            subDirectory[i * Utility.SIZE_OF_FILEINFO
                                    + Utility.POS_STATE] =
                                    Utility.FREE;
                        }
                        return subDirectory[i * Utility.SIZE_OF_FILEINFO
                                + Utility.POS_FAT];
                    }
                }
            }
        } else if (type == Utility.DIRECTORY) {
            for (int i = 0; i < Utility.NUM_OF_SUBFILE; i++) {
                if (subDirectory[i * Utility.SIZE_OF_FILEINFO
                        + Utility.POS_STATE]
                        == Utility.DIRECTORY) {
                    tempName =
                            new String(
                                    subDirectory,
                                    i * Utility.SIZE_OF_FILEINFO + Utility.POS_NAME,
                                    Utility.LEN_OF_NAME)
                                    .trim();
                    if (tempName.compareTo(name) == 0) {
                        if (delete == true) {
                            subDirectory[i * Utility.SIZE_OF_FILEINFO
                                    + Utility.POS_STATE] =
                                    Utility.FREE;
                        }
                        return subDirectory[i * Utility.SIZE_OF_FILEINFO
                                + Utility.POS_FAT];
                    }
                }
            }
        }
        return -1;
    }

    //得到当前目录的信息
    ArrayList getCurrentDirInfo() {
        ArrayList infoList = new ArrayList();
        int currentDirectory =
                ((Integer) directoryStack.get(directoryStack.size() - 1))
                        .intValue();
        System.out.println("currentDirectory : " + currentDirectory);
        if (currentDirectory == -1) {
            for (int i = 0; i < Utility.NUM_OF_ROOTFILE; i++) {
                if (rootTable[i][Utility.POS_STATE] != Utility.FREE) {
                    infoList.add(new String(rootTable[i]));
                }
            }
            return infoList;
        }

        char[] subDirectory = dataArea[currentDirectory];
        for (int i = 0; i < Utility.NUM_OF_SUBFILE; i++) {
            if (subDirectory[i * Utility.SIZE_OF_FILEINFO + Utility.POS_STATE]
                    != Utility.FREE) {
                String addStr =
                        new String(
                                subDirectory,
                                i * Utility.SIZE_OF_FILEINFO,
                                Utility.SIZE_OF_FILEINFO);
                infoList.add(addStr);
            }
        }
        return infoList;
    }

    //显示当前目录信息
    private void showCurrentDirInfo() {
        ArrayList infoList = this.getCurrentDirInfo();
        String name;
        char state, fatIndex, size, readonly;
        for (int i = 0; i < infoList.size(); i++) {
            String temp = (String) infoList.get(i);
            name = temp.substring(0, 12);
            state = temp.charAt(12);
            fatIndex = temp.charAt(13);
            size = temp.charAt(14);
            readonly = temp.charAt(15);
            System.out.println(
                    "Name : "
                            + name.trim()
                            + "  State:"
                            + (int) state
                            + "  Fat:"
                            + (int) fatIndex
                            + "  Size:"
                            + (int) size
                            + "  ReadOnly:"
                            + (int) readonly);
        }
        if (infoList.size() == 0) {
            System.out.println("Empty!!");
        }
        return;
    }

    public String getCurrentPath() {
        String pathStr = "", temp;
        for (int i = 0; i < path.size(); i++) {
            temp = (String) path.get(i);
            pathStr = pathStr + temp + "\\";
        }
        pathStr = pathStr + ">";
        System.out.println(pathStr);
        return pathStr;
    }
}
