package com.eebbk.bfc.download.demo.util;

import java.util.HashMap;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-11-01 21:54
 * Email: jacklulu29@gmail.com
 */

public class ErrorCodeUtil {

    private static HashMap<String, String> sErrorCodeMap = new HashMap<>();

    static {
        sErrorCodeMap.put("03010001", "下载未知异常");
        sErrorCodeMap.put("03010002", "用户手动暂停下载");
        sErrorCodeMap.put("03010003", "其他原因暂停下载");
        sErrorCodeMap.put("03010004", "将要运行时下载状态不为等待状态");
        sErrorCodeMap.put("03010005", "权限清单没有添加网络权限");
        sErrorCodeMap.put("03010006", "运行任务对象为空");
        sErrorCodeMap.put("03010007", "网络没有连接，请检测网络是否畅通");
        sErrorCodeMap.put("03010008", "由于文件超过大小不能使用网络");
        sErrorCodeMap.put("03010009", "由于文件超过大小不建议使用网络");
        sErrorCodeMap.put("03010010", "下载任务没有使用当前网络的权限，如需继续请增加相应网络使用权限");
        sErrorCodeMap.put("03010011", "网络堵塞");
        sErrorCodeMap.put("03010012", "缓存文件路径为空");
        sErrorCodeMap.put("03010013", "缓存文件被文件夹占用");
        sErrorCodeMap.put("03010014", "创建缓存文件错误");
        sErrorCodeMap.put("03010015", "创建缓存文件IO异常");
        sErrorCodeMap.put("03010016", "创建的缓存文件没有找到");
        sErrorCodeMap.put("03010017", "存储空间不足");
        sErrorCodeMap.put("03010018", "跳到指定文件指定位置时发生IO异常");
        sErrorCodeMap.put("03010019", "跳到指定文件指定位置时参数错误");
        sErrorCodeMap.put("03010020", "设置文件大小时参数错误");
        sErrorCodeMap.put("03010021", "设置文件大小时发生IO异常");
        sErrorCodeMap.put("03010022", "下载完成，但是下载文件真实大小不等于总大小");
        sErrorCodeMap.put("03010023", "读写文件发生IO异常");
        sErrorCodeMap.put("03010024", "删除目标文件错误");
        sErrorCodeMap.put("03010025", "重命名文件错误");
        sErrorCodeMap.put("03010026", "发生RemoteException错误");
        sErrorCodeMap.put("03010027", "下载文件大小超过总大小");
        sErrorCodeMap.put("03010028", "查询任务时cursor为空");
        sErrorCodeMap.put("03010029", "网络不允许使用漫游");
        sErrorCodeMap.put("03010030", "下载连接超时");
        sErrorCodeMap.put("03010031", "保存文件夹被文件占用");
        sErrorCodeMap.put("03010032", "创建保存路径错误");
        sErrorCodeMap.put("03010033", "重启服务恢复查询任务时cursor为空");
        sErrorCodeMap.put("03010034", "文件名长度超过255");
        sErrorCodeMap.put("03010035", "文件名称含有非法字符：/");
        sErrorCodeMap.put("03010036", "连接异常");
        sErrorCodeMap.put("03010037", "下载的缓存文件不存在，是否已经被移除");
        sErrorCodeMap.put("03010038", "获取网络状态信息失败，Context为空");
        sErrorCodeMap.put("03010039", "未知的网络状态错误类型");
        sErrorCodeMap.put("03010040", "已设置禁止在电信2g网络下进行下载");

        sErrorCodeMap.put("03020001", "校验未知异常");
        sErrorCodeMap.put("03020002", "校验器没有找到");
        sErrorCodeMap.put("03020003", "校验key无效");
        sErrorCodeMap.put("03020004", "校验错误");
        sErrorCodeMap.put("03020005", "校验运行时异常");
        sErrorCodeMap.put("03020006", "校验失败");

        sErrorCodeMap.put("03030001", "解压未知异常");
        sErrorCodeMap.put("03030002", "解压器为空");
        sErrorCodeMap.put("03030003", "不支持解压此文件");
        sErrorCodeMap.put("03030004", "解压错误");
        sErrorCodeMap.put("03010005", "解压运行时异常");
        sErrorCodeMap.put("03010006", "解压源文件路径是空的");

        sErrorCodeMap.put("03040001", "获取版本信息未知异常");

        sErrorCodeMap.put("03050001", "其他未知异常");

    }

    private ErrorCodeUtil() {
        // private construct
    }

    public static String getErrorStr(String errorCode){
        if(errorCode == null){
            return null;
        }
        if(sErrorCodeMap.containsKey(errorCode)){
            return sErrorCodeMap.get(errorCode);
        } else {
            String result = "";
            if(errorCode.startsWith("0301")){
                result += "下载异常：" + errorCode;
            } else if(errorCode.startsWith("0302")){
                result += "校验异常：" + errorCode;
            } else if(errorCode.startsWith("0303")){
                result += "解压异常：" + errorCode;
            } else if(errorCode.startsWith("0304")){
                result += "版本信息异常：";
            } else if(errorCode.startsWith("0305")){
                result += "其他异常：" + errorCode;
            }
        }
        return  " 未知原因 ";
    }

}
