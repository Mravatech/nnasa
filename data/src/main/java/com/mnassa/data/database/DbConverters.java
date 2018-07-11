package com.mnassa.data.database;

import android.arch.persistence.room.TypeConverter;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Peter on 7/11/2018.
 */
public class DbConverters {
    @TypeConverter
    @Nullable
    public static Date longToDate(@Nullable Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    @Nullable
    public static Long dateToLong(@Nullable Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    @Nullable
    public static List<String> stringToStringList(@Nullable String value) {
        if (TextUtils.isEmpty(value)) return null;

        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();
        return GsonHolder.GSON.fromJson(value, listType);
    }

    @TypeConverter
    @Nullable
    public static String stringListToString(@Nullable List<String> data) {
        return data == null ? null : GsonHolder.GSON.toJson(data);
    }

    private static class GsonHolder {
        private static final Gson GSON = new Gson();
    }

}
