package com.umutflash.openactivity;

import com.umutflash.openactivity.data.model.Spot;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SpotAPI {
    @POST("{new}.json")
    Call<Spot> setData(@Path("new") String s1, @Body Spot spot);
}
