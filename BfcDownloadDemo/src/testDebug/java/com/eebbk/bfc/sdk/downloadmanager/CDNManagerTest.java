package com.eebbk.bfc.sdk.downloadmanager;

import com.eebbk.bfc.bfclog.BfcLog;
import com.eebbk.bfc.sdk.download.util.LogUtil;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by lzy on 10/31/2018.
 */
public class CDNManagerTest {

    private List<String> xyUrls = new ArrayList<>();
    private List<String> originalUrls = new ArrayList<>();
    private List<String> fakeString = new ArrayList<>();

    @Before
    public void setUp() {
        BfcLog sBfcLog = new BfcLog.Builder().showLog(false).build();
        LogUtil.setLog(sBfcLog, false);
        CDNManager.initXYVodSDK(true);

        xyUrls.add("http://127.0.0.1:2021/apk-oppo.eebbk.net/apk-oppo/2018/08/27/030906394_4142e3ebd5bd5571.apk?xyop=3");
        originalUrls.add("https://apk-oppo.eebbk.net/apk-oppo/2018/08/27/030906394_4142e3ebd5bd5571.apk");
        xyUrls.add("http://127.0.0.1:2021/down.eebbk.net/xzzc/yydd/Tc62ddUEkEU0FkWwhsZraq8qiNW7D8bn/d4人教PEP小学英语三年级上册(三年级起点)(V2.0)-6.tia?xyop=1");
        originalUrls.add("http://down.eebbk.net/xzzc/yydd/Tc62ddUEkEU0FkWwhsZraq8qiNW7D8bn/d4人教PEP小学英语三年级上册(三年级起点)(V2.0)-6.tia");

        xyUrls.add("http://127.0.0.1:2021/down.eebbk.net/xzzc/yydd/Tc62ddUEkEU0FkWwhsZraq8qiNW7D8bn/d4人教PEP小学英语三年级上册(三年级起点)(V2.0)-6.tia?xyop=download");
        originalUrls.add("http://down.eebbk.net/xzzc/yydd/Tc62ddUEkEU0FkWwhsZraq8qiNW7D8bn/d4人教PEP小学英语三年级上册(三年级起点)(V2.0)-6.tia");

        fakeString.add("");
        fakeString.add("null");
        fakeString.add("zhognerngalvnaligervlnadflgkvjaerlgikje");
        fakeString.add("http://");
    }

    @Test
    public void url_REWRITE() {
//        for (int index = 0; index < originalUrls.size(); index++) {
//            assertEquals("transformSourceUrl test", xyUrls.get(index), CDNManager.url_REWRITE(originalUrls.get(index)));
//        }

        for (int index = 0; index < fakeString.size(); index++) {
            assertEquals("transformSourceUrl test", fakeString.get(index), CDNManager.url_REWRITE(fakeString.get(index)));
        }
    }

    @Test
    public void transformSourceUrl() {
        for (int index = 0; index < xyUrls.size(); index++) {
            assertEquals("transformSourceUrl test", originalUrls.get(index), CDNManager.transformSourceUrl(xyUrls.get(index)));
        }

        for (int index = 0; index < fakeString.size(); index++) {
            assertEquals("transformSourceUrl test", fakeString.get(index), CDNManager.transformSourceUrl(fakeString.get(index)));
        }
    }

    @Test
    public void isXYVodUrl() {
        for (int index = 0; index < xyUrls.size(); index++) {
            assertTrue("isXYVodUrl test", CDNManager.isXYVodUrl(xyUrls.get(index)));
        }

        for (int index = 0; index < originalUrls.size(); index++) {
            assertFalse("isXYVodUrl test", CDNManager.isXYVodUrl(originalUrls.get(index)));
        }

        for (int index = 0; index < fakeString.size(); index++) {
            assertFalse("isXYVodUrl test", CDNManager.isXYVodUrl(fakeString.get(index)));
        }
    }
}