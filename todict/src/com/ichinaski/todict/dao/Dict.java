package com.ichinaski.todict.dao;

public class Dict {
    private long mID;
    private String mName;
    
    public Dict (long id, String name) {
        mID = id;
        mName = name;
    }
    
    public long getID() {
        return mID;
    }
    
    public String getName() {
        return mName;
    }

}
