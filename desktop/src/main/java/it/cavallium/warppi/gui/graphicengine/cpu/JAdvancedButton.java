//
// Decompiled by Procyon v0.5.30
//

package it.cavallium.warppi.gui.graphicengine.cpu;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JButton;

public class JAdvancedButton extends JButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8445811316606975284L;
	public Image backgroundImage;
	public Dimension backgroundSize;
	public boolean drawColor = false;
	public boolean drawDefaultComponent = false;
	public int state;

	public JAdvancedButton() {
		this.setOpaque(false);
		this.backgroundImage = null;
		this.backgroundSize = new Dimension(0, 0);
	}

	public JAdvancedButton(final BufferedImage backgroundImage) throws IOException {
		this.setOpaque(false);
		this.backgroundImage = backgroundImage;
		if (backgroundImage != null) {
			this.backgroundSize = new Dimension(backgroundImage.getWidth(), backgroundImage.getHeight());
		}
		this.setMinimumSize(this.backgroundSize);
		this.setMaximumSize(this.backgroundSize);
		this.setPreferredSize(this.backgroundSize);
		this.setSize(this.backgroundSize);
	}

	public JAdvancedButton(final BufferedImage backgroundImage, final Dimension backgroundSize) throws IOException {
		this.setOpaque(false);
		this.backgroundImage = backgroundImage;
		this.backgroundSize = backgroundSize;
		this.setMinimumSize(this.backgroundSize);
		this.setMaximumSize(this.backgroundSize);
		this.setPreferredSize(this.backgroundSize);
		this.setSize(this.backgroundSize);
	}

	public void paintComponent(final Graphics g) {
		if (drawColor) {
			final Graphics2D g2d = (Graphics2D) g.create();
			final AlphaComposite acomp = AlphaComposite.getInstance(3, 1.0f);
			g2d.setComposite(acomp);
			g2d.setColor(getBackground());
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
			g2d.dispose();
		}
		if (this.backgroundImage != null) {
			final Graphics2D g2d = (Graphics2D) g.create();
			final AlphaComposite acomp = AlphaComposite.getInstance(3, 1.0f);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			g2d.setComposite(acomp);
			g2d.drawImage(this.backgroundImage, 0, (int) this.backgroundSize.getHeight() * -state, (int) this.backgroundSize.getWidth(), (int) (this.backgroundSize.getHeight() * 3), null);
			g2d.setFont(g.getFont());
			g2d.setColor(super.getForeground());
			g2d.drawString(this.getText(), super.getWidth() / 2 - g.getFontMetrics().stringWidth(this.getText()) / 2, super.getHeight() / 2 + g.getFontMetrics().getHeight() / 4);
			g2d.dispose();
		}
		if (drawDefaultComponent) {
			super.paintComponent(g);
		}
		super.setBorderPainted(drawDefaultComponent);
	}
	
	@Override
	public boolean isEnabled() {
		if (canclick == false) {
			return false;
		}
		return super.isEnabled();
	}

	@Override
    public void fireActionPerformed(ActionEvent paramActionEvent)
    {
		if (this.getCanClick()) {
			super.fireActionPerformed(paramActionEvent);
		}
    }
	
	public void setCanClick(boolean can) {
		canclick = can;
	}
	
	private boolean canclick = true;
	
	public boolean getCanClick() {
		return canclick;
	}
}
