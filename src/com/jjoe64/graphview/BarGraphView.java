package com.jjoe64.graphview;

import android.content.Context;
import android.graphics.Canvas;

/**
 * Draws a Bar Chart
 * @author Muhammad Shahab Hameed
 */
public class BarGraphView extends GraphView {
  public BarGraphView(Context context, String title) {
    super(context, title);
  }

  @Override
    public double drawSeries(Canvas canvas, GraphViewData[] values, float graphwidth, float graphheight,
        float top_border, float bottom_border, double minX, double minY, double diffX, double diffY,
        float horstart) {
      float colwidth = (graphwidth - (2 * bottom_border)) / values.length;
      double size = 0;

      // draw data
      for (int i = 0; i < values.length; i++) {
        float valY = (float) (values[i].valueY - minY);
        float ratY = (float) (valY / diffY);
        float y = graphheight * ratY;
        size += y;
        canvas.drawRect((i * colwidth) + horstart, (bottom_border - y) + graphheight, ((i * colwidth) + horstart) + (colwidth - 1), graphheight + bottom_border - 1, paint);
      }
      return size;
    }
}
