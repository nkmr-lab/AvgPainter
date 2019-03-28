
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
}

PointF [] DoubleBack( PointF [] _points ) {
  PointF [] _retPoints = new PointF [_points.length*2-1];
  for ( int i=0; i<_points.length; i++ ) {
    _retPoints[i] = new PointF( _points[i].x, _points[i].y );
    _retPoints[_retPoints.length-i-1] = new PointF( _points[i].x, _points[i].y );
  }
  return _retPoints;
}
