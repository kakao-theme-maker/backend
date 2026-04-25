package com.komentum.theme.theme.repository.order;

import com.komentum.theme.theme.domain.QThemeComponent;
import com.komentum.theme.theme.enums.ThemeSortType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberExpression;
import java.util.ArrayList;
import java.util.List;

public class ThemeOrder {

  public static OrderSpecifier<?>[] toOrders(List<ThemeSortType> sortTypes,
      QThemeComponent themeComponent, NumberExpression<Long> preferCount) {
    List<OrderSpecifier<?>> orders = new ArrayList<>();
    for (ThemeSortType sortType : sortTypes) {
      orders.add(switch (sortType) {
        case PREFER_DESC -> {
          if (preferCount == null) {
            throw new IllegalArgumentException("preferCount is null");
          }
          yield preferCount.desc();
        }
        case CREATED_DESC -> themeComponent.createdAt.desc();
      });
    }
    return orders.toArray(new OrderSpecifier<?>[0]);
  }
}
