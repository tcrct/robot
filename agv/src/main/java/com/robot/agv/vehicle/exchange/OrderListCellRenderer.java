/**
 * Copyright (c) Fraunhofer IML
 */
package com.robot.agv.vehicle.exchange;

import com.robot.agv.vehicle.telegrams.OrderAction;
import com.robot.agv.vehicle.telegrams.OrderRequest;

import java.awt.Component;
import javax.swing.*;

/**
 * Renders order telegrams when displayed in a list.
 *
 * @author Martin Grzenia (Fraunhofer IML)
 */
public class OrderListCellRenderer
    extends JLabel
    implements ListCellRenderer<OrderRequest> {

  /**
   * A prototype for the list to compute its preferred size.
   */
  public static final OrderRequest PROTOTYPE_TELEGRAM
      = new OrderRequest(0, 0, 0, OrderAction.NONE);

  @Override
  public Component getListCellRendererComponent(JList<? extends OrderRequest> list,
                                                OrderRequest value,
                                                int index,
                                                boolean isSelected,
                                                boolean cellHasFocus) {
    StringBuilder sb = new StringBuilder();
    sb.append('#');
    sb.append(value.getId());
    sb.append(": ");
    sb.append(value.getDestinationId());
    sb.append(' ');
    sb.append(value.getDestinationAction());
    sb.append("...");
    setText(sb.toString());
    return this;
  }

}
