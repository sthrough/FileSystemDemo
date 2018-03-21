public class Utility {
    //某个目录下文件概括信息(包括文本文件和子目录)的大小（char）
    static public char SIZE_OF_FILEINFO = 16;

    //模拟扇区的大小
    static public char SIZE_OF_SECTOR = 512;

    //（根目录区）根目录的文件信息的最大数目（默认用了一个扇区存放。）
    static public char NUM_OF_ROOTFILE =
            (char) (SIZE_OF_SECTOR / SIZE_OF_FILEINFO);

    //（数据区）子目录的文件信息的最大数目（默认也只用一个扇区）
    static public char NUM_OF_SUBFILE =
            (char) (SIZE_OF_SECTOR / SIZE_OF_FILEINFO);

    //数据区总的扇区个数(默认有5M的数据空间)
    static public char NUM_OF_DATASECTOR = 3 * 1024;

    //Fat表中表示结束的标记
    static public char END_OF_FILE = '#'; //0xffff;
    static public char FREE_FOR_FAT = '*'; //0xfffe;

    //文件信息的相关信息定位
    //其中：12位的文件名，1位状态位，1位Fat表起始位，1位文件大小(目录该位不用)，1位标记只读
    static public char POS_NAME = 0;
    static public char LEN_OF_NAME = 12;
    static public char POS_STATE = 12;
    static public char POS_FAT = 13;
    static public char POS_SIZE = 14;
    static public char POS_READONLY = 15;

    //文件信息的状态标志类型
    static public char FREE = 100;
    static public char FILE = 101;
    static public char DIRECTORY = 102;

    //文件的占的最大扇区数
    static public char MAX_SECTOR = 100;

    //  界面参数
    static public final int WIDTH = 600;
    static public final int HEIGHT = 600;

    //默认的配置文件名
    static public final String INIT_FILE ="deploy.ini";
}
