
// implemented by matasuna
void saveHandwiting(String _filename, ArrayList<Stroke> _m_Strokes, String _character) {

  _filename+=".json";
  JSONObject HandwritingJSON;//保存する情報そのもの
  JSONArray strokesJSON;//複数画の情報を保存

  /*環境依存の変数*/
  int magnification;
  int default_double_back_margin;

  /*環境依存の変数*/
  magnification = 200;
  default_double_back_margin = 16;

  strokesJSON = new JSONArray();//strokeの集まり配列をstrokesにする
  HandwritingJSON = new JSONObject();//吐き出すファイル

  //_charstrokeクラスの情報をすべて引きずり出しjsonで保存する
  HandwritingJSON.setString("character", _character);//書かれている文字情報
  HandwritingJSON.setInt("strokeLength", _m_Strokes.size());

  for (Stroke _stroke : _m_Strokes) {//_m_Strokesに登録サれてる画数分回す
    JSONObject strokeJSON = new JSONObject();//1画分が入る

    /***********/
    //オリジナルpointを保存する
    //origpointを_stroke.m_orgPtに書き換え必要
    if ( _stroke.m_orgPt != null ) {
      JSONArray points = savePoints(_stroke.m_orgPt);
      strokeJSON.setJSONArray("points", points);
    }
    /***********/

    /***********/
    //DFTの級数を保存する
    //dftを_stroke.m_Fourierに書き換え必要
    Fourier dft = _stroke.m_Fourier;
    JSONObject DFT = saveDFT(dft, _stroke.m_iAppropriateDegreeOfFourier);
    strokeJSON.setJSONObject("DFT", DFT);
    /***********/

    //splineの情報を保存する(環境依存のため)
    JSONObject spline = new JSONObject();
    spline.setInt("magnification", magnification);
    spline.setInt("default_double_back_margin", default_double_back_margin);
    strokeJSON.setJSONObject("spline", spline);

    //1画を確定して保存
    strokesJSON.append(strokeJSON);
  }
  HandwritingJSON.setJSONArray("strokes", strokesJSON);
  saveJSONObject(HandwritingJSON, _filename);
}

JSONArray savePoints(PointF[] _points) {
  JSONArray points = new JSONArray();
  if ( _points == null) return  points;
  for (PointF _point : _points) {
    JSONObject point = new JSONObject();
    point.setFloat("x", _point.x);
    point.setFloat("y", _point.y);
    points.append(point);
  }
  return points;
}

JSONObject saveDFT(Fourier _dft, int _iDegree) {

  //float [] reX;   // xについてFourierSeriesの実部
  //float [] reY    // yについてFourierSeriesの実部 
  //float [] imX;   // xについてFourierSeriesの虚部
  //float [] imYY;  // yについてFourierSeriesの虚部

  /*********************/
  //_dftからの情報に書き換えてください
  float[] _reX = _dft.m_aX;
  float[] _reY = _dft.m_aY;
  float[] _imX = _dft.m_bX;
  float[] _imY = _dft.m_bY;
  float _strokeLength = 0.0;
  int _maxDegree = _iDegree;
  int _splineSize = 0;
  float _thresholdOfCoefficient = 0.0;
  float aX = 1.0;//1点だった場合の中心
  float aY = 1.0;
  /*********************/

  JSONObject _DFT = new JSONObject();

  JSONArray reX = ArrayToDFT(_reX, _iDegree);
  JSONArray reY = ArrayToDFT(_reY, _iDegree);
  JSONArray imX = ArrayToDFT(_imX, _iDegree);
  JSONArray imY = ArrayToDFT(_imY, _iDegree);

  _DFT.setJSONArray("reX", reX);
  _DFT.setJSONArray("reY", reY);
  _DFT.setJSONArray("imX", imX);
  _DFT.setJSONArray("imY", imY);
  _DFT.setFloat("aX", aX);
  _DFT.setFloat("aY", aY);
  _DFT.setFloat("strokeLength", _strokeLength);
  _DFT.setInt("maxDegree", _maxDegree);
  _DFT.setInt("splineSize", _splineSize);
  _DFT.setFloat("thresholdOfCoefficient", _thresholdOfCoefficient);
  return _DFT;
}

JSONArray ArrayToDFT(float[] coefficients, int _iDegree) {
  //DFTの級数の配列をjsonへ
  JSONArray _DFT= new JSONArray();
  for ( int i=0; i< _iDegree ; i++) {
    float coefficient = coefficients[i];
    _DFT.append(coefficient);
  }
  return _DFT;
}

