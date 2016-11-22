package com.zedy.realestate.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akexorcist.localizationactivity.LocalizationActivity;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.zedy.realestate.BuildConfig;
import com.zedy.realestate.R;
import com.zedy.realestate.adapters.ItemClickListener;
import com.zedy.realestate.adapters.Section;
import com.zedy.realestate.adapters.SectionedExpandableLayoutHelper;
import com.zedy.realestate.app.AppController;
import com.zedy.realestate.jsonParser.Parse;
import com.zedy.realestate.models.Item;
import com.zedy.realestate.models.Task;
import com.zedy.realestate.store.RealEstatePrefStore;
import com.zedy.realestate.utils.Constants;
import com.zedy.realestate.utils.CustomTypefaceSpan;
import com.zedy.realestate.utils.SweetDialogHelper;
import com.zedy.realestate.utils.Utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends LocalizationActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ItemClickListener {

    private static final String TAG = "HomeActivity";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.noData)
    LinearLayout noDataView;

    private SectionedExpandableLayoutHelper sectionedExpandableLayoutHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        setToolbar();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        changeFontOfNavigation();

        initSwipeToRefresh();

        sectionedExpandableLayoutHelper = new SectionedExpandableLayoutHelper(this,
                mRecyclerView, this, 1);

        noDataView.setVisibility(View.VISIBLE);
    }

    private void initSwipeToRefresh() {
        //noinspection ResourceAsColor
        mSwipeRefresh.setColorScheme(
                R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);
        mSwipeRefresh.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        24,
                        getResources().getDisplayMetrics()));


        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                initiateRefresh();
            }
        });
        initiateRefresh();

    }

    //change font of drawer
    private void changeFontOfNavigation() {
        Menu m = navigationView.getMenu();
        for (int i = 0; i < m.size(); i++) {
            MenuItem mi = m.getItem(i);

            //for aapplying a font to subMenu ...
            SubMenu subMenu = mi.getSubMenu();
            if (subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }

            //the method we have create in activity
            applyFontToMenuItem(mi);
        }
    }

    private void applyFontToMenuItem(MenuItem mi) {
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/normal.ttf");
        SpannableString mNewTitle = new SpannableString(mi.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mi.setTitle(mNewTitle);
    }

    private void setToolbar() {
        setSupportActionBar(toolbar);
        /*
        * hide title
        * */
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //toolbar.setNavigationIcon(R.drawable.ic_toolbar);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/bold.ttf");
        TextView tv = (TextView) toolbar.findViewById(R.id.toolbar_title);
        tv.setTypeface(font);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.translation) {
            showSingleChoiceListLangaugeAlertDialog();
        } else if (id == R.id.nav_share) {
            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "ZEDY REAL ESTATE");
                String sAux = "\nLet me recommend you this application\n\n";
                sAux = sAux + "https://play.google.com/store/apps/details?id=com.zedy.realestate \n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "choose one"));
            } catch (Exception e) {
                //e.toString();
            }
        } else if (id == R.id.about_us) {
            startActivity(new Intent(this, AboutUsActivity.class));
            overridePendingTransition(R.anim.push_up_enter, R.anim.push_up_exit);
        } else if (id == R.id.log_out) {
            new RealEstatePrefStore(this).clearPreference();
            startActivity(new Intent(this, SplashActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            overridePendingTransition(R.anim.push_up_enter, R.anim.push_up_exit);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // change language
    private String mCheckedItem;

    public void showSingleChoiceListLangaugeAlertDialog() {
        final String[] list = new String[]{getString(R.string.language_arabic), getString(R.string.language_en)};
        int checkedItemIndex;

        switch (getLanguage()) {
            case "en":
                checkedItemIndex = 1;
                break;
            default:
                checkedItemIndex = 0;

        }
        mCheckedItem = list[checkedItemIndex];

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.language))
                .setSingleChoiceItems(list,
                        checkedItemIndex,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCheckedItem = list[which];
                                if (which == 0) {
                                    setLanguage("ar");
                                    changeFirstTimeOpenAppState(4);
                                    dialog.dismiss();
                                } else if (which == 1) {
                                    setLanguage("en");
                                    changeFirstTimeOpenAppState(5);
                                    dialog.dismiss();
                                }
                            }
                        })
                .show();
    }

    private void changeFirstTimeOpenAppState(int language) {
        new RealEstatePrefStore(this).addPreference(Constants.PREFERENCE_FIRST_TIME_OPEN_APP_STATE, 1);
        new RealEstatePrefStore(this).addPreference(Constants.PREFERENCE_LANGUAGE, language);
    }

    @Override
    public void itemClicked(Item item) {
        Log.d(TAG, item.getName());
    }

    @Override
    public void itemClicked(Section section) {
        Log.d(TAG, section.getName());
    }

    private void initiateRefresh() {
        if (mSwipeRefresh != null && !mSwipeRefresh.isRefreshing())
            mSwipeRefresh.setRefreshing(true);
        getTasks();
    }

    private void onRefreshComplete() {
        if (mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
    }

    private void getTasks() {
        if (Utils.isOnline(this)) {
            String url = BuildConfig.API_BASE_URL + "tasks";
            StringRequest jsonObjReq = new StringRequest(Request.Method.POST,
                    url,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, response.toString());
                            response = StringEscapeUtils.unescapeJava(response);
                            List<Task> taskList = Parse.parseTasks(response);
                            if (taskList.size() > 0){
                                sectionedExpandableLayoutHelper.removeAllSection();
                                noDataView.setVisibility(View.GONE);
                                for (Task task: taskList) {
                                    createSection(task);
                                }
                            }else {
                                noDataView.setVisibility(View.VISIBLE);
                            }
                            onRefreshComplete();


                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    onRefreshComplete();
                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("token", Constants.token);
                    params.put("engId", new RealEstatePrefStore(HomeActivity.this)
                            .getPreferenceValue(Constants.userId));

                    return params;
                }

            };

            // disable cache
            jsonObjReq.setShouldCache(false);
            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(jsonObjReq);
        } else {
            onRefreshComplete();
            new SweetDialogHelper(this).showErrorMessage(getString(R.string.error),
                    getString(R.string.there_is_no_Inter_net));
        }

    }

    private void createSection(Task task){

        String[] tasks = task.getDescribtion().split("،");
        ArrayList<Item> arrayList = new ArrayList<>();
        for (int i = 0; i < tasks.length; i++) {
            arrayList.add(new Item(tasks[i], i));
        }
        sectionedExpandableLayoutHelper.addSection(task.getId(),
                task.getTitle() + " - " + task.getProject(),
                task.getDone().equalsIgnoreCase("1"), arrayList);
        sectionedExpandableLayoutHelper.notifyDataSetChanged();


    }
}
