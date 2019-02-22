package com.mnassa.data.database;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

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

    /**
     * Read the object from Base64 string.
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T fromString(String string) throws IOException, ClassNotFoundException {
        byte[] data = Base64.decode(string, 0);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data))) {
            Object o = objectInputStream.readObject();
            objectInputStream.close();
            return (T) o;
        }
    }

    /**
     * Write the object to a Base64 string.
     */
    @SuppressLint("NewApi")
    public static String toString(Serializable o) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(o);
            objectOutputStream.close();
            return Base64.encodeToString(byteArrayOutputStream.toByteArray(), 0);
        }
    }

}
