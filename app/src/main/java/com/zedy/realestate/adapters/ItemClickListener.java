package com.zedy.realestate.adapters;


import com.zedy.realestate.models.Item;

/**
 * Created by lenovo on 2/23/2016.
 */
public interface ItemClickListener {
    void itemClicked(Item item);
    void itemClicked(Section section);
}
