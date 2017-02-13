package com.github.ridkins.rud;

import android.content.Context;
import android.content.SharedPreferences;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

public class TimeLineFragment extends BaseTweetListFragment implements ToolbarTitleInterface,NavigationPositionInterface {

    @Override
    public ResponseList<Status> getResponseList(Paging paging) throws TwitterException {
        return GlobalApplication.twitter.getHomeTimeline(paging);
    }

    @Override
    public int getTitleResourceId() {
        return R.string.timeline;
    }

    @Override
    public int getNavigationPosition() {
        return R.id.nav_timeline;
    }

}
