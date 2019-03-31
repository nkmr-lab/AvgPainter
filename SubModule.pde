
class PointF {
  float x;
  float y;
  
  PointF() { 
    x = 0.0; 
    y = 0.0;
  }
  
  PointF( float _x, float _y ) { 
    x = _x; 
    y = _y;
  }
  
  PointF( PointF _pt ){
    x = _pt.x;
    y = _pt.y;
  }
}

PointF [] DoubleBack( PointF [] _points ) {
  PointF [] _retPoints = new PointF [_points.length*2-1];
  for ( int i=0; i<_points.length; i++ ) {
    _retPoints[i] = new PointF( _points[i].x, _points[i].y );
    _retPoints[_retPoints.length-i-1] = new PointF( _points[i].x, _points[i].y );
  }
  return _retPoints;
}

boolean isDrawingSameStroke( Stroke _st1, Stroke _st2 ) {
  if ( _st1.m_FourierSeriesPt == null || _st2.m_FourierSeriesPt == null ) return false; 
  if ( _st1.m_FourierSeriesPt.length == 0 || _st2.m_FourierSeriesPt.length == 0 ) return false; 

  // まず長さの違いを比較
  float len1 = 0.0;
  float len2 = 0.0;
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

  float accept_diff_length = 0.4;
  if ( len2 >= len1 * (1.0 - accept_diff_length) && len2 <= len1 * (1.0 + accept_diff_length) ) {
    println( "Acceptable difference of length", len1, len2 );
  } else {
    println( "NOT acceptable difference of length", len1, len2 );
    return false;
  }

  float accept_diff_ratio = 0.5;
  float ratio_hw1 = (rightBottom1.x-leftTop1.x)/(rightBottom1.y-leftTop1.y);
  float ratio_hw2 = (rightBottom2.x-leftTop2.x)/(rightBottom2.y-leftTop2.y);

  //if ( ratio_hw1/ratio_hw2 > 1.5-accept_diff_ratio && ratio_hw1/ratio_hw2 < 1.5+accept_diff_ratio){
  //if ( ratio_hw1/ratio_hw2 > 1.0-accept_diff_ratio && ratio_hw1/ratio_hw2 < 1.0+accept_diff_ratio ){
  if ( (ratio_hw1>ratio_hw2&&ratio_hw1/ratio_hw2 < 2.0+accept_diff_ratio)||
    (ratio_hw1<ratio_hw2&&ratio_hw2/ratio_hw1 < 2.0+accept_diff_ratio)) {
    println( "Acceptable difference of ratio h/w", ratio_hw1, ratio_hw2 );
  } else {
    println( "NOT acceptable difference of ratio h/w", ratio_hw1, ratio_hw2 );
    return false;
  }


  PointF center1 = new PointF( (leftTop1.x+rightBottom1.x)/2, (leftTop1.y+rightBottom1.y)/2 );
  PointF center2 = new PointF( (leftTop2.x+rightBottom2.x)/2, (leftTop2.y+rightBottom2.y)/2 );
  float R1 = max( -leftTop1.x+rightBottom1.x, -leftTop1.y+rightBottom1.y ) / 2;
  float R2 = max( -leftTop2.x+rightBottom2.x, -leftTop2.y+rightBottom2.y ) / 2;



  float accept_diff_radius = 0.5;
  if ( R2 >= R1 * (1.0 - accept_diff_radius) && R2 <= R1 * (1.0 + accept_diff_radius) ) {
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

String getCurrentTime() {
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

  return strFileName;
}

void refreshCanvas() {
  pushStyle();
  fill(255);
  noStroke();
  rect(g_sideMenuWidth, 0, g_canvasWidth, g_canvasHeight);
  popStyle();
}

// ControlP5関連のセットアップ
void configCtrlP5() {

  slider = new ControlP5(this);
  slider.addSlider("g_stWeight")
    .setLabel("Stroke Weight")
    .setRange(0, 50)//0~100の間
    .setValue(5)//初期値
    .setPosition(50, 150)//位置
    .setSize(100, 20);//大きさ

  slider.addSlider("g_stColorR")
    .setLabel("Color-R")
    .setRange(0, 255)//0~100の間
    .setValue(0)//初期値
    .setPosition(50, 200)//位置
    .setSize(100, 20);//大きさ

  slider.addSlider("g_stColorG")
    .setLabel("Color-G")
    .setRange(0, 255)//0~100の間
    .setValue(0)//初期値
    .setPosition(50, 250)//位置
    .setSize(100, 20);//大きさ

  slider.addSlider("g_stColorB")
    .setLabel("Color-B")
    .setRange(0, 255)//0~100の間
    .setValue(0)//初期値
    .setPosition(50, 300)//位置
    .setSize(100, 20);//大きさ

  button = new ControlP5(this);

  button.addButton("tappedLoad")
    .setLabel("load")//テキスト
    .setPosition(25, 350)
    .setSize(100, 40);

  button.addButton("tappedSave")
    .setLabel("save")//テキスト
    .setPosition(155, 350)
    .setSize(100, 40);

  button.addButton("tappedUndo")
    .setLabel("undo")//テキスト
    .setPosition(25, 420)
    .setSize(100, 40);

  button.addButton("tappedReset")
    .setLabel("reset")//テキスト
    .setPosition(155, 420)
    .setSize(100, 40);
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
    String filePath = selection.getAbsolutePath();

    if (filePath.endsWith("png")) {
      println("っぺ");
      PImage img = loadImage(filePath);
      image(img, g_sideMenuWidth, 0, g_canvasWidth, g_canvasHeight);
    }
    if (filePath.endsWith("json")) {
      println("ストローク読み込み");
      String [] lines = loadStrings(selection.getAbsolutePath());
    }
  }
}
