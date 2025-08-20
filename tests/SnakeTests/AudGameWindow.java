public abstract class AudGameWindow {
	private long startTime = -1;
	
	private boolean showFps = false;
	
	
	public static boolean hasStarted = false;
	
	public String title;
	
	public AudGameWindow() {
	}
	
	public void start() {
		startTime = System.currentTimeMillis();
		hasStarted = true;
	}
	

	public void stop() {
	}

	public int getGameAreaWidth() {
		return 800;
	}
	
	public int getGameAreaHeight() {
		return 600;
	}

	public void setGameAreaWidth(int value) {
	}

	public void setGameAreaHeight(int value) {
	}
	
	public void setTitle(String s) {
		title = s;
	}
	
	/**
	 * Enable/disable fps display.
	 */
	public void setShowFps(boolean showFps) {
		this.showFps = showFps;
	}
	
	/**
	 * Check if the number of frames rendered per second is displayed.
	 */
	public boolean getShowFps() {
		return showFps;
	}
	
	/**
	 * Returns the time at which the game was started.
	 * 
	 * @return
	 *   the system time in milliseconds at which the start() method was called
	 */
	public long getStartTime() {
		if (startTime == -1l) {
			throw new IllegalStateException("The game has not been started yet!");
		}
		return startTime;
	}
	
	/**
	 * Shows a Swing dialog with the given text.
	 * @param text The text to be displayed in the dialog window.
	 */
	protected void showDialog(String text) {
		
	}

	
	/**
	 * Update the game, move objects etc.
	 * This is called by the game timer once every timerInterval milliseconds
	 * (this value can changed in the constructor and defaults to 32ms).
	 *   
	 * @param time
	 *   The current game time in milliseconds.
	 */
	public abstract void updateGame(long time);
	
	/**
	 * Paint the game.
	 * This is called automatically when the game needs to be painted.
	 * 
	 * @param g
	 *   A Graphics object (actually Graphics2D) that can be used for
	 *   painting the game.
	 */
	public abstract void paintGame(AudGraphics g);
	
	/**
	 * Handle keyboard input.
	 * 
	 * @param keyCode
	 *   The pressed key. The value is the one of the key codes
	 *   defined in java.awt.event.KeyEvent (e.g. KeyEvent.VK_SPACE).
	 */
	public abstract void handleInput(int keyCode);
	
	
	/**
	 * Most important KeyEvents. We can't use java.awt.KeyEvent as the EST does not allow java.awt :(
	 */
	public static class KeyEvent {

	    /**
	     * Constant for the non-numpad <b>left</b> arrow key.
	     * @see #VK_KP_LEFT
	     */
	    public static final int VK_LEFT           = 0x25;

	    /**
	     * Constant for the non-numpad <b>up</b> arrow key.
	     * @see #VK_KP_UP
	     */
	    public static final int VK_UP             = 0x26;

	    /**
	     * Constant for the non-numpad <b>right</b> arrow key.
	     * @see #VK_KP_RIGHT
	     */
	    public static final int VK_RIGHT          = 0x27;

	    /**
	     * Constant for the non-numpad <b>down</b> arrow key.
	     * @see #VK_KP_DOWN
	     */
	    public static final int VK_DOWN           = 0x28;
	    
	    /**
	     * This value is used to indicate that the keyCode is unknown.
	     * KEY_TYPED events do not have a keyCode value; this value
	     * is used instead.
	     */
	    public static final int VK_UNDEFINED      = 0x0;
	}
}
