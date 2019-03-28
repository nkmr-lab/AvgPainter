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

  void showResults() {
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
  boolean isNeedToReorder() {
    for ( int i=0; i<m_shortestFirstPath.size (); i++ ) {
      // 1つでも一致していないものがあったら要並び替え
      if ( m_shortestFirstPath.get(i) != m_shortestTargetPath.get(i) ) return true;
    }
    return false;
  }

  // 再帰で木を展開していくことによって全部のパターンを出力
  // 組合せ爆発するので現在のバージョンでは使っていない
  void generateEveryPattern( IntList _rest, IntList _ordered ) {
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
  void setDistance( int [][] _distance, int [][] _distanceRev ) {
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
  void showTables( float [][] _distance, float [][] _distanceRev ) {
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
  void findingShortestPathStart( float [][] _distance, float [][] _distanceRev )
  {
    // 値の初期化
    m_shortestDistance = 0.0;
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

  float getDist( int _row, int _col, float [][] _distance, float [][] _distanceRev ) {
    if( _row < 0 ){
      println( "error dist: ", _row, _col );
    }
    if ( _col < 0 ) {
      return _distanceRev[_row-1][-1*_col-1];
    }
    return _distance[_row-1][_col-1];
  }

  void replaceRowCol( float [][] _distance, float [][] _distanceRev ) {
    for ( int i=0; i<_distance.length; i++ ) {
      for ( int j=0; j<_distance.length; j++ ) {
        if ( i==j ) continue;

        float dist = 0.0;
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
  void findingShortestPath( float [][] _distance, float [][] _distanceRev, IntList _firstPath, IntList _targetPath, float _totalDistance )
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
float getDistanceFromFourier( Fourier _first, PointF _firstLeftTop, PointF _firstRightBottom, Fourier _target, PointF _targetLeftTop, PointF _targetRightBottom, boolean _bReverse, boolean _bParallelMove ) {
  float dist = 0.0;
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

    dist += (sq(_first.m_aX[i]-ax)+sq(_first.m_bX[i]-bx)+sq(_first.m_aY[i]-ay)+sq(_first.m_bY[i]-by))*(1.0/(i+1));
    //dist += (abs(_first.m_aX[i]-ax)+abs(_first.m_bX[i]-bx)+abs(_first.m_aY[i]-ay)+abs(_first.m_bY[i]-by))*(1.0/(i+1));
  }

  // 値が大きいので1/1000する
  return dist/1000.0;
}

// コサイン類似度で距離計算をしてみるとどうか？
float getDistanceFromFourierCos( Fourier _first, PointF _firstLeftTop, PointF _firstRightBottom, Fourier _target, PointF _targetLeftTop, PointF _targetRightBottom, boolean _bReverse, boolean _bParallelMove ) {
  float dist = 0.0;


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

  float targetSumSQ = 0.0;
  float firstSumSQ = 0.0;
  float sumInnerProduct = 0.0;

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
    dist += (sq(_first.m_aX[i]-ax)+sq(_first.m_bX[i]+bx)+sq(_first.m_aY[i]-ay)+sq(_first.m_bY[i]+by))*(1.0/(i+1));
    //  dist += (abs(_first.m_aX[i]-ax)+abs(_first.m_bX[i]-bx)+abs(_first.m_aY[i]-ay)+abs(_first.m_bY[i]-by))*(1.0/(i+1));
  }

  float cosSimilarity = sumInnerProduct / (sqrt(firstSumSQ)*sqrt(targetSumSQ));
  return 1.1-cosSimilarity;
  // 値が大きいので1/1000する
  //  return dist/1000.0;
}

