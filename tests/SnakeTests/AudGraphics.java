public class AudGraphics {

	AudColor color;
	AudColor bgColor;
	
    public AudGraphics() {
    }

    public void draw3DRect(int x, int y, int width, int height,
                           boolean raised) {
    }

    public void fill3DRect(int x, int y, int width, int height,
                           boolean raised) {
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
    }

    public void fillRect(int x, int y, int width, int height) {
    }

    public void drawRect(int x, int y, int width, int height) {
    }

    public void clearRect(int x, int y, int width, int height) {
    }

    public void drawRoundRect(int x, int y, int width, int height,
                                       int arcWidth, int arcHeight) {
    }

    public void fillRoundRect(int x, int y, int width, int height,
                                       int arcWidth, int arcHeight) {

    }

    public void drawOval(int x, int y, int width, int height) {

    }

    public void fillOval(int x, int y, int width, int height) {

    }

    public void drawArc(int x, int y, int width, int height,
                                 int startAngle, int arcAngle) {

    }

    public void fillArc(int x, int y, int width, int height,
                                 int startAngle, int arcAngle) {

    }

    public void drawPolyline(int xPoints[], int yPoints[],
                                      int nPoints) {

    }

    public void drawPolygon(int xPoints[], int yPoints[],
                                     int nPoints) {

    }

    public void fillPolygon(int xPoints[], int yPoints[],
                                     int nPoints) {

    }

    public void drawString(String str, int x, int y) {

    }

    public AudColor getColor() {
    	return this.color;
    }

    public void setColor(AudColor color) {
    	this.color = color;
    }
    
    public void setBackground(AudColor color) {
    	this.bgColor = color;
    }

    public AudColor getBackground() {
    	return this.bgColor;
    }
}