PointF [] jsonToPointF(JSONArray _jsonPoints) {
  ArrayList<PointF> _pointFs = new ArrayList<PointF>();
  for (int i = 0; i<_jsonPoints.size (); i++) {
    JSONObject jsonPoint = _jsonPoints.getJSONObject(i);
    PointF _point = new PointF(jsonPoint.getFloat("x"), jsonPoint.getFloat("y"));
    _pointFs.add(_point);
  }
  PointF[] _points=(PointF[])_pointFs.toArray(new PointF[0]);
  return _points;
}

Fourier jsonToFourier(JSONObject _dft) {
  float[] reX = jsonToDFTArray(_dft.getJSONArray("reX"));
  float[] reY = jsonToDFTArray(_dft.getJSONArray("reY"));
  float[] imX = jsonToDFTArray(_dft.getJSONArray("imX"));
  float[] imY = jsonToDFTArray(_dft.getJSONArray("imY"));

  /************/
  //各種設定ファイル
  //float _strokeLength = _dft.getFloat("strokeLenfgth");
  //int _maxDegree = _dft.getInt("maxDegree");
  //int _splineSize = _dft.getInt("splineSize");
  //float _thresholdOfCoefficient = _dft.getFloat("thresholdOfCoefficient");
  /************/
  return new Fourier(reX, imX, reY, imY);
}
float[] jsonToDFTArray(JSONArray _jsondft) {
  float [] _coefficient = new float[_jsondft.size()];
  for (int i=0; i<_jsondft.size (); i++) {
    _coefficient[i] = _jsondft.getFloat(i);
  }
  return _coefficient;
}

//ArrayList<Stroke> loadHandwiting(String _filename) {
//  /*環境依存の変数*/
//  int magnification;
//  int default_double_back_margin;
//
//  ArrayList<Stroke> _m_Strokes = new ArrayList<Stroke>();
//  JSONObject jsonload = loadJSONObject(_filename);
//  String character = jsonload.getString("character");
//  int strokeLength = jsonload.getInt("strokeLength");
//
//  JSONArray _strokes = jsonload.getJSONArray("strokes");
//
//  for (int i = 0; i< _strokes.size(); i++) {
//    JSONObject _stroke=_strokes.getJSONObject(i);
//    JSONArray jsonPoints = _stroke.getJSONArray("points");
//    PointF [] points = jsonToPointF(jsonPoints);//pointFの配列を作成
//    Stroke stroke = new Stroke(points);//strokeを作成
//    JSONObject jsonDFT = _stroke.getJSONObject("DFT");
//    Fourier _m_Fourier = jsonToFourier(jsonDFT);
//    stroke.m_Fourier = _m_Fourier;//DFTをstrokeに追加
//
//    /*環境依存の変数*/
//    JSONObject spline = _stroke.getJSONObject("spline");
//    magnification = spline.getInt("magnification");
//    default_double_back_margin = spline.getInt("default_double_back_margin");
//    _m_Strokes.add(stroke);
//  }
//  return _m_Strokes;
//}
//
//PointF [] jsonToPointF(JSONArray _jsonPoints) {
//  ArrayList<PointF> _pointFs = new ArrayList<PointF>();
//  for (int i = 0; i<_jsonPoints.size(); i++) {
//    JSONObject jsonPoint = _jsonPoints.getJSONObject(i);
//    PointF _point = new PointF(jsonPoint.getFloat("x"), jsonPoint.getFloat("y"));
//    _pointFs.add(_point);
//  }
//  PointF[] _points=(PointF[])_pointFs.toArray(new PointF[0]);
//  return _points;
//}
//
//Fourier jsonToFourier(JSONObject _dft) {
//  float[] reX = jsonToDFTArray(_dft.getJSONArray("reX"));
//  float[] reY = jsonToDFTArray(_dft.getJSONArray("reY"));
//  float[] imX = jsonToDFTArray(_dft.getJSONArray("imX"));
//  float[] imY = jsonToDFTArray(_dft.getJSONArray("imY"));
//
//  /************/
//  //各種設定ファイル
//  float _strokeLength = _dft.getFloat("strokeLength");
//  int _maxDegree = _dft.getInt("maxDegree");
//  int _splineSize = _dft.getInt("splineSize");
//  float _thresholdOfCoefficient = _dft.getFloat("thresholdOfCoefficient");
//  /************/
//  return new Fourier(reX, imX, reY, imY);
//}
//float[] jsonToDFTArray(JSONArray _jsondft) {
//  float [] _coefficient = new float[_jsondft.size()];
//  for (int i=0; i<_jsondft.size(); i++) {
//    _coefficient[i] = _jsondft.getFloat(i);
//  }
//  return _coefficient;
//}
