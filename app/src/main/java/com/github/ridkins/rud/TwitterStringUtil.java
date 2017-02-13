package com.github.ridkins.rud;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;

import java.util.List;

import rx.Observable;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;



public class TwitterStringUtil {

    @NonNull
    public static String plusAtMark(String string){
        return "@" + string;
    }

    public static CharSequence getLinkedSequence(Status item, Context mContext){

        String tweet = item.getText();

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(tweet);

        for (SymbolEntity symbolEntity : item.getSymbolEntities()) {
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {

                }
            }, tweet.offsetByCodePoints(0,symbolEntity.getStart()), tweet.offsetByCodePoints(0,symbolEntity.getEnd()), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        for (HashtagEntity hashtagEntity : item.getHashtagEntities()) {
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {

                }
            }, tweet.offsetByCodePoints(0,hashtagEntity.getStart()), tweet.offsetByCodePoints(0,hashtagEntity.getEnd()), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        for (UserMentionEntity userMentionEntity : item.getUserMentionEntities()) {
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View view) {


                }
            }, tweet.offsetByCodePoints(0,userMentionEntity.getStart()), tweet.offsetByCodePoints(0,userMentionEntity.getEnd()), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        List<URLEntity> urlEntities=Observable
                .concat(Observable.from(item.getURLEntities()),
                        Observable.create(
                                subscriber -> {
                                    if(item.getMediaEntities().length>0){
                                        subscriber.onNext(item.getMediaEntities()[0]);
                                    }
                                    subscriber.onCompleted();
                                }
                        ))
                .toSortedList((i1,i2)->i1.getStart()-i2.getStart())
                .toBlocking()
                .single();

        int sp = 0;

        for (URLEntity entity : urlEntities) {
            String url = entity.getURL();
            String expandedUrl = entity.getExpandedURL();
            String displayUrl = entity.getDisplayURL();

            int urlLength = url.codePointCount(0, url.length());
            int displayUrlLength = displayUrl.codePointCount(0, displayUrl.length());
            int dusp = displayUrlLength - urlLength;
            spannableStringBuilder.replace(tweet.offsetByCodePoints(0,entity.getStart()) + sp, tweet.offsetByCodePoints(0,entity.getEnd()) + sp, displayUrl);
            spannableStringBuilder.setSpan(new URLSpan(expandedUrl), tweet.offsetByCodePoints(0,entity.getStart()) + sp, tweet.offsetByCodePoints(0,entity.getEnd()) + sp + dusp, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            sp+=dusp;
        }

        return spannableStringBuilder;

    }

}
