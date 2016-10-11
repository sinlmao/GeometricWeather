package wangdaye.com.geometricweather.view.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.Location;
import wangdaye.com.geometricweather.model.database.helper.DatabaseHelper;
import wangdaye.com.geometricweather.model.item.LocationItem;
import wangdaye.com.geometricweather.view.adapter.LocationAdapter;

/**
 * Manage dialog.
 * */

public class ManageDialog extends DialogFragment
        implements LocationAdapter.MyItemClickListener, TextView.OnEditorActionListener {
    // widget
    private EditText editText;
    private OnLocationChangedListener listener;

    // data
    private LocationAdapter adapter;

    /** <br> life cycle. */

    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_manage, null, false);
        this.initData();
        this.initWidget(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        return builder.create();
    }

    /** <br> data. */

    private void initData() {
        List<Location> locationList = DatabaseHelper.getInstance(getActivity()).readLocation();
        List<LocationItem> itemList = new ArrayList<>();
        for (Location l : locationList) {
            itemList.add(new LocationItem(l.location));
        }
        this.adapter = new LocationAdapter(itemList);
        adapter.setOnItemClickListener(this);
    }

    /** <br> UI. */

    private void initWidget(View view) {
        this.editText = (EditText) view.findViewById(R.id.fragment_manage_search_text);
        editText.setOnEditorActionListener(this);

        RecyclerView locationView = (RecyclerView) view.findViewById(R.id.fragment_manage_recycleView);
        locationView.setLayoutManager(new LinearLayoutManager(getActivity()));
        locationView.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new LocationSwipeCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT));
        itemTouchHelper.attachToRecyclerView(locationView);
    }

    /** <br> interface. */

    // on select location listener.

    public interface OnLocationChangedListener {
        void selectLocation(String result);
        void changeLocationList(List<String> nameList);
    }

    public void setOnLocationChangedListener(OnLocationChangedListener l) {
        this.listener = l;
    }

    // on location item click.

    @Override
    public void onItemClick(View view, int position) {
        if (listener != null) {
            listener.selectLocation(adapter.itemList.get(position).locationName);
        }
        dismiss();
    }

    // on editor action listener.

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            String location = v.getText().toString();
            editText.setText("");

            if (location.length() > 0 && listener != null) {
                listener.selectLocation(location);
                dismiss();
            }
            return true;
        }
        return false;
    }

    /** <br> callback. */

    public class LocationSwipeCallback extends ItemTouchHelper.SimpleCallback {

        LocationSwipeCallback(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView,
                              RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            adapter.moveData(fromPosition, toPosition);
            if (listener != null) {
                List<String> nameList = new ArrayList<>();
                for (LocationItem i : adapter.itemList) {
                    nameList.add(i.locationName);
                }
                listener.changeLocationList(nameList);
            }

            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();

            if (adapter.itemList.size() <= 1) {
                LocationItem item = adapter.itemList.get(position);
                adapter.removeData(position);
                adapter.insertData(item, position);
                Toast.makeText(getActivity(),
                        getString(R.string.feedback_location_list_cannot_be_null),
                        Toast.LENGTH_SHORT).show();
            } else {
                adapter.removeData(position);
                Toast.makeText(getActivity(),
                        getString(R.string.feedback_delete_succeed),
                        Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    List<String> nameList = new ArrayList<>();
                    for (LocationItem i : adapter.itemList) {
                        nameList.add(i.locationName);
                    }
                    listener.changeLocationList(nameList);
                }
            }
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            switch (actionState) {
                case ItemTouchHelper.ACTION_STATE_SWIPE:
                    final float alpha = 1 - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);
                    break;
            }
        }
    }
}
