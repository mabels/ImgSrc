package com.adviser.imgsrc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;

import lombok.Data;
import lombok.val;

@Data
public class Image {
	private boolean redirect = false;
	private int width = 100;
	private int height = 100;
	private Color backcolor = new Color(0x444444);
	private Color textcolor = new Color(0xffffff);
	private String text = null;

	private Format format = null;

	private Color asRGBColor(String s) {
		int rgb = 0xCCCCC;
		final long val = Long.parseLong(s.toUpperCase(), 16);
		if (s.length() == 3) {
			rgb = (int)((((val >> 0) & 0xf) | (((val >> 0) & 0xf) << 4)) << 0
					| (((val >> 4) & 0xf) | (((val >> 4) & 0xf) << 4)) << 8
					| (((val >> 8) & 0xf) | (((val >> 8) & 0xf) << 4)) << 16);
		} else if (s.length() == 6) {
			rgb = (int)(((val >> 0) & 0xff) << 0 | ((val >> 8) & 0xff) << 8
					| ((val >> 16) & 0xff) << 16);
		}
		return new Color(rgb);
	}

	private Color asABGRColor(String s) {
		format.setColorSpace(BufferedImage.TYPE_4BYTE_ABGR);
		int rgb = 0xFFCCCCC;
		final long val = Long.parseLong(s.toUpperCase(), 16);
		if (s.length() == 4) {
			rgb = (int)((((val >> 0) & 0xf) | (((val >> 0) & 0xf) << 4)) << 24
					| (((val >> 4) & 0xf) | (((val >> 4) & 0xf) << 4)) << 0
					| (((val >> 8) & 0xf) | (((val >> 8) & 0xf) << 4)) << 8
					| (((val >> 12) & 0xf) | (((val >> 12) & 0xf) << 4)) << 16);
			//System.out.println("RGBA:" + Integer.toHexString(val) + ":"
			//		+ Integer.toHexString(rgb));
			return new Color(rgb, true);
		} else if (s.length() == 8) {
			rgb = (int)(((val >> 0) & 0xff) << 24 | ((val >> 8) & 0xff) << 0
					| ((val >> 16) & 0xff) << 8 | ((val >> 24) & 0xff) << 16);
			return new Color(rgb);
		}
		return new Color(rgb);
	}

	private static Pattern _4er = Pattern.compile("\\p{XDigit}{4}");
	private static Pattern _8er = Pattern.compile("\\p{XDigit}{8}");
	private static Pattern _3er = Pattern.compile("\\p{XDigit}{3}");
	private static Pattern _6er = Pattern.compile("\\p{XDigit}{6}");
	private static Random rand = new Random(System.currentTimeMillis());

	private Color asColor(String s) {
		final char first = s.charAt(0);
		int len = s.length();
		boolean random = false;
		if (len > 0 && (first == 'r' || first == 'R')) {
			this.setRedirect(true);
			s = s.substring(1);
			len = s.length();
			random = true;
		}
		Color ret = null;
		if ((len == 4 && _4er.matcher(s).matches())
				|| (len == 8 && _8er.matcher(s).matches())) {
			ret = asABGRColor(s);
		} else if ((len == 3 && _3er.matcher(s).matches())
				|| (len == 6 && _6er.matcher(s).matches())) {
			ret = asRGBColor(s);
		}
		if (random && ret != null) {
			int r = rand.nextInt() & 0xf;
			for (int i = 0; i < r; ++i) {
				if ((r & 0x8) != 0)
					ret = ret.brighter();
				else
					ret = ret.darker();
			}
		}
		return ret;
	}

