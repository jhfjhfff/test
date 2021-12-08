package demo1;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("all")
//编写俄罗斯方块主类
public class Tetris extends JPanel {

    //声明正在下落的方块
    private Tetromino currentOne = Tetromino.randomOne();
    //声明将要下落的方块
    private Tetromino nextOne = Tetromino.randomOne();
    //声明游戏的主区域
    private Cell[][] wall = new Cell[18][9];
    //声明一个单元格的值为48像素
    private static final int CELL_SIZE = 48;

    //声明游戏分数池
    int[] scores_poll = {0,1,2,5,10};
    //声明当前游戏的分数
    private int totalScore = 0;
    //声明当前消除的行数；
    private int totalLine = 0;

    //声明游戏的三种状态，分别是：游戏中，暂停，结束
    public static final int PLAYING = 0;
    public static final int PAUSE = 1;
    public static final int GAMEOVER = 2;
    //声明变量存放当前游戏状态的值
    private int game_state;
    //声明一个数组，用来显示游戏状态
    String[] show_state = {"P[pause]","C[continue]","S[replay]"};

    //载入方块图片
    public static BufferedImage I;
    public static BufferedImage J;
    public static BufferedImage L;
    public static BufferedImage O;
    public static BufferedImage S;
    public static BufferedImage T;
    public static BufferedImage Z;
    public static BufferedImage backImage;

