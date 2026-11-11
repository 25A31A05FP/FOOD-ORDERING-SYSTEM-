package com.foodordering.view;

import com.foodordering.model.FoodItem;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import javax.swing.JPanel;

public class FoodIllustration extends JPanel {
    private final FoodItem food;
    private static final Color[] BACKGROUNDS = {
        new Color(238, 224, 204), new Color(218, 234, 213), new Color(244, 222, 209),
        new Color(225, 228, 213), new Color(241, 231, 205), new Color(230, 219, 231)
    };
    private static final Color[] INGREDIENTS = {
        new Color(203, 108, 57), new Color(79, 132, 83), new Color(237, 180, 77),
        new Color(174, 76, 67), new Color(235, 222, 190), new Color(113, 81, 60)
    };

    public FoodIllustration(FoodItem food) {
        this.food = food;
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(220, 150));
        setMinimumSize(new java.awt.Dimension(150, 110));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = getWidth();
        int height = getHeight();
        Color background = BACKGROUNDS[Math.floorMod(food.category().hashCode(), BACKGROUNDS.length)];
        g.setPaint(new GradientPaint(0, 0, background.brighter(), width, height, background));
        g.fillRoundRect(0, 0, width, height, 20, 20);
        int diameter = Math.min(width, height) * 58 / 100;
        int plateX = (width - diameter) / 2;
        int plateY = (height - diameter) / 2;
        g.setColor(new Color(255, 255, 255, 225));
        g.fill(new Ellipse2D.Double(plateX, plateY, diameter, diameter));
        g.setColor(new Color(224, 225, 216));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(new Ellipse2D.Double(plateX + 7, plateY + 7, diameter - 14, diameter - 14));

        int seed = Math.floorMod(food.name().hashCode(), INGREDIENTS.length);
        for (int i = 0; i < 5; i++) {
            int offsetX = (i % 3 - 1) * diameter / 5;
            int offsetY = (i / 3 == 0 ? -1 : 1) * (i % 2 == 0 ? diameter / 5 : 0);
            int size = Math.max(10, diameter / 5);
            g.setColor(INGREDIENTS[(seed + i) % INGREDIENTS.length]);
            g.fill(new Ellipse2D.Double(
                    width / 2.0 + offsetX - size / 2.0,
                    height / 2.0 + offsetY - size / 2.0,
                    size, size));
        }
        g.dispose();
    }
}
