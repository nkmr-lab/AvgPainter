
class g_iStrokeWeightPanel {
  float posX, posY;
  int max, min;
  float size;
  boolean over;
  boolean locked;
  color col = color(0, 0, 0);

  g_iStrokeWeightPanel (float _px, float _py, int _mx, int _mi, float _s) {
    posX = _px;
    posY = _py;
    max = _mx;
    min = _mi;
    size = _s;
  }

  void update() {

    if (overEvent()) {
      over = true;
    } else {
      over = false;
    }

    locked =false;

    if (mousePressed && over) {
      locked = true;
    }

    if (locked) {
      if ( dragged2right() ) {
        size+=3;
      } else if ( dragged2left() ) {
        size-=3;
      }
    }

    size = constrain(size, min, max);
  }


  void setColor(color _col) {
    col = _col;
  }

  boolean overEvent() {
    if (dist(mouseX, mouseY, posX, posY) < max/2) {
      return true;
    } else {
      return false;
    }
  }

  boolean dragged2right() {
    if (mouseX > pmouseX) {
      return true;
    } else {
      return false;
    }
  }

  boolean dragged2left() {
    if (mouseX < pmouseX) {
      return true;
    } else {
      return false;
    }
  }

  void display() {
    noStroke();
    fill(col);
    ellipse(posX, posY, size, size);
  }
}
