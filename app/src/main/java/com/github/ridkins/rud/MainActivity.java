package com.github.ridkins.rud;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import twitter4j.TwitterException;
import twitter4j.User;


public class MainActivity extends AppCompatActivity {
static boolean showUserImage;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SharedPreferences isUserImage = PreferenceManager.getDefaultSharedPreferences(this);
        showUserImage = isUserImage.getBoolean("showUserImage", true);
        if (GlobalApplication.twitter == null) {
            finish();
            startActivity(new Intent(this, OAuthActivity.class));
            return;
        }

        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(0x00000000);
        }

        if (GlobalApplication.user == null) {
            Observable
                    .create(subscriber->{
                        try {
                            GlobalApplication.user=GlobalApplication.twitter.verifyCredentials();
                            subscriber.onNext(GlobalApplication.user);
                            subscriber.onCompleted();
                        } catch (TwitterException e) {
                            subscriber.onError(e);
                        }
                    })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result-> setDrawerHeader((User) result),
                            Throwable::printStackTrace,
                            ()->{}
                    );
        } else {
            setDrawerHeader(GlobalApplication.user);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item-> {
            int id = item.getItemId();

            if(!item.isChecked()){
                switch(id){
                    case R.id.nav_timeline:
                        replaceFragment(new TimeLineFragment());
                        break;
                                        case R.id.nav_settings:
                        startActivity(new Intent(this,SettingsActivity.class));
                        break;
                }
            }

            drawer.closeDrawer(GravityCompat.START);

            return (id != R.id.nav_settings);

        });



        TabLayout tabLayout=(TabLayout) findViewById(R.id.toolbar_tab);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> attachFragment(getMainFragment(), navigationView, tabLayout));

        if(savedInstanceState==null){
            Fragment top=new TimeLineFragment();
            addFragment(top);
            attachFragment(top, navigationView, tabLayout);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GlobalApplication.user=null;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        attachFragment(getMainFragment(), (NavigationView) findViewById(R.id.nav_view), (TabLayout) findViewById(R.id.toolbar_tab));
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



    private void addFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.mainLayout, fragment)
                .commit();
    }

    private void replaceFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out,android.R.anim.fade_in,android.R.anim.fade_out)
                .replace(R.id.mainLayout, fragment)
                .commit();
    }

    private Fragment getMainFragment(){
        return getSupportFragmentManager().findFragmentById(R.id.mainLayout);
    }

    private void attachFragment(Fragment fragment,NavigationView navigationView, TabLayout tabLayout){
        if (fragment != null) {
            if(fragment instanceof ToolbarTitleInterface){
                setTitle(((ToolbarTitleInterface)fragment).getTitleResourceId());
            }

            if(fragment instanceof NavigationPositionInterface){
                navigationView.setCheckedItem(((NavigationPositionInterface)fragment).getNavigationPosition());
            }

            if(fragment instanceof UseTabsInterface) {
                if(tabLayout.getVisibility()!=View.VISIBLE){
                    tabLayout.setVisibility(View.VISIBLE);
                }
                tabLayout.setupWithViewPager(((UseTabsInterface)fragment).getTabsViewPager());
            } else{
                if(tabLayout.getVisibility()!=View.GONE) {
                    tabLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    private void setDrawerHeader(User user){
        ((TextView)findViewById(R.id.user_name)).setText(user.getName());
        ((TextView)findViewById(R.id.user_id)).setText(TwitterStringUtil.plusAtMark(user.getScreenName()));
        ((TextView)findViewById(R.id.user_name)).setTextColor(Color.BLACK);
        ((TextView)findViewById(R.id.user_id)). setTextColor(Color.BLACK);
        ImageView userImage=(ImageView)findViewById(R.id.user_image);
        ImageView userBackgroundImage=(ImageView)findViewById(R.id.user_bg_image);



        RequestManager manager=Glide.with(this);

        manager.load(user.getBiggerProfileImageURL()).into(userImage);
        manager.load(user.getProfileBannerRetinaURL()).into(userBackgroundImage);
    }

}