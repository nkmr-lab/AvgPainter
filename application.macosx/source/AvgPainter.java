import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 

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



ControlP5 slider;
ControlP5 button;

// ============= config ==============
//// フーリエの最大次数（次数を高くし過ぎると色々問題が有るため）
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
// =======================================

// UI関連
int g_canvasWidth = 500;
int g_canvasHeight = 500;
int g_sideMenuWidth = 300;


boolean g_bStroking = false;
PointF [] g_mouseStroke;
CharStroke g_curCharStroke;
Stroke g_avgStroke;
int g_sameStIndex = -1;

int g_stWeight;
int g_stColorR, g_stColorG, g_stColorB;


public void settings() {
  size( g_canvasWidth + g_sideMenuWidth, g_canvasHeight );
}

public void setup() {
  
  background( 255 );
  g_curCharStroke = new CharStroke();

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

public void draw() {
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

public void tappedSave() {
  saveFrame( "save/" + getCurrentTime() + ".png" );
}

public void tappedLoad() {
}

public void tappedUndo() {
  println("aaa");
  g_curCharStroke.undo();
  refreshCanvas();
  g_curCharStroke.displayStroke();
}

public void tappedReset() {
  println("reset");
}

public void mousePressed() {
  eventListener();
}

public void mouseReleased() {

  if ( g_bStroking ) {
    g_bStroking = false;

    if ( g_mouseStroke.length > 3 ) {
      Stroke addSt = new Stroke( g_mouseStroke, color(g_stColorR, g_stColorG, g_stColorB), g_stWeight );
      addSt.doFourier();

      int index = g_curCharStroke.isSameStroke(addSt);
      if ( index != -1 ) {
        Stroke st = g_curCharStroke.get(index);
        st.setWeight( g_stWeight );
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

public void eventListener() {
  // 平均ストロークがタップされたら
  if ( mouseX < g_sideMenuWidth ) {
    return;
  }

  if ( g_avgStroke != null && g_avgStroke.isInside( mouseX, mouseY )) {
    println("くりっくされたよ");
    g_curCharStroke.removeLast();
    g_curCharStroke.remove(g_sameStIndex);
    g_avgStroke.setColor( color(g_stColorR, g_stColorG, g_stColorB) );
    g_curCharStroke.add( g_avgStroke );
    refreshCanvas();
    g_curCharStroke.displayStroke();
    g_avgStroke = null;
  } else {
    g_bStroking = true;
    g_mouseStroke = new PointF[0];
    g_avgStroke = null;
  }

  // アンドゥボタンがタップされたら
}

public void showMenu() {
  fill( 0 );
  rect( 0, 0, 300, height );

  fill( 0, 45, 93 );
  rect( 100, 20, 100, 100 );

  fill( g_stColorR, g_stColorG, g_stColorB );
  ellipse( 150, 70, g_stWeight, g_stWeight);
}

public void updateCursor() {
  if ( g_avgStroke != null && g_avgStroke.isInside( mouseX, mouseY ) ) {
    cursor(HAND);
  } else {
    cursor(ARROW);
  }
}

class CharStroke {
  ArrayList<Stroke> m_Strokes;
  ArrayList<Stroke> m_undoStrokes;

  CharStroke() {
    m_Strokes = new ArrayList<Stroke>();
    m_undoStrokes = new ArrayList<Stroke>();
  }

  public Stroke get(int num) {
    return m_Strokes.get(num);
  }

  public void add(Stroke addSt) {
    m_Strokes.add( addSt );
  }

  public void remove(int num) {
    m_Strokes.remove( num );
  }

  public void removeLast() {
    if ( m_Strokes.size() > 0 ) {
      m_Strokes.remove( m_Strokes.size()-1 );
    }
  }


  public void undo() {
    if ( m_Strokes.size() > 0 ) {
      println("undo");
      Stroke undoStroke = m_Strokes.get( m_Strokes.size()-1 );
      m_undoStrokes.add( undoStroke );
      m_Strokes.remove( m_Strokes.size()-1 );
    }
  }

  public boolean isTapped() {
    boolean isTapped = false;
    for (Stroke st : m_Strokes) {
      isTapped |= st.isInside(mouseX, mouseY);
    }
    return isTapped;
  }

  public int isSameStroke(Stroke addSt) {
    boolean isExistSameStroke = false;
    int index = -1;
    for (int i=0; i<m_Strokes.size(); i++) {
      Stroke st = m_Strokes.get(i);
      if ( isDrawingSameStroke( addSt, st ) ) {
        index = i;
      }
    }

    return index;
  }

  public void displayStroke() {
    for ( Stroke st : m_Strokes ) {
      st.displayStrokeByFourier( g_iMultiple );
    }
  }
}

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

class Stroke {
  PointF [] m_orgPt;
  PointF [] m_SplinePt;
  PointF [] m_FourierSeriesPt;
  Fourier m_Fourier;
  boolean m_bFourier;
  int m_iAppropriateDegreeOfFourier;
  int m_Color;
  int m_Weight;

  Stroke( PointF [] _orgPt, int _col, int _weight, String _type)
  {
    m_orgPt = _orgPt;
    m_SplinePt = _orgPt;
    m_Fourier = new Fourier( );
    m_bFourier = false;
    m_Color = _col;
    m_Weight = _weight;
  }

  Stroke(Stroke _st) {
    m_orgPt = _st.m_orgPt;
    m_SplinePt = _st.m_SplinePt;
    m_Fourier = new Fourier(_st.m_Fourier);
    m_FourierSeriesPt = _st.m_FourierSeriesPt;
    m_bFourier = _st.m_bFourier;
    m_Color = _st.m_Color;
    m_Weight = _st.m_Weight;
  }

  Stroke( PointF [] _orgPt, int _col, int _weight) {
    m_orgPt = _orgPt;
    m_SplinePt = _orgPt;
    m_Fourier = new Fourier( );
    m_bFourier = false;
    m_Color = _col;
    m_Weight = _weight;
  }

  Stroke( Stroke _st, int _col, int _weight) {
    m_orgPt = _st.m_orgPt;
    m_SplinePt = _st.m_SplinePt;
    m_Fourier = new Fourier(_st.m_Fourier);
    m_FourierSeriesPt = _st.m_FourierSeriesPt;
    m_bFourier = _st.m_bFourier;
    m_Color = _col;
    m_Weight = _weight;
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
    m_Color = _col;
  }

  public void setWeight(int _weight) {
    m_Weight = _weight;
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

  public void doSpline( int _iMultiple ){
   
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

  public void displayStrokeByFourier(int _iMultiple) {
    if ( m_bFourier == false ) {
      doSpline( _iMultiple );
      doFourier();
    }

    pushStyle();
    smooth( );
    stroke( m_Color );
    strokeWeight( m_Weight );
    for (int num = 0; num <= m_FourierSeriesPt.length/2; num++) {
      line( PApplet.parseInt(m_FourierSeriesPt[num].x), PApplet.parseInt(m_FourierSeriesPt[num].y), PApplet.parseInt(m_FourierSeriesPt[num+1].x), PApplet.parseInt(m_FourierSeriesPt[num+1].y) );
    }
    popStyle();
  }

  public boolean isInside(int _x, int _y) {
    boolean judge = false;
    for (int num = 0; num < m_FourierSeriesPt.length; num++) {
      if (dist(_x, _y, PApplet.parseInt(m_FourierSeriesPt[num].x), PApplet.parseInt(m_FourierSeriesPt[num].y))<=m_Weight+5) {
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
  if ( (ratio_hw1>ratio_hw2&&ratio_hw1/ratio_hw2 < 2.0f+accept_diff_ratio)||
    (ratio_hw1<ratio_hw2&&ratio_hw2/ratio_hw1 < 2.0f+accept_diff_ratio)) {
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

public String getCurrentTime() {
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

public void refreshCanvas() {
  pushStyle();
  fill(255);
  noStroke();
  rect(g_sideMenuWidth, 0, g_canvasWidth, g_canvasHeight);
  popStyle();
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
