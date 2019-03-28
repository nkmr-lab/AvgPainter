
class Button {
  int m_iX;
  int m_iY;
  int m_iWidth;
  int m_iHeight;
  String m_strName;
  boolean m_bIsPressed;
  int m_iRed = 0;
  int m_iBlue = 255;
  int m_iGreen = 0;
  int m_iCommand = 0;

  Button( int _x, int _y, int _w, int _h, String _name, int _iCommand ) {
    m_iX = _x;
    m_iY = _y;
    m_iWidth = _w;
    m_iHeight = _h;
    m_strName = _name;
    m_iCommand = _iCommand;
  }
  
  Button( int _x, int _y, String _name ){
    m_iX = _x;
    m_iY = _y;
    m_iWidth = 50;
    m_iHeight = 50;
    m_strName = _name;
  }

  void SetColor( int _r, int _g, int _b ) {
    m_iRed = _r;
    m_iGreen = _g;
    m_iBlue = _b;
  }

  void Show() {
    fill(m_iRed, m_iGreen, m_iBlue);
    rect( m_iX, m_iY, m_iWidth, m_iHeight );
    fill(255);
    text( m_strName, m_iX+m_iWidth/2, m_iY+m_iHeight/2 -2 );
  }

  boolean IsInside( int _x, int _y ){
    if( _x >= m_iX && _x <= m_iX + m_iWidth && _y >= m_iY && _y <= m_iY + m_iHeight ){
      return true;
    }
    return false;
  } 
}
