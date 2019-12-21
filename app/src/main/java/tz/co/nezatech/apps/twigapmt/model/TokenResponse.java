package tz.co.nezatech.apps.twigapmt.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class TokenResponse implements Parcelable {
    @SerializedName("access_token")
    String accessToken;
    @SerializedName("expires_in")
    long expiresIn;
    @SerializedName("token_type")
    String tokenType;
    @SerializedName("scope")
    String scope;
    @SerializedName("refresh_token")
    String refreshToken;

    public TokenResponse() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.expiresIn);
        dest.writeString(this.accessToken);
        dest.writeString(this.accessToken);
        dest.writeString(this.accessToken);
    }
}
