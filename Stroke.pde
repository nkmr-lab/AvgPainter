/*描写した線の情報*/
class Stroke {
  PointF [] m_orgPt;
  PointF [] m_SplinePt;
  PointF [] m_FourierSeriesPt;
  Fourier m_Fourier;
  boolean m_bFourier;
  int m_iAppropriateDegreeOfFourier;
  color col;
  int weight;
  String type;
  int colorFirstNum=11;
  boolean isAverageStroke=false;
  int doSplineCount;
  
  int avgListNum;

  Stroke( PointF [] _orgPt, color _col, int _weight, String _type)
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

  Stroke( PointF [] _orgPt, color _col, int _weight) {
    m_orgPt = _orgPt;
    m_SplinePt = _orgPt;
    m_Fourier = new Fourier( );
    m_bFourier = false;
    col = _col;
    weight = _weight;
    type = "pen";
    isAverageStroke=false;
  }
  
  Stroke( Stroke _st, color _col, int _weight) {
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

  PointF getGravityCenter() {
    PointF retPt = new PointF();
    retPt.x = 0.0;
    retPt.y = 0.0;
    for ( int i=0; i<m_orgPt.length; i++ ) {
      retPt.x += m_orgPt[i].x;
      retPt.y += m_orgPt[i].y;
    }
    retPt.x /= m_orgPt.length;
    retPt.y /= m_orgPt.length;
    return retPt;
  }

  void setColor(color _col) {
    col = _col;
  }

  void setType(String _type) {
    type = _type;
  }

  void doReverse() {
    PointF [] tempPt = new PointF [m_orgPt.length];
    for ( int i=0; i<m_orgPt.length; i++ ) {
      tempPt[m_orgPt.length-i-1] = m_orgPt[i];
    }
    for ( int i=0; i<m_orgPt.length; i++ ) {
      m_orgPt[i] = tempPt[i];
    }
  }

  void doSpline( int _iMultiple )
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


  void doFourier() {
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

  void display( int _iShowMode, float _fZoom ) {
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

  void doAverageByStroke( Stroke _addStroke ) {

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

  void displayStroke() {
    
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

  void displayStrokeByFourier( int _iMultiple ) {
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
      Canvas.line( int(m_FourierSeriesPt[num].x+0.5), int(m_FourierSeriesPt[num].y+0.5), int(m_FourierSeriesPt[num+1].x+0.5), int(m_FourierSeriesPt[num+1].y+0.5) );
    }
    Canvas.endDraw();
  }
  
  void displayAverage() {
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
      Canvas.line( int(m_FourierSeriesPt[num].x+0.5), int(m_FourierSeriesPt[num].y+0.5), int(m_FourierSeriesPt[num+1].x+0.5), int(m_FourierSeriesPt[num+1].y+0.5) );
    }
    Canvas.endDraw();
    
    //m_Fourier.ShowEquations(10,g_fThresholdOfCoefficient);
  }

  boolean isInside(int _x, int _y) {
    boolean judge = false;
    for (int num = 0; num < m_FourierSeriesPt.length/2; num++) {
      if (dist(_x, _y, m_FourierSeriesPt[num].x, m_FourierSeriesPt[num].y)<=strokeW+15) {
        judge |= true;
      }
    }

    return judge;
  }

  boolean isEqual(Stroke _st) {

    boolean flag=true;
    for ( int i=0; i<m_orgPt.length; i++ ) {
      if ( m_orgPt[i].x == _st.m_orgPt[i].x && m_orgPt[i].y == _st.m_orgPt[i].y) {
        flag &= true;
      }
    }
    return flag;
  }
}

