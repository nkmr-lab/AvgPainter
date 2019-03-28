/*描画*/
void setFill(int _x, int _y, color _c, int _t) {

  color c = get(_x, _y);

  if (c == _c) {
    stroke(255, 0, 0);
    point(_x, _y);
    setFill( _x-1, _y, _c, _t);
    setFill( _x+1, _y, _c, _t);
    setFill( _x, _y-1, _c, _t);
    setFill( _x, _y+1, _c, _t);
  }
}
