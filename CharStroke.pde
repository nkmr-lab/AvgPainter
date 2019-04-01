
class CharStroke {
  ArrayList<Stroke> m_Strokes;
  ArrayList<Stroke> m_undoStrokes;

  CharStroke() {
    m_Strokes = new ArrayList<Stroke>();
    m_undoStrokes = new ArrayList<Stroke>();
  }
  
  // JSON形式でストローク情報を保存する
  void saveStrokes( String _fileName ) {
    JSONObject HandwritingJSON = new JSONObject();//吐き出すファイルそのもの
    JSONArray strokesJSON = new JSONArray(); //strokeの集まり配列をstrokesにする

    //_charstrokeクラスの情報をすべて引きずり出しjsonで保存する
    HandwritingJSON.setInt("strokeLength", m_Strokes.size());

    for (Stroke _stroke : m_Strokes) {//_m_Strokesに登録サれてる画数分回す
      JSONObject strokeJSON = new JSONObject();//1画分が入る

      /* ====================================== */
      // オリジナルの点列を保存する
      
      PointF [] rmMarginPt = new PointF[ _stroke.m_orgPt.length ];
      for(int i=0; i<rmMarginPt.length; i++){
        rmMarginPt[i] = new PointF( _stroke.m_orgPt[i].x - g_sideMenuWidth, _stroke.m_orgPt[i].y );
      }
      JSONArray points = savePoints(rmMarginPt);
      strokeJSON.setJSONArray("points", points);

      /* ====================================== */

      /* ====================================== */
      // DFTの級数を保存する
      Fourier dft = _stroke.m_Fourier;
      //dft. ＊＊＊フーリエの数式も移動させる（やり方忘れたのであとで）＊＊＊
      JSONObject DFT = saveDFT(dft, _stroke.m_iAppropriateDegreeOfFourier);
      strokeJSON.setJSONObject("DFT", DFT);
      /* ====================================== */

      /* ====================================== */
      // splineの情報を保存する(環境依存のため)
      JSONObject spline = new JSONObject();
      //spline.setInt("magnification", magnification);
      //spline.setInt("default_double_back_margin", default_double_back_margin);
      //strokeJSON.setJSONObject("spline", spline);
      /* ====================================== */

      // 追加していく
      strokesJSON.append(strokeJSON);
    }
    HandwritingJSON.setJSONArray("strokes", strokesJSON);
    saveJSONObject(HandwritingJSON, _fileName);
  }

  void loadStrokesFromJSON( String _fileName ) {
    m_Strokes = new ArrayList<Stroke>(); // 一応初期化する
    JSONObject jsonload = loadJSONObject( _fileName );
    JSONArray _strokes = jsonload.getJSONArray("strokes");

    for (int i = 0; i< _strokes.size (); i++) {
      JSONObject _stroke = _strokes.getJSONObject(i);
      JSONArray jsonPoints = _stroke.getJSONArray("points");
      PointF [] points = jsonToPointF(jsonPoints);//pointFの配列を作成
      Stroke stroke = new Stroke(points);//strokeを作成

      // Fourierしてあるかを確認
      if ( _stroke.isNull("DFT") == false ) {
        println("~~~~~~~~~~");
        JSONObject jsonDFT = _stroke.getJSONObject("DFT");
        Fourier _m_Fourier = jsonToFourier(jsonDFT);
        stroke.m_Fourier = _m_Fourier;//DFTをstrokeに追加
        /*環境依存の変数*/
        //JSONObject spline = _stroke.getJSONObject("spline");
        //magnification = spline.getInt("magnification");
        //default_double_back_margin = spline.getInt("default_double_back_margin");
      } else {
        // してなかった場合Fourierしてあげる
        stroke.doSpline( g_iMultiple );
        stroke.doFourier();
      }
      m_Strokes.add(stroke);
    }
    //calculateRect();
  }

  Stroke get(int num) {
    return m_Strokes.get(num);
  }

  void add(Stroke addSt) {
    m_Strokes.add( addSt );
  }

  void remove(int num) {
    m_Strokes.remove( num );
  }

  void removeLast() {
    if ( m_Strokes.size() > 0 ) {
      m_Strokes.remove( m_Strokes.size()-1 );
    }
  }


  void undo() {
    if ( m_Strokes.size() > 0 ) {
      println("undo");
      Stroke undoStroke = m_Strokes.get( m_Strokes.size()-1 );
      m_undoStrokes.add( undoStroke );
      m_Strokes.remove( m_Strokes.size()-1 );
    }
  }

  boolean isTapped() {
    boolean isTapped = false;
    for (Stroke st : m_Strokes) {
      isTapped |= st.isInside(mouseX, mouseY);
    }
    return isTapped;
  }

  int isSameStroke(Stroke addSt) {
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

  void displayStroke() {
    for ( Stroke st : m_Strokes ) {
      st.displayStrokeByFourier( g_iMultiple );
    }
  }
}
