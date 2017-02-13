package com.github.ridkins.rud;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.util.TimeSpanConverter;


class StatusesAdapter extends BaseListAdapter<Status,StatusesAdapter.ViewHolder> {


    StatusesAdapter(Context context, ArrayList<Status> data) {
        super(context,data);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        Status status=data.get(position);
        Status item = status.isRetweet()?status.getRetweetedStatus():status;
        AppConfiguration conf=GlobalApplication.configuration;
        if((conf.isPatternTweetMuteEnabled() && item.getText().matches(conf.getTweetMutePattern())) ||
                (conf.isPatternUserScreenNameMuteEnabled() && item.getUser().getScreenName().matches(conf.getUserScreenNameMutePattern())) ||
                (conf.isPatternUserNameMuteEnabled() && item.getUser().getName().matches(conf.getUserNameMutePattern())) ||
                (conf.isPatternTweetSourceMuteEnabled() && item.getSource().matches(conf.getTweetSourceMutePattern()))
                ){
            return 1;
        }
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i==1){

            ViewHolder vh=new ViewHolder(inflater.inflate(R.layout.layout_tweet, viewGroup, false));
            vh.tweetContext.setTextColor(context.getResources().getColor(R.color.imageToggleDisable));
            vh.tweetContext.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            return vh;
        }
        return new ViewHolder(inflater.inflate(R.layout.layout_tweet, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        Status status=data.get(i);

        final Status item = status.isRetweet()?status.getRetweetedStatus():status;



        String profileImageUrl;
        if (GlobalApplication.configuration.getTimelineImageLoadMode()<=AppConfiguration.IMAGE_LOAD_MODE_LOW){
            profileImageUrl=item.getUser().getProfileImageURLHttps();
        } else {
            profileImageUrl=item.getUser().getBiggerProfileImageURLHttps();
        }



        if(MainActivity.showUserImage) {
            imageRequestManager.load(profileImageUrl).into(viewHolder.tweetUserImage);
        }else{
            viewHolder.tweetUserImage.setVisibility(View.GONE);

        }


        viewHolder.tweetUserName.setText(item.getUser().getName());
        viewHolder.tweetUserId.setText(TwitterStringUtil.plusAtMark(item.getUser().getScreenName()));
        viewHolder.tweetContext.setText(TwitterStringUtil.getLinkedSequence(item,context));
        viewHolder.tweetContext.setMovementMethod(LinkMovementMethod.getInstance());
        viewHolder.tweetContext.setFocusable(false);

        viewHolder.tweetTimeStampText.setText(viewHolder.timeSpanConverter.toTimeSpanString(item.getCreatedAt().getTime()));
        viewHolder.tweetUserImage.setOnClickListener(v->{
            ViewCompat.setTransitionName(viewHolder.tweetUserImage,"tweet_user_image");

        });
        viewHolder.itemView.setOnClickListener(v -> {
            ViewCompat.setTransitionName(viewHolder.tweetUserImage,"tweet_user_image");
            context.startActivity(
                    new Intent(context,ShowTweetActivity.class).putExtra("status",item),
                    ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, viewHolder.tweetUserImage,"tweet_user_image").toBundle()
            );
        });

        Status quotedStatus=item.getQuotedStatus();
        if(quotedStatus!=null){
            if (viewHolder.tweetQuoteTweetLayout.getVisibility() != View.VISIBLE) {
                viewHolder.tweetQuoteTweetLayout.setVisibility(View.VISIBLE);
            }
            viewHolder.tweetQuoteTweetLayout.setOnClickListener(v -> context.startActivity(
                    new Intent(context,ShowTweetActivity.class).putExtra("statusId",(Long)quotedStatus.getId()))
            );
            viewHolder.tweetQuoteTweetUserName.setText(quotedStatus.getUser().getName());
            viewHolder.tweetQuoteTweetUserId.setText(TwitterStringUtil.plusAtMark(quotedStatus.getUser().getScreenName()));
            viewHolder.tweetQuoteTweetContext.setText(quotedStatus.getText());
        }else{
            if (viewHolder.tweetQuoteTweetLayout.getVisibility() != View.GONE) {
                viewHolder.tweetQuoteTweetLayout.setVisibility(View.GONE);
            }
        }

        MediaEntity mediaEntities[]=item.getMediaEntities();

        if (mediaEntities.length!=0){
            viewHolder.tweetImageTableView.setVisibility(View.VISIBLE);
            viewHolder.tweetImageTableView.setImageNumber(mediaEntities.length);
            for (int ii = 0; ii < mediaEntities.length; ii++) {
                ImageView imageView=viewHolder.tweetImageTableView.getImageView(ii);


                int loadMode=GlobalApplication.configuration.getTimelineImageLoadMode();

                if (loadMode!=AppConfiguration.IMAGE_LOAD_MODE_NONE){
                    String mediaUrlPrefix;
                    switch (loadMode){
                        case AppConfiguration.IMAGE_LOAD_MODE_LOW:
                            mediaUrlPrefix="small";
                            break;
                        case AppConfiguration.IMAGE_LOAD_MODE_FULL:
                            mediaUrlPrefix="large";
                            break;
                        default:
                            mediaUrlPrefix="medium";
                    }
                    imageRequestManager
                            .load(mediaEntities[ii].getMediaURLHttps()+":"+mediaUrlPrefix)
                            .centerCrop()
                            .into(imageView);
                } else {
                    imageRequestManager
                            .load(R.drawable.border_frame)
                            .into(imageView);
                }
            }
        }
        else{
            viewHolder.tweetImageTableView.setVisibility(View.GONE);
        }


    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        for (int i=0;i<4;i++){
            Glide.clear(holder.tweetImageTableView.getImageView(i));
        }
    }

    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView tweetUserImage;
        TextView tweetRetweetUserName;
        TextView tweetUserName;
        TextView tweetUserId;
        TextView tweetContext;
        TextView tweetTimeStampText;
        RelativeLayout tweetQuoteTweetLayout;
        TextView tweetQuoteTweetUserName;
        TextView tweetQuoteTweetUserId;
        TextView tweetQuoteTweetContext;
        TweetImageTableView tweetImageTableView;


        TimeSpanConverter timeSpanConverter;

        ViewHolder(final View itemView) {
            super(itemView);
            tweetUserImage=(ImageView) itemView.findViewById(R.id.TLimage);
            tweetRetweetUserName=(TextView) itemView.findViewById(R.id.tweet_retweet_user_name);
            tweetUserId=(TextView) itemView.findViewById(R.id.tweet_user_id);
            tweetUserName=(TextView) itemView.findViewById(R.id.tweet_user_name);
            tweetContext=(TextView) itemView.findViewById(R.id.tweet_content);
            tweetTimeStampText=(TextView) itemView.findViewById(R.id.tweet_time_stamp_text);
            tweetQuoteTweetLayout=(RelativeLayout)  itemView.findViewById(R.id.tweet_quote_tweet);
            tweetQuoteTweetUserName=(TextView) itemView.findViewById(R.id.tweet_quote_tweet_user_name);
            tweetQuoteTweetUserId=(TextView) itemView.findViewById(R.id.tweet_quote_tweet_user_id);
            tweetQuoteTweetContext=(TextView) itemView.findViewById(R.id.tweet_quote_tweet_content);
            tweetImageTableView=(TweetImageTableView) itemView.findViewById(R.id.tweet_image_container);



            timeSpanConverter=new TimeSpanConverter();
        }
    }
}
