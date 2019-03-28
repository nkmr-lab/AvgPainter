
// AvgPainter: 平均化によって手書きを綺麗にするドローイングツール
// implemented By 新納真次郎

// キャンバスの設定
PGraphics Canvas;
public int CanvasSizeX = 600;
public int CanvasSizeY = 600;
public int MenuAreaSize = 100;

// 仮キャンバス(最後のストロークを表示するキャンバス)
PGraphics AvgCanvas;

// ペンの設定 初期設定 太さ:1px, 色:黒Str
String strokeType = "pen";
int strokeW = 1;
color strokeC = color( 0, 0, 0 );

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
float g_fThresholdOfCoefficient = 0.001;
// フルスクリーンにするかどうかのフラグ
boolean g_bFullScreen = false;
// スプライン補間した後の近接点の除去に利用
double g_fThresholdToRemove = 0.05;
// スプライン補間する際の倍数
int g_iMultiple = 10;
// 平均化対象のストロークの距離の閾値
//int g_iDistance = 5; // a0使うとき
float g_fDistance = 0.5; // a0使わないとき


boolean g_bStroking = false;
boolean g_bStrokeStarting = false;
PointF [] g_mouseStroke;

PImage []temp;
int avg_count = 0;
int avg_time = 0;
color tmpColor;


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

void settings() {
  size(  CanvasSizeX, CanvasSizeY + MenuAreaSize );
}

void setup() {

  pixelDensity(displayDensity());

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
  checkButton.setBoxLineColor(#A5A5A5);
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

void draw() {
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

void showButtons() {
  // メニューバー描画
  noStroke();
  fill( #88D0FF );
  rect( 0, 0, CanvasSizeX, MenuAreaSize );

  checkButton.update();

  strWPanel.update();
  strWPanel.display();

  noFill();
  stroke(#A5A5A5);
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



void showGUI() {

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

void openFile() {
  selectInput("Select a file to process:", "fileSelected");
}

void fileSelected(File selection) {
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
          float x=float(position[0])*1.5;
          float y=float(position[1])*1.5;
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

void saveStrokes() {
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
      tmp = loadImage("tmp/tmp0.png");
    } else {
      tmp = loadImage("tmp/tmp"+(count-1)+".png");
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

void mouseReleased() {
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

void mousePressed() {
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
        PImage tmp=loadImage("tmp/tmp0.png");
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
          tmp = loadImage("tmp/tmp0.png");
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
    PImage tmp=loadImage("tmp/tmp0.png");
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
  } else if (mouseY < MenuAreaSize ) {
    MenuPressed = true;
  }

  showButtons();
}


// ペンタブの時に使用
void keyPressed() {
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
void undo() {
  if (count>0) {
    count-=1;
    avg_stroke = null;
    und_stroke = (Stroke)pre_stroke_list.get(count);
    pre_stroke_list.remove(count);
    println("undo is "+count);

    println("now count is "+count);
    Canvas.beginDraw();
    Canvas.background(255);
    PImage tmp = loadImage("tmp/tmp0.png");
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
