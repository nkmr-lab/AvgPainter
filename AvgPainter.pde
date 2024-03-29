
// AvgPainter: 平均化によって手書きを綺麗にするドローイングツール
// implemented By 新納真次郎

import controlP5.*;

ControlP5 slider;
ControlP5 button;

// ============= config ==============
//// フーリエの最大次数（次数を高くし過ぎると色々問題が有るため）
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
// =======================================

// UI関連
int g_canvasWidth = 500;
int g_canvasHeight = 500;
int g_sideMenuWidth = 300;

// ストローク関連
int g_stWeight;
int g_stColorR, g_stColorG, g_stColorB;

boolean g_bStroking = false;
PointF [] g_mouseStroke;
CharStroke g_curCharStroke;
Stroke g_avgStroke;
int g_sameStIndex = -1;

void settings() {
  size( g_canvasWidth + g_sideMenuWidth, g_canvasHeight );
}

void setup() {
  pixelDensity(2);
  background( 255 );
  g_curCharStroke = new CharStroke();
  configCtrlP5();
}

void draw() {
  updateCursor();
  showMenu();

  if ( g_bStroking ) {
    pushStyle();
    strokeWeight( g_stWeight );
    stroke( g_stColorR, g_stColorG, g_stColorB );
    line( pmouseX, pmouseY, mouseX, mouseY );
    popStyle();
    g_mouseStroke = (PointF[])append( g_mouseStroke, new PointF( mouseX, mouseY) ) ;
  }
}

void tappedSave() {
  g_curCharStroke.saveStrokes("save/" + getCurrentTime() + ".json");
  saveFrame( "save/" + getCurrentTime() + ".png" );
}

void tappedLoad() {
  openFile();
}

void tappedUndo() {
  println("aaa");
  g_curCharStroke.undo();
  refreshCanvas();
  g_curCharStroke.displayStroke();
}

void tappedReset() {
  println("reset");
}

void mousePressed() {
  eventListener();
}

void mouseReleased() {

  if ( g_bStroking ) {
    g_bStroking = false;

    if ( g_mouseStroke.length > 3 ) {
      Stroke addSt = new Stroke( g_mouseStroke, color(g_stColorR, g_stColorG, g_stColorB), g_stWeight );
      addSt.doFourier();

      int index = g_curCharStroke.isSameStroke(addSt);
      if ( index != -1 ) {
        Stroke st = g_curCharStroke.get(index);
        g_avgStroke = st.getAverageByStroke( addSt );

        // 平均線と元の線がだいたい一緒かを確認する( 違った場合は反転処理 )
        Boolean avgIsSame = isDrawingSameStroke( addSt, g_avgStroke );

        if ( !avgIsSame ) {
          println("反転処理!!!");
          addSt.reverse();
          addSt.doForceFourier();
          
          // ⚠ なぜかここの描画がちょっとだけおかしい
          g_avgStroke = st.getAverageByStroke( addSt );
        }

        g_avgStroke.setColor( color(255, 0, 0) );
        g_avgStroke.displayStrokeByFourier(g_iMultiple);
        g_sameStIndex = index;
      }

      g_curCharStroke.add( addSt );
    }

    g_mouseStroke = null;
  }
}

void eventListener() {
  // 平均ストロークがタップされたら
  if ( mouseX < g_sideMenuWidth ) {
    return;
  }

  if ( g_avgStroke != null && g_avgStroke.isInside( mouseX, mouseY )) {
    println("平均線がクリックされたよ");
    g_curCharStroke.removeLast();
    g_curCharStroke.remove(g_sameStIndex);
    g_avgStroke.setColor( color(g_stColorR, g_stColorG, g_stColorB) );
    g_curCharStroke.add( g_avgStroke );
    refreshCanvas();
    g_curCharStroke.displayStroke();
    g_avgStroke = null;
  } else if ( g_avgStroke != null ) { 
    refreshCanvas();
    g_curCharStroke.displayStroke();
    g_avgStroke = null;

    g_bStroking = true;
    g_mouseStroke = new PointF[0];
  } else {
    g_bStroking = true;
    g_mouseStroke = new PointF[0];
    g_avgStroke = null;
  }

  // アンドゥボタンがタップされたら
}

void showMenu() {
  fill( 0 );
  rect( 0, 0, 300, height );

  fill( 0, 45, 93 );
  rect( 100, 20, 100, 100 );

  fill( g_stColorR, g_stColorG, g_stColorB );
  ellipse( 150, 70, g_stWeight, g_stWeight);
}

void updateCursor() {
  if ( g_avgStroke != null && g_avgStroke.isInside( mouseX, mouseY ) ) {
    cursor(HAND);
  } else {
    cursor(ARROW);
  }
}
