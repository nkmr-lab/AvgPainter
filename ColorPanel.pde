
class ColorPanel {

  color [] colors;
  int posX, posY, sizeX, sizeY, marginX, marginY;
  int selectedIndex = 0;

  ColorPanel() {
    posX = 308;
    posY = 30;
    sizeX = 10;
    sizeY = 10;
    marginX = 5;
    marginY = 5;
    setColors();
  }

  void setColors() {
    colors = new color[0];
    pushStyle();
    colorMode(HSB, 12, 100, 100);
    for (int y=0; y<3; y++) {
      for (int x=0; x<4; x++) {
        // 最後のパレットだけ黒にする
        color addColor;
        if (x==3 && y ==2) {
          addColor = color( 0, 0, 0 );
        } else {
          addColor = color( x+y*3, 100, 100);
        }
        colors = (color[])append( colors, addColor );
      }
    }
    popStyle();
  }

  void display() {
    pushStyle();
    for (int y=0; y<3; y++) {
      for (int x=0; x<4; x++) {
        fill( colors[x + y * 3] );
        if ( x+y*3 == selectedIndex ) {
          stroke(255, 0, 0);
        } else {
          stroke( 0 );
        }
        rect(posX + x * (sizeX + marginX), posY + y * (sizeY + marginY), sizeX, sizeY);
      }
    }
    popStyle();
  }

  void listenSelect() {
    boolean isSelected = false;
    for (int y=0; y<3; y++) {
      for (int x=0; x<4; x++) {
        if ( mouseX >= posX + x * (sizeX + marginX) &&
          mouseX <= posX + x * (sizeX + marginX) + sizeX &&
          mouseY >= posY + y * (sizeY + marginY) &&
          mouseY <= posY + y * (sizeY + marginY) + sizeY )
        {
          isSelected |= true;
          selectedIndex = x+y*3;
        }
      }
    }

    if ( isSelected ) {
      println("クリックされたよ");
    }
  }

  void addColor() {
  }

  void removeColor() {
  }

  color getNowColor() {
    return colors[selectedIndex];
  }
}
