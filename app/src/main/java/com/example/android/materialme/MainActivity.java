/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.materialme;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/***
 * Main Activity for the Material Me app, a mock sports news application with poor design choices
 */
public class MainActivity extends AppCompatActivity {

    //Member variables
    private RecyclerView mRecyclerView;
    private ArrayList<Sport> mSportsData;
    private SportsAdapter mAdapter;
    ArrayList<Integer> deletedItems = new ArrayList<>();
    ArrayList<Integer> movedFromItems = new ArrayList<>();
    ArrayList<Integer> movedToItems = new ArrayList<>();
    static final String STATE_DELETED_ITEMS = "deleted items";
    static final String STATE_MOVED_FROM_ITEMS = "moved from items";
    static final String STATE_MOVED_TO_ITEMS = "moved to items";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        int gridColumnCount = getResources().getInteger(R.integer.grid_column_count);

        //Initialize the RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        //Set the Layout Manager
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridColumnCount));

        //Initialize the ArrayLIst that will contain the data
        mSportsData = new ArrayList<>();

        //Initialize the adapter and set it ot the RecyclerView
        mAdapter = new SportsAdapter(this, mSportsData);
        mRecyclerView.setAdapter(mAdapter);

        //Get the data
        initializeData();

        if (savedInstanceState != null) {
            deletedItems = savedInstanceState.getIntegerArrayList(STATE_DELETED_ITEMS);
            movedFromItems = savedInstanceState.getIntegerArrayList(STATE_MOVED_FROM_ITEMS);
            movedToItems = savedInstanceState.getIntegerArrayList(STATE_MOVED_TO_ITEMS);
            if (deletedItems != null) {
                for (int deletedItem : deletedItems) {
                    mSportsData.remove(deletedItem);
                    mAdapter.notifyItemRemoved(deletedItem);
                }
            }

            if (movedFromItems != null && movedToItems != null) {
                for (int i = 0; i < movedFromItems.size(); i++) {
                    Collections.swap(mSportsData, movedFromItems.get(i), movedToItems.get(i));
                    mAdapter.notifyItemMoved(movedFromItems.get(i), movedToItems.get(i));
                }
            }
        }


        int swipeDirs;
        if (gridColumnCount > 1)
            swipeDirs = 0;
        else
            swipeDirs = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper
                .SimpleCallback(ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT |
                ItemTouchHelper.DOWN | ItemTouchHelper.UP,
                swipeDirs) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                movedFromItems.add(viewHolder.getAdapterPosition());
                movedToItems.add(target.getAdapterPosition());
                int movedItemFromPosition = movedFromItems.get(movedFromItems.indexOf(viewHolder.getAdapterPosition()));
                int movedItemToPosition = movedToItems.get(movedToItems.indexOf(target.getAdapterPosition()));
                Collections.swap(mSportsData, movedItemFromPosition, movedItemToPosition);
                mAdapter.notifyItemMoved(movedItemFromPosition, movedItemToPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                deletedItems.add(viewHolder.getAdapterPosition());
                int swipedItemPosition = deletedItems.get(deletedItems.indexOf(viewHolder.getAdapterPosition()));
                mSportsData.remove(swipedItemPosition);
                mAdapter.notifyItemRemoved(deletedItems.get(deletedItems.indexOf(viewHolder.getAdapterPosition())));
            }
        });
        helper.attachToRecyclerView(mRecyclerView);
    }

    /**
     * Method for initializing the sports data from resources.
     */
    private void initializeData() {
        //Get the resources from the XML file
        String[] sportsList = getResources().getStringArray(R.array.sports_titles);
        String[] sportsInfo = getResources().getStringArray(R.array.sports_info);
        TypedArray sportsImageResources = getResources().obtainTypedArray(R.array.sports_images);

        //Clear the existing data (to avoid duplication)
        mSportsData.clear();


        //Create the ArrayList of Sports objects with the titles and information about each sport
        for (int i = 0; i < sportsList.length; i++) {
            mSportsData.add(new Sport(sportsList[i], sportsInfo[i], sportsImageResources.getResourceId(i, 0)));
        }
        sportsImageResources.recycle();

        //Notify the adapter of the change
        mAdapter.notifyDataSetChanged();
    }

    public void resetSports(View view) {
        initializeData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putIntegerArrayList(STATE_DELETED_ITEMS, deletedItems);
        outState.putIntegerArrayList(STATE_MOVED_FROM_ITEMS, movedFromItems);
        outState.putIntegerArrayList(STATE_MOVED_TO_ITEMS, movedToItems);
        super.onSaveInstanceState(outState);
    }
}
