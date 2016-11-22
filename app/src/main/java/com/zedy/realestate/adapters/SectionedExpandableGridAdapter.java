package com.zedy.realestate.adapters;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.zedy.realestate.BuildConfig;
import com.zedy.realestate.R;
import com.zedy.realestate.activities.HomeActivity;
import com.zedy.realestate.activities.LoginActivity;
import com.zedy.realestate.app.AppController;
import com.zedy.realestate.models.Item;
import com.zedy.realestate.store.RealEstatePrefStore;
import com.zedy.realestate.utils.Constants;
import com.zedy.realestate.utils.SweetDialogHelper;
import com.zedy.realestate.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import fr.quentinklein.slt.LocationTracker;

/**
 * Created by lenovo on 2/23/2016.
 */
public class SectionedExpandableGridAdapter extends RecyclerView.Adapter<SectionedExpandableGridAdapter.ViewHolder> {

    //data array
    private ArrayList<Object> mDataArrayList;

    //context
    private final Context mContext;

    //listeners
    private final ItemClickListener mItemClickListener;
    private final SectionStateChangeListener mSectionStateChangeListener;

    //view type
    private static final int VIEW_TYPE_SECTION = R.layout.layout_section;
    private static final int VIEW_TYPE_ITEM = R.layout.layout_item; //TODO : change this

    // for post done task
    private Location mLocation;

    public SectionedExpandableGridAdapter(Context context, ArrayList<Object> dataArrayList,
                                          final GridLayoutManager gridLayoutManager, ItemClickListener itemClickListener,
                                          SectionStateChangeListener sectionStateChangeListener) {
        mContext = context;
        mItemClickListener = itemClickListener;
        mSectionStateChangeListener = sectionStateChangeListener;
        mDataArrayList = dataArrayList;

        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return isSection(position) ? gridLayoutManager.getSpanCount() : 1;
            }
        });
    }

    private boolean isSection(int position) {
        return mDataArrayList.get(position) instanceof Section;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(viewType, parent, false), viewType);
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Typeface fontBold = Typeface.createFromAsset(mContext.getAssets(), "fonts/bold.ttf");
        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/normal.ttf");
        switch (holder.viewType) {
            case VIEW_TYPE_ITEM:
                final Item item = (Item) mDataArrayList.get(position);
                holder.itemTextView.setText(item.getName());
                holder.itemTextView.setTypeface(font);
                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mItemClickListener.itemClicked(item);
                    }
                });
                break;
            case VIEW_TYPE_SECTION:
                final Section section = (Section) mDataArrayList.get(position);
                holder.sectionTextView.setText(section.getName());
                holder.sectionTextView.setTypeface(fontBold);
                holder.sectionTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mItemClickListener.itemClicked(section);
                    }
                });
                holder.sectionToggleButton.setChecked(section.isExpanded);
                holder.sectionToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mSectionStateChangeListener.onSectionStateChanged(section, isChecked);
                    }
                });
                holder.sectionImageViewDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // done
                        doneTask(section.getId());

                    }
                });

                if (section.isTaskDone())
                    holder.sectionImageViewDone.setImageResource(R.drawable.ic_check_green_24dp);
                break;
        }
    }

    private void doneTask(final String taskId) {
        // open confirmation dialog
        new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(mContext.getString(R.string.confirm))
                .setContentText(mContext.getString(R.string.are_you_confirm))
                .setCancelText(mContext.getString(R.string.cancel))
                .setConfirmText(mContext.getString(R.string.ok))
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        // Check if has GPS
                        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            buildAlertMessageNoGps();
                        } else {
                            getLocation(mContext, taskId);
                        }
                    }
                })
                .show();


    }

    private void buildAlertMessageNoGps() {
        new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(mContext.getString(R.string.open_gps))
                .setContentText(mContext.getString(R.string.why_open_gps))
                .setConfirmText(mContext.getString(R.string.yes_open_gps))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        mContext.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return mDataArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isSection(position))
            return VIEW_TYPE_SECTION;
        else return VIEW_TYPE_ITEM;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        //common
        View view;
        int viewType;

        //for section
        TextView sectionTextView;
        ToggleButton sectionToggleButton;
        ImageView sectionImageViewDone;

        //for item
        TextView itemTextView;

        public ViewHolder(View view, int viewType) {
            super(view);
            this.viewType = viewType;
            this.view = view;
            if (viewType == VIEW_TYPE_ITEM) {
                itemTextView = (TextView) view.findViewById(R.id.text_item);
            } else {
                sectionTextView = (TextView) view.findViewById(R.id.text_section);
                sectionToggleButton = (ToggleButton) view.findViewById(R.id.toggle_button_section);
                sectionImageViewDone = (ImageView) view.findViewById(R.id.taskDone);
            }
        }
    }

    private void setTaskDone(final String taskId) {
        if (Utils.isOnline(mContext)) {
            final SweetDialogHelper sdh = new SweetDialogHelper((FragmentActivity) mContext);
            sdh.showMaterialProgress(mContext.getString(R.string.loading));
            String url = BuildConfig.API_BASE_URL + "task/done";
            StringRequest jsonObjReq = new StringRequest(Request.Method.POST,
                    url,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            sdh.dismissDialog();
                            // show success message
                            new SweetDialogHelper((FragmentActivity) mContext)
                                    .showSuccessfulMessage(mContext.getString(R.string.done)
                                    , mContext.getString(R.string.message_done));

                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    sdh.dismissDialog();

                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("token", Constants.token);
                    params.put("engId", new RealEstatePrefStore(mContext)
                            .getPreferenceValue(Constants.userId));
                    params.put("taskId", taskId);
                    if (mLocation != null)
                        params.put("coordinators", mLocation.getAltitude() + ", " + mLocation.getLongitude());

                    return params;
                }

            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq);
        } else {
            new SweetDialogHelper((FragmentActivity) mContext).showErrorMessage(mContext.getString(R.string.error),
                    mContext.getString(R.string.there_is_no_Inter_net));
        }
    }

    private void getLocation(Context ctx, final String taskId) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // You need to ask the user to enable the permissions
        } else {
            final SweetDialogHelper sdh = new SweetDialogHelper((FragmentActivity) ctx);
            sdh.showMaterialProgress(ctx.getString(R.string.loading));
            LocationTracker tracker = new LocationTracker(ctx) {
                @Override
                public void onLocationFound(Location location) {
                    // Do some stuff
                    mLocation = location;
                    sdh.dismissDialog();
                    setTaskDone(taskId);
                    stopListening();
                }

                @Override
                public void onTimeout() {
                    sdh.dismissDialog();

                    // open dialog try again
                    new SweetAlertDialog(mContext, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(mContext.getString(R.string.time_out))
                            .setContentText(mContext.getString(R.string.cant_get_location))
                            .setCancelText(mContext.getString(R.string.cancel))
                            .setConfirmText(mContext.getString(R.string.try_again))
                            .showCancelButton(true)
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.cancel();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    doneTask(taskId);
                                }
                            })
                            .show();


                }
            };
            tracker.startListening();
        }
    }
}
