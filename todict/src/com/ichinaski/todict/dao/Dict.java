package com.ichinaski.todict.dao;

public class Dict {
    private long mID;
    private String mLang1, mLang2;
    
    public Dict (long id, String lang1, String lang2) {
        mID = id;
        mLang1 = lang1;
        mLang2 = lang2;
    }
    
    public long getID() {
        return mID;
    }
    
    public String getLang1() {
        return mLang1;
    }
    
    public String getLang2() {
        return mLang2;
    }
    
    @Override
    public String toString() {
        return mLang1 + " - " + mLang2;
    }

}
