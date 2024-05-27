package com.example.labolatorium4;

import android.os.Parcel;
import android.os.Parcelable;

public class PostepInfo implements Parcelable {
    public int mPobranychBajtow;
    public int mRozmiar;
    public String mStatus;

    public PostepInfo(int mPobranychBajtow, int mRozmiar, String mStatus) {
        this.mPobranychBajtow = mPobranychBajtow;
        this.mRozmiar = mRozmiar;
        this.mStatus = mStatus;
    }

    protected PostepInfo(Parcel in) {
        mPobranychBajtow = in.readInt();
        mRozmiar = in.readInt();
        mStatus = in.readString();
    }

    public static final Creator<PostepInfo> CREATOR = new Creator<PostepInfo>() {
        @Override
        public PostepInfo createFromParcel(Parcel in) {
            return new PostepInfo(in);
        }

        @Override
        public PostepInfo[] newArray(int size) {
            return new PostepInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mPobranychBajtow);
        dest.writeInt(mRozmiar);
        dest.writeString(mStatus);
    }
}
