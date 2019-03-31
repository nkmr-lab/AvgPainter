
class CharStroke {
  ArrayList<Stroke> m_Strokes;
  ArrayList<Stroke> m_undoStrokes;

  CharStroke() {
    m_Strokes = new ArrayList<Stroke>();
    m_undoStrokes = new ArrayList<Stroke>();
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
