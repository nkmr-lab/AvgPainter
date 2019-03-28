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
  if( (ratio_hw1>ratio_hw2&&ratio_hw1/ratio_hw2 < 2.0+accept_diff_ratio)||
  (ratio_hw1<ratio_hw2&&ratio_hw2/ratio_hw1 < 2.0+accept_diff_ratio)){
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

// ストロークを追加する
void addStroke(Stroke _st1, int _iMultiple){
  _st1.doSpline( _iMultiple );
  _st1.doFourier();
  
  // pre_strokeを書き換える
  //pre_stroke[count%pre_stroke_max] = new Stroke(_st1);
  pre_stroke_list.add(new Stroke(_st1));
  println("stroke drawed. now count is "+count);
}

// ここで平均化の対象かどうかの判定とか行う
void judgeStroke(Stroke _st1,Stroke _st2, int _iMultiple,boolean _checkBtnVal) {
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

void canvasTmpSave() {
  if (pushedUndo==false) {
    Canvas.save("tmp"+count+".png");
    println("count is "+count);
  }
  //pushedUndo=false;
  count++;
}
