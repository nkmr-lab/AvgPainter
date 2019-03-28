
// AvgPainter: 平均化によって手書きを綺麗にするドローイングツール
// implemented By 新納真次郎

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

boolean g_bStroking = false;
int strokeW = 10;
PointF [] g_mouseStroke;
CharStroke g_curCharStroke;
Stroke g_avgStroke;
int g_sameStIndex = -1;

void setup() {
  size( 600, 400 );
  pixelDensity(2);
  background( 255 );
  g_curCharStroke = new CharStroke();
}

void draw() {
  if ( g_avgStroke != null && g_avgStroke.isInside( mouseX, mouseY ) ) {
    cursor(HAND);
  } else {
    cursor(ARROW);
  }

  if ( g_bStroking ) {
    strokeWeight( 10 );
    stroke( 0 );
    line( pmouseX, pmouseY, mouseX, mouseY );
    g_mouseStroke = (PointF[])append( g_mouseStroke, new PointF( mouseX, mouseY) ) ;
  }
}

void mousePressed() {
  eventListener();
}

void mouseReleased() {

  if ( g_bStroking ) {
    g_bStroking = false;

    if ( g_mouseStroke.length > 3 ) {
      Stroke addSt = new Stroke( g_mouseStroke, color(0), 10 );
      addSt.doFourier();

      int index = g_curCharStroke.isSameStroke(addSt);
      if ( index != -1 ) {
        Stroke st = g_curCharStroke.get(index);
        st.setWeight( 10 );
        st.setColor(color(255, 0, 0));
        st.doAverageByStroke( addSt );
        st.displayStrokeByFourier(g_iMultiple);
        g_avgStroke = st;
        g_sameStIndex = index;
      }

      g_curCharStroke.add( addSt );
    }

    g_mouseStroke = null;
  }
}

void eventListener() {
  // 平均ストロークがタップされたら
  if ( g_avgStroke != null && g_avgStroke.isInside( mouseX, mouseY )) {
    println("くりっくされたよ");
    g_curCharStroke.removeLast();
    g_curCharStroke.remove(g_sameStIndex);
    g_avgStroke.setColor( color(0) );
    g_curCharStroke.add( g_avgStroke );
    background( 255 );
    g_curCharStroke.displayStroke();
    g_avgStroke = null;
  } else {
    g_bStroking = true;
    g_mouseStroke = new PointF[0];
    g_avgStroke = null;
  }


  // アンドゥボタンがタップされたら
}

void tappedUndoBtn() {
  g_curCharStroke.undo();
}
