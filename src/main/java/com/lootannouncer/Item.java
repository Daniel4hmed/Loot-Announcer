package com.lootannouncer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Item {

    private int ID;
    private int grandExchangePrice;
    private int quantity;
    private String name;

}