    static{
        try {
            I = ImageIO.read(new File("resources/images/I.png"));
            J = ImageIO.read(new File("resources/images/I.png"));
            L = ImageIO.read(new File("resources/images/I.png"));
            O = ImageIO.read(new File("resources/images/I.png"));
            S = ImageIO.read(new File("resources/images/I.png"));
            T = ImageIO.read(new File("resources/images/I.png"));
            Z = ImageIO.read(new File("resources/images/I.png"));
            backImage = ImageIO.read(new File("resources/images/background.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(backImage,0,0,null);
        //平移坐标轴
        g.translate(22,15);
        //绘制游戏的主区域
        paintWall(g);
        
        //绘制正在下落的四方格
        paintCurrentOne(g);

        //绘制将要下落的四方格
        paintNextOne(g);
        
        //绘制游戏得分
        paintScore(g);

        //绘制当前游戏状态
        paintState(g);
    }

    //游戏按键
    public void start(){
        game_state = PLAYING;
        KeyListener l = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                switch (code){
                    case KeyEvent.VK_DOWN:
                        sortDropAction();//下落一格
                        break;
                    case KeyEvent.VK_LEFT:
                        movelLeftAction();//左移一格
                        break;
                    case KeyEvent.VK_RIGHT:
                        movelRightAction();//右移一格
                        break;
                    case KeyEvent.VK_UP:
                        rorateRightAction();//顺时针旋转
                        break;
                    case KeyEvent.VK_SPACE:
                        handDropAction();//瞬间下落
                        break;
                    case KeyEvent.VK_P:
                        //判断当前游戏的状态
                        if (game_state == PLAYING){
                            game_state = PAUSE;
                        }
                        break;
                    case KeyEvent.VK_C:
                        //判断当前游戏的状态
                        if (game_state == PAUSE){
                            game_state = PLAYING;
                        }
                        break;
                    case KeyEvent.VK_S:
                        //表示游戏重新开始
                        game_state = PLAYING;
                        wall = new Cell[18][9];
                        currentOne = Tetromino.randomOne();
                        nextOne = Tetromino.randomOne();
                        totalScore = 0;
                        totalLine = 0;
                        break;

                }
            }
        };

        //将俄罗斯方块设置为焦点
        this.addKeyListener(l);
        this.requestFocus();

        //判断当前游戏状态在游戏中时，每隔0.5s下落一格
        while (true){
            if (game_state == PLAYING){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //判断能否下落
                if (canDrop()){
                    currentOne.softDrop();
                }else{
                    //嵌入到墙中
                    landToWall();
                    //判断能否消行
                    destoryLine();
                    //判断游戏是否结束
                    if (isGameOver()){
                        game_state = GAMEOVER;
                    }else {
                        currentOne = nextOne;
                        nextOne = Tetromino.randomOne();
                    }
                }
            }
            repaint();
        }

    }

    //按键一下，四方格下落一格
    public void sortDropAction(){
        //判断能否下落
        if (canDrop()){
            //当前四方格下落一格
            currentOne.softDrop();
        }else{
            //将四方格嵌入到墙中
            landToWall();
            //判断能否消行
            destoryLine();
            //判断游戏是否结束
            if (isGameOver()){
                game_state = GAMEOVER;
            }else{
                //当游戏没有结束时，继续生成新的四方格
                currentOne = nextOne;
                nextOne = Tetromino.randomOne();
            }

        }
    }

    //创建顺时针旋转
    public void rorateRightAction(){
        currentOne.rotateRight();
        //判断是否越界或者是否重合
        if (outOfBounds() || coincide()){
            currentOne.rotateLeft();
        }
    }


    //瞬间下落
    public void handDropAction(){

        while (true){
            //判断四方格能否下落
            if (canDrop()){
                currentOne.softDrop();
            }else {
                break;
            }
        }
        //嵌入到墙中
        landToWall();
        //判断能否消行
        destoryLine();
        //判断游戏是否结束
        if (isGameOver()){
            game_state = GAMEOVER;
        }else{
            //游戏没有结束，继续生成一个新的四方格
            currentOne = nextOne;
            nextOne = Tetromino.randomOne();
        }
    }

    //把当前四方格嵌入到墙中
    private void landToWall() {
        Cell[] cells = currentOne.cells;;
        for (Cell cell :
                cells) {
            int row = cell.getRow();
            int col = cell.getCol();
            wall[row][col] = cell;
        }
    }

    //判断四方格能否下落
    public boolean canDrop(){
        Cell[] cells = currentOne.cells;
        for (Cell cell :
                cells) {
            int row = cell.getRow();
            int col = cell.getCol();
            //判断是否到达底部
            if (row == wall.length -1){
                return false;
            }else if(wall[row+1][col] != null){
                return false;
            }
        }
        return true;
    }

    //创建消行方法
    public void destoryLine(){
        //声明变量，统计当前消除的总行数
        int line = 0;
        Cell[] cells = currentOne.cells;
        for (Cell cell :
                cells) {
            int row = cell.getRow();
            //判断当前行已满
            if (isFullLine(row)){
                line++;
                for (int i=row;i>0;i--){
                    System.arraycopy(wall[i-1],0,wall[i],0,wall[0].length);
                }
                //wall[0] = new Cell[9];
            }
        }

        //在分数池中获取分数，累加到总分数里面
        totalScore += scores_poll[line];
        //统计消除总行数的值
        totalLine += line;
    }

    //判断当前行是否已满
    public boolean isFullLine(int row){
        Cell[] cells = wall[row];
        for (Cell cell :
                cells) {
            if (cell == null){
                return false;
            }
        }

        return true;
    }

    //实现判断游戏是否结束
    public boolean isGameOver(){
        Cell[] cells = nextOne.cells;
        for (Cell cell :
                cells) {
            int row = cell.getRow();
            int col = cell.getCol();
            if (wall[row][col]!=null){
                return true;
            }
        }

        return false;
    }

    private void paintState(Graphics g) {
        if (game_state == PLAYING){
            g.drawString(show_state[PLAYING],500,660);
        } else if(game_state == PAUSE){
            g.drawString(show_state[PAUSE],500,660);
        } else if (game_state == GAMEOVER){
            g.drawString(show_state[GAMEOVER],500,660);
            g.setColor(Color.red);
            g.setFont (new Font(Font.SANS_SERIF,Font.BOLD,52));
            g.drawString("GAME OVER!!!",36,400);
        }
    }

    private void paintScore(Graphics g) {
        g.setFont(new Font(Font.SANS_SERIF,Font.BOLD,30));
        g.drawString("SCORE:"+ totalScore,500,248);
        g.drawString("LINE:"+totalLine,500,435);
    }

    private void paintNextOne(Graphics g) {
        Cell[] cells = nextOne.cells;
        for (Cell cell :
                cells) {
            int x = cell.getCol() * CELL_SIZE + 370;
            int y = cell.getRow() * CELL_SIZE + 20;
            g.drawImage(cell.getImage(),x,y,null);
        }
    }

    private void paintCurrentOne(Graphics g) {
        Cell[] cells = currentOne.cells;
        for (Cell cell :
                cells) {
            int x = cell.getCol() * CELL_SIZE;
            int y = cell.getRow() * CELL_SIZE;
            g.drawImage(cell.getImage(),x,y,null);
        }
    }

    private void paintWall(Graphics g) {
        for (int i=0;i<wall.length;i++){//wall.length指的是行数
            for (int j=0;j<wall[i].length;j++){//wall[i].length指的是列数
                int x = j*CELL_SIZE;
                int y = i*CELL_SIZE;
                Cell cell = wall[i][j];
                //判断当前单元格是否有小方块，如果没有则绘制矩形。否则将小方块嵌入到墙中
                if (cell == null){
                    g.drawRect(x,y,CELL_SIZE,CELL_SIZE);
                }else {
                    g.drawImage(cell.getImage(),x,y,null);
                }

            }
        }
    }

    //判断游戏是否出界
    public boolean outOfBounds(){
        Cell[] cells = currentOne.cells;
        for (Cell cell :
                cells) {
            int col = cell.getCol();
            int row = cell.getRow();
            if (row < 0 || row > wall.length-1 || col < 0 || col > wall[1].length-1){
                return true;
            }
        }

        return false;
    }

    //判断方块是否重合
    public boolean coincide(){
        Cell[] cells = currentOne.cells;
        for (Cell cell :
                cells) {
            int col = cell.getCol();
            int row = cell.getRow();
            if (wall[row][col] != null){
                return true;
            }
        }

        return  false;
    }

    //按键一次四方格左移一次
    public void movelLeftAction(){
        currentOne.moveLeft();
        //判断是否越界，或者四方格是否重合
        if (outOfBounds() || coincide()){
            currentOne.moveRight();
        }
    }

    //按键一次四方格右移一次
    public void movelRightAction(){
        currentOne.moveRight();
        //判断是否越界，或者四方格是否重合
        if (outOfBounds() || coincide()){
            currentOne.moveLeft();
        }
    }


    public static void main(String[] args) {
        //创建一个窗口对象
        JFrame frame = new JFrame("俄罗斯方块");
        //创建游戏界面，也就是面板
        Tetris panel = new Tetris();
        //将面板嵌入到窗口中
        frame.add(panel);
        //设置可见
        frame.setVisible(true);
        //设置窗口的尺寸
        frame.setSize(810,940);
        //设置窗口居中
        frame.setLocationRelativeTo(null);
        //窗口关闭的时候，程序终止
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //游戏主要逻辑封装在方法中
        panel.start();
    }
}
