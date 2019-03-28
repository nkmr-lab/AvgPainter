import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class AvgPainter extends PApplet {


// AvgPainter: 平均化によって手書きを綺麗にするドローイングツール
// implemented By 新納真次郎

// キャンバスの設定
PGraphics Canvas;
public int CanvasSizeX;
public int CanvasSizeY;
public int MenuAreaSize;

// 仮キャンバス(最後のストロークを表示するキャンバス)
PGraphics AvgCanvas;

// ペンの設定 初期設定 太さ:1px, 色:黒Str
String strokeType = "pen";
int strokeW = 1;
int strokeC = color( 0, 0, 0 );

int selectColor = 11;

// カラーエディタの設定

// フォント
PFont font;

// アイコン定義
PImage new_icon;
PImage sav_icon;
PImage lod_icon;
PImage und_icon;
PImage stw_icon;



ADRadio radioButton;
StrokeWeightPanel strWPanel;

// チェックボタンの設定
String[] checkButtonText = {
  "アンドゥした線と平均化", 
  "近しい実線と平均化"
};
ADCheck checkButton;


//デバッグ用
int debugFileNum=0;
int averageStroke=0;

//int radio;

boolean avg_strokePressed;
// ==============================================
// 平均ペン
// フーリエの最大次数（次数を高くし過ぎると色々問題が有るため）
int g_iMaxDegreeOfFourier = 50;
// 法線ベクトルを表示するかどうか（現在そもそも削除）
boolean g_bShowNormalVector = false;
// フーリエ級数展開の係数カットの閾値
float g_fThresholdOfCoefficient = 0.001f;
// フルスクリーンにするかどうかのフラグ
boolean g_bFullScreen = false;
// スプライン補間した後の近接点の除去に利用
double g_fThresholdToRemove = 0.05f;
// スプライン補間する際の倍数
int g_iMultiple = 10;
// 平均化対象のストロークの距離の閾値
//int g_iDistance = 5; // a0使うとき
float g_fDistance = 0.5f; // a0使わないとき


boolean g_bStroking = false;
boolean g_bStrokeStarting = false;
PointF [] g_mouseStroke;

PImage []temp;
int avg_count = 0;
int avg_time = 0;
int tmpColor;


boolean ave_selected;

boolean normalPen_drawing = false;

boolean pushedUndo=false;

int paint_count;
// 続けてストロークを書いた回数（メニュー欄のボタンを押したとき0になる）

int averageType;
//1:2本の線を引いた時の平均化 2:アンドゥと引いた線の平均化

int listNum=0;

boolean clickedAvgStroke=false;

//
float strokeDist;
int beforeX;
int beforeY;
int pointBisible=0;

// ==============================================


Button btn_new = new Button( 5+60*0, 25, "新規作成");
Button btn_sav = new Button( 5+60*1, 25, "保存");
Button btn_lod = new Button( 5+60*2, 25, "読み込み");
Button btn_und = new Button( 5+60*3, 25, "戻る");
Button btn_stw = new Button( 5+60*4, 25, "太さ");

ArrayList pre_stroke_list; // 過去引いたストローク
ArrayList avg_stroke_list; // 平均のストローク,赤で表示
Stroke pre_stroke; // 1つ前のストローク
Stroke avg_stroke; // 平均のストローク,赤で表示
Stroke cur_stroke; // 現在追加しようとしているストローク
Stroke und_stroke; // アンドゥした時に消えたストローク,平均の対象か調べる際に必要
Stroke sav_stroke; // セーブする時にundo_strokeの情報を一時保存するストローク
Stroke debug_stroke;

int pre_stroke_max = 70; // pre_strokeの最大容量

int count = 0;
int load_count = 0;

boolean MenuPressed = false;

int saveCount = 0;

public void setup() {
  // processing3用
  //surface.setTitle("Average Painter v5.0");

  // processing2用
  frame.setTitle("Average Painter v9.0");

  // フルスクリーンかどうかでウィンドウサイズを変更
  //processing3用
  /*
  if(skechFullScreen()){
   MenuAreaSize=100;
   CanvasSizeX=displayWidth;
   CanvasSizeY=displayHeight-MenuAreaSize;
   surface.setSize( displayWidth, displayHeight );
   }else{
   MenuAreaSize=100;
   CanvasSizeX=800;
   CanvasSizeY=800;
   surface.setSize( CanvasSizeX, CanvasSizeY + MenuAreaSize );
   }
   */

  if (frame.isUndecorated()) {
    MenuAreaSize=100;
    CanvasSizeX=displayWidth;
    CanvasSizeY=displayHeight-MenuAreaSize;
    //processing2
    size( displayWidth, displayHeight );
    //processing3
    //surface.setSize( displayWidth, displayHeight );
  } else {
    MenuAreaSize=100;
    CanvasSizeX=600;
    CanvasSizeY=600;
    //processing2
    size(  CanvasSizeX, CanvasSizeY + MenuAreaSize );
    //processing3
    //surface.setSize(  CanvasSizeX,  CanvasSizeY + MenuAreaSize );
  }

  font = createFont("Meiriyo", 20, true);
  textFont(font);
  textAlign(CENTER, CENTER);
  Canvas = createGraphics(CanvasSizeX, CanvasSizeY);
  AvgCanvas = createGraphics(CanvasSizeX, CanvasSizeY);

  background( 255 );
  temp = new PImage [0];
  g_mouseStroke = null;

  //平均化機能ON/OFF用のラジオボタンの設定
  checkButton = new ADCheck(20+60*6, 30, checkButtonText, "checkButton");
  checkButton.setDebugOn();
  checkButton.setValue(0);
  checkButton.setboxSide(10);
  checkButton.setBoxLineWidth(1);
  checkButton.setBoxLineColor(0xffA5A5A5);
  checkButton.setValue(0);
  checkButton.setValue(1);

  strWPanel = new StrokeWeightPanel(5+60*4+25, 50, 50, 1, 8);

  new_icon = loadImage("src/icon/create2.png");
  lod_icon = loadImage("src/icon/load2.png");
  und_icon = loadImage("src/icon/undo2.png");
  sav_icon = loadImage("src/icon/save2.png");
  stw_icon = loadImage("src/icon/strokeWeight2.png");

  // showGUIは重いのでとりあえずボタンは最初以外は描画しない
  // 必要に応じて再描画する
  background(255);
  showButtons();

  // ArrayListの初期化
  pre_stroke_list = new ArrayList();
  avg_stroke_list = new ArrayList();

  // フルスクリーンかどうかの判定
  if (keyPressed==true&&keyCode==SHIFT) {
    println("shift pushed");
    //size(displayWidth, displayHeight);
  }
}

public void draw() {
  noStroke();
  fill(255, 255, 255);
  rect( 0, MenuAreaSize, CanvasSizeX, CanvasSizeY + MenuAreaSize );

  showGUI();

  if ( mousePressed && MenuPressed ) {
    // メニューエリアをクリックしてる時だけフレームごとにshowButtonsする
    showButtons();
  }

  strokeW = (int)strWPanel.size;

  if (avg_stroke != null) {
    load_count = count;
  }

  if (strokeType.equals("pen")) {

    if ( mousePressed ) {
      if ( g_bStroking == true ) {

        // 点は描画されないようにする
        strokeDist+=dist(beforeX, beforeY, mouseX, mouseY);
        if (strokeDist>1) {
          pointBisible=1;
        }

        // g_bStrokeStarting:ストロークの書き始めの関数
        if (strokeDist>1) {
          if ( g_bStrokeStarting ) {
            canvasTmpSave();
            g_bStrokeStarting = false;
          }

          Canvas.beginDraw();
          Canvas.translate(0, -MenuAreaSize);
          g_mouseStroke = (PointF[])append( g_mouseStroke, new PointF( mouseX, mouseY-MenuAreaSize ) );
          Canvas.strokeWeight( 2 );
          Canvas.stroke( strokeC );
          Canvas.strokeWeight( strokeW );
          Canvas.line( mouseX, mouseY, pmouseX, pmouseY );
          Canvas.endDraw();
        }
      }
    } else {
      // 2点以上とれている場合にストロークとして処理して登録する
      if (strokeDist>1&&g_mouseStroke !=null && g_mouseStroke.length > 1 && g_bStroking) {
        println("g_mouseStroke.length : "+g_mouseStroke.length);
        Canvas.beginDraw();

        paint_count++;
        /*
        if (pushedUndo==false) {
         saveCount++;
         //save(saveCount+".png");
         //Canvas.save("aaa.png");
         }
         */
        Canvas.translate(0, -MenuAreaSize);
        Canvas.endDraw();
        cur_stroke = new Stroke( g_mouseStroke, strokeC, strokeW );
        cur_stroke.colorFirstNum = selectColor;
        //cur_stroke.displayStroke();

        // 平均化するかどうかの判定
        if (und_stroke!=null) {
          //undoした線と平均化
          averageType=1;
          addStroke(cur_stroke, g_iMultiple);
          judgeStroke(cur_stroke, und_stroke, g_iMultiple, checkButton.getValue(0));
          //addStroke(cur_stroke, und_stroke, g_iMultiple);
        } else {
          //既に描画されている線と平均化
          averageType=2;
          addStroke(cur_stroke, g_iMultiple);
          if (count>1) {
            //pre_stroke=(Stroke)pre_stroke_list.get(count-2);
            for (listNum=0; listNum<count-1; listNum++) {

              judgeStroke(cur_stroke, ((Stroke)pre_stroke_list.get(listNum)), g_iMultiple, checkButton.getValue(1));
              println("No"+listNum+": "+((Stroke)pre_stroke_list.get(listNum)).doSplineCount);
            }
          }
          println("Drawed. Now count is "+count);
          println("list size is "+pre_stroke_list.size());
        }


        // undoの薄線を表示しない場合
        if (pushedUndo==true&&avg_stroke==null) {
          Canvas.beginDraw();
          Canvas.background(255);
          for (int i=0; i<pre_stroke_list.size (); i++) {
            //pre_stroke=(Stroke)pre_stroke_list.get(i);
            ((Stroke)pre_stroke_list.get(i)).displayStroke();
          }
        }
        //cur_stroke=null;
        pushedUndo=false;
      }
      g_bStroking = false;
    }
  }
}

public void showButtons() {
  // メニューバー描画
  noStroke();
  fill( 0xff88D0FF );
  rect( 0, 0, CanvasSizeX, MenuAreaSize );

  checkButton.update();

  strWPanel.update();
  strWPanel.display();

  noFill();
  stroke(0xffA5A5A5);
  rect(5+60*4, 25, 50, 50, 10);
  rect(5+60*5, 25, 60, 50, 10);

  // 色選択部分
  colorMode(HSB, 12, 100, 100);
  for (int x=0; x<4; x++) {
    for (int y=0; y<3; y++) {

      fill(x+y*4, 100, 100);

      if (x==2 && y ==2) {
        fill(0, 0, 100);
      }
      if (x==3 && y ==2) {
        fill(0);
      }

      if (mouseX>=10+60*5+13*x && mouseX<=10+60*5+10+13*x && mouseY>=30+15*y && mouseY<=40+15*y) {
        //色を指定した場合
        if (mousePressed) {
          selectColor = (x+1)+y*4;
          strWPanel.setColor(color(x+y*4, 100, 100));
          strokeC = color(x+y*4, 100, 100);
          strokeType ="pen";

          // 白と黒のカラーパレット用意してあげる
          if (x==2 && y==2) {
            selectColor = 10;
            strWPanel.setColor(color(0, 0, 100));
            strokeC = color(0, 0, 100);
          } else if (x==3 && y==2) {
            selectColor = 11;
            strWPanel.setColor(color(0, 0, 0));
            strokeC = color(0, 0, 0);
          }
        }
      }
      rect(10+60*5+13*x, 30+15*y, 10, 10);
    }
  }

  colorMode(RGB, 255, 255, 255);

  image(new_icon, 5, 25);
  image(sav_icon, 5+60*1, 25);
  image(lod_icon, 5+60*2, 25);
  image(und_icon, 5+60*3, 25);
  image(stw_icon, 5+60*4, 25);
  
}



public void showGUI() {

  // キャンバス描画
  pushMatrix();
  translate(0, MenuAreaSize);
  Canvas.beginDraw();
  Canvas.endDraw();
  image(Canvas, 0, 0);
  popMatrix();

  pushMatrix();
  translate(0, MenuAreaSize);
  AvgCanvas.beginDraw();
  AvgCanvas.endDraw();
  image(AvgCanvas, 0, 0);
  popMatrix();

  // グリッド描画
  stroke( 200 );
  strokeWeight(1);
  line(CanvasSizeX/2, MenuAreaSize, CanvasSizeX/2, CanvasSizeY+MenuAreaSize);
  line(0, MenuAreaSize+CanvasSizeY/2, CanvasSizeX, MenuAreaSize+CanvasSizeY/2);
}

public void openFile() {
  selectInput("Select a file to process:", "fileSelected");
}

public void fileSelected(File selection) {
  if (selection == null) {
    println("Window was closed or the user hit cancel.");
  } else {
    println("User selected " + selection.getAbsolutePath());

    // 拡張子をチェック txtだったらストローク読み込み, pngだったら画像読み込み
    String fname[] = split(selection.getAbsolutePath(), '.');
    String extension = fname[1];

    if (extension.equals("png")) {
      PImage img = loadImage(selection.getAbsolutePath());
      Canvas.image(img, 0, 0, CanvasSizeX, CanvasSizeY);
      println("画像読み込み");
    }
    if (extension.equals("txt")) {
      println("ストローク読み込み");
      String [] lines = loadStrings(selection.getAbsolutePath());

      pre_stroke_list = new ArrayList();
      for (int i=1; i<lines.length-1; i++) {
        String line=lines[i].substring(2, lines[i].length()-3);
        println(i+"画目:"+line);
        println(" ");
        String list[]=split(line, "},{");
        for (int j=0; j<list.length-1; j++) {
          println(list[j]);
          String position[]=split(list[j], ",");
          println("X:"+position[0]);
          println("Y:"+position[1]);
          float x=PApplet.parseFloat(position[0])*1.5f;
          float y=PApplet.parseFloat(position[1])*1.5f;
          if (j==0) {
            g_mouseStroke = new PointF [1];
            g_mouseStroke[0] = new PointF( x-80, y-50 );
          } else {
            g_mouseStroke = (PointF[])append( g_mouseStroke, new PointF( x-80, y-50 ) );
          }
        }
        Stroke st = new Stroke( g_mouseStroke, strokeC, strokeW );
        // strokeを追加
        addStroke( st, g_iMultiple );
      }
      background(255);
      showButtons();
      for ( int s=0; s<pre_stroke_list.size (); s++) {
        Stroke _st = (Stroke)pre_stroke_list.get(s);
        _st.displayStrokeByFourier(g_iMultiple);
      }
      pre_stroke_list = new ArrayList();
    }
  }
}

public void saveStrokes() {
  String strFileName = year() + "";
  if ( month() < 10 ) strFileName += "0" + month();
  else strFileName += month();
  if ( day() < 10 ) strFileName += "0" + day();
  else strFileName += day();
  strFileName += "_";
  if ( hour() < 10 ) strFileName += "0" + hour();
  else strFileName += hour();
  if ( minute() < 10 ) strFileName += "0" + minute();
  else strFileName += minute();
  if ( second() < 10 ) strFileName += "0" + second();
  else strFileName += second();

  // und_strokeの線は消えた状態で保存
  if (und_stroke!=null) {
    //stroke sav_stroke = new Stroke(und_stroke);
    // strokeを追加
    Canvas.beginDraw();
    Canvas.background(255);
    PImage tmp;
    if (count-1<1) {
      tmp = loadImage("tmp0.png");
    } else {
      tmp = loadImage("tmp"+(count-1)+".png");
    }

    Canvas.image(tmp, 0, 0, Canvas.width, Canvas.height);
    if (count-1>0) {
      pre_stroke=(Stroke)pre_stroke_list.get(count);
      pre_stroke.displayStroke();
    }
    Canvas.endDraw();
  }

  //g_curCharStroke.SaveStrokes( strFileName );
  Canvas.save( "dst/save/" + strFileName + ".png" );
  if (und_stroke!=null) {
    und_stroke.displayStroke();
  }
}

public void mouseReleased() {
  MenuPressed = false;
  //cur_stroke.colorFirstNum=selectColor-1;

  if (ave_selected) {
    saveCount-=1;
    save(saveCount+".png");
    ave_selected=false;
  }

  if (normalPen_drawing) {

    Canvas.endDraw();
    normalPen_drawing=false;
  }
}

public void mousePressed() {
  avg_strokePressed = false;

  println("mouseX:"+mouseX+", mouseY:"+mouseY);
  if (avg_stroke !=null ) {
    //if ( avg_stroke != null && avg_stroke.isInside(mouseX, mouseY-MenuAreaSize) ) {
    for (int i=0; i<avg_stroke_list.size (); i++) {
      if (((Stroke)avg_stroke_list.get(i)).isInside(mouseX, mouseY-MenuAreaSize)) {
        // 平均ストロークを確定させる
        println("average stroke clicked!!");
        Canvas.beginDraw();
        Canvas.background(255);

        println("average selected");

        if (count - avg_time == 1) { 
          avg_count++;
        } else {
          avg_count = 0;
        }

        // 変更を踏まえてキャンバスに書き直す
        PImage tmp=loadImage("tmp0.png");
        //undo後の平均
        if (averageType==1) {
          Canvas.image(tmp, 0, 0, Canvas.width, Canvas.height);
          pre_stroke_list.remove(count-1);
          for (int j=0; j<pre_stroke_list.size (); j++) {
            pre_stroke=(Stroke)pre_stroke_list.get(j);
            ((Stroke)pre_stroke_list.get(j)).displayStroke();
          }
          //近しいストロークとの平均
        } else if (averageType==2) {
          count--;
          println("average find. now count is "+count);
          pre_stroke_list.remove(count);
          pre_stroke_list.remove(((Stroke)avg_stroke_list.get(i)).avgListNum);
          println("second remove number is "+((Stroke)avg_stroke_list.get(i)).avgListNum);
          tmp = loadImage("tmp0.png");
          Canvas.image(tmp, 0, 0, Canvas.width, Canvas.height);
          if (pre_stroke_list.size()>0) {
            for (int j=0; j<pre_stroke_list.size (); j++) {
              ((Stroke)pre_stroke_list.get(j)).displayStroke();
            }
          }
        }

        Canvas.endDraw();


        ((Stroke)avg_stroke_list.get(i)).setColor(strokeC);

        ((Stroke)avg_stroke_list.get(i)).displayStroke();

        pre_stroke_list.add(new Stroke(((Stroke)avg_stroke_list.get(i))));
        println("selected! now count is "+count);
        //pre_stroke = new Stroke(avg_stroke2);

        avg_stroke_list = new ArrayList();
        avg_stroke = null;
        avg_time = count;
        averageType = 0;
        und_stroke = null;
        ave_selected=true;
        avg_strokePressed =true;

        clickedAvgStroke=true;
      }
    }
  }
  if ( avg_stroke != null && clickedAvgStroke==false) {
    //平均ストロークがクリックされなかった時
    println("average stroke not clicked…");
    Canvas.beginDraw();
    Canvas.background(255);
    PImage tmp=loadImage("tmp0.png");
    Canvas.image(tmp, 0, 0, Canvas.width, Canvas.height);
    Canvas.endDraw();

    if (pre_stroke_list.size()>0) {
      for (int i=0; i<pre_stroke_list.size (); i++) {
        ((Stroke)pre_stroke_list.get(i)).displayStroke();
      }
    }

    cur_stroke.setColor(strokeC);
    cur_stroke.displayStroke();

    avg_stroke_list = new ArrayList();
    pushedUndo = false;
    avg_stroke = null;
    und_stroke = null;
  }

  clickedAvgStroke=false;

  //描き始めの処理
  if (mouseY > MenuAreaSize && !avg_strokePressed) {
    g_mouseStroke = new PointF [1];
    g_mouseStroke[0] = new PointF( mouseX, mouseY-MenuAreaSize );
    println("down: mousePt[0]"+mouseX+";"+mouseY);
    //strokeWeight( 2 );
    //stroke( 255, 0, 0 );
    //point( mouseX, mouseY );
    strokeDist=0;
    beforeX=mouseX;
    beforeY=mouseY;
    pointBisible=0;

    g_bStroking = true;
    g_bStrokeStarting = true;
  } else if (btn_new.IsInside(mouseX, mouseY)) {
    println("push 新規作成ボタン");
    Canvas.beginDraw();
    Canvas.background(255);
    Canvas.endDraw();

    pre_stroke_list = new ArrayList();
    avg_stroke_list = new ArrayList();
    pre_stroke = null;
    cur_stroke = null;
    avg_stroke = null;
    und_stroke = null;

    count = 0;

    avg_time = 0;
  } else if (btn_sav.IsInside(mouseX, mouseY)) {
    println("push セーブボタン");
    paint_count=0;
    saveStrokes();
  } else if (btn_lod.IsInside(mouseX, mouseY)) {
    println("push 読み込みボタン");
    paint_count=0;
    openFile();
  } else if (btn_und.IsInside(mouseX, mouseY)) {
    println("push 戻るボタン");
    paint_count=0;
    undo();
  } 
  
  else if (mouseY < MenuAreaSize ) {
    MenuPressed = true;
  }

  showButtons();
}


// ペンタブの時に使用
public void keyPressed() {
  if (key == 'A' || key == 'a') {  // Aキーに反応
    println("push A");
    saveCount++;
    save(saveCount+".png");
  } else if (key == 'B' || key == 'b') {  // Bキーに反応
    println("push B");

    //radioButton.setValue(1);
  }
}

//戻るを押したとき
public void undo() {
  if (count>0) {
    count-=1;
    avg_stroke = null;
    und_stroke = (Stroke)pre_stroke_list.get(count);
    pre_stroke_list.remove(count);
    println("undo is "+count);

    println("now count is "+count);
    Canvas.beginDraw();
    Canvas.background(255);
    PImage tmp = loadImage("tmp0.png");
    Canvas.image(tmp, 0, 0, Canvas.width, Canvas.height);

    // pre_stroke_list内のストロークを全部描画する
    for (int i=0; i<pre_stroke_list.size (); i++) {
      pre_stroke=(Stroke)pre_stroke_list.get(i);
      ((Stroke)pre_stroke_list.get(i)).displayStroke();
    }

    // undoのストロークを薄い色で表示
    if (checkButton.getValue(0)) {
      colorMode(HSB, 12, 100, 100);
      if (und_stroke.colorFirstNum==11) {
        und_stroke.col=color(0, 0, 78);
      } else {
        und_stroke.col=color(und_stroke.colorFirstNum-1, 20, 100);
      }
      und_stroke.displayStroke();
      colorMode(RGB, 255, 255, 255);
    }
    Canvas.endDraw();
    pushedUndo=true;
  }
}

class Button {
  int m_iX;
  int m_iY;
  int m_iWidth;
  int m_iHeight;
  String m_strName;
  boolean m_bIsPressed;
  int m_iRed = 0;
  int m_iBlue = 255;
  int m_iGreen = 0;
  int m_iCommand = 0;

  Button( int _x, int _y, int _w, int _h, String _name, int _iCommand ) {
    m_iX = _x;
    m_iY = _y;
    m_iWidth = _w;
    m_iHeight = _h;
    m_strName = _name;
    m_iCommand = _iCommand;
  }
  
  Button( int _x, int _y, String _name ){
    m_iX = _x;
    m_iY = _y;
    m_iWidth = 50;
    m_iHeight = 50;
    m_strName = _name;
  }

  public void SetColor( int _r, int _g, int _b ) {
    m_iRed = _r;
    m_iGreen = _g;
    m_iBlue = _b;
  }

  public void Show() {
    fill(m_iRed, m_iGreen, m_iBlue);
    rect( m_iX, m_iY, m_iWidth, m_iHeight );
    fill(255);
    text( m_strName, m_iX+m_iWidth/2, m_iY+m_iHeight/2 -2 );
  }

  public boolean IsInside( int _x, int _y ){
    if( _x >= m_iX && _x <= m_iX + m_iWidth && _y >= m_iY && _y <= m_iY + m_iHeight ){
      return true;
    }
    return false;
  } 
}

/*いじらない*/
class Fourier {
  float [] m_aX;   //xについてFourierSeriesの実部
  float [] m_bX;   //xについてFourierSeriesの虚部
  float [] m_aY;   //yについてFourierSeriesの実部
  float [] m_bY;   //yについてFourierSeriesの虚部

  Fourier() {
    m_aX = null;
    m_bX = null;
    m_aY = null;
    m_bY = null;
  }

  Fourier( int _iDegree ) {
    Init( _iDegree );
  }
  
  Fourier(Fourier _fourier){
    m_aX = _fourier.m_aX;
    m_bX = _fourier.m_bX;
    m_aY = _fourier.m_aY;
    m_bY = _fourier.m_bY;
    
  }

  // 初期化
  public void Init( int _iDegree ) {
    m_aX = new float [_iDegree+1];
    m_aY = new float [_iDegree+1];
    m_bX = new float [_iDegree+1];
    m_bY = new float [_iDegree+1];
    for ( int i=0; i<_iDegree+1; i++ ) {
      m_aX[i] = 0.0f;
      m_aY[i] = 0.0f;
      m_bX[i] = 0.0f;
      m_bY[i] = 0.0f;
    }
  }

  // フーリエ級数展開
  public void ExpansionFourierSeries( PointF [] _arrayPt, int _iMaxDegree ) {
    int k, n;
    int _iNumOfUnit = _arrayPt.length;

    m_aX = new float [_iMaxDegree+1]; // FourierSeriesの実部
    m_bX = new float [_iMaxDegree+1]; // FourierSeriesの虚部
    m_aY = new float [_iMaxDegree+1]; // FourierSeriesの実部
    m_bY = new float [_iMaxDegree+1]; // FourierSeriesの虚部
    println("num of unit", _iNumOfUnit );

    // フーリエ級数展開の主たる部分
    for (k=0; k<=min (_iMaxDegree, _iNumOfUnit/2); k++) {
      // xのk次についてフーリエ級数展開 
      m_aX[k] = 0.0f; // a_xk
      m_bX[k] = 0.0f; // b_xk
      // yのk次についてフーリエ級数展開
      m_aY[k] = 0.0f;
      m_bY[k] = 0.0f;

      // -PI -> PI
      for (n=0; n<_iNumOfUnit; n++) {
        float t = TWO_PI * (float)n / (float)(_iNumOfUnit) - PI;
        m_aX[k] += _arrayPt[n].x * Math.cos( k * t );
        m_bX[k] += _arrayPt[n].x * Math.sin( k * t );

        m_aY[k] += _arrayPt[n].y * Math.cos( k * t );
        m_bY[k] += _arrayPt[n].y * Math.sin( k * t );
      }

      m_aX[k] = m_aX[k] * (2.0f/(_iNumOfUnit));
      m_bX[k] = m_bX[k] * (2.0f/(_iNumOfUnit));
      m_aY[k] = m_aY[k] * (2.0f/(_iNumOfUnit));
      m_bY[k] = m_bY[k] * (2.0f/(_iNumOfUnit));
    }

    // ここで2分の1倍する！
    m_aX[0] /= 2;
    m_aY[0] /= 2;
    m_bX[0] /= 2;
    m_bY[0] /= 2;
  }

  // 係数をまとめて設定する
  public void SetCoefficientValue( float [] _faX, float [] _fbX, float [] _faY, float [] _fbY ) {
    m_aX = _faX;
    m_bX = _fbX;
    m_aY = _faY;
    m_bY = _fbY;
  }

  /*************/
  // 適切な次数を求める（次数を上げ過ぎると拡大した時にウネウネするため）
  public int GetAppropriateDegree( int _iMaxDegree, int _iNumOfPoints, float _fThresholdForCals) {
    PointF [] now = null;
    PointF [] pre = null;
    int _start = 2;
    int iRetDegree = _start;

    // 次数を上げた時の変化を見ることで適切な次数を求める
    for (int l=_start; l<=_iMaxDegree; l++) {
      float sumBetween = 0;
      now = GetFourierSeries( l, _iNumOfPoints, _fThresholdForCals );
      if ( pre != null ) {
        for (int t = 0; t < now.length; t++) {
          sumBetween = sumBetween + dist( now[t].x, now[t].y, pre[t].x, pre[t].y );
        }
        if ( sumBetween / now.length < 1 ) {
          iRetDegree = l;
          break;
        }
        iRetDegree = l;
      }
      pre = now;
      now = null;
    }
    return iRetDegree;
  }

  public PointF [] GetFourierSeries( int _iDegree, int _iNumOfPoints, float _fThresholdForCals ) {
    // フーリエ級数展開を利用して求めた点列を取得する
    PointF [] _retPoints = new PointF [_iNumOfPoints];
    for ( int i=0; i<_iNumOfPoints; i++ ) {
      float x = m_aX[0];
      float y = m_aY[0];
      for ( int k=1; k<=_iDegree; k++ ) {
        float t = TWO_PI * (float)i/_iNumOfPoints;
        if ( abs(m_aX[k]) > _fThresholdForCals ) x += (m_aX[k] * cos( k*t ));
        if ( abs(m_bX[k]) > _fThresholdForCals ) x += (m_bX[k] * sin( k*t ));
        if ( abs(m_aY[k]) > _fThresholdForCals ) y += (m_aY[k] * cos( k*t ));
        if ( abs(m_bY[k]) > _fThresholdForCals ) y += (m_bY[k] * sin( k*t ));
      }

      _retPoints[i] = new PointF( x, y );
    }
    return _retPoints;
  }

  public void ShowEquations( int _iNumOfDegree, float _fThreshold ) {
    // 単に数式を表示する
    println( "f(x,t) = " );
    for ( int i=0; i<=_iNumOfDegree; i++ ) {
      if ( abs(m_aX[i]) > _fThreshold ) 
        print( " + " + m_aX[i] + " * Cos[" + i + "t]" );
      if ( abs(m_bX[i]) > _fThreshold ) 
        print( " + " + m_bX[i] + " * Sin[" + i + "t]" );
      println();
    }
    println();

    println( "f(y,t) = " );
    for ( int i=0; i<=_iNumOfDegree; i++ ) {
      if ( abs(m_aY[i]) > _fThreshold ) 
        print( " + " + m_aY[i] + " * Cos[" + i + "t]" );
      if ( abs(m_bY[i]) > _fThreshold ) 
        print( " + " + m_bY[i] + " * Sin[" + i + "t]" );
      println();
    }
    println();
  }
}

/*いじらない*/
class GraphAnalysis {
  int [][] m_distance; // 距離配列
  int [][] m_distanceRev; // 反転バージョンの距離配列
  float m_shortestDistance;
  IntList m_shortestFirstPath;
  IntList m_shortestTargetPath;
  ArrayList<IntList> m_orderToReorder;

  GraphAnalysis() {
    m_shortestDistance = -1;
    m_shortestFirstPath = new IntList();
    m_shortestTargetPath = new IntList();
    m_orderToReorder = new ArrayList<IntList>();
  }

  public void showResults() {
    println( "shortest path pairs" );
    print( " >>>" );
    for ( int i=0; i<m_shortestFirstPath.size (); i++ ) {
      print( " " + m_shortestFirstPath.get(i) );
    }
    println();
    print( " >>>" );
    for ( int i=0; i<m_shortestTargetPath.size (); i++ ) {
      print( " " + m_shortestTargetPath.get(i) );
    }
    println();
  }

  // 並び替えの必要性
  public boolean isNeedToReorder() {
    for ( int i=0; i<m_shortestFirstPath.size (); i++ ) {
      // 1つでも一致していないものがあったら要並び替え
      if ( m_shortestFirstPath.get(i) != m_shortestTargetPath.get(i) ) return true;
    }
    return false;
  }

  // 再帰で木を展開していくことによって全部のパターンを出力
  // 組合せ爆発するので現在のバージョンでは使っていない
  public void generateEveryPattern( IntList _rest, IntList _ordered ) {
    for ( int i=0; i<_rest.size (); i++ ) {
      IntList next_rest = new IntList();
      IntList next_ordered = new IntList();
      IntList next_orderedRev = new IntList();

      for ( int j=0; j<_rest.size (); j++ ) {
        if ( i != j ) {
          next_rest.append( _rest.get(j) );
        }
      }

      for ( int j=0; j<_ordered.size (); j++ ) {
        next_ordered.append( _ordered.get(j) );
        next_orderedRev.append( _ordered.get(j) );
      }

      next_ordered.append( _rest.get(i) );
      generateEveryPattern( next_rest, next_ordered );

      next_orderedRev.append( -1*_rest.get(i) );
      generateEveryPattern( next_rest, next_orderedRev );
    }

    if ( _rest.size() == 0 ) {
      m_orderToReorder.add( _ordered );
    }
  }

  // 距離のセット（今使ってない）
  public void setDistance( int [][] _distance, int [][] _distanceRev ) {
    m_distance = new int [_distance.length][_distance[0].length];
    m_distanceRev = new int [_distance.length][_distance[0].length];
    for ( int i=0; i<_distance.length; i++ ) {
      for ( int j=0; j<_distance[0].length; j++ ) {
        m_distance[i][j] = _distance[i][j];
        m_distanceRev[i][j] = _distanceRev[i][j];
      }
    }
  }

  // 距離テーブルの表示（デバッグ用）
  public void showTables( float [][] _distance, float [][] _distanceRev ) {
    for ( int i=0; i<_distance.length; i++ ) {
      for ( int j=0; j<_distance[i].length; j++ ) {
        print( " "+floor(_distance[i][j]) );
      }
      for ( int j=0; j<_distance[i].length; j++ ) {
        print( " "+floor(_distanceRev[i][j]) );
      }
      println();
    }
  }

  // グラフ理論に基づく最小経路探索のスタート部分
  // 各行の最小値を発見し，その度にその最小値に該当する行と段に-1をセット
  // すべての行と段が-1になったら終了
  // 局所解に陥ってしまうことがあるので，すべての行について1位を発見し，そこからスタート
  // なお，これでも局所解に陥ることはある
  // あと，最も出現頻度が高いであろう全部の順序があっているものは初期値としてセット
  public void findingShortestPathStart( float [][] _distance, float [][] _distanceRev )
  {
    // 値の初期化
    m_shortestDistance = 0.0f;
    m_shortestFirstPath.clear();
    m_shortestTargetPath.clear();
    // すべての順序があっている場合の値とパスをセット
    for ( int i=0; i<_distance.length; i++ ) {
      m_shortestDistance += _distance[i][i];
      m_shortestFirstPath.append( i+1 );
      m_shortestTargetPath.append( i+1 );
    }

    // 各行の最小値をスタートとして，最短経路探索を走らせる
    for ( int i=0; i<_distance.length; i++ ) {
      int minFirst = i;
      int minTarget = 0;
      boolean minReverse = false;
      float minValue = _distance[i][0];

      println( "=========== START " + (minFirst+1) + " =======================");
      showTables( _distance, _distanceRev );

      for ( int j=0; j<_distance[0].length; j++ ) {
        if ( _distance[i][j] < minValue ) {
          minValue = _distance[i][j];
          minFirst = i;
          minTarget = j;
          minReverse = false;
        }
        if ( _distanceRev[i][j] < minValue ) {
          minValue = _distanceRev[i][j];
          minFirst = i;
          minTarget = j;
          minReverse = true;
        }
      }

      IntList next_firstPath = new IntList();
      IntList next_targetPath = new IntList();

      next_firstPath.append( minFirst+1 );
      if ( minReverse == true ) {
        next_targetPath.append( -1*(minTarget+1) );
      } else {
        next_targetPath.append( minTarget+1 );
      }
      float [][] next_distance = new float [_distance.length][_distance[0].length];
      float [][] next_distanceRev = new float [_distance.length][_distance[0].length];
      for ( int k=0; k<_distance.length; k++ ) {
        for ( int l=0; l<_distance[0].length; l++ ) {
          next_distance[k][l] = _distance[k][l];
          next_distanceRev[k][l] = _distanceRev[k][l];
          if ( k == minFirst || l == minTarget ) {
            next_distance[k][l] = -1;
            next_distanceRev[k][l] = -1;
          }
        }
      }

      println( "start = " + (minFirst+1) + ", " + next_targetPath.get(0) );
      findingShortestPath( next_distance, next_distanceRev, next_firstPath, next_targetPath, minValue );
    }
  }

  public float getDist( int _row, int _col, float [][] _distance, float [][] _distanceRev ) {
    if( _row < 0 ){
      println( "error dist: ", _row, _col );
    }
    if ( _col < 0 ) {
      return _distanceRev[_row-1][-1*_col-1];
    }
    return _distance[_row-1][_col-1];
  }

  public void replaceRowCol( float [][] _distance, float [][] _distanceRev ) {
    for ( int i=0; i<_distance.length; i++ ) {
      for ( int j=0; j<_distance.length; j++ ) {
        if ( i==j ) continue;

        float dist = 0.0f;
        int c1 = m_shortestTargetPath.get(i);
        int c2 = m_shortestTargetPath.get(j);
        int r1 = m_shortestFirstPath.get(i);
        int r2 = m_shortestFirstPath.get(j);

        float orgDist = getDist( r1, c1, _distance, _distanceRev ) + getDist( r2, c2, _distance, _distanceRev );

        if ( orgDist > getDist( r2, c1, _distance, _distanceRev ) + getDist( r1, c2, _distance, _distanceRev ) ) {
          orgDist = getDist( r2, c1, _distance, _distanceRev ) + getDist( r1, c2, _distance, _distanceRev );
          m_shortestFirstPath.set(i, r2);
          m_shortestTargetPath.set(i, c1);
          m_shortestFirstPath.set(j, r1);
          m_shortestTargetPath.set(j, c2);
          println( "replace!", c1, r1, c2, r2 );
        }
        if ( orgDist > getDist( r2, -1*c1, _distance, _distanceRev ) + getDist( r1, c2, _distance, _distanceRev ) ) {
          orgDist = getDist( r2, -1*c1, _distance, _distanceRev ) + getDist( r1, c2, _distance, _distanceRev );
          m_shortestFirstPath.set(i, r2);
          m_shortestTargetPath.set(i, -1*c1);
          m_shortestFirstPath.set(j, r1);
          m_shortestTargetPath.set(j, c2);
          println( "replace!", c1, r1, c2, r2 );
        }
        if ( orgDist > getDist( r2, -1*c1, _distance, _distanceRev ) + getDist( r1, -1*c2, _distance, _distanceRev ) ) {
          orgDist = getDist( r2, -1*c1, _distance, _distanceRev ) + getDist( r1, -1*c2, _distance, _distanceRev );
          m_shortestFirstPath.set(i, r2);
          m_shortestTargetPath.set(i, -1*c1);
          m_shortestFirstPath.set(j, r1);
          m_shortestTargetPath.set(j, -1*c2);
          println( "replace!", c1, r1, c2, r2 );
        }
        if ( orgDist > getDist( r2, c1, _distance, _distanceRev ) + getDist( r1, -1*c2, _distance, _distanceRev ) ) {
          orgDist = getDist( r2, c1, _distance, _distanceRev ) + getDist( r1, -1*c2, _distance, _distanceRev );
          m_shortestFirstPath.set(i, r2);
          m_shortestTargetPath.set(i, c1);
          m_shortestFirstPath.set(j, r1);
          m_shortestTargetPath.set(j, -1*c2);
          println( "replace!", c1, r1, c2, r2 );
        }
      }
    }
  }

  // 最短経路探索のアルゴリズム
  // (1) 行段の中で-1を除く最小値を発見する
  // (2) 最小値に該当する行と段を-1で埋め，最小値を距離に足す（この行と段をパスに入れていく）
  // (3) すべてが-1になるまで(1)に戻る．すべてが-1になると距離を返す
  public void findingShortestPath( float [][] _distance, float [][] _distanceRev, IntList _firstPath, IntList _targetPath, float _totalDistance )
  {
    showTables( _distance, _distanceRev );

    int minFirst = -1;
    int minTarget = -1;
    boolean minReverse = false;
    float minValue = -1;

    for ( int i=0; i<_distance.length; i++ ) {
      for ( int j=0; j<_distance[i].length; j++ ) {
        if ( _distance[i][j] > 0 ) {
          if ( minValue < 0 ) {
            minReverse = false;
            minValue = _distance[i][j];
            minFirst = i;
            minTarget = j;
            //println( "> ", minValue, minFirst+1, minTarget+1);
          } else if ( minValue > _distance[i][j] ) {
            minReverse = false;
            minValue = _distance[i][j];
            minFirst = i;
            minTarget = j;
            //println( "> ", minValue, minFirst+1, minTarget+1);
          }
        }

        if ( _distanceRev[i][j] > 0 ) {
          if ( minValue < 0 ) {
            minReverse = true;
            minValue = _distanceRev[i][j];
            minFirst = i;
            minTarget = j;
            println( "> ", minValue, minFirst+1, -1*(minTarget+1));
          } else if ( minValue > _distanceRev[i][j] ) {
            minReverse = true;
            minValue = _distanceRev[i][j];
            minFirst = i;
            minTarget = j;
            println( "> ", minValue, minFirst+1, -1*(minTarget+1));
          }
        }
      }
    }

    // 全部が-1になるとここが呼び出される
    if ( minValue < 0 ) {
      // デバッグ用の出力
      println( "detected path" );
      print( " >>" );
      for ( int i=0; i<_firstPath.size (); i++ ) {
        print( " " + _firstPath.get(i) );
      }
      println();
      print( " >>" );
      for ( int i=0; i<_targetPath.size (); i++ ) {
        print( " " + _targetPath.get(i) );
      }
      println();
      println( " distance = " +  _totalDistance + ", current shortest distance = " + m_shortestDistance );
      // 距離が最短になったら差し替える
      if ( m_shortestDistance < 0 || m_shortestDistance > _totalDistance ) {
        println( "!!!!SHORTEST!!!!" );
        // 最短距離を差し替える
        m_shortestDistance = _totalDistance;

        // 念のためクリアして最短距離のパスをコピー
        m_shortestFirstPath.clear();
        m_shortestTargetPath.clear();
        m_shortestFirstPath = _firstPath;
        m_shortestTargetPath = _targetPath;
      }

      // 全部-1になったら再帰終了
      return;
    }

    if ( minReverse ) {
      println( "* shortest: ", minValue, minFirst+1, -1*(minTarget) );
    } else {
      println( "* shortest: ", minValue, minFirst+1, minTarget+1 );
    }

    // 行を first に，段を target に追加
    _firstPath.append( minFirst+1 );
    if ( minReverse == true ) {
      _targetPath.append( -1*(minTarget+1) );
    } else {
      _targetPath.append( minTarget+1 );
    }

    // 再帰処理のための距離配列を再セット
    // 利用した行と段の値を-1に全部セットする
    float [][] next_distance = new float [_distance.length][_distance[0].length];
    float [][] next_distanceRev = new float [_distance.length][_distance[0].length];
    for ( int i=0; i<_distance.length; i++ ) {
      for ( int j=0; j<_distance[0].length; j++ ) {
        next_distance[i][j] = _distance[i][j];
        next_distanceRev[i][j] = _distanceRev[i][j];
        if ( i == minFirst || j == minTarget ) {
          next_distance[i][j] = -1;
          next_distanceRev[i][j] = -1;
        }
      }
    }

    // 再帰的に呼び出していく
    findingShortestPath( next_distance, next_distanceRev, _firstPath, _targetPath, _totalDistance+minValue );
  }
}

// 距離計算の関数
public float getDistanceFromFourier( Fourier _first, PointF _firstLeftTop, PointF _firstRightBottom, Fourier _target, PointF _targetLeftTop, PointF _targetRightBottom, boolean _bReverse, boolean _bParallelMove ) {
  float dist = 0.0f;
  println( "------------------ first -----------------" );
  //_first.ShowEquations( 10, (float)g_fThresholdToRemove );
  println( "------------------ second ----------------" );
  //_target.ShowEquations( 10, (float)g_fThresholdToRemove );

  // 「のの　」と「　のの」というパターンで「の」が重なる問題を解決するため
  // 距離計算のための数式を平行移動させて，「のの」と「のの」にし，対応関係が
  // ちゃんといくように設定しておく
  // そのために書く文字・図形の左上と右下の座標を取得し，式を変形する
  float x1 = _firstLeftTop.x;
  float x2 = _firstRightBottom.x;
  float x3 = _targetLeftTop.x;
  float x4 = _targetRightBottom.x;
  float y1 = _firstLeftTop.y;
  float y2 = _firstRightBottom.y;
  float y3 = _targetLeftTop.y;
  float y4 = _targetRightBottom.y;

  // i=0からならa0を使う、1からなら使わない（場所）
  for ( int i=1; i<_first.m_aX.length; i++ ) {
    // 平行移動しない場合の値をセット
    float ax = _target.m_aX[i];
    float bx = _target.m_bX[i];
    float ay = _target.m_aY[i];
    float by = _target.m_bY[i];

    // 平行移動する場合はこちらの値をセット（なんとか補間）
    if ( _bParallelMove ) {
      if ( i==0 ) {
        ax = _target.m_aX[i] - (x4-x3)*x1/(x2-x1) + x3;
        bx = _target.m_bX[i];
        ay = _target.m_aY[i] - (y4-y3)*y1/(y2-y1) + y3;
        by = _target.m_bY[i];
      } else {
        ax = _target.m_aX[i] * (x4-x3) / (x2-x1);
        bx = _target.m_bX[i] * (x4-x3) / (x2-x1);
        ay = _target.m_aY[i] * (y4-y3) / (y2-y1);
        by = _target.m_bY[i] * (y4-y3) / (y2-y1);
      }
    }  

    // 重み付けをどうするかが悩ましい所．absでやるかsqでやるか...
    if ( _bReverse && i > 0 ) {
      if ( i%2 == 1 ) {
        ax = -ax;
        ay = -ay;
      } else {
        bx = -bx;
        by = -by;
      }
    }

    dist += (sq(_first.m_aX[i]-ax)+sq(_first.m_bX[i]-bx)+sq(_first.m_aY[i]-ay)+sq(_first.m_bY[i]-by))*(1.0f/(i+1));
    //dist += (abs(_first.m_aX[i]-ax)+abs(_first.m_bX[i]-bx)+abs(_first.m_aY[i]-ay)+abs(_first.m_bY[i]-by))*(1.0/(i+1));
  }

  // 値が大きいので1/1000する
  return dist/1000.0f;
}

// コサイン類似度で距離計算をしてみるとどうか？
public float getDistanceFromFourierCos( Fourier _first, PointF _firstLeftTop, PointF _firstRightBottom, Fourier _target, PointF _targetLeftTop, PointF _targetRightBottom, boolean _bReverse, boolean _bParallelMove ) {
  float dist = 0.0f;


  // 「のの　」と「　のの」というパターンで「の」が重なる問題を解決するため
  // 距離計算のための数式を平行移動させて，「のの」と「のの」にし，対応関係が
  // ちゃんといくように設定しておく
  // そのために書く文字・図形の左上と右下の座標を取得し，式を変形する
  float x1 = _firstLeftTop.x;
  float x2 = _firstRightBottom.x;
  float x3 = _targetLeftTop.x;
  float x4 = _targetRightBottom.x;
  float y1 = _firstLeftTop.y;
  float y2 = _firstRightBottom.y;
  float y3 = _targetLeftTop.y;
  float y4 = _targetRightBottom.y;

  float targetSumSQ = 0.0f;
  float firstSumSQ = 0.0f;
  float sumInnerProduct = 0.0f;

  for ( int i=0; i<_first.m_aX.length; i++ ) {
    // 平行移動しない場合の値をセット
    float ax = _target.m_aX[i];
    float bx = _target.m_bX[i];
    float ay = _target.m_aY[i];
    float by = _target.m_bY[i];

    // 平行移動する場合はこちらの値をセット（なんとか補間）
    if ( _bParallelMove ) {
      if ( i==0 ) {
        ax = _target.m_aX[i] - (x4-x3)*x1/(x2-x1) + x3;
        bx = _target.m_bX[i];
        ay = _target.m_aY[i] - (y4-y3)*y1/(y2-y1) + y3;
        by = _target.m_bY[i];
      } else {
        ax = _target.m_aX[i] * (x4-x3) / (x2-x1);
        bx = _target.m_bX[i] * (x4-x3) / (x2-x1);
        ay = _target.m_aY[i] * (y4-y3) / (y2-y1);
        by = _target.m_bY[i] * (y4-y3) / (y2-y1);
      }
    }  

    // 重み付けをどうするかが悩ましい所．absでやるかsqでやるか...
    if ( _bReverse ) {
      if ( i%2 == 1 ) {
        ax = -ax; 
        ay = -ay;
      } else {
        bx = -bx; 
        by = -by;
      }
    }

    sumInnerProduct += _first.m_aX[i]*ax + _first.m_bX[i]*bx + _first.m_aY[i]*ay + _first.m_bY[i]*by;
    firstSumSQ += sq(_first.m_aX[i])+sq(_first.m_bX[i])+sq(_first.m_aY[i])+sq(_first.m_bY[i]); 
    targetSumSQ += sq(ax)+sq(ay)+sq(bx)+sq(by);   
    dist += (sq(_first.m_aX[i]-ax)+sq(_first.m_bX[i]+bx)+sq(_first.m_aY[i]-ay)+sq(_first.m_bY[i]+by))*(1.0f/(i+1));
    //  dist += (abs(_first.m_aX[i]-ax)+abs(_first.m_bX[i]-bx)+abs(_first.m_aY[i]-ay)+abs(_first.m_bY[i]-by))*(1.0/(i+1));
  }

  float cosSimilarity = sumInnerProduct / (sqrt(firstSumSQ)*sqrt(targetSumSQ));
  return 1.1f-cosSimilarity;
  // 値が大きいので1/1000する
  //  return dist/1000.0;
}

/*いじらない*/
class Spline {
  Spline() {
    ;
  }

  public PointF [] GetSpline( PointF [] _arrayPt, int _multiple )
  {
    float [] _arrayT = new float [_arrayPt.length];
    for ( int i=0; i<_arrayPt.length; i++ ) {
      _arrayT[i] = (float)i*TWO_PI/(_arrayPt.length-1)-PI;
    }

    PointF [] _points = GetSplineSeries( _arrayT, _arrayPt, _multiple );

    PointF [] _retPoints = new PointF [_points.length*2-1];
    for ( int i=0; i<_points.length; i++ ) {
      _retPoints[i] = _points[i];
      _retPoints[_retPoints.length-i-1]  = _points[i];
    }
    return _retPoints;
  }

  public PointF [] GetInterXYSeries( float [] _t, PointF [] _arrayPt, int _multiple )
  {
    PointF [] _retPoints = new PointF [_arrayPt.length*_multiple];
    _retPoints[0] = new PointF( _arrayPt[0].x, _arrayPt[0].y );
    _retPoints[_arrayPt.length*_multiple-1] = new PointF( _arrayPt[_arrayPt.length-1].x, _arrayPt[_arrayPt.length-1].y );
    for ( int i=1; i<_arrayPt.length*_multiple; i++ ) {
      _retPoints[i] = new PointF(
        i*(_arrayPt[0].x+_arrayPt[_arrayPt.length-1].x)/(_arrayPt.length*_multiple-1), 
        i*(_arrayPt[0].x+_arrayPt[_arrayPt.length-1].x)/(_arrayPt.length*_multiple-1) );
    }
    return _retPoints;
  }

  public PointF [] GetSplineSeries( float [] _t, PointF [] _arrayPt, int _multiple )
  {
    if( _arrayPt.length == 2 ){
      return GetInterXYSeries( _t, _arrayPt, _multiple );
    }
    PointF [] _retPoints;

    float [] _arrayX = new float [_arrayPt.length];
    float [] _arrayY = new float [_arrayPt.length];
    for ( int i=0; i<_arrayPt.length; i++ ) {
      _arrayX[i] = _arrayPt[i].x;
      _arrayY[i] = _arrayPt[i].y;
    }

    // multi倍の点を取る
    float [] _interX = GetSplineValues( _t, _arrayX, _multiple );
    float [] _interY = GetSplineValues( _t, _arrayY, _multiple );

    // Remove duplicate points
    int number = 1;
    int skipFrom = 1;
    for ( int i=1; i<_interX.length; i++ ) {
      if ( dist( (float)_interX[i], (float)_interY[i], (float)_interX[skipFrom], (float)_interY[skipFrom])<g_fThresholdToRemove ) {
      } else if ( _interX[i] == -1 && _interY[i] == -1 ) {
      } else {
        skipFrom = i;
        number++;
      }
    }

    _retPoints = new PointF [number];  
    _retPoints[0] = new PointF( _interX[0], _interY[0] );

    number = 1;
    skipFrom = 1;
    for ( int i=1; i<_interX.length; i++ ) {
      if ( dist( (float)_interX[i], (float)_interY[i], (float)_interX[skipFrom], (float)_interY[skipFrom])<g_fThresholdToRemove) {
      } else if ( _interX[i] == -1 && _interY[i] == -1 ) {
      } else {
        skipFrom = i;
        _retPoints[number] = new PointF( _interX[i], _interY[i] );
        number++;
      }
    }

    println( "original array size = " + _interX.length );
    println( "         array size = " + _retPoints.length );
    return _retPoints;
  }

  public float [] GetSplineValues(float [] _t, float [] _value, int _multiple) {
    float [] retValue = new float [(_value.length-1) * _multiple+1];

    int n = _t.length -1;
    float h[] = new float [ n ];
    float b[] = new float [ n ];
    float d[] = new float [ n ];
    float g[] = new float [ n ];
    float u[] = new float [ n ];
    float r[] = new float [n+1];
    float q[] = new float [ n ];
    float s[] = new float [ n ];

    int i1 = 0;

    for (i1 = 0; i1 < n; i1++) {
      h[i1] = _t[i1+1] - _t[i1];
    }
    for (i1 = 1; i1 < n; i1++) {
      b[i1] = (float) (2.0f * (h[i1] + h[i1-1]));
      d[i1] = (float) (3.0f * ((_value[i1+1] - _value[i1]) / h[i1] - (_value[i1] - _value[i1-1]) / h[i1-1]));
    }
    g[1] = h[1] / b[1];
    for (i1 = 2; i1 < n-1; i1++) {
      g[i1] = h[i1] / (b[i1] - h[i1-1] * g[i1-1]);
    }
    u[1] = d[1] / b[1];
    for (i1 = 2; i1 < n; i1++) {
      u[i1] = (d[i1] - h[i1-1] * u[i1-1]) / (b[i1] - h[i1-1] * g[i1-1]);
    }

    r[0]    = 0.0f;
    r[n]    = 0.0f;
    r[n-1]  = u[n-1];
    for (i1 = n-2; i1 >= 1; i1--) {
      r[i1] = u[i1] - g[i1] * r[i1+1];
    }

    int num = 0;
    for (int i = 0; i < _value.length-1; i++) {
      float between = _t[i+1]-_t[i];
      float splineT = between/_multiple;
      for (float j = 0; j < _multiple; j++ ) {
        float sp = j * splineT;
        float qi = (float) ((_value[i+1] - _value[i]) / h[i] - h[i] * (r[i+1] + 2.0f * r[i]) / 3.0f);
        float si = (float) ((r[i+1] - r[i]) / (3.0f * h[i]));
        float y1 = _value[i] + sp * (qi + sp * (r[i]  + si * sp));
        retValue[num] = y1;
        num++;
      }
    }
    retValue[retValue.length-1] = _value[_value.length-1];

    return retValue;
  }
}

// by nino

class StrokeWeightPanel {
  float posX,posY;
  int max,min;
  float size;
  boolean over;
  boolean locked;
  int col = color(0,0,0);

  StrokeWeightPanel (float _px, float _py, int _mx, int _mi, float _s) {
    posX = _px;
    posY = _py;
    max = _mx;
    min = _mi;
    size = _s;
  }

  public void update() {
    
    if (overEvent()) {
      over = true;
    } else {
      over = false;
    }
    locked =false;
    if (mousePressed && over) {
      locked = true;
    }
    
    
    if (locked) {
      if ( dragged2right() ){
        size+=3;
      } else if ( dragged2left() ){
        size-=3;
      }
    }
    
    size = constrain(size, min, max);
  }


  public void setColor(int _col){
    col = _col;
  }
  
  public boolean overEvent() {
    if (dist(mouseX,mouseY,posX,posY) < max/2) {
      return true;
    } else {
      return false;
    }
  }
  
  public boolean dragged2right(){
    if(mouseX > pmouseX){
      return true;
    } else {
      return false;
    }
  }
  
  public boolean dragged2left(){
    if(mouseX < pmouseX){
      return true;
    } else {
      return false;
    }
  }
  
  
  public void display() {
    noStroke();
    fill(col);
    ellipse(posX, posY, size, size);
  }

}

/*描写した線の情報*/
class Stroke {
  PointF [] m_orgPt;
  PointF [] m_SplinePt;
  PointF [] m_FourierSeriesPt;
  Fourier m_Fourier;
  boolean m_bFourier;
  int m_iAppropriateDegreeOfFourier;
  int col;
  int weight;
  String type;
  int colorFirstNum=11;
  boolean isAverageStroke=false;
  int doSplineCount;
  
  int avgListNum;

  Stroke( PointF [] _orgPt, int _col, int _weight, String _type)
  {
    m_orgPt = _orgPt;
    m_SplinePt = _orgPt;
    m_Fourier = new Fourier( );
    m_bFourier = false;
    col = _col;
    weight = _weight;
    type = _type;
    isAverageStroke=false;
  }

  Stroke(Stroke _st) {
    m_orgPt = _st.m_orgPt;
    m_SplinePt = _st.m_SplinePt;
    m_Fourier = new Fourier(_st.m_Fourier);
    m_FourierSeriesPt = _st.m_FourierSeriesPt;
    m_bFourier = _st.m_bFourier;
    col = _st.col;
    weight = _st.weight;
    type = _st.type;
    colorFirstNum = _st.colorFirstNum;
    isAverageStroke=_st.isAverageStroke;
  }

  Stroke( PointF [] _orgPt, int _col, int _weight) {
    m_orgPt = _orgPt;
    m_SplinePt = _orgPt;
    m_Fourier = new Fourier( );
    m_bFourier = false;
    col = _col;
    weight = _weight;
    type = "pen";
    isAverageStroke=false;
  }
  
  Stroke( Stroke _st, int _col, int _weight) {
    m_orgPt = _st.m_orgPt;
    m_SplinePt = _st.m_SplinePt;
    m_Fourier = new Fourier(_st.m_Fourier);
    m_FourierSeriesPt = _st.m_FourierSeriesPt;
    m_bFourier = _st.m_bFourier;
    col = _col;
    weight = _weight;
    type = _st.type;
    colorFirstNum = _st.colorFirstNum;
    isAverageStroke=true;
  }

  Stroke( int _iSize ) {
    m_orgPt = new PointF [_iSize];
    m_SplinePt = new PointF [_iSize];
    m_Fourier = new Fourier( min(_iSize/2, g_iMaxDegreeOfFourier) );
  }

  public PointF getGravityCenter() {
    PointF retPt = new PointF();
    retPt.x = 0.0f;
    retPt.y = 0.0f;
    for ( int i=0; i<m_orgPt.length; i++ ) {
      retPt.x += m_orgPt[i].x;
      retPt.y += m_orgPt[i].y;
    }
    retPt.x /= m_orgPt.length;
    retPt.y /= m_orgPt.length;
    return retPt;
  }

  public void setColor(int _col) {
    col = _col;
  }

  public void setType(String _type) {
    type = _type;
  }

  public void doReverse() {
    PointF [] tempPt = new PointF [m_orgPt.length];
    for ( int i=0; i<m_orgPt.length; i++ ) {
      tempPt[m_orgPt.length-i-1] = m_orgPt[i];
    }
    for ( int i=0; i<m_orgPt.length; i++ ) {
      m_orgPt[i] = tempPt[i];
    }
  }

  public void doSpline( int _iMultiple )
  {
    doSplineCount++;
    // 0 ～ PI で t を作成する
    float [] _arrayT = new float [m_orgPt.length];
    for ( int j=0; j<m_orgPt.length; j++ ) {
      _arrayT[j] = (float)j*PI/(m_orgPt.length-1);
    }

    Spline sp = new Spline();
    m_SplinePt = sp.GetSplineSeries( _arrayT, m_orgPt, _iMultiple );
  }


  public void doFourier() {
    if ( m_bFourier == true ) return;

    // ストロークを折り返して2倍にする
    // フーリエ級数展開では始点終点が同じであることが理想であるため
    for ( int i=0; i<m_SplinePt.length/2+1; i++ ) {
      float temp = m_SplinePt[i].x; 
      m_SplinePt[i].x = m_SplinePt[m_SplinePt.length-i-1].x;
      m_SplinePt[m_SplinePt.length-i-1].x = temp;  
      temp = m_SplinePt[i].y; 
      m_SplinePt[i].y = m_SplinePt[m_SplinePt.length-i-1].y;
      m_SplinePt[m_SplinePt.length-i-1].y = temp;
    } 
    m_SplinePt = DoubleBack( m_SplinePt );
    m_Fourier.ExpansionFourierSeries( m_SplinePt, g_iMaxDegreeOfFourier );
    int iDegree = m_Fourier.GetAppropriateDegree( g_iMaxDegreeOfFourier, m_SplinePt.length, g_fThresholdOfCoefficient );
    m_FourierSeriesPt = m_Fourier.GetFourierSeries( iDegree, m_SplinePt.length/2, g_fThresholdOfCoefficient );
    m_bFourier = true;
  }

  public void display( int _iShowMode, float _fZoom ) {
    strokeWeight(2);

    //次数の導出とか
    int iDegree = m_Fourier.GetAppropriateDegree( g_iMaxDegreeOfFourier, m_SplinePt.length, g_fThresholdOfCoefficient );
    m_FourierSeriesPt = m_Fourier.GetFourierSeries( iDegree, m_SplinePt.length, g_fThresholdOfCoefficient );
    stroke( 0, 0, 255 );
    Canvas.beginDraw();
    for ( int i=0; i<m_FourierSeriesPt.length-1; i++ ) {
      Canvas.line( m_FourierSeriesPt[i].x*_fZoom, m_FourierSeriesPt[i].y*_fZoom, m_FourierSeriesPt[i+1].x*_fZoom, m_FourierSeriesPt[i+1].y*_fZoom );
    }
    Canvas.endDraw();
    //m_Fourier.ShowEquations(10,g_fThresholdOfCoefficient);
  }

  public void doAverageByStroke( Stroke _addStroke ) {

    for ( int k=0; k<=g_iMaxDegreeOfFourier; k++ ) {
      m_Fourier.m_aX[k] = (_addStroke.m_Fourier.m_aX[k] + m_Fourier.m_aX[k] ) / 2; 
      m_Fourier.m_aY[k] = (_addStroke.m_Fourier.m_aY[k] + m_Fourier.m_aY[k] ) / 2; 
      m_Fourier.m_bX[k] = (_addStroke.m_Fourier.m_bX[k] + m_Fourier.m_bX[k] ) / 2; 
      m_Fourier.m_bY[k] = (_addStroke.m_Fourier.m_bY[k] + m_Fourier.m_bY[k] ) / 2;
    }

    m_iAppropriateDegreeOfFourier = m_Fourier.GetAppropriateDegree( g_iMaxDegreeOfFourier, m_SplinePt.length, g_fThresholdOfCoefficient );
    println( "appropriate degree", m_iAppropriateDegreeOfFourier );
    m_FourierSeriesPt = m_Fourier.GetFourierSeries( m_iAppropriateDegreeOfFourier, m_SplinePt.length/2, g_fThresholdOfCoefficient );
  }

  public void displayStroke() {
    
    if(isAverageStroke){
      m_orgPt = m_FourierSeriesPt;
    }
    
    strokeWeight(2);
    stroke( 255, 0, 0 );
    Canvas.beginDraw();
    Canvas.smooth();
    Canvas.strokeWeight(weight);
    Canvas.stroke(col);
    for ( int i=0; i<m_orgPt.length-1; i++ ) {
      //   Canvas.stroke( 0, 0, 200 );
      //  Canvas.strokeWeight(2);
      Canvas.line( m_orgPt[i].x, m_orgPt[i].y, m_orgPt[i+1].x, m_orgPt[i+1].y );
    }
    Canvas.endDraw();
    
    if(isAverageStroke){
      m_orgPt = m_SplinePt;
    }
    
  }

  public void displayStrokeByFourier( int _iMultiple ) {
    if ( m_bFourier == false ) {
      //doSpline( _iMultiple );
      //doFourier();
    }

    smooth();
    stroke( 0, 0, 200 );
    strokeWeight(2);
    float lx = 0;
    float ly = 0;

    Canvas.beginDraw();
    Canvas.smooth();
    //   Canvas.stroke( 0, 0, 200 );
    //   Canvas.strokeWeight(2);
    Canvas.strokeWeight(weight);
    Canvas.stroke(col);
    for (int num = 0; num < m_FourierSeriesPt.length/2; num++) {
      Canvas.line( PApplet.parseInt(m_FourierSeriesPt[num].x+0.5f), PApplet.parseInt(m_FourierSeriesPt[num].y+0.5f), PApplet.parseInt(m_FourierSeriesPt[num+1].x+0.5f), PApplet.parseInt(m_FourierSeriesPt[num+1].y+0.5f) );
    }
    Canvas.endDraw();
  }
  
  public void displayAverage() {
    m_iAppropriateDegreeOfFourier = m_Fourier.GetAppropriateDegree( g_iMaxDegreeOfFourier, m_SplinePt.length, g_fThresholdOfCoefficient );
    println( "appropriate degree", m_iAppropriateDegreeOfFourier );
    m_FourierSeriesPt = m_Fourier.GetFourierSeries( m_iAppropriateDegreeOfFourier, m_SplinePt.length/2, g_fThresholdOfCoefficient );

    smooth();
    stroke( 0, 0, 200 );
    strokeWeight(2);
    float lx = 0;
    float ly = 0;
    Canvas.beginDraw();
    Canvas.smooth();
    // Canvas.stroke( 0, 0, 200 );
    // Canvas.strokeWeight(2);
    for (int num = 0; num < m_FourierSeriesPt.length/2; num++) {
      Canvas.line( PApplet.parseInt(m_FourierSeriesPt[num].x+0.5f), PApplet.parseInt(m_FourierSeriesPt[num].y+0.5f), PApplet.parseInt(m_FourierSeriesPt[num+1].x+0.5f), PApplet.parseInt(m_FourierSeriesPt[num+1].y+0.5f) );
    }
    Canvas.endDraw();
    
    //m_Fourier.ShowEquations(10,g_fThresholdOfCoefficient);
  }

  public boolean isInside(int _x, int _y) {
    boolean judge = false;
    for (int num = 0; num < m_FourierSeriesPt.length/2; num++) {
      if (dist(_x, _y, m_FourierSeriesPt[num].x, m_FourierSeriesPt[num].y)<=strokeW+15) {
        judge |= true;
      }
    }

    return judge;
  }

  public boolean isEqual(Stroke _st) {

    boolean flag=true;
    for ( int i=0; i<m_orgPt.length; i++ ) {
      if ( m_orgPt[i].x == _st.m_orgPt[i].x && m_orgPt[i].y == _st.m_orgPt[i].y) {
        flag &= true;
      }
    }
    return flag;
  }
}


class PointF {
  float x;
  float y;
  PointF() { 
    x = 0.0f; 
    y = 0.0f;
  }
  PointF( float _x, float _y ) { 
    x = _x; 
    y = _y;
  }
}

public PointF [] DoubleBack( PointF [] _points ) {
  PointF [] _retPoints = new PointF [_points.length*2-1];
  for ( int i=0; i<_points.length; i++ ) {
    _retPoints[i] = new PointF( _points[i].x, _points[i].y );
    _retPoints[_retPoints.length-i-1] = new PointF( _points[i].x, _points[i].y );
  }
  return _retPoints;
}



public void setFill(int _x, int _y, int _c) {

  int c = get(_x, _y);
  if (c != color(0) ) {
    fill(100, 0, 0);
    point(_x, _y);

    if (c == _c) {
      setFill( _x-1, _y, _c);
      setFill( _x+1, _y, _c);
      setFill( _x, _y-1, _c);
      setFill( _x, _y+1, _c);
    }
  }
}
public boolean isDrawingSameStroke( Stroke _st1, Stroke _st2 ) {
  if ( _st1.m_FourierSeriesPt == null || _st2.m_FourierSeriesPt == null ) return false; 
  if ( _st1.m_FourierSeriesPt.length == 0 || _st2.m_FourierSeriesPt.length == 0 ) return false; 

  // まず長さの違いを比較
  float len1 = 0.0f;
  float len2 = 0.0f;
  //  _st1.doFourier();
  //  _st2.doFourier();
  PointF leftTop1 = new PointF(_st1.m_FourierSeriesPt[0].x, _st1.m_FourierSeriesPt[0].y);
  PointF rightBottom1 = new PointF(_st1.m_FourierSeriesPt[0].x, _st1.m_FourierSeriesPt[0].y);
  PointF leftTop2 = new PointF(_st2.m_FourierSeriesPt[0].x, _st2.m_FourierSeriesPt[0].y);
  PointF rightBottom2 = new PointF(_st2.m_FourierSeriesPt[0].x, _st2.m_FourierSeriesPt[0].y);

  for ( int i=0; i<_st1.m_FourierSeriesPt.length-1; i++ ) {
    len1 += dist( _st1.m_FourierSeriesPt[i].x, _st1.m_FourierSeriesPt[i].y, _st1.m_FourierSeriesPt[i+1].x, _st1.m_FourierSeriesPt[i+1].y );
    if ( leftTop1.x > _st1.m_FourierSeriesPt[i+1].x ) leftTop1.x = _st1.m_FourierSeriesPt[i+1].x; 
    if ( leftTop1.y > _st1.m_FourierSeriesPt[i+1].y ) leftTop1.y = _st1.m_FourierSeriesPt[i+1].y; 
    if ( rightBottom1.x < _st1.m_FourierSeriesPt[i+1].x ) rightBottom1.x = _st1.m_FourierSeriesPt[i+1].x; 
    if ( rightBottom1.y < _st1.m_FourierSeriesPt[i+1].y ) rightBottom1.y = _st1.m_FourierSeriesPt[i+1].y;
  }
  for ( int i=0; i<_st2.m_FourierSeriesPt.length-1; i++ ) {
    len2 += dist( _st2.m_FourierSeriesPt[i].x, _st2.m_FourierSeriesPt[i].y, _st2.m_FourierSeriesPt[i+1].x, _st2.m_FourierSeriesPt[i+1].y );
    if ( leftTop2.x > _st2.m_FourierSeriesPt[i+1].x ) leftTop2.x = _st2.m_FourierSeriesPt[i+1].x; 
    if ( leftTop2.y > _st2.m_FourierSeriesPt[i+1].y ) leftTop2.y = _st2.m_FourierSeriesPt[i+1].y; 
    if ( rightBottom2.x < _st2.m_FourierSeriesPt[i+1].x ) rightBottom2.x = _st2.m_FourierSeriesPt[i+1].x; 
    if ( rightBottom2.y < _st2.m_FourierSeriesPt[i+1].y ) rightBottom2.y = _st2.m_FourierSeriesPt[i+1].y;
  }

  float accept_diff_length = 0.4f;
  if ( len2 >= len1 * (1.0f - accept_diff_length) && len2 <= len1 * (1.0f + accept_diff_length) ) {
    println( "Acceptable difference of length", len1, len2 );
  } else {
    println( "NOT acceptable difference of length", len1, len2 );
    return false;
  }

  float accept_diff_ratio = 0.5f;
  float ratio_hw1 = (rightBottom1.x-leftTop1.x)/(rightBottom1.y-leftTop1.y);
  float ratio_hw2 = (rightBottom2.x-leftTop2.x)/(rightBottom2.y-leftTop2.y);

  //if ( ratio_hw1/ratio_hw2 > 1.5-accept_diff_ratio && ratio_hw1/ratio_hw2 < 1.5+accept_diff_ratio){
  //if ( ratio_hw1/ratio_hw2 > 1.0-accept_diff_ratio && ratio_hw1/ratio_hw2 < 1.0+accept_diff_ratio ){
  if( (ratio_hw1>ratio_hw2&&ratio_hw1/ratio_hw2 < 2.0f+accept_diff_ratio)||
  (ratio_hw1<ratio_hw2&&ratio_hw2/ratio_hw1 < 2.0f+accept_diff_ratio)){
   println( "Acceptable difference of ratio h/w", ratio_hw1, ratio_hw2 );
   } else {
   println( "NOT acceptable difference of ratio h/w", ratio_hw1, ratio_hw2 );
   return false;
   }
   

  PointF center1 = new PointF( (leftTop1.x+rightBottom1.x)/2, (leftTop1.y+rightBottom1.y)/2 );
  PointF center2 = new PointF( (leftTop2.x+rightBottom2.x)/2, (leftTop2.y+rightBottom2.y)/2 );
  float R1 = max( -leftTop1.x+rightBottom1.x, -leftTop1.y+rightBottom1.y ) / 2;
  float R2 = max( -leftTop2.x+rightBottom2.x, -leftTop2.y+rightBottom2.y ) / 2;



  float accept_diff_radius = 0.5f;
  if ( R2 >= R1 * (1.0f - accept_diff_radius) && R2 <= R1 * (1.0f + accept_diff_radius) ) {
    println( "Acceptable difference of radius", R1, R2 );
  } else {
    println( "NOT acceptable difference of radius", R1, R2 );
    return false;
  }

  if ( dist( center1.x, center1.y, center2.x, center2.y ) > R1 + R2 ) {
    println( "separated: dist( c1, c2 ) < R1 + R2" );
    return false;
  } else   if ( dist( center1.x, center1.y, center2.x, center2.y ) > (R1 + R2)/2 ) {
    println( "difference: dist( c1, c2 ) < (R1 + R2) / 2" );
    return false;
  }


  println( "Maybe same stroke!" );
  return true;
}

// ストロークを追加する
public void addStroke(Stroke _st1, int _iMultiple){
  _st1.doSpline( _iMultiple );
  _st1.doFourier();
  
  // pre_strokeを書き換える
  //pre_stroke[count%pre_stroke_max] = new Stroke(_st1);
  pre_stroke_list.add(new Stroke(_st1));
  println("stroke drawed. now count is "+count);
}

// ここで平均化の対象かどうかの判定とか行う
public void judgeStroke(Stroke _st1,Stroke _st2, int _iMultiple,boolean _checkBtnVal) {
  if (_st2 != null &&_checkBtnVal) {

    if ( isDrawingSameStroke( _st1, _st2 ) == true ) {
      
      avg_stroke = new Stroke(_st2);//new Stroke(_st);
      avg_stroke.doAverageByStroke( _st1 );
      
      if ( !isDrawingSameStroke( avg_stroke, _st2 ) ) {
        avg_stroke_list = new ArrayList();
        //avg_stroke = null;
        println("Reverse");
        
      } else {
        println(avg_stroke.colorFirstNum+":"+_st2.colorFirstNum);
        averageStroke++;
        
        if(avg_stroke.colorFirstNum==1||_st1.colorFirstNum==1){
          avg_stroke.setColor(color(0, 0, 255));
        }else{
          avg_stroke.setColor(color(255, 0, 0)); 
        }
        
        /*
        // デバッグ用 平均化されるストローク2本を表示します
        averageSaveFile(averageStroke,_st1,_st2);
        */
        
        avg_stroke.displayStrokeByFourier( _iMultiple ); // 赤い線で平均ストロークを描画
        avg_stroke_list.add(new Stroke(avg_stroke,strokeC,strokeW));
        ((Stroke)avg_stroke_list.get(avg_stroke_list.size()-1)).colorFirstNum=_st1.colorFirstNum;
        ((Stroke)avg_stroke_list.get(avg_stroke_list.size()-1)).avgListNum=listNum;
        println("averaeListNumber is "+((Stroke)avg_stroke_list.get(avg_stroke_list.size()-1)).avgListNum);
        //avg_stroke=null; // これをすると失敗する
      }
    }
  }

  und_stroke = null;
}

/*
void canvasRedraw() {
  Canvas.beginDraw();
  Canvas.background(255);
  PImage tmp = loadImage("tmp"+(count-2)+".png");
  Canvas.image(tmp, 0, 0, Canvas.width, Canvas.height);
  Canvas.endDraw();
}
*/

public void canvasTmpSave() {
  if (pushedUndo==false) {
    Canvas.save("tmp"+count+".png");
    println("count is "+count);
  }
  //pushedUndo=false;
  count++;
}
/*
 * ----------------------------------
 *  Radio Button Class for Processing 2.0
 * ----------------------------------
 *
 * this is a simple radio button class. The following shows 
 * you how to use it in a minimalistic way.
 *
 * DEPENDENCIES:
 *   N/A
 *
 * Created:  April, 12 2012
 * Author:   Alejandro Dirgan
 * Version:  0.14
 *
 * License:  GPLv3
 *   (http://www.fsf.org/licensing/)
 *
 * Follow Us
 *    adirgan.blogspot.com
 *    twitter: @ydirgan
 *    https://www.facebook.com/groups/mmiiccrrooss/
 *    https://plus.google.com/b/111940495387297822358/
 *
 * DISCLAIMER **
 * THIS SOFTWARE IS PROVIDED TO YOU "AS IS," AND WE MAKE NO EXPRESS OR IMPLIED WARRANTIES WHATSOEVER 
 * WITH RESPECT TO ITS FUNCTIONALITY, OPERABILITY, OR USE, INCLUDING, WITHOUT LIMITATION, ANY IMPLIED 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR INFRINGEMENT. WE EXPRESSLY 
 * DISCLAIM ANY LIABILITY WHATSOEVER FOR ANY DIRECT, INDIRECT, CONSEQUENTIAL, INCIDENTAL OR SPECIAL 
 * DAMAGES, INCLUDING, WITHOUT LIMITATION, LOST REVENUES, LOST PROFITS, LOSSES RESULTING FROM BUSINESS 
 * INTERRUPTION OR LOSS OF DATA, REGARDLESS OF THE FORM OF ACTION OR LEGAL THEORY UNDER WHICH THE LIABILITY 
 * MAY BE ASSERTED, EVEN IF ADVISED OF THE POSSIBILITY OR LIKELIHOOD OF SUCH DAMAGES.
*/


/*
 this is a simple radio button class. The following shows you how to use it in a minimalistic way.

String[] options = {"First","Second","Third", "Fourth", "Fifth"}; 
boolean[] results;
ADCheck radioButton;
int radio;


PFont output; 

void setup()
{
  size(300,300);
  smooth();
  output = createFont("Arial",22,true);  

  radioButton = new ADCheck(111, 67, options, "radioButton"); 
  radioButton.setDebugOn();
  radioButton.setBoxFillColor(#F7ECD4);  

  results = new boolean[radioButton.length()];

}

void draw()
{
  background(#FFFFFF);

  radioButton.update();
  results=radioButton.getValues();
  
  textFont(output,24);   
  text(options[radioButton.getLastPicked()], (width-textWidth(options[radioButton.getLastPicked()]))/2, height-20);


}

*/

class ADCheck
{
  
  int externalBoxColor=0xff000000;
  int externalFillBoxColor=0xffFFFFFF;
  int internalBoxColor=0xff000000;
  int internalFillBoxColor=0xff000000;
  
  boolean fillExternalBox=false;
  
  PFont rText;
  int textColor=0xff000000;
  int textShadowColor=0xff7E7E7E;
  boolean textShadow=false;
  int textPoints=14;
  
  int xTextOffset=10;
  int yTextSpacing=14;
  
  int boxSide=14;
  float borderLineWidth=0.5f;
 
  float boxLineWidth=0.2f;
  boolean boxFilled=false;
  int boxLineColor=0xff000000;
  int boxFillColor=0xffF4F5D7;
  boolean boxVisible=true;
  
  int boxXMargin=5;
  int boxYMargin=5;
    
  
  String[] radioText;
  boolean[] radioChoose; 
  
  int over=0;
  int nC;
  
  int rX, rY;
  
  float maxTextWidth=0;
  
  String radioLabel;
  
  boolean debug=false;
  
  boolean pressOnlyOnce=true;
  int deb=0;

  int bX, bY, bW, bH;

  
///////////////////////////////////////////////////////  
  ADCheck(int x, int y, String[] op, String id)
  {
    rX=x;
    rY=y;
    radioText=op;
    
    nC=op.length;
    radioChoose = new boolean[nC];
        
    rText = createFont("MS gothic",16,true);
    textFont(rText,textPoints);   
    textAlign(LEFT);
    
    for (int i=0; i<nC; i++) 
    {
      if (textWidth(radioText[i]) > maxTextWidth) maxTextWidth=textWidth(radioText[i]);
      radioChoose[i]=false;
    }
    
    radioChoose[over]=true;
    
    radioLabel=id;
    calculateBox();
    
  }
///////////////////////////////////////////////////////  
  public void calculateBox()
  {
 
    bX=rX-boxXMargin;
    bY=rY-boxYMargin;
    bW=boxSide*2+xTextOffset+(int )maxTextWidth-5;
    bH=radioText.length*boxSide + (radioText.length-1)*yTextSpacing + boxYMargin*2;
  }  
  
///////////////////////////////////////////////////////  
  public void setValue(int n)
  {
    if (n<0) n=0;
    if (n>nC) n=nC;
    
   radioChoose[n]=!radioChoose[n];  
   over=n; 
  }
///////////////////////////////////////////////////////  
  public void deBounce(int n)
  {
    if (pressOnlyOnce) 
      return;
    else
      
    if (deb++ > n) 
    {
      deb=0;
      pressOnlyOnce=true;
    }
    
  }  
  
///////////////////////////////////////////////////////  
  public boolean mouseOver()
  {
    boolean result=false; 
    
    if (debug)
      if ((mouseX>=bX) && (mouseX<=bX+bW) && (mouseY>=bY) && (mouseY<=bY+bH))
      {
        if (mousePressed && mouseButton==LEFT && keyPressed)
        {
          if (keyCode==CONTROL)
          {
            rX=rX+(int )((float )(mouseX-pmouseX)*1);
            rY=rY+(int )((float )(mouseY-pmouseY)*1);
            calculateBox();
          }
          if (keyCode==SHIFT && pressOnlyOnce) 
          {
            printGeometry();
            pressOnlyOnce=false;
          }
          deBounce(5);
          
        }
      }
      
    for (int i=0; i<nC; i++)
    {
      if ((mouseX>=(rX)) && (mouseX<=(rX+boxSide)) && (mouseY>=(rY+i*(yTextSpacing+boxSide))) && (mouseY<=(rY+i*(yTextSpacing+boxSide)+boxSide)))
      {
        result=true;
        
        if (mousePressed && mouseButton==LEFT && pressOnlyOnce)
        {
          over=i;
          setValue(over);
          pressOnlyOnce=false;
        }
        deBounce(5);
        i=nC;
      }
      else
      {
        result=false;
      }
    } 
    return result;
  }
///////////////////////////////////////////////////////  
  public void drawExternalBox()
  {
    if (!boxVisible) return;
    if (boxFilled)
      fill(boxFillColor);
    else
      noFill();
    strokeWeight(boxLineWidth);
    stroke(boxLineColor);
    rect(bX, bY, bW, bH, 10);
  }  
///////////////////////////////////////////////////////  
  public void drawBoxes()
  {
    strokeWeight(borderLineWidth);
    for (int i=0; i<nC; i++)
    {
      if (!fillExternalBox) 
        noFill();
      else
        fill(externalFillBoxColor);  
      stroke(externalBoxColor);  
      rect(rX, rY+(i*(yTextSpacing+boxSide)), boxSide, boxSide);

      //if (i==4) println((i*yTextSpacing));

      fill(internalFillBoxColor);
      stroke(internalBoxColor);  

      if (radioChoose[i])
      {
        strokeWeight(3);
        line(rX+(boxSide+2),rY+i*(yTextSpacing+boxSide)+2,rX+5,rY+i*(yTextSpacing+boxSide)+boxSide-3);
        line(rX+2,rY+i*(yTextSpacing+boxSide)+3,rX+5,rY+i*(yTextSpacing+boxSide)+boxSide-3);
        strokeWeight(boxLineWidth);
      }
    }
    mouseOver();
   
  }
///////////////////////////////////////////////////////  
  public void drawText()
  {
    float yOffset=rY+(boxSide+textPoints)/2;
    float xOffset=rX+boxSide+xTextOffset;
    stroke(textColor);
    textFont(rText,textPoints);   
    textAlign(LEFT);

    for (int i=0; i<nC; i++)
    {
      if (textShadow)
      {
        stroke(textShadowColor);
        text(radioText[i], xOffset+1, yOffset+(i*(yTextSpacing+boxSide)+1));
        stroke(textColor);
      }
      text(radioText[i], xOffset, yOffset+(i*(yTextSpacing+boxSide)));
    }
    
  }  
  
///////////////////////////////////////////////////////  
  public int update()
  {
    drawExternalBox();
    drawBoxes();
    drawText();
    
    return over;
  }

///////////////////////////////////////////////////////  
  public int getLastPicked()
  {
    return over;
  }
///////////////////////////////////////////////////////  
  public boolean getValue(int n)
  {
    if (n<0) n=0;
    if (n>nC) n=nC;

    return radioChoose[n];  
  }
///////////////////////////////////////////////////////  
  public int length()
  {
    return radioChoose.length;  
  } 
///////////////////////////////////////////////////////  
  public boolean[] getValues()
  {
    return radioChoose;  
  } 
///////////////////////////////////////////////////////  
  public void setDebugOn()
  {
    debug=true;
  }
///////////////////////////////////////////////////////  
  public void setDebugOff()
  {
    debug=false;
  }
///////////////////////////////////////////////////////  
  public void printGeometry()
  {
    println("radio = new ADradio("+rX+", "+rY+", arrayOfOptions"+", \""+radioLabel+"\");");

  }
///////////////////////////////////////////////////////  
  public void setexternalBoxColor(int c)
  {
    externalBoxColor=c;
  }
///////////////////////////////////////////////////////  
  public void setexternalFillBoxColor(int c)
  {
    externalFillBoxColor=c;
  }
///////////////////////////////////////////////////////  
  public void setinternalBoxColorr(int c)
  {
    externalFillBoxColor=c;
  }
///////////////////////////////////////////////////////  
  public void setinternalFillBoxColor(int c)
  {
    externalFillBoxColor=c;
  }
///////////////////////////////////////////////////////  
  public void setTextColor(int c)
  {
    textColor=c;
  }
///////////////////////////////////////////////////////  
  public void setTextShadowColor(int c)
  {
    textShadowColor=c;
  }
///////////////////////////////////////////////////////  
  public void setShadowOn()
  {
    textShadow=true;
  }
///////////////////////////////////////////////////////  
  public void setShadowOff()
  {
    textShadow=false;
  }
///////////////////////////////////////////////////////  
  public void setTextSize(int s)
  {
    textPoints=s;
  }
///////////////////////////////////////////////////////  
  public void setXTextOffset(int s)
  {
    xTextOffset=s;
  }
///////////////////////////////////////////////////////  
  public void setYTexSpacing(int s)
  {
    yTextSpacing=s;
  }
///////////////////////////////////////////////////////  
  public void setboxSide(int s)
  {
    boxSide=s;
  }
///////////////////////////////////////////////////////  
  public void setBoxLineWidth(int s)
  {
    boxLineWidth=s;
  }
///////////////////////////////////////////////////////  
  public void setBoxLineColor(int c)
  {
    boxLineColor=c;
  }
///////////////////////////////////////////////////////  
  public void setBoxFillColor(int c)
  {
    boxFillColor=c;
    setBoxFilledOn();
  }
///////////////////////////////////////////////////////  
  public void setBoxFilledOn()
  {
    boxFilled=true;
  }
///////////////////////////////////////////////////////  
  public void setBoxFilledOff()
  {
    boxFilled=false;
  }
///////////////////////////////////////////////////////  
  public void setBoxVisibleOn()
  {
    boxVisible=true;
  }
///////////////////////////////////////////////////////  
  public void setBoxVisibleOff()
  {
    boxVisible=false;
  }
///////////////////////////////////////////////////////  
  public void setLabel(String l)
  {
    radioLabel=l;
  }

}



/*
 * ----------------------------------
 *  Radio Button Class for Processing 2.0
 * ----------------------------------
 *
 * this is a simple radio button class. The following shows
 * you how to use it in a minimalistic way.
 *
 * DEPENDENCIES:
 *   N/A
 *
 * Created:  April, 12 2012
 * Author:   Alejandro Dirgan
 * Version:  0.14
 *
 * License:  GPLv3
 *   (http://www.fsf.org/licensing/)
 *
 * Follow Us
 *    adirgan.blogspot.com
 *    twitter: @ydirgan
 *    https://www.facebook.com/groups/mmiiccrrooss/
 *    https://plus.google.com/b/111940495387297822358/
 *
 * DISCLAIMER **
 * THIS SOFTWARE IS PROVIDED TO YOU "AS IS," AND WE MAKE NO EXPRESS OR IMPLIED WARRANTIES WHATSOEVER
 * WITH RESPECT TO ITS FUNCTIONALITY, OPERABILITY, OR USE, INCLUDING, WITHOUT LIMITATION, ANY IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR INFRINGEMENT. WE EXPRESSLY
 * DISCLAIM ANY LIABILITY WHATSOEVER FOR ANY DIRECT, INDIRECT, CONSEQUENTIAL, INCIDENTAL OR SPECIAL
 * DAMAGES, INCLUDING, WITHOUT LIMITATION, LOST REVENUES, LOST PROFITS, LOSSES RESULTING FROM BUSINESS
 * INTERRUPTION OR LOSS OF DATA, REGARDLESS OF THE FORM OF ACTION OR LEGAL THEORY UNDER WHICH THE LIABILITY
 * MAY BE ASSERTED, EVEN IF ADVISED OF THE POSSIBILITY OR LIKELIHOOD OF SUCH DAMAGES.
*/
 
 
/*
 this is a simple radio button class. The following shows you how to use it in a minimalistic way.
 
 
String[] options = {"First","Second","Third", "Fourth"};
ADradio radioButton;
int radio;
 
 
PFont output;
 
void setup()
{
  size(300,300);
  smooth();
  output = createFont("Arial",24,true); 
 
  radioButton = new ADradio(117, 78, options, "radioButton");
  radioButton.setDebugOn();
  radioButton.setBoxFillColor(#F7ECD4); 
  radioButton.setValue(1);
 
}
 
void draw()
{
  background(#FFFFFF);
 
  radioButton.update();
 
  textFont(output,24);  
  text(options[radioButton.getValue()], (width-textWidth(options[radioButton.getValue()]))/2, height-20);
 
}
 
 
*/
 
class ADRadio
{
   
  int externalCircleColor=0xff000000;
  int externalFillCircleColor=0xffFFFFFF;
  int internalCircleColor=0xff000000;
  int internalFillCircleColor=0xff000000;
   
  boolean fillExternalCircle=false;
   
  PFont rText;
  int textColor=0xff000000;
  int textShadowColor=0xff7E7E7E;
  boolean textShadow=false;
  int textPoints=14;
   
  int xTextOffset=20;
  int yTextSpacing=14;
   
  int circleRadius=16;
  float circleLineWidth=0.5f;
  
  float boxLineWidth=0.2f;
  boolean boxFilled=false;
  int boxLineColor=0xff000000;
  int boxFillColor=0xffF4F5D7;
  boolean boxVisible=true;
   
  String[] radioText;
  boolean[] radioChoose;
   
  int over=0;
  int nC;
   
  int rX, rY;
   
  float maxTextWidth=0;
   
  String radioLabel;
   
  boolean debug=false;
   
  int boxXMargin=5;
  int boxYMargin=5;
   
  int bX, bY, bW, bH;
  int boxRadius=0;
  
  boolean pressOnlyOnce=true;
  int deb=0;   
   
/////////////////////////////////////////////////////// 
  ADRadio(int x, int y, String[] op, String id)
  {
    rX=x;
    rY=y;
    radioText=op;
     
    nC=op.length;
    radioChoose = new boolean[nC];
         
    rText = createFont("MS gothic",16,true);     
    textFont(rText,textPoints);  
    textAlign(LEFT);
     
    for (int i=0; i<nC; i++)
    {
      if (textWidth(radioText[i]) > maxTextWidth) maxTextWidth=textWidth(radioText[i]);
      radioChoose[i]=false;
    }
     
    radioChoose[over]=true;
     
    radioLabel=id;
     
    calculateBox();
     
  }
   
/////////////////////////////////////////////////////// 
  public void calculateBox()
  {
    bX=rX-circleRadius/2-boxXMargin;
    bY=rY-circleRadius/2-boxYMargin;
    bW=circleRadius*2+xTextOffset+(int )maxTextWidth-10;
    bH=radioText.length*circleRadius + (radioText.length-1)*yTextSpacing + boxYMargin*2;
  } 
/////////////////////////////////////////////////////// 
  public void setValue(int n)
  {
    if (n<0) n=0;
    if (n>(nC-1)) n=nC-1;
     
   for (int i=0; i<nC; i++) radioChoose[i]=false;
   radioChoose[n]=true; 
   over=n;
  }
/////////////////////////////////////////////////////// 
  public void deBounce(int n)
  {
    if (pressOnlyOnce)
      return;
    else
       
    if (deb++ > n)
    {
      deb=0;
      pressOnlyOnce=true;
    }
     
  }  /////////////////////////////////////////////////////// 
  public boolean mouseOver()
  {
    boolean result=false;
     
    if (debug)
      if ((mouseX>=bX) && (mouseX<=bX+bW) && (mouseY>=bY) && (mouseY<=bY+bH))
      {
        if (mousePressed && mouseButton==LEFT && keyPressed)
        {
          if (keyCode==CONTROL)
          {
            rX=rX+(int )((float )(mouseX-pmouseX)*1);
            rY=rY+(int )((float )(mouseY-pmouseY)*1);
            calculateBox();
          }
          if (keyCode==SHIFT && pressOnlyOnce)
          {
            printGeometry();
            pressOnlyOnce=false;
          }
          deBounce(5);
           
        }
      }
       
    for (int i=0; i<nC; i++)
    {
      if ((mouseX>=(rX-circleRadius)) && (mouseX<=(rX+circleRadius)) && (mouseY>=(rY+(i*(yTextSpacing+circleRadius))-circleRadius)) && (mouseY<=(rY+(i*(yTextSpacing+circleRadius))+circleRadius)))
      {
        result=true;
         
        if (mousePressed && mouseButton==LEFT && pressOnlyOnce)
        {
          over=i;
          setValue(over);
          pressOnlyOnce=false;
        }
        deBounce(5);
        i=nC;
      }
      else
      {
        result=false;
      }
    }
    return result;
  }
/////////////////////////////////////////////////////// 
  public void drawBox()
  {
    if (!boxVisible) return;
    if (boxFilled)
      fill(boxFillColor);
    else
      noFill();
    strokeWeight(boxLineWidth);
    stroke(boxLineColor);
 
    rect(bX, bY, bW, bH, boxRadius);
 
  } 
/////////////////////////////////////////////////////// 
  public void drawCircles()
  {
    strokeWeight(circleLineWidth);
    for (int i=0; i<nC; i++)
    {
      if (!fillExternalCircle)
        noFill();
      else
        fill(externalFillCircleColor); 
      stroke(externalCircleColor); 
      ellipse(rX, rY+(i*(yTextSpacing+circleRadius)), circleRadius, circleRadius);
 
      fill(internalFillCircleColor);
      stroke(internalCircleColor); 
 
      if (radioChoose[i])
         ellipse(rX, rY+(i*(yTextSpacing+circleRadius)), circleRadius-8, circleRadius-8);
    }
    mouseOver();
    
  }
/////////////////////////////////////////////////////// 
  public void drawText()
  {
    float yOffset=rY+textPoints/3+1;
    stroke(textColor);
    textFont(rText,textPoints);  
    textAlign(LEFT);
 
    for (int i=0; i<nC; i++)
    {
      if (textShadow)
      {
        stroke(textShadowColor);
        text(radioText[i], rX+xTextOffset+1, yOffset+(i*(yTextSpacing+circleRadius))+1);
        stroke(textColor);
      }
      text(radioText[i], rX+xTextOffset, yOffset+(i*(yTextSpacing+circleRadius)));
    }
     
  } 
   
/////////////////////////////////////////////////////// 
  public int update()
  {
    drawBox();
    drawCircles();
    drawText();
     
    return over;
  }
 
/////////////////////////////////////////////////////// 
  public int getValue()
  {
    return over;
  }
  
/////////////////////////////////////////////////////// 
  public void setDebugOn()
  {
    debug=true;
  }
/////////////////////////////////////////////////////// 
  public void setDebugOff()
  {
    debug=false;
  }
/////////////////////////////////////////////////////// 
  public void printGeometry()
  {
    println("radio = new ADradio("+rX+", "+rY+", arrayOfOptions"+", \""+radioLabel+"\");");
 
  }
/////////////////////////////////////////////////////// 
  public void setExternalCircleColor(int c)
  {
    externalCircleColor=c;
  }
/////////////////////////////////////////////////////// 
  public void setExternalFillCircleColor(int c)
  {
    externalFillCircleColor=c;
  }
/////////////////////////////////////////////////////// 
  public void setInternalCircleColorr(int c)
  {
    externalFillCircleColor=c;
  }
/////////////////////////////////////////////////////// 
  public void setInternalFillCircleColor(int c)
  {
    externalFillCircleColor=c;
  }
/////////////////////////////////////////////////////// 
  public void setTextColor(int c)
  {
    textColor=c;
  }
/////////////////////////////////////////////////////// 
  public void setTextShadowColor(int c)
  {
    textShadowColor=c;
  }
/////////////////////////////////////////////////////// 
  public void setShadowOn()
  {
    textShadow=true;
  }
/////////////////////////////////////////////////////// 
  public void setShadowOff()
  {
    textShadow=false;
  }
/////////////////////////////////////////////////////// 
  public void setTextSize(int s)
  {
    textPoints=s;
  }
/////////////////////////////////////////////////////// 
  public void setXTextOffset(int s)
  {
    xTextOffset=s;
  }
/////////////////////////////////////////////////////// 
  public void setyTextSpacing(int s)
  {
    yTextSpacing=s;
  }
/////////////////////////////////////////////////////// 
  public void setCircleRadius(int s)
  {
    circleRadius=s;
  }
/////////////////////////////////////////////////////// 
  public void setBoxLineWidth(int s)
  {
    boxLineWidth=s;
  }
/////////////////////////////////////////////////////// 
  public void setBoxLineColor(int c)
  {
    boxLineColor=c;
  }
/////////////////////////////////////////////////////// 
  public void setBoxRadius(int s)
  {
    boxRadius=s;
  }
/////////////////////////////////////////////////////// 
  public void setBoxFillColor(int c)
  {
    boxFillColor=c;
    setBoxFilledOn();
  }
/////////////////////////////////////////////////////// 
  public void setBoxFilledOn()
  {
    boxFilled=true;
  }
/////////////////////////////////////////////////////// 
  public void setBoxFilledOff()
  {
    boxFilled=false;
  }
/////////////////////////////////////////////////////// 
  public void setBoxVisibleOn()
  {
    boxVisible=true;
  }
/////////////////////////////////////////////////////// 
  public void setBoxVisibleOff()
  {
    boxVisible=false;
  }
/////////////////////////////////////////////////////// 
  public void setLabel(String l)
  {
    radioLabel=l;
  }
 
}

/*描画*/
public void setFill(int _x, int _y, int _c, int _t) {

  int c = get(_x, _y);

  if (c == _c) {
    stroke(255, 0, 0);
    point(_x, _y);
    setFill( _x-1, _y, _c, _t);
    setFill( _x+1, _y, _c, _t);
    setFill( _x, _y-1, _c, _t);
    setFill( _x, _y+1, _c, _t);
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "AvgPainter" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