	public String getPath() {
	  final StringBuffer sb = new StringBuffer();
	  sb.append("/");
	  sb.append(Integer.toString(this.getWidth()));
      sb.append("/");
	  sb.append(Integer.toString(this.getHeight()));
      sb.append("/");
	  sb.append(Integer.toHexString(this.getBackcolor().getRGB()));
      sb.append("/");
	  sb.append(Integer.toHexString(this.getTextcolor().getRGB()));
      sb.append("/");
	  sb.append(this.getText());
	  sb.append(this.getFormat().getSuffix());
	  return sb.toString();
	}
	private boolean frame = false;
	private static Pattern _frame = Pattern.compile("(.*)\\.[xX](.*)");
	private String hasFrame(String path) {
	  Matcher match = _frame.matcher(path);
	  if (match.matches()) {
	    frame = true;
	    return match.group(1) + match.group(2);
	  }
	  return path;
	}
	
	private static Pattern _split = Pattern.compile("/+");
	public static Image fromPath(String path) {
		final val img = new Image();
		/*
		 * /height/width/backcolor/textcolor/text<.format>
		 */
		img.setFormat(Format.fromPath(path));
		path = img.getFormat().getCleanPath().trim();
		path = img.hasFrame(path);
		String[] paths = _split.split(path, 0);
		int ofs = 0;
		if (paths.length > 0 && paths[0].isEmpty()) {
			ofs = 1;
			if (paths.length == 1 && paths[0].isEmpty()) {
				paths = new String[0];
			}
		}
		if (paths.length >= ofs + 1) {
			try {
				img.setWidth(Integer.parseInt(paths[ofs + 0]));
				img.setHeight(img.getWidth());
			} catch (Exception e) {
				img.setText(paths[ofs + 0]);
			}
		}
		if (paths.length >= ofs + 2) {
			try {
				img.setHeight(Integer.parseInt(paths[ofs + 1]));
			} catch (Exception e) {
				img.setText(paths[ofs + 1]);
			}
		}
		if (paths.length >= ofs + 3) {
			val color = img.asColor(paths[ofs + 2]);
			if (color != null)
				img.setBackcolor(color);
			else
				img.setText(paths[ofs + 2]);
		}
		if (paths.length >= ofs + 4) {
			val color = img.asColor(paths[ofs + 3]);
			if (color != null)
				img.setTextcolor(color);
			else
				img.setText(paths[ofs + 3]);
		}
		if (paths.length >= ofs + 5) {
			img.setText(paths[ofs + 4]);
		}

		return img;
	}

	public String getText() {
		if (text != null)
			return text;
		return "" + width + "x" + height;
	}

	public void drawCenteredString(String s, int w, int h, Graphics2D g) {

		int proz = 80;
		while (proz > 10) {
			final int fheight = (Math.min(h, w) * proz) / 100;
			if (fheight < 3)
				return;
			final Font font = new Font("Sans-Serif", Font.PLAIN, fheight);
			g.setFont(font);
			final FontMetrics fm = g.getFontMetrics();
			final int sw = fm.stringWidth(s);
			if (sw < w) {
				final int x = (w - sw) / 2;
				final int y = (fm.getAscent() + (h - (fm.getAscent() + fm
						.getDescent())) / 2);
				g.drawString(s, x, y);
				break;
			}
			proz -= 5;
		}
	}
	
	public ByteArrayOutputStream getStream() throws IOException {
	  return getFormat().getStream(drawImage());
	}

	public BufferedImage drawImage() {
		if (width > 2048 || height > 2048) {
			throw new RuntimeErrorException(new Error(
					"Image too big max 2048x2048:" + width + "x" + height));
		}
		final BufferedImage image = new BufferedImage(width, height,
				this.format.getColorSpace());
		final Graphics2D graph = image.createGraphics();
		if (this.isFrame()) {
      graph.setPaint(textcolor);
      graph.fillRect(0, 0, width, height);
      graph.setPaint(backcolor);
      graph.fillRect(5, 5, width-10, height-10);
      graph.setPaint(textcolor);
      graph.drawLine(0, 0, width, height);
      graph.drawLine(width, 0, 0, height);
		  
		} else {
	    graph.setPaint(backcolor);
		  graph.fillRect(0, 0, width, height);
		}
		graph.setColor(textcolor);
		drawCenteredString(this.getText(), width, height, graph);
		return image;
	}
}
